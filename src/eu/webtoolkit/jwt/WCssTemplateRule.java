package eu.webtoolkit.jwt;


/**
 * Returns the declarations.
 * <p>
 * This is a semi-colon separated list of CSS declarations.
 */
public class WCssTemplateRule extends WCssRule {
	/**
	 * Returns the declarations.
	 * <p>
	 * This is a semi-colon separated list of CSS declarations.
	 */
	public WCssTemplateRule(String selector) {
		super(selector);
		this.widget_ = new WCssTemplateWidget(this);
	}

	/**
	 * Returns the declarations.
	 * <p>
	 * This is a semi-colon separated list of CSS declarations.
	 */
	public void remove() {
		if (this.widget_ != null)
			this.widget_.remove();
		super.remove();
	}

	/**
	 * Returns the declarations.
	 * <p>
	 * This is a semi-colon separated list of CSS declarations.
	 */
	public WWidget getTemplateWidget() {
		return this.widget_;
	}

	/**
	 * Returns the declarations.
	 * <p>
	 * This is a semi-colon separated list of CSS declarations.
	 */
	public String getDeclarations() {
		DomElement e = new DomElement(DomElement.Mode.ModeUpdate, this.widget_
				.getDomElementType());
		this.updateDomElement(e, true);
		return e.getCssStyle();
	}

	/**
	 * Returns the declarations.
	 * <p>
	 * This is a semi-colon separated list of CSS declarations.
	 */
	public boolean updateDomElement(DomElement element, boolean all) {
		this.widget_.updateDom(element, all);
		return true;
	}

	/**
	 * Returns the declarations.
	 * <p>
	 * This is a semi-colon separated list of CSS declarations.
	 */
	private WCssTemplateWidget widget_;
}
