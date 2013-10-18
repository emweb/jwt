package eu.webtoolkit.jwt.render;

import com.pdfjet.PDF;
import com.pdfjet.Page;

public class PdfRenderUtils {
	public static Page createPage(PDF pdf, double width, double height) {
		float [] size =  new float [2];
		size[0] = (float) width;
		size[1] = (float) height;
		try {
			return new Page(pdf, size);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
