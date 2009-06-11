package eu.webtoolkit.jwt;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

class DomElement {
	public enum Mode {
		ModeCreate, ModeUpdate;

		public int getValue() {
			return ordinal();
		}
	}

	public DomElement(DomElement.Mode mode, DomElementType type) {
		this.mode_ = mode;
		this.wasEmpty_ = this.mode_ == DomElement.Mode.ModeCreate;
		this.deleted_ = false;
		this.removeAllChildren_ = false;
		this.minMaxSizeProperties_ = false;
		this.replaced_ = null;
		this.insertBefore_ = null;
		this.type_ = type;
		this.id_ = "";
		this.numManipulations_ = 0;
		this.methodCalls_ = new ArrayList<String>();
		this.timeOut_ = -1;
		this.javaScript_ = "";
		this.javaScriptEvenWhenDeleted_ = "";
		this.var_ = "";
		this.attributes_ = new HashMap<String, String>();
		this.properties_ = new HashMap<Property, String>();
		this.eventHandlers_ = new HashMap<String, DomElement.EventHandler>();
		this.childrenToAdd_ = new ArrayList<DomElement.ChildInsertion>();
		this.childrenHtml_ = null;
		this.timeouts_ = new ArrayList<DomElement.TimeoutEvent>();
		this.discardWithParent_ = true;
	}

	public void destroy() {
		for (int i = 0; i < this.childrenToAdd_.size(); ++i) {
			/* delete this.childrenToAdd_.get(i).child */;
		}
		/* delete this.replaced_ */;
		/* delete this.insertBefore_ */;
		/* delete this.childrenHtml_ */;
	}

	public static String urlEncodeS(String url) {
		StringWriter result = new StringWriter();
		for (int i = 0; i < url.length(); ++i) {
			char c = url.charAt(i);
			if (c < 31 || c >= 127 || unsafeChars_.indexOf(c) != -1) {
				result.append('%');
				result.append(Integer.toHexString(c));
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
			throw new WtException("Cannot update widget without id");
		}
		DomElement e = new DomElement(DomElement.Mode.ModeUpdate, type);
		e.id_ = id;
		return e;
	}

	public static DomElement getForUpdate(WObject object, DomElementType type) {
		return getForUpdate(object.getFormName(), type);
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
		++this.numManipulations_;
		this.javaScript_ += child.javaScriptEvenWhenDeleted_
				+ child.javaScript_;
		child.javaScriptEvenWhenDeleted_ = "";
		child.javaScript_ = "";
		if (this.wasEmpty_
				&& this.canWriteInnerHTML(WApplication.getInstance())) {
			if (!(this.childrenHtml_ != null)) {
				this.childrenHtml_ = new StringWriter();
			}
			EscapeOStream sout = new EscapeOStream(this.childrenHtml_);
			child.asHTML(sout, this.timeouts_);
			/* delete child */;
		} else {
			this.childrenToAdd_.add(new DomElement.ChildInsertion(-1, child));
		}
	}

	public void insertChildAt(DomElement child, int pos) {
		++this.numManipulations_;
		this.javaScript_ += child.javaScriptEvenWhenDeleted_
				+ child.javaScript_;
		child.javaScriptEvenWhenDeleted_ = "";
		child.javaScript_ = "";
		this.childrenToAdd_.add(new DomElement.ChildInsertion(pos, child));
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

	public void setEventSignal(String eventName, AbstractEventSignal signal) {
		this.setEvent(eventName, signal.getJavaScript(), signal.encodeCmd(),
				signal.isExposedSignal());
	}

	public void setEvent(String eventName, String jsCode, String signalName,
			boolean isExposed) {
		WApplication app = WApplication.getInstance();
		boolean anchorClick = this.getType() == DomElementType.DomElement_A
				&& eventName == WInteractWidget.CLICK_SIGNAL;
		boolean nonEmpty = isExposed || anchorClick || jsCode.length() != 0;
		StringWriter js = new StringWriter();
		if (nonEmpty) {
			if (app.getEnvironment().agentIsIEMobile()) {
				js.append("var e=window.event;");
			} else {
				js.append("var e=event||window.event;");
			}
		}
		if (anchorClick) {
			js.append("if(e.ctrlKey||e.metaKey)return true;else{");
		}
		if (isExposed) {
			js.append(app.getJavaScriptClass()).append("._p_.update(this,'")
					.append(signalName).append("',e,true);");
		}
		js.append(jsCode);
		if (anchorClick) {
			js.append("}");
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

	public static class EventAction {
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
		String code = "";
		for (int i = 0; i < actions.size(); ++i) {
			if (actions.get(i).jsCondition.length() != 0) {
				code += "if(" + actions.get(i).jsCondition + "){";
			}
			if (actions.get(i).exposed) {
				code += WApplication.getInstance().getJavaScriptClass()
						+ "._p_.update(this,'" + actions.get(i).updateCmd
						+ "',e,true);";
			}
			code += actions.get(i).jsCode;
			if (actions.get(i).jsCondition.length() != 0) {
				code += "}";
			}
		}
		this.setEvent(eventName, code, "");
	}

	public void setId(String id, boolean andName) {
		++this.numManipulations_;
		this.id_ = id;
		if (andName) {
			this.setAttribute("name", id);
		}
	}

	public final void setId(String id) {
		setId(id, false);
	}

	public void setId(WObject object, boolean andName) {
		this.setId(object.getFormName(), andName);
	}

	public final void setId(WObject object) {
		setId(object, false);
	}

	public void setTimeout(int msec, boolean jsRepeat) {
		++this.numManipulations_;
		this.timeOut_ = msec;
		this.timeOutJSRepeat_ = jsRepeat;
	}

	public void callMethod(String method) {
		++this.numManipulations_;
		this.methodCalls_.add(method);
	}

	public void callJavaScript(String jsCode, boolean evenWhenDeleted) {
		++this.numManipulations_;
		if (!evenWhenDeleted) {
			this.javaScript_ += jsCode;
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

	public void removeAllChildren() {
		++this.numManipulations_;
		this.removeAllChildren_ = true;
		this.wasEmpty_ = true;
	}

	public void removeFromParent() {
		++this.numManipulations_;
		this.deleted_ = true;
	}

	public void replaceWith(DomElement newElement, boolean hideWithDisplay) {
		++this.numManipulations_;
		this.replaced_ = newElement;
		this.hideWithDisplay_ = hideWithDisplay;
	}

	public void insertBefore(DomElement sibling) {
		++this.numManipulations_;
		this.insertBefore_ = sibling;
	}

	public void setDiscardWithParent(boolean discard) {
		this.discardWithParent_ = discard;
	}

	public boolean discardWithParent() {
		return this.discardWithParent_;
	}

	public enum Priority {
		Delete, Create, Update;

		public int getValue() {
			return ordinal();
		}
	}

	public static class TimeoutEvent {
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

	public List<DomElement.TimeoutEvent> TimeoutList;

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
			if (this.deleted_ || this.removeAllChildren_) {
				this.declare(out);
				if (this.deleted_) {
					out.append(this.javaScriptEvenWhenDeleted_).append(
							this.var_).append(".parentNode.removeChild(")
							.append(this.var_).append(");\n");
				} else {
					if (this.removeAllChildren_) {
						out.append(this.var_).append(".innerHTML='';\n");
					}
				}
			}
			return this.var_;
		case Create:
			if (this.mode_ == DomElement.Mode.ModeCreate) {
				this.declare(out);
				if (this.id_.length() != 0) {
					out.append(this.var_).append(".setAttribute('id', '")
							.append(this.id_).append("');\n");
				}
				this.setJavaScriptProperties(out, WApplication.getInstance());
				this.setJavaScriptAttributes(out);
			}
			return this.var_;
		case Update: {
			if (this.deleted_) {
				break;
			}
			WApplication app = WApplication.getInstance();
			if (this.mode_ == DomElement.Mode.ModeUpdate
					&& this.numManipulations_ == 1) {
				if (this.properties_.get(Property.PropertyStyleDisplay) != null) {
					String style = this.properties_
							.get(Property.PropertyStyleDisplay);
					if (style.equals("none")) {
						out.append("Wt2_99_2.hide('").append(this.id_).append(
								"');\n");
						return this.var_;
					} else {
						if (style.length() == 0) {
							out.append("Wt2_99_2.show('").append(this.id_)
									.append("');\n");
							return this.var_;
						} else {
							if (style.equals("inline")) {
								out.append("Wt2_99_2.inline('" + this.id_
										+ "');\n");
								return this.var_;
							} else {
								if (style.equals("block")) {
									out.append("Wt2_99_2.block('" + this.id_
											+ "');\n");
									return this.var_;
								}
							}
						}
					}
				}
			}
			this.processEvents(app);
			this.processProperties(app);
			if (this.replaced_ != null) {
				this.declare(out);
				String varr = this.replaced_.asJavaScript(out,
						DomElement.Priority.Create);
				this.replaced_.asJavaScript(out, DomElement.Priority.Update);
				out.append("Wt2_99_2.unstub(").append(this.var_).append(',')
						.append(varr).append(',').append(
								this.hideWithDisplay_ ? 1 : 0).append(");\n");
				return this.var_;
			} else {
				if (this.insertBefore_ != null) {
					this.declare(out);
					String varr = this.insertBefore_.asJavaScript(out,
							DomElement.Priority.Create);
					out.append(this.var_).append(".parentNode.insertBefore(")
							.append(varr).append(",")
							.append(this.var_ + ");\n");
					this.insertBefore_.asJavaScript(out,
							DomElement.Priority.Update);
					return this.var_;
				}
			}
			if (this.mode_ != DomElement.Mode.ModeCreate) {
				this.setJavaScriptAttributes(out);
				this.setJavaScriptProperties(out, app);
			}
			for (Iterator<Map.Entry<String, DomElement.EventHandler>> i_it = this.eventHandlers_
					.entrySet().iterator(); i_it.hasNext();) {
				Map.Entry<String, DomElement.EventHandler> i = i_it.next();
				if (this.mode_ == DomElement.Mode.ModeUpdate
						|| i.getValue().jsCode.length() != 0) {
					this.declare(out);
					int fid = nextId_++;
					out.append("function f").append(fid).append("(event){")
							.append(i.getValue().jsCode).append("}\n");
					if (i.getKey().startsWith("key")
							&& this.id_.equals(app.getRoot().getFormName())) {
						out.append("document");
					} else {
						out.append(this.var_);
					}
					out.append(".on").append(i.getKey()).append("=f").append(
							fid).append(";\n");
				}
			}
			if (this.wasEmpty_ && this.canWriteInnerHTML(app)) {
				if (!this.childrenToAdd_.isEmpty()
						|| this.childrenHtml_ != null) {
					this.declare(out);
					out.append("Wt2_99_2.setHtml(").append(this.var_).append(
							",'");
					out.pushEscape(EscapeOStream.RuleSet.JsStringLiteralSQuote);
					if (this.childrenHtml_ != null) {
						out.append(this.childrenHtml_.toString());
					}
					List<DomElement.TimeoutEvent> timeouts = new ArrayList<DomElement.TimeoutEvent>();
					for (int i = 0; i < this.childrenToAdd_.size(); ++i) {
						this.childrenToAdd_.get(i).child.asHTML(out, timeouts);
					}
					out.popEscape();
					out.append("');\n");
					timeouts.addAll(this.timeouts_);
					for (int i = 0; i < timeouts.size(); ++i) {
						out.append(app.getJavaScriptClass()).append(
								"._p_.addTimerEvent('").append(
								timeouts.get(i).event).append("', ").append(
								timeouts.get(i).msec).append(",").append(
								timeouts.get(i).repeat ? "true" : "false")
								.append(");\n");
					}
				}
			} else {
				for (int i = 0; i < this.childrenToAdd_.size(); ++i) {
					this.declare(out);
					DomElement child = this.childrenToAdd_.get(i).child;
					String cvar = child.createAsJavaScript(out, this.var_,
							this.childrenToAdd_.get(i).pos);
					child.asJavaScript(out, DomElement.Priority.Update);
				}
			}
			for (int i = 0; i < this.methodCalls_.size(); ++i) {
				this.declare(out);
				out.append(this.var_).append(".").append(
						this.methodCalls_.get(i)).append(';').append('\n');
			}
			if (this.javaScriptEvenWhenDeleted_.length() != 0) {
				this.declare(out);
				out.append(this.javaScriptEvenWhenDeleted_).append('\n');
			}
			if (this.javaScript_.length() != 0) {
				this.declare(out);
				out.append(this.javaScript_).append('\n');
			}
			if (this.timeOut_ != -1) {
				out.append(app.getJavaScriptClass()).append(
						"._p_.addTimerEvent('").append(this.id_).append("', ")
						.append(this.timeOut_).append(",").append(
								this.timeOutJSRepeat_ ? "true" : "false")
						.append(");\n");
			}
			return this.var_;
		}
		}
		return this.var_;
	}

	public String asJavaScript(StringWriter js, boolean create) {
		EscapeOStream sout = new EscapeOStream(js);
		if (create) {
			this.asJavaScript(sout, DomElement.Priority.Create);
		} else {
			this.asJavaScript(sout, DomElement.Priority.Update);
		}
		return this.var_;
	}

	public void asHTML(EscapeOStream out, List<DomElement.TimeoutEvent> timeouts) {
		if (this.mode_ != DomElement.Mode.ModeCreate) {
			throw new WtException("DomElement::asHTML() called with ModeUpdate");
		}
		WApplication app = WApplication.getInstance();
		this.processEvents(app);
		this.processProperties(app);
		DomElement.EventHandler clickEvent = this.eventHandlers_
				.get(WInteractWidget.CLICK_SIGNAL);
		boolean needButtonWrap = !app.getEnvironment().hasAjax()
				&& clickEvent != null && clickEvent.jsCode.length() != 0
				&& !app.getEnvironment().agentIsSpiderBot();
		if (needButtonWrap) {
			String i = this.properties_.get(Property.PropertyStyleDisplay);
			if (i != null && i.equals("none")) {
				return;
			}
		}
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
			if (this.type_ == DomElementType.DomElement_AREA
					|| this.type_ == DomElementType.DomElement_INPUT
					|| this.type_ == DomElementType.DomElement_SELECT) {
				needButtonWrap = false;
			}
			if (this.type_ == DomElementType.DomElement_A) {
				String href = this.getAttribute("href");
				if (!href.equals("#")) {
					needButtonWrap = false;
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
				out.append("<button type=\"submit\" name=\"signal\" value=");
				fastHtmlAttributeValue(out, attributeValues,
						clickEvent.signalName);
				out.append(" class=\"Wt-wrap ");
				String l = this.attributes_.get("class");
				if (l != null) {
					out.append(l);
				}
				out.append("\"");
				String i = this.properties_.get(Property.PropertyDisabled);
				if (i != null && i.equals("true")) {
					out.append(" disabled=\"disabled\"");
				}
				if (app.getEnvironment().getAgent() != WEnvironment.UserAgent.Konqueror
						&& !app.getEnvironment().agentIsWebKit()) {
					style = "margin: -1px -3px -2px -3px;";
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
			if (!i.getKey().equals("style")
					&& (!app.getEnvironment().agentIsSpiderBot() || !i.getKey()
							.equals("name"))) {
				out.append(" ").append(i.getKey()).append("=");
				fastHtmlAttributeValue(out, attributeValues, i.getValue());
			}
		}
		if (app.getEnvironment().hasAjax()) {
			for (Iterator<Map.Entry<String, DomElement.EventHandler>> i_it = this.eventHandlers_
					.entrySet().iterator(); i_it.hasNext();) {
				Map.Entry<String, DomElement.EventHandler> i = i_it.next();
				if (i.getValue().jsCode.length() != 0) {
					out.append(" on").append(i.getKey()).append("=");
					fastHtmlAttributeValue(out, attributeValues,
							i.getValue().jsCode);
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
					self.methodCalls_.add("indeterminate=" + i.getValue());
				}
				break;
			case PropertyValue:
				out.append(" value=");
				fastHtmlAttributeValue(out, attributeValues, i.getValue());
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
			default:
				break;
			}
		}
		style += this.getCssStyle();
		if (style.length() != 0) {
			out.append(" style=");
			fastHtmlAttributeValue(out, attributeValues, style);
		}
		if (needButtonWrap && !supportButton) {
			out.append(" />");
		} else {
			if (!isSelfClosingTag(renderedType)) {
				out.append(">").append(innerHTML);
				for (int i = 0; i < this.childrenToAdd_.size(); ++i) {
					this.childrenToAdd_.get(i).child.asHTML(out, timeouts);
				}
				if (this.childrenHtml_ != null) {
					out.append(this.childrenHtml_.toString());
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
		for (int i = 0; i < this.methodCalls_.size(); ++i) {
			app.doJavaScript("Wt2_99_2.getElement('" + this.id_ + "')."
					+ this.methodCalls_.get(i) + ';');
		}
		if (this.timeOut_ != -1) {
			timeouts.add(new DomElement.TimeoutEvent(this.timeOut_, this.id_,
					this.timeOutJSRepeat_));
		}
		timeouts.addAll(this.timeouts_);
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
		return defaultInline_[this.type_.getValue()];
	}

	public void declare(EscapeOStream out) {
		if (this.var_.length() == 0) {
			this.var_ = "j" + String.valueOf(nextId_++);
			out.append("var ").append(this.var_).append("=");
			this.createReference(out);
			out.append(';').append('\n');
		}
	}

	public void createReference(EscapeOStream out) {
		if (this.mode_ == DomElement.Mode.ModeCreate) {
			out.append("document.createElement('").append(
					elementNames_[this.type_.getValue()]).append("')");
		} else {
			out.append("Wt2_99_2.getElement('").append(this.id_).append("')");
		}
	}

	public String createReference() {
		if (this.mode_ == DomElement.Mode.ModeCreate) {
			return CREATE + elementNames_[this.type_.getValue()] + "')";
		} else {
			return "Wt2_99_2.getElement('" + this.id_ + "')";
		}
	}

	public String getCssStyle() {
		String i = this.attributes_.get("style");
		if (this.properties_.isEmpty()) {
			return i != null ? i : "";
		}
		StringWriter style = new StringWriter();
		for (Iterator<Map.Entry<Property, String>> j_it = this.properties_
				.entrySet().iterator(); j_it.hasNext();) {
			Map.Entry<Property, String> j = j_it.next();
			if (j.getKey().getValue() >= Property.PropertyStylePosition
					.getValue()
					&& j.getKey().getValue() <= Property.PropertyStyleDisplay
							.getValue()) {
				if (j.getKey() == Property.PropertyStyleCursor
						&& j.getValue().equals("pointer")) {
					style.append("cursor:pointer;cursor:hand;");
				} else {
					style
							.append(
									cssNames[j.getKey().getValue()
											- Property.PropertyStylePosition
													.getValue()]).append(':')
							.append(j.getValue()).append(';');
				}
			}
		}
		if (i != null) {
			style.append(i);
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
		sout.flush();
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

	public String getJavaScript() {
		return this.javaScript_;
	}

	public void updateInnerHtmlOnly() {
		this.mode_ = DomElement.Mode.ModeUpdate;
		assert this.deleted_ == false;
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
				this.properties_.remove(i);
			}
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

	private boolean containsElement(DomElementType type) {
		for (int i = 0; i < this.childrenToAdd_.size(); ++i) {
			if (this.childrenToAdd_.get(i).child.type_ == type) {
				return true;
			}
			if (this.childrenToAdd_.get(i).child.containsElement(type)) {
				return true;
			}
		}
		return false;
	}

	private void processEvents(WApplication app) {
		DomElement self = this;
		String S_mousedown = WInteractWidget.MOUSE_DOWN_SIGNAL;
		String S_mouseup = WInteractWidget.MOUSE_UP_SIGNAL;
		String S_keypress = WInteractWidget.KEYPRESS_SIGNAL;
		DomElement.EventHandler mouseup = this.eventHandlers_.get(S_mouseup);
		if (mouseup != null && mouseup.jsCode.length() != 0) {
			MapUtils.access(self.eventHandlers_, S_mousedown,
					DomElement.EventHandler.class).jsCode = app
					.getJavaScriptClass()
					+ "._p_.saveDownPos(event);"
					+ MapUtils.access(self.eventHandlers_, S_mousedown,
							DomElement.EventHandler.class).jsCode;
		}
		DomElement.EventHandler mousedown = this.eventHandlers_
				.get(S_mousedown);
		if (mousedown != null && mousedown.jsCode.length() != 0) {
			MapUtils.access(self.eventHandlers_, S_mousedown,
					DomElement.EventHandler.class).jsCode = app
					.getJavaScriptClass()
					+ "._p_.capture(this);"
					+ MapUtils.access(self.eventHandlers_, S_mousedown,
							DomElement.EventHandler.class).jsCode;
		}
		DomElement.EventHandler keypress = this.eventHandlers_.get(S_keypress);
		if (keypress != null && keypress.jsCode.length() != 0) {
			MapUtils.access(self.eventHandlers_, S_keypress,
					DomElement.EventHandler.class).jsCode = "if (Wt2_99_2.isKeyPress(event)){"
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
					StringWriter style = new StringWriter();
					style.append("expression(Wt2_99_2.IEwidth(this,");
					if (minw != null) {
						style.append('\'').append(minw).append('\'');
						self.properties_.remove(Property.PropertyStyleMinWidth);
					} else {
						style.append("'0px'");
					}
					style.append(',');
					if (maxw != null) {
						style.append('\'').append(maxw).append('\'');
						self.properties_.remove(Property.PropertyStyleMaxWidth);
					} else {
						style.append("'100000px'");
					}
					style.append("));");
					self.properties_.put(Property.PropertyStyleWidth, style
							.toString());
				}
			}
			String i = self.properties_.get(Property.PropertyStyleMinHeight);
			if (i != null) {
				self.properties_.put(Property.PropertyStyleHeight, i);
				self.properties_.remove(Property.PropertyStyleMinHeight);
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
				out.append("Wt2_99_2.setHtml(").append(this.var_).append(',');
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
			default:
				if (i.getKey().getValue() >= Property.PropertyStylePosition
						.getValue()
						&& i.getKey().getValue() <= Property.PropertyStyleDisplay
								.getValue()) {
					out.append(this.var_).append(".style.")
							.append(
									cssCamelNames[i.getKey().getValue()
											- Property.PropertyStylePosition
													.getValue()]).append("=\'")
							.append(i.getValue()).append("\';");
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
			if (i.getKey().equals("class")) {
				out.append(this.var_).append(".className = ");
				jsStringLiteral(out, i.getValue(), '\'');
				out.append(';').append('\n');
			} else {
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
	}

	private String createAsJavaScript(EscapeOStream out, String parentVar,
			int pos) {
		this.var_ = "j" + String.valueOf(nextId_++);
		out.append("var ").append(this.var_).append("=");
		if (this.type_ == DomElementType.DomElement_TD) {
			out.append(parentVar).append(".insertCell(").append(pos).append(
					");");
		} else {
			if (this.type_ == DomElementType.DomElement_TR) {
				out.append(parentVar).append(".insertRow(").append(pos).append(
						");");
			} else {
				out.append("document.createElement('").append(
						elementNames_[this.type_.getValue()]).append("');");
				if (pos != -1) {
					out.append(parentVar).append(".insertBefore(").append(
							this.var_).append(",").append(parentVar).append(
							".childNodes[").append(pos).append("]);");
				} else {
					out.append(parentVar).append(".appendChild(").append(
							this.var_).append(");");
				}
			}
		}
		out.append('\n');
		return this.asJavaScript(out, DomElement.Priority.Create);
	}

	private DomElement.Mode mode_;
	private boolean wasEmpty_;
	private boolean deleted_;
	private boolean removeAllChildren_;
	private boolean hideWithDisplay_;
	private boolean minMaxSizeProperties_;
	private DomElement replaced_;
	private DomElement insertBefore_;
	private DomElementType type_;
	private String id_;
	private int numManipulations_;
	private List<String> methodCalls_;
	private int timeOut_;
	private boolean timeOutJSRepeat_;
	private String javaScript_;
	private String javaScriptEvenWhenDeleted_;
	private String var_;

	static class EventHandler {
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

	private Map<String, String> AttributeMap;
	private Map<Property, String> PropertyMap;
	private Map<String, DomElement.EventHandler> EventHandlerMap;
	private Map<String, String> attributes_;
	private Map<Property, String> properties_;
	private Map<String, DomElement.EventHandler> eventHandlers_;

	private static class ChildInsertion {
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
	private StringWriter childrenHtml_;
	private List<DomElement.TimeoutEvent> timeouts_;
	private boolean discardWithParent_;
	private static final String CREATE = "document.createElement('";
	private static String[] cssNames = { "position", "z-index", "float",
			"clear", "width", "height", "line-height", "min-width",
			"min-height", "max-width", "max-height", "left", "right", "top",
			"bottom", "vertical-align", "text-align", "padding", "margin-top",
			"margin-right", "margin-bottom", "margin-left", "cursor",
			"border-top", "border-right", "border-bottom", "border-left",
			"color", "overflow", "overflow", "font-family", "font-style",
			"font-variant", "font-weight", "font-size", "background-color",
			"background-image", "background-repeat", "background-attachment",
			"background-position", "text-decoration", "white-space",
			"table-layout", "border-spacing", "visibility", "display" };
	private static String[] cssCamelNames = { "position", "zIndex", "cssFloat",
			"clear", "width", "height", "lineHeight", "minWidth", "minHeight",
			"maxWidth", "maxHeight", "left", "right", "top", "bottom",
			"verticalAlign", "textAlign", "padding", "marginTop",
			"marginRight", "marginBottom", "marginLeft", "cursor", "borderTop",
			"borderRight", "borderBottom", "borderLeft", "color", "overflow",
			"overflow", "fontFamily", "fontStyle", "fontVariant", "fontWeight",
			"fontSize", "backgroundColor", "backgroundImage",
			"backgroundRepeat", "backgroundAttachment", "backgroundPosition",
			"textDecoration", "whiteSpace", "tableLayout", "borderSpacing",
			"visibility", "display" };
	static String[] elementNames_ = { "a", "br", "button", "col", "div",
			"fieldset", "form", "h1", "h2", "h3", "h4", "h5", "h6", "iframe",
			"img", "input", "label", "legend", "li", "ol", "option", "ul",
			"script", "select", "span", "table", "tbody", "thead", "th", "td",
			"textarea", "tr", "p", "canvas", "map", "area" };
	static boolean[] defaultInline_ = { true, true, true, false, false, false,
			false, true, false, false, false, false, false, true, true, true,
			true, true, false, false, true, false, false, true, true, false,
			false, false, false, false, true, false, false, false, false, true };
	static final String unsafeChars_ = "$&+,:;=?@'\"<>#%{}|\\^~[]`";
	private static int nextId_ = 0;
}
