/*
 * Copyright (C) 2009 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.treeviewdragdrop;

import eu.webtoolkit.jwt.WStandardItemModel;

/**
 * A specialized standard item model which report a specific drag and drop mime
 * type.
 * 
 * A specific drag and drop mime type instead of the generic abstract item model
 * is returned by the model.
 */
public class FileModel extends WStandardItemModel {
    /**
     * Constructor.
     */
    public FileModel() {
    	super();
    }

    @Override
    public String getMimeType() {
        return FolderView.FileSelectionMimeType;
    }

    /**
     * Date display format.
     */
    public static String dateDisplayFormat = "MMM dd, yyyy";

    /**
     * Date edit format.
     */
    public static String dateEditFormat = "dd-MM-yyyy";
}
