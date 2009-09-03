/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;


/**
 * A slot that is only implemented in client side JavaScript code.
 * <p>
 * 
 * This class provides a hook for adding your own JavaScript to respond to
 * events.
 * <p>
 * Carefully consider the use of this. Not only is writing cross-browser
 * JavaScript hard and tedious, but one must also be aware of possible security
 * problems (see further), and ofcourse, the event handling will not be
 * available when JavaScript is disabled or not present at all.
 * <p>
 * For some purposes, stateless slot implementations are not sufficient, since
 * they do not allow state inspection. At the same time, the non-availability in
 * case of disabled JavaScript may also be fine for some non-essential
 * functionality (see for example the {@link WSuggestionPopup} widget), or when
 * you simply do not care. For these situations a JSlot can be used to add
 * client-side event handling.
 * <p>
 * The JavaScript code may be set (or changed) using the
 * {@link JSlot#setJavaScript(String js) setJavaScript() } method which takes a
 * string that implements a JavaScript function with the following signature:
 * <p>
 * <code>
 function(sender, event) { <br> 
   // handle the event, and sender is a reference to the DOM element <br> 
   // which captured the event (and holds the signal). Therefore it <br> 
   // equivalent to the sender for a normal %Wt slot. <br> 
 <br> 
   // You can prevent the default action using: <br> 
   ${WT_CLASS}.cancelEvent(event); <br> 
   // (where ${WT_CLASS} should be the value of the WT_CLASS define <br> 
 }
</code>
 * <p>
 * In the JavaScript code, you may use {@link WWidget#getJsRef() getJsRef() } to
 * obtain the DOM element corresponding to any {@link WWidget}, or
 * {@link WObject#getId() getId() } to obtain the DOM id. In addition you may
 * trigger server-side events using the JavaScript WtSignalEmit function (see
 * {@link JSignal} documentation). That&apos;s how far we can help you. For the
 * rest you are left to yourself, buggy browsers and quirky JavaScript (<a
 * href="http://www.quirksmode.org/">http://www.quirksmode.org/</a> was a
 * reliable companion to me) -- good luck.
 */
public class JSlot {
	/**
	 * Construct a JavaScript-only slot within the parent scope.
	 * <p>
	 * The JavaScript will reside within the scope of the given widget. By
	 * picking a long-lived parent, one may reuse a single block of JavasCript
	 * code for multiple widgets.
	 * <p>
	 * If parent = 0, then the JavaScript will be inline.
	 */
	public JSlot(WWidget parent) {
		this.widget_ = parent;
		this.fid_ = nextFid_++;
		this.create();
	}

	/**
	 * Construct a JavaScript-only slot within the parent scope.
	 * <p>
	 * Calls {@link #JSlot(WWidget parent) this((WWidget)null)}
	 */
	public JSlot() {
		this((WWidget) null);
	}

	/**
	 * Construct a JavaScript-only slot with given JavaScript.
	 * <p>
	 * 
	 * @see JSlot#JSlot(WWidget parent)
	 * @see JSlot#setJavaScript(String js)
	 */
	public JSlot(String javaScript, WWidget parent) {
		this.widget_ = parent;
		this.fid_ = nextFid_++;
		this.create();
		this.setJavaScript(javaScript);
	}

	/**
	 * Construct a JavaScript-only slot with given JavaScript.
	 * <p>
	 * Calls {@link #JSlot(String javaScript, WWidget parent) this(javaScript,
	 * (WWidget)null)}
	 */
	public JSlot(String javaScript) {
		this(javaScript, (WWidget) null);
	}

	/**
	 * Destructor.
	 */
	public void destroy() {
		/* delete this.imp_ */;
	}

	/**
	 * Set or modify the JavaScript code associated with the slot.
	 * <p>
	 * When the slot is triggered, the corresponding JavaScript is executed.
	 * <p>
	 * The JavaScript function takes two parameters and thus should look like:
	 * <code>
       function(obj, event) { <br> 
         // ... <br> 
       }
  </code>
	 * <p>
	 * The first parameter <i>obj</i> is a reference to the DOM element that
	 * generates the event. The <i>event</i> refers to the JavaScript event
	 * object.
	 * <p>
	 * 
	 * @see WWidget#getJsRef()
	 */
	public void setJavaScript(String js) {
		if (this.widget_ != null) {
			WApplication.getInstance().declareJavaScriptFunction(
					this.getJsFunctionName(), js);
		} else {
			this.imp_.setJavaScript("{var f=" + js + "; f(this, e);}");
		}
	}

	/**
	 * Execute the JavaScript code.
	 * <p>
	 * Execute the JavaScript code, in the same way as when triggered by a
	 * {@link EventSignal}. This function returns immediately, and execution of
	 * the JavaScript code is deferred until after the event handling.
	 */
	public void exec() {
		WApplication.getInstance().doJavaScript(
				"{var e=null;" + this.imp_.getJavaScript() + "}");
	}

	private WWidget widget_;
	private AbstractEventSignal.LearningListener imp_;

	private String getJsFunctionName() {
		return "sf" + String.valueOf(this.fid_);
	}

	AbstractEventSignal.LearningListener getSlotimp() {
		return this.imp_;
	}

	private void create() {
		this.imp_ = new AbstractEventSignal.JavaScriptListener(this.widget_,
				null, this.widget_ != null ? WApplication.getInstance()
						.getJavaScriptClass()
						+ '.' + this.getJsFunctionName() + "(this, e);" : "");
	}

	private int fid_;
	private static int nextFid_ = 0;
}
