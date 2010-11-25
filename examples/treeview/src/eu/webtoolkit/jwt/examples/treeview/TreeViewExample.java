package eu.webtoolkit.jwt.examples.treeview;

import java.util.ArrayList;
import java.util.List;

import eu.webtoolkit.jwt.AlignmentFlag;
import eu.webtoolkit.jwt.Orientation;
import eu.webtoolkit.jwt.Signal1;
import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WLength;
import eu.webtoolkit.jwt.WMouseEvent;
import eu.webtoolkit.jwt.WObject;
import eu.webtoolkit.jwt.WPanel;
import eu.webtoolkit.jwt.WPushButton;
import eu.webtoolkit.jwt.WStandardItem;
import eu.webtoolkit.jwt.WStandardItemModel;
import eu.webtoolkit.jwt.WString;
import eu.webtoolkit.jwt.WText;
import eu.webtoolkit.jwt.WTreeView;

public class TreeViewExample extends WContainerWidget {
	private enum WeatherIcon {
		Sun("sun01.png"), 
		SunCloud("cloudy01.png"), 
		Cloud("w_cloud.png"), 
		Rain("rain.png"), 
		Storm("storm.png"), 
		Snow("snow.png");

		private String icon;

		private WeatherIcon(String icon) {
			this.icon = icon;
		}

		public String getIcon() {
			return icon;
		}
	}

	public TreeViewExample(WStandardItemModel model, CharSequence titleText) {
		super();
		this.model_ = model;
		this.belgium_ = this.model_.getItem(0, 0).getChild(0, 0);
		new WText(titleText, this);
		WPanel panel = new WPanel(this);
		panel.resize(new WLength(600), new WLength(300));
		panel.setCentralWidget(this.treeView_ = new WTreeView());
		if (!WApplication.getInstance().getEnvironment().hasAjax()) {
			this.treeView_.resize(WLength.Auto, new WLength(290));
		}
		this.treeView_.setAlternatingRowColors(true);
		this.treeView_.setRowHeight(new WLength(25));
		this.treeView_.setModel(this.model_);
		this.treeView_.setColumnWidth(1, new WLength(100));
		this.treeView_.setColumnAlignment(1, AlignmentFlag.AlignCenter);
		this.treeView_.setColumnWidth(3, new WLength(100));
		this.treeView_.setColumnAlignment(3, AlignmentFlag.AlignCenter);
		this.treeView_.setExpanded(model.getIndex(0, 0), true);
		this.treeView_.setExpanded(model.getIndex(0, 0, model.getIndex(0, 0)),
				true);
		WContainerWidget wc = new WContainerWidget(this);
		WPushButton b;
		b = new WPushButton("Toggle row height", wc);
		b.clicked().addListener(this, new Signal1.Listener<WMouseEvent>() {
			public void trigger(WMouseEvent e1) {
				TreeViewExample.this.toggleRowHeight();
			}
		});
		b.setToolTip("Toggles row height between 31px and 25px");
		b = new WPushButton("Toggle stripes", wc);
		b.clicked().addListener(this, new Signal1.Listener<WMouseEvent>() {
			public void trigger(WMouseEvent e1) {
				TreeViewExample.this.toggleStripes();
			}
		});
		b.setToolTip("Toggle alternating row colors");
		b = new WPushButton("Toggle root", wc);
		b.clicked().addListener(this, new Signal1.Listener<WMouseEvent>() {
			public void trigger(WMouseEvent e1) {
				TreeViewExample.this.toggleRoot();
			}
		});
		b.setToolTip("Toggles root item between all and the first continent.");
		b = new WPushButton("Add rows", wc);
		b.clicked().addListener(this, new Signal1.Listener<WMouseEvent>() {
			public void trigger(WMouseEvent e1) {
				TreeViewExample.this.addRows();
			}
		});
		b.setToolTip("Adds some cities to Belgium");
	}

	public WTreeView getTreeView() {
		return this.treeView_;
	}

	public static WStandardItemModel createModel(boolean useInternalPath,
			WObject parent) {
		WStandardItemModel result = new WStandardItemModel(0, 4, parent);
		result.setHeaderData(0, Orientation.Horizontal, "Places");
		result.setHeaderData(1, Orientation.Horizontal, "Weather");
		result.setHeaderData(2, Orientation.Horizontal, "Drink");
		result.setHeaderData(3, Orientation.Horizontal, "Visited");
		WStandardItem continent;
		WStandardItem country;
		result.appendRow(continent = continentItem("Europe"));
		continent.appendRow(country = countryItem("Belgium", "be"));
		country.appendRow(cityItems("Brussels", WeatherIcon.Rain, "Beer",
				useInternalPath, true));
		country.appendRow(cityItems("Leuven", WeatherIcon.Rain, "Beer",
				useInternalPath, true));
		continent.appendRow(country = countryItem("France", "fr"));
		country.appendRow(cityItems("Paris", WeatherIcon.Cloud, "Wine",
				useInternalPath, true));
		country.appendRow(cityItems("Bordeaux", WeatherIcon.SunCloud,
				"Bordeaux wine", useInternalPath, false));
		continent.appendRow(country = countryItem("Spain", "sp"));
		country.appendRow(cityItems("Barcelona", WeatherIcon.Sun, "Cava",
				useInternalPath, true));
		country.appendRow(cityItems("Madrid", WeatherIcon.Sun, "San Miguel",
				useInternalPath, false));
		result.appendRow(continent = continentItem("Africa"));
		continent.appendRow(country = countryItem("Morocco (المغرب)", "ma"));
		country.appendRow(cityItems("Casablanca", WeatherIcon.Sun, "Tea",
				useInternalPath, false));
		return result;
	}

	private WStandardItem belgium_;
	private WStandardItemModel model_;
	private WTreeView treeView_;

	private static WStandardItem continentItem(String continent) {
		return new WStandardItem(continent);
	}

	private static WStandardItem countryItem(String country, String code) {
		WStandardItem result = new WStandardItem(new WString(country));
		result.setIcon("icons/flag_" + code + ".png");
		return result;
	}

	private static List<WStandardItem> cityItems(String city,
			WeatherIcon weather, String drink, boolean useInternalPath,
			boolean visited) {
		List<WStandardItem> result = new ArrayList<WStandardItem>();
		WStandardItem item;
		item = new WStandardItem(new WString(city));
		result.add(item);
		item = new WStandardItem();
		item.setIcon("icons/" + weather.getIcon());
		result.add(item);
		item = new WStandardItem(drink);
		if (useInternalPath) {
			item.setInternalPath("/drinks/" + drink);
		}
		result.add(item);
		item = new WStandardItem();
		item.setCheckable(true);
		item.setChecked(visited);
		result.add(item);
		return result;
	}

	private void toggleRowHeight() {
		if (this.treeView_.getRowHeight().equals(new WLength(31))) {
			this.treeView_.setRowHeight(new WLength(25));
		} else {
			this.treeView_.setRowHeight(new WLength(31));
		}
	}

	private void toggleStripes() {
		this.treeView_.setAlternatingRowColors(!this.treeView_
				.hasAlternatingRowColors());
	}

	private void toggleRoot() {
		if ((this.treeView_.getRootIndex() == null || (this.treeView_
				.getRootIndex() != null && this.treeView_.getRootIndex()
				.equals(null)))) {
			this.treeView_.setRootIndex(this.model_.getIndex(0, 0));
		} else {
			this.treeView_.setRootIndex(null);
		}
	}

	private void addRows() {
		for (int i = 0; i < 5; ++i) {
			String cityName = "City "
					+ String.valueOf(this.belgium_.getRowCount() + 1);
			boolean useInternalPath = false;
			this.belgium_.appendRow(cityItems(cityName, WeatherIcon.Storm,
					"Juice", useInternalPath, false));
		}
	}
}
