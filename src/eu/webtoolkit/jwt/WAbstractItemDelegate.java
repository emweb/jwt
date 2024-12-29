/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import eu.webtoolkit.jwt.auth.*;
import eu.webtoolkit.jwt.auth.mfa.*;
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
 * Abstract delegate class for rendering an item in an item view.
 *
 * <p>Rendering of an item in a {@link WAbstractItemView} is delegated to an implementation of this
 * delegate class. The default implementation used by JWt&apos;s item views is {@link
 * WItemDelegate}. To provide specialized rendering support, you can reimplement this class (or
 * specialize {@link WItemDelegate}).
 *
 * <p>As a delegate is used for rendering multiple items, the class should not keep state about one
 * specific item.
 *
 * <p>A delegate may provide editing support by instantiating an editor when {@link
 * WAbstractItemDelegate#update(WWidget widget, WModelIndex index, EnumSet flags) update()} is
 * called with the {@link ViewItemRenderFlag#Editing} flag. In that case, you will also need to
 * implement {@link WAbstractItemDelegate#getEditState(WWidget widget, WModelIndex anon2)
 * getEditState()} and {@link WAbstractItemDelegate#setEditState(WWidget widget, WModelIndex anon2,
 * Object value) setEditState()} to support virtual scrolling and {@link
 * WAbstractItemDelegate#setModelData(Object editState, WAbstractItemModel model, WModelIndex anon3)
 * setModelData()} to save the edited value to the model. For an example, see the {@link
 * WItemDelegate}.
 *
 * <p>
 *
 * @see WAbstractItemView#setItemDelegateForColumn(int column, WAbstractItemDelegate delegate)
 */
public abstract class WAbstractItemDelegate extends WObject {
  private static Logger logger = LoggerFactory.getLogger(WAbstractItemDelegate.class);

  /** Constructor. */
  public WAbstractItemDelegate() {
    super();
    this.closeEditor_ = new Signal2<WWidget, Boolean>();
  }
  /**
   * Creates or updates a widget that renders an item.
   *
   * <p>The item is specified by its model <code>index</code>, which also indicates the model. If an
   * existing widget already renders the item, but needs to be updated, it is passed as the <code>
   * widget</code> parameter.
   *
   * <p>When <code>widget</code> is <code>null</code>, a new widget needs to be created and
   * returned.
   *
   * <p>If you want to replace the <code>widget</code> with a new one, return the new widget. The
   * old <code>widget</code> will be removed. Return <code>null</code> if you do not want to replace
   * the <code>widget</code>.
   *
   * <p>You can remove the <code>widget</code> from its parent for reuse with {@link
   * WWidget#removeFromParent()}.
   *
   * <p>The returned widget should be a widget that responds properly to be given a height, width
   * and style class. In practice, that means it cannot have a border or margin, and thus cannot be
   * a {@link WFormWidget} since those widgets typically have built-in borders and margins. If you
   * want to return a form widget (for editing the item), you should wrap it in a container widget.
   *
   * <p>The <code>flags</code> parameter indicates options for rendering the item.
   */
  public abstract WWidget update(
      WWidget widget, final WModelIndex index, EnumSet<ViewItemRenderFlag> flags);
  /**
   * Creates or updates a widget that renders an item.
   *
   * <p>Returns {@link #update(WWidget widget, WModelIndex index, EnumSet flags) update(widget,
   * index, EnumSet.of(flag, flags))}
   */
  public final WWidget update(
      WWidget widget,
      final WModelIndex index,
      ViewItemRenderFlag flag,
      ViewItemRenderFlag... flags) {
    return update(widget, index, EnumSet.of(flag, flags));
  }
  /**
   * Updates the model index of a widget.
   *
   * <p>This method is invoked by the view when due to row/column insertions or removals, the index
   * has shifted.
   *
   * <p>You should reimplement this method only if you are storing the model index in the <code>
   * widget</code>, to update the stored model index.
   *
   * <p>The default implementation does nothing.
   */
  public void updateModelIndex(WWidget widget, final WModelIndex anon2) {}
  /**
   * Returns the current edit state.
   *
   * <p>Because a View may support virtual scrolling in combination with editing, it may happen that
   * the view decides to delete the editor widget while the user is editing. To allow to reconstruct
   * the editor in its original state, the View will therefore ask for the editor to serialize its
   * state in a boost::any.
   *
   * <p>When the view decides to close an editor and save its value back to the model, he will first
   * call {@link WAbstractItemDelegate#getEditState(WWidget widget, WModelIndex anon2)
   * getEditState()} and then {@link WAbstractItemDelegate#setModelData(Object editState,
   * WAbstractItemModel model, WModelIndex anon3) setModelData()}.
   *
   * <p>The default implementation assumes a read-only delegate, and returns a boost::any().
   *
   * <p>
   *
   * @see WAbstractItemDelegate#setEditState(WWidget widget, WModelIndex anon2, Object value)
   * @see WAbstractItemDelegate#setModelData(Object editState, WAbstractItemModel model, WModelIndex
   *     anon3)
   */
  public Object getEditState(WWidget widget, final WModelIndex anon2) {
    return null;
  }
  /**
   * Sets the editor data from the editor state.
   *
   * <p>When the View scrolls back into view an item that was being edited, he will use {@link
   * WAbstractItemDelegate#setEditState(WWidget widget, WModelIndex anon2, Object value)
   * setEditState()} to allow the editor to restore its current editor state.
   *
   * <p>The default implementation assumes a read-only delegate and does nothing.
   *
   * <p>
   *
   * @see WAbstractItemDelegate#getEditState(WWidget widget, WModelIndex anon2)
   */
  public void setEditState(WWidget widget, final WModelIndex anon2, final Object value) {}
  /**
   * Returns whether the edited value is valid.
   *
   * <p>The default implementation does nothing and returns Valid.
   *
   * <p>
   *
   * @see WValidator#validate(String input)
   */
  public ValidationState validate(final WModelIndex anon1, final Object editState) {
    return ValidationState.Valid;
  }
  /**
   * Saves the edited data to the model.
   *
   * <p>The View will use this method to save the edited value to the model. The <code>editState
   * </code> is first fetched from the editor using {@link
   * WAbstractItemDelegate#getEditState(WWidget widget, WModelIndex anon2) getEditState()}.
   *
   * <p>The default implementation assumes a read-only delegate does nothing.
   */
  public void setModelData(
      final Object editState, WAbstractItemModel model, final WModelIndex anon3) {}
  /**
   * Signal which indicates that an editor needs to be closed.
   *
   * <p>The delegate should emit this signal when it decides for itself that it should be closed
   * (e.g. because the user confirmed the edited value or cancelled the editing). The View will then
   * rerender the item if needed.
   *
   * <p>The second boolean argument passed to the signal is a flag which indicates whether the
   * editor feels that the value should be saved or cancelled.
   *
   * <p>
   *
   * @see WAbstractItemView#closeEditor(WModelIndex index, boolean saveData)
   */
  public Signal2<WWidget, Boolean> closeEditor() {
    return this.closeEditor_;
  }

  private Signal2<WWidget, Boolean> closeEditor_;
}
