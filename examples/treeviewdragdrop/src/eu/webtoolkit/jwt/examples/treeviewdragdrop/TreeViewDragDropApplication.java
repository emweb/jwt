/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.treeviewdragdrop;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import eu.webtoolkit.jwt.AlignmentFlag;
import eu.webtoolkit.jwt.ItemDataRole;
import eu.webtoolkit.jwt.ItemFlag;
import eu.webtoolkit.jwt.Orientation;
import eu.webtoolkit.jwt.SelectionMode;
import eu.webtoolkit.jwt.Signal;
import eu.webtoolkit.jwt.Signal2;
import eu.webtoolkit.jwt.SortOrder;
import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WDate;
import eu.webtoolkit.jwt.WEnvironment;
import eu.webtoolkit.jwt.WGridLayout;
import eu.webtoolkit.jwt.WItemDelegate;
import eu.webtoolkit.jwt.WLength;
import eu.webtoolkit.jwt.WModelIndex;
import eu.webtoolkit.jwt.WMouseEvent;
import eu.webtoolkit.jwt.WSortFilterProxyModel;
import eu.webtoolkit.jwt.WStandardItem;
import eu.webtoolkit.jwt.WStandardItemModel;
import eu.webtoolkit.jwt.WTableView;
import eu.webtoolkit.jwt.WText;
import eu.webtoolkit.jwt.WTreeView;
import eu.webtoolkit.jwt.WVBoxLayout;
import eu.webtoolkit.jwt.WWidget;
import eu.webtoolkit.jwt.WXmlLocalizedStrings;
import eu.webtoolkit.jwt.chart.LabelOption;
import eu.webtoolkit.jwt.chart.WPieChart;
import eu.webtoolkit.jwt.examples.treeviewdragdrop.csv.CsvUtil;

/**
 * Main application class.
 */
public class TreeViewDragDropApplication extends WApplication {
    /**
     * Constructor.
     */
    public TreeViewDragDropApplication(WEnvironment env) {
        super(env);
        
        setCssTheme("polished");

        WXmlLocalizedStrings resourceBundle = new WXmlLocalizedStrings();
        resourceBundle
                .use("/eu/webtoolkit/jwt/examples/treeviewdragdrop/about");
        setLocalizedStrings(resourceBundle);

        /*
         * Create the data models.
         */
        folderModel_ = new WStandardItemModel(0, 1, this);
        populateFolders();

        fileModel_ = new FileModel(this);
        populateFiles();

        fileFilterModel_ = new WSortFilterProxyModel(this);
        fileFilterModel_.setSourceModel(fileModel_);
        fileFilterModel_.setDynamicSortFilter(true);
        fileFilterModel_.setFilterKeyColumn(0);
        fileFilterModel_.setFilterRole(ItemDataRole.UserRole);

        /*
         * Setup the user interface.
         */
        createUI();
    }

    /**
     * The folder model (used by folderView_)
     */
    private WStandardItemModel folderModel_;

    /**
     * The file model (used by fileView_)
     */
    private WStandardItemModel fileModel_;

    /**
     * The sort filter proxy model that adapts fileModel_
     */
    private WSortFilterProxyModel fileFilterModel_;

    /**
     * Maps folder id's to folder descriptions.
     */
    private Map<String, String> folderNameMap_ = new HashMap<String, String>();

    /**
     * The folder view.
     */
    private WTreeView folderView_;

    /**
     * The file view.
     */
    private WTableView fileView_;

    /**
     * Setup the user interface.
     */
    private void createUI() {
        WContainerWidget w = getRoot();
        w.setStyleClass("maindiv");

        /*
         * The main layout is a 3x2 grid layout.
         */
        WGridLayout layout = new WGridLayout();
        layout.addWidget(createTitle("Folders"), 0, 0);
        layout.addWidget(createTitle("Files"), 0, 1);
        layout.addWidget(folderView(), 1, 0);
        layout.setColumnResizable(0);

        // select the first folder
        folderView_.select(folderModel_.getIndex(0, 0, folderModel_.getIndex(0,
                0)));

        WVBoxLayout vbox = new WVBoxLayout();
        vbox.addWidget(fileView(), 0);
        vbox.addWidget(pieChart(), 0);
        vbox.setResizable(0);

        layout.addLayout(vbox, 1, 1);

        layout.addWidget(aboutDisplay(), 2, 0, 1, 2, AlignmentFlag.AlignTop);

        /*
         * Let row 1 and column 1 take the excess space.
         */
        layout.setRowStretch(1, 1);
        layout.setColumnStretch(1, 1);

        w.setLayout(layout);
    }

    /**
     * Creates a title widget.
     */
    private WText createTitle(String title) {
        WText result = new WText(title);
        result.setInline(false);
        result.setStyleClass("title");

        return result;
    }

    /**
     * Creates the folder WTreeView
     */
    private WTreeView folderView() {
        WTreeView treeView = new FolderView();

        /*
         * To support right-click, we need to disable the built-in browser
         * context menu.
         * 
         * Note that disabling the context menu and catching the right-click
         * does not work reliably on all browsers.
         */
        treeView
                .setAttributeValue("oncontextmenu",
                        "event.cancelBubble = true; event.returnValue = false; return false;");
        treeView.setModel(folderModel_);
        treeView.resize(new WLength(200), WLength.Auto);
        treeView.setSelectionMode(SelectionMode.SingleSelection);
        treeView.expandToDepth(1);
        treeView.selectionChanged().addListener(this, new Signal.Listener() {
            public void trigger() {
                folderChanged();
            }
        });

        folderView_ = treeView;

        return treeView;
    }

    /**
     * Creates the file table view (also a WTreeView)
     */
    private WTableView fileView() {
        WTableView tableView = new WTableView();

        // Hide the tree-like decoration on the first column, to make it
        // resemble a plain table
        tableView.setAlternatingRowColors(true);

        tableView.setModel(fileFilterModel_);
        tableView.setSelectionMode(SelectionMode.ExtendedSelection);
        tableView.setDragEnabled(true);

        tableView.setColumnWidth(0, new WLength(100));
        tableView.setColumnWidth(1, new WLength(150));
        tableView.setColumnWidth(2, new WLength(100));
        tableView.setColumnWidth(3, new WLength(60));
        tableView.setColumnWidth(4, new WLength(100));
        tableView.setColumnWidth(5, new WLength(100));

        WItemDelegate delegate = new WItemDelegate(this);
        delegate.setTextFormat(FileModel.dateDisplayFormat);
        tableView.setItemDelegateForColumn(4, delegate);
        tableView.setItemDelegateForColumn(5, delegate);

        tableView.setColumnAlignment(3, AlignmentFlag.AlignRight);
        tableView.setColumnAlignment(4, AlignmentFlag.AlignRight);
        tableView.setColumnAlignment(5, AlignmentFlag.AlignRight);

        tableView.sortByColumn(1, SortOrder.AscendingOrder);

        tableView.doubleClicked().addListener(this,
                new Signal2.Listener<WModelIndex, WMouseEvent>() {
                    public void trigger(WModelIndex mi, WMouseEvent me) {
                        editFile(mi);
                    }
                });

        fileView_ = tableView;

        return tableView;
    }

    /**
     * Edit a particular row.
     */
    private void editFile(WModelIndex item) {
        new FileEditDialog(fileView_.getModel(), item);
    }

    /**
     * Creates the chart.
     */
    private WWidget pieChart() {
        WPieChart chart = new WPieChart();
        chart.setModel(fileFilterModel_);
        chart.setTitle("File sizes");

        chart.setLabelsColumn(1); // Name
        chart.setDataColumn(3); // Size

        chart.setPerspectiveEnabled(true, 0.2);
        chart.setDisplayLabels(LabelOption.Outside, LabelOption.TextLabel);

        chart.setStyleClass("about");

        return chart;
    }

    /**
     * Creates the hints text.
     */
    private WWidget aboutDisplay() {
        WText result = new WText(tr("about-text"));
        result.setStyleClass("about");
        return result;
    }

    /**
     * Change the filter on the file view when the selected folder changes.
     */
    void folderChanged() {
        if (folderView_.getSelectedIndexes().size() == 0)
            return;

        WModelIndex selected = folderView_.getSelectedIndexes().first();
        Object d = selected.getData(ItemDataRole.UserRole);
        if (d != null) {
            String folder = (String) d;

            // For simplicity, we assume here that the folder-id does not
            // contain special regexp characters, otherwise these need to be
            // escaped -- or use the \Q \E qutoing escape regular expression
            // syntax (and escape \E)
            fileFilterModel_.setFilterRegExp(folder);
        }
    }

    /**
     * Populate the files model.
     * 
     * Data (and headers) is read from the CSV file. We add icons to the first
     * column, resolve the folder id to the actual folder name, and configure
     * item flags, and parse date values.
     */
    private void populateFiles() {
        fileModel_.getInvisibleRootItem().setRowCount(0);

        InputStream is = fileModel_.getClass().getResourceAsStream(
                "/eu/webtoolkit/jwt/examples/treeviewdragdrop/data/files.csv");
        CsvUtil.readFromCsv(new BufferedReader(new InputStreamReader(is)),
                fileModel_);

        for (int i = 0; i < fileModel_.getRowCount(); ++i) {
            WStandardItem item = fileModel_.getItem(i, 0);
            EnumSet<ItemFlag> flags = item.getFlags();
            flags.add(ItemFlag.ItemIsDragEnabled);
            item.setFlags(flags);
            item.setIcon("pics/file.gif");

            String folderId = item.getText().getValue();

            item.setData(folderId, ItemDataRole.UserRole);
            item.setText(folderNameMap_.get(folderId));

            convertToDate(fileModel_.getItem(i, 4));
            convertToDate(fileModel_.getItem(i, 5));
        }
    }

    /**
     * Convert a string to a date.
     */
    private void convertToDate(WStandardItem item) {
        WDate d = WDate.fromString(item.getText().getValue(),
                FileModel.dateEditFormat);
        item.setData(d, ItemDataRole.DisplayRole);
    }

    /**
     * Populate the folders model.
     */
    private void populateFolders() {
        WStandardItem level1;

        folderModel_.appendRow(level1 = createFolderItem("San Fransisco"));
        level1.appendRow(createFolderItem("Investors", "sf-investors"));
        level1.appendRow(createFolderItem("Fellows", "sf-fellows"));

        folderModel_.appendRow(level1 = createFolderItem("Sophia Antipolis"));
        level1.appendRow(createFolderItem("R&D", "sa-r_d"));
        level1.appendRow(createFolderItem("Services", "sa-services"));
        level1.appendRow(createFolderItem("Support", "sa-support"));
        level1.appendRow(createFolderItem("Billing", "sa-billing"));

        folderModel_.appendRow(level1 = createFolderItem("New York"));
        level1.appendRow(createFolderItem("Marketing", "ny-marketing"));
        level1.appendRow(createFolderItem("Sales", "ny-sales"));
        level1.appendRow(createFolderItem("Advisors", "ny-advisors"));

        folderModel_.appendRow(level1 = createFolderItem("Frankfürt"));
        level1.appendRow(createFolderItem("Sales", "frank-sales"));

        folderModel_.setHeaderData(0, Orientation.Horizontal, "SandBox");
    }

    /**
     * Create a folder item.
     * 
     * Configures flags for drag and drop support.
     */
    private WStandardItem createFolderItem(String location) {
        return createFolderItem(location, null);
    }

    /**
     * Create a folder item.
     * 
     * Configures flags for drag and drop support.
     */
    private WStandardItem createFolderItem(String location, String folderId) {
        WStandardItem result = new WStandardItem(location);

        EnumSet<ItemFlag> flags = result.getFlags();

        if (folderId != null) {
            result.setData(folderId);
            flags.add(ItemFlag.ItemIsDropEnabled);
            result.setFlags(flags);
            folderNameMap_.put(folderId, location);
        } else {
            flags.remove(ItemFlag.ItemIsSelectable);
            result.setFlags(flags);
        }

        result.setIcon("pics/folder.gif");

        return result;
    }
}