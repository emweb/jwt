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
 * Abstract delegate class for rendering a view item.
 * 
 * 
 * Rendering of an item in a {@link WTreeView} is delegated to an implementation
 * of this delegate class. The default implementation used by {@link WTreeView}
 * is {@link WItemDelegate}. To provide specialized rendering support, you can
 * reimplement this class, and indicate to the treeview to use this delegate for
 * rendering items.
 * <p>
 * As a delegate is used for rendering multiple items, the class should not keep
 * state about one specific item.
 * <p>
 * An example of a delegate that always renders the text in a line-edit, and
 * saves the modified value back to the (editable) model.
 * <p>
 * 
 * @see WTreeView#setItemDelegate(WAbstractItemDelegate delegate)
 * @see WTreeView#setItemDelegateForColumn(int column, WAbstractItemDelegate
 *      delegate)
 */
public abstract class WAbstractItemDelegate extends WObject {
	/**
	 * Constructor.
	 */
	public WAbstractItemDelegate(WObject parent) {
		super(parent);
	}

	public WAbstractItemDelegate() {
		this((WObject) null);
	}

	/**
	 * Desturctor.
	 */
	public void destroy() {
	}

	public abstract WWidget update(WWidget widget, WModelIndex index,
			EnumSet<ViewItemRenderFlag> flags);

	public final WWidget update(WWidget widget, WModelIndex index,
			ViewItemRenderFlag flag, ViewItemRenderFlag... flags) {
		return update(widget, index, EnumSet.of(flag, flags));
	}

	/**
	 * Update the model index of a widget.
	 * 
	 * This method is invoked by the view when due to row/column insertions or
	 * removals, an index was modified for a widget.
	 * <p>
	 * You should reimplement this method if you are storing the model index
	 * (e.g. for editing purposes) in the <i>widget</i>, which you should update
	 * to the new <i>index</i>.
	 * <p>
	 * The default implementation does nothing.
	 */
	public void updateModelIndex(WWidget widget, WModelIndex index) {
	}
}
