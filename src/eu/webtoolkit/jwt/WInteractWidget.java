/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * An abstract widget that can receive user-interface interaction
 * <p>
 * 
 * This abstract widget provides access to event signals that correspond to
 * user-interface interaction through mouse or keyboard.
 * <p>
 * When JavaScript is disabled, only the {@link WInteractWidget#clicked()
 * clicked()} event will propagate (but without event details information).
 */
public abstract class WInteractWidget extends WWebWidget {
	/**
	 * Create an InteractWidget with optional parent.
	 */
	public WInteractWidget(WContainerWidget parent) {
		super(parent);
		this.dragSlot_ = null;
	}

	/**
	 * Create an InteractWidget with optional parent.
	 * <p>
	 * Calls {@link #WInteractWidget(WContainerWidget parent)
	 * this((WContainerWidget)null)}
	 */
	public WInteractWidget() {
		this((WContainerWidget) null);
	}

	public void remove() {
		/* delete this.dragSlot_ */;
		super.remove();
	}

	/**
	 * Event signal emitted when a keyboard key is pushed down.
	 * <p>
	 * The keyWentDown signal is the first signal emitted when a key is pressed
	 * (before the keyPressed signal). Unlike
	 * {@link WInteractWidget#keyPressed() keyPressed()} however it is also
	 * emitted for modifier keys (such as &quot;shift&quot;,
	 * &quot;control&quot;, ...) or keyboard navigation keys that do not have a
	 * corresponding character.
	 * <p>
	 * 
	 * @see WInteractWidget#keyPressed()
	 * @see WInteractWidget#keyWentUp()
	 */
	public EventSignal1<WKeyEvent> keyWentDown() {
		return this.keyEventSignal(KEYDOWN_SIGNAL, true);
	}

	/**
	 * Event signal emitted when a &quot;character&quot; was entered.
	 * <p>
	 * The keyPressed signal is emitted when a key is pressed, and a character
	 * is entered. Unlike {@link WInteractWidget#keyWentDown() keyWentDown()},
	 * it is emitted only for key presses that result in a character being
	 * entered, and thus not for modifier keys or keyboard navigation keys.
	 * <p>
	 * 
	 * @see WInteractWidget#keyWentDown()
	 */
	public EventSignal1<WKeyEvent> keyPressed() {
		return this.keyEventSignal(KEYPRESS_SIGNAL, true);
	}

	/**
	 * Event signal emitted when a keyboard key is released.
	 * <p>
	 * This is the counter-part of the {@link WInteractWidget#keyWentDown()
	 * keyWentDown()} event. Every key-down has its corresponding key-up.
	 * <p>
	 * 
	 * @see WInteractWidget#keyWentDown()
	 */
	public EventSignal1<WKeyEvent> keyWentUp() {
		return this.keyEventSignal(KEYUP_SIGNAL, true);
	}

	/**
	 * Event signal emitted when enter was pressed.
	 * <p>
	 * This signal is emitted when the Enter or Return key was pressed.
	 * <p>
	 * 
	 * @see WInteractWidget#keyPressed()
	 * @see Key#Key_Enter
	 */
	public EventSignal enterPressed() {
		return this.voidEventSignal(ENTER_PRESS_SIGNAL, true);
	}

	/**
	 * Event signal emitted when escape was pressed.
	 * <p>
	 * This signal is emitted when the Escape key was pressed.
	 * <p>
	 * 
	 * @see WInteractWidget#keyPressed()
	 * @see Key#Key_Escape
	 */
	public EventSignal escapePressed() {
		return this.voidEventSignal(ESCAPE_PRESS_SIGNAL, true);
	}

	/**
	 * Event signal emitted when a mouse key was clicked on this widget.
	 * <p>
	 * The event details contains information such as the
	 * {@link WMouseEvent#getButton() button}, optional
	 * {@link WMouseEvent#getModifiers() keyboard modifiers}, and mouse
	 * coordinates relative to the {@link WMouseEvent#getWidget() widget}, the
	 * window {@link WMouseEvent#getWindow() window}, or the
	 * {@link WMouseEvent#getDocument() document}.
	 * <p>
	 * <p>
	 * <i><b>Note:</b>When JavaScript is disabled, the event details contain
	 * invalid information. </i>
	 * </p>
	 */
	public EventSignal1<WMouseEvent> clicked() {
		return this.mouseEventSignal(CLICK_SIGNAL, true);
	}

	/**
	 * Event signal emitted when a mouse key was double clicked on this widget.
	 * <p>
	 * The event details contains information such as the
	 * {@link WMouseEvent#getButton() button}, optional
	 * {@link WMouseEvent#getModifiers() keyboard modifiers}, and mouse
	 * coordinates relative to the {@link WMouseEvent#getWidget() widget}, the
	 * window {@link WMouseEvent#getWindow() window}, or the
	 * {@link WMouseEvent#getDocument() document}.
	 * <p>
	 * <p>
	 * <i><b>Note:</b>When JavaScript is disabled, the signal will never fire.
	 * </i>
	 * </p>
	 */
	public EventSignal1<WMouseEvent> doubleClicked() {
		return this.mouseEventSignal(DBL_CLICK_SIGNAL, true);
	}

	/**
	 * Event signal emitted when a mouse key was pushed down on this widget.
	 * <p>
	 * The event details contains information such as the
	 * {@link WMouseEvent#getButton() button}, optional
	 * {@link WMouseEvent#getModifiers() keyboard modifiers}, and mouse
	 * coordinates relative to the {@link WMouseEvent#getWidget() widget}, the
	 * window {@link WMouseEvent#getWindow() window}, or the
	 * {@link WMouseEvent#getDocument() document}.
	 * <p>
	 * <p>
	 * <i><b>Note:</b>When JavaScript is disabled, the signal will never fire.
	 * </i>
	 * </p>
	 */
	public EventSignal1<WMouseEvent> mouseWentDown() {
		return this.mouseEventSignal(MOUSE_DOWN_SIGNAL, true);
	}

	/**
	 * Event signal emitted when a mouse key was released on this widget.
	 * <p>
	 * The event details contains information such as the
	 * {@link WMouseEvent#getButton() button}, optional
	 * {@link WMouseEvent#getModifiers() keyboard modifiers}, and mouse
	 * coordinates relative to the {@link WMouseEvent#getWidget() widget}, the
	 * window {@link WMouseEvent#getWindow() window}, or the
	 * {@link WMouseEvent#getDocument() document}.
	 * <p>
	 * <p>
	 * <i><b>Note:</b>When JavaScript is disabled, the signal will never fire.
	 * </i>
	 * </p>
	 */
	public EventSignal1<WMouseEvent> mouseWentUp() {
		return this.mouseEventSignal(MOUSE_UP_SIGNAL, true);
	}

	/**
	 * Event signal emitted when the mouse went out of this widget.
	 * <p>
	 * <p>
	 * <i><b>Note:</b>When JavaScript is disabled, the signal will never fire.
	 * </i>
	 * </p>
	 */
	public EventSignal1<WMouseEvent> mouseWentOut() {
		return this.mouseEventSignal(MOUSE_OUT_SIGNAL, true);
	}

	/**
	 * Event signal emitted when the mouse entered this widget.
	 * <p>
	 * <p>
	 * <i><b>Note:</b>When JavaScript is disabled, the signal will never fire.
	 * </i>
	 * </p>
	 */
	public EventSignal1<WMouseEvent> mouseWentOver() {
		return this.mouseEventSignal(MOUSE_OVER_SIGNAL, true);
	}

	/**
	 * Event signal emitted when the mouse moved over this widget.
	 * <p>
	 * <p>
	 * <i><b>Note:</b>When JavaScript is disabled, the signal will never fire.
	 * </i>
	 * </p>
	 */
	public EventSignal1<WMouseEvent> mouseMoved() {
		return this.mouseEventSignal(MOUSE_MOVE_SIGNAL, true);
	}

	/**
	 * Configure dragging.
	 * <p>
	 * Enable drag&amp;drop for this widget. The mimeType is used to find a
	 * suitable drop target, which must accept dropping of this mimetype.
	 * <p>
	 * By default, the entire widget is dragged. One may specify another widget
	 * to be dragged (for example the parent), or a drag widget whose function
	 * is only to represent the drag visually (when isDragWidgetOnly = true).
	 * <p>
	 * The widget to be identified as source in the dropEvent may be given
	 * explicitly, and will default to this widget otherwise.
	 * <p>
	 * 
	 * @see WWidget#dropEvent(WDropEvent event)
	 * @see WWidget#acceptDrops(String mimeType, String hoverStyleClass)
	 * @see WDropEvent <p>
	 *      <i><b>Note:</b>When JavaScript is disabled, drag&amp;drop does not
	 *      work. </i>
	 *      </p>
	 */
	public void setDraggable(String mimeType, WWidget dragWidget,
			boolean isDragWidgetOnly, WObject sourceObject) {
		if (dragWidget == null) {
			dragWidget = this;
		}
		if (sourceObject == null) {
			sourceObject = this;
		}
		if (isDragWidgetOnly) {
			dragWidget.hide();
		}
		WApplication app = WApplication.getInstance();
		this.setAttributeValue("dmt", mimeType);
		this.setAttributeValue("dwid", dragWidget.getId());
		this.setAttributeValue("dsid", app.encodeObject(sourceObject));
		if (!(this.dragSlot_ != null)) {
			this.dragSlot_ = new JSlot();
			this.dragSlot_.setJavaScript("function(o,e){"
					+ app.getJavaScriptClass() + "._p_.dragStart(o,e);" + "}");
		}
		this.mouseWentDown().addListener(this.dragSlot_);
	}

	/**
	 * Configure dragging.
	 * <p>
	 * Calls
	 * {@link #setDraggable(String mimeType, WWidget dragWidget, boolean isDragWidgetOnly, WObject sourceObject)
	 * setDraggable(mimeType, (WWidget)null, false, (WObject)null)}
	 */
	public final void setDraggable(String mimeType) {
		setDraggable(mimeType, (WWidget) null, false, (WObject) null);
	}

	/**
	 * Configure dragging.
	 * <p>
	 * Calls
	 * {@link #setDraggable(String mimeType, WWidget dragWidget, boolean isDragWidgetOnly, WObject sourceObject)
	 * setDraggable(mimeType, dragWidget, false, (WObject)null)}
	 */
	public final void setDraggable(String mimeType, WWidget dragWidget) {
		setDraggable(mimeType, dragWidget, false, (WObject) null);
	}

	/**
	 * Configure dragging.
	 * <p>
	 * Calls
	 * {@link #setDraggable(String mimeType, WWidget dragWidget, boolean isDragWidgetOnly, WObject sourceObject)
	 * setDraggable(mimeType, dragWidget, isDragWidgetOnly, (WObject)null)}
	 */
	public final void setDraggable(String mimeType, WWidget dragWidget,
			boolean isDragWidgetOnly) {
		setDraggable(mimeType, dragWidget, isDragWidgetOnly, (WObject) null);
	}

	protected void updateDom(DomElement element, boolean all) {
		boolean updateKeyDown = false;
		EventSignal enterPress = this
				.voidEventSignal(ENTER_PRESS_SIGNAL, false);
		EventSignal escapePress = this.voidEventSignal(ESCAPE_PRESS_SIGNAL,
				false);
		EventSignal1<WKeyEvent> keyDown = this.keyEventSignal(KEYDOWN_SIGNAL,
				false);
		if (all) {
			updateKeyDown = enterPress != null && enterPress.isConnected()
					|| escapePress != null && escapePress.isConnected()
					|| keyDown != null && keyDown.isConnected();
		} else {
			updateKeyDown = enterPress != null && enterPress.needUpdate()
					|| escapePress != null && escapePress.needUpdate()
					|| keyDown != null && keyDown.needUpdate();
		}
		if (updateKeyDown) {
			List<DomElement.EventAction> actions = new ArrayList<DomElement.EventAction>();
			if (enterPress != null) {
				if (enterPress.isConnected()) {
					String extraJS = "";
					WEnvironment env = WApplication.getInstance()
							.getEnvironment();
					if (((this) instanceof WFormWidget ? (WFormWidget) (this)
							: null) != null
							&& !env.agentIsOpera() && !env.agentIsIE()) {
						extraJS = "var g=this.onchange;this.onchange=function(){this.onchange=g;};";
					}
					actions.add(new DomElement.EventAction(
							"(e.keyCode && e.keyCode == 13)", enterPress
									.getJavaScript()
									+ extraJS, enterPress.encodeCmd(),
							enterPress.isExposedSignal()));
				}
				enterPress.updateOk();
			}
			if (escapePress != null) {
				if (escapePress.isConnected()) {
					actions.add(new DomElement.EventAction(
							"(e.keyCode && e.keyCode == 27)", escapePress
									.getJavaScript(), escapePress.encodeCmd(),
							escapePress.isExposedSignal()));
				}
				escapePress.updateOk();
			}
			if (keyDown != null) {
				if (keyDown.isConnected()) {
					actions.add(new DomElement.EventAction("", keyDown
							.getJavaScript(), keyDown.encodeCmd(), keyDown
							.isExposedSignal()));
				}
				keyDown.updateOk();
			}
			if (!actions.isEmpty()) {
				element.setEvent("keydown", actions);
			} else {
				if (!all) {
					element.setEvent("keydown", "", "");
				}
			}
		}
		LinkedList<AbstractEventSignal> other = this.eventSignals();
		for (Iterator<AbstractEventSignal> i_it = other.iterator(); i_it
				.hasNext();) {
			AbstractEventSignal i = i_it.next();
			AbstractEventSignal s = i;
			this.updateSignalConnection(element, s, s.getName(), all);
			if (s.getName() == WInteractWidget.CLICK_SIGNAL
					&& this.flags_.get(BIT_REPAINT_TO_AJAX)) {
				WApplication.getInstance().doJavaScript(
						"Wt2_99_5.unwrap('" + this.getId() + "');");
			}
		}
		super.updateDom(element, all);
	}

	protected void propagateRenderOk(boolean deep) {
		LinkedList<AbstractEventSignal> other = this.eventSignals();
		for (Iterator<AbstractEventSignal> i_it = other.iterator(); i_it
				.hasNext();) {
			AbstractEventSignal i = i_it.next();
			AbstractEventSignal s = i;
			s.updateOk();
		}
		super.propagateRenderOk(deep);
	}

	protected JSlot dragSlot_;
	private static String KEYDOWN_SIGNAL = "M_keydown";
	static String KEYPRESS_SIGNAL = "keypress";
	private static String KEYUP_SIGNAL = "keyup";
	private static String ENTER_PRESS_SIGNAL = "M_enterpress";
	private static String ESCAPE_PRESS_SIGNAL = "M_escapepress";
	protected static String CLICK_SIGNAL = "click";
	private static String DBL_CLICK_SIGNAL = "dblclick";
	static String MOUSE_DOWN_SIGNAL = "mousedown";
	static String MOUSE_UP_SIGNAL = "mouseup";
	private static String MOUSE_OUT_SIGNAL = "mouseout";
	private static String MOUSE_OVER_SIGNAL = "mouseover";
	private static String MOUSE_MOVE_SIGNAL = "mousemove";
}
