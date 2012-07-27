/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

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

/**
 * A widget that renders an XHTML template.
 * <p>
 * 
 * The XHTML template may contain references to variables which replaced by
 * strings are widgets.
 * <p>
 * Since the template text may be supplied by a {@link WString}, you can
 * conveniently store the string in a message resource bundle, and make it
 * localized by using {@link WString#tr(String key) WString#tr()}.
 * <p>
 * Placeholders (for variables and functions) are delimited by:
 * <code>${...}</code>. To use a literal <code>&quot;${&quot;</code>, use
 * <code>&quot;$${&quot;</code>.
 * <p>
 * Usage example:
 * <p>
 * 
 * <pre>
 * {@code
 *  WString userName = ...;
 * 
 *  WTemplate t = new WTemplate();
 *  t.setTemplateText("<div> How old are you, ${friend} ? ${age-input} </div>");
 * 
 *  t.bindString("friend", userName, PlainText);
 *  t.bindWidget("age-input", ageEdit_ = new WLineEdit());
 * }
 * </pre>
 * <p>
 * There are currently three syntactic constructs defined: variable place
 * holders, functions and conditional blocks.
 * <p>
 * <h3>A. Variable placeholders</h3>
 * <p>
 * <code>${var}</code> defines a placeholder for the variable &quot;var&quot;,
 * and gets replaced with whatever is bound to that variable:
 * <ul>
 * <li>a widget, using
 * {@link WTemplate#bindWidget(String varName, WWidget widget) bindWidget()}</li>
 * <li>a string value, using
 * {@link WTemplate#bindString(String varName, CharSequence value, TextFormat textFormat)
 * bindString()} or {@link WTemplate#bindInt(String varName, int value)
 * bindInt()}</li>
 * <li>or in general, the result of
 * {@link WTemplate#resolveString(String varName, List args, Writer result)
 * resolveString()} and {@link WTemplate#resolveWidget(String varName)
 * resolveWidget()} methods.</li>
 * </ul>
 * <p>
 * Optionally, additional arguments can be specified using the following syntax:
 * <p>
 * <code>${var arg1=&quot;A value&quot; arg2=&apos;A second value&apos;}</code>
 * <p>
 * The arguments can thus be simple simple strings or quoted strings (single or
 * double quoted). These arguments are applied to a resolved widget in
 * {@link WTemplate#applyArguments(WWidget w, List args) applyArguments()} and
 * currently supports only style classes.
 * <p>
 * You can bind widgets and values to variables using
 * {@link WTemplate#bindWidget(String varName, WWidget widget) bindWidget()},
 * {@link WTemplate#bindString(String varName, CharSequence value, TextFormat textFormat)
 * bindString()} or {@link WTemplate#bindInt(String varName, int value)
 * bindInt()} or by reimplementing the
 * {@link WTemplate#resolveString(String varName, List args, Writer result)
 * resolveString()} and {@link WTemplate#resolveWidget(String varName)
 * resolveWidget()} methods.
 * <p>
 * <p>
 * <i><b>Note: </b>The use of XML comments (<code>&lt;!-- ... -.</code>) around
 * variables that are bound to widgets will result in bad behaviour since the
 * template parser is ignorant about these comments and the corresponding
 * widgets will believe that they are rendered but aren&apos;t actually.</i>
 * </p>
 * <h3>B. Functions</h3>
 * <p>
 * <code>${fun:arg}</code> defines a placeholder for applying a function
 * &quot;fun&quot; to an argument &quot;arg&quot;.
 * <p>
 * Optionally, additional arguments can be specified as with a variable
 * placeholder.
 * <p>
 * Functions are resolved by
 * {@link WTemplate#resolveFunction(String name, List args, Writer result)
 * resolveFunction()}, and the default implementation considers functions bound
 * with {@link WTemplate#addFunction(String name, WTemplate.Function function)
 * addFunction()}. There are currently two functions that are generally useful:
 * <p>
 * <ul>
 * <li>WTemplate::Functions::tr : resolves a localized strings, this is
 * convenient to create a language neutral template, which contains translated
 * strings</li>
 * </ul>
 * <p>
 * <ul>
 * <li>WTemplate::Function::id : resolves the id of a bound widget, this is
 * convenient to bind &lt;label&gt; elements to a form widget using its for
 * attribute.</li>
 * </ul>
 * <p>
 * For example, the following template uses the &quot;tr&quot; function to
 * translate the age-label using the &quot;age-label&quot; internationalized
 * key.
 * <p>
 * 
 * <pre>
 * {
 * 	&#064;code
 * 	WTemplate t = new WTemplate();
 * 	t.setTemplateText(&quot;&lt;div&gt; ${tr:age-label} ${age-input} &lt;/div&gt;&quot;);
 * 	t.addFunction(&quot;tr&quot;, Template.Functions.tr);
 * 	t.bindWidget(&quot;age-input&quot;, ageEdit = new WLineEdit());
 * }
 * </pre>
 * <p>
 * <h3>C. Conditional blocks</h3>
 * <p>
 * <code>${&lt;cond&gt;}</code> starts a conditional block with a condition name
 * &quot;cond&quot;, and must be closed by a balanced
 * <code>${&lt;/cond&gt;}</code>.
 * <p>
 * For example:
 * <p>
 * 
 * <pre>
 * {
 * 	&#064;code
 * 	WTemplate t = new WTemplate();
 * 	t
 * 			.setTemplateText(&quot;&lt;div&gt; ${&lt;if-register&gt;} Register ... ${&lt;/if-register&gt;}&lt;/div&gt;&quot;);
 * 	t.setCondition(&quot;if-register&quot;, true);
 * }
 * </pre>
 * <p>
 * Conditions are set using
 * {@link WTemplate#setCondition(String name, boolean value) setCondition()}.
 * <p>
 * <h3>CSS</h3>
 * <p>
 * This widget does not provide styling, and can be styled using inline or
 * external CSS as appropriate.
 */
public class WTemplate extends WInteractWidget {
	private static Logger logger = LoggerFactory.getLogger(WTemplate.class);

	private boolean _tr(List<WString> args, Writer result) throws IOException {
		if (args.size() >= 1) {
			WString s = WString.tr(args.get(0).toString());
			for (int j = 1; j < args.size(); ++j) {
				s.arg(args.get(j));
			}
			result.append(s.toString());
			return true;
		} else {
			logger.error(new StringWriter().append(
					"Functions::tr(): expects at least one argument")
					.toString());
			return false;
		}
	}

	private boolean _id(List<WString> args, Writer result) throws IOException {
		if (args.size() == 1) {
			WWidget w = this.resolveWidget(args.get(0).toString());
			if (w != null) {
				result.append(w.getId());
				return true;
			} else {
				return false;
			}
		} else {
			logger
					.error(new StringWriter().append(
							"Functions::tr(): expects exactly one argument")
							.toString());
			return false;
		}
	}

	static interface Function {
		public boolean evaluate(WTemplate t, List<WString> args, Writer result);
	}

	/**
	 * A function that resolves to a localized string.
	 * <p>
	 * 
	 * For example, when bound to the function <code>&quot;tr&quot;</code>, a
	 * place-holder
	 * 
	 * <pre>
	 * ${tr:name}
	 * </pre>
	 * 
	 * will be resolved to the value of:
	 * 
	 * <pre>
	 * {@code
	 *      WString#tr("name")
	 *   }
	 * </pre>
	 * <p>
	 * 
	 * @see WTemplate#addFunction(String name, WTemplate.Function function)
	 */
	public static class TrFunction implements WTemplate.Function {
		private static Logger logger = LoggerFactory
				.getLogger(TrFunction.class);

		public boolean evaluate(WTemplate t, List<WString> args, Writer result) {
			try {
				return t._tr(args, result);
			} catch (IOException ioe) {
				return false;
			}
		}
	}

	/**
	 * A function that resolves the id of a bound widget.
	 * <p>
	 * 
	 * For example, when bound to the function <code>&quot;id&quot;</code>, a
	 * place-holder
	 * 
	 * <pre>
	 * ${id:name}
	 * </pre>
	 * 
	 * will be resolved to the value of:
	 * 
	 * <pre>
	 * {@code
	 *      t.resolveWidget("name").id()
	 *   }
	 * </pre>
	 * <p>
	 * This is useful for binding labels to input elements.
	 * <p>
	 * 
	 * @see WTemplate#addFunction(String name, WTemplate.Function function)
	 */
	public static class IdFunction implements WTemplate.Function {
		private static Logger logger = LoggerFactory
				.getLogger(IdFunction.class);

		public boolean evaluate(WTemplate t, List<WString> args, Writer result) {
			try {
				return t._id(args, result);
			} catch (IOException ioe) {
				return false;
			}
		}
	}

	/**
	 * Type that holds predefined functions.
	 * <p>
	 * 
	 * @see WTemplate#addFunction(String name, WTemplate.Function function)
	 */
	public static class FunctionsList {
		private static Logger logger = LoggerFactory
				.getLogger(FunctionsList.class);

		/**
		 * A pre-defined {@link WTemplate.FunctionsList#tr} function.
		 */
		public WTemplate.TrFunction tr;
		/**
		 * A pre-defined {@link WTemplate.FunctionsList#id} function.
		 */
		public WTemplate.IdFunction id;

		private FunctionsList() {
			this.tr = new WTemplate.TrFunction();
			this.id = new WTemplate.IdFunction();
		}
	}

	/**
	 * Creates a template widget.
	 */
	public WTemplate(WContainerWidget parent) {
		super(parent);
		this.functions_ = new HashMap<String, WTemplate.Function>();
		this.strings_ = new HashMap<String, String>();
		this.widgets_ = new HashMap<String, WWidget>();
		this.conditions_ = new HashSet<String>();
		this.text_ = new WString();
		this.encodeInternalPaths_ = false;
		this.changed_ = false;
		this.setInline(false);
	}

	/**
	 * Creates a template widget.
	 * <p>
	 * Calls {@link #WTemplate(WContainerWidget parent)
	 * this((WContainerWidget)null)}
	 */
	public WTemplate() {
		this((WContainerWidget) null);
	}

	/**
	 * Creates a template widget with given template.
	 * <p>
	 * The <code>templateText</code> must be proper XHTML, and this is checked
	 * unless the XHTML is resolved from a message resource bundle. This
	 * behavior is similar to a {@link WText} when configured with the
	 * {@link TextFormat#XHTMLText} textformat.
	 */
	public WTemplate(CharSequence text, WContainerWidget parent) {
		super(parent);
		this.functions_ = new HashMap<String, WTemplate.Function>();
		this.strings_ = new HashMap<String, String>();
		this.widgets_ = new HashMap<String, WWidget>();
		this.conditions_ = new HashSet<String>();
		this.text_ = new WString();
		this.encodeInternalPaths_ = false;
		this.changed_ = false;
		this.setInline(false);
		this.setTemplateText(text);
	}

	/**
	 * Creates a template widget with given template.
	 * <p>
	 * Calls {@link #WTemplate(CharSequence text, WContainerWidget parent)
	 * this(text, (WContainerWidget)null)}
	 */
	public WTemplate(CharSequence text) {
		this(text, (WContainerWidget) null);
	}

	/**
	 * Returns the template.
	 * <p>
	 * 
	 * @see WTemplate#setTemplateText(CharSequence text, TextFormat textFormat)
	 */
	public WString getTemplateText() {
		return this.text_;
	}

	/**
	 * Sets the template text.
	 * <p>
	 * The <code>text</code> must be proper XHTML, and this is checked unless
	 * the XHTML is resolved from a message resource bundle or TextFormat is
	 * {@link TextFormat#XHTMLUnsafeText}. This behavior is similar to a
	 * {@link WText} when configured with the {@link TextFormat#XHTMLText}
	 * textformat.
	 * <p>
	 * Changing the template text does not {@link WTemplate#clear() clear()}
	 * bound widgets or values.
	 * <p>
	 * 
	 * @see WTemplate#clear()
	 */
	public void setTemplateText(CharSequence text, TextFormat textFormat) {
		this.text_ = WString.toWString(text);
		if (textFormat == TextFormat.XHTMLText && this.text_.isLiteral()) {
			if (!removeScript(this.text_)) {
				this.text_ = escapeText(this.text_, true);
			}
		} else {
			if (textFormat == TextFormat.PlainText) {
				this.text_ = escapeText(this.text_, true);
			}
		}
		this.changed_ = true;
		this.repaint(EnumSet.of(RepaintFlag.RepaintInnerHtml));
	}

	/**
	 * Sets the template text.
	 * <p>
	 * Calls {@link #setTemplateText(CharSequence text, TextFormat textFormat)
	 * setTemplateText(text, TextFormat.XHTMLText)}
	 */
	public final void setTemplateText(CharSequence text) {
		setTemplateText(text, TextFormat.XHTMLText);
	}

	/**
	 * Binds a string value to a variable.
	 * <p>
	 * Each occurrence of the variable within the template will be substituted
	 * by its value.
	 * <p>
	 * Depending on the <code>textFormat</code>, the <code>value</code> is
	 * validated according as for a {@link WText}.
	 * <p>
	 * 
	 * @see WTemplate#bindWidget(String varName, WWidget widget)
	 * @see WTemplate#bindInt(String varName, int value)
	 * @see WTemplate#resolveString(String varName, List args, Writer result)
	 */
	public void bindString(String varName, CharSequence value,
			TextFormat textFormat) {
		WString v = WString.toWString(value);
		if (textFormat == TextFormat.XHTMLText && v.isLiteral()) {
			if (!removeScript(v)) {
				v = escapeText(v, true);
			}
		} else {
			if (textFormat == TextFormat.PlainText) {
				v = escapeText(v, true);
			}
		}
		String i = this.strings_.get(varName);
		if (i == null || !i.equals(v.toString())) {
			this.strings_.put(varName, v.toString());
			this.changed_ = true;
			this.repaint(EnumSet.of(RepaintFlag.RepaintInnerHtml));
		}
	}

	/**
	 * Binds a string value to a variable.
	 * <p>
	 * Calls
	 * {@link #bindString(String varName, CharSequence value, TextFormat textFormat)
	 * bindString(varName, value, TextFormat.XHTMLText)}
	 */
	public final void bindString(String varName, CharSequence value) {
		bindString(varName, value, TextFormat.XHTMLText);
	}

	/**
	 * Binds an integer value to a variable.
	 * <p>
	 * 
	 * @see WTemplate#bindString(String varName, CharSequence value, TextFormat
	 *      textFormat)
	 */
	public void bindInt(String varName, int value) {
		this.bindString(varName, String.valueOf(value),
				TextFormat.XHTMLUnsafeText);
	}

	/**
	 * Binds a widget to a variable.
	 * <p>
	 * The corresponding variable reference within the template will be replaced
	 * with the widget (rendered as XHTML). Since a single widget may be
	 * instantiated only once in a template, the variable <code>varName</code>
	 * may occur at most once in the template.
	 * <p>
	 * If a widget was already bound to the variable, it is deleted first. If
	 * previously a string or other value was bound to the variable, it is
	 * removed.
	 * <p>
	 * You may also pass a <code>null</code> <code>widget</code>, which will
	 * resolve to an empty string.
	 * <p>
	 * 
	 * @see WTemplate#bindString(String varName, CharSequence value, TextFormat
	 *      textFormat)
	 * @see WTemplate#resolveWidget(String varName)
	 */
	public void bindWidget(String varName, WWidget widget) {
		WWidget i = this.widgets_.get(varName);
		if (i != null) {
			if (i == widget) {
				return;
			} else {
				if (i != null)
					i.remove();
				this.widgets_.remove(varName);
			}
		}
		if (widget != null) {
			widget.setParentWidget(this);
			this.widgets_.put(varName, widget);
			this.strings_.remove(varName);
		} else {
			String j = this.strings_.get(varName);
			if (j != null && j.length() == 0) {
				return;
			}
			this.strings_.put(varName, "");
		}
		this.changed_ = true;
		this.repaint(EnumSet.of(RepaintFlag.RepaintInnerHtml));
	}

	/**
	 * Binds an empty string to a variable.
	 * <p>
	 * 
	 * @see WTemplate#bindString(String varName, CharSequence value, TextFormat
	 *      textFormat)
	 */
	public void bindEmpty(String varName) {
		this.bindWidget(varName, (WWidget) null);
	}

	/**
	 * Binds a function.
	 * <p>
	 * Functions are useful to automatically resolve placeholders.
	 * <p>
	 * The syntax for a function &apos;fun&apos; applied to a single argument
	 * &apos;bla&apos; is:
	 * <p>
	 * <code>${fun:bla}</code>
	 * <p>
	 * There are two predefined functions, which can be bound using:
	 * <p>
	 * 
	 * <pre>
	 * {@code
	 *    WTemplate t = ...;
	 *    t.addFunction("id", WTemplate.Functions.id);
	 *    t.addFunction("tr", WTemplate.Functions.tr);
	 *   }
	 * </pre>
	 */
	public void addFunction(String name, WTemplate.Function function) {
		this.functions_.put(name, function);
	}

	/**
	 * Sets a condition.
	 * <p>
	 * This enables or disables the inclusion of a conditional block.
	 * <p>
	 * The default value of all conditions is <code>false</code>.
	 */
	public void setCondition(String name, boolean value) {
		if (this.conditionValue(name) != value) {
			if (value) {
				this.conditions_.add(name);
			} else {
				this.conditions_.remove(name);
			}
			this.changed_ = true;
			this.repaint(EnumSet.of(RepaintFlag.RepaintInnerHtml));
		}
	}

	/**
	 * Returns a condition value.
	 * <p>
	 * 
	 * @see WTemplate#setCondition(String name, boolean value)
	 */
	public boolean conditionValue(String name) {
		return this.conditions_.contains(name) != false;
	}

	/**
	 * Resolves the string value for a variable name.
	 * <p>
	 * This is the main method used to resolve variables in the template text,
	 * during rendering.
	 * <p>
	 * The default implementation considers first whether a string was bound
	 * using
	 * {@link WTemplate#bindString(String varName, CharSequence value, TextFormat textFormat)
	 * bindString()}. If so, that string is returned. If not, it will attempt to
	 * resolve a widget with that variable name using
	 * {@link WTemplate#resolveWidget(String varName) resolveWidget()}, and
	 * render it as XHTML. If that fails too,
	 * {@link WTemplate#handleUnresolvedVariable(String varName, List args, Writer result)
	 * handleUnresolvedVariable()} is called, passing the initial arguments.
	 * <p>
	 * You may want to reimplement this method to provide on-demand loading of
	 * strings for your template.
	 * <p>
	 * The result stream expects a UTF-8 encoded string value.
	 * <p>
	 * <p>
	 * <i><b>Warning:</b>When specializing this class, you need to make sure
	 * that you append proper XHTML to the <code>result</code>, without unsafe
	 * active contents. The
	 * {@link WTemplate#format(Writer result, String s, TextFormat textFormat)
	 * format()} methods may be used for this purpose.</i>
	 * </p>
	 * 
	 * @see WTemplate#renderTemplate(Writer result)
	 */
	public void resolveString(String varName, List<WString> args, Writer result)
			throws IOException {
		String i = this.strings_.get(varName);
		if (i != null) {
			result.append(i);
		} else {
			WWidget w = this.resolveWidget(varName);
			if (w != null) {
				w.setParentWidget(this);
				if (this.previouslyRendered_ != null
						&& this.previouslyRendered_.contains(w) != false) {
					result.append("<span id=\"").append(w.getId()).append(
							"\"> </span>");
				} else {
					this.applyArguments(w, args);
					w.htmlText(result);
				}
				this.newlyRendered_.add(w);
			} else {
				this.handleUnresolvedVariable(varName, args, result);
			}
		}
	}

	/**
	 * Handles a variable that could not be resolved.
	 * <p>
	 * This method is called from
	 * {@link WTemplate#resolveString(String varName, List args, Writer result)
	 * resolveString()} for variables that could not be resolved.
	 * <p>
	 * The default implementation implementation writes &quot;??&quot; + varName
	 * + &quot;??&quot; to the result stream.
	 * <p>
	 * The result stream expects a UTF-8 encoded string value.
	 * <p>
	 * <p>
	 * <i><b>Warning:</b>When specializing this class, you need to make sure
	 * that you append proper XHTML to the <code>result</code>, without unsafe
	 * active contents. The
	 * {@link WTemplate#format(Writer result, String s, TextFormat textFormat)
	 * format()} methods may be used for this purpose.</i>
	 * </p>
	 * 
	 * @see WTemplate#resolveString(String varName, List args, Writer result)
	 */
	public void handleUnresolvedVariable(String varName, List<WString> args,
			Writer result) throws IOException {
		result.append("??").append(varName).append("??");
	}

	/**
	 * Resolves a widget for a variable name.
	 * <p>
	 * The default implementation returns a widget that was bound using
	 * {@link WTemplate#bindWidget(String varName, WWidget widget) bindWidget()}.
	 * <p>
	 * You may want to reimplement this method to create widgets on-demand. All
	 * widgets that are returned by this method are reparented to the
	 * {@link WTemplate}, so they will be deleted when the template is
	 * destroyed, but they are not deleted by {@link WTemplate#clear() clear()}
	 * (unless bind was called on them as in the example below).
	 * <p>
	 * This method is typically used for delayed binding of widgets. Usage
	 * example:
	 * 
	 * <pre>
	 * {@code
	 *    {
	 *      if (Widget *known = WTemplate::resolveWidget(varName)) {
	 *        return known;
	 *      } else {
	 *        if (varName == "age-input") {
	 *          WWidget *w = new WLineEdit(); // widget only created when used
	 *          bind(varName, w);
	 *          return w;
	 *        }
	 *      }
	 *    }
	 *   }
	 * </pre>
	 */
	public WWidget resolveWidget(String varName) {
		WWidget j = this.widgets_.get(varName);
		if (j != null) {
			return j;
		} else {
			return null;
		}
	}

	/**
	 * Resolves a function call.
	 * <p>
	 * This resolves a function with name <code>name</code>, and one or more
	 * arguments <code>args</code>, and writes the result into the stream
	 * <code>result</code>. The method returns whether a function was matched
	 * and applied.
	 * <p>
	 * The default implementation considers functions that were bound using
	 * {@link WTemplate#addFunction(String name, WTemplate.Function function)
	 * addFunction()}.
	 * <p>
	 * 
	 * @see WTemplate#addFunction(String name, WTemplate.Function function)
	 */
	public boolean resolveFunction(String name, List<WString> args,
			Writer result) throws IOException {
		WTemplate.Function i = this.functions_.get(name);
		if (i != null) {
			boolean ok = i.evaluate(this, args, result);
			if (!ok) {
				result.append("??").append(name).append(":??");
			}
			return true;
		}
		return false;
	}

	// public T (String varName) ;
	/**
	 * Erases all variable bindings.
	 * <p>
	 * Removes all strings and deletes all widgets that were previously bound
	 * using
	 * {@link WTemplate#bindString(String varName, CharSequence value, TextFormat textFormat)
	 * bindString()} and
	 * {@link WTemplate#bindWidget(String varName, WWidget widget) bindWidget()}.
	 * <p>
	 * This also resets all conditions set using
	 * {@link WTemplate#setCondition(String name, boolean value) setCondition()}
	 * , but does not remove functions added with
	 * {@link WTemplate#addFunction(String name, WTemplate.Function function)
	 * addFunction()}
	 */
	public void clear() {
		this.setIgnoreChildRemoves(true);
		for (Iterator<Map.Entry<String, WWidget>> i_it = this.widgets_
				.entrySet().iterator(); i_it.hasNext();) {
			Map.Entry<String, WWidget> i = i_it.next();
			if (i.getValue() != null)
				i.getValue().remove();
		}
		this.setIgnoreChildRemoves(false);
		this.widgets_.clear();
		this.strings_.clear();
		this.conditions_.clear();
		this.changed_ = true;
		this.repaint(EnumSet.of(RepaintFlag.RepaintInnerHtml));
	}

	/**
	 * Enables internal path anchors in the XHTML template.
	 * <p>
	 * Anchors to internal paths are represented differently depending on the
	 * session implementation (plain HTML, Ajax or HTML5 history). By enabling
	 * this option, anchors which reference an internal path (by referring a URL
	 * of the form <code>href=&quot;#/...&quot;</code>), are re-encoded to link
	 * to the internal path.
	 * <p>
	 * The default value is <code>false</code>.
	 * <p>
	 * 
	 * @see WAnchor#setRefInternalPath(String path)
	 */
	public void setInternalPathEncoding(boolean enabled) {
		if (this.encodeInternalPaths_ != enabled) {
			this.encodeInternalPaths_ = enabled;
			this.changed_ = true;
			this.repaint(EnumSet.of(RepaintFlag.RepaintInnerHtml));
		}
	}

	/**
	 * Returns whether internal paths are enabled.
	 * <p>
	 * 
	 * @see WTemplate#setInternalPathEncoding(boolean enabled)
	 */
	public boolean hasInternalPathEncoding() {
		return this.encodeInternalPaths_;
	}

	/**
	 * Refreshes the template.
	 * <p>
	 * This rerenders the template.
	 */
	public void refresh() {
		if (this.text_.refresh()) {
			this.changed_ = true;
			this.repaint(EnumSet.of(RepaintFlag.RepaintInnerHtml));
		}
		super.refresh();
	}

	/**
	 * Renders the template into the given result stream.
	 * <p>
	 * The default implementation will parse the template, and resolve variables
	 * by calling
	 * {@link WTemplate#resolveString(String varName, List args, Writer result)
	 * resolveString()}.
	 * <p>
	 * You may want to reimplement this method to manage resources that are
	 * needed to load content on-demand (e.g. database objects), or support a
	 * custom template language.
	 */
	public void renderTemplate(Writer result) throws IOException {
		String text = "";
		WApplication app = WApplication.getInstance();
		if (app != null
				&& (this.encodeInternalPaths_ || app.getSession()
						.hasSessionIdInUrl())) {
			EnumSet<RefEncoderOption> options = EnumSet
					.noneOf(RefEncoderOption.class);
			if (this.encodeInternalPaths_) {
				options.add(RefEncoderOption.EncodeInternalPaths);
			}
			if (app.getSession().hasSessionIdInUrl()) {
				options.add(RefEncoderOption.EncodeRedirectTrampoline);
			}
			WString t = this.text_;
			RefEncoder.EncodeRefs(t, options);
			text = t.toString();
		} else {
			text = this.text_.toString();
		}
		int lastPos = 0;
		List<WString> args = new ArrayList<WString>();
		List<String> conditions = new ArrayList<String>();
		int suppressing = 0;
		for (int pos = text.indexOf('$'); pos != -1; pos = text.indexOf('$',
				pos)) {
			if (!(suppressing != 0)) {
				result.append(text.substring(lastPos, lastPos + pos - lastPos));
			}
			lastPos = pos;
			if (pos + 1 < text.length()) {
				if (text.charAt(pos + 1) == '$') {
					if (!(suppressing != 0)) {
						result.append('$');
					}
					lastPos += 2;
				} else {
					if (text.charAt(pos + 1) == '{') {
						int startName = pos + 2;
						int endName = StringUtils.findFirstOf(text, " \r\n\t}",
								startName);
						args.clear();
						int endVar = parseArgs(text, endName, args);
						if (endVar == -1) {
							logger.error(new StringWriter().append(
									"variable syntax error near \"").append(
									text.substring(pos)).append("\"")
									.toString());
							return;
						}
						String name = text.substring(startName, startName
								+ endName - startName);
						int nl = name.length();
						if (nl > 2 && name.charAt(0) == '<'
								&& name.charAt(nl - 1) == '>') {
							if (name.charAt(1) != '/') {
								String cond = name.substring(1, 1 + nl - 2);
								conditions.add(cond);
								if (suppressing != 0
										|| !this.conditionValue(cond)) {
									++suppressing;
								}
							} else {
								String cond = name.substring(2, 2 + nl - 3);
								if (!conditions.get(conditions.size() - 1)
										.equals(cond)) {
									logger
											.error(new StringWriter()
													.append(
															"mismatching condition block end: ")
													.append(cond).toString());
									return;
								}
								conditions.remove(conditions.size() - 1);
								if (suppressing != 0) {
									--suppressing;
								}
							}
						} else {
							if (!(suppressing != 0)) {
								int colonPos = name.indexOf(':');
								boolean handled = false;
								if (colonPos != -1) {
									String fname = name.substring(0,
											0 + colonPos);
									String arg0 = name.substring(colonPos + 1);
									args.add(0, new WString(arg0));
									if (this.resolveFunction(fname, args,
											result)) {
										handled = true;
									} else {
										args.remove(0);
									}
								}
								if (!handled) {
									this.resolveString(name, args, result);
								}
							}
						}
						lastPos = endVar + 1;
					} else {
						if (!(suppressing != 0)) {
							result.append('$');
						}
						lastPos += 1;
					}
				}
			} else {
				if (!(suppressing != 0)) {
					result.append('$');
				}
				lastPos += 1;
			}
			pos = lastPos;
		}
		result.append(text.substring(lastPos));
	}

	/**
	 * Applies arguments to a resolved widget.
	 * <p>
	 * Currently only a <code>class</code> argument is handled, which adds one
	 * or more style classes to the widget <code>w</code>, using
	 * {@link WWidget#addStyleClass(String styleClass, boolean force)
	 * WWidget#addStyleClass()}.
	 */
	protected void applyArguments(WWidget w, List<WString> args) {
		for (int i = 0; i < args.size(); ++i) {
			String s = args.get(i).toString();
			if (s.startsWith("class=")) {
				w.addStyleClass(new WString(s.substring(6)).toString());
			}
		}
	}

	void updateDom(DomElement element, boolean all) {
		try {
			if (this.changed_ || all) {
				Set<WWidget> previouslyRendered = new HashSet<WWidget>();
				List<WWidget> newlyRendered = new ArrayList<WWidget>();
				for (Iterator<Map.Entry<String, WWidget>> i_it = this.widgets_
						.entrySet().iterator(); i_it.hasNext();) {
					Map.Entry<String, WWidget> i = i_it.next();
					WWidget w = i.getValue();
					if (w.isRendered() && w.getWebWidget().domCanBeSaved()) {
						previouslyRendered.add(w);
					}
				}
				boolean saveWidgets = element.getMode() == DomElement.Mode.ModeUpdate;
				this.previouslyRendered_ = saveWidgets ? previouslyRendered
						: null;
				this.newlyRendered_ = newlyRendered;
				StringWriter html = new StringWriter();
				this.renderTemplate(html);
				this.previouslyRendered_ = null;
				this.newlyRendered_ = null;
				for (int i = 0; i < newlyRendered.size(); ++i) {
					WWidget w = newlyRendered.get(i);
					if (previouslyRendered.contains(w) != false) {
						if (saveWidgets) {
							element.saveChild(w.getId());
						}
						previouslyRendered.remove(w);
					}
				}
				element
						.setProperty(Property.PropertyInnerHTML, html
								.toString());
				this.changed_ = false;
				for (Iterator<WWidget> i_it = previouslyRendered.iterator(); i_it
						.hasNext();) {
					WWidget i = i_it.next();
					WWidget w = i;
					w.getWebWidget().setRendered(false);
				}
			}
			super.updateDom(element, all);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	DomElementType getDomElementType() {
		return this.isInline() ? DomElementType.DomElement_SPAN
				: DomElementType.DomElement_DIV;
	}

	void propagateRenderOk(boolean deep) {
		this.changed_ = false;
		super.propagateRenderOk(deep);
	}

	/**
	 * Utility method to safely format an XHTML string.
	 * <p>
	 * The string is formatted according to the indicated
	 * <code>textFormat</code>. It is recommended to use this method when
	 * specializing
	 * {@link WTemplate#resolveString(String varName, List args, Writer result)
	 * resolveString()} to avoid security risks.
	 */
	protected void format(Writer result, String s, TextFormat textFormat)
			throws IOException {
		this.format(result, new WString(s), textFormat);
	}

	/**
	 * Utility method to safely format an XHTML string.
	 * <p>
	 * Calls {@link #format(Writer result, String s, TextFormat textFormat)
	 * format(result, s, TextFormat.PlainText)}
	 */
	protected final void format(Writer result, String s) throws IOException {
		format(result, s, TextFormat.PlainText);
	}

	/**
	 * Utility method to safely format an XHTML string.
	 * <p>
	 * The string is formatted according to the indicated
	 * <code>textFormat</code>. It is recommended to use this method when
	 * specializing
	 * {@link WTemplate#resolveString(String varName, List args, Writer result)
	 * resolveString()} to avoid security risks.
	 */
	protected void format(Writer result, CharSequence s, TextFormat textFormat)
			throws IOException {
		WString v = WString.toWString(s);
		if (textFormat == TextFormat.XHTMLText) {
			if (!removeScript(v)) {
				v = escapeText(v, true);
			}
		} else {
			if (textFormat == TextFormat.PlainText) {
				v = escapeText(v, true);
			}
		}
		result.append(v.toString());
	}

	/**
	 * Utility method to safely format an XHTML string.
	 * <p>
	 * Calls {@link #format(Writer result, CharSequence s, TextFormat textFormat)
	 * format(result, s, TextFormat.PlainText)}
	 */
	protected final void format(Writer result, CharSequence s)
			throws IOException {
		format(result, s, TextFormat.PlainText);
	}

	protected void enableAjax() {
		super.enableAjax();
	}

	private Set<WWidget> previouslyRendered_;
	private List<WWidget> newlyRendered_;
	private Map<String, WTemplate.Function> functions_;
	private Map<String, String> strings_;
	private Map<String, WWidget> widgets_;
	private Set<String> conditions_;
	private WString text_;
	private boolean encodeInternalPaths_;
	private boolean changed_;

	private static int parseArgs(String text, int pos, List<WString> result) {
		int Error = -1;
		if (pos == -1) {
			return Error;
		}
		final int Next = 0;
		final int Name = 1;
		final int Value = 2;
		final int SValue = 3;
		final int DValue = 4;
		int state = Next;
		StringBuilder v = new StringBuilder();
		for (; pos < text.length(); ++pos) {
			char c = text.charAt(pos);
			switch (state) {
			case Next:
				if (!Character.isWhitespace(c)) {
					if (c == '}') {
						return pos;
					} else {
						if (Character.isLetter(c) || c == '_') {
							state = Name;
							v.setLength(0);
							v.append(c);
						} else {
							if (c == '\'') {
								state = SValue;
								v.setLength(0);
							} else {
								if (c == '"') {
									state = DValue;
									v.setLength(0);
								} else {
									return Error;
								}
							}
						}
					}
				}
				break;
			case Name:
				if (c == '=') {
					state = Value;
					v.append('=');
				} else {
					if (Character.isWhitespace(c)) {
						result.add(new WString(v.toString()));
						state = Next;
					} else {
						if (c == '}') {
							result.add(new WString(v.toString()));
							return pos;
						} else {
							if (Character.isLetterOrDigit(c) || c == '_'
									|| c == '-') {
								v.append(c);
							} else {
								return Error;
							}
						}
					}
				}
				break;
			case Value:
				if (c == '\'') {
					state = SValue;
				} else {
					if (c == '"') {
						state = DValue;
					} else {
						return Error;
					}
				}
				break;
			case SValue:
			case DValue:
				char quote = state == SValue ? '\'' : '"';
				int end = text.indexOf(quote, pos);
				if (end == -1) {
					return Error;
				}
				if (text.charAt(end - 1) == '\\') {
					v.append(text.substring(pos, pos + end - pos - 1)).append(
							quote);
				} else {
					v.append(text.substring(pos, pos + end - pos));
					result.add(new WString(v.toString()));
					state = Next;
				}
				pos = end;
			}
		}
		return pos == text.length() ? -1 : pos;
	}

	static String DropShadow_x1_x2 = "<span class=\"Wt-x1\"><span class=\"Wt-x1a\"></span></span><span class=\"Wt-x2\"><span class=\"Wt-x2a\"></span></span>";
	/**
	 * A collection of predefined functions.
	 */
	public static WTemplate.FunctionsList Functions = new WTemplate.FunctionsList();
}
