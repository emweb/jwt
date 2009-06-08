package eu.webtoolkit.jwt.examples.composer;

import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WText;

/**
 * A clickable option
 *
 * This widget is part of the %Wt composer example.
 *
 * On its own, an option is a text which is style "option".
 * An Option may also be used as items in an OptionList.
 *
 * @see OptionList
 */
public class Option extends WContainerWidget
{
	/**
	 * Create an Option with the given WMessage msg.
	 */
	public Option(final String msg, WContainerWidget parent)
	{
		super(parent);
		sep_ = null;
		list_ = null;

		setInline(true);
		option_ = new WText(tr(msg), this);
		option_.setStyleClass("option");
	}

	public Option(final String msg)
	{
		this(msg, null);
	}

	/**
	 * Change the WMessage msg.
	 */
	public void setMessage(final String msg)
	{
		option_.setText(tr(msg));
	}

	public void setHidden(boolean hidden)
	{
		super.setHidden(hidden);

		if (list_ != null)
			list_.optionVisibilityChanged(this, hidden);
	}

	/**
	 * The option command text.
	 */
	private WText option_;

	/**
	 * The separator '|'
	 */
	private WText sep_;

	/**
	 * The list in which this option is managed, if managed.
	 */
	private OptionList list_;

	void setOptionList(OptionList l)
	{
		list_ = l;
	}

	/**
	 * Create and show the separator.
	 */
	void addSeparator()
	{
		sep_ = new WText("|", this);
		sep_.setStyleClass("sep");
	}

	/**
	 * Show the separator
	 */
	void showSeparator()
	{
		sep_.show();
	}

	/**
	 * Hide the separator
	 */
	void hideSeparator()
	{
		sep_.hide();
	}
}
