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
import eu.webtoolkit.jwt.examples.dragdrop.*;

class EventsDemo extends ControlsWidget {
	private static Logger logger = LoggerFactory.getLogger(EventsDemo.class);

	public EventsDemo(EventDisplayer ed) {
		super(ed, true);
		this.lastKeyType_ = "";
		this.keyEventRepeatCounter_ = 0;
		addText(tr("events-intro"), this);
	}

	public void populateSubMenu(WMenu menu) {
		menu.addItem("WKeyEvent", this.wKeyEvent());
		menu.addItem("WMouseEvent", this.wMouseEvent());
		menu.addItem("WDropEvent", this.wDropEvent());
	}

	private WWidget wKeyEvent() {
		WContainerWidget result = new WContainerWidget();
		this.topic("WKeyEvent", result);
		addText(tr("events-WKeyEvent-1"), result);
		WLineEdit l = new WLineEdit(result);
		l.setTextSize(50);
		l.keyWentUp().addListener(this, new Signal1.Listener<WKeyEvent>() {
			public void trigger(WKeyEvent e1) {
				EventsDemo.this.showKeyWentUp(e1);
			}
		});
		l.keyWentDown().addListener(this, new Signal1.Listener<WKeyEvent>() {
			public void trigger(WKeyEvent e1) {
				EventsDemo.this.showKeyWentDown(e1);
			}
		});
		addText(tr("events-WKeyEvent-2"), result);
		l = new WLineEdit(result);
		l.setTextSize(50);
		l.keyPressed().addListener(this, new Signal1.Listener<WKeyEvent>() {
			public void trigger(WKeyEvent e1) {
				EventsDemo.this.showKeyPressed(e1);
			}
		});
		addText(tr("events-WKeyEvent-3"), result);
		l = new WLineEdit(result);
		l.setTextSize(50);
		l.enterPressed().addListener(this, new Signal.Listener() {
			public void trigger() {
				EventsDemo.this.showEnterPressed();
			}
		});
		l.escapePressed().addListener(this, new Signal.Listener() {
			public void trigger() {
				EventsDemo.this.showEscapePressed();
			}
		});
		new WBreak(result);
		addText("Last event: ", result);
		this.keyEventType_ = new WText(result);
		new WBreak(result);
		this.keyEventDescription_ = new WText(result);
		return result;
	}

	private WWidget wMouseEvent() {
		WContainerWidget result = new WContainerWidget();
		this.topic("WMouseEvent", result);
		addText(tr("events-WMouseEvent"), result);
		WContainerWidget c = new WContainerWidget(result);
		WHBoxLayout hlayout = new WHBoxLayout();
		c.setLayout(hlayout);
		WContainerWidget l = new WContainerWidget();
		WContainerWidget r = new WContainerWidget();
		new WText(
				"clicked<br/>doubleClicked<br/>mouseWentOut<br/>mouseWentOver",
				l);
		new WText(
				"mouseWentDown<br/>mouseWentUp<br/>mouseMoved<br/>mouseWheel",
				r);
		hlayout.addWidget(l);
		hlayout.addWidget(r);
		c.resize(new WLength(600), new WLength(300));
		l.getDecorationStyle().setBackgroundColor(WColor.gray);
		r.getDecorationStyle().setBackgroundColor(WColor.gray);
		l.setStyleClass("unselectable");
		r.setStyleClass("unselectable");
		l.clicked().addListener(this, new Signal1.Listener<WMouseEvent>() {
			public void trigger(WMouseEvent e1) {
				EventsDemo.this.showClicked(e1);
			}
		});
		l.doubleClicked().addListener(this,
				new Signal1.Listener<WMouseEvent>() {
					public void trigger(WMouseEvent e1) {
						EventsDemo.this.showDoubleClicked(e1);
					}
				});
		l.mouseWentOut().addListener(this, new Signal1.Listener<WMouseEvent>() {
			public void trigger(WMouseEvent e1) {
				EventsDemo.this.showMouseWentOut(e1);
			}
		});
		l.mouseWentOver().addListener(this,
				new Signal1.Listener<WMouseEvent>() {
					public void trigger(WMouseEvent e1) {
						EventsDemo.this.showMouseWentOver(e1);
					}
				});
		r.mouseMoved().addListener(this, new Signal1.Listener<WMouseEvent>() {
			public void trigger(WMouseEvent e1) {
				EventsDemo.this.showMouseMoved(e1);
			}
		});
		r.mouseWentUp().addListener(this, new Signal1.Listener<WMouseEvent>() {
			public void trigger(WMouseEvent e1) {
				EventsDemo.this.showMouseWentUp(e1);
			}
		});
		r.mouseWentDown().addListener(this,
				new Signal1.Listener<WMouseEvent>() {
					public void trigger(WMouseEvent e1) {
						EventsDemo.this.showMouseWentDown(e1);
					}
				});
		r.mouseWheel().addListener(this, new Signal1.Listener<WMouseEvent>() {
			public void trigger(WMouseEvent e1) {
				EventsDemo.this.showMouseWheel(e1);
			}
		});
		r.mouseWheel().preventDefaultAction(true);
		l
				.setAttributeValue("oncontextmenu",
						"event.cancelBubble = true; event.returnValue = false; return false;");
		r
				.setAttributeValue("oncontextmenu",
						"event.cancelBubble = true; event.returnValue = false; return false;");
		new WBreak(result);
		new WText("Last event: ", result);
		this.mouseEventType_ = new WText(result);
		new WBreak(result);
		this.mouseEventDescription_ = new WText(result);
		return result;
	}

	private WWidget wDropEvent() {
		WContainerWidget result = new WContainerWidget();
		this.topic("WDropEvent", result);
		addText(tr("events-WDropEvent"), result);
		new DragExample(result);
		return result;
	}

	private void showKeyWentUp(WKeyEvent e) {
		this.setKeyType("keyWentUp", e);
	}

	private void showKeyWentDown(WKeyEvent e) {
		this.setKeyType("keyWentDown", e);
	}

	private void showKeyPressed(WKeyEvent e) {
		this.setKeyType("keyPressed", e);
	}

	private void showEnterPressed() {
		this.setKeyType("enterPressed");
	}

	private void showEscapePressed() {
		this.setKeyType("escapePressed");
	}

	private void describe(WKeyEvent e) {
		try {
			StringWriter ss = new StringWriter();
			append(ss.append("Key: "), e.getKey()).append("<br/>").append(
					"Modifiers: ").append(modifiersToString(e.getModifiers()))
					.append("<br/>");
			int charCode = (int) e.getCharCode();
			if (charCode != 0) {
				ss.append("Char code: ").append(String.valueOf(charCode))
						.append("<br/>").append("text: ").append(
								Utils.htmlEncode(e.getText())).append("<br/>");
			}
			this.keyEventDescription_.setText(ss.toString());
		} catch (IOException ieo) {
			ieo.printStackTrace();
		}
	}

	private void setKeyType(String type, WKeyEvent e) {
		String repeatString = "";
		if (this.lastKeyType_.equals(type)) {
			this.keyEventRepeatCounter_++;
			repeatString = " (" + String.valueOf(this.keyEventRepeatCounter_)
					+ " times)";
		} else {
			this.lastKeyType_ = type;
			this.keyEventRepeatCounter_ = 0;
		}
		this.keyEventType_.setText(type + repeatString);
		if (e != null) {
			this.describe(e);
		} else {
			this.keyEventDescription_.setText("");
		}
	}

	private final void setKeyType(String type) {
		setKeyType(type, (WKeyEvent) null);
	}

	private WText keyEventType_;
	private WText keyEventDescription_;
	private String lastKeyType_;
	private int keyEventRepeatCounter_;

	private void showClicked(WMouseEvent e) {
		this.mouseEventType_.setText("clicked");
		this.describe(e);
	}

	private void showDoubleClicked(WMouseEvent e) {
		this.mouseEventType_.setText("doubleClicked");
		this.describe(e);
	}

	private void showMouseWentOut(WMouseEvent e) {
		this.mouseEventType_.setText("mouseWentOut");
		this.describe(e);
	}

	private void showMouseWentOver(WMouseEvent e) {
		this.mouseEventType_.setText("mouseWentOver");
		this.describe(e);
	}

	private void showMouseMoved(WMouseEvent e) {
		this.mouseEventType_.setText("mouseMoved");
		this.describe(e);
	}

	private void showMouseWentUp(WMouseEvent e) {
		this.mouseEventType_.setText("mouseWentUp");
		this.describe(e);
	}

	private void showMouseWentDown(WMouseEvent e) {
		this.mouseEventType_.setText("mouseWentDown");
		this.describe(e);
	}

	private void showMouseWheel(WMouseEvent e) {
		this.mouseEventType_.setText("mouseWheel");
		this.describe(e);
	}

	private WText mouseEventType_;
	private WText mouseEventDescription_;

	private void describe(WMouseEvent e) {
		try {
			StringWriter ss = new StringWriter();
			append(
					append(
							append(
									append(
											append(
													append(
															ss
																	.append("Button: "),
															e.getButton())
															.append("<br/>")
															.append(
																	"Modifiers: ")
															.append(
																	modifiersToString(e
																			.getModifiers()))
															.append("<br/>")
															.append(
																	"Document coordinates: "),
													e.getDocument()).append(
													"<br/>").append(
													"Window coordinates: "),
											e.getWindow()).append("<br/>")
											.append("Screen coordinates: "),
									e.getScreen()).append("<br/>").append(
									"Widget coordinates: "), e.getWidget())
							.append("<br/>").append("DragDelta coordinates: "),
					e.getDragDelta()).append("<br/>").append("Wheel delta: ")
					.append(String.valueOf(e.getWheelDelta())).append("<br/>");
			this.mouseEventDescription_.setText(ss.toString());
		} catch (IOException ieo) {
			ieo.printStackTrace();
		}
	}

	static Writer append(Writer o, WMouseEvent.Button b) {
		try {
			switch (b) {
			case NoButton:
				return o.append("No button");
			case LeftButton:
				return o.append("LeftButton");
			case RightButton:
				return o.append("RightButton");
			case MiddleButton:
				return o.append("MiddleButton");
			default:
				return o.append("Unknown Button");
			}
		} catch (IOException ieo) {
			ieo.printStackTrace();
			return null;
		}
	}

	static Writer append(Writer o, Key k) {
		try {
			switch (k) {
			default:
			case Key_unknown:
				return o.append("Key_unknown");
			case Key_Enter:
				return o.append("Key_Enter");
			case Key_Tab:
				return o.append("Key_Tab");
			case Key_Backspace:
				return o.append("Key_Backspace");
			case Key_Shift:
				return o.append("Key_Shift");
			case Key_Control:
				return o.append("Key_Control");
			case Key_Alt:
				return o.append("Key_Alt");
			case Key_PageUp:
				return o.append("Key_PageUp");
			case Key_PageDown:
				return o.append("Key_PageDown");
			case Key_End:
				return o.append("Key_End");
			case Key_Home:
				return o.append("Key_Home");
			case Key_Left:
				return o.append("Key_Left");
			case Key_Up:
				return o.append("Key_Up");
			case Key_Right:
				return o.append("Key_Right");
			case Key_Down:
				return o.append("Key_Down");
			case Key_Insert:
				return o.append("Key_Insert");
			case Key_Delete:
				return o.append("Key_Delete");
			case Key_Escape:
				return o.append("Key_Escape");
			case Key_F1:
				return o.append("Key_F1");
			case Key_F2:
				return o.append("Key_F2");
			case Key_F3:
				return o.append("Key_F3");
			case Key_F4:
				return o.append("Key_F4");
			case Key_F5:
				return o.append("Key_F5");
			case Key_F6:
				return o.append("Key_F6");
			case Key_F7:
				return o.append("Key_F7");
			case Key_F8:
				return o.append("Key_F8");
			case Key_F9:
				return o.append("Key_F9");
			case Key_F10:
				return o.append("Key_F10");
			case Key_F11:
				return o.append("Key_F11");
			case Key_F12:
				return o.append("Key_F12");
			case Key_Space:
				return o.append("Key_Space");
			case Key_A:
				return o.append("Key_A");
			case Key_B:
				return o.append("Key_B");
			case Key_C:
				return o.append("Key_C");
			case Key_D:
				return o.append("Key_D");
			case Key_E:
				return o.append("Key_E");
			case Key_F:
				return o.append("Key_F");
			case Key_G:
				return o.append("Key_G");
			case Key_H:
				return o.append("Key_H");
			case Key_I:
				return o.append("Key_I");
			case Key_J:
				return o.append("Key_J");
			case Key_K:
				return o.append("Key_K");
			case Key_L:
				return o.append("Key_L");
			case Key_M:
				return o.append("Key_M");
			case Key_N:
				return o.append("Key_N");
			case Key_O:
				return o.append("Key_O");
			case Key_P:
				return o.append("Key_P");
			case Key_Q:
				return o.append("Key_Q");
			case Key_R:
				return o.append("Key_R");
			case Key_S:
				return o.append("Key_S");
			case Key_T:
				return o.append("Key_T");
			case Key_U:
				return o.append("Key_U");
			case Key_V:
				return o.append("Key_V");
			case Key_W:
				return o.append("Key_W");
			case Key_X:
				return o.append("Key_X");
			case Key_Y:
				return o.append("Key_Y");
			case Key_Z:
				return o.append("Key_Z");
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
			return null;
		}
	}

	static Writer append(Writer o, Coordinates c) {
		try {
			return o.append(String.valueOf(c.x)).append(", ").append(
					String.valueOf(c.y));
		} catch (IOException ieo) {
			ieo.printStackTrace();
			return null;
		}
	}

	static String modifiersToString(EnumSet<KeyboardModifier> modifiers) {
		StringWriter o = new StringWriter();
		if (!EnumUtils.mask(modifiers, KeyboardModifier.ShiftModifier)
				.isEmpty()) {
			o.append("Shift ");
		}
		if (!EnumUtils.mask(modifiers, KeyboardModifier.ControlModifier)
				.isEmpty()) {
			o.append("Control ");
		}
		if (!EnumUtils.mask(modifiers, KeyboardModifier.AltModifier).isEmpty()) {
			o.append("Alt ");
		}
		if (!EnumUtils.mask(modifiers, KeyboardModifier.MetaModifier).isEmpty()) {
			o.append("Meta ");
		}
		if (modifiers.equals(0)) {
			o.append("No modifiers");
		}
		return o.toString();
	}

	private static final String modifiersToString(KeyboardModifier modifier,
			KeyboardModifier... modifiers) {
		return modifiersToString(EnumSet.of(modifier, modifiers));
	}
}
