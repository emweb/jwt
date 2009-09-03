/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.io.StringWriter;
import java.util.List;

/**
 * A rich-text XHTML editor
 * <p>
 * 
 * The editor provides interactive editing of XHTML text. By default it provides
 * basic mark-up (font, formatting, color, links, and lists), but additional
 * buttons may be added to the tool bars that add additional formatting options.
 * <p>
 * The implementation is based on <a
 * href="http://tinymce.moxiecode.com/">TinyMCE</a>. The widget may be
 * configured and tailored using the
 * {@link WTextEdit#setExtraPlugins(String plugins) setExtraPlugins() } and
 * {@link WTextEdit#setToolBar(int i, String config) setToolBar() } methods that
 * provide direct access to the underlying TinyMCE component.
 * <p>
 * To use this widget, you need to download TinyMCE (version 3.0.7 or later) and
 * deploy the tinymce/jscripts/tiny_mce folder to <i>tinyMCEURL</i>. The default
 * value for <i>tinyMCEURL</i> is <i>resourcesURL</i>&quot;/tiny_mce&quot;,
 * where <i>resourcesURL</i> is the configuration property that locates the JWt
 * resources/ folder (i.e., we assume by default that you copy the tiny_mce
 * folder to the resources/ folder).
 * <p>
 * The value may be overridden with a URL that points to the directory where the
 * tiny_mce folder is located, by configuring the <i>tinyMCEURL</i> property in
 * your JWt configuration file.
 * <p>
 * <div align="center"> <img src="doc-files//WTextEdit-1.png"
 * alt="Default configuration of a WTextEdit">
 * <p>
 * <strong>Default configuration of a WTextEdit</strong>
 * </p>
 * </div>
 */
public class WTextEdit extends WTextArea {
	/**
	 * Create a new text editor.
	 */
	public WTextEdit(WContainerWidget parent) {
		super();
		this.contentChanged_ = false;
		this.styleSheet_ = "";
		this.extraPlugins_ = "";
		for (int i = 0; i < 4; ++i) {
			this.buttons_[i] = "";
		}
		this.setInline(false);
		initTinyMCE();
		if (parent != null) {
			parent.addWidget(this);
		}
	}

	/**
	 * Create a new text editor.
	 * <p>
	 * Calls {@link #WTextEdit(WContainerWidget parent)
	 * this((WContainerWidget)null)}
	 */
	public WTextEdit() {
		this((WContainerWidget) null);
	}

	/**
	 * Create a new text editor and initialize with given text.
	 * <p>
	 * The <i>text</i> should be valid XHTML.
	 */
	public WTextEdit(String text, WContainerWidget parent) {
		super(text);
		this.contentChanged_ = false;
		this.styleSheet_ = "";
		this.extraPlugins_ = "";
		this.setInline(false);
		initTinyMCE();
		if (parent != null) {
			parent.addWidget(this);
		}
	}

	/**
	 * Create a new text editor and initialize with given text.
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
		this.setParent((WWidget) null);
		super.remove();
	}

	/**
	 * Set the content.
	 * <p>
	 * The <i>text</i> should be valid XHTML.
	 * <p>
	 * The default value is &quot;&quot;.
	 */
	public void setText(String text) {
		super.setText(text);
		this.contentChanged_ = true;
	}

	/**
	 * Set the stylesheet for displaying the content.
	 * <p>
	 * The content is rendered using the rules defined in this stylesheet. The
	 * stylesheet is also used to derive additional styles that are available in
	 * the text editor, for example in the &quot;styleselect&quot; button.
	 * <p>
	 * Multiple stylesheets may be specified as a comma separated list.
	 */
	public void setStyleSheet(String uri) {
		this.styleSheet_ = uri;
	}

	/**
	 * Returns the content stylesheet.
	 * <p>
	 * 
	 * @see WTextEdit#setStyleSheet(String uri)
	 */
	public String getStyleSheet() {
		return this.styleSheet_;
	}

	/**
	 * Load additional TinyMCE plugins.
	 * <p>
	 * Wt loads by default only the plugin &apos;safari&apos; (which adds
	 * support for the Safari web browser). Use this method to load additional
	 * plugins. Multiple plugins may be specified as a comma separated list.
	 * <p>
	 * The various plugins are described in the <a
	 * href="http://wiki.moxiecode.com/index.php/TinyMCE:Plugins">TinyMCE
	 * documentation</a>.
	 * <p>
	 * <p>
	 * <i><b>Note:</b>Plugins can only be loaded before the initial display of
	 * the widget. </i>
	 * </p>
	 */
	public void setExtraPlugins(String plugins) {
		this.extraPlugins_ = plugins;
	}

	/**
	 * Returns the extra plugins.
	 * <p>
	 * 
	 * @see WTextEdit#setExtraPlugins(String plugins)
	 */
	public String getExtraPlugins() {
		return this.extraPlugins_;
	}

	/**
	 * Configure a tool bar.
	 * <p>
	 * This configures the buttons for the <i>i&apos;th</i> tool bar (with 0
	 * &lt;= <i>i</i> &lt;= 3).
	 * <p>
	 * The syntax and available buttons is documented in the <a href="http://wiki.moxiecode.com/index.php/TinyMCE:Configuration/theme_advanced_buttons_1_n"
	 * >TinyMCE documentation</a>.
	 * <p>
	 * The default <i>config</i> for the first tool bar (<i>i</i> = 0) is:
	 * &quot;fontselect, |, bold, italic, underline, |, fontsizeselect, |,
	 * forecolor, backcolor, |, justifyleft, justifycenter, justifyright,
	 * justifyfull, |, anchor, |, numlist, bullist&quot;.
	 * <p>
	 * By default, the other three tool bars are disabled (<i>config</i> =
	 * &quot;&quot;).
	 * <p>
	 * Note that some buttons are only available after loading extra plugins
	 * using {@link WTextEdit#setExtraPlugins(String plugins) setExtraPlugins()
	 * }.
	 * <p>
	 * <p>
	 * <i><b>Note:</b>The tool bar configuration can only be set before the
	 * initial display of the widget. </i>
	 * </p>
	 */
	public void setToolBar(int i, String config) {
		this.buttons_[i] = config;
	}

	/**
	 * Returns a tool bar configuration.
	 * <p>
	 * 
	 * @see WTextEdit#setToolBar(int i, String config)
	 */
	public String getToolBar(int i) {
		return this.buttons_[i];
	}

	public void resize(WLength width, WLength height) {
		super.resize(width, height);
	}

	public void load() {
		WApplication
				.getInstance()
				.addAutoJavaScript(
						"{var e="
								+ this.getJsRef()
								+ ";if(e && e.ed){e.ed.save();Wt2_99_5.tinyMCEAdjust(e);}}");
		this.buttons_[0] = "fontselect,|,bold,italic,underline,|,fontsizeselect,|,forecolor,backcolor,|,justifyleft,justifycenter,justifyright,justifyfull,|,anchor,|,numlist,bullist";
		super.load();
	}

	protected DomElement renderRemove() {
		DomElement e = super.renderRemove();
		e.callJavaScript(this.getJsRef() + ".ed.remove();", true);
		return e;
	}

	protected void updateDom(DomElement element, boolean all) {
		super.updateDom(element, all);
		if (element.getType() == DomElementType.DomElement_TEXTAREA) {
			element.removeProperty(Property.PropertyStyleDisplay);
		}
		if (all && element.getType() == DomElementType.DomElement_TEXTAREA) {
			StringWriter config = new StringWriter();
			config.append("{button_tile_map:true,doctype:'"
					+ WApplication.getInstance().getDocType()
					+ "',relative_urls:true,plugins:'safari");
			if (this.extraPlugins_.length() != 0) {
				config.append(',').append(this.extraPlugins_);
			}
			config.append("'");
			config.append(",theme:'advanced'");
			for (int i = 0; i < 4; ++i) {
				config.append(",theme_advanced_buttons").append(
						String.valueOf(i + 1)).append(":'").append(
						this.buttons_[i]).append('\'');
			}
			config
					.append(",theme_advanced_toolbar_location:'top',theme_advanced_toolbar_align:'left'");
			if (this.styleSheet_.length() != 0) {
				config.append(",content_css: '").append(this.styleSheet_)
						.append('\'');
			}
			String init_cb = WApplication.getInstance().getJavaScriptClass()
					+ ".tmce" + this.getFormName();
			config.append(",init_instance_callback: '").append(init_cb).append(
					"'}");
			DomElement dummy = new DomElement(DomElement.Mode.ModeUpdate,
					DomElementType.DomElement_TABLE);
			this.updateDom(dummy, true);
			element.callJavaScript("{var e=" + this.getJsRef()
					+ ";e.ed=new tinymce.Editor('" + this.getFormName() + "',"
					+ config.toString() + ");e.ed.render();}");
			element.callJavaScript(init_cb
					+ "=function(){var d=Wt2_99_5.getElement('"
					+ this.getFormName()
					+ "_tbl'); d.style.cssText='width:100%;"
					+ dummy.getCssStyle() + "';Wt2_99_5.tinyMCEAdjust("
					+ this.getJsRef() + ");};");
			this.contentChanged_ = false;
		}
		if (!all && this.contentChanged_) {
			element.callJavaScript(this.getJsRef() + ".ed.load();");
			this.contentChanged_ = false;
		}
	}

	protected void getDomChanges(List<DomElement> result, WApplication app) {
		DomElement e = DomElement.getForUpdate(this.getFormName() + "_tbl",
				DomElementType.DomElement_TABLE);
		this.updateDom(e, false);
		super.getDomChanges(result, app);
		result.add(e);
	}

	protected int boxPadding(Orientation orientation) {
		return 0;
	}

	protected int boxBorder(Orientation orientation) {
		return 0;
	}

	private boolean contentChanged_;
	private String styleSheet_;
	private String extraPlugins_;
	private String[] buttons_ = new String[4];

	private static void initTinyMCE() {
		String tinyMCEBaseURL = WApplication.getResourcesUrl() + "tiny_mce/";
		tinyMCEBaseURL = WApplication.readConfigurationProperty(
				"tinyMCEBaseURL", tinyMCEBaseURL);
		if (tinyMCEBaseURL.length() != 0
				&& tinyMCEBaseURL.charAt(tinyMCEBaseURL.length() - 1) != '/') {
			tinyMCEBaseURL += '/';
		}
		WApplication app = WApplication.getInstance();
		app.doJavaScript("window.tinyMCE_GZ = { loaded: true };", false);
		if (app.require(tinyMCEBaseURL + "tiny_mce.js", "window['tinyMCE']")) {
			app.doJavaScript("tinymce.dom.Event._pageInit();tinyMCE.init();",
					false);
			app.getStyleSheet().addRule(".mceEditor", "height: 100%;");
			app
					.doJavaScript(
							"Wt2_99_5.tinyMCEAdjust=function(e){if (!e.ed.contentAreaContainer) return;var tbl=Wt2_99_5.getElement(e.id + '_tbl');var iframe = e.ed.contentAreaContainer.firstChild;var th=Wt2_99_5.pxself(tbl, 'height');if (th==0)if (e.parentNode.className=='Wt-grtd') {iframe.style.height='0px';th=e.parentNode.offsetHeight-Wt2_99_5.pxself(e.parentNode, 'paddingTop')-Wt2_99_5.pxself(e.parentNode, 'paddingBottom');} else return;th -= iframe.parentNode.offsetTop + 2;if (th <= 0)return;var nh=th+'px';if (iframe.style.height != nh) iframe.style.height=nh;};",
							false);
		}
	}
}
