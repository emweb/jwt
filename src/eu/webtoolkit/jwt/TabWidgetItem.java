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
		WContainerWidget c = new WContainerWidget();
		c.setInline(true);
		c.addWidget(super.createItemWidget());
		return c;
	}

	protected void updateItemWidget(WWidget itemWidget) {
		WContainerWidget c = ((itemWidget) instanceof WContainerWidget ? (WContainerWidget) (itemWidget)
				: null);
		WAnchor a = ((c.getChildren().get(0)) instanceof WAnchor ? (WAnchor) (c
				.getChildren().get(0)) : null);
		super.updateItemWidget(a);
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
