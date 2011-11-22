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

class TabWidgetItem extends WMenuItem {
	private static Logger logger = LoggerFactory.getLogger(TabWidgetItem.class);

	public TabWidgetItem(CharSequence text, WWidget contents,
			WMenuItem.LoadPolicy loadPolicy) {
		super(text, contents, loadPolicy);
	}

	protected WWidget createItemWidget() {
		if (!this.isCloseable()) {
			WContainerWidget c = new WContainerWidget();
			c.setInline(true);
			c.addWidget(super.createItemWidget());
			return c;
		} else {
			return super.createItemWidget();
		}
	}

	protected void updateItemWidget(WWidget itemWidget) {
		if (!this.isCloseable()) {
			WContainerWidget c = ((itemWidget) instanceof WContainerWidget ? (WContainerWidget) (itemWidget)
					: null);
			WWidget label = null;
			if (!this.isDisabled()) {
				label = ((c.getChildren().get(0)) instanceof WAnchor ? (WAnchor) (c
						.getChildren().get(0))
						: null);
			} else {
				label = ((c.getChildren().get(0)) instanceof WText ? (WText) (c
						.getChildren().get(0)) : null);
			}
			super.updateItemWidget(label);
		} else {
			super.updateItemWidget(itemWidget);
		}
	}

	protected AbstractSignal activateSignal() {
		WContainerWidget c = ((this.getItemWidget()) instanceof WContainerWidget ? (WContainerWidget) (this
				.getItemWidget())
				: null);
		return (((c.getChildren().get(0)) instanceof WInteractWidget ? (WInteractWidget) (c
				.getChildren().get(0))
				: null)).clicked();
	}
}
