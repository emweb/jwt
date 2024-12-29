/*
 * Copyright (C) 2009 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.composer;

import java.util.ArrayList;
import java.util.StringTokenizer;

import eu.webtoolkit.jwt.LengthUnit;
import eu.webtoolkit.jwt.WAnimation;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WLength;
import eu.webtoolkit.jwt.WTextArea;

/**
 * An edit field for an email addressee.
 * 
 * This widget is part of the %Wt composer example.
 */
public class AddresseeEdit extends WTextArea {
    /**
     * Create a new addressee edit with the given label. finalructs also a
     * widget to hold the label in the labelParent. The label will be hidden and
     * shown together with this field.
     */
    public AddresseeEdit(final String labelMsg, WContainerWidget parent,
            WContainerWidget labelParent) {
        super(parent);
        label_ = new Label(labelMsg, labelParent);

        setRows(3);
        setColumns(55);
        resize(new WLength(99, LengthUnit.Percentage), new WLength());

        setInline(false); // for IE to position the suggestions well

    }

    /**
     * Set a list of addressees.
     */
    public void setAddressees(final ArrayList<Contact> contacts) {
        String text = "";

        for (int i = 0; i < contacts.size(); ++i) {
            if (i != 0)
                text += ", ";
            text += contacts.get(i).formatted();
        }

        setText(text);
    }

    /**
     * Get a list of addressees
     */
    public ArrayList<Contact> addressees() {
        ArrayList<Contact> result = new ArrayList<Contact>();
        parse(result);

        return result;
    }

    /**
     * Reimplement hide() and show() to also hide() and show() the label.
     */
    public void setHidden(boolean hidden, WAnimation animation) {
        super.setHidden(hidden, animation);
        label_.setHidden(hidden, animation);
    }

    /**
     * The label associated with this edit.
     */
    private Label label_;

    /**
     * Parse the addressees into a list of contacts.
     * 
     * @param contacts
     * @return
     */
    private boolean parse(ArrayList<Contact> contacts) {
        StringTokenizer csv = new StringTokenizer(getText(), ",");

        while (csv.hasMoreElements()) {
            String addressee = csv.nextToken();

            addressee = addressee.trim();

            int pos = addressee.lastIndexOf(' ');
            if (pos != -1) {
                String email = addressee.substring(pos + 1);
                String name = addressee.substring(0, pos);

                email = email.trim();
                name = name.trim();
                if (email.charAt(0) == '<')
                    email = email.substring(1);
                if (email.charAt(email.length() - 1) == '>')
                    email = email.substring(0, email.length() - 1);

                if (email.length() != 0)
                    contacts.add(new Contact(name, email));
            } else if (addressee.length() != 0)
                contacts.add(new Contact("", addressee));
        }

        return true;
    }
}
