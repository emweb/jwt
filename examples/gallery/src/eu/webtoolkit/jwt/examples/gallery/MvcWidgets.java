/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.gallery;

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
import eu.webtoolkit.jwt.examples.treeview.*;

class MvcWidgets extends ControlsWidget {
	public MvcWidgets(EventDisplayer ed) {
		super(ed, true);
		new WText(tr("mvc-intro"), this);
		this.stringList_ = new WStringListModel(this);
		List<WString> strings = new ArrayList<WString>();
		strings.add(new WString("Alfa"));
		strings.add(new WString("Bravo"));
		strings.add(new WString("Charly"));
		strings.add(new WString("Delta"));
		strings.add(new WString("Echo"));
		strings.add(new WString("Foxtrot"));
		strings.add(new WString("Golf"));
		strings.add(new WString("Hotel"));
		strings.add(new WString("Indiana Jones"));
		this.stringList_.setStringList(strings);
	}

	public void populateSubMenu(WMenu menu) {
		menu.addItem("The Models", this.models());
		menu.addItem("Proxy models", this.proxyModels());
		menu.addItem("Combobox Views", DeferredWidget
				.deferCreate(new WidgetCreator() {
					public WWidget create() {
						return MvcWidgets.this.viewsCombo();
					}
				}));
		menu.addItem("WTableView", DeferredWidget
				.deferCreate(new WidgetCreator() {
					public WWidget create() {
						return MvcWidgets.this.viewsTable();
					}
				}));
		menu.addItem("WTreeView", DeferredWidget
				.deferCreate(new WidgetCreator() {
					public WWidget create() {
						return MvcWidgets.this.viewsTree();
					}
				}));
		menu.addItem("Chart Views", this.viewsChart());
	}

	private WLineEdit regexpFilter;
	private WSortFilterProxyModel filteredCocktails;
	private WSortFilterProxyModel filteredSortedCocktails;

	private WWidget models() {
		WContainerWidget result = new WContainerWidget();
		this.topic("WAbstractItemModel", "WAbstractListModel",
				"WStandardItemModel", "WStringListModel", result);
		new WText(tr("mvc-models"), result);
		return result;
	}

	private WWidget proxyModels() {
		WContainerWidget result = new WContainerWidget();
		this.topic("WAbstractProxyModel", "WSortFilterProxyModel", result);
		new WText(tr("mvc-proxymodels"), result);
		WStandardItemModel cocktails = new WStandardItemModel(result);
		cocktails.appendRow(new WStandardItem("The Last WordLime Rickey"));
		cocktails.appendRow(new WStandardItem("Gin pahit"));
		cocktails.appendRow(new WStandardItem("Alexander"));
		cocktails.appendRow(new WStandardItem("Montgomery"));
		cocktails.appendRow(new WStandardItem("Gin Sour"));
		cocktails.appendRow(new WStandardItem("Hanky-Panky"));
		cocktails.appendRow(new WStandardItem("Gimlet"));
		cocktails.appendRow(new WStandardItem("Chocolate Soldier"));
		cocktails.appendRow(new WStandardItem("Joker"));
		cocktails.appendRow(new WStandardItem("Mickey Slim"));
		cocktails.appendRow(new WStandardItem("Long Island Iced Tea"));
		cocktails.appendRow(new WStandardItem("Old Etonian"));
		cocktails.appendRow(new WStandardItem("Lorraine"));
		cocktails.appendRow(new WStandardItem("Bijou"));
		cocktails.appendRow(new WStandardItem("Bronx"));
		cocktails.appendRow(new WStandardItem("Gin and tonic"));
		cocktails.appendRow(new WStandardItem("Pall Mall"));
		cocktails.appendRow(new WStandardItem("Gin Fizz"));
		cocktails.appendRow(new WStandardItem("French 75"));
		cocktails.appendRow(new WStandardItem("Martini"));
		cocktails.appendRow(new WStandardItem("Negroni"));
		cocktails.appendRow(new WStandardItem("20th Century"));
		cocktails.appendRow(new WStandardItem("My Fair Lady"));
		cocktails.appendRow(new WStandardItem("Gibson"));
		new WText("<b>Filter regular expression: </b>", result);
		this.regexpFilter = new WLineEdit(result);
		this.regexpFilter.setText("Gi.*");
		this.regexpFilter.enterPressed().addListener(this,
				new Signal.Listener() {
					public void trigger() {
						MvcWidgets.this.changeRegexp();
					}
				});
		WPushButton filter = new WPushButton("Apply", result);
		filter.clicked().addListener(this, new Signal1.Listener<WMouseEvent>() {
			public void trigger(WMouseEvent e1) {
				MvcWidgets.this.changeRegexp();
			}
		});
		List<WAbstractItemModel> models = new ArrayList<WAbstractItemModel>();
		List<WString> headers = new ArrayList<WString>();
		headers.add(new WString("<b>Source:</b>"));
		models.add(cocktails);
		headers.add(new WString("<b>Sorted proxy:</b>"));
		WSortFilterProxyModel sortedCocktails = new WSortFilterProxyModel(this);
		sortedCocktails.setSourceModel(cocktails);
		sortedCocktails.setDynamicSortFilter(true);
		sortedCocktails.sort(0);
		models.add(sortedCocktails);
		headers.add(new WString("<b>Filtered proxy:</b>"));
		this.filteredCocktails = new WSortFilterProxyModel(this);
		this.filteredCocktails.setSourceModel(cocktails);
		this.filteredCocktails.setDynamicSortFilter(true);
		this.filteredCocktails.setFilterKeyColumn(0);
		this.filteredCocktails.setFilterRole(ItemDataRole.DisplayRole);
		this.filteredCocktails.setFilterRegExp(this.regexpFilter.getText());
		models.add(this.filteredCocktails);
		headers.add(new WString("<b>Sorted and filtered proxy:</b>"));
		this.filteredSortedCocktails = new WSortFilterProxyModel(this);
		this.filteredSortedCocktails.setSourceModel(cocktails);
		this.filteredSortedCocktails.setDynamicSortFilter(true);
		this.filteredSortedCocktails.setFilterKeyColumn(0);
		this.filteredSortedCocktails.setFilterRole(ItemDataRole.DisplayRole);
		this.filteredSortedCocktails.setFilterRegExp(this.regexpFilter
				.getText());
		this.filteredSortedCocktails.sort(0);
		models.add(this.filteredSortedCocktails);
		WTable layout = new WTable(result);
		for (int i = 0; i < headers.size(); ++i) {
			layout.getColumnAt(i).setWidth(
					new WLength(25, WLength.Unit.Percentage));
			layout.getElementAt(0, i).setPadding(new WLength(4));
			layout.getElementAt(0, i).setContentAlignment(
					EnumSet.of(AlignmentFlag.AlignCenter));
			new WText(headers.get(i), layout.getElementAt(0, i));
			new WBreak(layout.getElementAt(0, i));
			WSelectionBox view = new WSelectionBox(layout.getElementAt(0, i));
			view.setModel(models.get(i));
			view.setVerticalSize(cocktails.getRowCount());
			view.resize(new WLength(90, WLength.Unit.Percentage), WLength.Auto);
		}
		return result;
	}

	private WWidget viewsCombo() {
		WContainerWidget result = new WContainerWidget();
		this.topic("WComboBox", "WSelectionBox", "Ext::ComboBox", result);
		new WText(tr("mvc-stringlistviews"), result);
		new WText("<h3>WComboBox</h3>", result);
		new WComboBox(result).setModel(this.stringList_);
		new WText("<h3>WSelectionBox</h3>", result);
		new WSelectionBox(result).setModel(this.stringList_);
		return result;
	}

	private WWidget viewsTable() {
		WContainerWidget result = new WContainerWidget();
		this.topic("WTableView", result);
		new WText(tr("mvc-WTableView"), result);
		return result;
	}

	private WWidget viewsTree() {
		WContainerWidget result = new WContainerWidget();
		this.topic("WTreeView", result);
		WStandardItemModel model = TreeViewExample.createModel(false, this);
		TreeViewExample tv1 = new TreeViewExample(model, tr("mvc-WTreeView"));
		result.addWidget(tv1);
		TreeViewExample tv2 = new TreeViewExample(model,
				tr("mvc-WTreeView-column1Fixed"));
		result.addWidget(tv2);
		tv2.getTreeView().setColumn1Fixed(true);
		tv2.getTreeView().setColumnWidth(0, new WLength(300));
		return result;
	}

	private WWidget viewsChart() {
		WContainerWidget result = new WContainerWidget();
		this.topic("Chart::WCartesianChart", "Chart::WPieChart", result);
		new WText(tr("mvc-Chart"), result);
		return result;
	}

	private WStringListModel stringList_;

	private void changeRegexp() {
		this.filteredCocktails.setFilterRegExp(this.regexpFilter.getText());
		this.filteredSortedCocktails.setFilterRegExp(this.regexpFilter
				.getText());
	}
}
