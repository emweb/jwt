package eu.webtoolkit.jwt.examples.widgetgallery;

import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WWidget;

interface WidgetCreator {
	public WWidget create();
}
class DeferredWidget extends WContainerWidget {
	private WidgetCreator creator;
	public DeferredWidget(WidgetCreator creator) {
		this.creator = creator;
	}
	
	@Override
	public void load() {
		WWidget widget = creator.create();
		widget.addStyleClass("contents");
		addWidget(widget);
		super.load();
	}
	
	public static DeferredWidget deferCreate(WidgetCreator creator) {
		return new DeferredWidget(creator);
	}
}
