package eu.webtoolkit.jwt.examples.widgetgallery;
import java.io.IOException;
import com.pdfjet.A4;
import com.pdfjet.PDF;
import com.pdfjet.Page;
import eu.webtoolkit.jwt.WObject;
import eu.webtoolkit.jwt.WResource;
import eu.webtoolkit.jwt.WString;
import eu.webtoolkit.jwt.render.WPdfRenderer;
import eu.webtoolkit.jwt.servlet.WebRequest;
import eu.webtoolkit.jwt.servlet.WebResponse;
public class ReportResource extends WResource {

	public ReportResource() {
		suggestFileName("report.pdf");
	}
	
	@Override
	protected void handleRequest(WebRequest request, WebResponse response)
			throws IOException {
		response.setContentType("application/pdf");

		try {
			PDF pdf = new PDF(response.getOutputStream());		    
			renderReport(pdf);
			pdf.flush();
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	private void renderReport(PDF pdf) throws Exception {
		renderPdf(WString.tr("report.example"), pdf);
	}

	private void renderPdf(WString html, PDF pdf) throws Exception
	{
		Page page = new Page(pdf, A4.PORTRAIT);
		WPdfRenderer renderer = new WPdfRenderer(pdf, page);		
		renderer.setMargin(2.54);
		renderer.setDpi(96);
		renderer.render(html);
	}
}

