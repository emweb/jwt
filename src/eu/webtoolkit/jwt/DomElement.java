/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.lang.ref.*;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.http.*;
import javax.servlet.*;
import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.utils.*;
import eu.webtoolkit.jwt.servlet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to represent a DOM element.
 * <p>
 * 
 * This class is for internal use only.
 */
public class DomElement {
	private static Logger logger = LoggerFactory.getLogger(DomElement.class);

	enum Mode {
		ModeCreate, ModeUpdate;

		/**
		 * Returns the numerical representation of this enum.
		 */
		public int getValue() {
			return ordinal();
		}
	}

	public DomElement(DomElement.Mode mode, DomElementType type) {
		this.mode_ = mode;
		this.wasEmpty_ = this.mode_ == DomElement.Mode.ModeCreate;
		this.removeAllChildren_ = -1;
		this.minMaxSizeProperties_ = false;
		this.unstubbed_ = false;
		this.unwrapped_ = false;
		this.replaced_ = null;
		this.insertBefore_ = null;
		this.type_ = type;
		this.id_ = "";
		this.numManipulations_ = 0;
		this.timeOut_ = -1;
		this.javaScript_ = new EscapeOStream();
		this.javaScriptEvenWhenDeleted_ = "";
		this.var_ = "";
		this.attributes_ = new HashMap<String, String>();
		this.properties_ = new TreeMap<Property, String>();
		this.eventHandlers_ = new HashMap<String, DomElement.EventHandler>();
		this.childrenToAdd_ = new ArrayList<DomElement.ChildInsertion>();
		this.childrenToSave_ = new ArrayList<String>();
		this.updatedChildren_ = new ArrayList<DomElement>();
		this.childrenHtml_ = new EscapeOStream();
		this.timeouts_ = new ArrayList<DomElement.TimeoutEvent>();
		this.discardWithParent_ = true;
	}

	public static String urlEncodeS(String url) {
		return urlEncodeS(url, "");
	}

	public static String urlEncodeS(String url, String allowed) {
		StringWriter result = new StringWriter();
		for (int i = 0; i < url.length(); ++i) {
			char c = url.charAt(i);
			if (c <= 31 || c >= 127 || unsafeChars_.indexOf(c) != -1) {
				if (allowed.indexOf(c) != -1) {
					result.write(c);
				} else {
					result.append('%');
					if ((int) c < 16) {
						result.append('0');
					}
					result.append(Integer.toHexString(c));
				}
			} else {
				result.write(c);
			}
		}
		return result.toString();
	}

	public DomElement.Mode getMode() {
		return this.mode_;
	}

	public void setType(DomElementType type) {
		this.type_ = type;
	}

	public DomElementType getType() {
		return this.type_;
	}

	public static DomElement createNew(DomElementType type) {
		DomElement e = new DomElement(DomElement.Mode.ModeCreate, type);
		return e;
	}

	public static DomElement getForUpdate(String id, DomElementType type) {
		if (id.length() == 0) {
			throw new WException("Cannot update widget without id");
		}
		DomElement e = new DomElement(DomElement.Mode.ModeUpdate, type);
		e.id_ = id;
		return e;
	}

	public static DomElement getForUpdate(WObject object, DomElementType type) {
		return getForUpdate(object.getId(), type);
	}

	public static DomElement updateGiven(String var, DomElementType type) {
		DomElement e = new DomElement(DomElement.Mode.ModeUpdate, type);
		e.var_ = var;
		return e;
	}

	public String getVar() {
		return this.var_;
	}

	public void setWasEmpty(boolean how) {
		this.wasEmpty_ = how;
	}

	public void addChild(DomElement child) {
		if (child.getMode() == DomElement.Mode.ModeCreate) {
			++this.numManipulations_;
			if (this.wasEmpty_
					&& this.canWriteInnerHTML(WApplication.getInstance())) {
				child.asHTML(this.childrenHtml_, this.javaScript_,
						this.timeouts_);
				;
			} else {
				this.childrenToAdd_
						.add(new DomElement.ChildInsertion(-1, child));
			}
		} else {
			this.updatedChildren_.add(child);
		}
	}

	public void insertChildAt(DomElement child, int pos) {
		++this.numManipulations_;
		this.childrenToAdd_.add(new DomElement.ChildInsertion(pos, child));
	}

	public void saveChild(String id) {
		this.childrenToSave_.add(id);
	}

	public void setAttribute(String attribute, String value) {
		++this.numManipulations_;
		this.attributes_.put(attribute, value);
	}

	public String getAttribute(String attribute) {
		String i = this.attributes_.get(attribute);
		if (i != null) {
			return i;
		} else {
			return "";
		}
	}

	public void removeAttribute(String attribute) {
		this.attributes_.remove(attribute);
	}

	public void setProperty(Property property, String value) {
		++this.numManipulations_;
		this.properties_.put(property, value);
		if (property.getValue() >= Property.PropertyStyleMinWidth.getValue()
				&& property.getValue() <= Property.PropertyStyleMaxHeight
						.getValue()) {
			this.minMaxSizeProperties_ = true;
		}
	}

	public String getProperty(Property property) {
		String i = this.properties_.get(property);
		if (i != null) {
			return i;
		} else {
			return "";
		}
	}

	public void removeProperty(Property property) {
		this.properties_.remove(property);
	}

	public void setProperties(SortedMap<Property, String> properties) {
		for (Iterator<Map.Entry<Property, String>> i_it = properties.entrySet()
				.iterator(); i_it.hasNext();) {
			Map.Entry<Property, String> i = i_it.next();
			this.setProperty(i.getKey(), i.getValue());
		}
	}

	public SortedMap<Property, String> getProperties() {
		return this.properties_;
	}

	public void clearProperties() {
		this.numManipulations_ -= this.properties_.size();
		this.properties_.clear();
	}

	public void setEventSignal(String eventName, AbstractEventSignal signal) {
		this.setEvent(eventName, signal.getJavaScript(), signal.encodeCmd(),
				signal.isExposedSignal());
	}

	public void setEvent(String eventName, String jsCode, String signalName,
			boolean isExposed) {
		WApplication app = WApplication.getInstance();
		boolean anchorClick = this.getType() == DomElementType.DomElement_A
				&& eventName == WInteractWidget.CLICK_SIGNAL;
		StringBuilder js = new StringBuilder();
		if (isExposed || anchorClick || jsCode.length() != 0) {
			if (app.getEnvironment().agentIsIEMobile()) {
				js.append("var e=window.event,");
			} else {
				js.append("var e=event||window.event,");
			}
			js.append("o=this;");
			if (anchorClick) {
				js
						.append("if(e.ctrlKey||e.metaKey||(Wt3_2_3.button(e) > 1))return true;else{");
			}
			js.append(jsCode);
			if (isExposed) {
				js.append(app.getJavaScriptClass()).append("._p_.update(o,'")
						.append(signalName).append("',e,true);");
			}
			if (anchorClick) {
				js.append("}");
			}
		}
		++this.numManipulations_;
		this.eventHandlers_.put(eventName, new DomElement.EventHandler(js
				.toString(), signalName));
	}

	public final void setEvent(String eventName, String jsCode,
			String signalName) {
		setEvent(eventName, jsCode, signalName, false);
	}

	public void setEvent(String eventName, String jsCode) {
		this.eventHandlers_.put(eventName, new DomElement.EventHandler(jsCode,
				""));
	}

	public void addEvent(String eventName, String jsCode) {
		this.eventHandlers_.get(eventName).jsCode += jsCode;
	}

	static class EventAction {
		private static Logger logger = LoggerFactory
				.getLogger(EventAction.class);

		public String jsCondition;
		public String jsCode;
		public String updateCmd;
		public boolean exposed;

		public EventAction(String aJsCondition, String aJsCode,
				String anUpdateCmd, boolean anExposed) {
			this.jsCondition = aJsCondition;
			this.jsCode = aJsCode;
			this.updateCmd = anUpdateCmd;
			this.exposed = anExposed;
		}
	}

	public void setEvent(String eventName, List<DomElement.EventAction> actions) {
		StringBuilder code = new StringBuilder();
		for (int i = 0; i < actions.size(); ++i) {
			if (actions.get(i).jsCondition.length() != 0) {
				code.append("if(").append(actions.get(i).jsCondition).append(
						"){");
			}
			code.append(actions.get(i).jsCode);
			if (actions.get(i).exposed) {
				code.append(WApplication.getInstance().getJavaScriptClass())
						.append("._p_.update(o,'").append(
								actions.get(i).updateCmd).append("',e,true);");
			}
			if (actions.get(i).jsCondition.length() != 0) {
				code.append("}");
			}
		}
		this.setEvent(eventName, code.toString(), "");
	}

	public void setId(String id) {
		++this.numManipulations_;
		this.id_ = id;
	}

	public void setName(String name) {
		++this.numManipulations_;
		this.id_ = name;
		this.setAttribute("name", name);
	}

	public void setTimeout(int msec, boolean jsRepeat) {
		++this.numManipulations_;
		this.timeOut_ = msec;
		this.timeOutJSRepeat_ = jsRepeat;
	}

	public void callMethod(String method) {
		++this.numManipulations_;
		if (this.var_.length() == 0) {
			this.javaScript_.append("Wt3_2_3").append(".$('").append(this.id_)
					.append("').");
		} else {
			this.javaScript_.append(this.var_).append('.');
		}
		this.javaScript_.append(method).append(";\n");
	}

	public void callJavaScript(String jsCode, boolean evenWhenDeleted) {
		++this.numManipulations_;
		if (!evenWhenDeleted) {
			this.javaScript_.append(jsCode).append('\n');
		} else {
			this.javaScriptEvenWhenDeleted_ += jsCode;
		}
	}

	public final void callJavaScript(String jsCode) {
		callJavaScript(jsCode, false);
	}

	public String getId() {
		return this.id_;
	}

	public void removeAllChildren(int firstChild) {
		++this.numManipulations_;
		this.removeAllChildren_ = firstChild;
		this.wasEmpty_ = firstChild == 0;
	}

	public final void removeAllChildren() {
		removeAllChildren(0);
	}

	public void removeFromParent() {
		this.callJavaScript("Wt3_2_3.remove('" + this.getId() + "');", true);
	}

	public void replaceWith(DomElement newElement) {
		++this.numManipulations_;
		this.replaced_ = newElement;
	}

	public void unstubWith(DomElement newElement, boolean hideWithDisplay) {
		this.replaceWith(newElement);
		this.unstubbed_ = true;
		this.hideWithDisplay_ = hideWithDisplay;
	}

	public void insertBefore(DomElement sibling) {
		++this.numManipulations_;
		this.insertBefore_ = sibling;
	}

	public void unwrap() {
		++this.numManipulations_;
		this.unwrapped_ = true;
	}

	public void setDiscardWithParent(boolean discard) {
		this.discardWithParent_ = discard;
	}

	public boolean discardWithParent() {
		return this.discardWithParent_;
	}

	enum Priority {
		Delete, Create, Update;

		/**
		 * Returns the numerical representation of this enum.
		 */
		public int getValue() {
			return ordinal();
		}
	}

	static class TimeoutEvent {
		private static Logger logger = LoggerFactory
				.getLogger(TimeoutEvent.class);

		public int msec;
		public String event;
		public boolean repeat;

		public TimeoutEvent() {
			this.event = "";
		}

		public TimeoutEvent(int m, String e, boolean r) {
			this.msec = m;
			this.event = e;
			this.repeat = r;
		}
	}

	public void asJavaScript(Writer out) {
		this.mode_ = DomElement.Mode.ModeUpdate;
		EscapeOStream eout = new EscapeOStream(out);
		this.declare(eout);
		eout.append(this.var_).append(".setAttribute('id', '").append(this.id_)
				.append("');\n");
		this.mode_ = DomElement.Mode.ModeCreate;
		this.setJavaScriptProperties(eout, WApplication.getInstance());
		this.setJavaScriptAttributes(eout);
		this.asJavaScript(eout, DomElement.Priority.Update);
	}

	public String asJavaScript(EscapeOStream out, DomElement.Priority priority) {
		switch (priority) {
		case Delete:
			if (this.javaScriptEvenWhenDeleted_.length() != 0
					|| this.removeAllChildren_ >= 0) {
				out.append(this.javaScriptEvenWhenDeleted_);
				if (this.removeAllChildren_ >= 0) {
					this.declare(out);
					if (this.removeAllChildren_ == 0) {
						out.append(this.var_).append(".innerHTML='';\n");
					} else {
						out.append("$(").append(this.var_).append(
								").children(':gt(").append(
								this.removeAllChildren_ - 1).append(
								")').remove();");
					}
				}
			}
			return this.var_;
		case Create:
			if (this.mode_ == DomElement.Mode.ModeCreate) {
				if (this.id_.length() != 0) {
					out.append(this.var_).append(".setAttribute('id', '")
							.append(this.id_).append("');\n");
				}
				this.setJavaScriptAttributes(out);
				this.setJavaScriptProperties(out, WApplication.getInstance());
			}
			return this.var_;
		case Update: {
			WApplication app = WApplication.getInstance();
			boolean childrenUpdated = false;
			if (this.mode_ == DomElement.Mode.ModeUpdate
					&& this.numManipulations_ == 1) {
				for (int i = 0; i < this.updatedChildren_.size(); ++i) {
					DomElement child = this.updatedChildren_.get(i);
					child.asJavaScript(out, DomElement.Priority.Update);
				}
				childrenUpdated = true;
				if (this.properties_.get(Property.PropertyStyleDisplay) != null) {
					String style = this.properties_
							.get(Property.PropertyStyleDisplay);
					if (style.equals("none")) {
						out.append("Wt3_2_3.hide('").append(this.id_).append(
								"');\n");
						return this.var_;
					} else {
						if (style.length() == 0) {
							out.append("Wt3_2_3.show('").append(this.id_)
									.append("');\n");
							return this.var_;
						} else {
							if (style.equals("inline")) {
								out.append("Wt3_2_3.inline('" + this.id_
										+ "');\n");
								return this.var_;
							} else {
								if (style.equals("block")) {
									out.append("Wt3_2_3.block('" + this.id_
											+ "');\n");
									return this.var_;
								}
							}
						}
					}
				}
			}
			if (this.unwrapped_) {
				out.append("Wt3_2_3.unwrap('").append(this.id_).append("');\n");
			}
			this.processEvents(app);
			this.processProperties(app);
			if (this.replaced_ != null) {
				this.declare(out);
				String varr = this.replaced_.getCreateVar();
				StringBuilder insertJs = new StringBuilder();
				insertJs.append(this.var_).append(".parentNode.replaceChild(")
						.append(varr).append(',').append(this.var_).append(
								");\n");
				this.replaced_.createElement(out, app, insertJs.toString());
				if (this.unstubbed_) {
					out.append("Wt3_2_3.unstub(").append(this.var_).append(',')
							.append(varr).append(',').append(
									this.hideWithDisplay_ ? 1 : 0).append(
									");\n");
				}
				return this.var_;
			} else {
				if (this.insertBefore_ != null) {
					this.declare(out);
					String varr = this.insertBefore_.getCreateVar();
					StringBuilder insertJs = new StringBuilder();
					insertJs.append(this.var_).append(
							".parentNode.insertBefore(").append(varr).append(
							",").append(this.var_ + ");\n");
					this.insertBefore_.createElement(out, app, insertJs
							.toString());
					return this.var_;
				}
			}
			for (int i = 0; i < this.childrenToSave_.size(); ++i) {
				this.declare(out);
				out.append("var c").append(this.var_).append((int) i).append(
						'=').append("$('#").append(this.childrenToSave_.get(i))
						.append("')");
				if (app.getEnvironment().agentIsIE()) {
					out.append(".remove()");
				}
				out.append(";");
			}
			if (this.mode_ != DomElement.Mode.ModeCreate) {
				this.setJavaScriptProperties(out, app);
				this.setJavaScriptAttributes(out);
			}
			for (Iterator<Map.Entry<String, DomElement.EventHandler>> i_it = this.eventHandlers_
					.entrySet().iterator(); i_it.hasNext();) {
				Map.Entry<String, DomElement.EventHandler> i = i_it.next();
				if (this.mode_ == DomElement.Mode.ModeUpdate
						|| i.getValue().jsCode.length() != 0) {
					this.setJavaScriptEvent(out, i.getKey(), i.getValue(), app);
				}
			}
			this.renderInnerHtmlJS(out, app);
			for (int i = 0; i < this.childrenToSave_.size(); ++i) {
				out.append("$('#").append(this.childrenToSave_.get(i)).append(
						"').replaceWith(c").append(this.var_).append((int) i)
						.append(");");
			}
			if (!childrenUpdated) {
				for (int i = 0; i < this.updatedChildren_.size(); ++i) {
					DomElement child = this.updatedChildren_.get(i);
					child.asJavaScript(out, DomElement.Priority.Update);
				}
			}
			return this.var_;
		}
		}
		return this.var_;
	}

	public void asHTML(EscapeOStream out, EscapeOStream javaScript,
			List<DomElement.TimeoutEvent> timeouts, boolean openingTagOnly) {
		if (this.mode_ != DomElement.Mode.ModeCreate) {
			throw new WException("DomElement::asHTML() called with ModeUpdate");
		}
		WApplication app = WApplication.getInstance();
		this.processEvents(app);
		this.processProperties(app);
		DomElement.EventHandler clickEvent = this.eventHandlers_
				.get(WInteractWidget.CLICK_SIGNAL);
		boolean needButtonWrap = !app.getEnvironment().hasAjax()
				&& clickEvent != null && clickEvent.jsCode.length() != 0
				&& !app.getEnvironment().agentIsSpiderBot();
		boolean isSubmit = needButtonWrap;
		DomElementType renderedType = this.type_;
		if (needButtonWrap) {
			if (this.type_ == DomElementType.DomElement_BUTTON) {
				DomElement self = this;
				self.setAttribute("type", "submit");
				self.setAttribute("name", "signal=" + clickEvent.signalName);
				needButtonWrap = false;
			} else {
				if (this.type_ == DomElementType.DomElement_IMG) {
					renderedType = DomElementType.DomElement_INPUT;
					DomElement self = this;
					self.setAttribute("type", "image");
					self
							.setAttribute("name", "signal="
									+ clickEvent.signalName);
					needButtonWrap = false;
				}
			}
		}
		if (needButtonWrap) {
			String i = this.properties_.get(Property.PropertyStyleDisplay);
			if (i != null && i.equals("none")) {
				return;
			}
		}
		if (needButtonWrap) {
			if (this.type_ == DomElementType.DomElement_AREA
					|| this.type_ == DomElementType.DomElement_INPUT
					|| this.type_ == DomElementType.DomElement_SELECT) {
				needButtonWrap = false;
			}
			if (this.type_ == DomElementType.DomElement_A) {
				String href = this.getAttribute("href");
				if (app.getEnvironment().agentIsIE()
						&& app.getEnvironment().getAgent() != WEnvironment.UserAgent.IE6
						|| !href.equals("#")) {
					needButtonWrap = false;
				}
			} else {
				if (this.type_ == DomElementType.DomElement_AREA) {
					DomElement self = this;
					self.setAttribute("href", app.url(app.getInternalPath())
							+ "&signal=" + clickEvent.signalName);
				}
			}
		}
		final boolean isIEMobile = app.getEnvironment().agentIsIEMobile();
		final boolean supportButton = !isIEMobile;
		boolean needAnchorWrap = false;
		if (!needButtonWrap) {
			if (isIEMobile
					&& app.getEnvironment().hasAjax()
					&& clickEvent != null
					&& clickEvent.jsCode.length() != 0
					&& (this.type_ == DomElementType.DomElement_IMG
							|| this.type_ == DomElementType.DomElement_SPAN || this.type_ == DomElementType.DomElement_DIV)) {
				needAnchorWrap = true;
			}
		}
		if (!supportButton && this.type_ == DomElementType.DomElement_BUTTON) {
			renderedType = DomElementType.DomElement_INPUT;
			DomElement self = this;
			if (!isSubmit) {
				self.setAttribute("type", "button");
			}
			self.setAttribute("value", this.properties_
					.get(Property.PropertyInnerHTML));
			self.setProperty(Property.PropertyInnerHTML, "");
		}
		EscapeOStream attributeValues = out.push();
		attributeValues.pushEscape(EscapeOStream.RuleSet.HtmlAttribute);
		String style = "";
		if (needButtonWrap) {
			if (supportButton) {
				out.append("<button type=\"submit\" name=\"signal=");
				out.append(clickEvent.signalName, attributeValues);
				out.append("\" class=\"Wt-wrap ");
				String l = this.properties_.get(Property.PropertyClass);
				if (l != null) {
					out.append(l);
					SortedMap<Property, String> map = this.properties_;
					map.remove(Property.PropertyClass);
				}
				out.append('"');
				String wrapStyle = this.getCssStyle();
				if (!this.isDefaultInline()) {
					wrapStyle += "display: block;";
				}
				if (wrapStyle.length() != 0) {
					out.append(" style=");
					fastHtmlAttributeValue(out, attributeValues, wrapStyle);
				}
				String i = this.properties_.get(Property.PropertyDisabled);
				if (i != null && i.equals("true")) {
					out.append(" disabled=\"disabled\"");
				}
				if (app.getEnvironment().getAgent() != WEnvironment.UserAgent.Konqueror
						&& !app.getEnvironment().agentIsWebKit()
						&& !app.getEnvironment().agentIsIE()) {
					style = "margin: 0px -3px -2px -3px;";
				}
				out.append("><").append(elementNames_[renderedType.getValue()]);
			} else {
				if (this.type_ == DomElementType.DomElement_IMG) {
					out.append("<input type=\"image\"");
				} else {
					out.append("<input type=\"submit\"");
				}
				out.append(" name=");
				fastHtmlAttributeValue(out, attributeValues, "signal="
						+ clickEvent.signalName);
				out.append(" value=");
				String i = this.properties_.get(Property.PropertyInnerHTML);
				if (i != null) {
					fastHtmlAttributeValue(out, attributeValues, i);
				} else {
					out.append("\"\"");
				}
			}
		} else {
			if (needAnchorWrap) {
				out.append("<a href=\"#\" class=\"Wt-wrap\" onclick=");
				fastHtmlAttributeValue(out, attributeValues, clickEvent.jsCode);
				out.append("><").append(elementNames_[renderedType.getValue()]);
			} else {
				out.append("<").append(elementNames_[renderedType.getValue()]);
			}
		}
		if (this.id_.length() != 0) {
			out.append(" id=");
			fastHtmlAttributeValue(out, attributeValues, this.id_);
		}
		for (Iterator<Map.Entry<String, String>> i_it = this.attributes_
				.entrySet().iterator(); i_it.hasNext();) {
			Map.Entry<String, String> i = i_it.next();
			if (!app.getEnvironment().agentIsSpiderBot()
					|| !i.getKey().equals("name")) {
				out.append(" ").append(i.getKey()).append("=");
				fastHtmlAttributeValue(out, attributeValues, i.getValue());
			}
		}
		if (app.getEnvironment().hasAjax()) {
			for (Iterator<Map.Entry<String, DomElement.EventHandler>> i_it = this.eventHandlers_
					.entrySet().iterator(); i_it.hasNext();) {
				Map.Entry<String, DomElement.EventHandler> i = i_it.next();
				if (i.getValue().jsCode.length() != 0) {
					if (this.id_.equals(app.getDomRoot().getId())
							|| i.getKey() == WInteractWidget.MOUSE_WHEEL_SIGNAL
							&& app.getEnvironment().agentIsGecko()) {
						this.setJavaScriptEvent(javaScript, i.getKey(), i
								.getValue(), app);
					} else {
						out.append(" on").append(i.getKey()).append("=");
						fastHtmlAttributeValue(out, attributeValues, i
								.getValue().jsCode);
					}
				}
			}
		}
		String innerHTML = "";
		for (Iterator<Map.Entry<Property, String>> i_it = this.properties_
				.entrySet().iterator(); i_it.hasNext();) {
			Map.Entry<Property, String> i = i_it.next();
			switch (i.getKey()) {
			case PropertyText:
			case PropertyInnerHTML:
				innerHTML += i.getValue();
				break;
			case PropertyScript:
				innerHTML += "/*<![CDATA[*/\n" + i.getValue() + "\n/* ]]> */";
				break;
			case PropertyDisabled:
				if (i.getValue().equals("true")) {
					out.append(" disabled=\"disabled\"");
				}
				break;
			case PropertyReadOnly:
				if (i.getValue().equals("true")) {
					out.append(" readonly=\"readonly\"");
				}
				break;
			case PropertyTabIndex:
				out.append(" tabindex=\"").append(i.getValue()).append('"');
				break;
			case PropertyChecked:
				if (i.getValue().equals("true")) {
					out.append(" checked=\"checked\"");
				}
				break;
			case PropertySelected:
				if (i.getValue().equals("true")) {
					out.append(" selected=\"selected\"");
				}
				break;
			case PropertyMultiple:
				if (i.getValue().equals("true")) {
					out.append(" multiple=\"multiple\"");
				}
				break;
			case PropertyTarget:
				out.append(" target=\"").append(i.getValue()).append("\"");
				break;
			case PropertyIndeterminate:
				if (i.getValue().equals("true")) {
					DomElement self = this;
					self.callMethod("indeterminate=" + i.getValue());
				}
				break;
			case PropertyValue:
				if (this.type_ != DomElementType.DomElement_TEXTAREA) {
					out.append(" value=");
					fastHtmlAttributeValue(out, attributeValues, i.getValue());
				} else {
					innerHTML += i.getValue();
				}
				break;
			case PropertySrc:
				out.append(" src=");
				fastHtmlAttributeValue(out, attributeValues, i.getValue());
				break;
			case PropertyColSpan:
				out.append(" colspan=");
				fastHtmlAttributeValue(out, attributeValues, i.getValue());
				break;
			case PropertyRowSpan:
				out.append(" rowspan=");
				fastHtmlAttributeValue(out, attributeValues, i.getValue());
				break;
			case PropertyClass:
				out.append(" class=");
				fastHtmlAttributeValue(out, attributeValues, i.getValue());
				break;
			default:
				break;
			}
		}
		if (!needButtonWrap) {
			style += this.getCssStyle();
		}
		if (style.length() != 0) {
			out.append(" style=");
			fastHtmlAttributeValue(out, attributeValues, style);
		}
		if (needButtonWrap && !supportButton) {
			out.append(" />");
		} else {
			if (openingTagOnly) {
				out.append(">");
				if (innerHTML.length() != 0) {
					DomElement self = this;
					self.childrenHtml_.append(innerHTML);
				}
				return;
			}
			if (!isSelfClosingTag(renderedType)) {
				out.append(">");
				for (int i = 0; i < this.childrenToAdd_.size(); ++i) {
					this.childrenToAdd_.get(i).child.asHTML(out, javaScript,
							timeouts);
				}
				out.append(innerHTML);
				out.append(this.childrenHtml_.toString());
				if (renderedType == DomElementType.DomElement_DIV
						&& app.getEnvironment().getAgent() == WEnvironment.UserAgent.IE6
						&& innerHTML.length() == 0
						&& this.childrenToAdd_.isEmpty()
						&& this.childrenHtml_.isEmpty()) {
					out.append("&nbsp;");
				}
				out.append("</").append(elementNames_[renderedType.getValue()])
						.append(">");
			} else {
				out.append(" />");
			}
			if (needButtonWrap && supportButton) {
				out.append("</button>");
			} else {
				if (needAnchorWrap) {
					out.append("</a>");
				}
			}
		}
		javaScript.append(this.javaScriptEvenWhenDeleted_).append(
				this.javaScript_);
		if (this.timeOut_ != -1) {
			timeouts.add(new DomElement.TimeoutEvent(this.timeOut_, this.id_,
					this.timeOutJSRepeat_));
		}
		timeouts.addAll(this.timeouts_);
	}

	public final void asHTML(EscapeOStream out, EscapeOStream javaScript,
			List<DomElement.TimeoutEvent> timeouts) {
		asHTML(out, javaScript, timeouts, false);
	}

	public static void createTimeoutJs(Writer out,
			List<DomElement.TimeoutEvent> timeouts, WApplication app)
			throws IOException {
		for (int i = 0; i < timeouts.size(); ++i) {
			out.append(app.getJavaScriptClass()).append("._p_.addTimerEvent('")
					.append(timeouts.get(i).event).append("', ").append(
							String.valueOf(timeouts.get(i).msec)).append(",")
					.append(timeouts.get(i).repeat ? "true" : "false").append(
							");\n");
		}
	}

	public boolean isDefaultInline() {
		return isDefaultInline(this.type_);
	}

	public void declare(EscapeOStream out) {
		if (this.var_.length() == 0) {
			out.append("var ").append(this.getCreateVar()).append(
					"=Wt3_2_3.$('").append(this.id_).append("');\n");
		}
	}

	public String getCssStyle() {
		if (this.properties_.isEmpty()) {
			return "";
		}
		EscapeOStream style = new EscapeOStream();
		String styleProperty = null;
		for (Iterator<Map.Entry<Property, String>> j_it = this.properties_
				.entrySet().iterator(); j_it.hasNext();) {
			Map.Entry<Property, String> j = j_it.next();
			if (j.getKey() == Property.PropertyStyle) {
				styleProperty = j.getValue();
			} else {
				if (j.getKey().getValue() >= Property.PropertyStylePosition
						.getValue()
						&& j.getKey().getValue() <= Property.PropertyStyleBoxSizing
								.getValue()) {
					if (j.getKey() == Property.PropertyStyleCursor
							&& j.getValue().equals("pointer")) {
						style.append("cursor:pointer;cursor:hand;");
					} else {
						if (j.getValue().length() != 0) {
							style.append(
									cssNames_[j.getKey().getValue()
											- Property.PropertyStylePosition
													.getValue()]).append(':')
									.append(j.getValue()).append(';');
							if (j.getKey().getValue() >= Property.PropertyStyleBoxSizing
									.getValue()) {
								WApplication app = WApplication.getInstance();
								if (app.getEnvironment().agentIsGecko()) {
									style.append("-moz-");
								} else {
									if (app.getEnvironment().agentIsWebKit()) {
										style.append("-webkit-");
									}
								}
								style
										.append(
												cssNames_[j.getKey().getValue()
														- Property.PropertyStylePosition
																.getValue()])
										.append(':').append(j.getValue())
										.append(';');
							}
						}
					}
				} else {
					if (j.getKey() == Property.PropertyStyleWidthExpression) {
						style.append("width:expression(").append(j.getValue())
								.append(");");
					}
				}
			}
		}
		if (styleProperty != null) {
			style.append(styleProperty);
		}
		return style.toString();
	}

	public static void fastJsStringLiteral(EscapeOStream outRaw,
			EscapeOStream outEscaped, String s) {
		outRaw.append('\'');
		outRaw.append(s, outEscaped);
		outRaw.append('\'');
	}

	public static void jsStringLiteral(EscapeOStream out, String s,
			char delimiter) {
		out.append(delimiter);
		out
				.pushEscape(delimiter == '\'' ? EscapeOStream.RuleSet.JsStringLiteralSQuote
						: EscapeOStream.RuleSet.JsStringLiteralDQuote);
		out.append(s);
		out.popEscape();
		out.append(delimiter);
	}

	public static void jsStringLiteral(Writer out, String s, char delimiter) {
		EscapeOStream sout = new EscapeOStream(out);
		jsStringLiteral(sout, s, delimiter);
	}

	public static void fastHtmlAttributeValue(EscapeOStream outRaw,
			EscapeOStream outEscaped, String s) {
		outRaw.append('"');
		outRaw.append(s, outEscaped);
		outRaw.append('"');
	}

	public static void htmlAttributeValue(Writer out, String s) {
		EscapeOStream sout = new EscapeOStream(out);
		sout.pushEscape(EscapeOStream.RuleSet.HtmlAttribute);
		sout.append(s);
	}

	public static boolean isSelfClosingTag(String tag) {
		return tag.equals("br") || tag.equals("hr") || tag.equals("img")
				|| tag.equals("area") || tag.equals("col")
				|| tag.equals("input");
	}

	public static boolean isSelfClosingTag(DomElementType element) {
		return element == DomElementType.DomElement_BR
				|| element == DomElementType.DomElement_IMG
				|| element == DomElementType.DomElement_AREA
				|| element == DomElementType.DomElement_COL
				|| element == DomElementType.DomElement_INPUT;
	}

	public static DomElementType parseTagName(String tag) {
		for (int i = 0; i < DomElementType.DomElement_UNKNOWN.getValue(); ++i) {
			if (tag.equals(elementNames_[i])) {
				return DomElementType.values()[i];
			}
		}
		return DomElementType.DomElement_UNKNOWN;
	}

	public static String cssName(Property property) {
		return cssNames_[property.getValue()
				- Property.PropertyStylePosition.getValue()];
	}

	public static boolean isDefaultInline(DomElementType type) {
		return defaultInline_[type.getValue()];
	}

	public String getJavaScript() {
		return this.javaScript_.toString();
	}

	public void updateInnerHtmlOnly() {
		this.mode_ = DomElement.Mode.ModeUpdate;
		assert this.replaced_ == null;
		assert this.insertBefore_ == null;
		this.attributes_.clear();
		this.eventHandlers_.clear();
		for (Iterator<Map.Entry<Property, String>> i_it = this.properties_
				.entrySet().iterator(); i_it.hasNext();) {
			Map.Entry<Property, String> i = i_it.next();
			if (i.getKey() == Property.PropertyInnerHTML
					|| i.getKey() == Property.PropertyText
					|| i.getKey() == Property.PropertyTarget) {
			} else {
				i_it.remove();
			}
		}
	}

	public String addToParent(Writer out, String parentVar, int pos,
			WApplication app) {
		EscapeOStream sout = new EscapeOStream(out);
		return this.addToParent(sout, parentVar, pos, app);
	}

	public void createElement(Writer out, WApplication app, String domInsertJS) {
		EscapeOStream sout = new EscapeOStream(out);
		this.createElement(sout, app, domInsertJS);
	}

	public String getCreateVar() {
		this.var_ = "j" + String.valueOf(nextId_++);
		return this.var_;
	}

	static class EventHandler {
		private static Logger logger = LoggerFactory
				.getLogger(EventHandler.class);

		public String jsCode;
		public String signalName;

		public EventHandler() {
			this.jsCode = "";
			this.signalName = "";
		}

		public EventHandler(String j, String sn) {
			this.jsCode = j;
			this.signalName = sn;
		}
	}

	private boolean canWriteInnerHTML(WApplication app) {
		if (app.getEnvironment().agentIsIEMobile()) {
			return true;
		}
		if ((app.getEnvironment().agentIsIE() || app.getEnvironment()
				.getAgent() == WEnvironment.UserAgent.Konqueror)
				&& (this.type_ == DomElementType.DomElement_TBODY
						|| this.type_ == DomElementType.DomElement_THEAD
						|| this.type_ == DomElementType.DomElement_TABLE
						|| this.type_ == DomElementType.DomElement_TR
						|| this.type_ == DomElementType.DomElement_SELECT || this.type_ == DomElementType.DomElement_TD)) {
			return false;
		}
		return true;
	}

	// private boolean containsElement(DomElementType type) ;
	private void processEvents(WApplication app) {
		DomElement self = this;
		String S_keypress = WInteractWidget.KEYPRESS_SIGNAL;
		DomElement.EventHandler keypress = this.eventHandlers_.get(S_keypress);
		if (keypress != null && keypress.jsCode.length() != 0) {
			MapUtils.access(self.eventHandlers_, S_keypress,
					DomElement.EventHandler.class).jsCode = "if (Wt3_2_3.isKeyPress(event)){"
					+ MapUtils.access(self.eventHandlers_, S_keypress,
							DomElement.EventHandler.class).jsCode + '}';
		}
	}

	private void processProperties(WApplication app) {
		if (this.minMaxSizeProperties_
				&& app.getEnvironment().getAgent() == WEnvironment.UserAgent.IE6) {
			DomElement self = this;
			String w = self.properties_.get(Property.PropertyStyleWidth);
			String minw = self.properties_.get(Property.PropertyStyleMinWidth);
			String maxw = self.properties_.get(Property.PropertyStyleMaxWidth);
			if (minw != null || maxw != null) {
				if (w == null) {
					StringBuilder expr = new StringBuilder();
					expr.append("Wt3_2_3.IEwidth(this,");
					if (minw != null) {
						expr.append('\'').append(minw).append('\'');
						self.properties_.remove(Property.PropertyStyleMinWidth);
					} else {
						expr.append("'0px'");
					}
					expr.append(',');
					if (maxw != null) {
						expr.append('\'').append(maxw).append('\'');
						self.properties_.remove(Property.PropertyStyleMaxWidth);
					} else {
						expr.append("'100000px'");
					}
					expr.append(")");
					self.properties_.remove(Property.PropertyStyleWidth);
					self.properties_.put(Property.PropertyStyleWidthExpression,
							expr.toString());
				}
			}
			String i = self.properties_.get(Property.PropertyStyleMinHeight);
			if (i != null) {
				self.properties_.put(Property.PropertyStyleHeight, i);
			}
		}
	}

	private void setJavaScriptProperties(EscapeOStream out, WApplication app) {
		EscapeOStream escaped = out.push();
		boolean pushed = false;
		for (Iterator<Map.Entry<Property, String>> i_it = this.properties_
				.entrySet().iterator(); i_it.hasNext();) {
			Map.Entry<Property, String> i = i_it.next();
			this.declare(out);
			switch (i.getKey()) {
			case PropertyInnerHTML:
			case PropertyAddedInnerHTML:
				out.append("Wt3_2_3.setHtml(").append(this.var_).append(',');
				if (!pushed) {
					escaped
							.pushEscape(EscapeOStream.RuleSet.JsStringLiteralSQuote);
					pushed = true;
				}
				fastJsStringLiteral(out, escaped, i.getValue());
				out.append(
						i.getKey() == Property.PropertyInnerHTML ? ",false"
								: ",true").append(");");
				break;
			case PropertyScript:
				out.append(this.var_).append(".innerHTML=");
				if (!pushed) {
					escaped
							.pushEscape(EscapeOStream.RuleSet.JsStringLiteralSQuote);
					pushed = true;
				}
				fastJsStringLiteral(out, escaped, "/*<![CDATA[*/\n"
						+ i.getValue() + "\n/* ]]> */");
				out.append(';');
				break;
			case PropertyValue:
				out.append(this.var_).append(".value=");
				if (!pushed) {
					escaped
							.pushEscape(EscapeOStream.RuleSet.JsStringLiteralSQuote);
					pushed = true;
				}
				fastJsStringLiteral(out, escaped, i.getValue());
				out.append(';');
				break;
			case PropertyTarget:
				out.append(this.var_).append(".target='").append(i.getValue())
						.append("';");
				break;
			case PropertyIndeterminate:
				out.append(this.var_).append(".indeterminate=").append(
						i.getValue()).append(";");
				break;
			case PropertyDisabled:
				out.append(this.var_).append(".disabled=").append(i.getValue())
						.append(';');
				break;
			case PropertyReadOnly:
				out.append(this.var_).append(".readOnly=").append(i.getValue())
						.append(';');
				break;
			case PropertyTabIndex:
				out.append(this.var_).append(".tabIndex=").append(i.getValue())
						.append(';');
				break;
			case PropertyChecked:
				out.append(this.var_).append(".checked=").append(i.getValue())
						.append(';');
				break;
			case PropertySelected:
				out.append(this.var_).append(".selected=").append(i.getValue())
						.append(';');
				break;
			case PropertySelectedIndex:
				out.append(this.var_).append(".selectedIndex=").append(
						i.getValue()).append(';');
				break;
			case PropertyMultiple:
				out.append(this.var_).append(".multiple=").append(i.getValue())
						.append(';');
				break;
			case PropertySrc:
				out.append(this.var_).append(".src='").append(i.getValue())
						.append("\';");
				break;
			case PropertyColSpan:
				out.append(this.var_).append(".colSpan=").append(i.getValue())
						.append(";");
				break;
			case PropertyRowSpan:
				out.append(this.var_).append(".rowSpan=").append(i.getValue())
						.append(";");
				break;
			case PropertyClass:
				out.append(this.var_).append(".className=");
				if (!pushed) {
					escaped
							.pushEscape(EscapeOStream.RuleSet.JsStringLiteralSQuote);
					pushed = true;
				}
				fastJsStringLiteral(out, escaped, i.getValue());
				out.append(';');
				break;
			case PropertyText:
				out.append(this.var_).append(".text=");
				if (!pushed) {
					escaped
							.pushEscape(EscapeOStream.RuleSet.JsStringLiteralSQuote);
					pushed = true;
				}
				fastJsStringLiteral(out, escaped, i.getValue());
				out.append(';');
				break;
			case PropertyStyleFloat:
				out.append(this.var_).append(".style.").append(
						app.getEnvironment().agentIsIE() ? "styleFloat"
								: "cssFloat").append("=\'")
						.append(i.getValue()).append("\';");
				break;
			case PropertyStyleWidthExpression:
				out.append(this.var_).append(".style.setExpression('width',");
				if (!pushed) {
					escaped
							.pushEscape(EscapeOStream.RuleSet.JsStringLiteralSQuote);
					pushed = true;
				}
				fastJsStringLiteral(out, escaped, i.getValue());
				out.append(");");
				break;
			default:
				if (i.getKey().getValue() >= Property.PropertyStyle.getValue()
						&& i.getKey().getValue() <= Property.PropertyStyleBoxSizing
								.getValue()) {
					if (app.getEnvironment().getAgent() == WEnvironment.UserAgent.IE6) {
						out.append(this.var_).append(".style['").append(
								cssNames_[i.getKey().getValue()
										- Property.PropertyStylePosition
												.getValue()]).append("']='")
								.append(i.getValue()).append("';");
					} else {
						out.append(this.var_).append(".style.").append(
								cssCamelNames_[i.getKey().getValue()
										- Property.PropertyStyle.getValue()])
								.append("='").append(i.getValue()).append("';");
					}
				}
			}
			out.append('\n');
		}
	}

	private void setJavaScriptAttributes(EscapeOStream out) {
		for (Iterator<Map.Entry<String, String>> i_it = this.attributes_
				.entrySet().iterator(); i_it.hasNext();) {
			Map.Entry<String, String> i = i_it.next();
			this.declare(out);
			if (i.getKey().equals("style")) {
				out.append(this.var_).append(".style.cssText = ");
				jsStringLiteral(out, i.getValue(), '\'');
				out.append(';').append('\n');
			} else {
				out.append(this.var_).append(".setAttribute('").append(
						i.getKey()).append("',");
				jsStringLiteral(out, i.getValue(), '\'');
				out.append(");\n");
			}
		}
	}

	private void setJavaScriptEvent(EscapeOStream out, String eventName,
			DomElement.EventHandler handler, WApplication app) {
		boolean globalUnfocused = this.id_.equals(app.getDomRoot().getId());
		String extra1 = "";
		String extra2 = "";
		if (globalUnfocused) {
			extra1 = "var g = event||window.event; var t = g.target||g.srcElement;if ((!t||Wt3_2_3.hasTag(t,'DIV') ||Wt3_2_3.hasTag(t,'BODY') ||Wt3_2_3.hasTag(t,'HTML'))) { ";
			extra2 = "}";
		}
		int fid = nextId_++;
		out.append("function f").append(fid).append("(event){ ").append(extra1)
				.append(handler.jsCode).append(extra2).append("}\n");
		if (globalUnfocused) {
			out.append("document");
		} else {
			this.declare(out);
			out.append(this.var_);
		}
		if (eventName == WInteractWidget.MOUSE_WHEEL_SIGNAL
				&& app.getEnvironment().agentIsGecko()) {
			out.append(".addEventListener('DOMMouseScroll', f").append(fid)
					.append(", false);\n");
		} else {
			out.append(".on").append(eventName).append("=f").append(fid)
					.append(";\n");
		}
	}

	private void createElement(EscapeOStream out, WApplication app,
			String domInsertJS) {
		if (this.var_.length() == 0) {
			this.getCreateVar();
		}
		out.append("var ").append(this.var_).append("=");
		if (app.getEnvironment().agentIsIE()
				&& app.getEnvironment().getAgent().getValue() <= WEnvironment.UserAgent.IE8
						.getValue()
				&& this.type_ != DomElementType.DomElement_TEXTAREA) {
			out.append("document.createElement('");
			out.pushEscape(EscapeOStream.RuleSet.JsStringLiteralSQuote);
			List<DomElement.TimeoutEvent> timeouts = new ArrayList<DomElement.TimeoutEvent>();
			EscapeOStream dummy = new EscapeOStream();
			this.asHTML(out, dummy, timeouts, true);
			out.popEscape();
			out.append("');");
			out.append(domInsertJS);
			this.renderInnerHtmlJS(out, app);
		} else {
			out.append("document.createElement('").append(
					elementNames_[this.type_.getValue()]).append("');");
			out.append(domInsertJS);
			this.asJavaScript(out, DomElement.Priority.Create);
			this.asJavaScript(out, DomElement.Priority.Update);
		}
	}

	private String addToParent(EscapeOStream out, String parentVar, int pos,
			WApplication app) {
		this.getCreateVar();
		if (this.type_ == DomElementType.DomElement_TD
				|| this.type_ == DomElementType.DomElement_TR) {
			out.append("var ").append(this.var_).append("=");
			if (this.type_ == DomElementType.DomElement_TD) {
				out.append(parentVar).append(".insertCell(").append(pos)
						.append(");\n");
			} else {
				out.append(parentVar).append(".insertRow(").append(pos).append(
						");\n");
			}
			this.asJavaScript(out, DomElement.Priority.Create);
			this.asJavaScript(out, DomElement.Priority.Update);
		} else {
			StringBuilder insertJS = new StringBuilder();
			if (pos != -1) {
				insertJS.append("Wt3_2_3.insertAt(").append(parentVar).append(
						",").append(this.var_).append(",").append(pos).append(
						");");
			} else {
				insertJS.append(parentVar).append(".appendChild(").append(
						this.var_).append(");\n");
			}
			this.createElement(out, app, insertJS.toString());
		}
		return this.var_;
	}

	// private String createAsJavaScript(EscapeOStream out, String parentVar,
	// int pos, WApplication app) ;
	private void renderInnerHtmlJS(EscapeOStream out, WApplication app) {
		if (!this.childrenHtml_.isEmpty() || this.wasEmpty_
				&& this.canWriteInnerHTML(app)) {
			if (this.type_ == DomElementType.DomElement_DIV
					&& app.getEnvironment().getAgent() == WEnvironment.UserAgent.IE6
					|| !this.childrenToAdd_.isEmpty()
					|| !this.childrenHtml_.isEmpty()) {
				this.declare(out);
				out.append("Wt3_2_3.setHtml(").append(this.var_).append(",'");
				out.pushEscape(EscapeOStream.RuleSet.JsStringLiteralSQuote);
				out.append(this.childrenHtml_.toString());
				List<DomElement.TimeoutEvent> timeouts = new ArrayList<DomElement.TimeoutEvent>();
				EscapeOStream js = new EscapeOStream();
				for (int i = 0; i < this.childrenToAdd_.size(); ++i) {
					this.childrenToAdd_.get(i).child.asHTML(out, js, timeouts);
				}
				if (this.type_ == DomElementType.DomElement_DIV
						&& app.getEnvironment().getAgent() == WEnvironment.UserAgent.IE6
						&& this.childrenToAdd_.isEmpty()
						&& this.childrenHtml_.isEmpty()) {
					out.append("&nbsp;");
				}
				out.popEscape();
				out.append("');\n");
				timeouts.addAll(this.timeouts_);
				for (int i = 0; i < timeouts.size(); ++i) {
					out.append(app.getJavaScriptClass()).append(
							"._p_.addTimerEvent('").append(
							timeouts.get(i).event).append("', ").append(
							timeouts.get(i).msec).append(",").append(
							timeouts.get(i).repeat ? "true" : "false").append(
							");\n");
				}
				out.append(js);
			}
		} else {
			for (int i = 0; i < this.childrenToAdd_.size(); ++i) {
				this.declare(out);
				DomElement child = this.childrenToAdd_.get(i).child;
				child.addToParent(out, this.var_,
						this.childrenToAdd_.get(i).pos, app);
			}
		}
		if (!this.javaScript_.isEmpty()) {
			this.declare(out);
			out.append(this.javaScript_).append('\n');
		}
		if (this.timeOut_ != -1) {
			out.append(app.getJavaScriptClass()).append("._p_.addTimerEvent('")
					.append(this.id_).append("', ").append(this.timeOut_)
					.append(",").append(
							this.timeOutJSRepeat_ ? "true" : "false").append(
							");\n");
		}
	}

	private DomElement.Mode mode_;
	private boolean wasEmpty_;
	private int removeAllChildren_;
	private boolean hideWithDisplay_;
	private boolean minMaxSizeProperties_;
	private boolean unstubbed_;
	private boolean unwrapped_;
	private DomElement replaced_;
	private DomElement insertBefore_;
	private DomElementType type_;
	private String id_;
	private int numManipulations_;
	private int timeOut_;
	private boolean timeOutJSRepeat_;
	private EscapeOStream javaScript_;
	private String javaScriptEvenWhenDeleted_;
	private String var_;
	private boolean declared_;
	private Map<String, String> attributes_;
	private SortedMap<Property, String> properties_;
	private Map<String, DomElement.EventHandler> eventHandlers_;

	static class ChildInsertion {
		private static Logger logger = LoggerFactory
				.getLogger(ChildInsertion.class);

		public int pos;
		public DomElement child;

		public ChildInsertion() {
			this.pos = 0;
			this.child = null;
		}

		public ChildInsertion(int p, DomElement c) {
			this.pos = p;
			this.child = c;
		}
	}

	private List<DomElement.ChildInsertion> childrenToAdd_;
	private List<String> childrenToSave_;
	private List<DomElement> updatedChildren_;
	private EscapeOStream childrenHtml_;
	private List<DomElement.TimeoutEvent> timeouts_;
	private boolean discardWithParent_;
	static String[] elementNames_ = { "a", "br", "button", "col", "div",
			"fieldset", "form", "h1", "h2", "h3", "h4", "h5", "h6", "iframe",
			"img", "input", "label", "legend", "li", "ol", "option", "ul",
			"script", "select", "span", "table", "tbody", "thead", "tfoot",
			"th", "td", "textarea", "tr", "p", "canvas", "map", "area",
			"object", "param", "audio", "video", "source", "strong", "em" };
	static boolean[] defaultInline_ = { true, true, true, false, false, false,
			false, true, false, false, false, false, false, true, true, true,
			true, true, false, false, true, false, false, true, true, false,
			false, false, false, false, false, true, false, false, true, false,
			true, false, false, false, false, false, true, true };
	static String[] cssNames_ = { "position", "z-index", "float", "clear",
			"width", "height", "line-height", "min-width", "min-height",
			"max-width", "max-height", "left", "right", "top", "bottom",
			"vertical-align", "text-align", "padding", "padding-top",
			"padding-right", "padding-bottom", "padding-left", "margin-top",
			"margin-right", "margin-bottom", "margin-left", "cursor",
			"border-top", "border-right", "border-bottom", "border-left",
			"color", "overflow-x", "overflow-y", "opacity", "font-family",
			"font-style", "font-variant", "font-weight", "font-size",
			"background-color", "background-image", "background-repeat",
			"background-attachment", "background-position", "text-decoration",
			"white-space", "table-layout", "border-spacing",
			"page-break-before", "page-break-after", "zoom", "visibility",
			"display", "box-sizing" };
	static String[] cssCamelNames_ = { "cssText", "width", "position",
			"zIndex", "cssFloat", "clear", "width", "height", "lineHeight",
			"minWidth", "minHeight", "maxWidth", "maxHeight", "left", "right",
			"top", "bottom", "verticalAlign", "textAlign", "padding",
			"paddingTop", "paddingRight", "paddingBottom", "paddingLeft",
			"marginTop", "marginRight", "marginBottom", "marginLeft", "cursor",
			"borderTop", "borderRight", "borderBottom", "borderLeft", "color",
			"overflowX", "overflowY", "opacity", "fontFamily", "fontStyle",
			"fontVariant", "fontWeight", "fontSize", "backgroundColor",
			"backgroundImage", "backgroundRepeat", "backgroundAttachment",
			"backgroundPosition", "textDecoration", "whiteSpace",
			"tableLayout", "borderSpacing", "pageBreakBefore",
			"pageBreakAfter", "zoom", "visibility", "display", "boxSizing" };
	static final String unsafeChars_ = " $&+,:;=?@'\"<>#%{}|\\^~[]`";
	private static int nextId_ = 0;
}
