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

class BasicControls extends ControlsWidget {
	public BasicControls(EventDisplayer ed) {
		super(ed, true);
		new WText(tr("basics-intro"), this);
	}

	public void populateSubMenu(WMenu menu) {
		menu.addItem("WContainerWidget", this.wContainerWidget());
		menu.addItem("WTemplate", this.wTemplate());
		menu.addItem("WText", this.wText());
		menu.addItem("WAnchor", this.wAnchor());
		menu.addItem("WBreak", this.wBreak());
		menu.addItem("WImage", this.wImage());
		menu.addItem("WGroupBox", this.wGroupBox());
		menu.addItem("WStackedWidget", this.wStackedWidget());
		menu.addItem("WTable", this.wTable());
		menu.addItem("WMenu", this.wMenu());
		menu.addItem("WTree", this.wTree());
		menu.addItem("WTreeTable", this.wTreeTable());
		menu.addItem("WPanel", this.wPanel());
		menu.addItem("WTabWidget", this.wTabWidget());
		menu.addItem("WProgressBar", this.wProgressBar());
	}

	private WWidget wText() {
		WContainerWidget result = new WContainerWidget();
		this.topic("WText", result);
		new WText(tr("basics-WText"), result);
		new WText(
				"<p>This WText unexpectedly contains JavaScript, wich the XSS attack preventer detects and disables. <script>alert(\"You are under attack\");</script>A warning is printed in Wt's log messages.</p>",
				result);
		new WText(
				"<p>This WText contains malformed XML <h1></h2>.It will be turned into a PlainText formatted string.</p>",
				result);
		new WText(tr("basics-WText-events"), result);
		WText text;
		text = new WText("This text reacts to <tt>clicked()</tt><br/>", result);
		text.setStyleClass("reactive");
		this.ed_.showSignal(text.clicked(), "Text was clicked");
		text = new WText("This text reacts to <tt>doubleClicked()</tt><br/>",
				result);
		text.setStyleClass("reactive");
		this.ed_.showSignal(text.doubleClicked(), "Text was double clicked");
		text = new WText("This text reacts to <tt>mouseWentOver()</tt><br/>",
				result);
		text.setStyleClass("reactive");
		this.ed_.showSignal(text.mouseWentOver(), "Mouse went over text");
		text = new WText("This text reacts to <tt>mouseWentOut()</tt><br/>",
				result);
		text.setStyleClass("reactive");
		this.ed_.showSignal(text.mouseWentOut(), "Mouse went out text");
		return result;
	}

	private WWidget wTemplate() {
		WContainerWidget result = new WContainerWidget();
		this.topic("WTemplate", result);
		new WText(tr("basics-WTemplate"), result);
		WTemplate pre = new WTemplate("<pre>${text}</pre>", result);
		pre.bindString("text", tr("basics-WTemplate-example"),
				TextFormat.PlainText);
		new WText(tr("basics-WTemplate2"), result);
		WTemplate temp = new WTemplate(tr("basics-WTemplate-example"), result);
		temp.bindWidget("name-edit", new WLineEdit());
		temp.bindWidget("save-button", new WPushButton("Save"));
		temp.bindWidget("cancel-button", new WPushButton("Cancel"));
		return result;
	}

	private WWidget wBreak() {
		WContainerWidget result = new WContainerWidget();
		this.topic("WBreak", result);
		new WText(tr("basics-WBreak"), result);
		new WBreak(result);
		return result;
	}

	private WWidget wAnchor() {
		WContainerWidget result = new WContainerWidget();
		this.topic("WAnchor", result);
		new WText(tr("basics-WAnchor"), result);
		WAnchor a1 = new WAnchor("http://www.webtoolkit.eu/",
				"Wt homepage (in a new window)", result);
		a1.setTarget(AnchorTarget.TargetNewWindow);
		new WText(tr("basics-WAnchor-more"), result);
		WAnchor a2 = new WAnchor("http://www.webtoolkit.eu/", result);
		a2.setTarget(AnchorTarget.TargetNewWindow);
		new WImage("icons/wt_powered.jpg", a2);
		new WText(tr("basics-WAnchor-related"), result);
		return result;
	}

	private WWidget wImage() {
		WContainerWidget result = new WContainerWidget();
		this.topic("WImage", result);
		new WText(tr("basics-WImage"), result);
		new WText("An image: ", result);
		new WImage("icons/wt_powered.jpg", result);
		new WText(tr("basics-WImage-more"), result);
		return result;
	}

	private WWidget wTable() {
		WContainerWidget result = new WContainerWidget();
		this.topic("WTable", result);
		new WText(tr("basics-WTable"), result);
		WTable table = new WTable(result);
		table.setStyleClass("example-table");
		new WText("First warning signal", table.getElementAt(0, 0));
		new WText("09:25am", table.getElementAt(0, 1));
		WImage img = new WImage("icons/Pennant_One.png", table.getElementAt(0,
				2));
		img.resize(WLength.Auto, new WLength(30, WLength.Unit.Pixel));
		new WText("First perparatory signal", table.getElementAt(1, 0));
		new WText("09:26am", table.getElementAt(1, 1));
		img = new WImage("icons/Pennant_One.png", table.getElementAt(1, 2));
		img.resize(WLength.Auto, new WLength(30, WLength.Unit.Pixel));
		img = new WImage("icons/Papa.png", table.getElementAt(1, 2));
		img.resize(WLength.Auto, new WLength(30, WLength.Unit.Pixel));
		new WText("Second perparatory signal", table.getElementAt(2, 0));
		new WText("09:29am", table.getElementAt(2, 1));
		img = new WImage("icons/Pennant_One.png", table.getElementAt(2, 2));
		img.resize(WLength.Auto, new WLength(30, WLength.Unit.Pixel));
		new WText("Start", table.getElementAt(3, 0));
		new WText("09:30am", table.getElementAt(3, 1));
		new WText(tr("basics-WTable-more"), result);
		return result;
	}

	private WWidget wTree() {
		WContainerWidget result = new WContainerWidget();
		this.topic("WTree", "WTreeNode", result);
		new WText(tr("basics-WTree"), result);
		WIconPair folderIcon = new WIconPair("icons/yellow-folder-closed.png",
				"icons/yellow-folder-open.png", false);
		WTree tree = new WTree(result);
		tree.setSelectionMode(SelectionMode.ExtendedSelection);
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
		new WText(tr("basics-WTree-more"), result);
		return result;
	}

	private WWidget wTreeTable() {
		WContainerWidget result = new WContainerWidget();
		this.topic("WTreeTable", "WTreeTableNode", result);
		new WText(tr("basics-WTreeTable"), result);
		WTreeTable tt = new WTreeTable(result);
		tt.resize(new WLength(650), new WLength(200));
		tt.getTree().setSelectionMode(SelectionMode.ExtendedSelection);
		tt.addColumn("Yuppie Factor", new WLength(125));
		tt.addColumn("# Holidays", new WLength(125));
		tt.addColumn("Favorite Item", new WLength(125));
		WTreeTableNode ttr = new WTreeTableNode("All Personnel");
		ttr.setImagePack("resources/");
		tt.setTreeRoot(ttr, "Emweb Organigram");
		WTreeTableNode ttr1 = new WTreeTableNode("Upper Management",
				(WIconPair) null, ttr);
		WTreeTableNode ttn;
		ttn = new WTreeTableNode("Chief Anything Officer", (WIconPair) null,
				ttr1);
		ttn.setColumnWidget(1, new WText("-2.8"));
		ttn.setColumnWidget(2, new WText("20"));
		ttn.setColumnWidget(3, new WText("Scepter"));
		ttn = new WTreeTableNode("Vice President of Parties", (WIconPair) null,
				ttr1);
		ttn.setColumnWidget(1, new WText("13.57"));
		ttn.setColumnWidget(2, new WText("365"));
		ttn.setColumnWidget(3, new WText("Flag"));
		ttn = new WTreeTableNode("Vice President of Staplery",
				(WIconPair) null, ttr1);
		ttn.setColumnWidget(1, new WText("3.42"));
		ttn.setColumnWidget(2, new WText("27"));
		ttn.setColumnWidget(3, new WText("Perforator"));
		ttr1 = new WTreeTableNode("Middle management", (WIconPair) null, ttr);
		ttn = new WTreeTableNode("Boss of the house", (WIconPair) null, ttr1);
		ttn.setColumnWidget(1, new WText("9.78"));
		ttn.setColumnWidget(2, new WText("35"));
		ttn.setColumnWidget(3, new WText("Happy Animals"));
		ttn = new WTreeTableNode("Xena caretaker", (WIconPair) null, ttr1);
		ttn.setColumnWidget(1, new WText("8.66"));
		ttn.setColumnWidget(2, new WText("10"));
		ttn.setColumnWidget(3, new WText("Yellow bag"));
		ttr1 = new WTreeTableNode("Actual Workforce", (WIconPair) null, ttr);
		ttn = new WTreeTableNode("The Dork", (WIconPair) null, ttr1);
		ttn.setColumnWidget(1, new WText("9.78"));
		ttn.setColumnWidget(2, new WText("22"));
		ttn.setColumnWidget(3, new WText("Mojito"));
		ttn = new WTreeTableNode("The Stud", (WIconPair) null, ttr1);
		ttn.setColumnWidget(1, new WText("8.66"));
		ttn.setColumnWidget(2, new WText("46"));
		ttn.setColumnWidget(3, new WText("Toothbrush"));
		ttn = new WTreeTableNode("The Ugly", (WIconPair) null, ttr1);
		ttn.setColumnWidget(1, new WText("13.0"));
		ttn.setColumnWidget(2, new WText("25"));
		ttn.setColumnWidget(3, new WText("Paper bag"));
		ttr.expand();
		return result;
	}

	private WWidget wPanel() {
		WContainerWidget result = new WContainerWidget();
		this.topic("WPanel", result);
		new WText(tr("basics-WPanel"), result);
		WPanel panel = new WPanel(result);
		panel.setCentralWidget(new WText("This is a default panel"));
		new WBreak(result);
		panel = new WPanel(result);
		panel.setTitle("My second WPanel.");
		panel.setCentralWidget(new WText("This is a panel with a title"));
		new WBreak(result);
		panel = new WPanel(result);
		panel.setTitle("My third WPanel");
		panel.setCentralWidget(new WText(
				"This is a collapsible panel with a title"));
		panel.setCollapsible(true);
		new WText(tr("basics-WPanel-related"), result);
		return result;
	}

	private WWidget wTabWidget() {
		WContainerWidget result = new WContainerWidget();
		this.topic("WTabWidget", result);
		new WText(tr("basics-WTabWidget"), result);
		WTabWidget tw = new WTabWidget(result);
		tw.addTab(new WText("These are the contents of the first tab"),
				"Picadilly", WTabWidget.LoadPolicy.PreLoading);
		tw
				.addTab(
						new WText(
								"The contents of these tabs are pre-loaded in the browser to ensure swift switching."),
						"Waterloo", WTabWidget.LoadPolicy.PreLoading);
		tw
				.addTab(
						new WText(
								"This is yet another pre-loaded tab. Look how good this works."),
						"Victoria", WTabWidget.LoadPolicy.PreLoading);
		tw
				.addTab(
						new WText(
								"The colors of the tab widget can be changed by modifying some images."),
						"Tottenham");
		tw.setStyleClass("tabwidget");
		new WText(tr("basics-WTabWidget-more"), result);
		return result;
	}

	private WWidget wContainerWidget() {
		WContainerWidget result = new WContainerWidget();
		this.topic("WContainerWidget", result);
		new WText(tr("basics-WContainerWidget"), result);
		return result;
	}

	private WWidget wMenu() {
		WContainerWidget result = new WContainerWidget();
		this.topic("WMenu", result);
		new WText(tr("basics-WMenu"), result);
		return result;
	}

	private WWidget wGroupBox() {
		WContainerWidget result = new WContainerWidget();
		this.topic("WGroupBox", result);
		new WText(tr("basics-WGroupBox"), result);
		WGroupBox gb = new WGroupBox("A group box", result);
		gb.addWidget(new WText(tr("basics-WGroupBox-contents")));
		new WText(tr("basics-WGroupBox-related"), result);
		return result;
	}

	private WWidget wStackedWidget() {
		WContainerWidget result = new WContainerWidget();
		this.topic("WStackedWidget", result);
		new WText(tr("basics-WStackedWidget"), result);
		return result;
	}

	private WWidget wProgressBar() {
		WContainerWidget result = new WContainerWidget();
		this.topic("WProgressBar", result);
		result.addWidget(new WText(tr("basics-WProgressBar")));
		WProgressBar pb = new WProgressBar(result);
		pb.setValue(27);
		return result;
	}
}
