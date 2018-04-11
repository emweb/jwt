/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.lang.ref.*;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.http.*;
import javax.servlet.*;
import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.utils.*;
import eu.webtoolkit.jwt.servlet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A widget that allows dropping files for upload.
 * <p>
 * 
 * This widget accepts files that are dropped into it. A signal is triggered
 * whenever one or more files are dropped. The filename, type and size of these
 * files is immediately available through the {@link File} interface.
 * <p>
 * The file upload is done sequentially. All files before the
 * {@link WFileDropWidget#getCurrentIndex() getCurrentIndex()} have either
 * finished, failed or have been cancelled.
 * <p>
 * The widget has the default style-class &apos;Wt-filedropzone&apos;. The
 * style-class &apos;Wt-dropzone-hover&apos; is added when files are hovered
 * over the widget.
 */
public class WFileDropWidget extends WContainerWidget {
	private static Logger logger = LoggerFactory
			.getLogger(WFileDropWidget.class);

	/**
	 * A nested class of {@link WFileDropWidget} representing a file.
	 * <p>
	 * 
	 * The methods returning the filename, mime-type and size return valid
	 * values if the upload of this file is not yet finished. The method
	 * {@link WFileDropWidget.File#getUploadedFile() getUploadedFile()} is only
	 * available after the upload is finished.
	 */
	public static class File extends WObject {
		private static Logger logger = LoggerFactory.getLogger(File.class);

		/**
		 * Returns the client filename.
		 */
		public String getClientFileName() {
			return this.clientFileName_;
		}

		/**
		 * Returns the mime-type of the file.
		 */
		public String getMimeType() {
			return this.type_;
		}

		/**
		 * Returns the size of the file.
		 */
		public long getSize() {
			return this.size_;
		}

		/**
		 * Returns the uploaded file as a {@link UploadedFile}.
		 * <p>
		 * This method will throw an expection if the upload is not yet
		 * finished.
		 * <p>
		 * 
		 * @see WFileDropWidget.File#isUploadFinished()
		 */
		public UploadedFile getUploadedFile() {
			if (!this.uploadFinished_) {
				throw new RuntimeException();
			} else {
				return this.uploadedFile_;
			}
		}

		/**
		 * Returns true if the upload is finished.
		 * <p>
		 * When this method returns true, the uploaded file is available on the
		 * server.
		 * <p>
		 * 
		 * @see WFileDropWidget.File#getUploadedFile()
		 */
		public boolean isUploadFinished() {
			return this.uploadFinished_;
		}

		/**
		 * This signal allows you to track the upload progress of the file.
		 */
		public Signal2<Long, Long> dataReceived() {
			return this.dataReceived_;
		}

		/**
		 * This signal is triggered when the upload is finished.
		 * <p>
		 * This is also signalled using the {@link WFileDropWidget}
		 * {@link WFileDropWidget#uploaded() uploaded()} signal.
		 */
		public Signal uploaded() {
			return this.uploaded_;
		}

		File(int id, final String fileName, final String type, long size,
				WObject parent) {
			super(parent);
			this.id_ = id;
			this.clientFileName_ = fileName;
			this.type_ = type;
			this.size_ = size;
			this.uploadedFile_ = new UploadedFile();
			this.dataReceived_ = new Signal2<Long, Long>();
			this.uploaded_ = new Signal();
			this.uploadFinished_ = false;
			this.cancelled_ = false;
		}

		int getUploadId() {
			return this.id_;
		}

		void setUploadedFile(final UploadedFile file) {
			this.uploadFinished_ = true;
			this.uploadedFile_ = file;
		}

		void cancel() {
			this.cancelled_ = true;
		}

		public boolean isCancelled() {
			return this.cancelled_;
		}

		private int id_;
		private String clientFileName_;
		private String type_;
		private long size_;
		private UploadedFile uploadedFile_;
		private Signal2<Long, Long> dataReceived_;
		private Signal uploaded_;
		private boolean uploadFinished_;
		private boolean cancelled_;
	}

	/**
	 * Constructor.
	 */
	public WFileDropWidget(WContainerWidget parent) {
		super(parent);
		this.resource_ = null;
		this.currentFileIdx_ = 0;
		this.hoverStyleClass_ = "Wt-dropzone-hover";
		this.acceptDrops_ = true;
		this.acceptAttributes_ = "";
		this.dropIndicationEnabled_ = false;
		this.globalDropEnabled_ = false;
		this.dropSignal_ = new JSignal1<String>(this, "dropsignal") {
		};
		this.requestSend_ = new JSignal1<Integer>(this, "requestsend") {
		};
		this.fileTooLarge_ = new JSignal1<Long>(this, "filetoolarge") {
		};
		this.uploadFinished_ = new JSignal1<Integer>(this, "uploadfinished") {
		};
		this.doneSending_ = new JSignal(this, "donesending");
		this.dropEvent_ = new Signal1<List<WFileDropWidget.File>>();
		this.uploadStart_ = new Signal1<WFileDropWidget.File>();
		this.uploaded_ = new Signal1<WFileDropWidget.File>();
		this.tooLarge_ = new Signal2<WFileDropWidget.File, Long>();
		this.uploadFailed_ = new Signal1<WFileDropWidget.File>();
		this.uploads_ = new ArrayList<WFileDropWidget.File>();
		this.updateFlags_ = new BitSet();
		WApplication app = WApplication.getInstance();
		if (!app.getEnvironment().hasAjax()) {
			return;
		}
		this.setup();
	}

	/**
	 * Constructor.
	 * <p>
	 * Calls {@link #WFileDropWidget(WContainerWidget parent)
	 * this((WContainerWidget)null)}
	 */
	public WFileDropWidget() {
		this((WContainerWidget) null);
	}

	/**
	 * Returns the vector of uploads managed by this widget.
	 * <p>
	 * The files in this vector are handled sequentially by the widget. All
	 * {@link File} objects in this vector have either finished or failed if
	 * they are before the {@link WFileDropWidget#getCurrentIndex()
	 * getCurrentIndex()}, depending on the return value of
	 * {@link WFileDropWidget.File#isUploadFinished()
	 * WFileDropWidget.File#isUploadFinished()}. The other files are still being
	 * handled.
	 * <p>
	 * 
	 * @see WFileDropWidget#getCurrentIndex()
	 */
	public List<WFileDropWidget.File> getUploads() {
		return this.uploads_;
	}

	/**
	 * Return the index of the file that is currently being handled.
	 * <p>
	 * If nothing is to be done, this will return the size of the vector
	 * returned by {@link WFileDropWidget#getUploads() getUploads()}.
	 */
	public int getCurrentIndex() {
		return this.currentFileIdx_;
	}

	/**
	 * Cancels the upload of a file.
	 * <p>
	 * If you cancel a file that is still waiting to be uploaded, it will stay
	 * in the {@link WFileDropWidget#getUploads() getUploads()} vector, but it
	 * will be skipped.
	 */
	public void cancelUpload(WFileDropWidget.File file) {
		file.cancel();
		int i = file.getUploadId();
		this.doJavaScript(this.getJsRef() + ".cancelUpload("
				+ String.valueOf(i) + ");");
	}

	/**
	 * Removes the file.
	 * <p>
	 * This can be used to free resources of files that were already uploaded. A
	 * file can only be removed if its index in
	 * {@link WFileDropWidget#getUploads() getUploads()} is before the current
	 * index.
	 */
	public boolean remove(WFileDropWidget.File file) {
		for (int i = 0; i < this.currentFileIdx_ && i < this.uploads_.size(); i++) {
			if (this.uploads_.get(i) == file) {
				this.uploads_.remove(0 + i);
				this.currentFileIdx_--;
				return true;
			}
		}
		return false;
	}

	/**
	 * When set to false, the widget no longer accepts any files.
	 */
	public void setAcceptDrops(boolean enable) {
		if (enable == this.acceptDrops_) {
			return;
		}
		this.acceptDrops_ = enable;
		this.updateFlags_.set(BIT_ACCEPTDROPS_CHANGED);
		this.repaint();
	}

	/**
	 * Set the style class that is applied when a file is hovered over the
	 * widget.
	 * <p>
	 * 
	 * @deprecated Override the css rule
	 *             &apos;.Wt-filedropzone.Wt-dropzone-hover&apos; instead.
	 */
	public void setHoverStyleClass(final String className) {
		if (className.equals(this.hoverStyleClass_)) {
			return;
		}
		this.hoverStyleClass_ = className;
		this.updateFlags_.set(BIT_HOVERSTYLE_CHANGED);
		this.repaint();
	}

	/**
	 * Sets input accept attributes.
	 * <p>
	 * The accept attribute may be specified to provide user agents with a hint
	 * of what file types will be accepted. Use html input accept attributes as
	 * input. This only affects the popup that is shown when users click on the
	 * widget. A user can still drop any file type.
	 */
	public void setFilters(final String acceptAttributes) {
		if (acceptAttributes.equals(this.acceptAttributes_)) {
			return;
		}
		this.acceptAttributes_ = acceptAttributes;
		this.updateFlags_.set(BIT_FILTERS_CHANGED);
		this.repaint();
	}

	/**
	 * Highlight widget if a file is dragged anywhere on the page.
	 * <p>
	 * As soon as a drag enters anywhere on the page the styleclass
	 * &apos;Wt-dropzone-indication&apos; is added to this widget. This can be
	 * useful to point the user to the correct place to drop the file. Once the
	 * user drags a file over the widget itself, the styleclass
	 * &apos;Wt-dropzone-hover&apos; is also added. This can be enabled for
	 * multiple dropwidgets if only one of them is visible at the same time.
	 * <p>
	 * 
	 * @see WFileDropWidget#setGlobalDropEnabled(boolean enable)
	 */
	public void setDropIndicationEnabled(boolean enable) {
		if (enable == this.dropIndicationEnabled_) {
			return;
		}
		this.dropIndicationEnabled_ = enable;
		this.updateFlags_.set(BIT_DRAGOPTIONS_CHANGED);
		this.repaint();
	}

	/**
	 * Returns if the widget is highlighted for drags anywhere on the page.
	 * <p>
	 * 
	 * @see WFileDropWidget#setDropIndicationEnabled(boolean enable)
	 */
	public boolean isDropIndicationEnabled() {
		return this.dropIndicationEnabled_;
	}

	/**
	 * Allow dropping the files anywhere on the page.
	 * <p>
	 * If enabled, a drop anywhere on the page will be forwarded to this widget.
	 * <p>
	 * 
	 * @see WFileDropWidget#setDropIndicationEnabled(boolean enable)
	 */
	public void setGlobalDropEnabled(boolean enable) {
		if (enable == this.globalDropEnabled_) {
			return;
		}
		this.globalDropEnabled_ = enable;
		this.updateFlags_.set(BIT_DRAGOPTIONS_CHANGED);
		this.repaint();
	}

	/**
	 * Returns if all drops are forwarded to this widget.
	 * <p>
	 * 
	 * @see WFileDropWidget#setGlobalDropEnabled(boolean enable)
	 */
	public boolean isGlobalDropEnabled() {
		return this.globalDropEnabled_;
	}

	/**
	 * The signal triggers if one or more files are dropped.
	 */
	public Signal1<List<WFileDropWidget.File>> drop() {
		return this.dropEvent_;
	}

	/**
	 * The signal triggers when the upload of a file is about to begin.
	 * <p>
	 * After this signal is triggered, the upload automatically starts. The
	 * upload can still fail if the file is too large or if there is a network
	 * error.
	 */
	public Signal1<WFileDropWidget.File> newUpload() {
		return this.uploadStart_;
	}

	/**
	 * The signal is triggered if any file finished uploading.
	 */
	public Signal1<WFileDropWidget.File> uploaded() {
		return this.uploaded_;
	}

	/**
	 * The signal triggers when a file is too large for upload.
	 * <p>
	 * This signal is triggered when the widget attempts to upload the file.
	 */
	public Signal2<WFileDropWidget.File, Long> tooLarge() {
		return this.tooLarge_;
	}

	/**
	 * The signal triggers when an upload failed.
	 * <p>
	 * This signal will trigger when the widget skips over one of the files in
	 * the list for an unknown reason (e.g. happens when you drop a folder).
	 */
	public Signal1<WFileDropWidget.File> uploadFailed() {
		return this.uploadFailed_;
	}

	String renderRemoveJs(boolean recursive) {
		if (this.isRendered()) {
			String result = this.getJsRef() + ".destructor();";
			if (!recursive) {
				result += "Wt3_3_10.remove('" + this.getId() + "');";
			}
			return result;
		} else {
			return super.renderRemoveJs(recursive);
		}
	}

	protected void enableAjax() {
		this.setup();
		this.repaint();
		super.enableAjax();
	}

	void updateDom(final DomElement element, boolean all) {
		WApplication app = WApplication.getInstance();
		if (app.getEnvironment().hasAjax()) {
			if (this.updateFlags_.get(BIT_HOVERSTYLE_CHANGED) || all) {
				this.doJavaScript(this.getJsRef() + ".configureHoverClass('"
						+ this.hoverStyleClass_ + "');");
			}
			if (this.updateFlags_.get(BIT_ACCEPTDROPS_CHANGED) || all) {
				this.doJavaScript(this.getJsRef() + ".setAcceptDrops("
						+ (this.acceptDrops_ ? "true" : "false") + ");");
			}
			if (this.updateFlags_.get(BIT_FILTERS_CHANGED) || all) {
				this.doJavaScript(this.getJsRef() + ".setFilters("
						+ jsStringLiteral(this.acceptAttributes_) + ");");
			}
			if (this.updateFlags_.get(BIT_DRAGOPTIONS_CHANGED) || all) {
				this.doJavaScript(this.getJsRef() + ".setDropIndication("
						+ (this.dropIndicationEnabled_ ? "true" : "false")
						+ ");");
				this.doJavaScript(this.getJsRef() + ".setDropForward("
						+ (this.globalDropEnabled_ ? "true" : "false") + ");");
			}
			this.updateFlags_.clear();
		}
		super.updateDom(element, all);
	}

	static class WFileDropUploadResource extends WResource {
		private static Logger logger = LoggerFactory
				.getLogger(WFileDropUploadResource.class);

		public WFileDropUploadResource(WFileDropWidget fileDropWidget) {
			super(fileDropWidget);
			this.parent_ = fileDropWidget;
			this.app_ = WApplication.getInstance();
			this.setUploadProgress(true);
		}

		public void handleRequest(final WebRequest request,
				final WebResponse response) {
			WApplication.UpdateLock lock = WApplication.getInstance()
					.getUpdateLock();
			String fileId = request.getParameter("file-id");
			if (fileId == null || fileId.length() == 0) {
				response.setStatus(404);
				return;
			}
			int id = Integer.parseInt(fileId);
			boolean validId = this.parent_.incomingIdCheck(id);
			if (!validId) {
				response.setStatus(404);
				return;
			}
			List<UploadedFile> files = new ArrayList<UploadedFile>();
			CollectionUtils.findInMultimap(request.getUploadedFiles(), "data",
					files);
			if (files.isEmpty()) {
				response.setStatus(404);
				return;
			}
			this.parent_.setUploadedFile(files.get(0));
			response.setContentType("text/plain");
			lock.release();
		}

		public void setCurrentFile(WFileDropWidget.File file) {
			this.currentFile_ = file;
		}

		private WFileDropWidget parent_;
		private WApplication app_;
		private WFileDropWidget.File currentFile_;
	}

	private void setup() {
		WApplication app = WApplication.getInstance();
		app.loadJavaScript("js/WFileDropWidget.js", wtjs1());
		String maxFileSize = String.valueOf(WApplication.getInstance()
				.getMaximumRequestSize());
		this.setJavaScriptMember(" WFileDropWidget",
				"new Wt3_3_10.WFileDropWidget(" + app.getJavaScriptClass()
						+ "," + this.getJsRef() + "," + maxFileSize + ");");
		this.dropSignal_.addListener(this, new Signal1.Listener<String>() {
			public void trigger(String e1) {
				WFileDropWidget.this.handleDrop(e1);
			}
		});
		this.requestSend_.addListener(this, new Signal1.Listener<Integer>() {
			public void trigger(Integer e1) {
				WFileDropWidget.this.handleSendRequest(e1);
			}
		});
		this.fileTooLarge_.addListener(this, new Signal1.Listener<Long>() {
			public void trigger(Long e1) {
				WFileDropWidget.this.handleTooLarge(e1);
			}
		});
		this.uploadFinished_.addListener(this, new Signal1.Listener<Integer>() {
			public void trigger(Integer e1) {
				WFileDropWidget.this.emitUploaded(e1);
			}
		});
		this.doneSending_.addListener(this, new Signal.Listener() {
			public void trigger() {
				WFileDropWidget.this.stopReceiving();
			}
		});
		this.addStyleClass("Wt-filedropzone");
	}

	private void handleDrop(final String newDrops) {
		final com.google.gson.JsonElement dropdata;
		dropdata = (new com.google.gson.JsonParser()).parse(newDrops);
		;
		List<WFileDropWidget.File> drops = new ArrayList<WFileDropWidget.File>();
		com.google.gson.JsonArray dropped = (com.google.gson.JsonArray) dropdata;
		for (int i = 0; i < dropped.size(); ++i) {
			com.google.gson.JsonObject upload = (com.google.gson.JsonObject) dropped
					.get(i);
			int id = -1;
			long size = 0;
			String name = "";
			String type = "";
			for (Iterator<Map.Entry<String, com.google.gson.JsonElement>> it_it = upload
					.entrySet().iterator(); it_it.hasNext();) {
				Map.Entry<String, com.google.gson.JsonElement> it = it_it
						.next();
				if (it.getKey().equals("id")) {
					id = it.getValue().getAsInt();
				} else {
					if (it.getKey().equals("filename")) {
						name = it.getValue().getAsString();
					} else {
						if (it.getKey().equals("type")) {
							type = it.getValue().getAsString();
						} else {
							if (it.getKey().equals("size")) {
								size = it.getValue().getAsLong();
							} else {
								throw new RuntimeException();
							}
						}
					}
				}
			}
			WFileDropWidget.File file = new WFileDropWidget.File(id, name,
					type, size, this);
			drops.add(file);
			this.uploads_.add(file);
		}
		this.dropEvent_.trigger(drops);
		this.doJavaScript(this.getJsRef() + ".markForSending(" + newDrops
				+ ");");
	}

	private void handleTooLarge(long size) {
		if (this.currentFileIdx_ >= this.uploads_.size()) {
			return;
		}
		this.tooLarge_.trigger(this.uploads_.get(this.currentFileIdx_), size);
		this.currentFileIdx_++;
	}

	private void handleSendRequest(int id) {
		boolean fileFound = false;
		for (int i = this.currentFileIdx_; i < this.uploads_.size(); i++) {
			if (this.uploads_.get(i).getUploadId() == id) {
				fileFound = true;
				this.currentFileIdx_ = i;
				;
				this.resource_ = new WFileDropWidget.WFileDropUploadResource(
						this);
				this.resource_.dataReceived().addListener(this,
						new Signal2.Listener<Long, Long>() {
							public void trigger(Long e1, Long e2) {
								WFileDropWidget.this.onData(e1, e2);
							}
						});
				this.resource_.dataExceeded().addListener(this,
						new Signal1.Listener<Long>() {
							public void trigger(Long e1) {
								WFileDropWidget.this.onDataExceeded(e1);
							}
						});
				this.doJavaScript(this.getJsRef() + ".send('"
						+ this.resource_.getUrl() + "');");
				this.uploadStart_.trigger(this.uploads_
						.get(this.currentFileIdx_));
				break;
			} else {
				if (!this.uploads_.get(i).isCancelled()) {
					this.uploadFailed_.trigger(this.uploads_.get(i));
				}
			}
		}
		if (!fileFound) {
			this.doJavaScript(this.getJsRef() + ".cancelUpload("
					+ String.valueOf(id) + ");");
		} else {
			WApplication.getInstance().enableUpdates(true);
		}
	}

	private void emitUploaded(int id) {
		for (int i = 0; i < this.currentFileIdx_ && i < this.uploads_.size(); i++) {
			WFileDropWidget.File f = this.uploads_.get(i);
			if (f.getUploadId() == id) {
				f.uploaded().trigger();
				this.uploaded().trigger(f);
			}
		}
	}

	private void stopReceiving() {
		if (this.currentFileIdx_ < this.uploads_.size()) {
			for (int i = this.currentFileIdx_; i < this.uploads_.size(); i++) {
				if (!this.uploads_.get(i).isCancelled()) {
					this.uploadFailed_.trigger(this.uploads_.get(i));
				}
			}
			this.currentFileIdx_ = this.uploads_.size();
			WApplication.getInstance().enableUpdates(false);
		}
	}

	private void onData(long current, long total) {
		if (this.currentFileIdx_ >= this.uploads_.size()) {
			return;
		}
		WFileDropWidget.File file = this.uploads_.get(this.currentFileIdx_);
		file.dataReceived().trigger(current, total);
		WApplication.getInstance().triggerUpdate();
	}

	private void onDataExceeded(long dataExceeded) {
		if (this.currentFileIdx_ >= this.uploads_.size()) {
			return;
		}
		this.tooLarge_.trigger(this.uploads_.get(this.currentFileIdx_),
				dataExceeded);
		WApplication app = WApplication.getInstance();
		app.triggerUpdate();
	}

	private void setUploadedFile(UploadedFile file) {
		if (this.currentFileIdx_ >= this.uploads_.size()) {
			return;
		}
		WFileDropWidget.File f = this.uploads_.get(this.currentFileIdx_);
		this.currentFileIdx_++;
		f.setUploadedFile(file);
		if (this.currentFileIdx_ == this.uploads_.size()) {
			WApplication.getInstance().enableUpdates(false);
		}
	}

	private boolean incomingIdCheck(int id) {
		if (this.currentFileIdx_ >= this.uploads_.size()) {
			return false;
		}
		if (this.uploads_.get(this.currentFileIdx_).getUploadId() == id) {
			return true;
		} else {
			return false;
		}
	}

	private WFileDropWidget.WFileDropUploadResource resource_;
	private int currentFileIdx_;
	private String hoverStyleClass_;
	private boolean acceptDrops_;
	private String acceptAttributes_;
	private boolean dropIndicationEnabled_;
	private boolean globalDropEnabled_;
	private JSignal1<String> dropSignal_;
	private JSignal1<Integer> requestSend_;
	private JSignal1<Long> fileTooLarge_;
	private JSignal1<Integer> uploadFinished_;
	private JSignal doneSending_;
	private Signal1<List<WFileDropWidget.File>> dropEvent_;
	private Signal1<WFileDropWidget.File> uploadStart_;
	private Signal1<WFileDropWidget.File> uploaded_;
	private Signal2<WFileDropWidget.File, Long> tooLarge_;
	private Signal1<WFileDropWidget.File> uploadFailed_;
	private List<WFileDropWidget.File> uploads_;
	private static final int BIT_HOVERSTYLE_CHANGED = 0;
	private static final int BIT_ACCEPTDROPS_CHANGED = 1;
	private static final int BIT_FILTERS_CHANGED = 2;
	private static final int BIT_DRAGOPTIONS_CHANGED = 3;
	private BitSet updateFlags_;

	static WJavaScriptPreamble wtjs1() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptConstructor,
				"WFileDropWidget",
				"function(l,b,r){jQuery.data(b,\"lobj\",this);var c=this,p=\"Wt-dropzone-hover\",e=[],q=false,j=true,n=false,o=false,k=0,i=document.createElement(\"input\");i.type=\"file\";i.setAttribute(\"multiple\",\"multiple\");$(i).hide();b.appendChild(i);var h=document.createElement(\"div\");$(h).addClass(\"Wt-dropcover\");document.body.appendChild(h);this.eventContainsFile=function(a){var d=a.dataTransfer.types!=null&&a.dataTransfer.types.length>0&&a.dataTransfer.types[0]== \"Files\";return a.dataTransfer.items!=null&&a.dataTransfer.items.length>0&&a.dataTransfer.items[0].kind==\"file\"||d};this.validFileCheck=function(a,d,g){var f=new FileReader;f.onload=function(){d(true,g)};f.onerror=function(){d(false,g)};f.readAsText(a.slice(0,32))};b.setAcceptDrops=function(a){j=a};b.setDropIndication=function(a){n=a};b.setDropForward=function(a){o=a};b.ondragenter=function(a){if(j){if(c.eventContainsFile(a)){k===0&&c.setPageHoverStyle();k=2;c.setWidgetHoverStyle(true)}a.stopPropagation()}}; b.ondragleave=function(a){var d=a.clientX;a=a.clientY;var g=document.elementFromPoint(d,a);if(d===0&&a===0)g=null;if(g===h){c.setWidgetHoverStyle(false);k=1}else c.resetDragDrop()};b.ondragover=function(a){a.preventDefault()};bodyDragEnter=function(){if((n||o)&&$(b).is(\":visible\")){k=1;c.setPageHoverStyle()}};document.body.addEventListener(\"dragenter\",bodyDragEnter);h.ondragover=function(a){a.preventDefault();a.stopPropagation()};h.ondragleave=function(){!j||k!=1||c.resetDragDrop()};h.ondrop=function(a){a.preventDefault(); o?b.ondrop(a):c.resetDragDrop()};b.ondrop=function(a){a.preventDefault();if(j){c.resetDragDrop();window.FormData===undefined||a.dataTransfer.files==null||a.dataTransfer.files.length==0||c.addFiles(a.dataTransfer.files)}};this.addFiles=function(a){for(var d=[],g=0;g<a.length;g++){var f=new XMLHttpRequest;f.id=Math.floor(Math.random()*Math.pow(2,31));f.file=a[g];e.push(f);var m={};m.id=f.id;m.filename=f.file.name;m.type=f.file.type;m.size=f.file.size;d.push(m)}l.emit(b,\"dropsignal\",JSON.stringify(d))}; b.addEventListener(\"click\",function(){if(j){$(i).val(\"\");i.click()}});b.markForSending=function(a){for(var d=0;d<a.length;d++)for(var g=a[d].id,f=0;f<e.length;f++)if(e[f].id==g){e[f].ready=true;break}q||e[0].ready&&c.requestSend()};this.requestSend=function(){if(e[0].skip)c.uploadFinished(null);else{q=true;l.emit(b,\"requestsend\",e[0].id)}};b.send=function(a){xhr=e[0];if(xhr.file.size>r){l.emit(b,\"filetoolarge\",xhr.file.size);c.uploadFinished(null)}else c.validFileCheck(xhr.file,c.actualSend,a)};this.actualSend= function(a,d){if(a){xhr=e[0];xhr.addEventListener(\"load\",c.uploadFinished);xhr.addEventListener(\"error\",c.uploadFinished);xhr.addEventListener(\"abort\",c.uploadFinished);xhr.addEventListener(\"timeout\",c.uploadFinished);xhr.open(\"POST\",d);a=new FormData;a.append(\"file-id\",xhr.id);a.append(\"data\",xhr.file);xhr.send(a)}else c.uploadFinished(null)};this.uploadFinished=function(a){a!=null&&a.type==\"load\"&&a.currentTarget.status==200&&l.emit(b,\"uploadfinished\",e[0].id);e.splice(0,1);if(e[0]&&e[0].ready)c.requestSend(); else{q=false;l.emit(b,\"donesending\")}};b.cancelUpload=function(a){if(e[0]&&e[0].id==a)e[0].abort();else for(var d=1;d<e.length;d++)if(e[d].id==a)e[d].skip=true};i.onchange=function(){if(j)window.FormData===undefined||this.files==null||this.files.length==0||c.addFiles(this.files)};this.setPageHoverStyle=function(){if(n||o){$(h).addClass(\"Wt-dropzone-dragstyle\");$(b).addClass(\"Wt-dropzone-dragstyle\");n&&$(b).addClass(\"Wt-dropzone-indication\")}};this.setWidgetHoverStyle=function(a){a?$(b).addClass(p): $(b).removeClass(p)};this.resetDragDrop=function(){$(b).removeClass(\"Wt-dropzone-indication\");$(b).removeClass(\"Wt-dropzone-dragstyle\");$(h).removeClass(\"Wt-dropzone-dragstyle\");c.setWidgetHoverStyle(false);k=0};b.configureHoverClass=function(a){p=a};b.setFilters=function(a){i.setAttribute(\"accept\",a)};b.destructor=function(){document.body.removeEventListener(\"dragenter\",bodyDragEnter);document.body.removeChild(h)}}");
	}
}
