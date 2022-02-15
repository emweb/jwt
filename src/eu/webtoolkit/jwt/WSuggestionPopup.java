/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.servlet.*;
import eu.webtoolkit.jwt.utils.*;
import java.io.*;
import java.lang.ref.*;
import java.time.*;
import java.util.*;
import java.util.regex.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A widget which popups to assist in editing a textarea or lineedit.
 *
 * <p>This widget may be associated with one or more {@link WFormWidget WFormWidgets} (typically a
 * {@link WLineEdit} or a {@link WTextArea}).
 *
 * <p>The popup provides the user with suggestions to enter input. The popup can be used by one or
 * more editors, using {@link WSuggestionPopup#forEdit(WFormWidget edit, EnumSet triggers)
 * forEdit()}. The popup will show when the user starts editing the edit field, or when the user
 * opens the suggestions explicitly using a drop down icon or with the down key. The popup positions
 * itself intelligently just below or just on top of the edit field. It offers a list of suggestions
 * that match in some way with the current edit field, and dynamically adjusts this list. The
 * implementation for matching individual suggestions with the current text is provided through a
 * JavaScript function. This function may also highlight part(s) of the suggestions to provide
 * feed-back on how they match.
 *
 * <p>WSuggestionPopup is an MVC view class, using a simple {@link WStringListModel} by default. You
 * can set a custom model using {@link WSuggestionPopup#setModel(WAbstractItemModel model)
 * setModel()}. The model can provide different text for the suggestion text ({@link
 * ItemDataRole#Display}) and value ({@link WSuggestionPopup#getEditRole() getEditRole()}). The
 * member methods {@link WSuggestionPopup#clearSuggestions() clearSuggestions()} and {@link
 * WSuggestionPopup#addSuggestion(CharSequence suggestionText, CharSequence suggestionValue)
 * addSuggestion()} manipulate this model.
 *
 * <p>By default, the popup implements all filtering client-side. To support large datasets, you may
 * enable server-side filtering of suggestions based on the input. The server-side filtering may
 * provide a coarse filtering using a fixed size prefix of the entered text, and complement the
 * client-side filtering. To enable server-side filtering, use {@link
 * WSuggestionPopup#setFilterLength(int length) setFilterLength()} and listen to filter notification
 * using the modelFilter() signal. Whenever a filter event is generated you can adjust the
 * model&apos;s content according to the filter (e.g. using a {@link WSortFilterProxyModel}). By
 * using {@link WCompositeWidget#setMaximumSize(WLength width, WLength height)
 * WCompositeWidget#setMaximumSize()} you can also limit the maximum height of the popup, in which
 * case scrolling is supported (similar to a combo-box).
 *
 * <p>The class is initialized with an {@link Options} struct which configures how suggestion
 * filtering and result editing is done. Alternatively, you can provide two JavaScript functions,
 * one for filtering the suggestions, and one for editing the value of the textarea when a
 * suggestion is selected.
 *
 * <p>The matcherJS function must have the following JavaScript signature:
 *
 * <p>
 *
 * <pre>{@code
 * function (editElement) {
 * // fetch the location of cursor and current text in the editElement.
 *
 * // return a function that matches a given suggestion with the current value of the editElement.
 * return function(suggestion) {
 *
 * // 1) if suggestion is null, simply return the current text 'value'
 * // 2) check suggestion if it matches
 * // 3) add highlighting markup to suggestion if necessary
 *
 * return { match : ...,      // does the suggestion match ? (boolean)
 * suggestion : ...  // modified suggestion with highlighting
 * };
 * }
 * }
 *
 * }</pre>
 *
 * <p>The replacerJS function that edits the value has the following JavaScript signature.
 *
 * <p>
 *
 * <pre>{@code
 * function (editElement, suggestionText, suggestionValue) {
 * // editElement is the form element which must be edited.
 * // suggestionText is the displayed text for the matched suggestion.
 * // suggestionValue is the stored value for the matched suggestion.
 *
 * // computed modifiedEditValue and modifiedPos ...
 *
 * editElement.value = modifiedEditValue;
 * editElement.selectionStart = edit.selectionEnd = modifiedPos;
 * }
 *
 * }</pre>
 *
 * <p>To style the suggestions, you should style the &lt;span&gt; element inside this widget, and
 * the &lt;span&gt;.&quot;sel&quot; element to style the current selection.
 *
 * <p>Usage example:
 *
 * <pre>{@code
 * // options for email address suggestions
 * WSuggestionPopup.Options contactOptions = new WSuggestionPopup.Options();
 * contactOptions.highlightBeginTag = "<b>";
 * contactOptions.highlightEndTag = "</b>";
 * contactOptions.listSeparator = ','; //for multiple addresses)
 * contactOptions.whitespace = " \n";
 * contactOptions.wordSeparators = "-., \"@\n;"; //within an address
 * contactOptions.appendReplacedText = ", "; //prepare next email address
 *
 * WSuggestionPopup popup = new WSuggestionPopup(contactOptions, this);
 *
 * WTextArea textEdit = new WTextArea(this);
 * popup.forEdit(textEdit);
 *
 * // load popup data
 * for (int i = 0; i < contacts.size(); ++i)
 * popup.addSuggestion(contacts.get(i).formatted(), contacts.get(i).formatted());
 *
 * }</pre>
 *
 * <p>
 *
 * <p><i><b>Note: </b>This widget is not supposed to be added explicitly, as it is a global widget
 * (much like {@link WPopupWidget}). Managing its lifetime can be achieved with
 * WContainerWidget::addNew() or WObject::addChild(), where the former uses the latter. Do NOT bind
 * it to a template using {@link WTemplate#bindWidget(String varName, WWidget widget)
 * WTemplate#bindWidget()}, WTemplate::bindNew() or {@link WLayout#addWidget(WWidget w)
 * WLayout#addWidget()}. If bound this way, the placeholder is not replaced with the correct list of
 * suggestions, since this causes the widget to be placed into the widget tree twice.</i> A
 * screenshot of this example:
 *
 * <table border="1" cellspacing="3" cellpadding="3">
 * <tr><td><div align="center">
 * <img src="doc-files/WSuggestionPopup-default-1.png">
 * <p>
 * <strong>An example WSuggestionPopup (default)</strong></p>
 * </div>
 *
 * </td><td><div align="center">
 * <img src="doc-files/WSuggestionPopup-polished-1.png">
 * <p>
 * <strong>An example WSuggestionPopup (polished)</strong></p>
 * </div>
 *
 * </td></tr>
 * </table>
 *
 * <p>When using the DropDownIcon trigger, an additional style class is provided for the edit field:
 * <code>Wt-suggest-dropdown</code>, which renders the icon to the right inside the edit field. This
 * class may be used to customize how the drop down icon is rendered.
 *
 * <p>
 *
 * <p><i><b>Note: </b>This widget requires JavaScript support. </i>
 */
public class WSuggestionPopup extends WPopupWidget {
  private static Logger logger = LoggerFactory.getLogger(WSuggestionPopup.class);

  /**
   * A configuration object to generate a matcher and replacer JavaScript function.
   *
   * <p>
   *
   * @see WSuggestionPopup
   */
  public static class Options {
    private static Logger logger = LoggerFactory.getLogger(Options.class);

    /** Constructor. */
    public Options() {
      this.highlightBeginTag = "";
      this.highlightEndTag = "";
      this.listSeparator = 0;
      this.whitespace = "";
      this.wordSeparators = "";
      this.appendReplacedText = "";
      this.wordStartRegexp = "";
    }
    /**
     * Open tag to highlight a match in a suggestion.
     *
     * <p>Must be an opening markup tag, such as &lt;b&gt;.
     *
     * <p>Used during matching.
     */
    public String highlightBeginTag;
    /**
     * Close tag to highlight a match in a suggestion.
     *
     * <p>Must be a closing markup tag, such as &lt;/b&gt;.
     *
     * <p>Used during matching.
     */
    public String highlightEndTag;
    /**
     * When editing a list of values, the separator used for different items.
     *
     * <p>For example, &apos;,&apos; to separate different values on comma. Specify 0
     * (&apos;\0&apos;) for no list separation.
     *
     * <p>Used during matching and replacing.
     */
    public char listSeparator;
    /**
     * When editing a value, the whitespace characters ignored before the current value.
     *
     * <p>For example, &quot; \n&quot; to ignore spaces and newlines.
     *
     * <p>Used during matching and replacing.
     */
    public String whitespace;
    /**
     * Characters that start a word in a suggestion to match against.
     *
     * <p>For example, &quot; .@&quot; will also match with suggestion text after a space, a dot (.)
     * or an at-symbol (@). Alternatively you may also specify this as a regular expression in
     * <code>wordStartRegexp</code>.
     *
     * <p>Used during matching.
     */
    public String wordSeparators;
    /**
     * When replacing the current edited value with suggestion value, append the following string as
     * well.
     *
     * <p>Used during replacing.
     */
    public String appendReplacedText;
    /**
     * Regular expression that starts a word in a suggestion to match against.
     *
     * <p>When empty, the value of <code>wordSeparators</code> is used instead.
     *
     * <p>Used during replacing.
     */
    public String wordStartRegexp;
  }
  /**
   * Creates a suggestion popup.
   *
   * <p>The popup using a standard matcher and replacer implementation that is configured using the
   * provided <code>options</code>.
   *
   * <p>
   *
   * @see WSuggestionPopup#generateMatcherJS(WSuggestionPopup.Options options)
   * @see WSuggestionPopup#generateReplacerJS(WSuggestionPopup.Options options)
   */
  public WSuggestionPopup(final WSuggestionPopup.Options options) {
    super(new WContainerWidget());
    this.model_ = null;
    this.modelColumn_ = 0;
    this.filterLength_ = 0;
    this.filtering_ = false;
    this.defaultValue_ = -1;
    this.isDropDownIconUnfiltered_ = false;
    this.isAutoSelectEnabled_ = true;
    this.currentItem_ = -1;
    this.editRole_ = ItemDataRole.User;
    this.matcherJS_ = generateMatcherJS(options);
    this.replacerJS_ = generateReplacerJS(options);
    this.filterModel_ = new Signal1<String>();
    this.activated_ = new Signal2<Integer, WFormWidget>();
    this.modelConnections_ = new ArrayList<AbstractSignal.Connection>();
    this.currentInputText_ = "";
    this.filter_ = new JSignal1<String>(this.getImplementation(), "filter") {};
    this.jactivated_ = new JSignal2<String, String>(this.getImplementation(), "select") {};
    this.edits_ = new ArrayList<WFormWidget>();
    this.init();
  }
  /**
   * Creates a suggestion popup with given matcherJS and replacerJS.
   *
   * <p>See supra for the expected signature of the matcher and replace JavaScript functions.
   */
  public WSuggestionPopup(final String matcherJS, final String replacerJS) {
    super(new WContainerWidget());
    this.model_ = null;
    this.modelColumn_ = 0;
    this.filterLength_ = 0;
    this.filtering_ = false;
    this.defaultValue_ = -1;
    this.isDropDownIconUnfiltered_ = false;
    this.isAutoSelectEnabled_ = true;
    this.currentItem_ = -1;
    this.editRole_ = ItemDataRole.User;
    this.matcherJS_ = matcherJS;
    this.replacerJS_ = replacerJS;
    this.filterModel_ = new Signal1<String>();
    this.activated_ = new Signal2<Integer, WFormWidget>();
    this.modelConnections_ = new ArrayList<AbstractSignal.Connection>();
    this.currentInputText_ = "";
    this.filter_ = new JSignal1<String>(this.getImplementation(), "filter") {};
    this.jactivated_ = new JSignal2<String, String>(this.getImplementation(), "select") {};
    this.edits_ = new ArrayList<WFormWidget>();
    this.init();
  }
  /**
   * Lets this suggestion popup assist in editing an edit field.
   *
   * <p>A single suggestion popup may assist in several edits by repeated calls of this method.
   *
   * <p>The <code>popupTriggers</code> control how editing is triggered (either by the user editing
   * the field by entering keys or by an explicit drop down menu that is shown inside the edit).
   *
   * <p>
   *
   * @see WSuggestionPopup#removeEdit(WFormWidget edit)
   */
  public void forEdit(WFormWidget edit, EnumSet<PopupTrigger> triggers) {
    final AbstractEventSignal b = edit.keyPressed();
    this.connectObjJS(b, "editKeyDown");
    this.connectObjJS(edit.keyWentDown(), "editKeyDown");
    this.connectObjJS(edit.keyWentUp(), "editKeyUp");
    this.connectObjJS(edit.blurred(), "delayHide");
    if (triggers.contains(PopupTrigger.Editing)) {
      edit.addStyleClass("Wt-suggest-onedit");
    }
    if (triggers.contains(PopupTrigger.DropDownIcon)) {
      edit.addStyleClass("Wt-suggest-dropdown");
      final AbstractEventSignal c = edit.clicked();
      this.connectObjJS(c, "editClick");
      this.connectObjJS(edit.mouseMoved(), "editMouseMove");
    }
    this.edits_.add(edit);
  }
  /**
   * Lets this suggestion popup assist in editing an edit field.
   *
   * <p>Calls {@link #forEdit(WFormWidget edit, EnumSet triggers) forEdit(edit, EnumSet.of(trigger,
   * triggers))}
   */
  public final void forEdit(WFormWidget edit, PopupTrigger trigger, PopupTrigger... triggers) {
    forEdit(edit, EnumSet.of(trigger, triggers));
  }
  /**
   * Lets this suggestion popup assist in editing an edit field.
   *
   * <p>Calls {@link #forEdit(WFormWidget edit, EnumSet triggers) forEdit(edit,
   * EnumSet.of(PopupTrigger.Editing))}
   */
  public final void forEdit(WFormWidget edit) {
    forEdit(edit, EnumSet.of(PopupTrigger.Editing));
  }
  /**
   * Removes the edit field from the list of assisted editors.
   *
   * <p>The editor will no longer be assisted by this popup widget.
   *
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
   *
   * <p>This is equivalent to the user triggering the suggestion popup to be shown.
   */
  public void showAt(WFormWidget edit) {
    this.doJavaScript(this.getJsRef() + ".wtObj.showAt(" + edit.getJsRef() + ");");
  }
  /**
   * Clears the list of suggestions.
   *
   * <p>This clears the underlying model.
   *
   * <p>
   *
   * @see WSuggestionPopup#addSuggestion(CharSequence suggestionText, CharSequence suggestionValue)
   */
  public void clearSuggestions() {
    this.model_.removeRows(0, this.model_.getRowCount());
  }
  /**
   * Adds a new suggestion.
   *
   * <p>This adds an entry to the underlying model. The <code>suggestionText</code> is set as {@link
   * ItemDataRole#Display} and the <code>suggestionValue</code> (which is inserted into the edit
   * field on selection) is set as {@link WSuggestionPopup#getEditRole() getEditRole()}.
   *
   * <p>
   *
   * @see WSuggestionPopup#clearSuggestions()
   * @see WSuggestionPopup#setModel(WAbstractItemModel model)
   */
  public void addSuggestion(final CharSequence suggestionText, final CharSequence suggestionValue) {
    int row = this.model_.getRowCount();
    if (this.model_.insertRow(row)) {
      this.model_.setData(row, this.modelColumn_, suggestionText, ItemDataRole.Display);
      if (!(suggestionValue.length() == 0)) {
        this.model_.setData(row, this.modelColumn_, suggestionValue, this.getEditRole());
      }
    }
  }
  /**
   * Adds a new suggestion.
   *
   * <p>Calls {@link #addSuggestion(CharSequence suggestionText, CharSequence suggestionValue)
   * addSuggestion(suggestionText, WString.Empty)}
   */
  public final void addSuggestion(final CharSequence suggestionText) {
    addSuggestion(suggestionText, WString.Empty);
  }
  /**
   * Sets the model to be used for the suggestions.
   *
   * <p>The <code>model</code> may not be <code>null</code>.
   *
   * <p>The default value is a {@link WStringListModel} that is owned by the suggestion popup.
   *
   * <p>The {@link ItemDataRole#Display} is used for the suggestion text. The {@link
   * WSuggestionPopup#getEditRole() getEditRole()} is used for the suggestion value, unless empty,
   * in which case the suggestion text is used as value.
   *
   * <p>
   *
   * @see WSuggestionPopup#setModelColumn(int modelColumn)
   */
  public void setModel(final WAbstractItemModel model) {
    if (this.model_ != null) {
      for (int i = 0; i < this.modelConnections_.size(); ++i) {
        this.modelConnections_.get(i).disconnect();
      }
      this.modelConnections_.clear();
    }
    this.model_ = model;
    this.modelConnections_.add(
        this.model_
            .rowsInserted()
            .addListener(
                this,
                (WModelIndex e1, Integer e2, Integer e3) -> {
                  WSuggestionPopup.this.modelRowsInserted(e1, e2, e3);
                }));
    this.modelConnections_.add(
        this.model_
            .rowsRemoved()
            .addListener(
                this,
                (WModelIndex e1, Integer e2, Integer e3) -> {
                  WSuggestionPopup.this.modelRowsRemoved(e1, e2, e3);
                }));
    this.modelConnections_.add(
        this.model_
            .dataChanged()
            .addListener(
                this,
                (WModelIndex e1, WModelIndex e2) -> {
                  WSuggestionPopup.this.modelDataChanged(e1, e2);
                }));
    this.modelConnections_.add(
        this.model_
            .layoutChanged()
            .addListener(
                this,
                () -> {
                  WSuggestionPopup.this.modelLayoutChanged();
                }));
    this.modelConnections_.add(
        this.model_
            .modelReset()
            .addListener(
                this,
                () -> {
                  WSuggestionPopup.this.modelLayoutChanged();
                }));
    this.setModelColumn(this.modelColumn_);
  }
  /**
   * Returns the data model.
   *
   * <p>
   *
   * @see WSuggestionPopup#setModel(WAbstractItemModel model)
   */
  public WAbstractItemModel getModel() {
    return this.model_;
  }
  /**
   * Sets the column in the model to be used for the items.
   *
   * <p>The column <code>index</code> in the model will be used to retrieve data.
   *
   * <p>The default value is 0.
   *
   * <p>
   *
   * @see WSuggestionPopup#setModel(WAbstractItemModel model)
   */
  public void setModelColumn(int modelColumn) {
    this.modelColumn_ = modelColumn;
    this.impl_.clear();
    this.modelRowsInserted(null, 0, this.model_.getRowCount() - 1);
  }
  /**
   * Sets a default selected value.
   *
   * <p><code>row</code> is the model row that is selected by default (only if it matches the
   * current input).
   *
   * <p>The default value is -1, indicating no default.
   */
  public void setDefaultIndex(int row) {
    if (this.defaultValue_ != row) {
      this.defaultValue_ = row;
      if (this.isRendered()) {
        this.doJavaScript(
            this.getJsRef() + ".wtObj.defaultValue = " + String.valueOf(this.defaultValue_) + ';');
      }
    }
  }
  /** Returns the default value. */
  public int getDefaultIndex() {
    return this.defaultValue_;
  }
  /**
   * Creates a standard matcher JavaScript function.
   *
   * <p>This returns a JavaScript function that provides a standard implementation for the matching
   * input, based on the given <code>options</code>.
   */
  public static String generateMatcherJS(final WSuggestionPopup.Options options) {
    return instantiateStdMatcher(options) + ".match";
  }
  /**
   * Creates a standard replacer JavaScript function.
   *
   * <p>This returns a JavaScript function that provides a standard implementation for reacting to a
   * match activation, editing the line edit text.
   */
  public static String generateReplacerJS(final WSuggestionPopup.Options options) {
    return instantiateStdMatcher(options) + ".replace";
  }
  /**
   * Sets the minimum input length before showing the popup.
   *
   * <p>When the user has typed this much characters, {@link WSuggestionPopup#filterModel()
   * filterModel()} is emitted which allows you to filter the model based on the initial input. The
   * filtering is done as long as the model indicates that results are partial by setting a {@link
   * ItemDataRole#StyleClass} of &quot;Wt-more-data&quot; on the last item.
   *
   * <p>The default value is 0.
   *
   * <p>A value of -1 is a equivalent to 0 but filtering is always applied as if the last item
   * always has &quot;Wt-more-data&quot; (for backwards compatibility)
   *
   * <p>
   *
   * @see WSuggestionPopup#filterModel()
   */
  public void setFilterLength(int length) {
    this.filterLength_ = length;
  }
  /**
   * Returns the filter length.
   *
   * <p>
   *
   * @see WSuggestionPopup#setFilterLength(int length)
   */
  public int getFilterLength() {
    return this.filterLength_;
  }
  /**
   * Signal that indicates that the model should be filtered.
   *
   * <p>The argument is the initial input. When {@link PopupTrigger#Editing} is used as edit
   * trigger, its length will always equal the {@link WSuggestionPopup#getFilterLength()
   * getFilterLength()}. When {@link PopupTrigger#DropDownIcon} is used as edit trigger, the input
   * length may be less than {@link WSuggestionPopup#getFilterLength() getFilterLength()}, and the
   * the signal will be called repeatedly as the user provides more input.
   *
   * <p>For example, if you are using a {@link WSortFilterProxyModel}, you could react to this
   * signal with:
   *
   * <pre>{@code
   * public filterSuggestions(String filter) {
   * proxyModel.setFilterRegExp(filter + ".*");
   * }
   *
   * }</pre>
   */
  public Signal1<String> filterModel() {
    return this.filterModel_;
  }
  /**
   * Signal emitted when a suggestion was selected.
   *
   * <p>The selected item is passed as the first argument and the editor as the second.
   */
  public Signal2<Integer, WFormWidget> activated() {
    return this.activated_;
  }
  /**
   * When drop down icon is clicked the popup content will be unfiltered.
   *
   * <p>
   *
   * @see WSuggestionPopup#forEdit(WFormWidget edit, EnumSet triggers)
   * @see PopupTrigger
   */
  public void setDropDownIconUnfiltered(boolean isUnfiltered) {
    this.isDropDownIconUnfiltered_ = isUnfiltered;
  }
  /**
   * When the popup is opened the first item is automatically selected.
   *
   * <p>This cannot be changed after the widget is rendered. The default value is true.
   */
  public void setAutoSelectEnabled(boolean enabled) {
    this.isAutoSelectEnabled_ = enabled;
  }
  /**
   * Returns the last activated index.
   *
   * <p>Returns -1 if the popup hasn&apos;t been activated yet.
   *
   * <p>
   *
   * @see WSuggestionPopup#activated()
   */
  public int getCurrentItem() {
    return this.currentItem_;
  }
  /**
   * Sets the role used for editing the line edit with a chosen item.
   *
   * <p>The default value is {@link ItemDataRole#User}.
   */
  public void setEditRole(ItemDataRole role) {
    this.editRole_ = role;
  }
  /**
   * Returns the role used for editing the line edit.
   *
   * <p><i>{@link WSuggestionPopup#setEditRole(ItemDataRole role) setEditRole()}</i>
   */
  public ItemDataRole getEditRole() {
    return this.editRole_;
  }

  private WContainerWidget impl_;
  private WAbstractItemModel model_;
  private int modelColumn_;
  private int filterLength_;
  private boolean filtering_;
  private int defaultValue_;
  private boolean isDropDownIconUnfiltered_;
  private boolean isAutoSelectEnabled_;
  private int currentItem_;
  private ItemDataRole editRole_;
  private String matcherJS_;
  private String replacerJS_;
  private Signal1<String> filterModel_;
  private Signal2<Integer, WFormWidget> activated_;
  private List<AbstractSignal.Connection> modelConnections_;
  private String currentInputText_;
  private JSignal1<String> filter_;
  private JSignal2<String, String> jactivated_;
  private List<WFormWidget> edits_;

  private void init() {
    this.impl_ = ObjectUtils.cast(this.getImplementation(), WContainerWidget.class);
    this.impl_.setList(true);
    this.impl_.setLoadLaterWhenInvisible(false);
    this.setAttributeValue("style", "z-index: 10000; display: none; overflow: auto");
    this.setModel(new WStringListModel());
    this.impl_
        .escapePressed()
        .addListener(
            this,
            () -> {
              WSuggestionPopup.this.hide();
            });
    this.filter_.addListener(
        this,
        (String e1) -> {
          WSuggestionPopup.this.scheduleFilter(e1);
        });
    this.jactivated_.addListener(
        this,
        (String e1, String e2) -> {
          WSuggestionPopup.this.doActivate(e1, e2);
        });
  }

  private void scheduleFilter(String input) {
    this.currentInputText_ = input;
    this.scheduleRender();
  }

  private void doFilter(String input) {
    this.filtering_ = true;
    this.filterModel_.trigger(input);
    this.filtering_ = false;
    WApplication.getInstance()
        .doJavaScript(
            this.getJsRef()
                + ".wtObj.filtered("
                + WWebWidget.jsStringLiteral(input)
                + ","
                + (this.isPartialResults() ? "1" : "0")
                + ");");
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
      logger.error(new StringWriter().append("activate from bogus editor").toString());
      this.currentItem_ = -1;
      return;
    }
    for (int i = 0; i < this.impl_.getCount(); ++i) {
      if (this.impl_.getWidget(i).getId().equals(itemId)) {
        this.currentItem_ = i;
        this.activated_.trigger(i, edit);
        if (edit != null) {
          WLineEdit le = ObjectUtils.cast(edit, WLineEdit.class);
          WTextArea ta = ObjectUtils.cast(edit, WTextArea.class);
          if (le != null) {
            le.textInput().trigger();
          } else {
            if (ta != null) {
              ta.textInput().trigger();
            }
          }
          edit.changed().trigger();
        }
        return;
      }
    }
    this.currentItem_ = -1;
    logger.error(new StringWriter().append("activate for bogus item").toString());
  }

  private void connectObjJS(final AbstractEventSignal s, final String methodName) {
    String jsFunction =
        "function(obj, event) {var o = "
            + this.getJsRef()
            + ";if (o && o.wtObj) o.wtObj."
            + methodName
            + "(obj, event);}";
    s.addListener(jsFunction);
  }

  private void modelRowsInserted(final WModelIndex parent, int start, int end) {
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
      this.impl_.insertWidget(i, line);
      WModelIndex index = this.model_.getIndex(i, this.modelColumn_);
      Object d = index.getData();
      TextFormat format =
          index.getFlags().contains(ItemFlag.XHTMLText) ? TextFormat.XHTML : TextFormat.Plain;
      WAnchor anchor = new WAnchor();
      line.addWidget(anchor);
      WText value = new WText(StringUtils.asString(d), format);
      anchor.addWidget(value);
      Object d2 = index.getData(this.editRole_);
      if (!(d2 != null)) {
        d2 = d;
      }
      value.setAttributeValue("sug", StringUtils.asString(d2).toString());
      Object styleclass = index.getData(ItemDataRole.StyleClass);
      if ((styleclass != null)) {
        value.setAttributeValue("class", StringUtils.asString(styleclass).toString());
      }
    }
  }

  private void modelRowsRemoved(final WModelIndex parent, int start, int end) {
    if ((parent != null)) {
      return;
    }
    for (int i = start; i <= end; ++i) {
      if (start < this.impl_.getCount()) {
        {
          WWidget toRemove = this.impl_.removeWidget(this.impl_.getWidget(start));
          if (toRemove != null) toRemove.remove();
        }

      } else {
        break;
      }
    }
  }

  private void modelDataChanged(final WModelIndex topLeft, final WModelIndex bottomRight) {
    if ((topLeft.getParent() != null)) {
      return;
    }
    if (this.modelColumn_ < topLeft.getColumn() || this.modelColumn_ > bottomRight.getColumn()) {
      return;
    }
    for (int i = topLeft.getRow(); i <= bottomRight.getRow(); ++i) {
      WContainerWidget w = ObjectUtils.cast(this.impl_.getWidget(i), WContainerWidget.class);
      WAnchor anchor = ObjectUtils.cast(w.getWidget(0), WAnchor.class);
      WText value = ObjectUtils.cast(anchor.getWidget(0), WText.class);
      WModelIndex index = this.model_.getIndex(i, this.modelColumn_);
      Object d = index.getData();
      value.setText(StringUtils.asString(d));
      TextFormat format =
          index.getFlags().contains(ItemFlag.XHTMLText) ? TextFormat.XHTML : TextFormat.Plain;
      value.setTextFormat(format);
      Object d2 = this.model_.getData(i, this.modelColumn_, this.getEditRole());
      if (!(d2 != null)) {
        d2 = d;
      }
      value.setAttributeValue("sug", StringUtils.asString(d2).toString());
    }
  }

  private void modelLayoutChanged() {
    this.impl_.clear();
    this.modelRowsInserted(null, 0, this.model_.getRowCount() - 1);
  }

  private boolean isPartialResults() {
    if (this.filterLength_ < 0) {
      return true;
    } else {
      if (this.model_.getRowCount() > 0) {
        WModelIndex index = this.model_.getIndex(this.model_.getRowCount() - 1, this.modelColumn_);
        Object styleclass = index.getData(ItemDataRole.StyleClass);
        return (StringUtils.asString(styleclass).toString().equals("Wt-more-data".toString()));
      } else {
        return false;
      }
    }
  }

  private void defineJavaScript() {
    WApplication app = WApplication.getInstance();
    String THIS_JS = "js/WSuggestionPopup.js";
    app.loadJavaScript(THIS_JS, wtjs1());
    app.loadJavaScript(THIS_JS, wtjs2());
    String ddUnfiltered = this.isDropDownIconUnfiltered_ ? "true" : "false";
    String autoSelect = this.isAutoSelectEnabled_ ? "true" : "false";
    this.setJavaScriptMember(
        " WSuggestionPopup",
        "new Wt4_6_2.WSuggestionPopup("
            + app.getJavaScriptClass()
            + ","
            + this.getJsRef()
            + ","
            + this.replacerJS_
            + ","
            + this.matcherJS_
            + ","
            + String.valueOf(Math.max(0, this.filterLength_))
            + ","
            + String.valueOf(this.isPartialResults())
            + ","
            + String.valueOf(this.defaultValue_)
            + ","
            + ddUnfiltered
            + ","
            + autoSelect
            + ");");
  }

  protected void render(EnumSet<RenderFlag> flags) {
    if (flags.contains(RenderFlag.Full)) {
      this.defineJavaScript();
    }
    if (WApplication.getInstance().getEnvironment().hasAjax()) {
      this.doFilter(this.currentInputText_);
    }
    super.render(flags);
  }

  static WJavaScriptPreamble wtjs1() {
    return new WJavaScriptPreamble(
        JavaScriptScope.WtClassScope,
        JavaScriptObjectType.JavaScriptConstructor,
        "WSuggestionPopup",
        "function(q,h,B,H,r,w,C,x,c){function e(a){return $(a).hasClass(\"Wt-suggest-onedit\")||$(a).hasClass(\"Wt-suggest-dropdown\")}function j(){return h.style.display!=\"none\"}function k(a){h.style.display=\"block\";d.positionAtWidget(h.id,a.id,d.Vertical)}function s(a){a=d.target(a||window.event);if(!d.hasTag(a,\"UL\")){for(;a&&!d.hasTag(a,\"LI\");)a=a.parentNode;a&&y(a)}}function y(a){var b=a.firstChild.firstChild,f=d.getElement(i),g=b.innerHTML;b= b.getAttribute(\"sug\");f.focus();B(f,g,b);q.emit(h,\"select\",a.id,f.id);m();i=null}function m(){h.style.display=\"none\";if(i!=null&&D!=null){d.getElement(i).onkeydown=D;D=null}}function M(a){if(typeof d.theme===\"object\"&&d.theme.type===\"bootstrap\"&&d.theme.version===5)try{var b=getComputedStyle(a),f=parseInt(b.backgroundSize.match(/^([0-9]+)px ([0-9]+)px$/)[1],10),g=parseInt(b.backgroundPositionX.match(/^calc[(]100% - ([0-9]+)px[)]$/)[1],10);return f+g}catch(t){return 28}else return 16}function S(a){var b= h.childNodes;for(n=a?b[0]:b[b.length-1];n;n=a?n.nextSibling:n.previousSibling)if(d.hasTag(n,\"LI\")&&n.style.display!=\"none\")return n;return null}function T(a,b){for(a=b?a.nextSibling:a.previousSibling;a;a=b?a.nextSibling:a.previousSibling)if(d.hasTag(a,\"LI\"))if(a.style.display!=\"none\")return a;return null}function I(a){var b=a.parentNode;if(a.offsetTop+a.offsetHeight>b.scrollTop+b.clientHeight)b.scrollTop=a.offsetTop+a.offsetHeight-b.clientHeight;else if(a.offsetTop<b.scrollTop)b.scrollTop=a.offsetTop} $(\".Wt-domRoot\").add(h);h.wtObj=this;var u=this,d=q.WT,o=null,i=null,N=false,E=null,O=null,P=w,F=null,G=null,z=false;this.defaultValue=C;var D=null;this.showPopup=function(a){h.style.display=\"block\";G=o=null;D=a.onkeydown;a.onkeydown=function(b){u.editKeyDown(this,b||window.event)}};this.editMouseMove=function(a,b){if(e(a))if(d.widgetCoordinates(a,b).x>a.offsetWidth-M(a)){a.classList&&a.classList.add(\"Wt-suggest-dropdown-hover\");a.style.cursor=\"default\"}else{a.classList&&a.classList.remove(\"Wt-suggest-dropdown-hover\"); a.style.cursor=\"\"}};this.showAt=function(a,b){m();i=a.id;z=true;u.refilter(b)};this.editClick=function(a,b){if(e(a))if(d.widgetCoordinates(a,b).x>a.offsetWidth-M(a))if(i!=a.id||!j())u.showAt(a,\"\");else{m();i=null}};this.editKeyDown=function(a,b){if(!e(a))return true;if(i!=a.id)if($(a).hasClass(\"Wt-suggest-onedit\")){i=a.id;z=false}else if($(a).hasClass(\"Wt-suggest-dropdown\")&&b.keyCode==40){i=a.id;z=true}else{i=null;return true}if(j()){var f=o?d.getElement(o):null;if(b.keyCode==13||b.keyCode==9){if(f){y(f); d.cancelEvent(b);setTimeout(function(){a.focus()},0)}else m();return false}else if(b.keyCode==40||b.keyCode==38||b.keyCode==34||b.keyCode==33){if(b.type.toUpperCase()==\"KEYDOWN\"){N=true;d.cancelEvent(b,d.CancelDefaultAction)}if(b.type.toUpperCase()==\"KEYPRESS\"&&N==true){d.cancelEvent(b);return false}var g=f,t=b.keyCode==40||b.keyCode==34;if(g){b=b.keyCode==34||b.keyCode==33?h.clientHeight/f.offsetHeight:1;var p;for(p=0;g&&p<b;++p){var v=T(g,t);if(!v&&c)break;g=v}}else{g=S(t);I(g)}if(f){f.className= \"\";o=null}if(g&&d.hasTag(g,\"LI\")){g.className=\"active\";if(typeof q.theme===\"object\"&&q.theme.type===\"bootstrap\"&&q.theme.version>=4){f.firstChild.className=f.firstChild.className.replace(\"active\",\"\");g.firstChild.className+=\" active\"}o=g.id}return false}}return b.keyCode!=13&&b.keyCode!=9};this.filtered=function(a,b){E=a;P=b;u.refilter(G)};this.refilter=function(a){if(i){var b=o?d.getElement(o):null,f=d.getElement(i),g=H(f),t=h.childNodes,p=x&&a!=null?a:g(null);G=x?a:f.value;if(r>0||w)if(p.length< r&&!z){m();return}else{a=P?p:p.substring(0,Math.max(E!==null?E.length:0,r));if(a!=E)if(a!=O){O=a;q.emit(h,\"filter\",a)}}var v=a=null;p=z&&p.length==0;var A,Q;A=0;for(Q=t.length;A<Q;++A){var l=t[A];if(d.hasTag(l,\"LI\")){var J=l.firstChild;if(l.orig==null)l.orig=J.firstChild.innerHTML;var K=g(l.orig),U=p||K.match;if(K.suggestion!=J.firstChild.innerHTML)J.firstChild.innerHTML=K.suggestion;if(U){if(l.style.display!=\"\")l.style.display=\"\";if(a==null)a=l;if(A==this.defaultValue)v=l}else if(l.style.display!= \"none\")l.style.display=\"none\";if(l.className!=\"\")l.className=\"\"}}if(a==null)m();else{if(!j()){k(f);u.showPopup(f);b=null}if((c||v)&&(!b||b.style.display==\"none\")){b=v||a;b.parentNode.scrollTop=0;o=b.id}if(b){b.className=\"active\";if(typeof d.theme===\"object\"&&d.theme.type===\"bootstrap\"&&d.theme.version>=4)b.firstChild.className+=\" active\";I(b)}}}};this.editKeyUp=function(a,b){if(i!=null)if(e(a))if(!(!j()&&(b.keyCode==13||b.keyCode==9)))if(b.keyCode==27||b.keyCode==37||b.keyCode==39)m();else if(!(b.keyCode== 40||b.keyCode==38||b.keyCode==34||b.keyCode==33))if(a.value!=G){i=a.id;u.refilter(a.value)}else(a=o?d.getElement(o):null)&&I(a)};h.onclick=s;h.onscroll=function(){if(F){clearTimeout(F);var a=d.getElement(i);a&&a.focus()}};this.delayHide=function(a){F=setTimeout(function(){F=null;if(h&&(a==null||i==a.id))m()},300)}}");
  }

  static WJavaScriptPreamble wtjs2() {
    return new WJavaScriptPreamble(
        JavaScriptScope.WtClassScope,
        JavaScriptObjectType.JavaScriptConstructor,
        "WSuggestionPopupStdMatcher",
        "function(q,h,B,H,r,w,C){function x(c){var e=c.value;c=c.selectionStart?c.selectionStart:e.length;for(var j=B?e.lastIndexOf(B,c-1)+1:0;j<c&&H.indexOf(e.charAt(j))!=-1;)++j;return{start:j,end:c}}this.match=function(c){var e=x(c),j=c.value.substring(e.start,e.end),k;if(w.length==0)if(r.length!=0){k=\"(^|(?:[\";for(c=0;c<r.length;++c){for(e=r.charCodeAt(c).toString(16);e.length<4;)e=\"0\"+e;k+=\"\\\\u\"+e}k+=\"]))\"}else k=\"(^)\";else k=\"(\"+ w+\")\";k+=\"(\"+j.replace(new RegExp(\"([\\\\^\\\\\\\\\\\\][\\\\-.$*+?()|{}])\",\"g\"),\"\\\\$1\")+\")\";k=new RegExp(k,\"gi\");return function(s){if(!s)return j;var y=false;if(j.length){var m=s.replace(k,\"$1\"+q+\"$2\"+h);if(m!=s){y=true;s=m}}return{match:y,suggestion:s}}};this.replace=function(c,e,j){e=x(c);var k=c.value.substring(0,e.start)+j+C;if(e.end<c.value.length)k+=c.value.substring(e.end,c.value.length);c.value=k;if(c.selectionStart){c.selectionStart=e.start+j.length+C.length;c.selectionEnd=c.selectionStart}}}");
  }

  static String instantiateStdMatcher(final WSuggestionPopup.Options options) {
    StringBuilder s = new StringBuilder();
    s.append("new Wt4_6_2.WSuggestionPopupStdMatcher(")
        .append(WWebWidget.jsStringLiteral(options.highlightBeginTag))
        .append(", ")
        .append(WWebWidget.jsStringLiteral(options.highlightEndTag))
        .append(", ");
    if (options.listSeparator != 0) {
      s.append(WWebWidget.jsStringLiteral("" + options.listSeparator));
    } else {
      s.append("null");
    }
    s.append(", ")
        .append(WWebWidget.jsStringLiteral(options.whitespace))
        .append(", ")
        .append(WWebWidget.jsStringLiteral(options.wordSeparators))
        .append(", ")
        .append(WWebWidget.jsStringLiteral(options.wordStartRegexp))
        .append(", ")
        .append(WWebWidget.jsStringLiteral(options.appendReplacedText))
        .append(")");
    return s.toString();
  }
}
