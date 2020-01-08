/*
 * Copyright (C) 2009 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.composer;

import java.io.File;
import java.util.ArrayList;

import eu.webtoolkit.jwt.Signal;
import eu.webtoolkit.jwt.TextFormat;
import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WBreak;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WText;

/**
 * Main widget of the %Composer example.
 */
public class ComposeExample extends WContainerWidget {
    /**
     * create a new Composer example.
     */
    public ComposeExample(WContainerWidget parent) {
        super(parent);
        composer_ = new Composer(this);

        ArrayList<Contact> addressBook = new ArrayList<Contact>();
        addressBook
                .add(new Contact("Koen Deforche", "koen.deforche@gmail.com"));
        addressBook.add(new Contact("Koen alias1", "koen.alias1@yahoo.com"));
        addressBook.add(new Contact("Koen alias2", "koen.alias2@yahoo.com"));
        addressBook.add(new Contact("Koen alias3", "koen.alias3@yahoo.com"));
        addressBook.add(new Contact("Bartje", "jafar@hotmail.com"));
        composer_.setAddressBook(addressBook);

        ArrayList<Contact> contacts = new ArrayList<Contact>();
        contacts.add(new Contact("Koen Deforche", "koen.deforche@gmail.com"));

        composer_.setTo(contacts);
        composer_.setSubject("That's cool! Want to start your own google?");

        composer_.send.addListener(this, new Signal.Listener() {
            public void trigger() {
                send();
            }
        });
        composer_.discard.addListener(this, new Signal.Listener() {
            public void trigger() {
                discard();
            }
        });

        details_ = new WContainerWidget(this);
    }

    public ComposeExample() {
        this(null);
    }

    private Composer composer_;

    private WContainerWidget details_;

    private void send() {
        WContainerWidget feedback = new WContainerWidget(this);
        feedback.setStyleClass("feedback");

        WContainerWidget horiz = new WContainerWidget(feedback);
        new WText(
                "<p>We could have, but did not send the following email:</p>",
                horiz);

        ArrayList<Contact> contacts = composer_.to();
        if (!contacts.isEmpty())
            horiz = new WContainerWidget(feedback);
        for (int i = 0; i < contacts.size(); ++i) {
            new WText("To: \"" + contacts.get(i).name + "\" <"
                    + contacts.get(i).email + ">", TextFormat.PlainText, horiz);
            new WBreak(horiz);
        }

        contacts = composer_.cc();
        if (!contacts.isEmpty())
            horiz = new WContainerWidget(feedback);
        for (int i = 0; i < contacts.size(); ++i) {
            new WText("Cc: \"" + contacts.get(i).name + "\" <"
                    + contacts.get(i).email + ">", TextFormat.PlainText, horiz);
            new WBreak(horiz);
        }

        contacts = composer_.bcc();
        if (!contacts.isEmpty())
            horiz = new WContainerWidget(feedback);
        for (int i = 0; i < contacts.size(); ++i) {
            new WText("Bcc: \"" + contacts.get(i).name + "\" <"
                    + contacts.get(i).email + ">", TextFormat.PlainText, horiz);
            new WBreak(horiz);
        }

        horiz = new WContainerWidget(feedback);
        new WText("Subject: \"" + composer_.subject() + "\"", TextFormat.PlainText, horiz);

        ArrayList<Attachment> attachments = composer_.attachments();
        if (!attachments.isEmpty())
            horiz = new WContainerWidget(feedback);
        for (int i = 0; i < attachments.size(); ++i) {
            new WText("Attachment: \""
                    + attachments.get(i).fileName + "\" ("
                    + attachments.get(i).contentDescription + ")", TextFormat.PlainText, horiz);

            new File(attachments.get(i).spoolFileName).delete();

            new WText(", was in spool file: "
                    + attachments.get(i).spoolFileName, horiz);
            new WBreak(horiz);
        }

        String message = composer_.message();

        horiz = new WContainerWidget(feedback);
        new WText("Message body: ", horiz);
        new WBreak(horiz);

        if (message.length() != 0) {
            new WText(message, TextFormat.PlainText, horiz);
        } else
            new WText("<i>(empty)</i>", horiz);

        composer_.remove();
        details_.remove();

        WApplication.getInstance().quit();
    }

    private void discard() {
        WContainerWidget feedback = new WContainerWidget(this);
        feedback.setStyleClass("feedback");

        WContainerWidget horiz = new WContainerWidget(feedback);
        new WText(
                "<p>Wise decision! Everyone's mailbox is already full anyway.</p>",
                horiz);

        composer_.remove();
        details_.remove();

        WApplication.getInstance().quit();
    }
}
