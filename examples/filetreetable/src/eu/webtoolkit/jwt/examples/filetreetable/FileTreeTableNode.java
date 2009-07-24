/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.filetreetable;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import eu.webtoolkit.jwt.TextFormat;
import eu.webtoolkit.jwt.WIconPair;
import eu.webtoolkit.jwt.WText;
import eu.webtoolkit.jwt.WTreeTableNode;

/**
 * A single node in a file tree table.
 * 
 * The node manages the details about one file, and if the file is a directory,
 * populates a subtree with nodes for every directory item.
 * 
 * The tree node reimplements Wt::WTreeTableNode::populate() to populate a
 * directory node only when the node is expanded. In this way, only directories
 * that are actually browsed are loaded from disk.
 */
public class FileTreeTableNode extends WTreeTableNode {
    /**
     * Construct a new node for the given file.
     */
    public FileTreeTableNode(File path) {
        super("", createIcon(path));
        path_ = path;

        getLabel().setTextFormat(TextFormat.PlainText);
        getLabel().setText(path_.getName());

        if (path.exists()) {
            if (!path.isDirectory()) {
                long fsize = path.length();
                setColumnWidget(1, new WText(fsize + ""));
                getColumnWidget(1).setStyleClass("fsize");
            } else
                setSelectable(false);

            SimpleDateFormat formatter = new SimpleDateFormat("M dd yyyy");

            setColumnWidget(2, new WText(formatter.format(new Date(path
                    .lastModified()))));
            getColumnWidget(2).setStyleClass("date");
        }
    }

    /**
     * The path.
     */
    private File path_;

    @Override
    protected void populate() {
        if (path_.isDirectory()) {
            File[] files = path_.listFiles();
            for (File f : files)
                addChildNode(new FileTreeTableNode(f));
        }
    }

    @Override
    protected boolean isExpandable() {
        if (!isPopulated()) {
            return path_.isDirectory();
        } else
            return super.isExpandable();
    }

    /**
     * Create the iconpair for representing the path.
     */
    private static WIconPair createIcon(File path) {
        if (path.exists() && path.isDirectory())
            return new WIconPair("pics/yellow-folder-closed.png",
                    "pics/yellow-folder-open.png", false);
        else
            return new WIconPair("pics/document.png",
                    "pics/yellow-folder-open.png", false);
    }
}