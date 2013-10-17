package eu.webtoolkit.jwt.render;

import java.io.ByteArrayOutputStream;

import org.junit.Test;

import com.pdfjet.A4;
import com.pdfjet.PDF;
import com.pdfjet.Page;

import eu.webtoolkit.jwt.FileUtils;

public class XHTMLParserTest {
	@Test
	public void test1() throws Exception {
		PDF pdf = new PDF(new ByteArrayOutputStream());
		renderPdf(FileUtils.resourceToString("/eu/webtoolkit/jwt/render/test1.html"), pdf);
		pdf.flush();
	}

    private void renderPdf(String html, PDF pdf) throws Exception
    {
        Page page = new Page(pdf, A4.PORTRAIT);
        WPdfRenderer renderer = new WPdfRenderer(pdf, page);        
        renderer.setMargin(2.54);
        renderer.setDpi(96);
        renderer.render(html);
    }
}
