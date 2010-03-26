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
 * The class is initialized with two JavaScript functions, one for filtering the
 * suggestions, and one for editing the value of the textarea when a suggestion
 * is selected. Two static methods,
 * {@link WSuggestionPopup#generateMatcherJS(WSuggestionPopup.Options options)
 * generateMatcherJS()} and
 * {@link WSuggestionPopup#generateReplacerJS(WSuggestionPopup.Options options)
 * generateReplacerJS()} may be used to generate these functions based on a set
 * of options (in the {@link Options} struct). If the flexibility provided in
 * this way is not sufficient, and writing JavaScript does not give you an
 * instant heart-attack, you may provide your own implementations.
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
 *      // 1) remove markup from the suggestion
 *      // 2) check suggestion if it matches
 *      // 3) add markup to suggestion
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
 * WSuggestionPopup popup = new WSuggestionPopup(WSuggestionPopup
 * 		.generateMatcherJS(contactOptions), WSuggestionPopup
 * 		.generateReplacerJS(contactOptions), this);
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
	 * Creates a suggestion popup with given matcherJS and replacerJS.
	 */
	public WSuggestionPopup(String matcherJS, String replacerJS,
			WContainerWidget parent) {
		super(parent);
		this.model_ = null;
		this.modelColumn_ = 0;
		this.matcherJS_ = matcherJS;
		this.replacerJS_ = replacerJS;
		this.modelConnections_ = new ArrayList<AbstractSignal.Connection>();
		this.editKeyDown_ = new JSlot(parent);
		this.editKeyUp_ = new JSlot(parent);
		this.suggestionClicked_ = new JSlot(parent);
		this.delayHide_ = new JSlot(parent);
		String TEMPLATE = "${shadow-x1-x2}${contents}";
		this
				.setImplementation(this.impl_ = new WTemplate(new WString(
						TEMPLATE)));
		this.impl_.setStyleClass("Wt-suggest Wt-outset");
		this.impl_.bindString("shadow-x1-x2", WTemplate.DropShadow_x1_x2);
		this.impl_.bindWidget("contents",
				this.content_ = new WContainerWidget());
		this.setPopup(true);
		this.setPositionScheme(PositionScheme.Absolute);
		this.setJavaScript(this.editKeyDown_, "editKeyDown");
		this.setJavaScript(this.editKeyUp_, "editKeyUp");
		this.setJavaScript(this.suggestionClicked_, "suggestionClicked");
		this.setJavaScript(this.delayHide_, "delayHide");
		this.hide();
		this.setModel(new WStringListModel(this));
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
	 * A configuration object to generate a matcher and replacer JavaScript
	 * function
	 */
	public static class Options {
		/**
		 * Open tag to highlight a match in a suggestion.
		 * <p>
		 * Must be an opening markup tag, such as &lt;B&gt;. The tag name must
		 * be all uppercase! (really?)
		 */
		public String highlightBeginTag;
		/**
		 * Close tag to highlight a match in a suggestion.
		 * <p>
		 * Must be a closing markup tag, such as &lt;/B&gt;. The tag name must
		 * be all uppercase!
		 */
		public String highlightEndTag;
		/**
		 * When editing a list of values, the separator used for different
		 * items.
		 * <p>
		 * For example, &apos;,&apos; to separate different values on komma.
		 * Specify 0 (&apos;&apos;) for no list separation.
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
	 * Creates a matcher JavaScript function based on some generic options.
	 */
	public static String generateMatcherJS(WSuggestionPopup.Options options) {
		return ""
				+ "function (edit) {"
				+ generateParseEditJS(options)
				+ "value = edit.value.substring(start, end).toUpperCase();return function(suggestion) {var sep='"
				+ options.wordSeparators
				+ "';var matched = false;var i = 0;var sugup = suggestion.toUpperCase();var inserted = 0;if (value.length != 0) {while ((i != -1) && (i < sugup.length)) {  var matchpos = sugup.indexOf(value, i);  if (matchpos != -1) {    if ((matchpos == 0)       || (sep.indexOf(sugup.charAt(matchpos - 1)) != -1)) {"
				+ (options.highlightEndTag.length() != 0 ? "suggestion = suggestion.substring(0, matchpos + inserted) + '"
						+ options.highlightBeginTag
						+ "' + suggestion.substring(matchpos + inserted,     matchpos + inserted + value.length) + '"
						+ options.highlightEndTag
						+ "' + suggestion.substring(matchpos + inserted + value.length,     suggestion.length); inserted += "
						+ String.valueOf(options.highlightBeginTag.length()
								+ options.highlightEndTag.length()) + ";"
						: "")
				+ "      matched = true;    }    i = matchpos + 1;  } else     i = matchpos;}}return { match: matched,         suggestion: suggestion }}}";
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

	private WTemplate impl_;
	private WAbstractItemModel model_;
	private int modelColumn_;
	private String matcherJS_;
	private String replacerJS_;
	private WContainerWidget content_;
	private List<AbstractSignal.Connection> modelConnections_;
	private JSlot editKeyDown_;
	private JSlot editKeyUp_;
	private JSlot suggestionClicked_;
	private JSlot delayHide_;

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
			value.clicked().addListener(this.suggestionClicked_);
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
				+ this.replacerJS_ + "," + this.matcherJS_ + ");");
	}

	protected void render(EnumSet<RenderFlag> flags) {
		if (!EnumUtils.mask(flags, RenderFlag.RenderFull).isEmpty()) {
			this.defineJavaScript();
		}
		super.render(flags);
	}

	static String wtjs1(WApplication app) {
		return "Wt3_1_2.WSuggestionPopup = function(n,c,o,p){jQuery.data(c,\"obj\",this);var g=n.WT,h=null,j=null,k=false;this.editKeyDown=function(e,a){var f=h?g.getElement(h):null;if(c.style.display!=\"none\"&&f)if(a.keyCode==13||a.keyCode==9){f.firstChild.onclick();g.cancelEvent(a);setTimeout(function(){e.focus()},0);return false}else if(a.keyCode==40||a.keyCode==38){if(a.type.toUpperCase()==\"KEYDOWN\"){k=true;g.cancelEvent(a,g.CancelDefaultAction)}if(a.type.toUpperCase()==\"KEYPRESS\"&&k==true){g.cancelEvent(a); return false}var b=f;for(b=a.keyCode==40?b.nextSibling:b.previousSibling;b&&b.nodeName.toUpperCase()==\"DIV\"&&b.style.display==\"none\";b=a.keyCode==40?b.nextSibling:b.previousSibling);if(b&&b.nodeName.toUpperCase()==\"DIV\"){f.className=null;b.className=\"sel\";h=b.id}return false}return a.keyCode!=13&&a.keyCode!=9};this.editKeyUp=function(e,a){var f=h?g.getElement(h):null;if(!((a.keyCode==13||a.keyCode==9)&&c.style.display==\"none\"))if(a.keyCode==27||a.keyCode==37||a.keyCode==39){c.style.display=\"none\"; a.keyCode==27&&e.blur()}else{a=p(e);for(var b=null,l=c.lastChild.childNodes,i=0;i<l.length;i++){var d=l[i];if(d.nodeName.toUpperCase()==\"DIV\"){if(d.orig==null)d.orig=d.firstChild.innerHTML;else d.firstChild.innerHTML=d.orig;var m=a(d.firstChild.innerHTML);d.firstChild.innerHTML=m.suggestion;if(m.match){d.style.display=\"block\";if(b==null)b=d}else d.style.display=\"none\";d.className=null}}if(b==null)c.style.display=\"none\";else{if(c.style.display!=\"block\"){c.style.display=\"block\";g.positionAtWidget(c.id, e.id,g.Vertical);h=null;j=e.id;f=null}if(!f||f.style.display==\"none\"){h=b.id;b.className=\"sel\"}else f.className=\"sel\"}}};this.suggestionClicked=function(e){var a=g.getElement(j),f=e.innerHTML;e=e.getAttribute(\"sug\");a.focus();o(a,f,e);c.style.display=\"none\"};this.delayHide=function(){setTimeout(function(){if(c)c.style.display=\"none\"},300)}};";
	}

	static String generateParseEditJS(WSuggestionPopup.Options options) {
		return ""
				+ "var value = edit.value;var pos;if (edit.selectionStart) { pos = edit.selectionStart; }  else { pos = value.length; }var ws = '"
				+ options.whitespace
				+ "';"
				+ (options.listSeparator != 0 ? "var start = value.lastIndexOf('"
						+ options.listSeparator + "', pos - 1) + 1;"
						: "var start = 0;")
				+ "while ((start < pos)  && (ws.indexOf(value.charAt(start)) != -1))  start++;var end = pos;";
	}
}
