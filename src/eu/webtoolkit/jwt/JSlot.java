/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import eu.webtoolkit.jwt.auth.*;
import eu.webtoolkit.jwt.auth.mfa.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.servlet.*;
import eu.webtoolkit.jwt.utils.*;
import java.io.*;
import java.lang.ref.*;
import java.time.*;
import java.util.*;
import java.util.regex.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A slot that is only implemented in client side JavaScript code.
 *
 * <p>This class provides a hook for adding your own JavaScript to respond to events.
 *
 * <p>Carefully consider the use of this. Not only is writing cross-browser JavaScript hard and
 * tedious, but one must also be aware of possible security problems (see further), and of course,
 * the event handling will not be available when JavaScript is disabled or not present at all.
 *
 * <p>For some purposes, stateless slot implementations are not sufficient, since they do not allow
 * state inspection. At the same time, the non-availability in case of disabled JavaScript may also
 * be fine for some non-essential functionality (see for example the {@link WSuggestionPopup}
 * widget), or when you simply do not care. For these situations a JSlot can be used to add
 * client-side event handling.
 *
 * <p>The JavaScript code may be set (or changed) using the {@link JSlot#setJavaScript(String js,
 * int nbArgs) setJavaScript()} method which takes a string that implements a JavaScript function
 * with the following signature:
 *
 * <p>
 *
 * <pre>{@code
 * function(sender, event) {
 * // handle the event, and sender is a reference to the DOM element
 * // which captured the event (and holds the signal). Therefore it
 * // equivalent to the sender for a normal JWt slot.
 *
 * // You can prevent the default action using:
 * var fixed = jQuery.event.fix(event);
 * fixed.preventDefault();
 * fixed.stopPropagation();
 * }
 *
 * }</pre>
 *
 * <p>In the JavaScript code, you may use {@link WWidget#getJsRef()} to obtain the DOM element
 * corresponding to any {@link WWidget}, or {@link WObject#getId()} to obtain the DOM id. In
 * addition you may trigger server-side events using the JavaScript WtSignalEmit function (see
 * {@link JSignal} documentation).
 *
 * <p>A JSlot can take up to six extra arguments. This is so that a {@link JSignal} can pass its
 * arguments directly on to a JSlot, without communicating with the server.
 *
 * <p>That&apos;s how far we can help you. For the rest you are left to yourself, buggy browsers and
 * quirky JavaScript (<a href="http://www.quirksmode.org/">http://www.quirksmode.org/</a> was a
 * reliable companion to me) &ndash; good luck.
 *
 * <p>Note that the slot object needs to live as long as you want the JavaScript to be executed by
 * connected signals: when the slot is destroyed, the connection is destroyed just as with other
 * signal/slot connections where the target object is deleted. This means that it is (almost?)
 * always a bad idea to declare a {@link JSlot} on the stack.
 */
public class JSlot {
  private static Logger logger = LoggerFactory.getLogger(JSlot.class);

  /**
   * Constructs a JavaScript-only slot within the parent scope.
   *
   * <p>The JavaScript code block will reside within the scope of the given widget. By picking a
   * long-lived parent, one may reuse a single block of JavaScript code for multiple widgets.
   *
   * <p>When <code>parent</code> = <code>null</code>, then the JavaScript will be inlined in each
   * caller (possibly replicating the same JavaScript).
   *
   * <p>The slot will have no extra arguments.
   */
  public JSlot(WWidget parent) {
    this.widget_ = parent;
    this.fid_ = nextFid_.getAndIncrement();
    this.nbArgs_ = 0;
    this.create();
  }
  /**
   * Constructs a JavaScript-only slot within the parent scope.
   *
   * <p>Calls {@link #JSlot(WWidget parent) this((WWidget)null)}
   */
  public JSlot() {
    this((WWidget) null);
  }
  /**
   * Constructs a JavaScript-only slot and sets the JavaScript code.
   *
   * <p>The slot will have no extra arguments.
   *
   * <p>
   *
   * @see JSlot#JSlot(WWidget parent)
   * @see JSlot#setJavaScript(String js, int nbArgs)
   */
  public JSlot(final String javaScript, WWidget parent) {
    this.widget_ = parent;
    this.fid_ = nextFid_.getAndIncrement();
    this.nbArgs_ = 0;
    this.create();
    this.setJavaScript(javaScript);
  }
  /**
   * Constructs a JavaScript-only slot and sets the JavaScript code.
   *
   * <p>Calls {@link #JSlot(String javaScript, WWidget parent) this(javaScript, (WWidget)null)}
   */
  public JSlot(final String javaScript) {
    this(javaScript, (WWidget) null);
  }
  /**
   * Constructs a JavaScript-only slot and set the number of arguments.
   *
   * <p>
   *
   * @see JSlot#JSlot(WWidget parent)
   * @see JSlot#setJavaScript(String js, int nbArgs)
   */
  public JSlot(int nbArgs, WWidget parent) {
    this.widget_ = parent;
    this.fid_ = nextFid_.getAndIncrement();
    this.nbArgs_ = nbArgs;
    if (this.nbArgs_ < 0 || this.nbArgs_ > 6) {
      throw new WException("The number of arguments given must be between 0 and 6.");
    }
    this.create();
  }
  /**
   * Constructs a JavaScript-only slot and sets the JavaScript code and a number of arguments.
   *
   * <p>
   *
   * @see JSlot#JSlot(WWidget parent)
   * @see JSlot#setJavaScript(String js, int nbArgs)
   */
  public JSlot(final String javaScript, int nbArgs, WWidget parent) {
    this.widget_ = parent;
    this.fid_ = nextFid_.getAndIncrement();
    this.nbArgs_ = nbArgs;
    if (this.nbArgs_ < 0 || this.nbArgs_ > 6) {
      throw new WException("The number of arguments given must be between 0 and 6.");
    }
    this.create();
    this.setJavaScript(javaScript, this.nbArgs_);
  }
  /**
   * Constructs a JavaScript-only slot and sets the JavaScript code and a number of arguments.
   *
   * <p>Calls {@link #JSlot(String javaScript, int nbArgs, WWidget parent) this(javaScript, nbArgs,
   * (WWidget)null)}
   */
  public JSlot(final String javaScript, int nbArgs) {
    this(javaScript, nbArgs, (WWidget) null);
  }
  /**
   * Set or modify the JavaScript code associated with the slot.
   *
   * <p>When the slot is triggered, the corresponding function defined by <code>javaScript</code> is
   * executed.
   *
   * <p>The JavaScript function takes at least two parameters and thus should look like:
   *
   * <pre>{@code
   * function(obj, event) {
   * // ...
   * }
   *
   * }</pre>
   *
   * The first parameter <code>obj</code> is a reference to the DOM element that generates the
   * event. The <code>event</code> refers to the JavaScript event object.
   *
   * <p>The JavaScript function can take up to six extra arguments, which is to be configured using
   * the nbArgs parameter.
   *
   * <pre>{@code
   * function(obj, event, a1, a2, a3, a4, a5, a6) {
   * // ...
   * }
   *
   * }</pre>
   *
   * If this {@link JSlot} is connected to a {@link JSignal}, that {@link JSignal}&apos;s arguments
   * will be passed on to the {@link JSlot}.
   *
   * <p>
   *
   * @see WWidget#getJsRef()
   */
  public void setJavaScript(final String js, int nbArgs) {
    if (nbArgs < 0 || nbArgs > 6) {
      throw new WException("The number of arguments given must be between 0 and 6.");
    }
    this.nbArgs_ = nbArgs;
    WApplication app = WApplication.getInstance();
    if (this.widget_ != null && app != null) {
      WApplication.getInstance().declareJavaScriptFunction(this.getJsFunctionName(), js);
    } else {
      StringWriter ss = new StringWriter();
      ss.append("{var f=").append(js).append(";f(o,e");
      for (int i = 1; i <= nbArgs; ++i) {
        ss.append(",a").append(String.valueOf(i));
      }
      ss.append(");}");
      this.imp_.setJavaScript(ss.toString());
    }
  }
  /**
   * Set or modify the JavaScript code associated with the slot.
   *
   * <p>Calls {@link #setJavaScript(String js, int nbArgs) setJavaScript(js, 0)}
   */
  public final void setJavaScript(final String js) {
    setJavaScript(js, 0);
  }
  /**
   * Executes the JavaScript code.
   *
   * <p>This executes the JavaScript code in the same way as when triggered by a {@link
   * EventSignal}. This function returns immediately, and execution of the JavaScript code is
   * deferred until after the event handling.
   *
   * <p>The first two arguments are the <code>&quot;object, event&quot;</code> arguments of the
   * JavaScript event callback function.
   *
   * <p>
   *
   * @see JSlot#setJavaScript(String js, int nbArgs)
   */
  public void exec(
      final String object,
      final String event,
      final String arg1,
      final String arg2,
      final String arg3,
      final String arg4,
      final String arg5,
      final String arg6) {
    WApplication.getInstance()
        .doJavaScript(this.execJs(object, event, arg1, arg2, arg3, arg4, arg5, arg6));
  }
  /**
   * Executes the JavaScript code.
   *
   * <p>Calls {@link #exec(String object, String event, String arg1, String arg2, String arg3,
   * String arg4, String arg5, String arg6) exec("null", "null", "null", "null", "null", "null",
   * "null", "null")}
   */
  public final void exec() {
    exec("null", "null", "null", "null", "null", "null", "null", "null");
  }
  /**
   * Executes the JavaScript code.
   *
   * <p>Calls {@link #exec(String object, String event, String arg1, String arg2, String arg3,
   * String arg4, String arg5, String arg6) exec(object, "null", "null", "null", "null", "null",
   * "null", "null")}
   */
  public final void exec(final String object) {
    exec(object, "null", "null", "null", "null", "null", "null", "null");
  }
  /**
   * Executes the JavaScript code.
   *
   * <p>Calls {@link #exec(String object, String event, String arg1, String arg2, String arg3,
   * String arg4, String arg5, String arg6) exec(object, event, "null", "null", "null", "null",
   * "null", "null")}
   */
  public final void exec(final String object, final String event) {
    exec(object, event, "null", "null", "null", "null", "null", "null");
  }
  /**
   * Executes the JavaScript code.
   *
   * <p>Calls {@link #exec(String object, String event, String arg1, String arg2, String arg3,
   * String arg4, String arg5, String arg6) exec(object, event, arg1, "null", "null", "null",
   * "null", "null")}
   */
  public final void exec(final String object, final String event, final String arg1) {
    exec(object, event, arg1, "null", "null", "null", "null", "null");
  }
  /**
   * Executes the JavaScript code.
   *
   * <p>Calls {@link #exec(String object, String event, String arg1, String arg2, String arg3,
   * String arg4, String arg5, String arg6) exec(object, event, arg1, arg2, "null", "null", "null",
   * "null")}
   */
  public final void exec(
      final String object, final String event, final String arg1, final String arg2) {
    exec(object, event, arg1, arg2, "null", "null", "null", "null");
  }
  /**
   * Executes the JavaScript code.
   *
   * <p>Calls {@link #exec(String object, String event, String arg1, String arg2, String arg3,
   * String arg4, String arg5, String arg6) exec(object, event, arg1, arg2, arg3, "null", "null",
   * "null")}
   */
  public final void exec(
      final String object,
      final String event,
      final String arg1,
      final String arg2,
      final String arg3) {
    exec(object, event, arg1, arg2, arg3, "null", "null", "null");
  }
  /**
   * Executes the JavaScript code.
   *
   * <p>Calls {@link #exec(String object, String event, String arg1, String arg2, String arg3,
   * String arg4, String arg5, String arg6) exec(object, event, arg1, arg2, arg3, arg4, "null",
   * "null")}
   */
  public final void exec(
      final String object,
      final String event,
      final String arg1,
      final String arg2,
      final String arg3,
      final String arg4) {
    exec(object, event, arg1, arg2, arg3, arg4, "null", "null");
  }
  /**
   * Executes the JavaScript code.
   *
   * <p>Calls {@link #exec(String object, String event, String arg1, String arg2, String arg3,
   * String arg4, String arg5, String arg6) exec(object, event, arg1, arg2, arg3, arg4, arg5,
   * "null")}
   */
  public final void exec(
      final String object,
      final String event,
      final String arg1,
      final String arg2,
      final String arg3,
      final String arg4,
      final String arg5) {
    exec(object, event, arg1, arg2, arg3, arg4, arg5, "null");
  }
  /**
   * Returns a JavaScript statement that executes the slot.
   *
   * <p>This returns the JavaScript code to execute the slot.
   *
   * <p>The arguments are the <code>&quot;object, event&quot;</code> arguments of the JavaScript
   * event callback function.
   *
   * <p>
   *
   * @see JSlot#exec(String object, String event, String arg1, String arg2, String arg3, String
   *     arg4, String arg5, String arg6)
   */
  public String execJs(
      final String object,
      final String event,
      final String arg1,
      final String arg2,
      final String arg3,
      final String arg4,
      final String arg5,
      final String arg6) {
    StringWriter result = new StringWriter();
    result.append("{var o=").append(object);
    result.append(",e=").append(event);
    for (int i = 0; i < this.nbArgs_; ++i) {
      result.append(",a").append(String.valueOf(i + 1)).append("=");
      switch (i) {
        case 0:
          result.append(arg1);
          break;
        case 1:
          result.append(arg2);
          break;
        case 2:
          result.append(arg3);
          break;
        case 3:
          result.append(arg4);
          break;
        case 4:
          result.append(arg5);
          break;
        case 5:
          result.append(arg6);
          break;
      }
    }
    result.append(";").append(this.imp_.getJavaScript() + "}");
    return result.toString();
  }
  /**
   * Returns a JavaScript statement that executes the slot.
   *
   * <p>Returns {@link #execJs(String object, String event, String arg1, String arg2, String arg3,
   * String arg4, String arg5, String arg6) execJs("null", "null", "null", "null", "null", "null",
   * "null", "null")}
   */
  public final String execJs() {
    return execJs("null", "null", "null", "null", "null", "null", "null", "null");
  }
  /**
   * Returns a JavaScript statement that executes the slot.
   *
   * <p>Returns {@link #execJs(String object, String event, String arg1, String arg2, String arg3,
   * String arg4, String arg5, String arg6) execJs(object, "null", "null", "null", "null", "null",
   * "null", "null")}
   */
  public final String execJs(final String object) {
    return execJs(object, "null", "null", "null", "null", "null", "null", "null");
  }
  /**
   * Returns a JavaScript statement that executes the slot.
   *
   * <p>Returns {@link #execJs(String object, String event, String arg1, String arg2, String arg3,
   * String arg4, String arg5, String arg6) execJs(object, event, "null", "null", "null", "null",
   * "null", "null")}
   */
  public final String execJs(final String object, final String event) {
    return execJs(object, event, "null", "null", "null", "null", "null", "null");
  }
  /**
   * Returns a JavaScript statement that executes the slot.
   *
   * <p>Returns {@link #execJs(String object, String event, String arg1, String arg2, String arg3,
   * String arg4, String arg5, String arg6) execJs(object, event, arg1, "null", "null", "null",
   * "null", "null")}
   */
  public final String execJs(final String object, final String event, final String arg1) {
    return execJs(object, event, arg1, "null", "null", "null", "null", "null");
  }
  /**
   * Returns a JavaScript statement that executes the slot.
   *
   * <p>Returns {@link #execJs(String object, String event, String arg1, String arg2, String arg3,
   * String arg4, String arg5, String arg6) execJs(object, event, arg1, arg2, "null", "null",
   * "null", "null")}
   */
  public final String execJs(
      final String object, final String event, final String arg1, final String arg2) {
    return execJs(object, event, arg1, arg2, "null", "null", "null", "null");
  }
  /**
   * Returns a JavaScript statement that executes the slot.
   *
   * <p>Returns {@link #execJs(String object, String event, String arg1, String arg2, String arg3,
   * String arg4, String arg5, String arg6) execJs(object, event, arg1, arg2, arg3, "null", "null",
   * "null")}
   */
  public final String execJs(
      final String object,
      final String event,
      final String arg1,
      final String arg2,
      final String arg3) {
    return execJs(object, event, arg1, arg2, arg3, "null", "null", "null");
  }
  /**
   * Returns a JavaScript statement that executes the slot.
   *
   * <p>Returns {@link #execJs(String object, String event, String arg1, String arg2, String arg3,
   * String arg4, String arg5, String arg6) execJs(object, event, arg1, arg2, arg3, arg4, "null",
   * "null")}
   */
  public final String execJs(
      final String object,
      final String event,
      final String arg1,
      final String arg2,
      final String arg3,
      final String arg4) {
    return execJs(object, event, arg1, arg2, arg3, arg4, "null", "null");
  }
  /**
   * Returns a JavaScript statement that executes the slot.
   *
   * <p>Returns {@link #execJs(String object, String event, String arg1, String arg2, String arg3,
   * String arg4, String arg5, String arg6) execJs(object, event, arg1, arg2, arg3, arg4, arg5,
   * "null")}
   */
  public final String execJs(
      final String object,
      final String event,
      final String arg1,
      final String arg2,
      final String arg3,
      final String arg4,
      final String arg5) {
    return execJs(object, event, arg1, arg2, arg3, arg4, arg5, "null");
  }
  /** Returns the number of extra arguments this JSlot takes. */
  public int getNbArgs() {
    return this.nbArgs_;
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
    StringWriter ss = new StringWriter();
    if (this.widget_ != null) {
      WApplication app = WApplication.getInstance();
      if (app != null) {
        ss.append(WApplication.getInstance().getJavaScriptClass())
            .append(".")
            .append(this.getJsFunctionName())
            .append("(o,e");
        for (int i = 1; i <= this.nbArgs_; ++i) {
          ss.append(",a").append(String.valueOf(i));
        }
        ss.append(");");
      }
    }
    this.imp_ = new AbstractEventSignal.JavaScriptListener(this.widget_, null, ss.toString());
  }

  private final int fid_;
  private static java.util.concurrent.atomic.AtomicInteger nextFid_ =
      new java.util.concurrent.atomic.AtomicInteger(0);
  private int nbArgs_;
}
