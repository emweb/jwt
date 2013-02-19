/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.widgetgallery;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.lang.ref.*;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.http.*;
import javax.servlet.*;
import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.utils.*;
import eu.webtoolkit.jwt.servlet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class TreesTables extends TopicWidget {
	private static Logger logger = LoggerFactory.getLogger(TreesTables.class);

	public TreesTables() {
		super();
		addText(tr("mvc-intro"), this);
	}

	public void populateSubMenu(WMenu menu) {
		menu.setInternalBasePath("/trees-tables");
		menu.addItem("Tables", this.tables()).setPathComponent("");
		menu.addItem("Trees", DeferredWidget.deferCreate(new WidgetCreator() {
			public WWidget create() {
				return TreesTables.this.trees();
			}
		}));
		menu.addItem("Tree Tables", DeferredWidget
				.deferCreate(new WidgetCreator() {
					public WWidget create() {
						return TreesTables.this.treeTables();
					}
				}));
		menu.addItem("MVC Table Views", DeferredWidget
				.deferCreate(new WidgetCreator() {
					public WWidget create() {
						return TreesTables.this.tableViews();
					}
				}));
		menu.addItem("MVC Tree Views", DeferredWidget
				.deferCreate(new WidgetCreator() {
					public WWidget create() {
						return TreesTables.this.treeViews();
					}
				}));
		menu.addItem("MVC Item models", DeferredWidget
				.deferCreate(new WidgetCreator() {
					public WWidget create() {
						return TreesTables.this.itemModels();
					}
				}));
	}

	private WLineEdit regexpFilter;
	private WSortFilterProxyModel filteredCocktails;
	private WSortFilterProxyModel filteredSortedCocktails;

	private WWidget tables() {
		WTemplate result = new TopicTemplate("treestables-Tables");
		result.bindWidget("PlainTable", PlainTable());
		result.bindWidget("StyledTable", StyledTable());
		return result;
	}

	private WWidget trees() {
		WTemplate result = new TopicTemplate("treestables-Trees");
		result.bindWidget("Tree", Tree());
		return result;
	}

	private WWidget treeTables() {
		WTemplate result = new TopicTemplate("treestables-TreeTables");
		result.bindWidget("TreeTable", TreeTable());
		return result;
	}

	private WWidget tableViews() {
		WTemplate result = new TopicTemplate("treestables-TableViews");
		result.bindWidget("SmallTableView", SmallTableView());
		result.bindWidget("LargeTableView", LargeTableView());
		return result;
	}

	private WWidget treeViews() {
		WTemplate result = new TopicTemplate("treestables-TreeViews");
		result.bindWidget("TreeView", TreeView());
		return result;
	}

	private WWidget itemModels() {
		WTemplate result = new TopicTemplate("treestables-ItemModels");
		result.bindWidget("LargeTableView", LargeTableView());
		result.bindWidget("TreeView", TreeView());
		return result;
	}

	// private WWidget proxyModels() ;
	private WStringListModel stringList_;
	// private void changeRegexp() ;
	private static Employee[] employees = { new Employee("Mark", "Otto", 100),
			new Employee("Jacob", "Thornton", 50),
			new Employee("Larry the Bird", "", 10) };

	WWidget PlainTable() {
		WTable table = new WTable();
		table.setHeaderCount(1);
		table.setWidth(new WLength("100%"));
		table.getElementAt(0, 0).addWidget(new WText("#"));
		table.getElementAt(0, 1).addWidget(new WText("First Name"));
		table.getElementAt(0, 2).addWidget(new WText("Last Name"));
		table.getElementAt(0, 3).addWidget(new WText("Pay"));
		for (int i = 0; i < 3; ++i) {
			Employee employee = employees[i];
			int row = i + 1;
			table.getElementAt(row, 0).addWidget(
					new WText(new WString("{1}").arg(row)));
			table.getElementAt(row, 1).addWidget(new WText(employee.firstName));
			table.getElementAt(row, 2).addWidget(new WText(employee.lastName));
			table.getElementAt(row, 3).addWidget(
					new WLineEdit(new WString("{1}").arg(employee.pay)
							.toString()));
		}
		return table;
	}

	void addOptionToggle(final WWidget widget, String option,
			final String styleClass, WContainerWidget parent) {
		final WCheckBox checkBox = new WCheckBox(option, parent);
		checkBox.setInline(false);
		checkBox.changed().addListener(this, new Signal.Listener() {
			public void trigger() {
				widget.toggleStyleClass(styleClass, checkBox.isChecked());
			}
		});
	}

	WWidget StyledTable() {
		WTable table = new WTable();
		table.setHeaderCount(1);
		table.getElementAt(0, 0).addWidget(new WText("#"));
		table.getElementAt(0, 1).addWidget(new WText("First Name"));
		table.getElementAt(0, 2).addWidget(new WText("Last Name"));
		table.getElementAt(0, 3).addWidget(new WText("Pay"));
		for (int i = 0; i < 3; ++i) {
			Employee employee = employees[i];
			int row = i + 1;
			new WText(new WString("{1}").arg(row), table.getElementAt(row, 0));
			new WText(employee.firstName, table.getElementAt(row, 1));
			new WText(employee.lastName, table.getElementAt(row, 2));
			new WLineEdit(new WString("{1}").arg(employee.pay).toString(),
					table.getElementAt(row, 3));
		}
		table.addStyleClass("table form-inline");
		WContainerWidget result = new WContainerWidget();
		result.addWidget(table);
		new WText("Options:", result);
		addOptionToggle(table, "borders", "table-bordered", result);
		addOptionToggle(table, "hover", "table-hover", result);
		addOptionToggle(table, "condensed", "table-condensed", result);
		addOptionToggle(table, "stripes", "table-striped", result);
		return result;
	}

	WWidget Tree() {
		WTree tree = new WTree();
		tree.setSelectionMode(SelectionMode.ExtendedSelection);
		WIconPair folderIcon = new WIconPair("icons/yellow-folder-closed.png",
				"icons/yellow-folder-open.png", false);
		WTreeNode node = new WTreeNode("Furniture", folderIcon);
		tree.setTreeRoot(node);
		node.getLabel().setTextFormat(TextFormat.PlainText);
		node.setLoadPolicy(WTreeNode.LoadPolicy.NextLevelLoading);
		node.addChildNode(new WTreeNode("Table"));
		node.addChildNode(new WTreeNode("Cupboard"));
		WTreeNode three = new WTreeNode("Chair");
		node.addChildNode(three);
		node.addChildNode(new WTreeNode("Coach"));
		node.expand();
		three.addChildNode(new WTreeNode("Doc"));
		three.addChildNode(new WTreeNode("Grumpy"));
		three.addChildNode(new WTreeNode("Happy"));
		three.addChildNode(new WTreeNode("Sneezy"));
		three.addChildNode(new WTreeNode("Dopey"));
		three.addChildNode(new WTreeNode("Bashful"));
		three.addChildNode(new WTreeNode("Sleepy"));
		return tree;
	}

	static WTreeTableNode addNode(WTreeTableNode parent, String name,
			String yuppie, String holidays, String favorite) {
		WTreeTableNode node = new WTreeTableNode(name, (WIconPair) null, parent);
		node.setColumnWidget(1, new WText(yuppie));
		node.setColumnWidget(2, new WText(holidays));
		node.setColumnWidget(3, new WText(favorite));
		return node;
	}

	WWidget TreeTable() {
		WTreeTable treeTable = new WTreeTable();
		treeTable.resize(new WLength(650), new WLength(200));
		treeTable.getTree().setSelectionMode(SelectionMode.ExtendedSelection);
		treeTable.addColumn("Yuppie Factor", new WLength(125));
		treeTable.addColumn("# Holidays", new WLength(125));
		treeTable.addColumn("Favorite Item", new WLength(125));
		WTreeTableNode root = new WTreeTableNode("All Personnel");
		treeTable.setTreeRoot(root, "Emweb Organigram");
		WTreeTableNode group;
		group = new WTreeTableNode("Upper Management", (WIconPair) null, root);
		addNode(group, "Chief Anything Officer", "-2.8", "20", "Scepter");
		addNode(group, "Vice President of Parties", "13.57", "365", "Flag");
		addNode(group, "Vice President of Staplery", "3.42", "27", "Perforator");
		group = new WTreeTableNode("Middle management", (WIconPair) null, root);
		addNode(group, "Boss of the house", "9.78", "35", "Happy Animals");
		addNode(group, "Xena caretaker", "8.66", "10", "Yellow bag");
		group = new WTreeTableNode("Actual Workforce", (WIconPair) null, root);
		addNode(group, "The Dork", "9.78", "22", "Mojito");
		addNode(group, "The Stud", "8.66", "46", "Toothbrush");
		addNode(group, "The Ugly", "13.0", "25", "Paper bag");
		root.expand();
		return treeTable;
	}

	WWidget getVirtualModel() {
		return null;
	}

	WWidget SmallTableView() {
		WTableView tableView = new WTableView();
		tableView.setModel(CsvUtil.csvToModel("" + "table.csv"));
		tableView.setColumnResizeEnabled(false);
		tableView.setColumnAlignment(0, AlignmentFlag.AlignCenter);
		tableView.setHeaderAlignment(0, EnumSet.of(AlignmentFlag.AlignCenter));
		tableView.setAlternatingRowColors(true);
		tableView.setRowHeight(new WLength(28));
		tableView.setHeaderHeight(new WLength(28));
		tableView.setSelectionMode(SelectionMode.SingleSelection);
		for (int i = 0; i < tableView.getModel().getColumnCount(); ++i) {
			tableView.setColumnWidth(i, new WLength(120));
		}
		tableView.setWidth(new WLength(127 * tableView.getModel()
				.getColumnCount()));
		return tableView;
	}

	WWidget LargeTableView() {
		WTableView tableView = new WTableView();
		tableView.setModel(new VirtualModel(10000, 50));
		tableView.setRowHeaderCount(1);
		tableView.setSortingEnabled(false);
		tableView.setAlternatingRowColors(true);
		tableView.setRowHeight(new WLength(28));
		tableView.setHeaderHeight(new WLength(28));
		tableView.resize(new WLength(650), new WLength(400));
		return tableView;
	}

	WWidget getGitModel() {
		return null;
	}

	WWidget TreeView() {
		WTreeView treeView = new WTreeView();
		treeView.resize(new WLength(600), new WLength(400));
		WAbstractItemModel model = new GitModel("/home/koen/git/jwt", treeView);
		treeView.setModel(model);
		treeView.setRowHeight(new WLength(24));
		treeView.setHeaderHeight(new WLength(24));
		treeView.setSortingEnabled(false);
		treeView.setSelectionMode(SelectionMode.SingleSelection);
		return treeView;
	}
}
