/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.composer;

import java.util.EnumSet;

import eu.webtoolkit.jwt.AlignmentFlag;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WText;

/**
 * A label.
 *
 * A label is a WText that is styled as "label", and aligned
 * to the right in its parent.
 */
public class Label extends WText
{
	public Label(final String msg, WContainerWidget parent)
	{
		super(tr(msg), parent);

		setStyleClass("label");
		parent.setContentAlignment(EnumSet.of(AlignmentFlag.AlignRight));

	}
}
