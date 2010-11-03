/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.composer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import eu.webtoolkit.jwt.AlignmentFlag;
import eu.webtoolkit.jwt.Side;
import eu.webtoolkit.jwt.Signal;
import eu.webtoolkit.jwt.Signal1;
import eu.webtoolkit.jwt.WAnchor;
import eu.webtoolkit.jwt.WCheckBox;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WFileResource;
import eu.webtoolkit.jwt.WFileUpload;
import eu.webtoolkit.jwt.WFont.Size;
import eu.webtoolkit.jwt.WLength;
import eu.webtoolkit.jwt.WMouseEvent;
import eu.webtoolkit.jwt.WProgressBar;
import eu.webtoolkit.jwt.WString;
import eu.webtoolkit.jwt.WText;
import eu.webtoolkit.jwt.WtServlet;
import eu.webtoolkit.jwt.servlet.UploadedFile;

/**
 * An edit field for an email attachment.
 * 
 * This widget managements one attachment edit: it shows a file upload control,
 * handles the upload, and gives feed-back on the file uploaded.
 * 
 * This widget is part of the JWt composer example.
 */
public class AttachmentEdit extends WContainerWidget {
	/**
	 * Creates an attachment edit field.
	 */
	public AttachmentEdit(final Composer composer, WContainerWidget parent) {
		super(parent);
		composer_ = composer;
		uploadDone_ = new Signal();
		uploadFailed_ = false;

		/*
		 * The file upload itself.
		 */
		upload_ = new WFileUpload(this);
		upload_.setMultiple(true);
		upload_.setFileTextSize(40);

		/*
		 * A progress bar
		 */
		//Only set the progress bar when server push is supported
		if (WtServlet.isAsyncSupported()) {
			WProgressBar progress = new WProgressBar();
			progress.setFormat(WString.Empty);
			progress.setVerticalAlignment(AlignmentFlag.AlignMiddle);
			upload_.setProgressBar(progress);
		}

		/*
		 * The 'remove' option.
		 */
		remove_ = new Option(tr("msg.remove"), this);
		upload_.getDecorationStyle().getFont().setSize(Size.Smaller);
		upload_.setVerticalAlignment(AlignmentFlag.AlignMiddle);
		remove_.setMargin(5, Side.Left);

		remove_.getItem().clicked()
				.addListener(this, new Signal1.Listener<WMouseEvent>() {
					public void trigger(WMouseEvent arg) {
						hide();
					}
				});

		remove_.getItem().clicked()
				.addListener(this, new Signal1.Listener<WMouseEvent>() {
					public void trigger(WMouseEvent arg) {
						remove();
					}
				});

		// The error message.
		error_ = new WText("", this);
		error_.setStyleClass("error");
		error_.setMargin(new WLength(5), Side.Left);

		/*
		 * React to events.
		 */

		// Try to catch the fileupload change signal to trigger an upload.
		// We could do like google and at a delay with a WTimer as well...
		upload_.changed().addListener(upload_, new Signal.Listener() {
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
					public void trigger(Integer size) {
						fileTooLarge(size);
					}
				});

		/*
		 * Connect the uploadDone signal to the Composer's attachmentDone, so
		 * that the Composer can keep track of attachment upload progress, if it
		 * wishes.
		 */
		uploadDone_.addListener(composer, new Signal.Listener() {
			public void trigger() {
				composer.attachmentDone();
			}
		});
	}

	/**
	 * Updates the file now.
	 * 
	 * Returns whether a new file will be uploaded. If so, the uploadDone signal
	 * will be triggered when the file is uploaded (or failed to upload).
	 */
	public boolean uploadNow() {
		/*
		 * See if this attachment still needs to be uploaded, and return if a
		 * new asynchronous upload is started.
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
	 * Returns whether the upload failed.
	 */
	public boolean uploadFailed() {
		return uploadFailed_;
	}

	/**
	 * Returns the attachment.
	 */
	public List<Attachment> attachments() {
		List<Attachment> result = new ArrayList<Attachment>();

		for (int i = 0; i < uploadInfo_.size(); ++i) {
			if (uploadInfo_.get(i).keep_.isChecked()) {
				UploadedFile f = uploadInfo_.get(i).info_;
				f.stealSpoolFile();
				result.add(new Attachment(f.getClientFileName(), f
						.getContentType(), f.getSpoolFileName()));
			}
		}

		return result;
	}

	/**
	 * Signal emitted when new attachment(s) have been uploaded (or failed to
	 * upload.
	 */
	public Signal uploadDone() {
		return uploadDone_;
	}

	private Composer composer_;

	private Signal uploadDone_;

	/**
	 * The WFileUpload control.
	 */
	private WFileUpload upload_;

	private class UploadInfo extends WContainerWidget {
		public UploadInfo(UploadedFile f, WContainerWidget parent) {
			super(parent);
			info_ = f;

			/*
			 * Include the file ?
			 */
			keep_ = new WCheckBox(this);
			keep_.setChecked();

			/*
			 * Give information on the file uploaded.
			 */
			long fsize = new File(info_.getSpoolFileName()).length();
			String size;
			if (fsize < 1024)
				size = "" + fsize + " bytes";
			else
				size = "" + ((int) (fsize / 1024)) + "kb";

			String fn = info_.getClientFileName();

			downloadLink_ = new WAnchor("", fn + " (<i>"
					+ info_.getContentType() + "</i>) " + size, this);

			WFileResource res = new WFileResource(info_.getContentType(),
					info_.getSpoolFileName(), this);
			res.suggestFileName(info_.getClientFileName());
			downloadLink_.setResource(res);
		}

		public UploadedFile info_;

		/**
		 * Anchor referencing the file.
		 */
		public WAnchor downloadLink_;

		/**
		 * The check box to keep or discard the uploaded file.
		 */
		public WCheckBox keep_;
	};

	private List<UploadInfo> uploadInfo_ = new ArrayList<UploadInfo>();

	/**
	 * The text box to display an error (empty or too big file)
	 */
	private WText error_;

	/**
	 * The option to cancel the file upload
	 */
	private Option remove_;

	/**
	 * The state of the last upload process.
	 */
	private boolean uploadFailed_;

	/**
	 * Slot triggered when the WFileUpload completed an upload.
	 */
	private void uploaded() {
		List<UploadedFile> files = upload_.getUploadedFiles();

		if (files.size() != 0) {
			upload_.remove();
			upload_ = null;
			remove_.remove();
			remove_ = null;
			error_.remove();
			error_ = null;

			for (int i = 0; i < files.size(); ++i)
				uploadInfo_.add(new UploadInfo(files.get(i), this));
		} else {
			error_.setText(tr("msg.file-empty"));
			uploadFailed_ = true;
		}

		/*
		 * Signal to the Composer that a new asynchronous file upload was
		 * processed.
		 */
		uploadDone_.trigger();
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
		 * Signal to the Composer that a new asyncrhonous file upload was
		 * processed.
		 */
		uploadDone_.trigger();
	}

	/**
	 * Slot triggered when the users wishes to remove this attachment edit.
	 */
	public void remove() {
		super.remove();
		composer_.removeAttachment(this);
	}
}