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
 * Standard delegate class for rendering a view item.
 *
 * <p>This class provides the standard implementation for rendering an item (as in a {@link
 * WAbstractItemView}), and renders data provided by the standard data roles (see {@link
 * ItemDataRole}). It also provides default editing support using a line edit.
 *
 * <p>You may provide special editing support for an item by specializing the widget and reimplement
 * {@link WItemDelegate#createEditor(WModelIndex index, EnumSet flags) createEditor()}, {@link
 * WItemDelegate#setModelData(Object editState, WAbstractItemModel model, WModelIndex index)
 * setModelData()}, {@link WItemDelegate#getEditState(WWidget editor, WModelIndex index)
 * getEditState()}, and {@link WItemDelegate#setEditState(WWidget editor, WModelIndex index, Object
 * value) setEditState()}.
 */
public class WItemDelegate extends WAbstractItemDelegate {
  private static Logger logger = LoggerFactory.getLogger(WItemDelegate.class);

  /** Create an item delegate. */
  public WItemDelegate() {
    super();
    this.textFormat_ = "";
  }
  /**
   * Creates or updates a widget that renders an item.
   *
   * <p>The following properties of an item are rendered:
   *
   * <p>
   *
   * <ul>
   *   <li>text using the {@link ItemDataRole#Display} data, with the format specified by {@link
   *       WItemDelegate#setTextFormat(String format) setTextFormat()}
   *   <li>a check box depending on the {@link ItemFlag#UserCheckable} flag and {@link
   *       ItemDataRole#Checked} data
   *   <li>an anchor depending on the value of {@link ItemDataRole#Link}
   *   <li>an icon depending on the value of {@link ItemDataRole#Decoration}
   *   <li>a tooltip depending on the value of {@link ItemDataRole#ToolTip}
   *   <li>a custom style class depending on the value of {@link ItemDataRole#StyleClass}
   * </ul>
   *
   * <p>When the flags indicates {@link ViewItemRenderFlag#Editing}, then {@link
   * WItemDelegate#createEditor(WModelIndex index, EnumSet flags) createEditor()} is called to
   * create a suitable editor for editing the item.
   */
  public WWidget update(
      WWidget widget, final WModelIndex index, EnumSet<ViewItemRenderFlag> flags) {
    boolean editing = widget != null && widget.find("t") == null;
    WItemDelegate.WidgetRef widgetRef = new WItemDelegate.WidgetRef(widget);
    if (flags.contains(ViewItemRenderFlag.Editing)) {
      if (!editing) {
        widgetRef.created = this.createEditor(index, flags);
        widgetRef.w = widgetRef.created;
        WInteractWidget iw = ObjectUtils.cast(widget, WInteractWidget.class);
        if (iw != null) {
          iw.mouseWentDown().preventPropagation();
          iw.clicked().preventPropagation();
        }
      }
    } else {
      if (editing) {
        widgetRef.w = null;
      }
    }
    boolean isNew = false;
    boolean haveCheckBox = (index != null) ? (index.getData(ItemDataRole.Checked) != null) : false;
    boolean haveLink = (index != null) ? (index.getData(ItemDataRole.Link) != null) : false;
    boolean haveIcon = (index != null) ? (index.getData(ItemDataRole.Decoration) != null) : false;
    if (!!EnumUtils.mask(flags, ViewItemRenderFlag.Editing).isEmpty()) {
      if (widgetRef.w != null) {
        if (haveCheckBox != (this.checkBox(widgetRef, index, false) != null)
            || haveLink != (this.anchorWidget(widgetRef, index, false) != null)
            || haveIcon != (this.iconWidget(widgetRef, index, false) != null)) {
          {
            WWidget toRemove = widgetRef.w.removeFromParent();
            if (toRemove != null) toRemove.remove();
          }

          widgetRef.w = null;
        }
      }
      if (!(widgetRef.w != null)) {
        isNew = true;
        widgetRef.created = new IndexText(index);
        IndexText t = (IndexText) widgetRef.created;
        t.setObjectName("t");
        if ((index != null) && !!EnumUtils.mask(index.getFlags(), ItemFlag.XHTMLText).isEmpty()) {
          t.setTextFormat(TextFormat.Plain);
        }
        t.setWordWrap(true);
        widgetRef.w = t;
      }
      if (!(index != null)) {
        if (isNew) {
          return widgetRef.created;
        } else {
          return null;
        }
      }
      Object checkedData = index.getData(ItemDataRole.Checked);
      if ((checkedData != null)) {
        CheckState state =
            checkedData.getClass().equals(Boolean.class)
                ? ((Boolean) checkedData) ? CheckState.Checked : CheckState.Unchecked
                : checkedData.getClass().equals(CheckState.class)
                    ? ((CheckState) checkedData)
                    : CheckState.Unchecked;
        IndexCheckBox icb =
            this.checkBox(
                widgetRef, index, true, true, index.getFlags().contains(ItemFlag.Tristate));
        icb.setCheckState(state);
        icb.setEnabled(index.getFlags().contains(ItemFlag.UserCheckable));
      } else {
        if (!isNew) {
          IndexCheckBox icb = this.checkBox(widgetRef, index, false);
          if (icb != null) {
            {
              WWidget toRemove = icb.removeFromParent();
              if (toRemove != null) toRemove.remove();
            }
          }
        }
      }
      Object linkData = index.getData(ItemDataRole.Link);
      if ((linkData != null)) {
        WLink link = ((WLink) linkData);
        IndexAnchor a = this.anchorWidget(widgetRef, index, true);
        a.setLink(link);
      }
      IndexText t = this.textWidget(widgetRef, index);
      WString label = StringUtils.asString(index.getData(), this.textFormat_);
      if ((label.length() == 0) && haveCheckBox) {
        label = new WString(" ");
      }
      t.setText(label);
      String iconUrl = StringUtils.asString(index.getData(ItemDataRole.Decoration)).toString();
      if (iconUrl.length() != 0) {
        this.iconWidget(widgetRef, index, true).setImageLink(new WLink(iconUrl));
      } else {
        if (!isNew) {
          WImage icw = this.iconWidget(widgetRef, index, false);
          if (icw != null) {
            {
              WWidget toRemove = icw.removeFromParent();
              if (toRemove != null) toRemove.remove();
            }
          }
        }
      }
    }
    if (index.getFlags().contains(ItemFlag.DeferredToolTip)) {
      widgetRef.w.setDeferredToolTip(
          true,
          index.getFlags().contains(ItemFlag.XHTMLText) ? TextFormat.XHTML : TextFormat.Plain);
    } else {
      WString tooltip = StringUtils.asString(index.getData(ItemDataRole.ToolTip));
      if (!(tooltip.length() == 0) || !isNew) {
        widgetRef.w.setToolTip(
            tooltip,
            index.getFlags().contains(ItemFlag.XHTMLText) ? TextFormat.XHTML : TextFormat.Plain);
      }
    }
    String sc = StringUtils.asString(index.getData(ItemDataRole.StyleClass)).toString();
    if (flags.contains(ViewItemRenderFlag.Selected)) {
      sc += " " + WApplication.getInstance().getTheme().getActiveClass();
    }
    if (flags.contains(ViewItemRenderFlag.Editing)) {
      sc += " Wt-delegate-edit";
    }
    widgetRef.w.setStyleClass(sc);
    if (index.getFlags().contains(ItemFlag.DropEnabled)) {
      widgetRef.w.setAttributeValue("drop", new WString("true").toString());
    } else {
      if (widgetRef.w.getAttributeValue("drop").length() != 0) {
        widgetRef.w.setAttributeValue("drop", new WString("f").toString());
      }
    }
    return widgetRef.created;
  }

  public void updateModelIndex(WWidget widget, final WModelIndex index) {
    WItemDelegate.WidgetRef w = new WItemDelegate.WidgetRef(widget);
    if (index.getFlags().contains(ItemFlag.UserCheckable)) {
      IndexCheckBox cb = this.checkBox(w, index, false);
      if (cb != null) {
        cb.setIndex(index);
      }
    }
    if (index.getFlags().contains(ItemFlag.DeferredToolTip)) {
      IndexText text = ObjectUtils.cast(widget, IndexText.class);
      if (text != null) {
        text.setIndex(index);
      }
      IndexAnchor anchor = ObjectUtils.cast(widget, IndexAnchor.class);
      if (anchor != null) {
        anchor.setIndex(index);
      }
      IndexContainerWidget c = ObjectUtils.cast(widget, IndexContainerWidget.class);
      if (c != null) {
        c.setIndex(index);
      }
    }
  }
  /**
   * Sets the text format string.
   *
   * <p>The {@link ItemDataRole#Display} data is converted to a string using {@link
   * StringUtils#asString(Object)}, passing the given format. If the format is an empty string, this
   * corresponds to {@link Object#toString()}.
   *
   * <p>The default value is &quot;&quot;.
   */
  public void setTextFormat(final String format) {
    this.textFormat_ = format;
  }
  /**
   * Returns the text format string.
   *
   * <p>
   *
   * @see WItemDelegate#setTextFormat(String format)
   */
  public String getTextFormat() {
    return this.textFormat_;
  }
  /**
   * Saves the edited data to the model.
   *
   * <p>The default implementation saves the current edit value to the model. You will need to
   * reimplement this method for a custom editor.
   *
   * <p>As an example of how to deal with a specialized editor, consider the default implementation:
   *
   * <pre>{@code
   * public void setModelData(Object editState, WAbstractItemModel model, WModelIndex index) {
   * model.setData(index, editState, ItemDataRole.ItemDataRole::Edit);
   * }
   *
   * }</pre>
   *
   * <p>
   *
   * @see WItemDelegate#createEditor(WModelIndex index, EnumSet flags)
   * @see WItemDelegate#getEditState(WWidget editor, WModelIndex index)
   */
  public void setModelData(
      final Object editState, WAbstractItemModel model, final WModelIndex index) {
    model.setData(index, editState, ItemDataRole.Edit);
  }
  /**
   * Returns the current edit state.
   *
   * <p>The default implementation returns the current text in the line edit. You will need to
   * reimplement this method for a custom editor.
   *
   * <p>As an example of how to deal with a specialized editor, consider the default implementation:
   *
   * <pre>{@code
   * public Object getEditState(WWidget editor) {
   * WContainerWidget w = (WContainerWidget) editor;
   * WLineEdit lineEdit = (WLineEdit) w.getWidget(0);
   * return lineEdit.getText();
   * }
   *
   * }</pre>
   *
   * <p>
   *
   * @see WItemDelegate#createEditor(WModelIndex index, EnumSet flags)
   * @see WItemDelegate#setEditState(WWidget editor, WModelIndex index, Object value)
   * @see WItemDelegate#setModelData(Object editState, WAbstractItemModel model, WModelIndex index)
   */
  public Object getEditState(WWidget editor, final WModelIndex index) {
    IndexContainerWidget w = ObjectUtils.cast(editor, IndexContainerWidget.class);
    WLineEdit lineEdit = ObjectUtils.cast(w.getWidget(0), WLineEdit.class);
    return lineEdit.getText();
  }
  /**
   * Sets the editor data from the editor state.
   *
   * <p>The default implementation resets the text in the line edit. You will need to reimplement
   * this method if for a custom editor.
   *
   * <p>As an example of how to deal with a specialized editor, consider the default implementation:
   *
   * <pre>{@code
   * public void setEditState(WWidget editor, WModelIndex index, Object value) {
   * WContainerWidget w = (WContainerWidget) editor;
   * WLineEdit lineEdit = (WLineEdit) w.getWidget(0);
   * lineEdit.setText((String) value);
   * }
   *
   * }</pre>
   *
   * <p>
   *
   * @see WItemDelegate#createEditor(WModelIndex index, EnumSet flags)
   */
  public void setEditState(WWidget editor, final WModelIndex index, final Object value) {
    IndexContainerWidget w = ObjectUtils.cast(editor, IndexContainerWidget.class);
    WLineEdit lineEdit = ObjectUtils.cast(w.getWidget(0), WLineEdit.class);
    lineEdit.setText(((String) value));
  }
  /**
   * Creates an editor for a data item.
   *
   * <p>The default implementation returns a {@link WLineEdit} which edits the item&apos;s {@link
   * ItemDataRole#Edit} value.
   *
   * <p>You may reimplement this method to provide a suitable editor, or to attach a custom
   * validator. In that case, you will probably also want to reimplement {@link
   * WItemDelegate#getEditState(WWidget editor, WModelIndex index) getEditState()}, {@link
   * WItemDelegate#setEditState(WWidget editor, WModelIndex index, Object value) setEditState()},
   * and {@link WItemDelegate#setModelData(Object editState, WAbstractItemModel model, WModelIndex
   * index) setModelData()}.
   *
   * <p>The editor should not keep a reference to the model index (it does not need to since {@link
   * WItemDelegate#setModelData(Object editState, WAbstractItemModel model, WModelIndex index)
   * setModelData()} will provide the proper model index to save the data to the model). Otherwise,
   * because model indexes may shift because of row or column insertions, you should reimplement
   * {@link WItemDelegate#updateModelIndex(WWidget widget, WModelIndex index) updateModelIndex()}.
   *
   * <p>As an example of how to provide a specialized editor, consider the default implementation,
   * which returns a {@link WLineEdit}:
   *
   * <pre>{@code
   * protected WWidget createEditor(WModelIndex index, EnumSet&lt;ViewItemRenderFlag&gt; flags) {
   * final WContainerWidget result = new WContainerWidget();
   * result.setSelectable(true);
   * WLineEdit lineEdit = new WLineEdit();
   * lineEdit.setText(StringUtils.asString(index.getData(ItemDataRole.ItemDataRole::Edit), this.textFormat_).toString());
   * lineEdit.enterPressed().addListener(this, new Signal.Listener() {
   * public void trigger() {
   * WItemDelegate.this.closeEditor().trigger(result, true);
   * }
   * });
   * lineEdit.escapePressed().addListener(this, new Signal.Listener() {
   * public void trigger() {
   * WItemDelegate.this.closeEditor().trigger(result, false);
   * }
   * });
   *
   * if (flags.contains(ViewItemRenderFlag.ViewItemRenderFlag::Focused))
   * lineEdit.setFocus();
   *
   * result.setLayout(new WHBoxLayout());
   * result.getLayout().setContentsMargins(1, 1, 1, 1);
   * result.getLayout().addWidget(lineEdit);
   * return result;
   * }
   *
   * }</pre>
   */
  protected WWidget createEditor(final WModelIndex index, EnumSet<ViewItemRenderFlag> flags) {
    IndexContainerWidget result = new IndexContainerWidget(index);
    result.setSelectable(true);
    WLineEdit lineEdit = new WLineEdit();
    lineEdit.setText(
        StringUtils.asString(index.getData(ItemDataRole.Edit), this.textFormat_).toString());
    final IndexContainerWidget resultPtr = result;
    lineEdit
        .enterPressed()
        .addListener(
            this,
            () -> {
              WItemDelegate.this.doCloseEditor(resultPtr, true);
            });
    lineEdit
        .escapePressed()
        .addListener(
            this,
            () -> {
              WItemDelegate.this.doCloseEditor(resultPtr, false);
            });
    lineEdit.escapePressed().preventPropagation();
    if (flags.contains(ViewItemRenderFlag.Focused)) {
      lineEdit.setFocus(true);
    }
    lineEdit.resize(
        new WLength(100, LengthUnit.Percentage), new WLength(100, LengthUnit.Percentage));
    result.addWidget(lineEdit);
    return result;
  }
  /**
   * Creates an editor for a data item.
   *
   * <p>Returns {@link #createEditor(WModelIndex index, EnumSet flags) createEditor(index,
   * EnumSet.of(flag, flags))}
   */
  protected final WWidget createEditor(
      final WModelIndex index, ViewItemRenderFlag flag, ViewItemRenderFlag... flags) {
    return createEditor(index, EnumSet.of(flag, flags));
  }

  private String textFormat_;

  static class WidgetRef {
    private static Logger logger = LoggerFactory.getLogger(WidgetRef.class);

    public WWidget created;
    public WWidget w;

    public WidgetRef(WWidget widget) {
      this.created = null;
      this.w = widget;
    }
  }

  private IndexCheckBox checkBox(
      final WItemDelegate.WidgetRef w,
      final WModelIndex index,
      boolean autoCreate,
      boolean update,
      boolean triState) {
    IndexCheckBox checkBox = ObjectUtils.cast(w.w.find("c"), IndexCheckBox.class);
    if (!(checkBox != null)) {
      if (autoCreate) {
        IndexCheckBox newBox = checkBox = new IndexCheckBox(index);
        checkBox.setObjectName("c");
        checkBox.clicked().preventPropagation();
        IndexContainerWidget wc = ObjectUtils.cast(w.w.find("o"), IndexContainerWidget.class);
        if (!(wc != null)) {
          WWidget oldW = null;
          if (w.created != null) {
            oldW = w.created;
          }
          w.created = new IndexContainerWidget(index);
          wc = (IndexContainerWidget) w.created;
          wc.setObjectName("o");
          w.w.setInline(true);
          w.w.setStyleClass(WString.Empty.toString());
          if (w.w.getParent() != null) {
            assert !(oldW != null);
            oldW = w.w.removeFromParent();
          }
          wc.addWidget(oldW);
          w.w = wc;
        }
        wc.insertWidget(0, newBox);
        final IndexCheckBox cb = checkBox;
        checkBox
            .changed()
            .addListener(
                this,
                () -> {
                  WItemDelegate.this.onCheckedChange(cb);
                });
      } else {
        return null;
      }
    }
    if (update) {
      checkBox.setTristate(triState);
    }
    return checkBox;
  }

  private final IndexCheckBox checkBox(
      final WItemDelegate.WidgetRef w, final WModelIndex index, boolean autoCreate) {
    return checkBox(w, index, autoCreate, false, false);
  }

  private final IndexCheckBox checkBox(
      final WItemDelegate.WidgetRef w,
      final WModelIndex index,
      boolean autoCreate,
      boolean update) {
    return checkBox(w, index, autoCreate, update, false);
  }

  private IndexText textWidget(final WItemDelegate.WidgetRef w, final WModelIndex index) {
    return ObjectUtils.cast(w.w.find("t"), IndexText.class);
  }

  private WImage iconWidget(
      final WItemDelegate.WidgetRef w, final WModelIndex index, boolean autoCreate) {
    WImage image = ObjectUtils.cast(w.w.find("i"), WImage.class);
    if (image != null || !autoCreate) {
      return image;
    }
    WContainerWidget wc = ObjectUtils.cast(w.w.find("a"), IndexAnchor.class);
    if (!(wc != null)) {
      wc = ObjectUtils.cast(w.w.find("o"), IndexContainerWidget.class);
    }
    if (!(wc != null)) {
      WWidget newWc = new IndexContainerWidget(index);
      wc = (IndexContainerWidget) newWc;
      wc.setObjectName("o");
      wc.addWidget(w.created != null ? w.created : w.w.removeFromParent());
      w.created = newWc;
      w.w = wc;
    }
    WWidget newImage = new WImage();
    image = (WImage) newImage;
    image.mouseWentDown().preventDefaultAction(true);
    image.setObjectName("i");
    image.setStyleClass("icon");
    wc.insertWidget(wc.getCount() - 1, newImage);
    if (WApplication.getInstance().getEnvironment().agentIsIE()) {
      WImage inv =
          new WImage(
              new WLink(WApplication.getInstance().getOnePixelGifUrl()), (WContainerWidget) null);
      inv.setStyleClass("rh w0 icon");
      inv.resize(new WLength(0), WLength.Auto);
      wc.insertWidget(wc.getCount() - 1, inv);
    }
    return image;
  }

  private final WImage iconWidget(final WItemDelegate.WidgetRef w, final WModelIndex index) {
    return iconWidget(w, index, false);
  }

  private IndexAnchor anchorWidget(
      final WItemDelegate.WidgetRef w, final WModelIndex index, boolean autoCreate) {
    IndexAnchor anchor = ObjectUtils.cast(w.w.find("a"), IndexAnchor.class);
    if (anchor != null || !autoCreate) {
      return anchor;
    }
    WWidget newAnchor = new IndexAnchor(index);
    anchor = (IndexAnchor) newAnchor;
    anchor.setObjectName("a");
    IndexContainerWidget wc = ObjectUtils.cast(w.w.find("o"), IndexContainerWidget.class);
    if (wc != null) {
      int firstToMove = 0;
      WCheckBox cb = ObjectUtils.cast(wc.getWidget(0), WCheckBox.class);
      if (cb != null) {
        firstToMove = 1;
      }
      wc.insertWidget(firstToMove, newAnchor);
      while (wc.getCount() > firstToMove + 1) {
        WWidget c = wc.getWidget(firstToMove + 1);
        WWidget uc = wc.removeWidget(c);
        anchor.addWidget(uc);
      }
    } else {
      anchor.addWidget(w.created != null ? w.created : w.w.removeFromParent());
      w.created = newAnchor;
      w.w = anchor;
    }
    return anchor;
  }

  private final IndexAnchor anchorWidget(final WItemDelegate.WidgetRef w, final WModelIndex index) {
    return anchorWidget(w, index, false);
  }

  private void onCheckedChange(IndexCheckBox cb) {
    WAbstractItemModel model = cb.getIndex().getModel();
    if (cb.isTristate()) {
      model.setData(cb.getIndex(), cb.getCheckState(), ItemDataRole.Checked);
    } else {
      model.setData(cb.getIndex(), cb.isChecked(), ItemDataRole.Checked);
    }
  }

  private void doCloseEditor(WWidget editor, boolean save) {
    this.closeEditor().trigger(editor, save);
  }
}
