/*
 * Copyright (C) 2009 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.composer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

import eu.webtoolkit.jwt.AlignmentFlag;
import eu.webtoolkit.jwt.LengthUnit;
import eu.webtoolkit.jwt.Side;
import eu.webtoolkit.jwt.Signal;
import eu.webtoolkit.jwt.Signal1;
import eu.webtoolkit.jwt.WCompositeWidget;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WImage;
import eu.webtoolkit.jwt.WLength;
import eu.webtoolkit.jwt.WLineEdit;
import eu.webtoolkit.jwt.WLink;
import eu.webtoolkit.jwt.WMouseEvent;
import eu.webtoolkit.jwt.WPushButton;
import eu.webtoolkit.jwt.WTable;
import eu.webtoolkit.jwt.WText;
import eu.webtoolkit.jwt.WTextArea;

/**
 * An E-mail composer widget.
 * 
 * This widget is part of the %Wt composer example.
 */
public class Composer extends WCompositeWidget {
    /**
     * Construct a new Composer
     */
    public Composer(WContainerWidget parent) {
        super(parent);
        saving_ = false;
        sending_ = false;

        setImplementation(layout_ = new WContainerWidget());

        createUi();
    }

    public Composer() {
        this(null);
    }

    /**
     * Set message To: contacts
     */
    public void setTo(final ArrayList<Contact> to) {
        toEdit_.setAddressees(to);
    }

    /**
     * Set subject.
     */
    public void setSubject(final String subject) {
        subject_.setText(subject);
    }

    /**
     * Set the message.
     */
    public void setMessage(final String message) {
        message_.setText(message);
    }

    /**
     * Set the address book, for autocomplete suggestions.
     */
    public void setAddressBook(final ArrayList<Contact> addressBook) {
        contactSuggestions_.setAddressBook(addressBook);
    }

    /**
     * Get the To: contacts.
     */
    public ArrayList<Contact> to() {
        return toEdit_.addressees();
    }

    /**
     * Get the Cc: contacts.
     */
    public ArrayList<Contact> cc() {
        return ccEdit_.addressees();
    }

    /**
     * Get the Bc: contacts.
     */
    public ArrayList<Contact> bcc() {
        return bccEdit_.addressees();
    }

    /**
     * Get the subject.
     */
    public String subject() {
        return subject_.getText();
    }

    /**
     * Get the list of attachments. The ownership of the attachment spool files
     * is transferred to the caller as well, be sure to delete them !
     */
    public ArrayList<Attachment> attachments() {
        ArrayList<Attachment> attachments = new ArrayList<Attachment>();

        for (int i = 0; i < attachments_.size() - 1; ++i) {
        	List<Attachment> toadd = attachments_.get(i).attachments();
        	attachments.addAll(toadd);
        }

        return attachments;
    }

    /**
     * Get the message.
     */
    public String message() {
        return message_.getText();
    }

    /**
     * The message is ready to be sent...
     */
    public Signal send = new Signal();

    /**
     * The message must be discarded.
     */
    public Signal discard = new Signal();

    private WContainerWidget layout_;

    private WPushButton topSendButton_, topSaveNowButton_, topDiscardButton_;

    private WPushButton botSendButton_, botSaveNowButton_, botDiscardButton_;

    private WText statusMsg_;

    private WTable edits_;

    /**
     * To: Addressees edit.
     */
    private AddresseeEdit toEdit_;

    /**
     * Cc: Addressees edit.
     */
    private AddresseeEdit ccEdit_;

    /**
     * Bcc: Addressees edit.
     */
    private AddresseeEdit bccEdit_;

    /**
     * The suggestions popup for the addressee edits.
     */
    private ContactSuggestions contactSuggestions_;

    /**
     * The subject line edit.
     */
    private WLineEdit subject_;

    /**
     * OptionsList for editing Cc or Bcc
     */
    private OptionList options_;

    /**
     * Option for editing Cc:
     */
    private Option addcc_;

    /**
     * Option for editing Bcc:
     */
    private Option addbcc_;

    /**
     * Option for attaching a file.
     */
    private Option attachFile_;

    /**
     * Option for attaching another file.
     */
    private Option attachOtherFile_;

    /**
     * Array which holds all the attachments, including one extra invisible one.
     */
    private ArrayList<AttachmentEdit> attachments_ = new ArrayList<AttachmentEdit>();

    /**
     * WTextArea for the main message.
     */
    private WTextArea message_;

    /**
     * state when waiting asyncrhonously for attachments to be uploaded
     */
    private boolean saving_, sending_;

    /**
     * number of attachments waiting to be uploaded during saving
     */
    private int attachmentsPending_;

    /**
     * Add an attachment edit.
     */
    private void attachMore() {
        /*
         * Create and append the next AttachmentEdit, that will be hidden.
         */
        AttachmentEdit edit = new AttachmentEdit(this, null);
        edits_.getElementAt(5, 1).insertBefore(edit, attachOtherFile_);
        attachments_.add(edit);
        attachments_.get(attachments_.size() - 1).hide();
    }

    /**
     * Remove the given attachment edit.
     */
    void removeAttachment(AttachmentEdit attachment) {
        /*
         * Remove the given attachment from the attachments list.
         */
        int i = attachments_.indexOf(attachment);
        if (i != -1) {
            attachments_.remove(i);
            attachment.remove();

            if (attachments_.size() == 1) {
                /*
                 * This was the last visible attachment, thus, we should switch
                 * the option control again.
                 */
                attachOtherFile_.hide();
                attachFile_.show();
            }
        }
    }

    /**
     * Slot attached to the Send button. Tries to save the mail message, and if
     * successful, sends it.
     */
    private void sendIt() {
        if (!sending_) {
            sending_ = true;

            /*
             * First save -- this will check for the sending_ state signal if
             * successful.
             */
            saveNow();
        }
    }

    /**
     * Slot attached to the Save now button. Tries to save the mail message, and
     * gives feedback on failure and on success.
     */
    private void saveNow() {
        if (!saving_) {
            saving_ = true;

            /*
             * Check if any attachments still need to be uploaded. This may be
             * the case when fileupload change events could not be caught (for
             * example in Konqueror).
             */
            attachmentsPending_ = 0;

            for (int i = 0; i < attachments_.size() - 1; ++i) {
                if (attachments_.get(i).uploadNow()) {
                    ++attachmentsPending_;

                    // this will trigger attachmentDone() when done, see
                    // the AttachmentEdit finalructor.
                }
            }

            System.err.println("Attachments pending: " + attachmentsPending_);
            if (attachmentsPending_ != 0)
                setStatusMsg("msg.uploading", "status");
            else
                saved();
        }
    }

    /**
     * Slot attached to the Discard button. Discards the current message: emits
     * the discard event.
     */
    private void discardIt() {
        discard.trigger();
    }

    /**
     * Slotcalled when an attachment has been uploaded. This used during while
     * saving the email and waiting for remaining attachments to be uploaded. It
     * is connected to the AttachmentEdit control signals that are emitted when
     * an attachment has been processed.
     */
    void attachmentDone() {
        if (saving_) {
            --attachmentsPending_;
            System.err.println("Attachments still: " + attachmentsPending_);

            if (attachmentsPending_ == 0)
                saved();
        }

    }

    /**
     * Ceate the user-interface.
     */
    private void createUi() {
        setStyleClass("darker");

        // horizontal layout container, used for top and bottom buttons.
        WContainerWidget horiz;

        /*
         * Top buttons
         */
        horiz = new WContainerWidget(layout_);
        horiz.setPadding(new WLength(5), Side.AllSides);
        topSendButton_ = new WPushButton(tr("msg.send"), horiz);
        topSendButton_.setStyleClass("default"); // default action
        topSaveNowButton_ = new WPushButton(tr("msg.savenow"), horiz);
        topDiscardButton_ = new WPushButton(tr("msg.discard"), horiz);

        // Text widget which shows status messages, next to the top buttons.
        statusMsg_ = new WText(horiz);
        statusMsg_.setMargin(new WLength(15, LengthUnit.Pixel), EnumSet
                .of(Side.Left));

        /*
         * To, Cc, Bcc, Subject, Attachments They are organized in a two-column
         * table: left column for labels, and right column for the edit.
         */
        edits_ = new WTable(layout_);
        edits_.setStyleClass("lighter");
        edits_.resize(new WLength(100, LengthUnit.Percentage), new WLength());
        edits_.getElementAt(0, 0).resize(
                new WLength(1, LengthUnit.Percentage), new WLength());

        /*
         * To, Cc, Bcc
         */
        toEdit_ = new AddresseeEdit("msg.to", edits_.getElementAt(0, 1), edits_
                .getElementAt(0, 0));
        // add some space above To:
        edits_.getElementAt(0, 1).setMargin(new WLength(5),
                EnumSet.of(Side.Top));
        ccEdit_ = new AddresseeEdit("msg.cc", edits_.getElementAt(1, 1), edits_
                .getElementAt(1, 0));
        bccEdit_ = new AddresseeEdit("msg.bcc", edits_.getElementAt(2, 1),
                edits_.getElementAt(2, 0));

        ccEdit_.hide();
        bccEdit_.hide();

        /*
         * Addressbook suggestions popup
         */
        contactSuggestions_ = new ContactSuggestions(layout_);
        contactSuggestions_.forEdit(toEdit_);
        contactSuggestions_.forEdit(ccEdit_);
        contactSuggestions_.forEdit(bccEdit_);

        /*
         * We use an OptionList widget to show the expand options for ccEdit_
         * and bccEdit_ nicely next to each other, separated by pipe characters.
         */
        options_ = new OptionList(edits_.getElementAt(3, 1));

        options_.add(addcc_ = new Option(tr("msg.addcc")));
        options_.add(addbcc_ = new Option(tr("msg.addbcc")));
        /*
         * Subject
         */
        new Label("msg.subject", edits_.getElementAt(4, 0));
        subject_ = new WLineEdit(edits_.getElementAt(4, 1));
        subject_
                .resize(new WLength(99, LengthUnit.Percentage), new WLength());

        /*
         * Attachments
         */
        new WImage(new WLink("pics/paperclip.png"), edits_.getElementAt(5, 0));
        edits_.getElementAt(5, 0).setContentAlignment(
                EnumSet.of(AlignmentFlag.Top, AlignmentFlag.Right));

        // Attachment edits: we always have the next attachmentedit ready
        // but hidden. This improves the response time, since the show()
        // and hide() slots are stateless.
        attachments_.add(new AttachmentEdit(this, edits_.getElementAt(5, 1)));
        attachments_.get(attachments_.size() - 1).hide();

        /*
         * Two options for attaching files. The first does not say 'another'.
         */
        attachFile_ = new Option(tr("msg.attachfile"), edits_.getElementAt(5, 1));
        attachOtherFile_ = new Option(tr("msg.attachanother"), edits_.getElementAt(
                5, 1));
        attachOtherFile_.hide();

        /*
         * Message
         */
        message_ = new WTextArea(layout_);
        message_.setColumns(80);
        message_.setRows(10); // should be 20, but let's keep it smaller
        message_.setMargin(new WLength(10, LengthUnit.Pixel));

        /*
         * Bottom buttons
         */
        horiz = new WContainerWidget(layout_);
        horiz.setPadding(new WLength(5), Side.AllSides);
        botSendButton_ = new WPushButton(tr("msg.send"), horiz);
        botSendButton_.setStyleClass("default");
        botSaveNowButton_ = new WPushButton(tr("msg.savenow"), horiz);
        botDiscardButton_ = new WPushButton(tr("msg.discard"), horiz);

        /*
         * Button events.
         */
        topSendButton_.clicked().addListener(this,
                new Signal1.Listener<WMouseEvent>() {

                    public void trigger(WMouseEvent a) {
                        sendIt();
                    }

                });
        botSendButton_.clicked().addListener(this,
                new Signal1.Listener<WMouseEvent>() {

                    public void trigger(WMouseEvent a) {
                        sendIt();
                    }
                });
        topSaveNowButton_.clicked().addListener(this,
                new Signal1.Listener<WMouseEvent>() {

                    public void trigger(WMouseEvent a) {
                        saveNow();
                    }

                });
        botSaveNowButton_.clicked().addListener(this,
                new Signal1.Listener<WMouseEvent>() {

                    public void trigger(WMouseEvent a) {
                        saveNow();
                    }

                });
        topDiscardButton_.clicked().addListener(this,
                new Signal1.Listener<WMouseEvent>() {

                    public void trigger(WMouseEvent a) {
                        discardIt();
                    }

                });
        botDiscardButton_.clicked().addListener(this,
                new Signal1.Listener<WMouseEvent>() {

                    public void trigger(WMouseEvent a) {
                        discardIt();
                    }

                });

        /*
         * Option events to show the cc or Bcc edit. Clicking on the option
         * should both show the corresponding edit, and hide the option itself.
         */

        addcc_.clicked().addListener(this, new Signal1.Listener<WMouseEvent>() {
            public void trigger(WMouseEvent a) {
                ccEdit_.setHidden(false);
                ccEdit_.setFocus();
                addcc_.setHidden(true);
                options_.update();
            }
        });

        addbcc_.clicked().addListener(this,
                new Signal1.Listener<WMouseEvent>() {
                    public void trigger(WMouseEvent a) {
                        bccEdit_.setHidden(false);
                        bccEdit_.setFocus();
                        addbcc_.setHidden(true);
                        options_.update();
                    }
        	});

        /*
         * Option event to attach the first attachment. We show the first
         * attachment, and call attachMore() to prepare the next attachment edit
         * that will be hidden. In addition, we need to show the 'attach More'
         * option, and hide the 'attach' option.
         */
        attachFile_.clicked().addListener(this,
                new Signal1.Listener<WMouseEvent>() {
                    public void trigger(WMouseEvent a) {
                        attachments_.get(attachments_.size() - 1).show();
                        attachOtherFile_.show();
                        attachFile_.hide();
                        attachMore();
                    }
                });

        attachOtherFile_.clicked().addListener(this,
                new Signal1.Listener<WMouseEvent>() {
                    public void trigger(WMouseEvent a) {
                        attachments_.get(attachments_.size() - 1).show();
                        attachMore();
                    }
                });
    }

    /**
     * All attachments have been processed, determine the result of saving the
     * message. addcc_.clicked.addListener(addcc_.doHide);
     */
    private void saved() {
        /*
         * All attachments have been processed.
         */

        boolean attachmentsFailed = false;
        for (int i = 0; i < attachments_.size() - 1; ++i)
            if (attachments_.get(i).uploadFailed()) {
                attachmentsFailed = true;
                break;
            }

        if (attachmentsFailed) {
            setStatusMsg("msg.attachment.failed", "error");
        } else {
            // Get today's date
            Date date = new Date();

            // Some examples
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss a");
            String s = formatter.format(date);

            setStatusMsg("msg.ok", "status");
            statusMsg_.setText("Draft saved at " + s);

            if (sending_) {
                send.trigger();
                return;
            }
        }

        saving_ = false;
        sending_ = false;

    }

    /**
     * Set the status message, and apply the given style.
     */
    private void setStatusMsg(final String msg, final String style) {
        statusMsg_.setText(tr(msg));
        statusMsg_.setStyleClass(style);
    }
}
