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
 * {@link WTextEdit#setExtraPlugins(String plugins) setExtraPlugins()} and
 * {@link WTextEdit#setToolBar(int i, String config) setToolBar()} methods that
 * provide direct access to the underlying TinyMCE component.
 * <p>
 * The value may be overridden with a URL that points to the directory where the
 * <code>tiny_mce</code> folder is located, by configuring the <i>tinyMCEURL</i>
 * property in your JWt configuration file, see DOCREF<a class="el"
 * href="overview.html#config_general">configuration properties</a>.
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
	/**
	 * Creates a new text editor.
	 */
	public WTextEdit(WContainerWidget parent) {
		super(parent);
		this.contentChanged_ = false;
		this.styleSheet_ = "";
		this.extraPlugins_ = "";
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
		this.contentChanged_ = false;
		this.styleSheet_ = "";
		this.extraPlugins_ = "";
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
	 * Loads additional TinyMCE plugins.
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
	 * <i><b>Note: </b>Plugins can only be loaded before the initial display of
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
	 * Configures a tool bar.
	 * <p>
	 * This configures the buttons for the <code>i&apos;th</code> tool bar (with
	 * 0 &lt;= <code>i</code> &lt;= 3).
	 * <p>
	 * The syntax and available buttons is documented in the <a href="http://wiki.moxiecode.com/index.php/TinyMCE:Configuration/theme_advanced_buttons_1_n"
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
	 * Note that some buttons are only available after loading extra plugins
	 * using {@link WTextEdit#setExtraPlugins(String plugins) setExtraPlugins()}.
	 * <p>
	 * <p>
	 * <i><b>Note: </b>The tool bar configuration can only be set before the
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

	DomElement renderRemove() {
		DomElement e = super.renderRemove();
		e.callJavaScript(this.getJsRef() + ".ed.remove();", true);
		return e;
	}

	void updateDom(DomElement element, boolean all) {
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
			config.append(",init_instance_callback: ").append(this.getJsRef())
					.append(".init").append("}");
			DomElement dummy = new DomElement(DomElement.Mode.ModeUpdate,
					DomElementType.DomElement_TABLE);
			this.updateDom(dummy, true);
			element.callMethod("init=function(){var d=Wt3_1_5.getElement('"
					+ this.getId() + "_tbl');d.style.cssText='width:100%;"
					+ dummy.getCssStyle() + "';};");
			element.callMethod("ed=new tinymce.Editor('" + this.getId() + "',"
					+ config.toString() + ");");
			element.callMethod("ed.render();");
			element.callMethod("ed.onChange.add(function(ed) { ed.save(); });");
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

	private void init() {
		for (int i = 0; i < 4; ++i) {
			this.buttons_[i] = "";
		}
		this.setInline(false);
		this.buttons_[0] = "fontselect,|,bold,italic,underline,|,fontsizeselect,|,forecolor,backcolor,|,justifyleft,justifycenter,justifyright,justifyfull,|,anchor,|,numlist,bullist";
		initTinyMCE();
		this.setJavaScriptMember(WT_RESIZE_JS,
				"function(e,w,h){Wt3_1_5.tinyMCEResize(e, w, h); };");
	}

	private static void initTinyMCE() {
		String tinyMCEBaseURL = WApplication.getResourcesUrl() + "tiny_mce/";
		tinyMCEBaseURL = WApplication.readConfigurationProperty(
				"tinyMCEBaseURL", tinyMCEBaseURL);
		if (tinyMCEBaseURL.length() != 0
				&& tinyMCEBaseURL.charAt(tinyMCEBaseURL.length() - 1) != '/') {
			tinyMCEBaseURL += '/';
		}
		WApplication app = WApplication.getInstance();
		if (app.getEnvironment().hasAjax()) {
			app.doJavaScript("window.tinyMCE_GZ = { loaded: true };", false);
		}
		if (app.require(tinyMCEBaseURL + "tiny_mce.js", "window['tinyMCE']")) {
			if (app.getEnvironment().hasAjax()) {
				app.doJavaScript("tinymce.dom.Event._pageInit();", false);
			}
			app.doJavaScript("tinyMCE.init();", false);
			app.getStyleSheet().addRule(".mceEditor", "height: 100%;");
			app
					.doJavaScript(
							"Wt3_1_5.tinyMCEResize=function(e,w,h){e.style.height = (h - 2) + 'px';var iframe = Wt3_1_5.getElement(e.id + '_ifr');if (iframe) {var row=iframe.parentNode.parentNode,tbl=row.parentNode.parentNode,i, il;for (i=0, il=tbl.rows.length; i<il; i++) {if (tbl.rows[i] != row)h -= Math.max(28, tbl.rows[i].offsetHeight);}h = (h - 2) + 'px';if (iframe.style.height != h) iframe.style.height=h;}};",
							false);
		}
	}
}
