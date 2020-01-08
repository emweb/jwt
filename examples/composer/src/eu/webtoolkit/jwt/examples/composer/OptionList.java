/*
 * Copyright (C) 2009 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.composer;

import java.util.ArrayList;

import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WLength;

/**
 * A list of options, separated by '|'
 * 
 * This widget is part of the JWt composer example.
 * 
 * An OptionList displays a list of Option widgets, which are separated by a '|'
 * separator.
 * 
 * For example, Foo | Bar | Huu
 * 
 * When Options are hidden, the separators are adjusted so that there is no
 * separator after the last visible option. However, this requires a call of
 * update() each time an option is hidden or shown. This is because the removing
 * of separators is optimized in stateless implementations, and thus in
 * client-side JavaScript code. Since the behaviour is not entirely stateless,
 * the update() method resets stateless implementations if necessary.
 * 
 * @see OptionList
 */
public class OptionList extends WContainerWidget {
    /**
     * Create an OptionList.
     */
    public OptionList(WContainerWidget parent) {
        super(parent);
        resize(new WLength(), new WLength(2.5, WLength.Unit.FontEx));
    }

    /**
     * Add an Option to the list.
     */
    public void add(Option option) {
        addWidget(option);
        option.setOptionList(this);

        if (options_.size() != 0) {
            options_.get(options_.size() - 1).addSeparator();
        }

        options_.add(option);
    }

    /**
     * Update the stateless implementations after an Option has been hidden or
     * shown.
     */
    public void update() {
    }

    /**
     * The list of options.
     */
    private ArrayList<Option> options_ = new ArrayList<Option>();

    /**
     * An option changed visibility: possibly update the separators inbetween
     * 
     * @param opt
     * @param hidden
     */
    void optionVisibilityChanged(Option opt, boolean hidden) {
        /*
         * Check if it was the last visible option, in that case the second last
         * visible option loses its separator.
         */
        for (int i = options_.size() - 1; i > 0; --i) {
            if (options_.get(i) == opt) {
                for (int j = i - 1; j >= 0; --j) {
                    if (!options_.get(j).isHidden()) {
                        if (hidden)
                            options_.get(j).hideSeparator();
                        else
                            options_.get(j).showSeparator();
                        break;
                    }
                }
                break;
            } else if (!options_.get(i).isHidden())
                break;
        }
    }
}
