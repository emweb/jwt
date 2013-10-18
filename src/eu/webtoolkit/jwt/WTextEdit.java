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
 * A rich-text XHTML editor.
 * <p>
 * 
 * The editor provides interactive editing of XHTML text. By default it provides
 * basic mark-up (font, formatting, color, links, and lists), but additional
 * buttons may be added to the tool bars that add additional formatting options.
 * <p>
 * The implementation is based on <a
 * href="http://tinymce.moxiecode.com/">TinyMCE</a>. The widget may be
 * configured and tailored using the
 * {@link WTextEdit#setConfigurationSetting(String name, Object value)
 * setConfigurationSetting()} and related methods that provide direct access to
 * the underlying TinyMCE component.
 * <p>
 * You can use this widget with TinyMCE version 3 or version 4.
 * <p>
 * The choice is global and set using
 * {@link Configuration#setTinyMCEVersion(int)}.
 * <p>
 * <h3>CSS</h3>
 * <p>
 * Styling through CSS is not applicable.
 * <p>
 * <div align="center"> <img src="doc-files//WTextEdit-1.png"
 * alt="Default configuration of a WTextEdit">
 * <p>
 * <strong>Default configuration of a WTextEdit</strong>
 * </p>
 * </div>
 */
public class WTextEdit extends WTextArea {
	private static Logger logger = LoggerFactory.getLogger(WTextEdit.class);

	/**
	 * Creates a new text editor.
	 */
	public WTextEdit(WContainerWidget parent) {
		super(parent);
		this.onChange_ = new JSignal(this, "change");
		this.contentChanged_ = false;
		this.configurationSettings_ = new HashMap<String, Object>();
		this.init();
	}

	/**
	 * Creates a new text editor.
	 * <p>
	 * Calls {@link #WTextEdit(WContainerWidget parent)
	 * this((WContainerWidget)null)}
	 */
	public WTextEdit() {
		this((WContainerWidget) null);
	}

	/**
	 * Creates a new text editor and initialize with given text.
	 * <p>
	 * The <code>text</code> should be valid XHTML.
	 */
	public WTextEdit(String text, WContainerWidget parent) {
		super(text, parent);
		this.onChange_ = new JSignal(this, "change");
		this.contentChanged_ = false;
		this.configurationSettings_ = new HashMap<String, Object>();
		this.init();
	}

	/**
	 * Creates a new text editor and initialize with given text.
	 * <p>
	 * Calls {@link #WTextEdit(String text, WContainerWidget parent) this(text,
	 * (WContainerWidget)null)}
	 */
	public WTextEdit(String text) {
		this(text, (WContainerWidget) null);
	}

	/**
	 * Destructor.
	 */
	public void remove() {
		this.setParentWidget((WWidget) null);
		super.remove();
	}

	/**
	 * Returns the TinyMCE version.
	 * <p>
	 * This returns the configured version of TinyMCE (currently 3 or 4).
	 */
	public int getVersion() {
		return this.version_;
	}

	/**
	 * Sets the content.
	 * <p>
	 * The <code>text</code> should be valid XHTML.
	 * <p>
	 * The default value is &quot;&quot;.
	 */
	public void setText(String text) {
		super.setText(text);
		this.contentChanged_ = true;
	}

	/**
	 * Sets the stylesheet for displaying the content.
	 * <p>
	 * The content is rendered using the rules defined in this stylesheet. The
	 * stylesheet is also used to derive additional styles that are available in
	 * the text editor, for example in the &quot;styleselect&quot; button.
	 * <p>
	 * Multiple stylesheets may be specified as a comma separated list.
	 */
	public void setStyleSheet(String uri) {
		this.setConfigurationSetting("content_css", uri);
	}

	/**
	 * Returns the content stylesheet.
	 * <p>
	 * 
	 * @see WTextEdit#setStyleSheet(String uri)
	 */
	public String getStyleSheet() {
		return StringUtils
				.asString(this.getConfigurationSetting("content_css"))
				.toString();
	}

	/**
	 * Loads additional TinyMCE plugins.
	 * <p>
	 * Wt loads by default only the plugin &apos;safari&apos; (which adds
	 * support for the Safari web browser). Use this method to load additional
	 * plugins. Multiple plugins may be specified as a comma separated list.
	 * <p>
	 * The various plugins are described in the <a
	 * href="http://www.tinymce.com/wiki.php/Plugins">TinyMCE documentation</a>.
	 * <p>
	 * <p>
	 * <i><b>Note: </b>Plugins can only be loaded before the initial display of
	 * the widget. </i>
	 * </p>
	 */
	public void setExtraPlugins(String plugins) {
		this.setConfigurationSetting("plugins", plugins);
	}

	/**
	 * Returns the extra plugins.
	 * <p>
	 * 
	 * @see WTextEdit#setExtraPlugins(String plugins)
	 */
	public String getExtraPlugins() {
		return StringUtils.asString(this.getConfigurationSetting("plugins"))
				.toString();
	}

	/**
	 * Configures a tool bar.
	 * <p>
	 * This configures the buttons for the <code>i&apos;th</code> tool bar (with
	 * 0 &lt;= <code>i</code> &lt;= 3).
	 * <p>
	 * <h3>TinyMCE 3</h3>
	 * <p>
	 * The syntax and available buttons is documented in the <a href=
	 * "http://www.tinymce.com/wiki.php/Configuration3x:theme_advanced_buttons_1_n"
	 * >TinyMCE documentation</a>.
	 * <p>
	 * The default <i>config</i> for the first tool bar (<code>i</code> = 0) is:
	 * &quot;fontselect, |, bold, italic, underline, |, fontsizeselect, |,
	 * forecolor, backcolor, |, justifyleft, justifycenter, justifyright,
	 * justifyfull, |, anchor, |, numlist, bullist&quot;.
	 * <p>
	 * By default, the other three tool bars are disabled (<code>config</code> =
	 * &quot;&quot;).
	 * <p>
	 * <h3>TinyMCE 4</h3>
	 * <p>
	 * The syntax and available buttons is documented in the <a
	 * href="http://www.tinymce.com/wiki.php/Configuration:toolbar%3CN%3E"
	 * >TinyMCEdocumentation</a>.
	 * <p>
	 * The default <i>config</i> for the first tool bar (<code>i</code> = 0) is:
	 * &quot;undo redo | styleselect | bold italic | link&quot;.
	 * <p>
	 * Some buttons are only available after loading extra plugins using
	 * {@link WTextEdit#setExtraPlugins(String plugins) setExtraPlugins()}.
	 * <p>
	 * <p>
	 * <i><b>Note: </b>The tool bar configuration can only be set before the
	 * initial display of the widget. </i>
	 * </p>
	 */
	public void setToolBar(int i, String config) {
		String setting = "";
		if (this.version_ < 4) {
			setting = "theme_advanced_buttons";
		} else {
			setting = "toolbar";
		}
		this.setConfigurationSetting(setting + String.valueOf(i + 1), config);
	}

	/**
	 * Returns a tool bar configuration.
	 * <p>
	 * 
	 * @see WTextEdit#setToolBar(int i, String config)
	 */
	public String getToolBar(int i) {
		String setting = "";
		if (this.version_ < 4) {
			setting = "theme_advanced_buttons";
		} else {
			setting = "toolbar";
		}
		return StringUtils.asString(
				this.getConfigurationSetting(setting + String.valueOf(i + 1)))
				.toString();
	}

	/**
	 * Configure a TinyMCE setting.
	 * <p>
	 * A list of possible settings can be found at: <a
	 * href="http://tinymce.moxiecode.com/wiki.php/Configuration"
	 * >http://tinymce.moxiecode.com/wiki.php/Configuration</a>
	 * <p>
	 * The widget itself will also define a number of configuration settings and
	 * these may be overridden using this method.
	 */
	public void setConfigurationSetting(String name, Object value) {
		this.configurationSettings_.put(name, value);
	}

	/**
	 * Returns a TinyMCE configuration setting&apos;s value.
	 * <p>
	 * An empty boost::any is returned when no value could be found for the
	 * provided argument.
	 */
	public Object getConfigurationSetting(String name) {
		Object it = this.configurationSettings_.get(name);
		if (it != null) {
			return it;
		} else {
			return null;
		}
	}

	public void resize(WLength width, WLength height) {
		super.resize(width, height);
	}

	String renderRemoveJs() {
		if (this.isRendered()) {
			return this.getJsRef() + ".ed.remove();Wt3_3_1.remove('"
					+ this.getId() + "');";
		} else {
			return super.renderRemoveJs();
		}
	}

	void updateDom(DomElement element, boolean all) {
		super.updateDom(element, all);
		if (element.getType() == DomElementType.DomElement_TEXTAREA) {
			element.removeProperty(Property.PropertyStyleDisplay);
		}
		if (all && element.getType() == DomElementType.DomElement_TEXTAREA) {
			StringWriter config = new StringWriter();
			config.append("{");
			boolean first = true;
			for (Iterator<Map.Entry<String, Object>> it_it = this.configurationSettings_
					.entrySet().iterator(); it_it.hasNext();) {
				Map.Entry<String, Object> it = it_it.next();
				if (it.getKey().equals("plugins")) {
					continue;
				}
				if (!first) {
					config.append(',');
				}
				first = false;
				config.append(it.getKey()).append(": ").append(
						StringUtils.asJSLiteral(it.getValue(),
								TextFormat.XHTMLUnsafeText));
			}
			if (!first) {
				config.append(',');
			}
			config.append("plugins: '").append(this.getPlugins()).append("'");
			config.append(",init_instance_callback: obj.init}");
			DomElement dummy = new DomElement(DomElement.Mode.ModeUpdate,
					DomElementType.DomElement_TABLE);
			this.updateDom(dummy, true);
			element.callJavaScript("(function() { var obj = $('#"
					+ this.getId() + "').data('obj');obj.render("
					+ config.toString() + ","
					+ jsStringLiteral(dummy.getCssStyle()) + ","
					+ (this.changed().isConnected() ? "true" : "false")
					+ ");})();");
			this.contentChanged_ = false;
		}
		if (!all && this.contentChanged_) {
			element.callJavaScript(this.getJsRef() + ".ed.load();");
			this.contentChanged_ = false;
		}
	}

	void getDomChanges(List<DomElement> result, WApplication app) {
		DomElement e = DomElement.getForUpdate(this.getFormName() + "_tbl",
				DomElementType.DomElement_TABLE);
		this.updateDom(e, false);
		super.getDomChanges(result, app);
		result.add(e);
	}

	boolean domCanBeSaved() {
		return false;
	}

	protected int boxPadding(Orientation orientation) {
		return 0;
	}

	protected int boxBorder(Orientation orientation) {
		return 0;
	}

	private JSignal onChange_;
	private int version_;
	private boolean contentChanged_;
	private Map<String, Object> configurationSettings_;

	private String getPlugins() {
		String plugins = this.getExtraPlugins();
		if (this.version_ == 3) {
			if (plugins.length() != 0) {
				plugins += ",";
			}
			plugins += "safari";
		}
		return plugins;
	}

	private void init() {
		WApplication app = WApplication.getInstance();
		this.setInline(false);
		initTinyMCE();
		this.version_ = getTinyMCEVersion();
		this.setJavaScriptMember(" WTextEdit", "new Wt3_3_1.WTextEdit("
				+ app.getJavaScriptClass() + "," + this.getJsRef() + ");");
		this.setJavaScriptMember(WT_RESIZE_JS,
				"function(e, w, h) { var obj = $('#" + this.getId()
						+ "').data('obj'); obj.wtResize(e, w, h); };");
		String direction = app.getLayoutDirection() == LayoutDirection.LeftToRight ? "ltr"
				: "rtl";
		this.setConfigurationSetting("directionality", direction);
		String toolbar = "";
		if (this.version_ < 4) {
			toolbar = "fontselect,|,bold,italic,underline,|,fontsizeselect,|,forecolor,backcolor,|,justifyleft,justifycenter,justifyright,justifyfull,|,anchor,|,numlist,bullist";
		} else {
			toolbar = "undo redo | styleselect | bold italic | link";
		}
		this.setToolBar(0, toolbar);
		for (int i = 1; i <= 3; i++) {
			this.setToolBar(i, "");
		}
		this.setConfigurationSetting("doctype", WApplication.getInstance()
				.getDocType());
		this.setConfigurationSetting("relative_urls", true);
		if (this.version_ < 4) {
			this.setConfigurationSetting("button_tile_map", true);
			this.setConfigurationSetting("theme", "advanced");
			this.setConfigurationSetting("theme_advanced_toolbar_location",
					"top");
			this
					.setConfigurationSetting("theme_advanced_toolbar_align",
							"left");
		}
		this.onChange_.addListener(this, new Signal.Listener() {
			public void trigger() {
				WTextEdit.this.propagateOnChange();
			}
		});
	}

	private void propagateOnChange() {
		this.changed().trigger();
	}

	private static void initTinyMCE() {
		String THIS_JS = "js/WTextEdit.js";
		WApplication app = WApplication.getInstance();
		if (!app.isJavaScriptLoaded(THIS_JS)) {
			if (app.getEnvironment().hasAjax()) {
				app
						.doJavaScript("window.tinyMCE_GZ = { loaded: true };",
								false);
			}
			int version = getTinyMCEVersion();
			String folder = version == 3 ? "tiny_mce/" : "tinymce/";
			String jsFile = version == 3 ? "tiny_mce.js" : "tinymce.js";
			String tinyMCEBaseURL = WApplication.getRelativeResourcesUrl()
					+ folder;
			tinyMCEBaseURL = WApplication.readConfigurationProperty(
					"tinyMCEBaseURL", tinyMCEBaseURL);
			if (tinyMCEBaseURL.length() != 0
					&& tinyMCEBaseURL.charAt(tinyMCEBaseURL.length() - 1) != '/') {
				tinyMCEBaseURL += '/';
			}
			app.require(tinyMCEBaseURL + jsFile, "window['tinyMCE']");
			app.getStyleSheet().addRule(".mceEditor",
					"display: block; position: absolute;");
			app.loadJavaScript(THIS_JS, wtjs1());
		}
	}

	private static int getTinyMCEVersion() {
		String version = "3";
		version = WApplication.readConfigurationProperty("tinyMCEVersion",
				version);
		return Integer.parseInt(version);
	}

	static WJavaScriptPreamble wtjs1() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptConstructor,
				"WTextEdit",
				"function(p,d){jQuery.data(d,\"obj\",this);var n,o,q=this,b=p.WT,r;if(!tinymce.dom.Event.domLoaded)tinymce.dom.Event.domLoaded=true;tinyMCE.init({mode:\"none\"});this.render=function(a,f,c){r=f;d.ed=new tinymce.Editor(d.id,a,tinymce.EditorManager);d.ed.render();c&&d.ed.onChange.add(function(){p.emit(d,\"change\")})};this.init=function(){var a=b.getElement(d.id+\"_ifr\"),f,c;if(tinymce.EditorManager.majorVersion<4){c=f=a.parentNode.parentNode.parentNode.parentNode; f=f.parentNode}else f=a.parentNode.parentNode.parentNode;if(c){c.style.cssText=\"width:100%;\"+r;d.style.height=c.offsetHeight+\"px\"}f.wtResize=d.wtResize;b.isGecko?setTimeout(function(){q.wtResize(d,n,o)},100):q.wtResize(d,n,o);$((b.isIE?document.frames[a.id].document:a.contentDocument).body).bind(\"paste\",function(k){function e(m){return m.indexOf(\"image/\")==0}var h=k.clipboardData||k.originalEvent.clipboardData,j,l;if(h&&h.types){j=0;for(l=h.types.length;j<l;++j)if(e(h.types[j])||e(h.items[j].type)){var i= h.items[j].getAsFile(),g=new FileReader;g.onload=function(){d.ed.insertContent('<img src=\"'+this.result+'\"></img>')};g.readAsDataURL(i);b.cancelEvent(k)}}})};this.wtResize=function(a,f,c){if(!(c<0)){var k=b.getElement(a.id+\"_ifr\");if(k){var e=0;b.px(a,\"marginLeft\");b.px(a,\"marginRight\");e=b.px(a,\"marginTop\")+b.px(a,\"marginBottom\");if(!b.boxSizing(a)){b.px(a,\"borderLeftWidth\");b.px(a,\"borderRightWidth\");b.px(a,\"paddingLeft\");b.px(a,\"paddingRight\");e+=b.px(a,\"borderTopWidth\")+b.px(a,\"borderBottomWidth\")+ b.px(a,\"paddingTop\")+b.px(a,\"paddingBottom\")}a.style.height=c-e+\"px\";var h,j=d.style.position!==\"absolute\";if(tinymce.EditorManager.majorVersion<4){var l=k.parentNode.parentNode,i=l.parentNode.parentNode,g,m;h=i;e=i.parentNode;if(!j&&typeof f!==\"undefined\")e.style.width=f-2+\"px\";g=0;for(m=i.rows.length;g<m;g++)if(i.rows[g]!=l)c-=i.rows[g].offsetHeight}else{l=k.parentNode;i=l.parentNode;e=i.parentNode;if(!j&&typeof f!==\"undefined\")e.style.width=f-2+\"px\";g=0;for(m=i.children.length;g<m;g++)if(i.children[g]!= l)c-=i.children[g].offsetHeight+1;c-=1}if(!(c<0)){c+=\"px\";if(j){e.style.position=\"static\";e.style.display=\"block\"}else{e.style.position=a.style.position;e.style.left=a.style.left;e.style.top=a.style.top;if(!j&&h)h.style.width=f+\"px\";if(h){h.style.height=c+\"px\";e.style.height=a.style.height}}if(k.style.height!=c){k.style.height=c;p.layouts2.setElementDirty(d)}}}else{n=f;o=c}}};o=d.offsetHeight;n=d.offsetWidth}");
	}
}
