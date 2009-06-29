package eu.webtoolkit.jwt;

import java.io.FileInputStream;
import java.io.IOException;

import eu.webtoolkit.jwt.servlet.WebRequest;
import eu.webtoolkit.jwt.servlet.WebResponse;
import eu.webtoolkit.jwt.utils.StreamUtils;

/**
 * A resource which streams data from a local file.
 * <p>
 * To update the resource, either use setFileName() to point it to a
 * new file, or trigger the {@link #dataChanged()} signal when only the
 * file contents has changed, but not the filename.
 */
public class WFileResource extends WResource {
	/**
	 * Creates a new file resource.
	 * 
	 * @param mimeType the mime type of the file.
	 * @param fileName the file name.
	 */
	public WFileResource(String mimeType, String fileName) {
		super();
		this.mimeType_ = mimeType;
		this.fileName_ = fileName;
	}

	/**
	 * Changes the file name.
	 * <p>
	 * This causes the resource to be refreshed in the browser by triggering {@link #dataChanged()}.
	 * 
	 * @param fileName the new filename.
	 */
	public void setFileName(String fileName) {
		this.fileName_ = fileName;
		this.dataChanged().trigger();
	}

	/**
	 * Returns the file name.
	 * 
	 * @return the file name.
	 */
	public String getFileName() {
		return this.fileName_;
	}

	/**
	 * Returns the mime type.
	 * 
	 * @return the mime type.
	 */
	public String getMimeType() {
		return this.mimeType_;
	}

	/**
	 * Changes the mime type.
	 * <p>
	 * This causes the resource to be refreshed in the browser by triggering {@link #dataChanged()}.
	 * 
	 * @param mimeType
	 */
	public void setMimeType(String mimeType) {
		this.mimeType_ = mimeType;
		this.dataChanged().trigger();
	}

	private String mimeType_;
	private String fileName_;

	@Override
	public void handleRequest(WebRequest request, WebResponse response) {
		response.setContentType(mimeType_);

		try {
			FileInputStream fis = new FileInputStream(fileName_);
			try {
				StreamUtils.copy(fis, response.getOutputStream());
				response.getOutputStream().flush();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				StreamUtils.closeQuietly(fis);
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
}
