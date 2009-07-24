/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.composer;

import java.util.List;

import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WSuggestionPopup;

/**
 * A suggestion popup suggesting contacts from an addressbook.
 * 
 * This popup provides suggestions from a list of contact, by matching parts of
 * the name or email adress with the current value being edited. It also
 * supports editing a list of email addresses.
 * 
 * The popup is only available when JavaScript is available, and is implemented
 * entirely on the client-side.
 */
public class ContactSuggestions extends WSuggestionPopup {
    private static WSuggestionPopup.Options contactOptions = new WSuggestionPopup.Options();

    static {
        contactOptions.highlightBeginTag = "<b>";
        contactOptions.highlightEndTag = "</b>";
        contactOptions.listSeparator = ',';
        contactOptions.whitespace = " \\n";
        contactOptions.wordSeparators = "-., \"@\\n;";
        contactOptions.appendReplacedText = ", ";
    }

    /**
     * Create a new ContactSuggestions popup.
     */
    public ContactSuggestions(WContainerWidget parent) {
        super(WSuggestionPopup.generateMatcherJS(contactOptions),
                WSuggestionPopup.generateReplacerJS(contactOptions), parent);
    }

    /**
     * Set the address book.
     */
    public void setAddressBook(List<Contact> contacts) {
        clearSuggestions();

        for (int i = 0; i < contacts.size(); ++i)
            addSuggestion(contacts.get(i).formatted(), contacts.get(i)
                    .formatted());
    }
}