package eu.webtoolkit.jwt;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.http.*;
import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.utils.*;
import eu.webtoolkit.jwt.servlet.*;

/**
 * A widget that allows a file to be uploaded
 * 
 * 
 * This widget is displayed as a box in which a filename can be entered and a
 * browse button.
 * <p>
 * Depending on availability of JavaScript, the behaviour of the widget is
 * different, but the API is designed in a way which facilitates a portable use.
 * <p>
 * When JavaScript is available, the file will not be uploaded until
 * {@link WFileUpload#upload()} is called. This will start an asynchronous
 * upload (and thus return immediately). When the file has been uploaded, the
 * {@link WFileUpload#uploaded()} signal is emitted, or if the file was too
 * large, the {@link WFileUpload#fileTooLarge()} signal is emitted.
 * <p>
 * When no JavaScript is available, the file will be uploaded with the next
 * click event. Thus, {@link WFileUpload#upload()} has no effect -- the file
 * will already be uploaded, and the corresponding signals will already be
 * emitted. To test if {@link WFileUpload#upload()} will start an upload, you
 * may check using the {@link WFileUpload#canUpload()} call.
 * <p>
 * Thus, to properly use the widget, one needs to follow these rules:
 * <ul>
 * <li>
 * Be prepared to handle the {@link WFileUpload#uploaded()} or
 * {@link WFileUpload#fileTooLarge()} signals also when
 * {@link WFileUpload#upload()} was not called.</li>
 * <li>
 * Check using {@link WFileUpload#canUpload()} if {@link WFileUpload#upload()}
 * will schedule a new upload. if (!canUpload()) then
 * {@link WFileUpload#upload()} will not have any effect. if (
 * {@link WFileUpload#canUpload()}), {@link WFileUpload#upload()} will start a
 * new file upload, which completes succesfully using an
 * {@link WFileUpload#uploaded()} signal or a {@link WFileUpload#fileTooLarge()}
 * signals gets emitted.</li>
 * </ul>
 * <p>
 * The WFileUpload widget must be hidden or deleted when a file is received. In
 * addition it is wise to prevent the user from uploading the file twice as in
 * the example below.
 * <p>
 * The uploaded file is automatically spooled to a local temporary file which
 * will be deleted together with the {@link WFileUpload} widget, unless
 * {@link WFileUpload#stealSpooledFile()} is called.
 * <p>
 * The file upload itself corresponds to a
 * <code>&lt;input type=&quot;file&quot;&gt;</code> tag, but may be wrapped in a
 * <code>&lt;form&gt;</code> tag.
 * <p>
 * WFileUpload is an {@link WWidget#setInline(boolean inlined) inline} widget.
 */
public class WFileUpload extends WWebWidget {
	/**
	 * Construct a file upload widget.
	 */
	public WFileUpload(WContainerWidget parent) {
		super(parent);
		this.textSize_ = 20;
		this.spoolFileName_ = "";
		this.clientFileName_ = "";
		this.contentDescription_ = "";
		this.isStolen_ = false;
		this.doUpload_ = false;
		this.fileTooLarge_ = new Signal1<Integer>(this);
		boolean methodIframe = WApplication.getInstance().getEnvironment()
				.hasAjax();
		if (methodIframe) {
			this.fileUploadTarget_ = new WFileUploadResource(this);
		} else {
			this.fileUploadTarget_ = null;
		}
		this.setInline(true);
		if (!(this.fileUploadTarget_ != null)) {
			this.setFormObject(true);
		}
	}

	public WFileUpload() {
		this((WContainerWidget) null);
	}

	public void remove() {
		if (!this.isStolen_) {
			new File(this.spoolFileName_).delete();
		}
		super.remove();
	}

	/**
	 * Set the size of the file input.
	 */
	public void setFileTextSize(int chars) {
		this.textSize_ = chars;
	}

	/**
	 * Get the size of the file input.
	 */
	public int getFileTextSize() {
		return this.textSize_;
	}

	/**
	 * Get the spooled location of the uploaded file.
	 * 
	 * Returns the temporary filename in which the uploaded file was spooled.
	 * The file is guaranteed to exist as long as the {@link WFileUpload} widget
	 * is not deleted, or a new file is not uploaded.
	 * <p>
	 * 
	 * @see WFileUpload#stealSpooledFile()
	 * @see WFileUpload#uploaded()
	 */
	public String getSpoolFileName() {
		return this.spoolFileName_;
	}

	/**
	 * Get the client filename.
	 */
	public String getClientFileName() {
		return this.clientFileName_;
	}

	/**
	 * Get the client content description.
	 */
	public String getContentDescription() {
		return this.contentDescription_;
	}

	/**
	 * Steal the spooled file.
	 * 
	 * By stealing the file, the spooled file will no longer be deleted together
	 * with this widget, which means you need to take care of managing that.
	 */
	public void stealSpooledFile() {
		this.isStolen_ = true;
	}

	/**
	 * Check if no filename was given and thus no file uploaded.
	 * 
	 * Return whether a non-empty filename was given.
	 */
	public boolean isEmptyFileName() {
		return this.clientFileName_.length() == 0;
	}

	/**
	 * Returns whether {@link WFileUpload#upload()} will start a new file
	 * upload.
	 * 
	 * A call to {@link WFileUpload#upload()} will only start a new file upload
	 * if there is no JavaScript support. Otherwise, the most recent file will
	 * already be uploaded.
	 */
	public boolean canUpload() {
		return this.fileUploadTarget_ != null;
	}

	/**
	 * Signal emitted when a new file was uploaded.
	 * 
	 * This signal is emitted when a new file has been received. It is good
	 * practice to hide or delete the {@link WFileUpload} widget when a file has
	 * been uploaded succesfully.
	 * <p>
	 * 
	 * @see WFileUpload#upload()
	 * @see WFileUpload#fileTooLarge()
	 */
	public EventSignal uploaded() {
		return this.voidEventSignal(UPLOADED_SIGNAL, true);
	}

	/**
	 * Signal emitted when the user tried to upload a too large file.
	 * 
	 * The parameter is the approximate size of the file the user tried to
	 * upload.
	 * <p>
	 * The maximum file size is determined by the maximum request size, which
	 * may be configured in the configuration file (&lt;max-request-size&gt;).
	 * <p>
	 * 
	 * @see WFileUpload#uploaded()
	 * @see WApplication#requestTooLarge()
	 */
	public Signal1<Integer> fileTooLarge() {
		return this.fileTooLarge_;
	}

	/**
	 * Signal emitted when the user selected a new file.
	 * 
	 * One could react on the user selecting a (new) file, by uploading the file
	 * immediately.
	 * <p>
	 * Caveat: this signal is not emitted with konqueror and possibly other
	 * browsers. Thus, in the above scenario you should still provide an
	 * alternative way to call the {@link WFileUpload#upload()} method.
	 */
	public EventSignal changed() {
		return this.voidEventSignal(CHANGE_SIGNAL, true);
	}

	/**
	 * Start the file upload.
	 * 
	 * The {@link WFileUpload#uploaded()} signal is emitted when a file is
	 * uploaded, or the {@link WFileUpload#fileTooLarge()} signal is emitted
	 * when the file size exceeded the maximum request size.
	 * <p>
	 * 
	 * @see WFileUpload#uploaded()
	 * @see WFileUpload#canUpload()
	 */
	public void upload() {
		if (this.fileUploadTarget_ != null) {
			this.doUpload_ = true;
			this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyIEMobile));
		}
	}

	private int textSize_;
	private String spoolFileName_;
	private String clientFileName_;
	private String contentDescription_;
	private boolean isStolen_;
	private boolean doUpload_;
	private Signal1<Integer> fileTooLarge_;
	private WResource fileUploadTarget_;

	void requestTooLarge(int size) {
		this.fileTooLarge().trigger(size);
	}

	protected void updateDom(DomElement element, boolean all) {
		if (this.fileUploadTarget_ != null && this.doUpload_) {
			element.callMethod("submit()");
			this.doUpload_ = false;
		}
		super.updateDom(element, all);
	}

	protected DomElement createDomElement(WApplication app) {
		DomElement result = DomElement.createNew(this.getDomElementType());
		result.setId(this, true);
		EventSignal change = this.voidEventSignal(CHANGE_SIGNAL, false);
		if (this.fileUploadTarget_ != null) {
			DomElement form = result;
			form.setAttribute("method", "post");
			form.setAttribute("action", this.fileUploadTarget_.generateUrl());
			form.setAttribute("enctype", "multipart/form-data");
			form.setAttribute("style", "margin:0;padding:0;display:inline");
			form
					.setProperty(Property.PropertyTarget, "if"
							+ this.getFormName());
			DomElement i = DomElement
					.createNew(DomElementType.DomElement_IFRAME);
			i.setAttribute("class", "Wt-resource");
			i.setAttribute("src", this.fileUploadTarget_.generateUrl());
			i.setId("if" + this.getFormName(), true);
			DomElement d = DomElement.createNew(DomElementType.DomElement_SPAN);
			d.addChild(i);
			form.addChild(d);
			DomElement input = DomElement
					.createNew(DomElementType.DomElement_INPUT);
			input.setAttribute("type", "file");
			input.setAttribute("name", "data");
			input.setAttribute("size", String.valueOf(this.textSize_));
			input.setId("in" + this.getFormName());
			if (change != null) {
				this.updateSignalConnection(input, change, "change", true);
			}
			form.addChild(input);
		} else {
			result.setAttribute("type", "file");
			result.setAttribute("size", String.valueOf(this.textSize_));
			if (change != null) {
				this.updateSignalConnection(result, change, "change", true);
			}
		}
		this.updateDom(result, true);
		return result;
	}

	protected DomElementType getDomElementType() {
		return this.fileUploadTarget_ != null ? DomElementType.DomElement_FORM
				: DomElementType.DomElement_INPUT;
	}

	protected void propagateRenderOk(boolean deep) {
		super.propagateRenderOk(deep);
	}

	protected void setFormData(WObject.FormData formData) {
		if (formData.file != null) {
			this.setFormData(formData.file);
		}
	}

	protected void setFormData(UploadedFile file) {
		this.spoolFileName_ = file.getSpoolFileName();
		this.clientFileName_ = new WString(file.getClientFileName()).toString();
		this.contentDescription_ = new WString(file.getContentType())
				.toString();
		file.stealSpoolFile();
		this.isStolen_ = false;
	}

	private static String CHANGE_SIGNAL = "M_change";
	private static String UPLOADED_SIGNAL = "M_uploaded";
}
