/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.treeviewdragdrop;

import java.util.SortedMap;
import java.util.TreeSet;

import eu.webtoolkit.jwt.ItemDataRole;
import eu.webtoolkit.jwt.WAbstractItemModel;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WDropEvent;
import eu.webtoolkit.jwt.WItemSelectionModel;
import eu.webtoolkit.jwt.WModelIndex;
import eu.webtoolkit.jwt.WTreeView;

/**
 * A specialized treeview that supports a custom drop event.
 */
public class FolderView extends WTreeView {
	/**
	 * Constant that indicates the mime type for a selection of files.
	 * 
	 * Every kind of dragged data should be identified using a unique mime type.
	 */
	public static final String FileSelectionMimeType = "application/x-computers-selection";

	/**
	 * Constructor.
	 */
	public FolderView() {
		this(null);
	}
	
	/**
	 * Constructor.
	 */
	public FolderView(WContainerWidget parent) {
		super(parent);
		/*
		 * Accept drops for the custom mime type.
		 */
		acceptDrops(FileSelectionMimeType);
	}

	/**
	 * Drop event.
	 */
	@Override
	protected void dropEvent(WDropEvent event, WModelIndex target) {
		/*
		 * We reimplement the drop event to handle the dropping of a selection
		 * of computers.
		 * 
		 * The test below would always be true in this case, since we only
		 * indicated support for that particular mime type.
		 */
		if (event.getMimeType().equals(FileSelectionMimeType)) {
			/*
			 * The source object for a drag of a selection from a WTreeView is a
			 * WItemSelectionModel.
			 */
			WItemSelectionModel selection = (WItemSelectionModel) event.getSource();

			/*
			 * You can access the source model from the selection and
			 * manipulate it.
			 */
			WAbstractItemModel sourceModel = selection.getModel();

			TreeSet<WModelIndex> toChange = new TreeSet<WModelIndex>(selection.getSelectedIndexes());

			for (WModelIndex index : toChange.descendingSet()) {
				/*
				 * Copy target folder to file. Since we are using a dynamic
				 * WSortFilterProxyModel that filters on folder, this will
				 * also result in the removal of the file from the current
				 * view.
				 */
				SortedMap<Integer, Object> data = getModel().getItemData(target);
				data.put(ItemDataRole.DecorationRole, index
						.getData(ItemDataRole.DecorationRole));
				sourceModel.setItemData(index, data);
			}
		}
	}
}