/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.widgetgallery;

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

class ComboDelegate extends WItemDelegate {
	private static Logger logger = LoggerFactory.getLogger(ComboDelegate.class);

	public ComboDelegate(WAbstractItemModel items) {
		super();
		this.items_ = items;
	}

	public void setModelData(final Object editState, WAbstractItemModel model,
			final WModelIndex index) {
		int stringIdx = (int) StringUtils.asNumber(editState);
		model.setData(index, stringIdx, ItemDataRole.UserRole);
		model.setData(index, this.items_.getData(stringIdx, 0),
				ItemDataRole.DisplayRole);
	}

	public Object getEditState(WWidget editor) {
		WComboBox combo = (((((editor) instanceof WContainerWidget ? (WContainerWidget) (editor)
				: null)).getWidget(0)) instanceof WComboBox ? (WComboBox) ((((editor) instanceof WContainerWidget ? (WContainerWidget) (editor)
				: null)).getWidget(0))
				: null);
		return combo.getCurrentIndex();
	}

	public void setEditState(WWidget editor, final Object value) {
		WComboBox combo = (((((editor) instanceof WContainerWidget ? (WContainerWidget) (editor)
				: null)).getWidget(0)) instanceof WComboBox ? (WComboBox) ((((editor) instanceof WContainerWidget ? (WContainerWidget) (editor)
				: null)).getWidget(0))
				: null);
		combo.setCurrentIndex((int) StringUtils.asNumber(value));
	}

	protected WWidget createEditor(final WModelIndex index,
			EnumSet<ViewItemRenderFlag> flags) {
		final WContainerWidget container = new WContainerWidget();
		WComboBox combo = new WComboBox(container);
		combo.setModel(this.items_);
		combo.setCurrentIndex((int) StringUtils.asNumber(index
				.getData(ItemDataRole.UserRole)));
		combo.changed().addListener(this, new Signal.Listener() {
			public void trigger() {
				ComboDelegate.this.doCloseEditor(container, true);
			}
		});
		combo.enterPressed().addListener(this, new Signal.Listener() {
			public void trigger() {
				ComboDelegate.this.doCloseEditor(container, true);
			}
		});
		combo.escapePressed().addListener(this, new Signal.Listener() {
			public void trigger() {
				ComboDelegate.this.doCloseEditor(container, false);
			}
		});
		return container;
	}

	private WAbstractItemModel items_;

	private void doCloseEditor(WWidget editor, boolean save) {
		this.closeEditor().trigger(editor, save);
	}
}
