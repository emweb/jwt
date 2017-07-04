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
 * The widget has the default style-class &apos;Wt-filedropzone&apos;. An
 * additional style class is applied when files are hovered over the widget.
 * This can be configured using the method
 * {@link WFileDropWidget#setHoverStyleClass(String className)
 * setHoverStyleClass()}.
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

		public File(int id, final String fileName, final String type,
				long size, WObject parent) {
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

		public int getUploadId() {
			return this.id_;
		}

		public void setUploadedFile(final UploadedFile file) {
			this.uploadFinished_ = true;
			this.uploadedFile_ = file;
		}

		public void cancel() {
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
		this.doJavaScript(this.getJsRef() + ".setAcceptDrops("
				+ (enable ? "true" : "false") + ");");
	}

	/**
	 * Set the style class that is applied when a file is hovered over the
	 * widget.
	 */
	public void setHoverStyleClass(final String className) {
		this.doJavaScript(this.getJsRef() + ".configureHoverClass('"
				+ className + "');");
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

	protected void enableAjax() {
		this.setup();
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
				"new Wt3_3_8.WFileDropWidget(" + app.getJavaScriptClass() + ","
						+ this.getJsRef() + "," + maxFileSize + ");");
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

	static WJavaScriptPreamble wtjs1() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptConstructor,
				"WFileDropWidget",
				"function(h,d,m){jQuery.data(d,\"lobj\",this);var f=this,k=\"Wt-filedropzone-hover\",c=[],l=false,j=true;this.eventContainsFile=function(a){var b=a.dataTransfer.types!=null&&a.dataTransfer.types.length>0&&a.dataTransfer.types[0]==\"Files\";return a.dataTransfer.items!=null&&a.dataTransfer.items.length>0&&a.dataTransfer.items[0].kind==\"file\"||b};this.validFileCheck=function(a,b,g){var e=new FileReader;e.onload=function(){b(true,g)};e.onerror= function(){b(false,g)};e.readAsText(a)};d.setAcceptDrops=function(a){j=a};d.ondragenter=function(a){j&&f.eventContainsFile(a)&&f.setHoverStyle(true)};d.ondragleave=function(){j&&f.setHoverStyle(false)};d.ondragover=function(a){a.preventDefault()};d.ondrop=function(a){a.preventDefault();if(j){f.setHoverStyle(false);if(!(window.FormData===undefined||a.dataTransfer.files==null||a.dataTransfer.files.length==0)){Math.floor(Math.random()*32768);for(var b=[],g=0;g<a.dataTransfer.files.length;g++){var e= new XMLHttpRequest;e.id=Math.floor(Math.random()*Math.pow(2,31));e.file=a.dataTransfer.files[g];c.push(e);var i={};i.id=e.id;i.filename=e.file.name;i.type=e.file.type;i.size=e.file.size;b.push(i)}console.log(b);h.emit(d,\"dropsignal\",JSON.stringify(b))}}};d.markForSending=function(a){for(var b=0;b<a.length;b++)for(var g=a[b].id,e=0;e<c.length;e++)if(c[e].id==g){c[e].ready=true;break}l||c[0].ready&&f.requestSend()};this.requestSend=function(){if(c[0].skip)f.uploadFinished(null);else{l=true;h.emit(d, \"requestsend\",c[0].id)}};d.send=function(a){console.log(\"sending file\");xhr=c[0];if(xhr.file.size>m){h.emit(d,\"filetoolarge\",xhr.file.size);f.uploadFinished(null)}else f.validFileCheck(xhr.file,f.actualSend,a)};this.actualSend=function(a,b){if(a){xhr=c[0];xhr.addEventListener(\"load\",f.uploadFinished);xhr.addEventListener(\"error\",f.uploadFinished);xhr.addEventListener(\"abort\",f.uploadFinished);xhr.addEventListener(\"timeout\",f.uploadFinished);xhr.open(\"POST\",b);a=new FormData;a.append(\"file-id\",xhr.id); a.append(\"data\",xhr.file);xhr.send(a)}else f.uploadFinished(null)};this.uploadFinished=function(a){console.log(\"finished sending (type = \"+a+\")\");a!=null&&a.type==\"load\"&&a.currentTarget.status==200&&h.emit(d,\"uploadfinished\",c[0].id);c.splice(0,1);if(c[0]&&c[0].ready)f.requestSend();else{l=false;h.emit(d,\"donesending\")}};d.cancelUpload=function(a){if(c[0].id==a)c[0].abort();else for(var b=1;b<c.length;b++)if(c[b].id==a)c[b].skip=true};this.setHoverStyle=function(a){a?$(d).addClass(k):$(d).removeClass(k)}; d.configureHoverClass=function(a){k=a}}");
	}
}
