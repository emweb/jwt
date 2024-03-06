/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.servlet.*;
import eu.webtoolkit.jwt.utils.*;
import java.io.*;
import java.lang.ref.*;
import java.time.*;
import java.util.*;
import java.util.regex.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A widget that allows dropping files for upload.
 *
 * <p>This widget accepts files that are dropped into it. A signal is triggered whenever one or more
 * files are dropped. The filename, type and size of these files is immediately available through
 * the {@link File} interface.
 *
 * <p>The file upload is done sequentially. All files before the {@link
 * WFileDropWidget#getCurrentIndex() getCurrentIndex()} have either finished, failed or have been
 * cancelled.
 *
 * <p>The widget has the default style-class &apos;Wt-filedropzone&apos;. The style-class
 * &apos;Wt-dropzone-hover&apos; is added when files are hovered over the widget.
 */
public class WFileDropWidget extends WContainerWidget {
  private static Logger logger = LoggerFactory.getLogger(WFileDropWidget.class);

  /**
   * A nested class of {@link WFileDropWidget} representing a file.
   *
   * <p>The methods returning the filename, mime-type and size return valid values if the upload of
   * this file is not yet finished. The method {@link WFileDropWidget.File#getUploadedFile()
   * getUploadedFile()} is only available after the upload is finished.
   */
  public static class File extends WObject {
    private static Logger logger = LoggerFactory.getLogger(File.class);

    /** Returns the client filename. */
    public String getClientFileName() {
      return this.clientFileName_;
    }
    /** Returns the mime-type of the file. */
    public String getMimeType() {
      return this.type_;
    }
    /** Returns the size of the file. */
    public long getSize() {
      return this.size_;
    }
    /**
     * Returns the uploaded file as a {@link UploadedFile}.
     *
     * <p>This method will throw an expection if the upload is not yet finished.
     *
     * <p>
     *
     * @see WFileDropWidget.File#isUploadFinished()
     */
    public UploadedFile getUploadedFile() {
      if (!this.uploadFinished_) {
        throw new WException("Can not access uploaded files before upload is done.");
      } else {
        return this.uploadedFile_;
      }
    }
    /**
     * Returns true if the upload is finished.
     *
     * <p>When this method returns true, the uploaded file is available on the server.
     *
     * <p>
     *
     * @see WFileDropWidget.File#getUploadedFile()
     */
    public boolean isUploadFinished() {
      return this.uploadFinished_;
    }
    /**
     * This signal allows you to track the upload progress of the file.
     *
     * <p>The first argument is the number of bytes received so far, and the second argument is the
     * total number of bytes.
     */
    public Signal2<Long, Long> dataReceived() {
      return this.dataReceived_;
    }
    /**
     * This signal is triggered when the upload is finished.
     *
     * <p>This is also signalled using the {@link WFileDropWidget} {@link
     * WFileDropWidget#uploaded()} signal.
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

    public File(int id, final String fileName, final String type, long size, long chunkSize) {
      super();
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
        FileUtils.appendFile(file.getSpoolFileName(), this.uploadedFile_.getSpoolFileName());
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

    public void emitDataReceived(long current, long total, boolean filterSupported) {
      if (!this.filterEnabled_ || !filterSupported || this.chunkSize_ == 0) {
        this.dataReceived_.trigger(current, total);
      } else {
        long currentChunkSize = this.chunkSize_;
        int nbChunks = (int) (this.size_ / this.chunkSize_);
        if (this.nbReceivedChunks_ == nbChunks) {
          currentChunkSize = this.size_ - this.nbReceivedChunks_ * this.chunkSize_;
        }
        long progress =
            this.nbReceivedChunks_ * this.chunkSize_
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
  /** Constructor. */
  public WFileDropWidget(WContainerWidget parentContainer) {
    super();
    this.uploadWorkerResource_ = null;
    this.resource_ = (WFileDropWidget.WFileDropUploadResource) null;
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
    this.dropSignal_ = new JSignal1<String>(this, "dropsignal") {};
    this.requestSend_ = new JSignal1<Integer>(this, "requestsend") {};
    this.fileTooLarge_ = new JSignal1<Long>(this, "filetoolarge") {};
    this.uploadFinished_ = new JSignal1<Integer>(this, "uploadfinished") {};
    this.doneSending_ = new JSignal(this, "donesending");
    this.jsFilterNotSupported_ = new JSignal(this, "filternotsupported");
    this.dropEvent_ = new Signal1<List<WFileDropWidget.File>>();
    this.uploadStart_ = new Signal1<WFileDropWidget.File>();
    this.uploaded_ = new Signal1<WFileDropWidget.File>();
    this.tooLarge_ = new Signal2<WFileDropWidget.File, Long>();
    this.uploadFailed_ = new Signal1<WFileDropWidget.File>();
    this.uploads_ = new ArrayList<WFileDropWidget.File>();
    this.updateFlags_ = new BitSet();
    this.updatesEnabled_ = false;
    WApplication app = WApplication.getInstance();
    if (!app.getEnvironment().hasAjax()) {
      return;
    }
    this.setup();
    if (parentContainer != null) parentContainer.addWidget(this);
  }
  /**
   * Constructor.
   *
   * <p>Calls {@link #WFileDropWidget(WContainerWidget parentContainer)
   * this((WContainerWidget)null)}
   */
  public WFileDropWidget() {
    this((WContainerWidget) null);
  }
  /**
   * Returns the vector of uploads managed by this widget.
   *
   * <p>The files in this vector are handled sequentially by the widget. All {@link File} objects in
   * this vector have either finished or failed if they are before the {@link
   * WFileDropWidget#getCurrentIndex() getCurrentIndex()}, depending on the return value of {@link
   * WFileDropWidget.File#isUploadFinished() isUploadFinished()}. The other files are still being
   * handled.
   *
   * <p>
   *
   * <p><i><b>Remark: </b>Since version 4.7.0, this method returns a copy of the vector because we
   * changed the internal vector to hold values of type std::unique_ptr. </i>
   *
   * @see WFileDropWidget#getCurrentIndex()
   */
  public List<WFileDropWidget.File> getUploads() {
    List<WFileDropWidget.File> copy = new ArrayList<WFileDropWidget.File>();
    for (WFileDropWidget.File upload : this.uploads_) {
      copy.add(upload);
    }
    return copy;
  }
  /**
   * Return the index of the file that is currently being handled.
   *
   * <p>If nothing is to be done, this will return the size of the vector returned by {@link
   * WFileDropWidget#getUploads() getUploads()}.
   */
  public int getCurrentIndex() {
    return this.currentFileIdx_;
  }
  /**
   * Cancels the upload of a file.
   *
   * <p>If you cancel a file that is still waiting to be uploaded, it will stay in the {@link
   * WFileDropWidget#getUploads() getUploads()} vector, but it will be skipped.
   */
  public void cancelUpload(WFileDropWidget.File file) {
    file.cancel();
    int i = file.getUploadId();
    this.doJavaScript(this.getJsRef() + ".cancelUpload(" + String.valueOf(i) + ");");
  }
  /**
   * Removes the file.
   *
   * <p>This can be used to free resources of files that were already uploaded. A file can only be
   * removed if its index in {@link WFileDropWidget#getUploads() getUploads()} is before the current
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
  /** When set to false, the widget no longer accepts any files. */
  public void setAcceptDrops(boolean enable) {
    if (enable == this.acceptDrops_) {
      return;
    }
    this.acceptDrops_ = enable;
    this.updateFlags_.set(BIT_ACCEPTDROPS_CHANGED);
    this.repaint();
  }
  /**
   * Set the style class that is applied when a file is hovered over the widget.
   *
   * <p>
   *
   * @deprecated Override the css rule '.Wt-filedropzone.Wt-dropzone-hover' instead.
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
   *
   * <p>The accept attribute may be specified to provide user agents with a hint of what file types
   * will be accepted. Use html input accept attributes as input. This only affects the popup that
   * is shown when users click on the widget. A user can still drop any file type.
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
   *
   * <p>As soon as a drag enters anywhere on the page the styleclass
   * &apos;Wt-dropzone-indication&apos; is added to this widget. This can be useful to point the
   * user to the correct place to drop the file. Once the user drags a file over the widget itself,
   * the styleclass &apos;hover-style&apos; is also added. This can be enabled for multiple
   * dropwidgets if only one of them is visible at the same time.
   *
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
   *
   * <p>
   *
   * @see WFileDropWidget#setDropIndicationEnabled(boolean enable)
   */
  public boolean isDropIndicationEnabled() {
    return this.dropIndicationEnabled_;
  }
  /**
   * Allow dropping the files anywhere on the page.
   *
   * <p>If enabled, a drop anywhere on the page will be forwarded to this widget.
   *
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
   *
   * <p>
   *
   * @see WFileDropWidget#setGlobalDropEnabled(boolean enable)
   */
  public boolean isGlobalDropEnabled() {
    return this.globalDropEnabled_;
  }
  /** Supply a function to process file data before it is uploaded to the server. */
  public void setJavaScriptFilter(
      final String filterFn, long chunksize, final List<String> imports) {
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
   * Supply a function to process file data before it is uploaded to the server.
   *
   * <p>Calls {@link #setJavaScriptFilter(String filterFn, long chunksize, List imports)
   * setJavaScriptFilter(filterFn, 0, new ArrayList<String>())}
   */
  public final void setJavaScriptFilter(final String filterFn) {
    setJavaScriptFilter(filterFn, 0, new ArrayList<String>());
  }
  /**
   * Supply a function to process file data before it is uploaded to the server.
   *
   * <p>Calls {@link #setJavaScriptFilter(String filterFn, long chunksize, List imports)
   * setJavaScriptFilter(filterFn, chunksize, new ArrayList<String>())}
   */
  public final void setJavaScriptFilter(final String filterFn, long chunksize) {
    setJavaScriptFilter(filterFn, chunksize, new ArrayList<String>());
  }
  /** The signal triggers if one or more files are dropped. */
  public Signal1<List<WFileDropWidget.File>> drop() {
    return this.dropEvent_;
  }
  /**
   * The signal triggers when the upload of a file is about to begin.
   *
   * <p>After this signal is triggered, the upload automatically starts. The upload can still fail
   * if the file is too large or if there is a network error.
   */
  public Signal1<WFileDropWidget.File> newUpload() {
    return this.uploadStart_;
  }
  /** The signal is triggered if any file finished uploading. */
  public Signal1<WFileDropWidget.File> uploaded() {
    return this.uploaded_;
  }
  /**
   * The signal triggers when a file is too large for upload.
   *
   * <p>This signal is triggered when the widget attempts to upload the file.
   *
   * <p>The second argument is the size of the file in bytes.
   */
  public Signal2<WFileDropWidget.File, Long> tooLarge() {
    return this.tooLarge_;
  }
  /**
   * The signal triggers when an upload failed.
   *
   * <p>This signal will trigger when the widget skips over one of the files in the list for an
   * unknown reason (e.g. happens when you drop a folder).
   */
  public Signal1<WFileDropWidget.File> uploadFailed() {
    return this.uploadFailed_;
  }

  String renderRemoveJs(boolean recursive) {
    if (this.isRendered()) {
      String result = this.getJsRef() + ".destructor();";
      if (!recursive) {
        result += "Wt4_10_4.remove('" + this.getId() + "');";
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
        this.doJavaScript(
            this.getJsRef() + ".configureHoverClass('" + this.hoverStyleClass_ + "');");
      }
      if (this.updateFlags_.get(BIT_ACCEPTDROPS_CHANGED) || all) {
        this.doJavaScript(
            this.getJsRef() + ".setAcceptDrops(" + (this.acceptDrops_ ? "true" : "false") + ");");
      }
      if (this.updateFlags_.get(BIT_FILTERS_CHANGED) || all) {
        this.doJavaScript(
            this.getJsRef() + ".setFilters(" + jsStringLiteral(this.acceptAttributes_) + ");");
      }
      if (this.updateFlags_.get(BIT_DRAGOPTIONS_CHANGED) || all) {
        this.doJavaScript(
            this.getJsRef()
                + ".setDropIndication("
                + (this.dropIndicationEnabled_ ? "true" : "false")
                + ");");
        this.doJavaScript(
            this.getJsRef()
                + ".setDropForward("
                + (this.globalDropEnabled_ ? "true" : "false")
                + ");");
      }
      if (this.updateFlags_.get(BIT_JSFILTER_CHANGED) || all) {
        this.createWorkerResource();
        this.doJavaScript(
            this.getJsRef()
                + ".setUploadWorker(\""
                + (this.uploadWorkerResource_ != null ? this.uploadWorkerResource_.getUrl() : "")
                + "\");");
        this.doJavaScript(
            this.getJsRef() + ".setChunkSize(" + String.valueOf(this.chunkSize_) + ");");
      }
      this.updateFlags_.clear();
    }
    super.updateDom(element, all);
  }

  private void setup() {
    WApplication app = WApplication.getInstance();
    app.loadJavaScript("js/WFileDropWidget.js", wtjs1());
    String maxFileSize = String.valueOf(WApplication.getInstance().getMaximumRequestSize());
    this.setJavaScriptMember(
        " WFileDropWidget",
        "new Wt4_10_4.WFileDropWidget("
            + app.getJavaScriptClass()
            + ","
            + this.getJsRef()
            + ","
            + maxFileSize
            + ");");
    this.dropSignal_.addListener(
        this,
        (String e1) -> {
          WFileDropWidget.this.handleDrop(e1);
        });
    this.requestSend_.addListener(
        this,
        (Integer e1) -> {
          WFileDropWidget.this.handleSendRequest(e1);
        });
    this.fileTooLarge_.addListener(
        this,
        (Long e1) -> {
          WFileDropWidget.this.handleTooLarge(e1);
        });
    this.uploadFinished_.addListener(
        this,
        (Integer e1) -> {
          WFileDropWidget.this.emitUploaded(e1);
        });
    this.doneSending_.addListener(
        this,
        () -> {
          WFileDropWidget.this.stopReceiving();
        });
    this.jsFilterNotSupported_.addListener(
        this,
        () -> {
          WFileDropWidget.this.disableJavaScriptFilter();
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
      com.google.gson.JsonObject upload = (com.google.gson.JsonObject) dropped.get(i);
      int id = -1;
      long size = 0;
      String name = "";
      String type = "";
      for (Iterator<Map.Entry<String, com.google.gson.JsonElement>> it_it =
              upload.entrySet().iterator();
          it_it.hasNext(); ) {
        Map.Entry<String, com.google.gson.JsonElement> it = it_it.next();
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
      WFileDropWidget.File file = new File(id, name, type, size, this.chunkSize_);
      drops.add(file);
      this.uploads_.add(file);
    }
    this.dropEvent_.trigger(drops);
    this.doJavaScript(this.getJsRef() + ".markForSending(" + newDrops + ");");
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
        WFileDropWidget.File currentFile = this.uploads_.get(this.currentFileIdx_);
        this.resource_ = new WFileDropUploadResource(this, currentFile);
        this.resource_
            .dataReceived()
            .addListener(
                this,
                (Long e1, Long e2) -> {
                  WFileDropWidget.this.onData(e1, e2);
                });
        this.resource_
            .dataExceeded()
            .addListener(
                this,
                (Long e1) -> {
                  WFileDropWidget.this.onDataExceeded(e1);
                });
        this.doJavaScript(
            this.getJsRef()
                + ".send('"
                + this.resource_.getUrl()
                + "', "
                + (currentFile.isFilterEnabled() ? "true" : "false")
                + ");");
        this.uploadStart_.trigger(currentFile);
        break;
      } else {
        if (!this.uploads_.get(i).isCancelled()) {
          this.uploadFailed_.trigger(this.uploads_.get(i));
        }
      }
    }
    if (!fileFound) {
      this.doJavaScript(this.getJsRef() + ".cancelUpload(" + String.valueOf(id) + ");");
    } else {
      this.updatesEnabled_ = true;
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
      if (this.updatesEnabled_) {
        WApplication.getInstance().enableUpdates(false);
        this.updatesEnabled_ = false;
      }
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
    this.tooLarge_.trigger(this.uploads_.get(this.currentFileIdx_), dataExceeded);
    WApplication app = WApplication.getInstance();
    app.triggerUpdate();
  }

  private void createWorkerResource() {
    if (this.uploadWorkerResource_ != null) {

      this.uploadWorkerResource_ = null;
    }
    if (this.jsFilterFn_.length() == 0) {
      return;
    }
    this.uploadWorkerResource_ = new WMemoryResource("text/javascript");
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
      if (this.updatesEnabled_) {
        WApplication.getInstance().enableUpdates(false);
        this.updatesEnabled_ = false;
      }
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
  private static final String WORKER_JS =
      "let featureCheck = false; let currentUpload = null; onmessage = function(msg) { if (msg.data[\"cmd\"] === \"send\") { if (featureCheck === false) { uploadFinished(null); return; } if (currentUpload) cancelCurrentUpload(); send(msg.data[\"upload\"], msg.data[\"url\"], msg.data[\"chunksize\"]); } else if (msg.data[\"cmd\"] === \"cancel\") { if (currentUpload && (msg.data[\"upload\"].id === currentUpload.id) ) { cancelCurrentUpload(); } } else if (msg.data[\"cmd\"] === \"check\") { featureCheck = runFeatureCheck(); postMessage({\"workerfeatures\" : (featureCheck ? \"valid\" : \"invalid\")}); } else { console.log(\"WFileDropWidget_worker received cmd: \" + msg.data[\"cmd\"]); } }; var cancelCurrentUpload = function() { currentUpload.skip = true; if (currentUpload.request) currentUpload.request.abort(); currentUpload = null; }; var send = function(upload, url, chunksize) { currentUpload = upload; currentUpload.filtered = true; var filter = createFilter(upload, chunksize); var sendChunk = function(chunk) { if (chunk === null || chunk.upload === null || chunk.upload.id === null || chunk.data === null || chunk.last === null) { console.error(\"File upload: chunk is missing properties, check the documentation for the correct format.\"); uploadFinished(null); return; } if (chunk.upload.skip) { uploadFinished(null); return; } var xhr = new XMLHttpRequest(); chunk.upload.request = xhr; callback = chunk.last ? uploadFinished : chunkFinished; xhr.addEventListener(\"load\", callback); xhr.addEventListener(\"error\", callback); xhr.addEventListener(\"abort\", callback); xhr.addEventListener(\"timeout\", callback); xhr.open(\"POST\", url); var fd = new FormData(); fd.append(\"file-id\", chunk.upload.id); fd.append(\"data\", chunk.data); fd.append(\"last\", chunk.last); fd.append(\"filtered\", chunk.upload.filtered); xhr.send(fd); }; var chunkFinished = function(e) { if (e != null && e.type === 'load' && e.currentTarget.status === 200) { filter(sendChunk); } else { uploadFinished(null); } }; filter(sendChunk); }; var uploadFinished = function(e) { var success = (e != null && e.type === 'load' && e.currentTarget.status === 200); postMessage(success); currentUpload = null; }; var runFeatureCheck = function(e) { var reader = ( (!!self.FileReader) && (!!self.FileReaderSync) ); var formData = !!self.FormData; return reader && formData; };";
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
  private boolean updatesEnabled_;

  static WJavaScriptPreamble wtjs1() {
    return new WJavaScriptPreamble(
        JavaScriptScope.WtClassScope,
        JavaScriptObjectType.JavaScriptConstructor,
        "WFileDropWidget",
        "(function(e,t,n){t.wtLObj=this;const i=this,o=e.WT;let s=\"Wt-dropzone-hover\";const d=\"Wt-dropzone-indication\",r=\"Wt-dropzone-dragstyle\",l=[];let a=!1,c=!0,u=!1,f=!1,p=null,h=0,g=0;const m=document.createElement(\"input\");m.type=\"file\";m.setAttribute(\"multiple\",\"multiple\");m.style.display=\"none\";t.appendChild(m);const v=document.createElement(\"div\");v.classList.add(\"Wt-dropcover\");document.body.appendChild(v);this.validFileCheck=function(e,t,n){const i=new FileReader;i.onload=function(){t(!0,n)};i.onerror=function(){t(!1,n)};i.readAsText(e.slice(0,32))};t.setAcceptDrops=function(e){c=e};t.setDropIndication=function(e){u=e};t.setDropForward=function(e){f=e};t.ondragenter=function(e){if(c){if(function(e){const t=e.dataTransfer?.items??null,n=null!==t&&Array.prototype.some.call(t,(e=>\"file\"===e.kind)),i=e.dataTransfer?.types??null,o=null!==i&&i.includes(\"Files\");return n||o}(e)){0===g&&i.setPageHoverStyle();g=2;i.setWidgetHoverStyle(!0)}e.stopPropagation()}};t.ondragleave=function(e){const t=e.clientX,n=e.clientY;let o=document.elementFromPoint(t,n);0===t&&0===n&&(o=null);if(o!==v)i.resetDragDrop();else{i.setWidgetHoverStyle(!1);g=1}};t.ondragover=function(e){e.preventDefault()};const y=function(e){if((u||f)&&\"none\"!==o.css(t,\"display\")&&c){g=1;i.setPageHoverStyle()}};document.body.addEventListener(\"dragenter\",y);v.ondragover=function(e){e.preventDefault();e.stopPropagation()};v.ondragleave=function(e){c&&1===g&&i.resetDragDrop()};v.ondrop=function(e){e.preventDefault();f?t.ondrop(e):i.resetDragDrop()};t.ondrop=function(e){e.preventDefault();if(c){i.resetDragDrop();0!==e.dataTransfer.files.length&&i.addFiles(e.dataTransfer.files)}};this.addFiles=function(n){const i=[];for(const e of n){const t=new Object;t.id=Math.floor(Math.random()*Math.pow(2,31));t.file=e;l.push(t);const n={};n.id=t.id;n.filename=t.file.name;n.type=t.file.type;n.size=t.file.size;i.push(n)}e.emit(t,\"dropsignal\",JSON.stringify(i))};t.addEventListener(\"click\",(function(){if(c){m.value=\"\";m.click()}}));t.markForSending=function(e){for(const t of e){const e=t.id;for(const t of l)if(t.id===e){t.ready=!0;break}}a||l[0].ready&&i.requestSend()};this.requestSend=function(){if(l[0].skip)i.uploadFinished(null);else{a=!0;e.emit(t,\"requestsend\",l[0].id)}};t.send=function(o,s){const d=l[0];if(d.file.size>n){e.emit(t,\"filetoolarge\",d.file.size);i.uploadFinished(null)}else{const e=null!==p&&s?i.workerSend:i.actualSend;i.validFileCheck(d.file,e,o)}};this.actualSend=function(e,t){if(!e){i.uploadFinished(null);return}const n=new XMLHttpRequest;n.addEventListener(\"load\",i.uploadFinished);n.addEventListener(\"error\",i.uploadFinished);n.addEventListener(\"abort\",i.uploadFinished);n.addEventListener(\"timeout\",i.uploadFinished);n.open(\"POST\",t);l[0].request=n;const o=new FormData;o.append(\"file-id\",l[0].id);o.append(\"data\",l[0].file);n.send(o)};this.workerSend=function(e,t){if(e){p.upload=l[0];p.postMessage({cmd:\"send\",url:t,upload:l[0],chunksize:h})}else i.uploadFinished(null)};this.uploadFinished=function(n){(null!=n&&\"load\"===n.type&&200===n.currentTarget.status||!0===n)&&e.emit(t,\"uploadfinished\",l[0].id);l.splice(0,1);if(l[0]&&l[0].ready)i.requestSend();else{a=!1;e.emit(t,\"donesending\")}};t.cancelUpload=function(e){if(l[0]&&l[0].id===e){l[0].skip=!0;l[0].request?l[0].request.abort():p&&p.upload===l[0]&&p.postMessage({cmd:\"cancel\",upload:l[0]})}else for(let t=1;t<l.length;t++)l[t].id===e&&(l[t].skip=!0)};m.onchange=function(){c&&null!==this.files&&0!==this.files.length&&i.addFiles(this.files)};this.setPageHoverStyle=function(){if(u||f){v.classList.add(r);t.classList.add(r);u&&t.classList.add(d)}};this.setWidgetHoverStyle=function(e){t.classList.toggle(s,e)};this.resetDragDrop=function(){t.classList.remove(d);t.classList.remove(r);v.classList.remove(r);i.setWidgetHoverStyle(!1);g=0};t.configureHoverClass=function(e){s=e};t.setFilters=function(e){m.setAttribute(\"accept\",e)};t.setUploadWorker=function(n){if(n&&window.Worker){p=new Worker(n);p.onmessage=function(n){if(n.data.workerfeatures){if(\"valid\"!==n.data.workerfeatures){t.setUploadWorker(null);e.emit(t,\"filternotsupported\")}}else i.uploadFinished(n.data)};p.postMessage({cmd:\"check\"})}else p=null};t.setChunkSize=function(e){h=e};t.destructor=function(){document.body.removeEventListener(\"dragenter\",y);document.body.removeChild(v)}})");
  }

  static final class WFileDropUploadResource extends WResource {
    private static Logger logger = LoggerFactory.getLogger(WFileDropUploadResource.class);

    WFileDropUploadResource(WFileDropWidget fileDropWidget, WFileDropWidget.File file) {
      super();
      this.parent_ = fileDropWidget;
      this.currentFile_ = file;
      this.setUploadProgress(true);
    }

    void setCurrentFile(WFileDropWidget.File file) {
      this.currentFile_ = file;
    }

    protected void handleRequest(final WebRequest request, final WebResponse response) {
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
      CollectionUtils.findInMultimap(request.getUploadedFiles(), "data", files);
      if (files.isEmpty()) {
        response.setStatus(404);
        return;
      }
      String filtFlag = request.getParameter("filtered");
      this.currentFile_.setIsFiltered(filtFlag != null && filtFlag.equals("true"));
      String lastFlag = request.getParameter("last");
      boolean isLast = lastFlag == null || lastFlag != null && lastFlag.equals("true");
      this.currentFile_.handleIncomingData(files.get(0), isLast);
      if (isLast) {
        this.parent_.proceedToNextFile();
      }
      response.setContentType("text/plain");
    }

    private WFileDropWidget parent_;
    private WFileDropWidget.File currentFile_;
  }
}
