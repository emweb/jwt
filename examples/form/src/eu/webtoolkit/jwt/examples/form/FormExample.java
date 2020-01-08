/*
 * Copyright (C) 2009 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.form;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import eu.webtoolkit.jwt.AlignmentFlag;
import eu.webtoolkit.jwt.Signal1;
import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WMouseEvent;
import eu.webtoolkit.jwt.WText;

/**
 * Main widget for the %Form example.
 * 
 * This class demonstrates, next instantiating the form itself, handling of
 * different languages.
 */
public class FormExample extends WContainerWidget {
    /**
     * Instantiate a new form example.
     */
    public FormExample(WContainerWidget parent) {
        super(parent);
        WContainerWidget langLayout = new WContainerWidget(this);
        langLayout.setContentAlignment(AlignmentFlag.AlignRight);
        new WText(tr("language"), langLayout);

        final String lang[] = { "en", "nl" };

        for (String l : lang) {
            final WText t = new WText(l, langLayout);
            t.setMargin(5);
            t.clicked().addListener(this, new Signal1.Listener<WMouseEvent>() {
                public void trigger(WMouseEvent a1) {
                    changeLanguage(t);
                }
            });

            languageSelects_.add(t);
        }

        /*
         * Start with the reported locale, if available
         */
        setLanguage(WApplication.getInstance().getLocale());

        Form form = new Form(this);
        form.setMargin(20);
    }

    /**
     * Change the language.
     */
    private void changeLanguage(WText t) {
        setLanguage(new Locale(t.getText().getValue()));
    }

    private List<WText> languageSelects_ = new ArrayList<WText>();

    private void setLanguage(Locale locale) {
        boolean haveLang = false;

        for (int i = 0; i < languageSelects_.size(); ++i) {
            WText t = languageSelects_.get(i);

            // prefix match, e.g. en matches en-us.
            boolean isLang = locale.toString().contains(t.getText().getValue());
            t.setStyleClass(isLang ? "langcurrent" : "lang");

            haveLang = haveLang || isLang;
        }

        if (!haveLang) {
            languageSelects_.get(0).setStyleClass("langcurrent");
            WApplication.getInstance().setLocale(
                    new Locale(languageSelects_.get(0).getText().getValue()));
        } else
            WApplication.getInstance().setLocale(locale);
    }
}
