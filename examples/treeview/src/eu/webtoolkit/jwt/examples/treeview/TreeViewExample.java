/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.treeview;

import java.util.ArrayList;

import eu.webtoolkit.jwt.AlignmentFlag;
import eu.webtoolkit.jwt.Orientation;
import eu.webtoolkit.jwt.SelectionMode;
import eu.webtoolkit.jwt.Signal1;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WLength;
import eu.webtoolkit.jwt.WMouseEvent;
import eu.webtoolkit.jwt.WPushButton;
import eu.webtoolkit.jwt.WStandardItem;
import eu.webtoolkit.jwt.WStandardItemModel;
import eu.webtoolkit.jwt.WText;
import eu.webtoolkit.jwt.WTreeView;

public class TreeViewExample extends WContainerWidget {
	enum WeatherIcon {
		Sun("sun01.png"), 
		SunCloud("cloudy01.png"), 
		Cloud("w_cloud.png"), 
		Rain("rain.png"), 
		Storm("storm.png"), 
		Snow("snow.png");

		private String icon;

		public String getIcon() {
			return icon;
		}

		private WeatherIcon(String icon) {
			this.icon = icon;
		}
	}

	public TreeViewExample(boolean useInternalPath, WContainerWidget parent) {
		super(parent);
		useInternalPath_ = useInternalPath;

		new WText(tr("treeview-introduction"), this);

		/*
		 * Setup a model.
		 * 
		 * We use the standard item model, which is a general model suitable for
		 * hierarchical (tree-like) data, but stores all data in memory.
		 */
		model_ = new WStandardItemModel(0, 4, this);

		/*
		 * Headers ...
		 */
		model_.setHeaderData(0, Orientation.Horizontal, "Places");
		model_.setHeaderData(1, Orientation.Horizontal, "Weather");
		model_.setHeaderData(2, Orientation.Horizontal, "Drink");
		model_.setHeaderData(3, Orientation.Horizontal, "Visited");

		/*
		 * ... and data
		 */
		WStandardItem continent, country;

		model_.appendRow(continent = continentItem("Europe"));

		continent.appendRow(country = countryItem("Belgium", "be"));
		country
				.appendRow(cityItems("Brussels", WeatherIcon.Rain, "Beer", true));
		country.appendRow(cityItems("Leuven", WeatherIcon.Rain, "Beer", true));

		belgium_ = country;

		continent.appendRow(country = countryItem("France", "fr"));
		country.appendRow(cityItems("Paris", WeatherIcon.Cloud, "Wine", true));
		country.appendRow(cityItems("Bordeaux", WeatherIcon.SunCloud,
				"Bordeaux wine", false));

		continent.appendRow(country = countryItem("Spain", "sp"));
		country
				.appendRow(cityItems("Barcelona", WeatherIcon.Sun, "Cava", true));
		country.appendRow(cityItems("Madrid", WeatherIcon.Sun, "San Miguel",
				false));

		model_.appendRow(continent = continentItem("Africa"));

		continent.appendRow(country = countryItem("Morocco (المغرب)", "ma"));
		country
				.appendRow(cityItems("Casablanca", WeatherIcon.Sun, "Tea",
						false));

		/*
		 * Now create the view
		 */
		treeView_ = new WTreeView(this);
		// treeView_.setColumn1Fixed(true);
		treeView_.setAlternatingRowColors(!treeView_.hasAlternatingRowColors());
		treeView_.setRowHeight(new WLength(30));
		// treeView_.setHeaderHeight(40, true);
		treeView_.setModel(model_);
		// treeView_.setSortingEnabled(false);
		// treeView_.setColumnResizeEnabled(false);
		treeView_.setSelectionMode(SelectionMode.NoSelection);
		treeView_.resize(600, 300);

		treeView_.setColumnWidth(1, new WLength(100));
		treeView_.setColumnAlignment(1, AlignmentFlag.AlignCenter);
		treeView_.setColumnWidth(3, new WLength(100));
		treeView_.setColumnAlignment(3, AlignmentFlag.AlignCenter);

		/*
		 * Expand the first (and single) top level node
		 */
		treeView_.setExpanded(model_.getIndex(0, 0), true);

		/*
		 * Setup some buttons to manipulate the view and the model.
		 */
		WContainerWidget wc = new WContainerWidget(this);
		WPushButton b;

		b = new WPushButton("Toggle row height", wc);
		b.clicked().addListener(this, new Signal1.Listener<WMouseEvent>() {
			public void trigger(WMouseEvent a1) {
				toggleRowHeight();
			}
		});
		b.setToolTip("Toggles row height between 30px and 25px");

		b = new WPushButton("Toggle stripes", wc);
		b.clicked().addListener(this, new Signal1.Listener<WMouseEvent>() {
			public void trigger(WMouseEvent a1) {
				toggleStripes();
			}
		});
		b.setToolTip("Toggle alternating row colors");

		b = new WPushButton("Toggle root", wc);
		b.clicked().addListener(this, new Signal1.Listener<WMouseEvent>() {
			public void trigger(WMouseEvent a1) {
				toggleRoot();
			}
		});
		b
				.setToolTip("Toggles root item between all and the first continent.");

		b = new WPushButton("Add rows", wc);
		b.clicked().addListener(this, new Signal1.Listener<WMouseEvent>() {
			public void trigger(WMouseEvent a1) {
				addRows();
			}
		});
		b.setToolTip("Adds some cities to Belgium");
	}

	private boolean useInternalPath_;
	private WStandardItem belgium_;
	private WStandardItemModel model_;
	private WTreeView treeView_;

	private WStandardItem continentItem(String continent) {
		return new WStandardItem(continent);
	}

	private WStandardItem countryItem(String country, String code) {
		WStandardItem result = new WStandardItem(country);
		result.setIcon("pics/flag_" + code + ".png");

		return result;
	}

	private ArrayList<WStandardItem> cityItems(String city,
			WeatherIcon weather, String drink, boolean visited) {
		ArrayList<WStandardItem> result = new ArrayList<WStandardItem>();
		WStandardItem item;

		// column 0: country
		item = new WStandardItem(city);
		result.add(item);

		// column 1: weather
		item = new WStandardItem();
		item.setIcon("pics/" + weather.getIcon());
		result.add(item);

		// column 2: drink
		item = new WStandardItem(drink);
		if (useInternalPath_) {
			item.setInternalPath("/drinks/" + drink);
		}
		result.add(item);

		// column 3: visited
		item = new WStandardItem();
		item.setCheckable(true);
		item.setChecked(visited);
		result.add(item);

		return result;
	}

	private void toggleRowHeight() {
		if (treeView_.getRowHeight().equals(new WLength(30)))
			treeView_.setRowHeight(new WLength(25));
		else
			treeView_.setRowHeight(new WLength(30));
	}

	private void toggleStripes() {
		treeView_.setAlternatingRowColors(!treeView_.hasAlternatingRowColors());
	}

	private void toggleRoot() {
		if (treeView_.getRootIndex() == null)
			treeView_.setRootIndex(model_.getIndex(0, 0));
		else
			treeView_.setRootIndex(null);
	}

	private void addRows() {
		for (int i = 0; i < 5; ++i) {
			String cityName = "City " + (belgium_.getRowCount() + 1);

			belgium_.appendRow(cityItems(cityName, WeatherIcon.Storm, "Juice",
					false));
		}
	}
}
