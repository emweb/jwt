/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.ArrayList;
import java.util.List;
import eu.webtoolkit.jwt.utils.StringUtils;

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
 * 
 * // set style
 * popup.setStyleClass(&quot;suggest&quot;);
 * </pre>
 * 
 * </blockquote>
 * <p>
 * Example CSS:
 * <p>
 * <blockquote>
 * 
 * <pre>
 * .suggest {
 *   background-color: #e0ecff;
 *   color: #1010cc;
 *   border: 1px solid #666666;
 *   cursor: default;
 *   font-size: smaller;
 *   padding: 2px;
 * }
 * 
 * .suggest span {
 *   padding-left: 0.5em;
 *   padding-right: 0.5em;  
 * }
 * 
 * .suggest .sel {
 *   background-color: #C3D9FF;
 * }
 * </pre>
 * 
 * </blockquote>
 * <p>
 * A screenshot of this example: <div align="center"> <img
 * src="doc-files//WSuggestionPopup-1.png" alt="Example of WSuggestionPopup">
 * <p>
 * <strong>Example of WSuggestionPopup</strong>
 * </p>
 * </div>
 * <p>
 * <i><b>Note:</b>This widget requires JavaScript support. </i>
 * </p>
 */
public class WSuggestionPopup extends WCompositeWidget {
	/**
	 * Construct a {@link WSuggestionPopup} with given matcherJS and replacerJS.
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
		this.setImplementation(this.content_ = new WContainerWidget());
		this.setPopup(true);
		this.setPositionScheme(PositionScheme.Absolute);
		this.editKeyDown_
				.setJavaScript("function(edit, event) {  var self = "
						+ this.getJsRef()
						+ ";  var sel = self.getAttribute('sel');  if (sel != null) sel = Wt2_99_5.getElement(sel);  if (self.style.display != 'none' && sel != null) {    if ((event.keyCode == 13) || (event.keyCode == 9)) {      sel.firstChild.onclick();      Wt2_99_5.cancelEvent(event);      return false;    } else if (event.keyCode == 40 || event.keyCode == 38) {      if (event.type.toUpperCase() == 'KEYDOWN')        self.setAttribute('kd', 'true');      if (event.type.toUpperCase() == 'KEYPRESS'        && self.getAttribute('kd') == 'true') {        Wt2_99_5.cancelEvent(event);        return false;      }      var n = sel;      for (var n = (event.keyCode == 40) ? n.nextSibling : n.previousSibling;            n != null && n.nodeName.toUpperCase() == 'DIV' && n.style.display == 'none';           n = (event.keyCode == 40) ? n.nextSibling : n.previousSibling) { }      if (n != null && n.nodeName.toUpperCase() == 'DIV') {        sel.setAttribute('class', null);        n.setAttribute('class', 'sel');        self.setAttribute('sel', n.id);      }      return false;    }  }  return (event.keyCode != 13 && event.keyCode != 9);}");
		this.editKeyUp_
				.setJavaScript("function(edit, event) {  var self = "
						+ this.getJsRef()
						+ ";  var sel = self.getAttribute('sel');  if (sel != null) sel = Wt2_99_5.getElement(sel);  if (event.keyCode == 27 || event.keyCode == 37 || event.keyCode == 39) {    self.style.display = 'none';    if (event.keyCode == 27)      edit.blur();  } else {    var text = edit.value;    var matcher = "
						+ this.matcherJS_
						+ "(edit);    var first = null;    for (var i = 0; i < self.childNodes.length; i++) {      var child = self.childNodes[i];      if (child.nodeName.toUpperCase() == 'DIV') {        if (child.getAttribute('orig') == null)          child.setAttribute('orig', child.firstChild.innerHTML);        else          child.firstChild.innerHTML = child.getAttribute('orig');        var result = matcher(child.firstChild.innerHTML);        child.firstChild.innerHTML = result.suggestion;        if (result.match) {          child.style.display = 'block';          if (first == null) first = child;        } else          child.style.display = 'none';        child.className = null;      }    }    if (first == null) {      self.style.display = 'none';    } else {      if (self.style.display != 'block') {        self.style.display = 'block';        edit.parentNode.insertBefore(self, edit.nextSibling);        self.setAttribute('sel', null);        sel = null;      }      if ((sel == null) || (sel.style.display == 'none')) {        self.setAttribute('sel', first.id);        first.className = 'sel';      } else {        sel.className = 'sel';      }    }  }}");
		this.suggestionClicked_
				.setJavaScript("function(suggestion, event) {  var self = "
						+ this.getJsRef()
						+ ";  var edit = self.previousSibling;  var sText = suggestion.innerHTML;  var sValue = suggestion.getAttribute('sug');  var replacer = "
						+ this.replacerJS_
						+ ";  edit.focus();  replacer(edit, sText, sValue);  self.style.display = 'none';}");
		this.delayHide_
				.setJavaScript("function(edit, event) {  setTimeout(\"if ("
						+ this.getJsRef() + ") " + this.getJsRef()
						+ ".style.display = 'none';\", 300);}");
		this.hide();
		this.setModel(new WStringListModel(this));
	}

	/**
	 * Construct a {@link WSuggestionPopup} with given matcherJS and replacerJS.
	 * <p>
	 * Calls
	 * {@link #WSuggestionPopup(String matcherJS, String replacerJS, WContainerWidget parent)
	 * this(matcherJS, replacerJS, (WContainerWidget)null)}
	 */
	public WSuggestionPopup(String matcherJS, String replacerJS) {
		this(matcherJS, replacerJS, (WContainerWidget) null);
	}

	/**
	 * Let this suggestion popup assist in editing the given edit field.
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
	 * Clear the list of suggestions.
	 */
	public void clearSuggestions() {
		this.model_.removeRows(0, this.model_.getRowCount());
	}

	/**
	 * Add a new suggestion.
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
	 * Set the model to be used for the suggestions.
	 * <p>
	 * The <i>model</i> may not be 0, and ownership of the model is not
	 * transferred.
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
	 * Set the column in the model to be used for the items.
	 * <p>
	 * The column <i>index</i> in the model will be used to retrieve data.
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
	 * Return the data model.
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
		 * Specify 0 for no list separation.
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
	 * Create a matcher JavaScript function based on some generic options.
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
				+ "      matched = true;    }    i = matchpos + 1;  } else    i = matchpos;}}return { match: matched,         suggestion: suggestion }}}";
	}

	/**
	 * Create a replacer JavaScript function based on some generic options.
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
