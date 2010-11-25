/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

class TabWidgetItem extends WMenuItem {
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
