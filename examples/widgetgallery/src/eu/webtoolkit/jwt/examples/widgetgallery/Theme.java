package eu.webtoolkit.jwt.examples.widgetgallery;

import java.util.List;

import eu.webtoolkit.jwt.WBootstrap5Theme;
import eu.webtoolkit.jwt.WLink;
import eu.webtoolkit.jwt.WLinkedCssStyleSheet;

public final class Theme extends WBootstrap5Theme {
	@Override
	public List<WLinkedCssStyleSheet> getStyleSheets() {
		return List.of(new WLinkedCssStyleSheet(new WLink("style/main.css")));
	}
}
