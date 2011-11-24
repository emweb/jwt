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
 * implementation for matching individual suggestions with the current text is
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
 * addSuggestion()} manipulate this model. Note that a {@link WStringListModel}
 * does not support {@link ItemDataRole#UserRole} data, so you may need to use a
 * {@link WStandardItemModel} instead.
 * <p>
 * By default, the popup implements all filtering client-side. To support large
 * datasets, you may enable server-side filtering of suggestions based on the
 * input. The server-side filtering may provide a coarse filtering using a fixed
 * size prefix of the entered text, and complement the client-side filtering. To
 * enable server-side filtering, use
 * {@link WSuggestionPopup#setFilterLength(int length) setFilterLength()} and
 * listen to filter notification using the modelFilter() signal. Whenever a
 * filter event is generated you can adjust the model&apos;s content according
 * to the filter (e.g. using a {@link WSortFilterProxyModel}). By using
 * {@link WSuggestionPopup#setMaximumSize(WLength width, WLength height)
 * setMaximumSize()} you can also limit the maximum height of the popup, in
 * which case scrolling is supported (similar to a combo-box).
 * <p>
 * The class is initialized with an {@link Options} struct which configures how
 * suggestion filtering and result editing is done. Alternatively, you can
 * provide two JavaScript functions, one for filtering the suggestions, and one
 * for editing the value of the textarea when a suggestion is selected.
 * <p>
 * The matcherJS function must have the following JavaScript signature:
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
 *      // 2) check suggestion if it matches
 *      // 3) add highlighting markup to suggestion if necessary
 * 
 *      return { match : ...,      // does the suggestion match ? (boolean)
 *               suggestion : ...  // modified suggestion with highlighting
 *              };
 *    }
 *  }
 * </pre>
 * 
 * </blockquote>
 * <p>
 * The replacerJS function that edits the value has the following JavaScript
 * signature.
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
 * .Wt-suggest .content div : A suggestion element
 * .Wt-suggest .sel :         A selected suggestion element
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
	private static Logger logger = LoggerFactory
			.getLogger(WSuggestionPopup.class);

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
	 * function.
	 * <p>
	 * 
	 * @see WSuggestionPopup
	 */
	public static class Options {
		private static Logger logger = LoggerFactory.getLogger(Options.class);

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
		this.filtering_ = false;
		this.matcherJS_ = generateMatcherJS(options);
		this.replacerJS_ = generateReplacerJS(options);
		this.filterModel_ = new Signal1<String>(this);
		this.activated_ = new Signal2<Integer, WFormWidget>(this);
		this.modelConnections_ = new ArrayList<AbstractSignal.Connection>();
		this.filter_ = new JSignal1<String>(this.impl_, "filter") {
		};
		this.jactivated_ = new JSignal2<String, String>(this.impl_, "select") {
		};
		this.edits_ = new ArrayList<WFormWidget>();
		this.global_ = false;
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
		this.filtering_ = false;
		this.defaultValue_ = -1;
		this.matcherJS_ = matcherJS;
		this.replacerJS_ = replacerJS;
		this.filterModel_ = new Signal1<String>();
		this.activated_ = new Signal2<Integer, WFormWidget>();
		this.modelConnections_ = new ArrayList<AbstractSignal.Connection>();
		this.filter_ = new JSignal1<String>(this.impl_, "filter") {
		};
		this.jactivated_ = new JSignal2<String, String>(this.impl_, "select") {
		};
		this.edits_ = new ArrayList<WFormWidget>();
		this.global_ = false;
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
	 * Lets this suggestion popup assist in editing an edit field.
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
		AbstractEventSignal b = edit.keyPressed();
		this.connectObjJS(b, "editKeyDown");
		this.connectObjJS(edit.keyWentDown(), "editKeyDown");
		this.connectObjJS(edit.keyWentUp(), "editKeyUp");
		this.connectObjJS(edit.blurred(), "delayHide");
		if (!EnumUtils.mask(triggers, WSuggestionPopup.PopupTrigger.Editing)
				.isEmpty()) {
			edit.addStyleClass("Wt-suggest-onedit");
		}
		if (!EnumUtils.mask(triggers,
				WSuggestionPopup.PopupTrigger.DropDownIcon).isEmpty()) {
			edit.addStyleClass("Wt-suggest-dropdown");
			AbstractEventSignal c = edit.clicked();
			this.connectObjJS(c, "editClick");
			this.connectObjJS(edit.mouseMoved(), "editMouseMove");
		}
		this.edits_.add(edit);
	}

	/**
	 * Lets this suggestion popup assist in editing an edit field.
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
	 * Lets this suggestion popup assist in editing an edit field.
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
	 * Shows the suggestion popup at an edit field.
	 * <p>
	 * This is equivalent to the user triggering the suggestion popup to be
	 * shown.
	 */
	public void showAt(WFormWidget edit) {
		this.doJavaScript("jQuery.data(" + this.getJsRef() + ", 'obj').showAt("
				+ edit.getJsRef() + ")");
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
	 * Returns the data model.
	 * <p>
	 * 
	 * @see WSuggestionPopup#setModel(WAbstractItemModel model)
	 */
	public WAbstractItemModel getModel() {
		return this.model_;
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
	 * Sets a default selected value.
	 * <p>
	 * <code>row</code> is the model row that is selected by default (only if it
	 * matches the current input).
	 * <p>
	 * The default value is -1, indicating no default.
	 */
	public void setDefaultIndex(int row) {
		if (this.defaultValue_ != row) {
			this.defaultValue_ = row;
			if (this.isRendered()) {
				this.doJavaScript("jQuery.data(" + this.getJsRef()
						+ ", 'obj').defaultValue = "
						+ String.valueOf(this.defaultValue_) + ';');
			}
		}
	}

	/**
	 * Returns the default value.
	 * <p>
	 */
	public int getDefaultIndex() {
		return this.defaultValue_;
	}

	/**
	 * Creates a standard matcher JavaScript function.
	 * <p>
	 * This returns a JavaScript function that provides a standard
	 * implementation for the matching input, based on the given
	 * <code>options</code>.
	 */
	public static String generateMatcherJS(WSuggestionPopup.Options options) {
		return instantiateStdMatcher(options) + ".match";
	}

	/**
	 * Creates a standard replacer JavaScript function.
	 * <p>
	 * This returns a JavaScript function that provides a standard
	 * implementation for the matching input, based on the given
	 * <code>options</code>.
	 */
	public static String generateReplacerJS(WSuggestionPopup.Options options) {
		return instantiateStdMatcher(options) + ".replace";
	}

	/**
	 * Sets the minimum input length before showing the popup.
	 * <p>
	 * When the user has typed this much characters,
	 * {@link WSuggestionPopup#filterModel() filterModel()} is emitted which
	 * allows you to filter the model based on the initial input.
	 * <p>
	 * The default value is 0, which has the effect of always showing the entire
	 * model.
	 * <p>
	 * A value of -1 indicates that server-side filtering is continuously
	 * applied, no matter the length of the text input.
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

	/**
	 * Controls how the suggestion popup is positioned.
	 * <p>
	 * When <code>global</code> is <code>true</code>, then the popup will
	 * position itself globally. This avoids that the popup is affected by
	 * enclosing parents with overflow settings that clip the popup. This makes
	 * the popup however no longer follow the popup line edit when this line
	 * edit moves.
	 * <p>
	 * The default is <code>false</code>.
	 */
	public void setGlobalPopup(boolean global) {
		this.global_ = global;
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
	private boolean filtering_;
	private int defaultValue_;
	private String matcherJS_;
	private String replacerJS_;
	private WContainerWidget content_;
	private Signal1<String> filterModel_;
	private Signal2<Integer, WFormWidget> activated_;
	private List<AbstractSignal.Connection> modelConnections_;
	private JSignal1<String> filter_;
	private JSignal2<String, String> jactivated_;
	private List<WFormWidget> edits_;
	private boolean global_;

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
		this.filtering_ = true;
		this.filterModel_.trigger(input);
		this.filtering_ = false;
		this.doJavaScript("jQuery.data(" + this.getJsRef()
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
			logger.error(new StringWriter()
					.append("activate from bogus editor").toString());
		}
		for (int i = 0; i < this.content_.getCount(); ++i) {
			if (this.content_.getWidget(i).getId().equals(itemId)) {
				this.activated_.trigger(i, edit);
				return;
			}
		}
		logger.error(new StringWriter().append("activate for bogus item")
				.toString());
	}

	private void connectObjJS(AbstractEventSignal s, String methodName) {
		String jsFunction = "function(obj, event) {var o = jQuery.data("
				+ this.getJsRef() + ", 'obj');if (o) o." + methodName
				+ "(obj, event);}";
		s.addListener(jsFunction);
	}

	private void modelRowsInserted(WModelIndex parent, int start, int end) {
		if (this.filterLength_ != 0 && !this.filtering_) {
			return;
		}
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
			if (start < this.content_.getCount()) {
				if (this.content_.getWidget(start) != null)
					this.content_.getWidget(start).remove();
			} else {
				break;
			}
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
		this.modelRowsInserted(null, 0, this.model_.getRowCount() - 1);
	}

	private void defineJavaScript() {
		WApplication app = WApplication.getInstance();
		String THIS_JS = "js/WSuggestionPopup.js";
		app.loadJavaScript(THIS_JS, wtjs1());
		app.loadJavaScript(THIS_JS, wtjs2());
		this.doJavaScript("new Wt3_1_11.WSuggestionPopup("
				+ app.getJavaScriptClass() + "," + this.getJsRef() + ","
				+ this.replacerJS_ + "," + this.matcherJS_ + ","
				+ String.valueOf(this.filterLength_) + ","
				+ String.valueOf(this.defaultValue_) + ","
				+ (this.global_ ? "true" : "false") + ");");
	}

	void render(EnumSet<RenderFlag> flags) {
		if (!EnumUtils.mask(flags, RenderFlag.RenderFull).isEmpty()) {
			this.defineJavaScript();
		}
		super.render(flags);
	}

	static WJavaScriptPreamble wtjs1() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptConstructor,
				"WSuggestionPopup",
				"function(u,f,x,D,r,y,z){function c(a){return $(a).hasClass(\"Wt-suggest-onedit\")||$(a).hasClass(\"Wt-suggest-dropdown\")}function e(){return f.style.display!=\"none\"}function i(a){d.positionAtWidget(f.id,a.id,d.Vertical,z,true)}function n(a){a=d.target(a||window.event);if(a.className!=\"content\"){for(;a&&!d.hasTag(a,\"DIV\");)a=a.parentNode;a&&p(a)}}function p(a){var b=a.firstChild,k=d.getElement(g),l=b.innerHTML;b=b.getAttribute(\"sug\");k.focus(); u.emit(f,\"select\",a.id,k.id);x(k,l,b);m();g=null}function m(){f.style.display=\"none\";if(g!=null&&A!=null){d.getElement(g).onkeydown=A;A=null}}function B(a,b){for(a=b?a.nextSibling:a.previousSibling;a;a=b?a.nextSibling:a.previousSibling)if(d.hasTag(a,\"DIV\"))if(a.style.display!=\"none\")return a;return null}function G(a){var b=a.parentNode;if(a.offsetTop+a.offsetHeight>b.scrollTop+b.clientHeight)b.scrollTop=a.offsetTop+a.offsetHeight-b.clientHeight;else if(a.offsetTop<b.scrollTop)b.scrollTop=a.offsetTop} $(\".Wt-domRoot\").add(f);jQuery.data(f,\"obj\",this);var s=this,d=u.WT,o=null,g=null,H=false,I=null,J=null,C=null,E=null,t=false;this.defaultValue=y;var A=null;this.showPopup=function(a){f.style.display=\"\";E=o=null;A=a.onkeydown;a.onkeydown=function(b){s.editKeyDown(this,b||window.event)}};this.editMouseMove=function(a,b){if(c(a))a.style.cursor=d.widgetCoordinates(a,b).x>a.offsetWidth-16?\"default\":\"\"};this.showAt=function(a){m();g=a.id;t=true;s.refilter()};this.editClick=function(a,b){if(c(a))if(d.widgetCoordinates(a, b).x>a.offsetWidth-16)if(g!=a.id||!e())s.showAt(a);else{m();g=null}};this.editKeyDown=function(a,b){if(!c(a))return true;if(g!=a.id)if($(a).hasClass(\"Wt-suggest-onedit\")){g=a.id;t=false}else if($(a).hasClass(\"Wt-suggest-dropdown\")&&b.keyCode==40){g=a.id;t=true}else{g=null;return true}var k=o?d.getElement(o):null;if(e()&&k)if(b.keyCode==13||b.keyCode==9){p(k);d.cancelEvent(b);setTimeout(function(){a.focus()},0);return false}else if(b.keyCode==40||b.keyCode==38||b.keyCode==34||b.keyCode==33){if(b.type.toUpperCase()== \"KEYDOWN\"){H=true;d.cancelEvent(b,d.CancelDefaultAction)}if(b.type.toUpperCase()==\"KEYPRESS\"&&H==true){d.cancelEvent(b);return false}var l=k,q=b.keyCode==40||b.keyCode==34;b=b.keyCode==34||b.keyCode==33?f.clientHeight/k.offsetHeight:1;var j;for(j=0;l&&j<b;++j){var v=B(l,q);if(!v)break;l=v}if(l&&d.hasTag(l,\"DIV\")){k.className=\"\";l.className=\"sel\";o=l.id}return false}return b.keyCode!=13&&b.keyCode!=9};this.filtered=function(a){I=a;s.refilter()};this.refilter=function(){var a=o?d.getElement(o):null, b=d.getElement(g),k=D(b),l=f.lastChild.childNodes,q=k(null);E=b.value;if(r!=0)if(q.length<r&&!t){m();return}else{var j=r==-1?q:q.substring(0,r);if(j!=I){if(j!=J){J=j;u.emit(f,\"filter\",j)}if(!t){m();return}}}var v=j=null;q=t&&q.length==0;var w,K;w=0;for(K=l.length;w<K;++w){var h=l[w];if(d.hasTag(h,\"DIV\")){if(h.orig==null)h.orig=h.firstChild.innerHTML;var F=k(h.orig),L=q||F.match;if(F.suggestion!=h.firstChild.innerHTML)h.firstChild.innerHTML=F.suggestion;if(L){if(h.style.display!=\"\")h.style.display= \"\";if(j==null)j=h;if(w==this.defaultValue)v=h}else if(h.style.display!=\"none\")h.style.display=\"none\";if(h.className!=\"\")h.className=\"\"}}if(j==null)m();else{if(!e()){i(b);s.showPopup(b);a=null}if(!a||a.style.display==\"none\"){a=v||j;a.parentNode.scrollTop=0;o=a.id}a.className=\"sel\";G(a)}};this.editKeyUp=function(a,b){if(g!=null)if(c(a))if(!(!e()&&(b.keyCode==13||b.keyCode==9)))if(b.keyCode==27||b.keyCode==37||b.keyCode==39)m();else if(a.value!=E)s.refilter();else(a=o?d.getElement(o):null)&&G(a)};f.lastChild.onclick= n;f.lastChild.onscroll=function(){if(C){clearTimeout(C);var a=d.getElement(g);a&&a.focus()}};this.delayHide=function(a){C=setTimeout(function(){C=null;if(f&&(a==null||g==a.id))m()},300)}}");
	}

	static WJavaScriptPreamble wtjs2() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptConstructor,
				"WSuggestionPopupStdMatcher",
				"function(u,f,x,D,r,y){function z(c){var e=c.value;c=c.selectionStart?c.selectionStart:e.length;for(var i=x?e.lastIndexOf(x,c-1)+1:0;i<c&&D.indexOf(e.charAt(i))!=-1;)++i;return{start:i,end:c}}this.match=function(c){var e=z(c),i=c.value.substring(e.start,e.end),n=\"^\";if(r.length!=0)n=\"(^|(?:[\"+r+\"]))\";n+=\"(\"+i.replace(new RegExp(\"([\\\\^\\\\\\\\\\\\][\\\\-.$*+?()|{}])\",\"g\"),\"\\\\$1\")+\")\";n=new RegExp(n,\"gi\");return function(p){if(!p)return i; var m=false;if(i.length){var B=p.replace(n,\"$1\"+u+\"$2\"+f);if(B!=p){m=true;p=B}}return{match:m,suggestion:p}}};this.replace=function(c,e,i){e=z(c);var n=c.value.substring(0,e.start)+i+y;if(e.end<c.value.length)n+=c.value.substring(e.end,c.value.length);c.value=n;if(c.selectionStart){c.selectionStart=e.start+i.length+y.length;c.selectionEnd=c.selectionStart}}}");
	}

	static String instantiateStdMatcher(WSuggestionPopup.Options options) {
		StringBuilder s = new StringBuilder();
		s.append("new Wt3_1_11.WSuggestionPopupStdMatcher(").append(
				WWebWidget.jsStringLiteral(options.highlightBeginTag)).append(
				", ").append(
				WWebWidget.jsStringLiteral(options.highlightEndTag)).append(
				", ");
		if (options.listSeparator != 0) {
			s.append(WWebWidget.jsStringLiteral("" + options.listSeparator));
		} else {
			s.append("null");
		}
		s.append(", ").append(WWebWidget.jsStringLiteral(options.whitespace))
				.append(", ").append(
						WWebWidget.jsStringLiteral(options.wordSeparators))
				.append(", ").append(
						WWebWidget.jsStringLiteral(options.appendReplacedText))
				.append(")");
		return s.toString();
	}
}
