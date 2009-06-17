package eu.webtoolkit.jwt;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A widget that renders a Flash object (also known as Flash movie)
 * <p>
 * 
 * This class dynamically loads a .swf Flash file in the browser.
 * <p>
 * This widget is a container, which means that you can instantiate additional
 * widgets inside it. These widgets can for example be the content that is shown
 * when a Flash player is not available on the system or when JavaScript is
 * disabled.
 * <p>
 * Usage example: <code>
 WFlash *player = new WFlash(&quot;dummy.swf&quot;, parent); <br> 
 player-&gt;resize(300, 600); <br> 
 player-&gt;setFlashParameter(&quot;allowScriptAccess&quot;, &quot;always&quot;); <br> 
 player-&gt;setFlashParameter(&quot;quality&quot;, &quot;high&quot;); <br> 
 player-&gt;setFlashParameter(&quot;bgcolor&quot;, &quot;#aaaaaa&quot;); <br> 
 player-&gt;setFlashVariable(&quot;someVar&quot;, &quot;foo&quot;);
</code>
 * <p>
 * This class uses <i>resourcesURL</i>&quot;swfobject.js&quot;, a companion
 * JavaScript library, which is distributed with Wt in the resources folder.
 * <i>resourcesURL</i> is the configuration property that locates the Wt
 * resources/ folder inside your docroot.* This class requires swfobject.js in
 * the resources folder.
 */
public class WFlashObject extends WContainerWidget {
	/**
	 * Constructs a Flash widget.
	 */
	public WFlashObject(String url, WContainerWidget parent) {
		super(parent);
		this.url_ = url;
		this.isRendered_ = false;
		this.sizeChanged_ = false;
		this.parameters_ = new HashMap<String, WString>();
		this.variables_ = new HashMap<String, WString>();
		WApplication.getInstance().require(
				WApplication.getResourcesUrl() + "swfobject.js");
	}

	/**
	 * Constructs a Flash widget.
	 * <p>
	 * Calls {@link #WFlashObject(String url, WContainerWidget parent) this(url,
	 * (WContainerWidget)null)}
	 */
	public WFlashObject(String url) {
		this(url, (WContainerWidget) null);
	}

	/**
	 * Destructor.
	 * <p>
	 * The Flash object is removed.
	 */
	public void remove() {
		WApplication.getInstance().doJavaScript(
				"swfobject.removeSWF(flash" + this.getFormName() + ");");
		super.remove();
	}

	public void resize(WLength width, WLength height) {
		if (this.isRendered_) {
			this.sizeChanged_ = true;
		}
		super.resize(width, height);
	}

	/**
	 * Sets a Flash parameter.
	 * <p>
	 * The Flash parameters are items such as quality, scale, menu, ...
	 * Depending on the browser they are passed as attributes or PARAM objects
	 * to the Flash movie. See the adobe website for more information about
	 * these parameters: <a
	 * href="http://www.adobe.com/cfusion/knowledgebase/index.cfm?id=tn_12701"
	 * >http://www.adobe.com/cfusion/knowledgebase/index.cfm?id=tn_12701</a>
	 * <p>
	 * Setting the same Flash parameter a second time will overwrite the
	 * previous value. Flash parameters can only be set before the widget is
	 * rendered for the first time, so it is recommended to call this method
	 * shortly after construction before returning to the idle loop.
	 */
	public void setFlashParameter(String name, CharSequence value) {
		WString v = new WString(value);
		this.parameters_.put(name, v);
	}

	/**
	 * Sets a Flash variable.
	 * <p>
	 * This method is a helper function to set variable values in the flashvars
	 * parameter.
	 * <p>
	 * Setting the same Flash parameter a second time will overwrite the
	 * previous value. Flash parameters can only be set before the widget is
	 * rendered for the first time, so it is recommended to call this method
	 * shortly after construction before returning to the idle loop.
	 */
	public void setFlashVariable(String name, CharSequence value) {
		WString v = new WString(value);
		this.variables_.put(name, v);
	}

	/**
	 * A JavaScript expression that returns the DOM node of the Flash object.
	 * <p>
	 * The Flash object is not stored in {@link WWidget#getJsRef()}, but in
	 * {@link WFlashObject#getJsFlashRef()}. Use this method in conjuction with
	 * {@link WApplication#doJavaScript(String javascript, boolean afterLoaded)}
	 * or {@link JSlot} in custom JavaScript code to refer to the Flash content.
	 */
	public String getJsFlashRef() {
		return "Wt2_99_2.getElement('flash" + this.getFormName() + "')";
	}

	protected void getDomChanges(List<DomElement> result, WApplication app) {
		super.getDomChanges(result, app);
		if (this.isRendered_ && this.sizeChanged_) {
			DomElement innerElement = DomElement.getForUpdate("flahs"
					+ this.getFormName(), DomElementType.DomElement_DIV);
			innerElement.setAttribute("width", String.valueOf((int) this
					.getWidth().toPixels()));
			innerElement.setAttribute("height", String.valueOf((int) this
					.getHeight().toPixels()));
			result.add(innerElement);
		}
	}

	protected DomElement createDomElement(WApplication app) {
		DomElement result = super.createDomElement(app);
		DomElement innerElement = DomElement
				.createNew(DomElementType.DomElement_DIV);
		innerElement.setId("flash" + this.getFormName());
		result.addChild(innerElement);
		String flashvars = mapToJsMap(this.variables_);
		String params = mapToJsMap(this.parameters_);
		String attributes = "{}";
		if (!this.getStyleClass().equals("")) {
			attributes = "{ styleclass: "
					+ jsStringLiteral(this.getStyleClass()) + " }";
		}
		WApplication.getInstance().doJavaScript(
				"swfobject.embedSWF(\"" + this.url_ + "\", \"" + "flash"
						+ this.getFormName() + "\", \""
						+ String.valueOf((int) this.getWidth().toPixels())
						+ "\", \""
						+ String.valueOf((int) this.getHeight().toPixels())
						+ "\", \"8.0.0\", " + "false, " + flashvars + ", "
						+ params + ", " + attributes + ");");
		this.isRendered_ = true;
		return result;
	}

	private String url_;
	private boolean isRendered_;
	private boolean sizeChanged_;
	private Map<String, WString> parameters_;
	private Map<String, WString> variables_;

	static String mapToJsMap(Map<String, WString> map) {
		StringWriter ss = new StringWriter();
		boolean first = true;
		ss.append("{");
		for (Iterator<Map.Entry<String, WString>> i_it = map.entrySet()
				.iterator(); i_it.hasNext();) {
			Map.Entry<String, WString> i = i_it.next();
			if (first) {
				first = false;
			} else {
				ss.append(", ");
			}
			ss.append(i.getKey()).append(": ").append(
					i.getValue().getJsStringLiteral());
		}
		ss.append("}");
		return ss.toString();
	}
}
