package eu.webtoolkit.jwt;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.EnumSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pdfjet.Cap;
import com.pdfjet.CodePage;
import com.pdfjet.Embed;
import com.pdfjet.Font;
import com.pdfjet.Image;
import com.pdfjet.ImageType;
import com.pdfjet.Join;
import com.pdfjet.PDF;
import com.pdfjet.Page;
import com.pdfjet.Point;

import eu.webtoolkit.jwt.FontSupport.FontMatch;
import eu.webtoolkit.jwt.WTransform.TRSSDecomposition;
import eu.webtoolkit.jwt.servlet.WebRequest;
import eu.webtoolkit.jwt.servlet.WebResponse;
import eu.webtoolkit.jwt.utils.EnumUtils;

public class WPdfImage extends WResource implements WPaintDevice {
	private static final Logger logger = LoggerFactory.getLogger(WPdfImage.class);

	private static Constructor<?> fontConstructor;

	static {
		for (Constructor<?> c : Font.class.getConstructors()) {
			Class<?>[] paramTypes = c.getParameterTypes();
			if (paramTypes.length == 4 &&
					paramTypes[0] == PDF.class &&
					paramTypes[1] == java.io.InputStream.class &&
					paramTypes[2].isPrimitive() && paramTypes[2].equals(java.lang.Integer.TYPE) &&
					paramTypes[3].isPrimitive() && paramTypes[3].equals(java.lang.Boolean.TYPE)) {
				fontConstructor = c;
				break;
			}
		}
	}

	public WPdfImage(WLength width, WLength height) {
		this.width = width;
		this.height = height;

		this.changeFlags = EnumSet.allOf(PainterChangeFlag.class);

        try {
        	this.bos = new ByteArrayOutputStream();
			this.pdf = new PDF(bos);
			this.page = new Page(pdf, getSizeArray(width, height));
			this.x = 0;
			this.y = 0;

			trueTypeFonts = new FontSupport(this);
		} catch (Exception e) {
			logger.info("Exception", e);
		}
	}

	public WPdfImage(PDF pdf, Page page, int x, int y, double width, double height) {
		this(pdf, page, x, y, new WLength(width), new WLength(height));
	}

	public WPdfImage(PDF pdf, Page page, int x, int y, WLength width, WLength height) {
		this.pdf = pdf;
		this.page = page;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;

		this.changeFlags = EnumSet.allOf(PainterChangeFlag.class);

		trueTypeFonts = new FontSupport(this);

		setDeviceTransform(new WTransform());
	}

	public void addFontCollection(String directory, boolean recursive)
	{
	  trueTypeFonts.addFontCollection(directory, recursive);
	}

	private float[] getSizeArray(WLength width, WLength height)
	{
		float [] size = new float[2];
		size[0] = (float)width.getValue();
		size[1] = (float)height.getValue();
		return size;
	}

	@Override
	public EnumSet<PaintDeviceFeatureFlag> getFeatures() {
		return EnumSet.of(PaintDeviceFeatureFlag.FontMetrics, PaintDeviceFeatureFlag.WordWrap);
	}

	@Override
	public WLength getWidth() {
		return this.width;
	}

	@Override
	public WLength getHeight() {
		return this.height;
	}

	@Override
	public void setChanged(EnumSet<PainterChangeFlag> flags) {
		this.changeFlags.addAll(flags);
	}

	@Override
	public void setChanged(PainterChangeFlag flag, PainterChangeFlag... flags) {
		setChanged(EnumSet.of(flag, flags));
	}

	private void _drawArc(double x,	double y, double ray, double ang1, double ang2)
	{
		boolean cont_flg = false;

		double angle = Math.abs(ang2 - ang1);
	    if (angle > 360)
	    	throw new RuntimeException("Angle out of range: " + angle);

	    while (ang1 < 0 || ang2 < 0) {
	        ang1 = ang1 + 360;
	        ang2 = ang2 + 360;
	    }

	    for (;;) {
	        if (Math.abs(ang2 - ang1) <= 90) {
	            _internalArc (x, y, ray, ang1, ang2, cont_flg);
	            break;
	        } else {
	            double tmp_ang = (ang2 > ang1 ? ang1 + 90 : ang1 - 90);

	            _internalArc (x, y, ray, ang1, tmp_ang, cont_flg);

	            cont_flg = true;

	            ang1 = tmp_ang;
	        }

	        if (Math.abs(ang1 - ang2) < 0.1)
	            break;
	    }
	}

	private void _internalArc(double x, double y, double ray, double ang1, double ang2, boolean cont_flg) {
		double rx0, ry0, rx1, ry1, rx2, ry2, rx3, ry3;
		double x0, y0, x1, y1, x2, y2, x3, y3;
		double delta_angle;
		double new_angle;

		delta_angle = (90 - (double) (ang1 + ang2) / 2) / 180 * Math.PI;
		new_angle = (double) (ang2 - ang1) / 2 / 180 * Math.PI;

		rx0 = ray * Math.cos(new_angle);
		ry0 = ray * Math.sin(new_angle);
		rx2 = (ray * 4.0 - rx0) / 3.0;
		ry2 = ((ray * 1.0 - rx0) * (rx0 - ray * 3.0)) / (3.0 * ry0);
		rx1 = rx2;
		ry1 = -ry2;
		rx3 = rx0;
		ry3 = -ry0;

		x0 = rx0 * Math.cos(delta_angle) - ry0 * Math.sin(delta_angle) + x;
		y0 = rx0 * Math.sin(delta_angle) + ry0 * Math.cos(delta_angle) + y;
		x1 = rx1 * Math.cos(delta_angle) - ry1 * Math.sin(delta_angle) + x;
		y1 = rx1 * Math.sin(delta_angle) + ry1 * Math.cos(delta_angle) + y;
		x2 = rx2 * Math.cos(delta_angle) - ry2 * Math.sin(delta_angle) + x;
		y2 = rx2 * Math.sin(delta_angle) + ry2 * Math.cos(delta_angle) + y;
		x3 = rx3 * Math.cos(delta_angle) - ry3 * Math.sin(delta_angle) + x;
		y3 = rx3 * Math.sin(delta_angle) + ry3 * Math.cos(delta_angle) + y;

		if (!cont_flg)
			_lineTo(x0, y0);

		_cubicBezierCurveTo(x1, y1, x2, y2, x3, y3);
	}

	@Override
	public void drawArc(WRectF rect, double startAngle, double spanAngle) {
		WPainterPath pp = new WPainterPath();
		pp.arcTo(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(), startAngle, spanAngle);
		drawPath(pp);
	}

	@Override
	public void drawRect(WRectF rect) {
		drawPath(rect.toPath());
	}

	@Override
	public void drawImage(WRectF rect, String imgUrl, int imgWidth, int imgHeight, WRectF sourceRect) {
		WDataInfo imgInfo = new WDataInfo();
		if (DataUri.isDataUri(imgUrl)) {
			imgInfo.setDataUri(imgUrl);
		} else {
			imgInfo.setFilePath(imgUrl);
			imgInfo.setUrl(imgUrl);
		}
		doDrawImage(rect, imgInfo, imgWidth, imgHeight, sourceRect);
	}

	@Override
	public void drawImage(WRectF rect, WAbstractDataInfo imgInfo, int imgWidth, int imgHeight, WRectF sourceRect) {
		doDrawImage(rect, imgInfo, imgWidth, imgHeight, sourceRect);
	}

	private void doDrawImage(WRectF rect, WAbstractDataInfo imgInfo, int imgWidth, int imgHeight, WRectF sourceRect) {
		processChangeFlags();

		Image image = null;
		if (imgInfo.hasDataUri()) {
			String imgUri = imgInfo.getDataUri();
			DataUri uri = new DataUri(imgUri);
			try {
				// we do not import the ImageUtils class from utils because we also use another ImageUtils
				ByteArrayInputStream stream = eu.webtoolkit.jwt.utils.ImageUtils.getByteArrayInputStream(uri.data);

				if ("image/png".equals(uri.mimeType))
					image = new Image(this.pdf, stream, ImageType.PNG);
				else if ("image/jpeg".equals(uri.mimeType))
					image = new Image(this.pdf, stream, ImageType.JPG);
				else if ("image/bmp".equals(uri.mimeType))
					image = new Image(this.pdf, stream, ImageType.BMP);
			} catch (Exception e) {
				logger.info("Error converting data URI to image", e);
				logger.trace("Data URI is {}", imgUri, e);
			}
		} else {
			String imgPath = imgInfo.getFilePath();
			String mimeType = ImageUtils.identifyMimeType(imgPath);
			try {
				if ("image/png".equals(mimeType))
					image = new Image(this.pdf, new BufferedInputStream(FileUtils.getResourceAsStream(imgPath)), ImageType.PNG);
				else if ("image/jpeg".equals(mimeType))
					image = new Image(this.pdf, new BufferedInputStream(FileUtils.getResourceAsStream(imgPath)), ImageType.JPG);
				else if ("image/bmp".equals(mimeType))
					image = new Image(this.pdf, new BufferedInputStream(FileUtils.getResourceAsStream(imgPath)), ImageType.BMP);
			} catch (Exception e) {
				logger.info("Error creating image from {}", imgPath, e);
			}
		}

		if (image != null) {
			WPointF p = currentTransform.map(new WPointF(rect.getX(), rect.getY()));
	        image.setPosition(p.getX(), p.getY());

	        double w = image.getWidth();
	        double h = image.getHeight();

			TRSSDecomposition d = new TRSSDecomposition();
			currentTransform.decomposeTranslateRotateScaleSkew(d);
	        float xScale  = (float) ((rect.getWidth() / w) * d.sx);
	        float yScale  = (float) ((rect.getHeight() / h) * d.sy);
	        image.scaleBy(xScale, yScale);
	        try {
				image.drawOn(this.page);
			} catch (Exception e) {
				logger.info("Exception while drawing image", e);
			}
		}
	}

	@Override
	public void drawLine(double x1, double y1, double x2, double y2) {
		  WPainterPath path = new WPainterPath();
		  path.moveTo(x1, y1);
		  path.lineTo(x2, y2);
		  drawPath(path);
	}

	private WPointF _transform(double x, double y) {
		return currentTransform.map(new WPointF(x, y));
	}

	private Point _transformToPoint(double x, double y) {
		WPointF p = _transform(x, y);
		return new Point(p.getX(), p.getY());
	}

	private void _moveTo(double x, double y){
		try {
			WPointF p_t = _transform(x, y);
			this.page.moveTo(p_t.getX(), p_t.getY());
		} catch (IOException e) {
			logger.info("IOException", e);
		}
	}

	private void _lineTo(double x, double y) {
		try {
			WPointF p_t = _transform(x, y);
			this.page.lineTo(p_t.getX(), p_t.getY());
		} catch (IOException e) {
			logger.info("IOException", e);
		}
	}

	private void _cubicBezierCurveTo(double x1, double y1, double x2, double y2, double x3, double y3) {
		Point p1 = _transformToPoint(x1, y1);
		Point p2 = _transformToPoint(x2, y2);
		Point p3 = _transformToPoint(x3, y3);
		try {
			this.page.bezierCurveTo(p1, p2, p3);
		} catch (IOException e) {
			logger.info("IOException", e);
		}
	}

	private void drawPlainPath(WPainterPath path) {
		List<WPainterPath.Segment> segments = path.getSegments();

		if (segments.size() > 0
				&& segments.get(0).getType() != SegmentType.MoveTo)
			_moveTo(0, 0);

		for (int i = 0; i < segments.size(); ++i) {
			WPainterPath.Segment s = segments.get(i);

			switch (s.getType()) {
			case MoveTo:
				_moveTo(s.getX(), s.getY());
				break;
			case LineTo:
				_lineTo(s.getX(), s.getY());
				break;
			case CubicC1: {
				final double x1 = s.getX();
				final double y1 = s.getY();
				final double x2 = segments.get(i + 1).getX();
				final double y2 = segments.get(i + 1).getY();
				final double x3 = segments.get(i + 2).getX();
				final double y3 = segments.get(i + 2).getY();

				WPointF current = path.getPositionAtSegment(i);
				_cubicBezierCurveTo(x1, y1, x2, y2, x3, y3);

				i += 2;
				break;
			}
			case CubicC2:
			case CubicEnd:
				assert (false);
			case ArcC: {
				final double x = s.getX();
				final double y = s.getY();
				final double radius = segments.get(i + 1).getX();
				double ang1 = segments.get(i + 2).getX();
				double ang2 = ang1 + segments.get(i + 2).getY();

				_drawArc(x, y, radius, ang1 + 90, ang2 + 90);

				i += 2;
				break;
			}
			case ArcR:
			case ArcAngleSweep:
				assert (false);
			case QuadC: {
				/*
				 * There is no quadratic bezier curve in pdfjet, so we emulate
				 * it using a cubic bezier curve.
				 */
				WPointF current = path.getPositionAtSegment(i);
				final double cpx = s.getX();
				final double cpy = s.getY();
				final double x = segments.get(i + 1).getX();
				final double y = segments.get(i + 1).getY();

				final double cp1x = current.getX() + 2.0 / 3.0 * (cpx - current.getX());
				final double cp1y = current.getY() + 2.0 / 3.0 * (cpy - current.getY());
				final double cp2x = cp1x + (x - current.getX()) / 3.0;
				final double cp2y = cp1y + (y - current.getY()) / 3.0;

				_cubicBezierCurveTo(cp1x,	cp1y, cp2x, cp2y, cpx, cpy);

				i += 1;

				break;
			}
			case QuadEnd:
				assert (false);
			}
		}
	}

	@Override
	public void drawPath(WPainterPath path) {
		processChangeFlags();

		try {
			boolean hasPen = getPainter().getPen().getStyle() != PenStyle.None;
			boolean hasBrush = getPainter().getBrush().getStyle() != BrushStyle.None;

			if (hasPen || hasBrush) {
				if (hasBrush) {
					prepareBrush();
					drawPlainPath(path);
					page.fillPath();
				}

				if (hasPen) {
	 				preparePen();
	 				drawPlainPath(path);
					page.strokePath();
				}
			} else {
				page.closePath();
			}
		} catch (Exception e) {
			logger.info("Exception drawing path", e);
		}
	}

	@Override
	public void drawText(WRectF rect, EnumSet<AlignmentFlag> flags, TextFlag textFlag, CharSequence text, WPointF clipPoint) {
		if (textFlag == TextFlag.WordWrap)
			throw new UnsupportedOperationException("drawText(): TextWordWrap not yet implemented");

		if (clipPoint != null && this.getPainter() != null) {
		    if (!this.getPainter().getClipPathTransform().map(this.getPainter().getClipPath())
			    .isPointInPath(this.getPainter().getWorldTransform().map(clipPoint))) {
			return;
		    }
		}

		WColor penColor = painter.getPen().getColor();
		try {
			setBrushColor(penColor);
		} catch (IOException e2) {
			logger.info("IOException", e2);
		}

		processChangeFlags();

		TRSSDecomposition d = new TRSSDecomposition();
		currentTransform.decomposeTranslateRotateScaleSkew(d);

		try {
			page.setTextDirection(360 - (int)Math.round(d.alpha *  180 / Math.PI));
		} catch (Exception e1) {
			logger.info("IOException", e1);
		}

		double px = 0, py = 0;

		AlignmentFlag horizontalAlign = EnumUtils.enumFromSet(EnumUtils.mask(flags, AlignmentFlag.AlignHorizontalMask));
		AlignmentFlag verticalAlign = EnumUtils.enumFromSet(EnumUtils.mask(flags, AlignmentFlag.AlignVerticalMask));

		String s = text.toString();

		switch (horizontalAlign) {
		case Left:
			px = rect.getLeft();
			break;
		case Right:
			px = rect.getRight() - this.font.stringWidth(s);
			break;
		case Center:
			px = rect.getCenter().getX() - this.font.stringWidth(s) / 2;
			break;
		}

		switch (verticalAlign) {
		case Bottom:
			py = rect.getBottom();
			break;
		case Top:
			py = rect.getTop() + getFontMetrics().getHeight();
			break;
		case Middle:
			py = rect.getCenter().getY() + getFontMetrics().getHeight()/2;
		}

		py -= getFontMetrics().getDescent();

		preparePen();
		try {
			WPointF p = new WPointF(px, py);
			p = currentTransform.map(p);

			double originalSize = this.font.getSize();
			this.font.setSize(originalSize * (d.sx + d.sy) / 2);

			page.drawString(font, s, p.getX(), p.getY());

			this.font.setSize(originalSize);
		} catch (IOException e) {
			logger.info("IOException", e);
		}

		WColor brushColor = painter.getBrush().getColor();
		try {
			setBrushColor(brushColor);
		} catch (IOException e2) {
			logger.info("IOException", e2);
		}
	}

	@Override
	public WTextItem measureText(CharSequence text, double maxWidth, boolean wordWrap) {
		processChangeFlags();

		if (wordWrap) {
			if (maxWidth == -1) {
				return new WTextItem(text, font.stringWidth(text.toString()));
			} else {
				String previousWord = null;
				double previousWordWidth = 0;

				for (int i = 0; i < text.length(); ++i) {
					double w;
					if (Character.isWhitespace(text.charAt(i)))
						w = font.stringWidth(text.subSequence(0, i).toString());
					else if (i == text.length() - 1)
						w = font.stringWidth(text.subSequence(0, i + 1).toString());
					else
						continue;

					String s = text.subSequence(0, i + 1).toString();

					if (w > maxWidth) {
						if (previousWord == null)
							return new WTextItem(s, w);
						else
							return new WTextItem(previousWord, previousWordWidth);
					} else {
						previousWord = s;
						previousWordWidth = w;
					}
				}
				return new WTextItem(text, font.stringWidth(text.toString()));
			}
		} else {
			return new WTextItem(text, font.stringWidth(text.toString()));
		}
	}

	@Override
	public WTextItem measureText(CharSequence text) {
		return this.measureText(text, -1);
	}

	@Override
	public WTextItem measureText(CharSequence text, double maxWidth) {
		return this.measureText(text, maxWidth, false);
	}

	@Override
	public WFontMetrics getFontMetrics() {
		processChangeFlags();

		double ascent = this.font.getAscent();
		double descent = this.font.getDescent();
		//TODO leading??
		double leading = 0;

		return new WFontMetrics(getPainter().getFont(), leading, ascent, descent);
	}

	@Override
	public void init() {

	}

	@Override
	public void done() {
	}

	@Override
	public boolean isPaintActive() {
		return painter != null;
	}

	@Override
	public WPainter getPainter() {
		return this.painter;
	}

	@Override
	public void setPainter(WPainter painter) {
		this.painter = painter;
	}

	@Override
	protected void handleRequest(WebRequest request, WebResponse response) throws IOException {
		response.setContentType("application/pdf");

		write(response.getOutputStream());
	}

	public void write(OutputStream os) throws IOException {
		try {
			this.pdf.flush();
		} catch (Exception e) {
			logger.info("Exception flushing pdf", e);
		}

		os.write(bos.toByteArray());
	}

	private void setPenColor(WColor c) throws IOException {
		page.setPenColor(c.getRed() / 255., c.getGreen() / 255., c.getBlue() / 255.);
	}

	private void preparePen() {
		WColor c = painter.getPen().getColor();
		try {
			setPenColor(c);

			if (stroke.pattern != null)
				page.setLinePattern(stroke.pattern);
			else
				page.setDefaultLinePattern();
			page.setLineCapStyle(stroke.cap);
			page.setLineJoinStyle(stroke.join);
			page.setPenWidth(stroke.width * 0.75); // pixel to point
		} catch (IOException e) {
			logger.info("IOException", e);
		}
	}

	private void setBrushColor(WColor c) throws IOException {
		page.setBrushColor(c.getRed() / 255., c.getGreen() / 255., c.getBlue() / 255.);
	}

	private void prepareBrush() {
		WColor c = painter.getBrush().getColor();
		try {
			setBrushColor(c);
		} catch (IOException e) {
			logger.info("IOException", e);
		}
	}

	private class Stroke {
		public Stroke(int cap, int join, float width, String pattern) {
			this.cap = cap;
			this.join = join;
			this.width = width;
			this.pattern = pattern;
		}

		int cap;
		int join;
		float width;
		String pattern;
	}
	private Stroke createStroke(WPainter painter, WPen pen) {
		int cap = 0;
		switch (pen.getCapStyle()) {
		case Flat:   cap = Cap.BUTT; break;
		case Round:  cap = Cap.ROUND; break;
		case Square: cap = Cap.PROJECTING_SQUARE; break;
		}

		int join = 0;
		switch (pen.getJoinStyle()) {
		case Bevel: join = Join.BEVEL; break;
		case Miter: join = Join.MITER; break;
		case Round: join = Join.ROUND; break;
		}

		float width = 0;
		if (pen.getStyle() != PenStyle.None)
			width = (float) painter.normalizedPenWidth(pen.getWidth(), false).toPixels();

		String pattern = null;
		switch (pen.getStyle()) {
		case DashLine:
			pattern = "[4 2] 0";
			break;
		case DotLine:
			pattern = "[1 2] 0";
			break;
		case DashDotLine:
			pattern = "[4 2 1 2] 0";
			break;
		case DashDotDotLine:
			pattern = "[4 2 1 2 1 2] 0";
			break;
		default:
			pattern = "[] 0";
			break;

		}

		return new Stroke(cap, join, width, pattern);
	}

	private void processChangeFlags() {
		boolean resetTransform = changeFlags.contains(PainterChangeFlag.Transform);

		//TODO clipping + transform
		/*
		  if (changeFlags.contains(PainterChangeFlag.Clipping)) {
			setTransform(painter.getClipPathTransform());
			if (painter.getClipPath().isEmpty())
				g2.setClip(null);
			else
				g2.setClip(createShape(painter.getClipPath()));
			resetTransform = true;
		}*/

		if (resetTransform) {
			currentTransform = painter.getCombinedTransform().clone();
			if (deviceTransform != null)
				currentTransform = deviceTransform.multiply(currentTransform);
		}

		if (changeFlags.contains(PainterChangeFlag.Pen))
			stroke = createStroke(painter, painter.getPen());

		if (resetTransform || changeFlags.contains(PainterChangeFlag.Font)) {
			TRSSDecomposition d = new TRSSDecomposition();
			currentTransform.decomposeTranslateRotateScaleSkew(d);

			this.font = createFont(painter.getFont());
		}

		changeFlags.clear();
	}

	private Font createFont(WFont font) {
		if (fontConstructor != null) {
			FontMatch fm = trueTypeFonts.matchFont(font);
			if (fm.isMatched()) {
				try {
					FileInputStream fis = new FileInputStream(fm.getFileName());
					Font f = (Font)fontConstructor.newInstance(pdf, fis, CodePage.UNICODE, Embed.YES);
					f.setSize(font.getSizeLength().toPixels());
					return f;
				} catch (IllegalArgumentException e) {
					logger.error("IllegalArgumentException while creating font {}", font.getCssText(), e);
				} catch (InstantiationException e) {
					logger.error("InstantiationException while creating font {}", font.getCssText(), e);
				} catch (IllegalAccessException e) {
					logger.error("IllegalAccessException while creating font {}", font.getCssText(), e);
				} catch (InvocationTargetException e) {
					logger.error("InvocationTargetException while creating font {}", font.getCssText(), e);
				} catch (FileNotFoundException e) {
					logger.info("FileNotFoundException while creating font {}", font.getCssText(), e);
				}
			}
		}

		String name = PdfUtils.toBase14Font(font);
		try {
			Font f = new Font(pdf, name);
			f.setSize(font.getSizeLength().toPixels());
			return f;
		} catch (Exception e) {
			logger.info("Error creating font {}", font.getCssText(), e);
			return null;
		}
	}

	public void setDeviceTransform(WTransform transform) {
		this.deviceTransform = new WTransform();
		this.deviceTransform.translate(this.x, this.y);
		this.deviceTransform.multiplyAndAssign(transform);
		changeFlags.add(PainterChangeFlag.Transform);
	}

	private WPainter painter;
	private EnumSet<PainterChangeFlag> changeFlags;

	private WTransform deviceTransform;

	private Font font;
	private Stroke stroke;

    private PDF pdf;
    private Page page;

    private double x;
    private double y;
    private WLength width;
    private WLength height;

    private WTransform currentTransform;

    private ByteArrayOutputStream bos;

    private FontSupport trueTypeFonts;
}
