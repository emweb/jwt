/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.EnumSet;
import eu.webtoolkit.jwt.utils.EnumUtils;

/**
 * Standard delegate class for rendering a view item.
 * <p>
 * 
 * This class provides the standard implementation for rendering an item in a
 * {@link WTreeView}.
 */
public class WItemDelegate extends WAbstractItemDelegate {
	/**
	 * Constructor.
	 */
	public WItemDelegate(WObject parent) {
		super(parent);
		this.textFormat_ = "";
		this.checkedChangeMapper_ = new WSignalMapper1<ItemCheckBox>(this);
		this.checkedChangeMapper_.mapped().addListener(this,
				new Signal1.Listener<ItemCheckBox>() {
					public void trigger(ItemCheckBox e1) {
						WItemDelegate.this.onCheckedChange(e1);
					}
				});
	}

	/**
	 * Constructor.
	 * <p>
	 * Calls {@link #WItemDelegate(WObject parent) this((WObject)null)}
	 */
	public WItemDelegate() {
		this((WObject) null);
	}

	public WWidget update(WWidget widget, WModelIndex index,
			EnumSet<ViewItemRenderFlag> flags) {
		WItemDelegate.WidgetRef widgetRef = new WItemDelegate.WidgetRef(widget);
		boolean isNew = false;
		if (!(widgetRef.w != null)) {
			isNew = true;
			WText t = new WText();
			if ((index != null)
					&& !!EnumUtils.mask(index.getFlags(),
							ItemFlag.ItemIsXHTMLText).isEmpty()) {
				t.setTextFormat(TextFormat.PlainText);
			}
			t.setWordWrap(true);
			widgetRef.w = t;
		}
		boolean haveCheckBox = false;
		if (!EnumUtils.mask(index.getFlags(), ItemFlag.ItemIsUserCheckable)
				.isEmpty()) {
			Object checkedData = index.getData(ItemDataRole.CheckStateRole);
			CheckState state = (checkedData == null) ? CheckState.Unchecked
					: checkedData.getClass().equals(Boolean.class) ? (Boolean) checkedData ? CheckState.Checked
							: CheckState.Unchecked
							: checkedData.getClass().equals(CheckState.class) ? (CheckState) checkedData
									: CheckState.Unchecked;
			this.checkBox(
					widgetRef,
					index,
					true,
					!EnumUtils.mask(index.getFlags(), ItemFlag.ItemIsTristate)
							.isEmpty()).setCheckState(state);
			haveCheckBox = true;
		} else {
			if (!isNew) {
				if (this.checkBox(widgetRef, index, false) != null)
					this.checkBox(widgetRef, index, false).remove();
			}
		}
		String internalPath = StringUtils.asString(
				index.getData(ItemDataRole.InternalPathRole)).toString();
		String url = StringUtils.asString(index.getData(ItemDataRole.UrlRole))
				.toString();
		if (internalPath.length() != 0 || url.length() != 0) {
			WAnchor a = this.anchorWidget(widgetRef);
			if (internalPath.length() != 0) {
				a.setRefInternalPath(internalPath);
			} else {
				a.setRef(url);
			}
		}
		WText t = this.textWidget(widgetRef);
		WString label = StringUtils.asString(index.getData(), this.textFormat_);
		if ((label.length() == 0) && haveCheckBox) {
			label = new WString(" ");
		}
		t.setText(label);
		String iconUrl = StringUtils.asString(
				index.getData(ItemDataRole.DecorationRole)).toString();
		if (iconUrl.length() != 0) {
			this.iconWidget(widgetRef, true).setImageRef(iconUrl);
		} else {
			if (!isNew) {
				if (this.iconWidget(widgetRef, false) != null)
					this.iconWidget(widgetRef, false).remove();
			}
		}
		WString tooltip = StringUtils.asString(index
				.getData(ItemDataRole.ToolTipRole));
		if (!(tooltip.length() == 0) || !isNew) {
			widgetRef.w.setToolTip(tooltip);
		}
		if (index.getColumn() != 0) {
			String sc = StringUtils.asString(
					index.getData(ItemDataRole.StyleClassRole)).toString();
			if (!EnumUtils.mask(flags, ViewItemRenderFlag.RenderSelected)
					.isEmpty()) {
				sc += " Wt-selected";
			}
			widgetRef.w.setStyleClass(sc);
		} else {
			widgetRef.w.setStyleClass(WString.Empty.toString());
		}
		if (!EnumUtils.mask(index.getFlags(), ItemFlag.ItemIsDropEnabled)
				.isEmpty()) {
			widgetRef.w.setAttributeValue("drop", new WString("true")
					.toString());
		} else {
			if (widgetRef.w.getAttributeValue("drop").length() != 0) {
				widgetRef.w.setAttributeValue("drop", new WString("f")
						.toString());
			}
		}
		return widgetRef.w;
	}

	public void updateModelIndex(WWidget widget, WModelIndex index) {
		WItemDelegate.WidgetRef w = new WItemDelegate.WidgetRef(widget);
		if (!EnumUtils.mask(index.getFlags(), ItemFlag.ItemIsUserCheckable)
				.isEmpty()) {
			ItemCheckBox cb = this.checkBox(w, index, false, false);
			if (cb != null) {
				cb.setIndex(index);
			}
		}
	}

	/**
	 * Set the text format string.
	 * <p>
	 * The DisplayRole data is converted to a string using
	 * {@link StringUtils#asString(Object)} by passing the given format.
	 * <p>
	 * The default value is &quot;&quot;.
	 */
	public void setTextFormat(String format) {
		this.textFormat_ = format;
	}

	/**
	 * Returns the text format string.
	 * <p>
	 * 
	 * @see WItemDelegate#setTextFormat(String format)
	 */
	public String getTextFormat() {
		return this.textFormat_;
	}

	private String textFormat_;
	private WSignalMapper1<ItemCheckBox> checkedChangeMapper_;

	static class WidgetRef {
		public WWidget w;

		public WidgetRef(WWidget widget) {
			this.w = widget;
		}
	}

	private ItemCheckBox checkBox(WItemDelegate.WidgetRef w, WModelIndex index,
			boolean autoCreate, boolean triState) {
		WText t = ((w.w) instanceof WText ? (WText) (w.w) : null);
		WAnchor a = ((w.w) instanceof WAnchor ? (WAnchor) (w.w) : null);
		WContainerWidget wc = ((w.w) instanceof WContainerWidget ? (WContainerWidget) (w.w)
				: null);
		if (t != null || a != null) {
			if (autoCreate) {
				wc = new WContainerWidget();
				wc.addWidget(w.w);
				w.w = wc;
			} else {
				return null;
			}
		}
		ItemCheckBox cb = ((wc.getWidget(0)) instanceof ItemCheckBox ? (ItemCheckBox) (wc
				.getWidget(0))
				: null);
		if (!(cb != null) && autoCreate) {
			cb = new ItemCheckBox(index);
			wc.insertWidget(0, cb);
			this.checkedChangeMapper_.mapConnect(cb.changed(), cb);
		}
		if (cb != null) {
			cb.setTristate(triState);
		}
		return cb;
	}

	private final ItemCheckBox checkBox(WItemDelegate.WidgetRef w,
			WModelIndex index, boolean autoCreate) {
		return checkBox(w, index, autoCreate, false);
	}

	private WText textWidget(WItemDelegate.WidgetRef w) {
		WText result = ((w.w) instanceof WText ? (WText) (w.w) : null);
		if (result != null) {
			return result;
		}
		WContainerWidget wc = ((w.w) instanceof WContainerWidget ? (WContainerWidget) (w.w)
				: null);
		result = ((wc.getWidget(wc.getCount() - 1)) instanceof WText ? (WText) (wc
				.getWidget(wc.getCount() - 1))
				: null);
		if (result != null) {
			return result;
		}
		wc = ((wc.getWidget(wc.getCount() - 1)) instanceof WContainerWidget ? (WContainerWidget) (wc
				.getWidget(wc.getCount() - 1))
				: null);
		return ((wc.getWidget(wc.getCount() - 1)) instanceof WText ? (WText) (wc
				.getWidget(wc.getCount() - 1))
				: null);
	}

	private WImage iconWidget(WItemDelegate.WidgetRef w, boolean autoCreate) {
		WText result = ((w.w) instanceof WText ? (WText) (w.w) : null);
		if (result != null) {
			if (autoCreate) {
				WContainerWidget wc = new WContainerWidget();
				WImage image = new WImage();
				image.setStyleClass("icon");
				wc.addWidget(image);
				if (WApplication.getInstance().getEnvironment().agentIsIE()) {
					WImage inv = new WImage(WApplication.getInstance()
							.getOnePixelGifUrl());
					inv.setStyleClass("rh w0 icon");
					inv.resize(new WLength(0), WLength.Auto);
					wc.addWidget(inv);
				}
				wc.addWidget(w.w);
				w.w = wc;
				return image;
			} else {
				return null;
			}
		}
		WContainerWidget wc = ((w.w) instanceof WContainerWidget ? (WContainerWidget) (w.w)
				: null);
		for (int i = 0; i < wc.getCount(); ++i) {
			WImage image = ((wc.getWidget(i)) instanceof WImage ? (WImage) (wc
					.getWidget(i)) : null);
			if (image != null) {
				return image;
			}
			WAnchor anchor = ((wc.getWidget(i)) instanceof WAnchor ? (WAnchor) (wc
					.getWidget(i))
					: null);
			if (anchor != null) {
				wc = anchor;
				i = -1;
			}
		}
		if (autoCreate) {
			WImage image = new WImage();
			image.setStyleClass("icon");
			wc.insertWidget(wc.getCount() - 1, image);
			if (WApplication.getInstance().getEnvironment().agentIsIE()) {
				WImage inv = new WImage(WApplication.getInstance()
						.getOnePixelGifUrl());
				inv.setStyleClass("rh w0 icon");
				wc.insertWidget(wc.getCount() - 1, inv);
			}
			return image;
		} else {
			return null;
		}
	}

	private final WImage iconWidget(WItemDelegate.WidgetRef w) {
		return iconWidget(w, false);
	}

	private WAnchor anchorWidget(WItemDelegate.WidgetRef w) {
		WAnchor result = ((w.w) instanceof WAnchor ? (WAnchor) (w.w) : null);
		if (result != null) {
			return result;
		} else {
			WText text = ((w.w) instanceof WText ? (WText) (w.w) : null);
			if (text != null) {
				WAnchor a = new WAnchor();
				a.addWidget(w.w);
				w.w = a;
				return a;
			}
			WContainerWidget wc = ((w.w) instanceof WContainerWidget ? (WContainerWidget) (w.w)
					: null);
			WWidget lw = wc.getWidget(wc.getCount() - 1);
			WAnchor a = ((lw) instanceof WAnchor ? (WAnchor) (lw) : null);
			if (a != null) {
				return a;
			}
			a = new WAnchor();
			int firstToMove = 0;
			WCheckBox cb = ((wc.getWidget(0)) instanceof WCheckBox ? (WCheckBox) (wc
					.getWidget(0))
					: null);
			if (cb != null) {
				firstToMove = 1;
			}
			wc.insertWidget(firstToMove, a);
			while (wc.getCount() > firstToMove + 1) {
				WWidget c = wc.getWidget(firstToMove + 1);
				wc.removeWidget(c);
				a.addWidget(c);
			}
			return a;
		}
	}

	private void onCheckedChange(ItemCheckBox cb) {
		WAbstractItemModel model = cb.getIndex().getModel();
		if (cb.isTristate()) {
			model.setData(cb.getIndex(), cb.getCheckState(),
					ItemDataRole.CheckStateRole);
		} else {
			model.setData(cb.getIndex(), cb.isChecked(),
					ItemDataRole.CheckStateRole);
		}
	}
}
