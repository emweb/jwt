/*
 * Copyright (C) 2009 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.composer;

/**
 * An email contact.
 * 
 * This widget is part of the JWt composer example.
 */
public class Contact {
    /**
     * The contact name.
     */
    public String name;

    /**
     * The contact email address.
     */
    public String email;

    /**
     * Create a new contact.
     */
    public Contact(final String name_, final String email_) {
        name = name_;
        email = email_;
    }

    /**
     * Get the typical single string form: "name" <email>
     */
    public String formatted() {
        return '"' + name + "\" <" + email + ">";
    }
}
