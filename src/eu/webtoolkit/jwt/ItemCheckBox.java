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

class ItemCheckBox extends WCheckBox {
	public ItemCheckBox(WModelIndex index) {
		super(false, (WContainerWidget) null);
		this.index_ = index;
	}

	public void setIndex(WModelIndex index) {
		this.index_ = index;
	}

	public WModelIndex getIndex() {
		return this.index_;
	}

	private WModelIndex index_;
}
