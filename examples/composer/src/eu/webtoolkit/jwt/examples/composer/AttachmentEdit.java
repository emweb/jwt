/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.composer;

import java.io.File;
import java.util.EnumSet;

import eu.webtoolkit.jwt.AlignmentFlag;
import eu.webtoolkit.jwt.Side;
import eu.webtoolkit.jwt.Signal;
import eu.webtoolkit.jwt.Signal1;
import eu.webtoolkit.jwt.WCheckBox;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WFileUpload;
import eu.webtoolkit.jwt.WFont;
import eu.webtoolkit.jwt.WLength;
import eu.webtoolkit.jwt.WMouseEvent;
import eu.webtoolkit.jwt.WProgressBar;
import eu.webtoolkit.jwt.WString;
import eu.webtoolkit.jwt.WText;

/**
 * An edit field for an email attachment.
 * 
 * This widget managements one attachment edit: it shows a file upload control,
 * handles the upload, and gives feed-back on the file uploaded.
 * 
 * This widget is part of the %Wt composer example.
 */
public class AttachmentEdit extends WContainerWidget {
    /**
     * Create an attachment edit field.
     */
    public AttachmentEdit(Composer composer, WContainerWidget parent) {
        super(parent);
        composer_ = composer;
        uploadFailed_ = false;
        /*
         * The file upload itself.
         */
        upload_ = new WFileUpload(this);
        
        WProgressBar progress = new WProgressBar();
        progress.setFormat(WString.Empty);
        progress.setVerticalAlignment(AlignmentFlag.AlignMiddle);
        upload_.setProgressBar(progress);
        
        upload_.setFileTextSize(40);

        /*
         * The 'remove' option.
         */
        remove_ = new Option("msg.remove", this);
        upload_.getDecorationStyle().getFont().setSize(WFont.Size.Smaller);
        remove_.setMargin(new WLength(5, WLength.Unit.Pixel), EnumSet
                .of(Side.Left));
        remove_.clicked().addListener(this,
                new Signal1.Listener<WMouseEvent>() {
                    public void trigger(WMouseEvent a) {
                        hide();
                    }
                });

        remove_.clicked().addListener(this,
                new Signal1.Listener<WMouseEvent>() {
                    public void trigger(WMouseEvent a) {
                        composer_.removeAttachment(AttachmentEdit.this);
                    }
                });

        /*
         * Fields that will display the feedback.
         */

        // The check box to include or exclude the attachment.
        keep_ = new WCheckBox(this);
        keep_.hide();

        // The uploaded file information.
        uploaded_ = new WText("", this);
        uploaded_.setStyleClass("option");
        uploaded_.hide();

        // The error message.
        error_ = new WText("", this);
        error_.setStyleClass("error");
        error_.setMargin(new WLength(5), EnumSet.of(Side.Left));

        /*
         * React to events.
         */

        // Try to catch the fileupload change signal to trigger an upload.
        // We could do like google and at a delay with a WTimer as well...
        upload_.changed().addListener(this, new Signal.Listener() {
            public void trigger() {
                upload_.upload();
            }
        });

        // React to a succesfull upload.
        upload_.uploaded().addListener(this, new Signal.Listener() {
            public void trigger() {
                uploaded();
            }
        });

        // React to a fileupload problem.
        upload_.fileTooLarge().addListener(this,
                new Signal1.Listener<Integer>() {
                    public void trigger(Integer a) {
                        fileTooLarge(a);
                    }
                });

        /*
         * Connect the uploadDone signal to the Composer's attachmentDone, so
         * that the Composer can keep track of attachment upload progress, if it
         * wishes.
         */
        uploadDone.addListener(this, new Signal.Listener() {
            public void trigger() {
                composer_.attachmentDone();
            }
        });
    }

    public AttachmentEdit(Composer composer) {
        this(composer, null);
    }

    /**
     * Update the file now. Returns whether a new file will be uploaded. If so,
     * the uploadDone signal will be signalled when the file is uploaded (or
     * failed to upload).
     */
    public boolean uploadNow() {
        /*
         * See if this attachment still needs to be uploaded, and return if a
         * new asyncrhonous upload is started.
         */
        if (upload_ != null) {
            if (upload_.canUpload()) {
                upload_.upload();
                return true;
            } else
                return false;
        } else
            return false;
    }

    /**
     * Return whether the upload failed.
     */
    public boolean uploadFailed() {
        return uploadFailed_;
    }

    /**
     * Return whether this attachment must be included in the message.
     */
    public boolean include() {
        return keep_.isChecked();
    }

    /**
     * Return the attachment.
     */
    public Attachment attachment() {
        return new Attachment(fileName_, contentDescription_, spoolFileName_);
    }

    /**
     * Signal emitted when a new attachment has been uploaded (or failed to
     * upload.
     */
    public Signal uploadDone = new Signal();

    private Composer composer_;

    /**
     * The WFileUpload control.
     */
    private WFileUpload upload_;

    /**
     * The text describing the uploaded file.
     */
    private WText uploaded_;

    /**
     * The check box to keep or discard the uploaded file.
     */
    private WCheckBox keep_;

    /**
     * The option to remove the file
     */
    private Option remove_;

    /**
     * The text box to display an error (empty or too big file)
     */
    private WText error_;

    /**
     * The state of the last upload process.
     */
    private boolean uploadFailed_;

    /**
     * The filename of the uploaded file.
     */
    private String fileName_;

    /**
     * The filename of the local spool file.
     */
    private String spoolFileName_;

    /**
     * The content description that was sent along with the file.
     */
    private String contentDescription_;

    /**
     * Slot triggered when the WFileUpload completed an upload.
     */
    private void uploaded() {
        if (!upload_.isEmptyFileName()) {
            fileName_ = upload_.getClientFileName();
            spoolFileName_ = upload_.getSpoolFileName();
            upload_.stealSpooledFile();
            contentDescription_ = upload_.getContentDescription();

            /*
             * Delete this widgets since we have a successful upload.
             */
            upload_.remove();
            upload_ = null;
            remove_.remove();
            remove_ = null;

            error_.setText("");

            /*
             * Include the file ?
             */
            keep_.show();
            keep_.setChecked();

            /*
             * Give information on the file uploaded.
             */
            File f = new File(spoolFileName_);
            String size;
            long fileSize = f.length();
            if (fileSize < 1024)
                size = fileSize + " bytes";
            else
                size = (long) (fileSize / 1024) + "kb";

            uploaded_.setText(escapeText(fileName_) + " (<i>"
                    + contentDescription_ + " </i>) " + size);
            uploaded_.show();

            uploadFailed_ = false;
        } else {
            error_.setText(tr("msg.file-empty"));
            uploadFailed_ = true;
        }

        /*
         * Signal to the Composer that a new asynchronous file upload was
         * processed.
         */
        uploadDone.trigger();
    }

    /**
     * Slot triggered when the WFileUpload received an oversized file.
     * 
     * @param size
     */
    private void fileTooLarge(int size) {
        error_.setText(tr("msg.file-too-large"));
        uploadFailed_ = true;

        /*
         * Signal to the Composer that a new asynchronous file upload was
         * processed.
         */
        uploadDone.trigger();
    }

    protected void finalize() throws Throwable {
        File f = new File(spoolFileName_);
        if (f.exists())
            f.delete();
    }
}
