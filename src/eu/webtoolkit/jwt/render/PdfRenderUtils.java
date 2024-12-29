package eu.webtoolkit.jwt.render;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pdfjet.PDF;
import com.pdfjet.Page;

public class PdfRenderUtils {
	private static final Logger logger = LoggerFactory.getLogger(PdfRenderUtils.class);
	
	public static Page createPage(PDF pdf, double width, double height) {
		float [] size =  new float [2];
		size[0] = (float) width;
		size[1] = (float) height;
		try {
			return new Page(pdf, size);
		} catch (Exception e) {
			logger.info("Exception creating page {}x{}", width, height, e);
			return null;
		}
	}
}
