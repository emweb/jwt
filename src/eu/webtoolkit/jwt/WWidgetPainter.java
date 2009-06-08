package eu.webtoolkit.jwt;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.http.*;
import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.utils.*;
import eu.webtoolkit.jwt.servlet.*;

abstract class WWidgetPainter {
	public void destroy() {
	}

	public abstract WPaintDevice getCreatePaintDevice();

	public abstract void createContents(DomElement element, WPaintDevice device);

	public abstract void updateContents(List<DomElement> result,
			WPaintDevice device);

	protected WWidgetPainter(WPaintedWidget widget) {
		this.widget_ = widget;
	}

	protected WPaintedWidget widget_;
}
