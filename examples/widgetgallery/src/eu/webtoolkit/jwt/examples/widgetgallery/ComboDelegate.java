/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.widgetgallery;

import eu.webtoolkit.jwt.*;
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

class ComboDelegate extends WItemDelegate {
  private static Logger logger = LoggerFactory.getLogger(ComboDelegate.class);

  public ComboDelegate(WAbstractItemModel items) {
    super();
    this.items_ = items;
  }

  public void setModelData(
      final Object editState, WAbstractItemModel model, final WModelIndex index) {
    int stringIdx = (int) StringUtils.asNumber(editState);
    model.setData(index, stringIdx, ItemDataRole.User);
    model.setData(index, this.items_.getData(stringIdx, 0), ItemDataRole.Display);
  }

  public Object getEditState(WWidget editor, final WModelIndex index) {
    WComboBox combo =
        ObjectUtils.cast(
            (ObjectUtils.cast(editor, WContainerWidget.class)).getWidget(0), WComboBox.class);
    return combo.getCurrentIndex();
  }

  public void setEditState(WWidget editor, final WModelIndex index, final Object value) {
    WComboBox combo =
        ObjectUtils.cast(
            (ObjectUtils.cast(editor, WContainerWidget.class)).getWidget(0), WComboBox.class);
    combo.setCurrentIndex((int) StringUtils.asNumber(value));
  }

  protected WWidget createEditor(final WModelIndex index, EnumSet<ViewItemRenderFlag> flags) {
    WContainerWidget container = new WContainerWidget();
    WComboBox combo = new WComboBox((WContainerWidget) container);
    combo.setModel(this.items_);
    combo.setCurrentIndex((int) StringUtils.asNumber(index.getData(ItemDataRole.User)));
    combo
        .changed()
        .addListener(
            this,
            () -> {
              ComboDelegate.this.doCloseEditor(container, true);
            });
    combo
        .enterPressed()
        .addListener(
            this,
            () -> {
              ComboDelegate.this.doCloseEditor(container, true);
            });
    combo
        .escapePressed()
        .addListener(
            this,
            () -> {
              ComboDelegate.this.doCloseEditor(container, false);
            });
    return container;
  }

  private WAbstractItemModel items_;

  private void doCloseEditor(WWidget editor, boolean save) {
    this.closeEditor().trigger(editor, save);
  }
}
