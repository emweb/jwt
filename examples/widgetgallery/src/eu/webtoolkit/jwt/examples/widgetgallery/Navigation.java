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

class Navigation extends TopicWidget {
	private static Logger logger = LoggerFactory.getLogger(Navigation.class);

	public Navigation() {
		super();
		addText(tr("navigation-intro"), this);
	}

	public void populateSubMenu(WMenu menu) {
		menu.addItem("Internal paths", this.internalPaths()).setPathComponent(
				"");
		menu.addItem("Anchor", DeferredWidget.deferCreate(new WidgetCreator() {
			public WWidget create() {
				return Navigation.this.anchor();
			}
		}));
		menu.addItem("Stacked widget", DeferredWidget
				.deferCreate(new WidgetCreator() {
					public WWidget create() {
						return Navigation.this.stackedWidget();
					}
				}));
		menu.addItem("Menu", DeferredWidget.deferCreate(new WidgetCreator() {
			public WWidget create() {
				return Navigation.this.menuWidget();
			}
		}));
		menu.addItem("Tab widget", DeferredWidget
				.deferCreate(new WidgetCreator() {
					public WWidget create() {
						return Navigation.this.tabWidget();
					}
				}));
		menu.addItem("Navigation bar", DeferredWidget
				.deferCreate(new WidgetCreator() {
					public WWidget create() {
						return Navigation.this.navigationBar();
					}
				}));
		menu.addItem("Popup menu", DeferredWidget
				.deferCreate(new WidgetCreator() {
					public WWidget create() {
						return Navigation.this.popupMenu();
					}
				}));
		menu.addItem("Split button", DeferredWidget
				.deferCreate(new WidgetCreator() {
					public WWidget create() {
						return Navigation.this.splitButton();
					}
				}));
		menu.addItem("Toolbar", DeferredWidget.deferCreate(new WidgetCreator() {
			public WWidget create() {
				return Navigation.this.toolBar();
			}
		}));
	}

	private WWidget internalPaths() {
		WTemplate result = new TopicTemplate("navigation-internalPaths");
		result.bindWidget("Path", Path());
		result.bindWidget("PathChange", PathChange());
		return result;
	}

	private WWidget anchor() {
		WTemplate result = new TopicTemplate("navigation-anchor");
		result.bindWidget("Anchor", Anchor());
		result.bindWidget("AnchorImage", AnchorImage());
		return result;
	}

	private WWidget stackedWidget() {
		WTemplate result = new TopicTemplate("navigation-stackedWidget");
		result.bindWidget("Stack", Stack());
		return result;
	}

	private WWidget tabWidget() {
		WTemplate result = new TopicTemplate("navigation-tabWidget");
		result.bindWidget("Tab", Tab());
		return result;
	}

	private WWidget menuWidget() {
		WTemplate result = new TopicTemplate("navigation-menu");
		result.bindWidget("Menu", Menu());
		return result;
	}

	private WWidget navigationBar() {
		WTemplate result = new TopicTemplate("navigation-navigationBar");
		result.bindWidget("NavigationBar", NavigationBar());
		return result;
	}

	private WWidget popupMenu() {
		WTemplate result = new TopicTemplate("navigation-popupMenu");
		result.bindWidget("Popup", Popup());
		return result;
	}

	private WWidget splitButton() {
		WTemplate result = new TopicTemplate("navigation-splitButton");
		result.bindWidget("SplitButton", SplitButton());
		return result;
	}

	private WWidget toolBar() {
		WTemplate result = new TopicTemplate("navigation-toolBar");
		result.bindWidget("ToolBar", ToolBar());
		return result;
	}

	WWidget Path() {
		WPushButton button = new WPushButton("Next");
		button
				.setLink(new WLink(WLink.Type.InternalPath,
						"/navigation/anchor"));
		return button;
	}

	static final void handlePathChange(WText out) {
		WApplication app = WApplication.getInstance();
		if (app.getInternalPath().equals("/navigation/shop")) {
			out.setText("<p>Currently shopping.</p>");
		} else {
			if (app.getInternalPath().equals("/navigation/eat")) {
				out.setText("<p>Needed some food, eating now!</p>");
			} else {
				out.setText("<p><i>Idle.</i></p>");
			}
		}
	}

	WWidget PathChange() {
		WContainerWidget container = new WContainerWidget();
		new WAnchor(new WLink(WLink.Type.InternalPath, "/navigation/shop"),
				"Shop", container);
		new WText(" ", container);
		new WAnchor(new WLink(WLink.Type.InternalPath, "/navigation/eat"),
				"Eat", container);
		final WText out = new WText(container);
		out.setInline(false);
		WApplication app = WApplication.getInstance();
		app.internalPathChanged().addListener(this, new Signal.Listener() {
			public void trigger() {
				handlePathChange(out);
			}
		});
		handlePathChange(out);
		return container;
	}

	WWidget Anchor() {
		WAnchor anchor = new WAnchor(new WLink("http://www.webtoolkit.eu/"),
				"Wt homepage (in a new window)");
		anchor.setTarget(AnchorTarget.TargetNewWindow);
		return anchor;
	}

	WWidget AnchorImage() {
		WAnchor anchor = new WAnchor(new WLink("http://www.emweb.be/"));
		anchor.setTarget(AnchorTarget.TargetNewWindow);
		new WImage(new WLink("pics/emweb_small.jpg"), anchor);
		return anchor;
	}

	WWidget Stack() {
		WContainerWidget container = new WContainerWidget();
		final WSpinBox sb = new WSpinBox(container);
		sb.setRange(0, 2);
		final WStackedWidget stack = new WStackedWidget(container);
		stack.addWidget(new WText(
				"<strong>Stacked widget-index 0</strong><p>Hello</p>"));
		stack.addWidget(new WText(
				"<strong>Stacked widget-index 1</strong><p>This is Wt</p>"));
		stack
				.addWidget(new WText(
						"<strong>Stacked widget-index 2</strong><p>Do you like it?</p>"));
		sb.changed().addListener(this, new Signal.Listener() {
			public void trigger() {
				if (sb.validate() != null) {
					stack.setCurrentIndex(sb.getValue());
				}
			}
		});
		return container;
	}

	WWidget Menu() {
		WContainerWidget container = new WContainerWidget();
		WStackedWidget contents = new WStackedWidget();
		WMenu menu = new WMenu(contents, Orientation.Vertical, container);
		menu.setStyleClass("nav nav-pills nav-stacked");
		menu.setWidth(new WLength(150));
		menu
				.addItem("Internal paths", new WTextArea(
						"Internal paths contents"));
		menu.addItem("Anchor", new WTextArea("Anchor contents"));
		menu
				.addItem("Stacked widget", new WTextArea(
						"Stacked widget contents"));
		menu.addItem("Tab widget", new WTextArea("Tab widget contents"));
		menu.addItem("Menu", new WTextArea("Menu contents"));
		container.addWidget(contents);
		return container;
	}

	WWidget Tab() {
		WContainerWidget container = new WContainerWidget();
		WTabWidget tabW = new WTabWidget(container);
		tabW.addTab(new WTextArea("This is the contents of the first tab."),
				"First", WTabWidget.LoadPolicy.PreLoading);
		tabW
				.addTab(
						new WTextArea(
								"The contents of the tabs are pre-loaded in the browser to ensure swift switching."),
						"Preload", WTabWidget.LoadPolicy.PreLoading);
		tabW
				.addTab(
						new WTextArea(
								"You could change any other style attribute of the tab widget by modifying the style class. The style class 'trhead' is applied to this tab."),
						"Style", WTabWidget.LoadPolicy.PreLoading)
				.setStyleClass("trhead");
		WMenuItem tab = tabW.addTab(new WTextArea(
				"You can close this tab by clicking on the close icon."),
				"Close");
		tab.setCloseable(true);
		tabW.setStyleClass("tabwidget");
		return container;
	}

	WWidget NavigationBar() {
		WContainerWidget container = new WContainerWidget();
		WNavigationBar navigation = new WNavigationBar(container);
		navigation.setTitle("Corpy Inc.", new WLink(
				"http://www.google.com/search?q=corpy+inc"));
		navigation.setResponsive(true);
		WStackedWidget contentsStack = new WStackedWidget(container);
		contentsStack.addStyleClass("contents");
		final WMenu leftMenu = new WMenu(contentsStack, container);
		navigation.addMenu(leftMenu);
		final WText searchResult = new WText("Buy or Sell... Bye!");
		leftMenu.addItem("Home", new WText("There is no better place!"));
		leftMenu.addItem("Layout", new WText("Layout contents")).setLink(
				new WLink(WLink.Type.InternalPath, "/layout"));
		leftMenu.addItem("Sales", searchResult);
		WMenu rightMenu = new WMenu();
		navigation.addMenu(rightMenu, AlignmentFlag.AlignRight);
		WPopupMenu popup = new WPopupMenu();
		popup.addItem("Contents");
		popup.addItem("Index");
		popup.addSeparator();
		popup.addItem("About");
		popup.itemSelected().addListener(this,
				new Signal1.Listener<WMenuItem>() {
					public void trigger(WMenuItem item) {
						final WMessageBox messageBox = new WMessageBox("Help",
								new WString("<p>Showing Help: {1}</p>")
										.arg(item.getText()), Icon.Information,
								EnumSet.of(StandardButton.Ok));
						messageBox.buttonClicked().addListener(messageBox,
								new Signal.Listener() {
									public void trigger() {
										if (messageBox != null)
											messageBox.remove();
									}
								});
						messageBox.show();
					}
				});
		WMenuItem item = new WMenuItem("Help");
		item.setMenu(popup);
		rightMenu.addItem(item);
		final WLineEdit edit = new WLineEdit();
		edit.setEmptyText("Enter a search item");
		edit.enterPressed().addListener(this, new Signal.Listener() {
			public void trigger() {
				leftMenu.select(2);
				searchResult.setText(new WString("Nothing found for {1}.")
						.arg(edit.getText()));
			}
		});
		navigation.addSearch(edit, AlignmentFlag.AlignRight);
		container.addWidget(contentsStack);
		return container;
	}

	WWidget Popup() {
		WContainerWidget container = new WContainerWidget();
		WPopupMenu popup = new WPopupMenu();
		final WText status = new WText();
		status.setMargin(new WLength(10), EnumSet.of(Side.Left, Side.Right));
		final WText out = new WText();
		popup.addItem("Connect").triggered().addListener(this,
				new Signal.Listener() {
					public void trigger() {
						out.setText("<p>Connecting...</p>");
					}
				});
		popup.addItem("Disconnect").triggered().addListener(this,
				new Signal.Listener() {
					public void trigger() {
						out.setText("<p>You are disconnected now.</p>");
					}
				});
		popup.addSeparator();
		popup.addItem("icons/house.png", "I'm home").triggered().addListener(
				this, new Signal.Listener() {
					public void trigger() {
						out.setText("");
					}
				});
		final WMenuItem item = popup.addItem("Don't disturb");
		item.setCheckable(true);
		item.triggered().addListener(this, new Signal.Listener() {
			public void trigger() {
				out.setText(new WString("<p>{1} item is {2}.</p>").arg(
						item.getText()).arg(
						item.isChecked() ? "checked" : "unchecked"));
			}
		});
		popup.addSeparator();
		WPopupMenu subMenu = new WPopupMenu();
		subMenu.addItem("Contents").triggered().addListener(this,
				new Signal.Listener() {
					public void trigger() {
						out
								.setText("<p>This could be a link to /contents.html.</p>");
					}
				});
		subMenu.addItem("Index").triggered().addListener(this,
				new Signal.Listener() {
					public void trigger() {
						out
								.setText("<p>This could be a link to /index.html.</p>");
					}
				});
		subMenu.addSeparator();
		subMenu.addItem("About").triggered().addListener(this,
				new Signal.Listener() {
					public void trigger() {
						final WMessageBox messageBox = new WMessageBox(
								"About",
								"<p>This is a program to make connections.</p>",
								Icon.Information, EnumSet.of(StandardButton.Ok));
						messageBox.show();
						messageBox.buttonClicked().addListener(messageBox,
								new Signal.Listener() {
									public void trigger() {
										if (messageBox != null)
											messageBox.remove();
									}
								});
					}
				});
		popup.addMenu("Help", subMenu);
		WPushButton button = new WPushButton(container);
		button.setMenu(popup);
		popup.itemSelected().addListener(this,
				new Signal1.Listener<WMenuItem>() {
					public void trigger(WMenuItem item) {
						status.setText(new WString("Selected menu item: {1}.")
								.arg(item.getText()));
					}
				});
		container.addWidget(status);
		container.addWidget(out);
		return container;
	}

	WWidget SplitButton() {
		WContainerWidget container = new WContainerWidget();
		WSplitButton sb = new WSplitButton("Save", container);
		final WText out = new WText(container);
		out.setMargin(new WLength(10), EnumSet.of(Side.Left));
		WPopupMenu popup = new WPopupMenu();
		popup.addItem("Save As ...");
		popup.addItem("Save Template");
		sb.getDropDownButton().setMenu(popup);
		sb.getActionButton().clicked().addListener(this, new Signal.Listener() {
			public void trigger() {
				out.setText("Saved!");
			}
		});
		popup.itemSelected().addListener(this,
				new Signal1.Listener<WMenuItem>() {
					public void trigger(WMenuItem item) {
						out.setText(item.getText());
					}
				});
		return container;
	}

	WPushButton createColorButton(String className, final CharSequence text) {
		WPushButton button = new WPushButton();
		button.setTextFormat(TextFormat.XHTMLText);
		button.setText(text);
		button.addStyleClass(className);
		return button;
	}

	WWidget ToolBar() {
		WContainerWidget container = new WContainerWidget();
		List<WPushButton> colorButtons = new ArrayList<WPushButton>();
		WToolBar toolBar = new WToolBar(container);
		toolBar.addButton(createColorButton("btn-primary", "Primary"));
		toolBar.addButton(createColorButton("btn-danger", "Danger"));
		toolBar.addButton(createColorButton("btn-success", "Success"));
		toolBar.addButton(createColorButton("btn-warning", "Warning"));
		toolBar.addButton(createColorButton("btn-inverse", "Inverse"));
		toolBar.addButton(createColorButton("", "Default"));
		WPushButton resetButton = new WPushButton("Reset");
		toolBar.addSeparator();
		toolBar.addButton(resetButton);
		return container;
	}
}
