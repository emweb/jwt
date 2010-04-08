/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import eu.webtoolkit.jwt.utils.EnumUtils;

/**
 * A widget which popups to assist in editing a textarea or lineedit.
 * <p>
 * 
 * This widget may be associated with one or more {@link WFormWidget
 * WFormWidgets} (typically a {@link WLineEdit} or a {@link WTextArea}).
 * <p>
 * When the user starts editing one of the associated widgets, this popup will
 * show just below it, offering a list of suggestions that match in some way
 * with the current edit. The mechanism for filtering the total list of
 * suggestions must be specified through a separate JavaScript function. This
 * function may also highlight part(s) of the suggestions to provide feed-back
 * on how they match.
 * <p>
 * WSuggestionPopup is an MVC view class, using a simple
 * {@link WStringListModel} by default. You can set a custom model using
 * {@link WSuggestionPopup#setModel(WAbstractItemModel model) setModel()}. The
 * member methods {@link WSuggestionPopup#clearSuggestions() clearSuggestions()}
 * and
 * {@link WSuggestionPopup#addSuggestion(CharSequence suggestionText, CharSequence suggestionValue)
 * addSuggestion()} manipulate the model.
 * <p>
 * The popup may support very large datasets, by using server-side filtering of
 * suggestions based on the input. You can enable this feature using
 * {@link WSuggestionPopup#setFilterLength(int length) setFilterLength()} and
 * then listen to a filter notification using the modelFilter() signal. By using
 * {@link WSuggestionPopup#setMaximumSize(WLength width, WLength height)
 * setMaximumSize()} you can also limit the maximum height of the popup, in
 * which case scrolling is provided (similar to a combo box).
 * <p>
 * The class is initialized with an {@link Options} struct which configures how
 * suggestion filtering and result editing is done. Alternatively, you can
 * provide two JavaScript functions, one for filtering the suggestions, and one
 * for editing the value of the textarea when a suggestion is selected.
 * <p>
 * The matcherJS function block must have the following JavaScript signature:
 * <p>
 * <blockquote>
 * 
 * <pre>
 * function (editElement) {
 *    // fetch the location of cursor and current text in the editElement.
 * 
 *    // return a function that matches a given suggestion with the current value of the editElement.
 *    return function(suggestion) {
 * 
 *      // 1) if suggestion is null, simply return the current text 'value'
 *      // 2) remove markup from the suggestion
 *      // 3) check suggestion if it matches
 *      // 4) add markup to suggestion
 * 
 *      return { match : ...,      // does the suggestion match ? (boolean)
 *               suggestion : ...  // modified suggestion markup
 *              };
 *    }
 *  }
 * </pre>
 * 
 * </blockquote>
 * <p>
 * The replacerJS function block that edits the value has the following
 * JavaScript signature.
 * <p>
 * <blockquote>
 * 
 * <pre>
 * function (editElement, suggestionText, suggestionValue) {
 *    // editElement is the form element which must be edited.
 *    // suggestionText is the displayed text for the matched suggestion.
 *    // suggestionValue is the stored value for the matched suggestion.
 * 
 *    // computed modifiedEditValue and modifiedPos ...
 * 
 *    editElement.value = modifiedEditValue;
 *    editElement.selectionStart = edit.selectionEnd = modifiedPos;
 *  }
 * </pre>
 * 
 * </blockquote>
 * <p>
 * To style the suggestions, you should style the &lt;span&gt; element inside
 * this widget, and the &lt;span&gt;.&quot;sel&quot; element to style the
 * current selection.
 * <p>
 * Usage example:
 * <p>
 * <blockquote>
 * 
 * <pre>
 * // options for email address suggestions
 * WSuggestionPopup.Options contactOptions = new WSuggestionPopup.Options();
 * contactOptions.highlightBeginTag = &quot;&lt;b&gt;&quot;;
 * contactOptions.highlightEndTag = &quot;&lt;/b&gt;&quot;;
 * contactOptions.listSeparator = ','; //for multiple addresses)
 * contactOptions.whitespace = &quot; \\n&quot;;
 * contactOptions.wordSeparators = &quot;-., \&quot;@\\n;&quot;; //within an address
 * contactOptions.appendReplacedText = &quot;, &quot;; //prepare next email address
 * 
 * WSuggestionPopup popup = new WSuggestionPopup(contactOptions, this);
 * 
 * WTextArea textEdit = new WTextArea(this);
 * popup.forEdit(textEdit);
 * 
 * // load popup data
 * for (int i = 0; i &lt; contacts.size(); ++i)
 * 	popup.addSuggestion(contacts.get(i).formatted(), contacts.get(i)
 * 			.formatted());
 * </pre>
 * 
 * </blockquote>
 * <p>
 * A screenshot of this example:
 * <table border="0" align="center" cellspacing="3" cellpadding="3">
 * <tr>
 * <td><div align="center"> <img src="doc-files//WSuggestionPopup-default-1.png"
 * alt="An example WSuggestionPopup (default)">
 * <p>
 * <strong>An example WSuggestionPopup (default)</strong>
 * </p>
 * </div></td>
 * <td><div align="center"> <img
 * src="doc-files//WSuggestionPopup-polished-1.png"
 * alt="An example WSuggestionPopup (polished)">
 * <p>
 * <strong>An example WSuggestionPopup (polished)</strong>
 * </p>
 * </div></td>
 * </tr>
 * </table>
 * <p>
 * <h3>CSS</h3>
 * <p>
 * The suggestion popup is styled by the current CSS theme. The look can be
 * overridden using the <code>Wt-suggest</code> CSS class and the following
 * selectors:
 * <p>
 * <div class="fragment">
 * 
 * <pre class="fragment">
 * .Wt-suggest span : A suggestion element
 * .Wt-suggest .sel : A selected suggestion element
 * </pre>
 * 
 * </div>
 * <p>
 * <p>
 * <i><b>Note: </b>This widget requires JavaScript support. </i>
 * </p>
 */
public class WSuggestionPopup extends WCompositeWidget {
	/**
	 * A configuration object to generate a matcher and replacer JavaScript
	 * function
	 */
	public static class Options {
		/**
		 * Open tag to highlight a match in a suggestion.
		 * <p>
		 * Must be an opening markup tag, such as &lt;b&gt;.
		 */
		public String highlightBeginTag;
		/**
		 * Close tag to highlight a match in a suggestion.
		 * <p>
		 * Must be a closing markup tag, such as &lt;/b&gt;.
		 */
		public String highlightEndTag;
		/**
		 * When editing a list of values, the separator used for different
		 * items.
		 * <p>
		 * For example, &apos;,&apos; to separate different values on komma.
		 * Specify 0 (&apos;\0&apos;) for no list separation.
		 */
		public char listSeparator;
		/**
		 * When editing a value, the whitespace characters ignored before the
		 * current value.
		 * <p>
		 * For example, &quot; \\n&quot; to ignore spaces and newlines.
		 */
		public String whitespace;
		/**
		 * To show suggestions based on matches of the edited value with parts
		 * of the suggestion.
		 * <p>
		 * For example, &quot; .@&quot; will also match with suggestion text
		 * after a space, a dot (.) or an at-symbol (@).
		 */
		public String wordSeparators;
		/**
		 * When replacing the current edited value with suggestion value, append
		 * the following string as well.
		 */
		public String appendReplacedText;
	}

	/**
	 * Creates a suggestion popup with given matcherJS and replacerJS.
	 * <p>
	 * See supra for the signature of the matcher and replace JavaScript
	 * functions.
	 */
	public WSuggestionPopup(String matcherJS, String replacerJS,
			WContainerWidget parent) {
		super(parent);
		this.impl_ = new WTemplate(new WString("${shadow-x1-x2}${contents}"));
		this.model_ = null;
		this.modelColumn_ = 0;
		this.filterLength_ = 0;
		this.matcherJS_ = matcherJS;
		this.replacerJS_ = replacerJS;
		this.filterModel_ = new Signal1<WString>();
		this.modelConnections_ = new ArrayList<AbstractSignal.Connection>();
		this.filter_ = new JSignal1<String>(this.impl_, "filter") {
		};
		this.editKeyDown_ = new JSlot(parent);
		this.editKeyUp_ = new JSlot(parent);
		this.delayHide_ = new JSlot(parent);
		this.init();
	}

	/**
	 * Creates a suggestion popup with given matcherJS and replacerJS.
	 * <p>
	 * Calls
	 * {@link #WSuggestionPopup(String matcherJS, String replacerJS, WContainerWidget parent)
	 * this(matcherJS, replacerJS, (WContainerWidget)null)}
	 */
	public WSuggestionPopup(String matcherJS, String replacerJS) {
		this(matcherJS, replacerJS, (WContainerWidget) null);
	}

	/**
	 * Creates a suggestion popup with given matcher and replacer options.
	 * <p>
	 * 
	 * @see WSuggestionPopup#generateMatcherJS(WSuggestionPopup.Options options)
	 * @see WSuggestionPopup#generateReplacerJS(WSuggestionPopup.Options
	 *      options)
	 */
	public WSuggestionPopup(WSuggestionPopup.Options options,
			WContainerWidget parent) {
		super(parent);
		this.impl_ = new WTemplate(new WString("${shadow-x1-x2}${contents}"));
		this.model_ = null;
		this.modelColumn_ = 0;
		this.filterLength_ = 0;
		this.matcherJS_ = generateMatcherJS(options);
		this.replacerJS_ = generateReplacerJS(options);
		this.filterModel_ = new Signal1<WString>();
		this.modelConnections_ = new ArrayList<AbstractSignal.Connection>();
		this.filter_ = new JSignal1<String>(this.impl_, "filter") {
		};
		this.editKeyDown_ = new JSlot(parent);
		this.editKeyUp_ = new JSlot(parent);
		this.delayHide_ = new JSlot(parent);
		this.init();
	}

	/**
	 * Creates a suggestion popup with given matcher and replacer options.
	 * <p>
	 * Calls
	 * {@link #WSuggestionPopup(WSuggestionPopup.Options options, WContainerWidget parent)
	 * this(options, (WContainerWidget)null)}
	 */
	public WSuggestionPopup(WSuggestionPopup.Options options) {
		this(options, (WContainerWidget) null);
	}

	/**
	 * Lets this suggestion popup assist in editing the given edit field.
	 * <p>
	 * A single suggestion popup may assist in several edits by repeated calls
	 * of this method.
	 */
	public void forEdit(WFormWidget edit) {
		edit.keyPressed().addListener(this.editKeyDown_);
		edit.keyWentDown().addListener(this.editKeyDown_);
		edit.keyWentUp().addListener(this.editKeyUp_);
		edit.blurred().addListener(this.delayHide_);
	}

	/**
	 * Clears the list of suggestions.
	 */
	public void clearSuggestions() {
		this.model_.removeRows(0, this.model_.getRowCount());
	}

	/**
	 * Adds a new suggestion.
	 */
	public void addSuggestion(CharSequence suggestionText,
			CharSequence suggestionValue) {
		int row = this.model_.getRowCount();
		if (this.model_.insertRow(row)) {
			this.model_.setData(row, this.modelColumn_, suggestionText,
					ItemDataRole.DisplayRole);
			this.model_.setData(row, this.modelColumn_, suggestionValue,
					ItemDataRole.UserRole);
		}
	}

	/**
	 * Sets the model to be used for the suggestions.
	 * <p>
	 * The <code>model</code> may not be <code>null</code>, and ownership of the
	 * model is not transferred.
	 * <p>
	 * The default value is a {@link WStringListModel} that is owned by the
	 * suggestion popup.
	 * <p>
	 * The {@link ItemDataRole#DisplayRole} is used for the suggestion text. The
	 * {@link ItemDataRole#UserRole} is used for the suggestion value, unless
	 * empty, in which case the suggestion text is used as value.
	 * <p>
	 * Note that since the default WStringListModel does not support UserRole
	 * data, you will want to change it to a more general model (e.g.
	 * {@link WStandardItemModel}) if you want suggestion values that are
	 * different from display values.
	 * <p>
	 * 
	 * @see WSuggestionPopup#setModelColumn(int modelColumn)
	 */
	public void setModel(WAbstractItemModel model) {
		if (this.model_ != null) {
			for (int i = 0; i < this.modelConnections_.size(); ++i) {
				this.modelConnections_.get(i).disconnect();
			}
			this.modelConnections_.clear();
		}
		this.model_ = model;
		this.modelConnections_.add(this.model_.rowsInserted().addListener(this,
				new Signal3.Listener<WModelIndex, Integer, Integer>() {
					public void trigger(WModelIndex e1, Integer e2, Integer e3) {
						WSuggestionPopup.this.modelRowsInserted(e1, e2, e3);
					}
				}));
		this.modelConnections_.add(this.model_.rowsRemoved().addListener(this,
				new Signal3.Listener<WModelIndex, Integer, Integer>() {
					public void trigger(WModelIndex e1, Integer e2, Integer e3) {
						WSuggestionPopup.this.modelRowsRemoved(e1, e2, e3);
					}
				}));
		this.modelConnections_.add(this.model_.dataChanged().addListener(this,
				new Signal2.Listener<WModelIndex, WModelIndex>() {
					public void trigger(WModelIndex e1, WModelIndex e2) {
						WSuggestionPopup.this.modelDataChanged(e1, e2);
					}
				}));
		this.modelConnections_.add(this.model_.layoutChanged().addListener(
				this, new Signal.Listener() {
					public void trigger() {
						WSuggestionPopup.this.modelLayoutChanged();
					}
				}));
		this.modelConnections_.add(this.model_.modelReset().addListener(this,
				new Signal.Listener() {
					public void trigger() {
						WSuggestionPopup.this.modelLayoutChanged();
					}
				}));
		this.setModelColumn(this.modelColumn_);
	}

	/**
	 * Sets the column in the model to be used for the items.
	 * <p>
	 * The column <code>index</code> in the model will be used to retrieve data.
	 * <p>
	 * The default value is 0.
	 * <p>
	 * 
	 * @see WSuggestionPopup#setModel(WAbstractItemModel model)
	 */
	public void setModelColumn(int modelColumn) {
		this.modelColumn_ = modelColumn;
		this.content_.clear();
		this.modelRowsInserted(null, 0, this.model_.getRowCount() - 1);
	}

	/**
	 * Returns the data model.
	 * <p>
	 * 
	 * @see WSuggestionPopup#setModel(WAbstractItemModel model)
	 */
	public WAbstractItemModel getModel() {
		return this.model_;
	}

	/**
	 * Creates a matcher JavaScript function based on some generic options.
	 */
	public static String generateMatcherJS(WSuggestionPopup.Options options) {
		return ""
				+ "function (edit) {"
				+ generateParseEditJS(options)
				+ "value = edit.value.substring(start, end);return function(suggestion) {if (!suggestion)return value;var sep='"
				+ options.wordSeparators
				+ "',matched = false,i = 0,sugup = suggestion.toUpperCase(),val = value.toUpperCase(),inserted = 0;if (val.length) {while ((i != -1) && (i < sugup.length)) {var matchpos = sugup.indexOf(val, i);if (matchpos != -1) {if ((matchpos == 0)|| (sep.indexOf(sugup.charAt(matchpos - 1)) != -1)) {"
				+ (options.highlightEndTag.length() != 0 ? "suggestion = suggestion.substring(0, matchpos + inserted) + '"
						+ options.highlightBeginTag
						+ "' + suggestion.substring(matchpos + inserted,     matchpos + inserted + val.length) + '"
						+ options.highlightEndTag
						+ "' + suggestion.substring(matchpos + inserted + val.length,     suggestion.length); inserted += "
						+ String.valueOf(options.highlightBeginTag.length()
								+ options.highlightEndTag.length()) + ";"
						: "")
				+ "matched = true;}i = matchpos + 1;} else i = matchpos;}}return { match: matched,suggestion: suggestion }}}";
	}

	/**
	 * Creates a replacer JavaScript function based on some generic options.
	 */
	public static String generateReplacerJS(WSuggestionPopup.Options options) {
		return ""
				+ "function (edit, suggestionText, suggestionValue) {"
				+ generateParseEditJS(options)
				+ "edit.value = edit.value.substring(0, start) +  suggestionValue "
				+ (options.appendReplacedText.length() != 0 ? "+ '"
						+ options.appendReplacedText + "'" : "")
				+ " + edit.value.substring(end, edit.value.length); if (edit.selectionStart) {   edit.selectionStart = start + suggestionValue.length"
				+ (options.appendReplacedText.length() != 0 ? "+ "
						+ String.valueOf(2) : "")
				+ ";   edit.selectionEnd = start + suggestionValue.length"
				+ (options.appendReplacedText.length() != 0 ? "+ "
						+ String.valueOf(2) : "") + "; }}";
	}

	/**
	 * Sets the minimum input length before showing the popup.
	 * <p>
	 * When the user has typed this much characters,
	 * {@link WSuggestionPopup#filterModel() filterModel()} is emitted which
	 * allows you to filter the model based on the initial input.
	 * <p>
	 * The default value is 0, which has the effect of always show the entire
	 * model.
	 * <p>
	 * 
	 * @see WSuggestionPopup#filterModel()
	 */
	public void setFilterLength(int length) {
		this.filterLength_ = length;
	}

	/**
	 * Returns the filter length.
	 * <p>
	 * 
	 * @see WSuggestionPopup#setFilterLength(int length)
	 */
	public int getFilterLength() {
		return this.filterLength_;
	}

	/**
	 * Signal that indicates that the model should be filtered.
	 * <p>
	 * The argument is the initial input (of length
	 * {@link WSuggestionPopup#getFilterLength() getFilterLength()}).
	 */
	public Signal1<WString> filterModel() {
		return this.filterModel_;
	}

	public void setMaximumSize(WLength width, WLength height) {
		this.content_.setMaximumSize(width, height);
	}

	private WTemplate impl_;
	private WAbstractItemModel model_;
	private int modelColumn_;
	private int filterLength_;
	private String matcherJS_;
	private String replacerJS_;
	private WContainerWidget content_;
	private Signal1<WString> filterModel_;
	private List<AbstractSignal.Connection> modelConnections_;
	private JSignal1<String> filter_;
	private JSlot editKeyDown_;
	private JSlot editKeyUp_;
	private JSlot delayHide_;

	private void init() {
		this.setImplementation(this.impl_);
		this.impl_.setStyleClass("Wt-suggest Wt-outset");
		this.impl_.bindString("shadow-x1-x2", WTemplate.DropShadow_x1_x2);
		this.impl_.bindWidget("contents",
				this.content_ = new WContainerWidget());
		this.content_.setStyleClass("content");
		this.setPopup(true);
		this.setPositionScheme(PositionScheme.Absolute);
		this.setJavaScript(this.editKeyDown_, "editKeyDown");
		this.setJavaScript(this.editKeyUp_, "editKeyUp");
		this.setJavaScript(this.delayHide_, "delayHide");
		this.hide();
		this.setModel(new WStringListModel(this));
		this.filter_.addListener(this, new Signal1.Listener<String>() {
			public void trigger(String e1) {
				WSuggestionPopup.this.doFilter(e1);
			}
		});
	}

	private void doFilter(String input) {
		this.filterModel_.trigger(new WString(input));
		WApplication app = WApplication.getInstance();
		app.doJavaScript("jQuery.data(" + this.getJsRef()
				+ ", 'obj').filtered(" + WWebWidget.jsStringLiteral(input)
				+ ")");
	}

	private void setJavaScript(JSlot slot, String methodName) {
		String jsFunction = "function(obj, event) {jQuery.data("
				+ this.getJsRef() + ", 'obj')." + methodName + "(obj, event);}";
		slot.setJavaScript(jsFunction);
	}

	private void modelRowsInserted(WModelIndex parent, int start, int end) {
		if (this.modelColumn_ >= this.model_.getColumnCount()) {
			return;
		}
		if ((parent != null)) {
			return;
		}
		for (int i = start; i <= end; ++i) {
			WContainerWidget line = new WContainerWidget();
			this.content_.insertWidget(i, line);
			Object d = this.model_.getData(i, this.modelColumn_);
			WText value = new WText(StringUtils.asString(d),
					TextFormat.PlainText);
			Object d2 = this.model_.getData(i, this.modelColumn_,
					ItemDataRole.UserRole);
			if ((d2 == null)) {
				d2 = d;
			}
			line.addWidget(value);
			value.setAttributeValue("sug", StringUtils.asString(d2).toString());
		}
	}

	private void modelRowsRemoved(WModelIndex parent, int start, int end) {
		if ((parent != null)) {
			return;
		}
		for (int i = start; i <= end; ++i) {
			if (this.content_.getWidget(i) != null)
				this.content_.getWidget(i).remove();
		}
	}

	private void modelDataChanged(WModelIndex topLeft, WModelIndex bottomRight) {
		if ((topLeft.getParent() != null)) {
			return;
		}
		if (this.modelColumn_ < topLeft.getColumn()
				|| this.modelColumn_ > bottomRight.getColumn()) {
			return;
		}
		for (int i = topLeft.getRow(); i <= bottomRight.getRow(); ++i) {
			WContainerWidget w = ((this.content_.getWidget(i)) instanceof WContainerWidget ? (WContainerWidget) (this.content_
					.getWidget(i))
					: null);
			WText value = ((w.getWidget(0)) instanceof WText ? (WText) (w
					.getWidget(0)) : null);
			Object d = this.model_.getData(i, this.modelColumn_);
			value.setText(StringUtils.asString(d));
			Object d2 = this.model_.getData(i, this.modelColumn_,
					ItemDataRole.UserRole);
			if ((d2 == null)) {
				d2 = d;
			}
			value.setAttributeValue("sug", StringUtils.asString(d2).toString());
		}
	}

	private void modelLayoutChanged() {
		this.content_.clear();
		this.setModelColumn(this.modelColumn_);
	}

	private void defineJavaScript() {
		WApplication app = WApplication.getInstance();
		String THIS_JS = "js/WSuggestionPopup.js";
		if (!app.isJavaScriptLoaded(THIS_JS)) {
			app.doJavaScript(wtjs1(app), false);
			app.setJavaScriptLoaded(THIS_JS);
		}
		app.doJavaScript("new Wt3_1_2.WSuggestionPopup("
				+ app.getJavaScriptClass() + "," + this.getJsRef() + ","
				+ this.replacerJS_ + "," + this.matcherJS_ + ","
				+ String.valueOf(this.filterLength_) + ");");
	}

	protected void render(EnumSet<RenderFlag> flags) {
		if (!EnumUtils.mask(flags, RenderFlag.RenderFull).isEmpty()) {
			this.defineJavaScript();
		}
		super.render(flags);
	}

	static String wtjs1(WApplication app) {
		return "Wt3_1_2.WSuggestionPopup = function(o,e,v,w,l){function p(){return e.style.display!=\"none\"}function k(){e.style.display=\"none\"}function x(a){f.positionAtWidget(e.id,a.id,f.Vertical)}function y(a){a=a.target||a.srcElement;if(a.className!=\"content\"){if(!f.hasTag(a,\"DIV\"))a=a.parentNode;q(a)}}function q(a){var b=a.firstChild;a=f.getElement(m);var g=b.innerHTML;b=b.getAttribute(\"sug\");a.focus();v(a,g,b);k()}jQuery.data(e,\"obj\",this);var n=this,f=o.WT,i=null,m=null,r=false,s=null,t=null; this.showPopup=function(){e.style.display=\"\";i=null};this.editKeyDown=function(a,b){m=a.id;var g=i?f.getElement(i):null;if(p()&&g)if(b.keyCode==13||b.keyCode==9){q(g);f.cancelEvent(b);setTimeout(function(){a.focus()},0);return false}else if(b.keyCode==40||b.keyCode==38||b.keyCode==34||b.keyCode==33){if(b.type.toUpperCase()==\"KEYDOWN\"){r=true;f.cancelEvent(b,f.CancelDefaultAction)}if(b.type.toUpperCase()==\"KEYPRESS\"&&r==true){f.cancelEvent(b);return false}var c=g,h=g,j=b.keyCode==40||b.keyCode==34; b=b.keyCode==34||b.keyCode==33?e.clientHeight/g.offsetHeight:1;var d;for(d=0;c&&d<b;++d){for(c=j?c.nextSibling:c.previousSibling;c&&c.nodeName.toUpperCase()==\"DIV\"&&c.style.display==\"none\";c=j?c.nextSibling:c.previousSibling)h=c;h=c||h}if(h&&h.nodeName.toUpperCase()==\"DIV\"){g.className=null;h.className=\"sel\";i=h.id}return false}return b.keyCode!=13&&b.keyCode!=9};this.filtered=function(a){s=a;n.refilter()};this.refilter=function(){var a=i?f.getElement(i):null,b=f.getElement(m),g=w(b);if(l){var c= g(null);if(c.length<l){k();return}else{c=c.substring(0,l);if(c!=s){k();if(c!=t){t=c;o.emit(e,\"filter\",c)}return}}}c=null;for(var h=e.lastChild.childNodes,j=0;j<h.length;j++){var d=h[j];if(d.nodeName.toUpperCase()==\"DIV\"){if(d.orig==null)d.orig=d.firstChild.innerHTML;else d.firstChild.innerHTML=d.orig;var u=g(d.firstChild.innerHTML);d.firstChild.innerHTML=u.suggestion;if(u.match){d.style.display=\"\";if(c==null)c=d}else d.style.display=\"none\";d.className=null}}if(c==null)k();else{if(!p()){x(b);n.showPopup(); a=null}if(!a||a.style.display==\"none\"){i=c.id;a=c;a.scrollIntoView()}a.className=\"sel\";if(a.offsetTop+a.offsetHeight>e.scrollTop+e.clientHeight)a.scrollIntoView(false);else a.offsetTop<e.scrollTop&&a.scrollIntoView(true)}};this.editKeyUp=function(a,b){if(!((b.keyCode==13||b.keyCode==9)&&e.style.display==\"none\"))if(b.keyCode==27||b.keyCode==37||b.keyCode==39){e.style.display=\"none\";b.keyCode==27&&a.blur()}else n.refilter()};e.lastChild.onclick=y;this.delayHide=function(){setTimeout(function(){e&&k()}, 300)}};";
	}

	static String generateParseEditJS(WSuggestionPopup.Options options) {
		return ""
				+ "var value = edit.value;var pos;if (edit.selectionStart)pos = edit.selectionStart;else pos = value.length;var ws='"
				+ options.whitespace
				+ "';"
				+ (options.listSeparator != 0 ? "var start = value.lastIndexOf('"
						+ options.listSeparator + "', pos - 1) + 1;"
						: "var start = 0;")
				+ "while ((start < pos)&& (ws.indexOf(value.charAt(start)) != -1))start++;var end = pos;";
	}
}
