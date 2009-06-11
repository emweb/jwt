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
 * A selection box allows selection from a list of options.
 * 
 * 
 * By default, a selection box may be used to let the user select one item from
 * a list. This may be changed to multiple selection mode using
 * {@link WSelectionBox#setSelectionMode(SelectionMode mode)}.
 * <p>
 * The current selection may be set and read using
 * {@link WComboBox#setCurrentIndex(int index)} and
 * {@link WComboBox#getCurrentIndex()}, for
 * {@link SelectionMode#SingleSelection SingleSelection} mode, or
 * {@link WSelectionBox#setSelectedIndexes(Set selection)} and
 * {@link WSelectionBox#getSelectedIndexes()} for
 * {@link SelectionMode#ExtendedSelection ExtendedSelection} mode. The
 * {@link WComboBox#activated()} and {@link WComboBox#sactivated()} signals are
 * not emited in the ExtendedSelection mode, use the
 * {@link WFormWidget#changed()} signal {@link WFormWidget#changed()}.
 * <p>
 * WSelectionBox is an MVC view class, using a simple string list model by
 * default. The model may be populated using addItem(const {@link WString}&amp;)
 * or {@link WComboBox#insertItem(int index, CharSequence text)} and the
 * contents can be cleared through {@link WComboBox#clear()}. These methods
 * manipulate the underlying {@link WComboBox#getModel()}.
 * <p>
 * To use the selectionbox with a custom model instead of the default
 * {@link WStringListModel}, use
 * {@link WComboBox#setModel(WAbstractItemModel model)}.
 * <p>
 * The widget corresponds to the HTML <code>&lt;select&gt;</code> tag.
 * <p>
 * WSelectionBox is an {@link WWidget#setInline(boolean inlined) inline} widget.
 */
public class WSelectionBox extends WComboBox {
	public WSelectionBox(WContainerWidget parent) {
		super(parent);
		this.verticalSize_ = 5;
		this.selectionMode_ = SelectionMode.SingleSelection;
		this.selection_ = new HashSet<Integer>();
		this.configChanged_ = false;
	}

	public WSelectionBox() {
		this((WContainerWidget) null);
	}

	/**
	 * Set the number of items that are visible.
	 * 
	 * If more items are available, a scroll-bar is provided.
	 */
	public int getVerticalSize() {
		return this.verticalSize_;
	}

	/**
	 * Get the number of items that are visible.
	 */
	public void setVerticalSize(int items) {
		this.verticalSize_ = items;
		this.configChanged_ = true;
		this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyAttribute));
	}

	/**
	 * Set the selection mode.
	 * 
	 * The default selection mode is SingleSelection. You can change to
	 * ExtendedSelection to allow selection of multiple items.
	 */
	public void setSelectionMode(SelectionMode mode) {
		if (mode != this.selectionMode_) {
			this.selectionMode_ = mode;
			this.configChanged_ = true;
			this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyAttribute));
			if (mode == SelectionMode.ExtendedSelection) {
				this.selection_.clear();
				if (this.getCurrentIndex() != -1) {
					this.selection_.add(this.getCurrentIndex());
				}
			} else {
				if (this.selection_.size() == 1) {
					this.setCurrentIndex(this.selection_.iterator().next());
				} else {
					this.setCurrentIndex(-1);
				}
				this.selection_.clear();
			}
		}
	}

	/**
	 * Get the selection mode.
	 * 
	 * @see WSelectionBox#setSelectionMode(SelectionMode mode)
	 */
	public SelectionMode getSelectionMode() {
		return this.selectionMode_;
	}

	/**
	 * Get the current selection (in ExtendedSelection mode).
	 * 
	 * Get the list of currently selected items. This method is only defined
	 * when {@link WSelectionBox#getSelectionMode()} is ExtendedSelection.
	 * Otherwise, you should use {@link WComboBox#getCurrentIndex()} to get item
	 * currently selected.
	 * <p>
	 * 
	 * @see WComboBox#getCurrentIndex()
	 */
	public Set<Integer> getSelectedIndexes() {
		return this.selection_;
	}

	/**
	 * Set the selection (in ExtendedSelection mode).
	 * 
	 * For an ExtendedSelection mode, set the list of currently selected items.
	 * <p>
	 * 
	 * @see WSelectionBox#getSelectedIndexes()
	 */
	public void setSelectedIndexes(Set<Integer> selection) {
		if (this.selectionMode_ != SelectionMode.ExtendedSelection) {
			throw new WtException(
					"WSelectionBox::setSelectedIndexes() can only be used for an ExtendedSelection mode");
		}
		this.selection_ = selection;
		this.selectionChanged_ = true;
		this.repaint(EnumSet.of(RepaintFlag.RepaintInnerHtml));
	}

	/**
	 * Clear the current selection.
	 * 
	 * Clears the current selection.
	 * <p>
	 * 
	 * @see WComboBox#setCurrentIndex(int index)
	 * @see WSelectionBox#setSelectedIndexes(Set selection)
	 */
	public void clearSelection() {
		if (this.selectionMode_ == SelectionMode.ExtendedSelection) {
			this.setSelectedIndexes(new HashSet<Integer>());
		} else {
			this.setCurrentIndex(-1);
		}
	}

	private int verticalSize_;
	private SelectionMode selectionMode_;
	private Set<Integer> selection_;
	private boolean configChanged_;

	protected void updateDom(DomElement element, boolean all) {
		if (this.configChanged_ || all) {
			element.setAttribute("size", String.valueOf(this.verticalSize_));
			if (!all || this.selectionMode_ == SelectionMode.ExtendedSelection) {
				element
						.setProperty(
								Property.PropertyMultiple,
								this.selectionMode_ == SelectionMode.ExtendedSelection ? "true"
										: "false");
				if (!all) {
					this.selectionChanged_ = true;
				}
			}
			this.configChanged_ = false;
		}
		if (this.selectionMode_ == SelectionMode.ExtendedSelection) {
			if (this.selectionChanged_ && !all) {
				for (int i = 0; i < this.getCount(); ++i) {
					element.callMethod("options[" + String.valueOf(i)
							+ "].selected="
							+ (this.isSelected(i) ? "true" : "false"));
				}
				this.selectionChanged_ = false;
			}
		}
		super.updateDom(element, all);
	}

	protected void setFormData(WObject.FormData formData) {
		if (this.selectionChanged_) {
			return;
		}
		if (this.selectionMode_ == SelectionMode.SingleSelection) {
			super.setFormData(formData);
		} else {
			this.selection_.clear();
			for (int j = 0; j < formData.values.size(); ++j) {
				String v = formData.values.get(j);
				if (v.length() != 0) {
					try {
						int i = Integer.parseInt(v);
						this.selection_.add(i);
					} catch (NumberFormatException error) {
						WApplication.getInstance().log("error").append(
								"WSelectionBox received illegal form value: '")
								.append(v).append("'");
					}
				}
			}
		}
	}

	protected void propagateRenderOk(boolean deep) {
		this.configChanged_ = false;
		this.selectionChanged_ = false;
		super.propagateRenderOk(deep);
	}

	protected boolean isSelected(int index) {
		if (this.selectionMode_ == SelectionMode.ExtendedSelection) {
			boolean i = this.selection_.contains(index);
			return i != false;
		} else {
			return super.isSelected(index);
		}
	}
}
