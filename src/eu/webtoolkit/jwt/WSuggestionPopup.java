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
 * The popup provides the user with suggestions to enter input. The popup can be
 * used by one or more editors, using
 * {@link WSuggestionPopup#forEdit(WFormWidget edit, EnumSet triggers)
 * forEdit()}. The popup will show when the user starts editing the edit field,
 * or when the user opens the suggestions explicitly using a drop down icon or
 * with the down key. The popup positions itself intelligently just below or
 * just on top of the edit field. It offers a list of suggestions that match in
 * some way with the current edit field, and dynamically adjusts this list. The
 * implementation for matching indivudal suggestions with the current text is
 * provided through a JavaScript function. This function may also highlight
 * part(s) of the suggestions to provide feed-back on how they match.
 * <p>
 * WSuggestionPopup is an MVC view class, using a simple
 * {@link WStringListModel} by default. You can set a custom model using
 * {@link WSuggestionPopup#setModel(WAbstractItemModel model) setModel()}. The
 * model can provide different text for the suggestion text (
 * {@link ItemDataRole#DisplayRole}) and value ({@link ItemDataRole#UserRole}).
 * The member methods {@link WSuggestionPopup#clearSuggestions()
 * clearSuggestions()} and
 * {@link WSuggestionPopup#addSuggestion(CharSequence suggestionText, CharSequence suggestionValue)
 * addSuggestion()} manipulate the model.
 * <p>
 * By default, the popup implements all filtering client-side. To support large
 * datasets, you may enable server-side filtering of suggestions based on the
 * input. The server-side filtering provides a coarse filtering using a fixed
 * size prefix of the entered text, and complements the client-side filtering.
 * Use {@link WSuggestionPopup#setFilterLength(int length) setFilterLength()}
 * and then listen to a filter notification using the modelFilter() signal. By
 * using {@link WSuggestionPopup#setMaximumSize(WLength width, WLength height)
 * setMaximumSize()} you can also limit the maximum height of the popup, in
 * which case scrolling is supported (similar to a combo-box).
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
 *      // 4) add markup to suggestion if necessary
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
 * When using the DropDownIcon trigger, an additional style class is provided
 * for the edit field: <code>Wt-suggest-dropdown</code>, which renders the icon
 * to the right inside the edit field. This class may be used to customize how
 * the drop down icon is rendered.
 * <p>
 * <p>
 * <i><b>Note: </b>This widget requires JavaScript support. </i>
 * </p>
 */
public class WSuggestionPopup extends WCompositeWidget {
	/**
	 * Enumeration that defines a trigger for showing the popup.
	 * <p>
	 * 
	 * @see WSuggestionPopup#forEdit(WFormWidget edit, EnumSet triggers)
	 */
	public enum PopupTrigger {
		/**
		 * Shows popup when the user starts editing.
		 * <p>
		 * The popup is shown when the currently edited text has a length longer
		 * than the {@link WSuggestionPopup#setFilterLength(int length) filter
		 * length}.
		 */
		Editing,
		/**
		 * Shows popup when user clicks a drop down icon.
		 * <p>
		 * The lineedit is modified to show a drop down icon, and clicking the
		 * icon shows the suggestions, very much like a WComboCox.
		 */
		DropDownIcon;

		/**
		 * Returns the numerical representation of this enum.
		 */
		public int getValue() {
			return ordinal();
		}
	}

	/**
	 * A configuration object to generate a matcher and replacer JavaScript
	 * function
	 * <p>
	 * 
	 * @see WSuggestionPopup
	 */
	public static class Options {
		/**
		 * Open tag to highlight a match in a suggestion.
		 * <p>
		 * Must be an opening markup tag, such as &lt;b&gt;.
		 * <p>
		 * Used during matching.
		 */
		public String highlightBeginTag;
		/**
		 * Close tag to highlight a match in a suggestion.
		 * <p>
		 * Must be a closing markup tag, such as &lt;/b&gt;.
		 * <p>
		 * Used during matching.
		 */
		public String highlightEndTag;
		/**
		 * When editing a list of values, the separator used for different
		 * items.
		 * <p>
		 * For example, &apos;,&apos; to separate different values on komma.
		 * Specify 0 (&apos;\0&apos;) for no list separation.
		 * <p>
		 * Used during matching and replacing.
		 */
		public char listSeparator;
		/**
		 * When editing a value, the whitespace characters ignored before the
		 * current value.
		 * <p>
		 * For example, &quot; \\n&quot; to ignore spaces and newlines.
		 * <p>
		 * Used during matching and replacing.
		 */
		public String whitespace;
		/**
		 * To show suggestions based on matches of the edited value with parts
		 * of the suggestion.
		 * <p>
		 * For example, &quot; .@&quot; will also match with suggestion text
		 * after a space, a dot (.) or an at-symbol (@).
		 * <p>
		 * Used during matching.
		 */
		public String wordSeparators;
		/**
		 * When replacing the current edited value with suggestion value, append
		 * the following string as well.
		 * <p>
		 * Used during replacing.
		 */
		public String appendReplacedText;
	}

	/**
	 * Creates a suggestion popup.
	 * <p>
	 * The popup using a standard matcher and replacer implementation that is
	 * configured using the provided <code>options</code>.
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
		this.filterModel_ = new Signal1<String>(this);
		this.activated_ = new Signal2<Integer, WFormWidget>(this);
		this.modelConnections_ = new ArrayList<AbstractSignal.Connection>();
		this.filter_ = new JSignal1<String>(this.impl_, "filter") {
		};
		this.jactivated_ = new JSignal2<String, String>(this.impl_, "select") {
		};
		this.editKeyDown_ = new JSlot(parent);
		this.editKeyUp_ = new JSlot(parent);
		this.editClick_ = new JSlot(parent);
		this.editMouseMove_ = new JSlot(parent);
		this.delayHide_ = new JSlot(parent);
		this.edits_ = new ArrayList<WFormWidget>();
		this.init();
	}

	/**
	 * Creates a suggestion popup.
	 * <p>
	 * Calls
	 * {@link #WSuggestionPopup(WSuggestionPopup.Options options, WContainerWidget parent)
	 * this(options, (WContainerWidget)null)}
	 */
	public WSuggestionPopup(WSuggestionPopup.Options options) {
		this(options, (WContainerWidget) null);
	}

	/**
	 * Creates a suggestion popup with given matcherJS and replacerJS.
	 * <p>
	 * See supra for the expected signature of the matcher and replace
	 * JavaScript functions.
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
		this.filterModel_ = new Signal1<String>();
		this.activated_ = new Signal2<Integer, WFormWidget>();
		this.modelConnections_ = new ArrayList<AbstractSignal.Connection>();
		this.filter_ = new JSignal1<String>(this.impl_, "filter") {
		};
		this.jactivated_ = new JSignal2<String, String>(this.impl_, "select") {
		};
		this.editKeyDown_ = new JSlot(parent);
		this.editKeyUp_ = new JSlot(parent);
		this.editClick_ = new JSlot(parent);
		this.editMouseMove_ = new JSlot(parent);
		this.delayHide_ = new JSlot(parent);
		this.edits_ = new ArrayList<WFormWidget>();
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
	 * Lets this suggestion popup assist in editing the given edit field.
	 * <p>
	 * A single suggestion popup may assist in several edits by repeated calls
	 * of this method.
	 * <p>
	 * The <code>popupTriggers</code> control how editing is triggered (either
	 * by the user editing the field by entering keys or by an explicit drop
	 * down menu that is shown inside the edit).
	 * <p>
	 * 
	 * @see WSuggestionPopup#removeEdit(WFormWidget edit)
	 */
	public void forEdit(WFormWidget edit,
			EnumSet<WSuggestionPopup.PopupTrigger> triggers) {
		edit.keyPressed().addListener(this.editKeyDown_);
		edit.keyWentDown().addListener(this.editKeyDown_);
		edit.keyWentUp().addListener(this.editKeyUp_);
		edit.blurred().addListener(this.delayHide_);
		if (!EnumUtils.mask(triggers, WSuggestionPopup.PopupTrigger.Editing)
				.isEmpty()) {
			edit.addStyleClass("Wt-suggest-onedit");
		}
		if (!EnumUtils.mask(triggers,
				WSuggestionPopup.PopupTrigger.DropDownIcon).isEmpty()) {
			edit.addStyleClass("Wt-suggest-dropdown");
			edit.clicked().addListener(this.editClick_);
			edit.mouseMoved().addListener(this.editMouseMove_);
		}
		this.edits_.add(edit);
	}

	/**
	 * Lets this suggestion popup assist in editing the given edit field.
	 * <p>
	 * Calls {@link #forEdit(WFormWidget edit, EnumSet triggers) forEdit(edit,
	 * EnumSet.of(trigger, triggers))}
	 */
	public final void forEdit(WFormWidget edit,
			WSuggestionPopup.PopupTrigger trigger,
			WSuggestionPopup.PopupTrigger... triggers) {
		forEdit(edit, EnumSet.of(trigger, triggers));
	}

	/**
	 * Lets this suggestion popup assist in editing the given edit field.
	 * <p>
	 * Calls {@link #forEdit(WFormWidget edit, EnumSet triggers) forEdit(edit,
	 * EnumSet.of(WSuggestionPopup.PopupTrigger.Editing))}
	 */
	public final void forEdit(WFormWidget edit) {
		forEdit(edit, EnumSet.of(WSuggestionPopup.PopupTrigger.Editing));
	}

	/**
	 * Removes the edit field from the list of assisted editors.
	 * <p>
	 * The editor will no longer be assisted by this popup widget.
	 * <p>
	 * 
	 * @see WSuggestionPopup#forEdit(WFormWidget edit, EnumSet triggers)
	 */
	public void removeEdit(WFormWidget edit) {
		if (this.edits_.remove(edit)) {
			edit.removeStyleClass("Wt-suggest-onedit");
			edit.removeStyleClass("Wt-suggest-dropdown");
		}
	}

	/**
	 * Clears the list of suggestions.
	 * <p>
	 * This clears the underlying model.
	 * <p>
	 * 
	 * @see WSuggestionPopup#addSuggestion(CharSequence suggestionText,
	 *      CharSequence suggestionValue)
	 */
	public void clearSuggestions() {
		this.model_.removeRows(0, this.model_.getRowCount());
	}

	/**
	 * Adds a new suggestion.
	 * <p>
	 * This adds an entry to the underlying model. The
	 * <code>suggestionText</code> is set as {@link ItemDataRole#DisplayRole}
	 * and the <code>suggestionValue</code> (which is inserted into the edit
	 * field on selection) is set as {@link ItemDataRole#UserRole}.
	 * <p>
	 * 
	 * @see WSuggestionPopup#clearSuggestions()
	 * @see WSuggestionPopup#setModel(WAbstractItemModel model)
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
	 * Creates a standard matcher JavaScript function.
	 * <p>
	 * This returns a JavaScript function that provides a standard
	 * implementation for the matching input, based on the given <code></code> .
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
	 * Creates a standard replacer JavaScript function.
	 * <p>
	 * This returns a JavaScript function that provides a standard
	 * implementation for the matching input, based on the given <code></code> .
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
	 * The argument is the initial input. When
	 * {@link WSuggestionPopup.PopupTrigger#Editing Editing} is used as edit
	 * trigger, its length will always equal the
	 * {@link WSuggestionPopup#getFilterLength() getFilterLength()}. When
	 * {@link WSuggestionPopup.PopupTrigger#DropDownIcon DropDownIcon} is used
	 * as edit trigger, the input length may be less than
	 * {@link WSuggestionPopup#getFilterLength() getFilterLength()}, and the the
	 * signal will be called repeatedly as the user provides more input.
	 * <p>
	 * For example, if you are using a {@link WSortFilterProxyModel}, you could
	 * react to this signal with:
	 * <p>
	 * <blockquote>
	 * 
	 * <pre>
	 * public filterSuggestions(String filter) {
	 * 	proxyModel.setFilterRegExp(filter + &quot;.*&quot;);
	 * }
	 * </pre>
	 * 
	 * </blockquote>
	 */
	public Signal1<String> filterModel() {
		return this.filterModel_;
	}

	/**
	 * Signal emitted when a suggestion was selected.
	 * <p>
	 * The selected item is passed as the first argument and the editor as the
	 * second.
	 */
	public Signal2<Integer, WFormWidget> activated() {
		return this.activated_;
	}

	public void setMaximumSize(WLength width, WLength height) {
		super.setMaximumSize(width, height);
		this.content_.setMaximumSize(width, height);
	}

	public void setMinimumSize(WLength width, WLength height) {
		super.setMinimumSize(width, height);
		this.content_.setMinimumSize(width, height);
	}

	private WTemplate impl_;
	private WAbstractItemModel model_;
	private int modelColumn_;
	private int filterLength_;
	private String matcherJS_;
	private String replacerJS_;
	private WContainerWidget content_;
	private Signal1<String> filterModel_;
	private Signal2<Integer, WFormWidget> activated_;
	private List<AbstractSignal.Connection> modelConnections_;
	private JSignal1<String> filter_;
	private JSignal2<String, String> jactivated_;
	private JSlot editKeyDown_;
	private JSlot editKeyUp_;
	private JSlot editClick_;
	private JSlot editMouseMove_;
	private JSlot delayHide_;
	private List<WFormWidget> edits_;

	private void init() {
		this.setImplementation(this.impl_);
		this.impl_.setLoadLaterWhenInvisible(false);
		this.impl_.setStyleClass("Wt-suggest Wt-outset");
		this.impl_.bindString("shadow-x1-x2", WTemplate.DropShadow_x1_x2);
		this.impl_.bindWidget("contents",
				this.content_ = new WContainerWidget());
		this.content_.setStyleClass("content");
		this.setAttributeValue("style", "z-index: 10000");
		this.setPositionScheme(PositionScheme.Absolute);
		this.setJavaScript(this.editKeyDown_, "editKeyDown");
		this.setJavaScript(this.editKeyUp_, "editKeyUp");
		this.setJavaScript(this.delayHide_, "delayHide");
		this.setJavaScript(this.editClick_, "editClick");
		this.setJavaScript(this.editMouseMove_, "editMouseMove");
		this.hide();
		this.setModel(new WStringListModel(this));
		this.filter_.addListener(this, new Signal1.Listener<String>() {
			public void trigger(String e1) {
				WSuggestionPopup.this.doFilter(e1);
			}
		});
		this.jactivated_.addListener(this,
				new Signal2.Listener<String, String>() {
					public void trigger(String e1, String e2) {
						WSuggestionPopup.this.doActivate(e1, e2);
					}
				});
	}

	private void doFilter(String input) {
		this.filterModel_.trigger(input);
		WApplication app = WApplication.getInstance();
		app.doJavaScript("jQuery.data(" + this.getJsRef()
				+ ", 'obj').filtered(" + WWebWidget.jsStringLiteral(input)
				+ ")");
	}

	private void doActivate(String itemId, String editId) {
		WFormWidget edit = null;
		for (int i = 0; i < this.edits_.size(); ++i) {
			if (this.edits_.get(i).getId().equals(editId)) {
				edit = this.edits_.get(i);
				break;
			}
		}
		if (edit == null) {
			WApplication.getInstance().log("error").append(
					"WSuggestionPopup activate from bogus editor");
		}
		for (int i = 0; i < this.content_.getCount(); ++i) {
			if (this.content_.getWidget(i).getId().equals(itemId)) {
				this.activated_.trigger(i, edit);
				return;
			}
		}
		WApplication.getInstance().log("error").append(
				"WSuggestionPopup activate for bogus item");
	}

	private void setJavaScript(JSlot slot, String methodName) {
		String jsFunction = "function(obj, event) {var o = jQuery.data("
				+ this.getJsRef() + ", 'obj');if (o) o." + methodName
				+ "(obj, event);}";
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
			if (this.content_.getWidget(start) != null)
				this.content_.getWidget(start).remove();
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
		app.doJavaScript("new Wt3_1_5.WSuggestionPopup("
				+ app.getJavaScriptClass() + "," + this.getJsRef() + ","
				+ this.replacerJS_ + "," + this.matcherJS_ + ","
				+ String.valueOf(this.filterLength_) + ");");
	}

	void render(EnumSet<RenderFlag> flags) {
		if (!EnumUtils.mask(flags, RenderFlag.RenderFull).isEmpty()) {
			this.defineJavaScript();
		}
		super.render(flags);
	}

	static String wtjs1(WApplication app) {
		return "Wt3_1_5.WSuggestionPopup = function(q,d,y,z,r){function m(a){return $(a).hasClass(\"Wt-suggest-onedit\")||$(a).hasClass(\"Wt-suggest-dropdown\")}function s(){return d.style.display!=\"none\"}function k(){d.style.display=\"none\"}function A(a){c.positionAtWidget(d.id,a.id,c.Vertical)}function B(a){a=a||window.event;a=a.target||a.srcElement;if(a.className!=\"content\"){for(;a&&!c.hasTag(a,\"DIV\");)a=a.parentNode;a&&t(a)}}function t(a){var b=a.firstChild,h=c.getElement(f),e=b.innerHTML;b=b.getAttribute(\"sug\"); h.focus();q.emit(d,\"select\",a.id,h.id);y(h,e,b);k();f=null}function C(a,b){for(a=b?a.nextSibling:a.previousSibling;a;a=b?a.nextSibling:a.previousSibling)if(c.hasTag(a,\"DIV\"))if(a.style.display!=\"none\")return a;return null}$(\".Wt-domRoot\").add(d);jQuery.data(d,\"obj\",this);var n=this,c=q.WT,l=null,f=null,u=false,v=null,w=null,o=null;this.showPopup=function(){d.style.display=\"\";l=null};this.editMouseMove=function(a,b){if(m(a))a.style.cursor=c.widgetCoordinates(a,b).x>a.offsetWidth-16?\"default\":\"\"};this.editClick= function(a,b){if(m(a))if(c.widgetCoordinates(a,b).x>a.offsetWidth-16)if(f!=a.id){k();f=a.id;n.refilter()}else{f=null;k()}};this.editKeyDown=function(a,b){if(!m(a))return true;if(f!=a.id)if($(a).hasClass(\"Wt-suggest-onedit\"))f=a.id;else if($(a).hasClass(\"Wt-suggest-dropdown\")&&b.keyCode==40)f=a.id;else{f=null;return true}var h=l?c.getElement(l):null;if(s()&&h)if(b.keyCode==13||b.keyCode==9){t(h);c.cancelEvent(b);setTimeout(function(){a.focus()},0);return false}else if(b.keyCode==40||b.keyCode==38|| b.keyCode==34||b.keyCode==33){if(b.type.toUpperCase()==\"KEYDOWN\"){u=true;c.cancelEvent(b,c.CancelDefaultAction)}if(b.type.toUpperCase()==\"KEYPRESS\"&&u==true){c.cancelEvent(b);return false}var e=h,p=b.keyCode==40||b.keyCode==34;b=b.keyCode==34||b.keyCode==33?d.clientHeight/h.offsetHeight:1;var j;for(j=0;e&&j<b;++j){var g=C(e,p);if(!g)break;e=g}if(e&&c.hasTag(e,\"DIV\")){h.className=\"\";e.className=\"sel\";l=e.id}return false}return b.keyCode!=13&&b.keyCode!=9};this.filtered=function(a){v=a;n.refilter()}; this.refilter=function(){var a=l?c.getElement(l):null,b=c.getElement(f),h=z(b),e=!$(b).hasClass(\"Wt-suggest-dropdown\"),p=d.lastChild.childNodes,j=h(null);if(r)if(e&&j.length<r){k();return}else{var g=j.substring(0,r);if(g!=v){if(g!=w){w=g;q.emit(d,\"filter\",g)}if(e){k();return}}}g=null;e=!e&&j.length==0;for(j=0;j<p.length;j++){var i=p[j];if(c.hasTag(i,\"DIV\")){if(i.orig==null)i.orig=i.firstChild.innerHTML;else i.firstChild.innerHTML=i.orig;var x=h(i.firstChild.innerHTML),D=e||x.match;i.firstChild.innerHTML= x.suggestion;if(D){i.style.display=\"\";if(g==null)g=i}else i.style.display=\"none\";i.className=\"\"}}if(g==null)k();else{if(!s()){A(b);n.showPopup();a=null}if(!a||a.style.display==\"none\"){l=g.id;a=g;a.parentNode.scrollTop=0}a.className=\"sel\";b=a.parentNode;if(a.offsetTop+a.offsetHeight>b.scrollTop+b.clientHeight)b.scrollTop=a.offsetTop+a.offsetHeight-b.clientHeight;else if(a.offsetTop<b.scrollTop)b.scrollTop=a.offsetTop}};this.editKeyUp=function(a,b){if(f!=null)if(m(a))if(!((b.keyCode==13||b.keyCode== 9)&&d.style.display==\"none\"))if(b.keyCode==27||b.keyCode==37||b.keyCode==39){d.style.display=\"none\";if(b.keyCode==27){f=null;$(a).hasClass(\"Wt-suggest-dropdown\")?k():a.blur()}}else n.refilter()};d.lastChild.onclick=B;d.lastChild.onscroll=function(){if(o){clearTimeout(o);var a=c.getElement(f);a&&a.focus()}};this.delayHide=function(a){o=setTimeout(function(){o=null;if(d&&(a==null||f==a.id))k()},300)}};";
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
