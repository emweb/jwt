package eu.webtoolkit.jwt;


/**
 * A layout manager which arranges widgets horizontally
 * 
 * 
 * This convenience class creates a horizontal box layout, laying contained
 * widgets out from left to right.
 * <p>
 * See the {@link WBoxLayout} documentation for available member methods and
 * more information.
 * <p>
 * Usage example:
 * <p>
 * <code>
 WContainerWidget w = new WContainerWidget(this); <br> 
		 <br> 
 WHBoxLayout layout = new WHBoxLayout(); <br> 
 layout.addWidget(new WText(&quot;One&quot;)); <br> 
 layout.addWidget(new WText(&quot;Two&quot;)); <br> 
 layout.addWidget(new WText(&quot;Three&quot;)); <br> 
 layout.addWidget(new WText(&quot;Four&quot;)); <br> 
	  <br> 
 w.setLayout(layout, AlignmentFlag.AlignTop, AlignmentFlag.AlignJustify);
</code>
 * <p>
 * <p>
 * <i><b>Note:</b>First consider if you can achieve your layout using CSS !</i>
 * </p>
 * 
 * @see WVBoxLayout
 */
public class WHBoxLayout extends WBoxLayout {
	/**
	 * Create a new horizontal box layout.
	 * 
	 * Use <i>parent</i>=0 to created a layout manager that can be nested inside
	 * other layout managers.
	 */
	public WHBoxLayout(WWidget parent) {
		super(WBoxLayout.Direction.LeftToRight, parent);
	}

	public WHBoxLayout() {
		this((WWidget) null);
	}
}
