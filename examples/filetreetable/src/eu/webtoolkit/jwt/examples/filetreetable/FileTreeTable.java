package eu.webtoolkit.jwt.examples.filetreetable;

import java.io.File;

import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WLength;
import eu.webtoolkit.jwt.WTreeTable;

/**
 * A tree table that displays a file tree.
 * 
 * The table allows one to browse a path, and all its subdirectories, using a
 * tree table. In addition to the file name, it shows file size and modification
 * date.
 * 
 * The table use FileTreeTableNode objects to display the actual content of the
 * table.
 * 
 * The tree table uses the LazyLoading strategy of WTreeNode to dynamically load
 * contents for the tree.
 * 
 * This widget is part of the JWt File Explorer example.
 */
public class FileTreeTable extends WTreeTable {
	/**
	 * Construct a new FileTreeTable.
	 * 
	 * Create a new FileTreeTable to browse the given path.
	 */
	public FileTreeTable(File path) {
		this(path, null);
	}
	
	/**
	 * Construct a new FileTreeTable.
	 * 
	 * Create a new FileTreeTable to browse the given path.
	 */
	public FileTreeTable(File path, WContainerWidget parent) {
		super(parent);
		addColumn("Size", new WLength(80));
		addColumn("Modified", new WLength(110));

		header(1).setStyleClass("fsize");
		header(2).setStyleClass("date");

		setTreeRoot(new FileTreeTableNode(path), "File");

		getTreeRoot().setImagePack("pics/filetreetable/");
		getTreeRoot().expand();
	}
}