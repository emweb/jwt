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

/**
 * A container widget that stacks its widgets on top of each other
 * 
 * 
 * This is a container widgets which at all times has only one item visible. The
 * widget accomplishes this using {@link WWebWidget#setHidden(boolean hidden)}
 * on the children.
 * <p>
 * Using {@link WStackedWidget#getCurrentIndex()} and
 * {@link WStackedWidget#setCurrentIndex(int index)} you can retrieve or set the
 * visible widget.
 * <p>
 * WStackedWidget, like {@link WContainerWidget}, is by default not inline.
 * <p>
 * 
 * @see WMenu
 */
public class WStackedWidget extends WContainerWidget {
	/**
	 * Created a new stacked container widget.
	 */
	public WStackedWidget(WContainerWidget parent) {
		super(parent);
		this.widgets_ = new ArrayList<WWidget>();
		this.currentIndex_ = -1;
	}

	public WStackedWidget() {
		this((WContainerWidget) null);
	}

	public void addWidget(WWidget widget) {
		this.insertWidget(this.widgets_.size(), widget);
	}

	/**
	 * Returns the number of widgets in the stack.
	 */
	public int getCount() {
		return this.widgets_.size();
	}

	/**
	 * Returns the index of the widget that is currently shown.
	 * 
	 * @see WStackedWidget#setCurrentIndex(int index)
	 * @see WStackedWidget#getCurrentWidget()
	 */
	public int getCurrentIndex() {
		return this.currentIndex_;
	}

	/**
	 * Returns the widget that is currently shown.
	 * 
	 * @see WStackedWidget#setCurrentWidget(WWidget widget)
	 * @see WStackedWidget#getCurrentIndex()
	 */
	public WWidget getCurrentWidget() {
		return this.widgets_.get(this.currentIndex_);
	}

	/**
	 * Returns the index of the given widget.
	 * 
	 * Returns -1 if the <i>widget</i> was not added.
	 * <p>
	 * 
	 * @see WStackedWidget#getWidget(int index)
	 */
	public int getIndexOf(WWidget widget) {
		return this.widgets_.indexOf(widget);
	}

	/**
	 * Insert a widget at a given index.
	 */
	public void insertWidget(int index, WWidget widget) {
		super.addWidget(widget);
		this.widgets_.add(0 + index, widget);
		if (this.currentIndex_ == -1) {
			this.currentIndex_ = 0;
		}
	}

	public void removeWidget(WWidget widget) {
		this.widgets_.remove(widget);
		if (this.currentIndex_ >= (int) this.widgets_.size()) {
			this.setCurrentIndex(this.widgets_.size() - 1);
		}
	}

	/**
	 * Returns the widget at the specified index.
	 * 
	 * @see WStackedWidget#getIndexOf(WWidget widget)
	 */
	public WWidget getWidget(int index) {
		return this.widgets_.get(index);
	}

	/**
	 * Shows a particular widget.
	 * 
	 * The widget with index <i>index</i> is made visible, while all other
	 * widgets are invisible.
	 * <p>
	 * 
	 * @see WStackedWidget#getCurrentIndex()
	 * @see WStackedWidget#setCurrentWidget(WWidget widget)
	 */
	public void setCurrentIndex(int index) {
		this.currentIndex_ = index;
		for (int i = 0; i < (int) this.widgets_.size(); ++i) {
			this.widgets_.get(i).setHidden(this.currentIndex_ != (int) i);
		}
	}

	/**
	 * Shows a particular widget.
	 * 
	 * The widget <i>widget</i>, which must have been added before, is made
	 * visible, while all other widgets are invisible.
	 * <p>
	 * 
	 * @see WStackedWidget#getCurrentWidget()
	 * @see WStackedWidget#setCurrentIndex(int index)
	 */
	public void setCurrentWidget(WWidget widget) {
		this.setCurrentIndex(this.getIndexOf(widget));
	}

	protected void removeChild(WWidget child) {
		this.removeWidget(child);
		super.removeChild(child);
	}

	protected DomElement createDomElement(WApplication app) {
		this.setCurrentIndex(this.currentIndex_);
		WApplication
				.getInstance()
				.doJavaScript(
						this.getJsRef()
								+ ".wtSetHeight = function(self, h){var j,jl,c;self.style.height=h+'px';for (j=0, jl=self.childNodes.length; j<jl; ++j){c=self.childNodes[j];c.style.height = self.style.height;}}");
		return super.createDomElement(app);
	}

	protected void getDomChanges(List<DomElement> result, WApplication app) {
		this.setCurrentIndex(this.currentIndex_);
		super.getDomChanges(result, app);
	}

	private List<WWidget> widgets_;
	private int currentIndex_;
}
