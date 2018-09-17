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
		 * <p>
		 * The first argument is the number of bytes received so far, and the
		 * second argument is the total number of bytes.
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

		public void setFilterEnabled(boolean enabled) {
			this.filterEnabled_ = enabled;
		}

		public boolean isFilterEnabled() {
			return this.filterEnabled_;
		}

		public boolean isFiltered() {
			return this.isFiltered_;
		}

		public File(int id, final String fileName, final String type,
				long size, long chunkSize, WObject parent) {
			super(parent);
			this.id_ = id;
			this.clientFileName_ = fileName;
			this.type_ = type;
			this.size_ = size;
			this.uploadedFile_ = new UploadedFile();
			this.dataReceived_ = new Signal2<Long, Long>();
			this.uploaded_ = new Signal();
			this.uploadStarted_ = false;
			this.uploadFinished_ = false;
			this.cancelled_ = false;
			this.filterEnabled_ = true;
			this.isFiltered_ = false;
			this.nbReceivedChunks_ = 0;
			this.chunkSize_ = chunkSize;
		}

		int getUploadId() {
			return this.id_;
		}

		public void handleIncomingData(final UploadedFile file, boolean last) {
			if (!this.uploadStarted_) {
				this.uploadedFile_ = file;
				this.uploadStarted_ = true;
			} else {
				FileUtils.appendFile(file.getSpoolFileName(),
						this.uploadedFile_.getSpoolFileName());
			}
			this.nbReceivedChunks_++;
			if (last) {
				this.uploadFinished_ = true;
			}
		}

		void cancel() {
			this.cancelled_ = true;
		}

		public boolean isCancelled() {
			return this.cancelled_;
		}

		public void emitDataReceived(long current, long total,
				boolean filterSupported) {
			if (!this.filterEnabled_ || !filterSupported
					|| this.chunkSize_ == 0) {
				this.dataReceived_.trigger(current, total);
			} else {
				long currentChunkSize = this.chunkSize_;
				int nbChunks = (int) (this.size_ / this.chunkSize_);
				if (this.nbReceivedChunks_ == nbChunks) {
					currentChunkSize = this.size_ - this.nbReceivedChunks_
							* this.chunkSize_;
				}
				long progress = this.nbReceivedChunks_
						* this.chunkSize_
						+ (long) ((double) current / (double) total * currentChunkSize);
				this.dataReceived_.trigger(progress, this.size_);
			}
		}

		public void setIsFiltered(boolean filtered) {
			this.isFiltered_ = filtered;
		}

		private int id_;
		private String clientFileName_;
		private String type_;
		private long size_;
		private UploadedFile uploadedFile_;
		private Signal2<Long, Long> dataReceived_;
		private Signal uploaded_;
		private boolean uploadStarted_;
		private boolean uploadFinished_;
		private boolean cancelled_;
		private boolean filterEnabled_;
		private boolean isFiltered_;
		private int nbReceivedChunks_;
		private long chunkSize_;
	}

	/**
	 * Constructor.
	 */
	public WFileDropWidget(WContainerWidget parent) {
		super(parent);
		this.uploadWorkerResource_ = null;
		this.resource_ = null;
		this.currentFileIdx_ = 0;
		this.jsFilterFn_ = "";
		this.jsFilterImports_ = new ArrayList<String>();
		this.chunkSize_ = 0;
		this.filterSupported_ = true;
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
		this.jsFilterNotSupported_ = new JSignal(this, "filternotsupported");
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
	 * Supply a function to process file data before it is uploaded to the
	 * server.
	 */
	public void setJavaScriptFilter(final String filterFn, long chunksize,
			final List<String> imports) {
		if (this.jsFilterFn_.equals(filterFn) && chunksize == this.chunkSize_) {
			return;
		}
		this.jsFilterFn_ = filterFn;
		Utils.copyList(imports, this.jsFilterImports_);
		this.chunkSize_ = chunksize;
		this.updateFlags_.set(BIT_JSFILTER_CHANGED);
		this.repaint();
	}

	/**
	 * Supply a function to process file data before it is uploaded to the
	 * server.
	 * <p>
	 * Calls
	 * {@link #setJavaScriptFilter(String filterFn, long chunksize, List imports)
	 * setJavaScriptFilter(filterFn, 0, new ArrayList<String>())}
	 */
	public final void setJavaScriptFilter(final String filterFn) {
		setJavaScriptFilter(filterFn, 0, new ArrayList<String>());
	}

	/**
	 * Supply a function to process file data before it is uploaded to the
	 * server.
	 * <p>
	 * Calls
	 * {@link #setJavaScriptFilter(String filterFn, long chunksize, List imports)
	 * setJavaScriptFilter(filterFn, chunksize, new ArrayList<String>())}
	 */
	public final void setJavaScriptFilter(final String filterFn, long chunksize) {
		setJavaScriptFilter(filterFn, chunksize, new ArrayList<String>());
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
	 * <p>
	 * The second argument is the size of the file in bytes.
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
				result += "Wt3_3_11.remove('" + this.getId() + "');";
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
			if (this.updateFlags_.get(BIT_JSFILTER_CHANGED) || all) {
				this.createWorkerResource();
				this.doJavaScript(this.getJsRef()
						+ ".setUploadWorker(\""
						+ (this.uploadWorkerResource_ != null ? this.uploadWorkerResource_
								.getUrl() : "") + "\");");
				this.doJavaScript(this.getJsRef() + ".setChunkSize("
						+ String.valueOf(this.chunkSize_) + ");");
			}
			this.updateFlags_.clear();
		}
		super.updateDom(element, all);
	}

	static class WFileDropUploadResource extends WResource {
		private static Logger logger = LoggerFactory
				.getLogger(WFileDropUploadResource.class);

		public WFileDropUploadResource(WFileDropWidget fileDropWidget,
				WFileDropWidget.File file) {
			super(fileDropWidget);
			this.parent_ = fileDropWidget;
			this.app_ = WApplication.getInstance();
			this.currentFile_ = file;
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
			String filtFlag = request.getParameter("filtered");
			this.currentFile_.setIsFiltered(filtFlag != null
					&& filtFlag.equals("true"));
			String lastFlag = request.getParameter("last");
			boolean isLast = lastFlag == null || lastFlag != null
					&& lastFlag.equals("true");
			this.currentFile_.handleIncomingData(files.get(0), isLast);
			if (isLast) {
				this.parent_.proceedToNextFile();
			}
			response.setContentType("text/plain");
			lock.release();
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
				"new Wt3_3_11.WFileDropWidget(" + app.getJavaScriptClass()
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
		this.jsFilterNotSupported_.addListener(this, new Signal.Listener() {
			public void trigger() {
				WFileDropWidget.this.disableJavaScriptFilter();
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
					type, size, this.chunkSize_, this);
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
						this, this.uploads_.get(this.currentFileIdx_));
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
				this.doJavaScript(this.getJsRef()
						+ ".send('"
						+ this.resource_.getUrl()
						+ "', "
						+ (this.uploads_.get(i).isFilterEnabled() ? "true"
								: "false") + ");");
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
		file.emitDataReceived(current, total, this.filterSupported_);
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

	private void createWorkerResource() {
		if (this.uploadWorkerResource_ != null) {
			;
			this.uploadWorkerResource_ = null;
		}
		if (this.jsFilterFn_.length() == 0) {
			return;
		}
		this.uploadWorkerResource_ = new WMemoryResource("text/javascript",
				this);
		StringWriter ss = new StringWriter();
		ss.append("importScripts(");
		for (int i = 0; i < this.jsFilterImports_.size(); i++) {
			ss.append("\"").append(this.jsFilterImports_.get(i)).append("\"");
			if (i < this.jsFilterImports_.size() - 1) {
				ss.append(", ");
			}
		}
		ss.append(");").append('\n');
		ss.append(this.jsFilterFn_).append('\n');
		ss.append(WORKER_JS);
		try {
			this.uploadWorkerResource_.setData(ss.toString().getBytes("utf8"));
		} catch (UnsupportedEncodingException e) {
		}
	}

	private void disableJavaScriptFilter() {
		this.filterSupported_ = false;
	}

	private void proceedToNextFile() {
		if (this.currentFileIdx_ >= this.uploads_.size()) {
			return;
		}
		this.currentFileIdx_++;
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

	private WMemoryResource uploadWorkerResource_;
	private WFileDropWidget.WFileDropUploadResource resource_;
	private int currentFileIdx_;
	private static final String WORKER_JS = "var featureCheck = false; var currentUpload = null; onmessage = function(msg) { if (msg.data[\"cmd\"] === \"send\") { if (featureCheck === false) { uploadFinished(null); return; } if (currentUpload) cancelCurrentUpload(); send(msg.data[\"upload\"], msg.data[\"url\"], msg.data[\"chunksize\"]); } else if (msg.data[\"cmd\"] === \"cancel\") { if (currentUpload && (msg.data[\"upload\"].id === currentUpload.id) ) { cancelCurrentUpload(); } } else if (msg.data[\"cmd\"] === \"check\") { featureCheck = runFeatureCheck(); postMessage({\"workerfeatures\" : (featureCheck ? \"valid\" : \"invalid\")}); } else { console.log(\"WFileDropWidget_worker received cmd: \" + msg.data[\"cmd\"]); } }; var cancelCurrentUpload = function() { currentUpload.skip = true; if (currentUpload.request) currentUpload.request.abort(); currentUpload = null; }; var send = function(upload, url, chunksize) { currentUpload = upload; currentUpload.filtered = true; var filter = createFilter(upload, chunksize); var sendChunk = function(chunk) { if (chunk === null || chunk.upload === null || chunk.upload.id === null || chunk.data === null || chunk.last === null) { console.error(\"File upload: chunk is missing properties, check the documentation for the correct format.\"); uploadFinished(null); return; } if (chunk.upload.skip) { uploadFinished(null); return; } var xhr = new XMLHttpRequest(); chunk.upload.request = xhr; callback = chunk.last ? uploadFinished : chunkFinished; xhr.addEventListener(\"load\", callback); xhr.addEventListener(\"error\", callback); xhr.addEventListener(\"abort\", callback); xhr.addEventListener(\"timeout\", callback); xhr.open(\"POST\", url); var fd = new FormData(); fd.append(\"file-id\", chunk.upload.id); fd.append(\"data\", chunk.data); fd.append(\"last\", chunk.last); fd.append(\"filtered\", chunk.upload.filtered); xhr.send(fd); }; var chunkFinished = function(e) { if (e != null && e.type === 'load' && e.currentTarget.status === 200) { filter(sendChunk); } else { uploadFinished(null); } }; filter(sendChunk); }; var uploadFinished = function(e) { var success = (e != null && e.type === 'load' && e.currentTarget.status === 200); postMessage(success); currentUpload = null; }; var runFeatureCheck = function(e) { var reader = ( (!!self.FileReader) && (!!self.FileReaderSync) ); var formData = !!self.FormData; return reader && formData; };";
	private String jsFilterFn_;
	private List<String> jsFilterImports_;
	private long chunkSize_;
	private boolean filterSupported_;
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
	private JSignal jsFilterNotSupported_;
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
	private static final int BIT_JSFILTER_CHANGED = 4;
	private BitSet updateFlags_;

	static WJavaScriptPreamble wtjs1() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptConstructor,
				"WFileDropWidget",
				"function(k,b,t){jQuery.data(b,\"lobj\",this);var e=this,q=\"Wt-dropzone-hover\",d=[],r=false,l=true,o=false,p=false,h=undefined,s=0,m=0,j=document.createElement(\"input\");j.type=\"file\";j.setAttribute(\"multiple\",\"multiple\");$(j).hide();b.appendChild(j);var i=document.createElement(\"div\");$(i).addClass(\"Wt-dropcover\");document.body.appendChild(i);this.eventContainsFile=function(a){var c=a.dataTransfer.types!=null&&a.dataTransfer.types.length> 0&&a.dataTransfer.types[0]===\"Files\";return a.dataTransfer.items!=null&&a.dataTransfer.items.length>0&&a.dataTransfer.items[0].kind===\"file\"||c};this.validFileCheck=function(a,c,g){var f=new FileReader;f.onload=function(){c(true,g)};f.onerror=function(){c(false,g)};f.readAsText(a.slice(0,32))};b.setAcceptDrops=function(a){l=a};b.setDropIndication=function(a){o=a};b.setDropForward=function(a){p=a};b.ondragenter=function(a){if(l){if(e.eventContainsFile(a)){m===0&&e.setPageHoverStyle();m=2;e.setWidgetHoverStyle(true)}a.stopPropagation()}}; b.ondragleave=function(a){var c=a.clientX;a=a.clientY;var g=document.elementFromPoint(c,a);if(c===0&&a===0)g=null;if(g===i){e.setWidgetHoverStyle(false);m=1}else e.resetDragDrop()};b.ondragover=function(a){a.preventDefault()};bodyDragEnter=function(){if((o||p)&&$(b).is(\":visible\")){m=1;e.setPageHoverStyle()}};document.body.addEventListener(\"dragenter\",bodyDragEnter);i.ondragover=function(a){a.preventDefault();a.stopPropagation()};i.ondragleave=function(){!l||m!=1||e.resetDragDrop()};i.ondrop=function(a){a.preventDefault(); p?b.ondrop(a):e.resetDragDrop()};b.ondrop=function(a){a.preventDefault();if(l){e.resetDragDrop();window.FormData===undefined||a.dataTransfer.files===null||a.dataTransfer.files.length===0||e.addFiles(a.dataTransfer.files)}};this.addFiles=function(a){for(var c=[],g=0;g<a.length;g++){var f={};f.id=Math.floor(Math.random()*Math.pow(2,31));f.file=a[g];d.push(f);var n={};n.id=f.id;n.filename=f.file.name;n.type=f.file.type;n.size=f.file.size;c.push(n)}k.emit(b,\"dropsignal\",JSON.stringify(c))};b.addEventListener(\"click\", function(){if(l){$(j).val(\"\");j.click()}});b.markForSending=function(a){for(var c=0;c<a.length;c++)for(var g=a[c].id,f=0;f<d.length;f++)if(d[f].id===g){d[f].ready=true;break}r||d[0].ready&&e.requestSend()};this.requestSend=function(){if(d[0].skip)e.uploadFinished(null);else{r=true;k.emit(b,\"requestsend\",d[0].id)}};b.send=function(a,c){upload=d[0];if(upload.file.size>t){k.emit(b,\"filetoolarge\",upload.file.size);e.uploadFinished(null)}else e.validFileCheck(upload.file,h!=undefined&&c?e.workerSend:e.actualSend, a)};this.actualSend=function(a,c){if(a){a=new XMLHttpRequest;a.addEventListener(\"load\",e.uploadFinished);a.addEventListener(\"error\",e.uploadFinished);a.addEventListener(\"abort\",e.uploadFinished);a.addEventListener(\"timeout\",e.uploadFinished);a.open(\"POST\",c);d[0].request=a;c=new FormData;c.append(\"file-id\",d[0].id);c.append(\"data\",d[0].file);a.send(c)}else e.uploadFinished(null)};this.workerSend=function(a,c){if(a){h.upload=d[0];h.postMessage({cmd:\"send\",url:c,upload:d[0],chunksize:s})}else e.uploadFinished(null)}; this.uploadFinished=function(a){if(a!=null&&a.type===\"load\"&&a.currentTarget.status===200||a===true)k.emit(b,\"uploadfinished\",d[0].id);d.splice(0,1);if(d[0]&&d[0].ready)e.requestSend();else{r=false;k.emit(b,\"donesending\")}};b.cancelUpload=function(a){if(d[0]&&d[0].id===a){d[0].skip=true;if(d[0].request)d[0].request.abort();else h&&h.upload===d[0]&&h.postMessage({cmd:\"cancel\",upload:d[0]})}else for(var c=1;c<d.length;c++)if(d[c].id===a)d[c].skip=true};j.onchange=function(){if(l)window.FormData===undefined|| this.files===null||this.files.length===0||e.addFiles(this.files)};this.setPageHoverStyle=function(){if(o||p){$(i).addClass(\"Wt-dropzone-dragstyle\");$(b).addClass(\"Wt-dropzone-dragstyle\");o&&$(b).addClass(\"Wt-dropzone-indication\")}};this.setWidgetHoverStyle=function(a){a?$(b).addClass(q):$(b).removeClass(q)};this.resetDragDrop=function(){$(b).removeClass(\"Wt-dropzone-indication\");$(b).removeClass(\"Wt-dropzone-dragstyle\");$(i).removeClass(\"Wt-dropzone-dragstyle\");e.setWidgetHoverStyle(false);m=0};b.configureHoverClass= function(a){q=a};b.setFilters=function(a){j.setAttribute(\"accept\",a)};b.setUploadWorker=function(a){if(a&&window.Worker){h=new Worker(a);h.onmessage=function(c){if(c.data.workerfeatures){if(c.data.workerfeatures!==\"valid\"){b.setUploadWorker(null);k.emit(b,\"filternotsupported\")}}else e.uploadFinished(c.data)};h.postMessage({cmd:\"check\"})}else h=undefined};b.setChunkSize=function(a){s=a};b.destructor=function(){document.body.removeEventListener(\"dragenter\",bodyDragEnter);document.body.removeChild(i)}}");
	}
}
