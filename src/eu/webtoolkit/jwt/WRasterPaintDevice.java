package eu.webtoolkit.jwt;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Iterator;

import javax.imageio.ImageIO;

import eu.webtoolkit.jwt.WPainterPath.Segment;
import eu.webtoolkit.jwt.servlet.WebRequest;
import eu.webtoolkit.jwt.servlet.WebResponse;
import eu.webtoolkit.jwt.utils.EnumUtils;

public class WRasterPaintDevice extends WResource implements WPaintDevice {
	private static final int MITER_LIMIT = 10;

	enum Format { PngFormat }

	private Format format;
	private WLength width;
	private WLength height;

	private WPainter painter;
	private EnumSet<ChangeFlag> changeFlags;

	private BufferedImage image;
	private Graphics2D g2;
	private Paint penPaint, brushPaint;
	private boolean fontChanged;

	public WRasterPaintDevice(String format, WLength width, WLength height) {
		this.width = width;
		this.height = height;
		if (format.equals("png"))
		    this.format = Format.PngFormat;
		else
		    throw new RuntimeException("Unsupported format: " + format);
		this.changeFlags = EnumSet.noneOf(ChangeFlag.class);
		
		if (width.toPixels() > 0 && height.toPixels() > 0)
			this.image = new BufferedImage((int)width.toPixels(), (int)height.toPixels(), BufferedImage.TYPE_INT_ARGB);
	}

	protected void handleRequest(WebRequest request, WebResponse response) throws IOException {
		response.setContentType("image/png");
		if (image != null)
			ImageIO.write(image, "png", response.getOutputStream());
	}

	
	public void done() {
	}

	
	public void drawArc(WRectF rect, double startAngle, double spanAngle) {
		drawShape(new Arc2D.Double(rect.getLeft(), rect.getTop(), rect.getWidth(), rect.getHeight(),
			startAngle, spanAngle, Arc2D.OPEN));
	}

	
	public void drawImage(WRectF rect, String imageUri, int imgWidth, int imgHeight, WRectF sourceRect) {
		// TODO Auto-generated method stub
	}

	
	public void drawLine(double x1, double y1, double x2, double y2) {
		drawShape(new Line2D.Double(x1, y1, x2, y2));
	}

	
	public void drawPath(WPainterPath path) {
		drawShape(createShape(path));
	}

	/**
	 * Converts a jwt.WPainterPath to an awt.Shape
	 * 
	 * @param path
	 * @return a shape that represents the path
	 */
	public static Shape createShape(WPainterPath path) {
		GeneralPath p = new GeneralPath();

		for (Iterator<Segment> i = path.getSegments().iterator(); i.hasNext();) {
			Segment s = i.next();
			switch (s.getType()) {
			case MoveTo:
				p.moveTo((float)s.getX(), (float)s.getY());
				break;
			case LineTo:
				p.lineTo((float)s.getX(), (float)s.getY());
				break;
			case QuadC:
				double x1 = s.getX();
				double y1 = s.getY();
				s = i.next();
				p.quadTo((float)x1, (float)y1, (float)s.getX(), (float)s.getY());
				break;
			case CubicC1:
				x1 = s.getX();
				y1 = s.getY();
				s = i.next();
				double x2 = s.getX();
				double y2 = s.getY();
				s = i.next();
				p.curveTo((float)x1, (float)y1, (float)x2, (float)y2, (float)s.getX(), (float)s.getY());
				break;
			case ArcC:
				double cx = s.getX();
				double cy = s.getY();
				s = i.next();
				double rx = s.getX();
				double ry = s.getY();
				s = i.next();
				Arc2D arc = new Arc2D.Double((float)(cx - rx), (float)(cy - ry), (float)(rx * 2), (float)(ry * 2), (float)s.getX(), (float)s.getY(), Arc2D.OPEN);
				p.append(arc, true);
				break;
			default:
				throw new RuntimeException("Unexpected segment type: " + s.getType());
			}
		}
		return p;
	}

	@Override
	public void drawText(WRectF rect, EnumSet<AlignmentFlag> flags, TextFlag textFlag, CharSequence text) {
		if (textFlag == TextFlag.TextWordWrap)
			throw new UnsupportedOperationException("drawText(): TextWordWrap not yet implemented");
		
		processChangeFlags();
		
		double px = 0, py = 0;

		AlignmentFlag horizontalAlign = EnumUtils.enumFromSet(EnumUtils.mask(flags, AlignmentFlag.AlignHorizontalMask));
		AlignmentFlag verticalAlign = EnumUtils.enumFromSet(EnumUtils.mask(flags, AlignmentFlag.AlignVerticalMask));

		String s = text.toString();
		
		switch (horizontalAlign) {
		case AlignLeft:
			px = rect.getLeft();
			break;
		case AlignRight:
			px = rect.getRight() - g2.getFontMetrics().stringWidth(s);
			break;
		case AlignCenter:
			px = rect.getCenter().getX() - g2.getFontMetrics().stringWidth(s)/2;
			break;
		}
		
		switch (verticalAlign) {
		case AlignBottom:
			py = rect.getBottom();
			break;
		case AlignTop:
			py = rect.getTop() + g2.getFontMetrics().getHeight();
			break;
		case AlignMiddle:
			py = rect.getCenter().getY() + g2.getFontMetrics().getHeight()/2;
		}
		
		py -= g2.getFontMetrics().getDescent();

		g2.setPaint(penPaint);
		g2.drawString(s, (float)px, (float)py);
	}

	
	public WLength getHeight() {
		return height;
	}
	

	public WPainter getPainter() {
		return painter;
	}

	
	public WLength getWidth() {
		return width;
	}

	
	public void init() {
		if (image != null)
			this.g2 = image.createGraphics();
		
		changeFlags.add(ChangeFlag.Pen);
		changeFlags.add(ChangeFlag.Brush);
		changeFlags.add(ChangeFlag.Font);
	}

	
	public boolean isPaintActive() {
		return painter != null;
	}

	
	public void setChanged(EnumSet<ChangeFlag> flags) {
		this.changeFlags.addAll(flags);
	}

	
	public void setChanged(ChangeFlag flag, ChangeFlag... flags) {
		setChanged(EnumSet.of(flag, flags));
	}

	public void setPainter(WPainter painter) {
		this.painter = painter;
	}

	private void drawShape(Shape shape) {
		processChangeFlags();

		if (painter.getPen().getStyle() != PenStyle.NoPen) {
			g2.setPaint(penPaint);
			g2.draw(shape);
		}

		if (painter.getBrush().getStyle() != BrushStyle.NoBrush) {
			g2.setPaint(brushPaint);
			g2.fill(shape);
		}
	}

	private void processChangeFlags() {
		boolean resetTransform = changeFlags.contains(ChangeFlag.Transform);

		if (changeFlags.contains(ChangeFlag.Clipping)) {
			setTransform(painter.getClipPathTransform());
			if (painter.getClipPath().isEmpty())
				g2.setClip(null);
			else
				g2.setClip(createShape(painter.getClipPath()));
			resetTransform = true;
		}

		if (resetTransform)
			setTransform(painter.getCombinedTransform());

		if (changeFlags.contains(ChangeFlag.Pen)) {
			g2.setStroke(createStroke(painter, painter.getPen()));
			penPaint = createColor(painter.getPen().getColor());
		}

		if (changeFlags.contains(ChangeFlag.Brush))
			brushPaint = createColor(painter.getBrush().getColor());
		
		if (changeFlags.contains(ChangeFlag.Font))
			g2.setFont(createFont(painter.getFont()));
		
		if (changeFlags.contains(ChangeFlag.Hints)) {
			if ((painter.getRenderHints() & WPainter.RenderHint.Antialiasing.getValue()) != 0)
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			else
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		}

		changeFlags.clear();
	}

	/** Converts a jwt.WColor to an awt.Color
	 *
	 * @param color the JWt color
	 * @return the corresponding AWT color
	 */
	public static Color createColor(WColor color) {
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
	}

	/** converts a jwt.WPen to an awt.Stroke
	 * 
	 * @param painter the painter used to take into account transformations for the pen width
	 * @param pen the JWt pen
	 * @return the corresponding AWT Stroke
	 */
	public static Stroke createStroke(WPainter painter, WPen pen) {
		int cap = 0;
		switch (pen.getCapStyle()) {
		case FlatCap:   cap = BasicStroke.CAP_BUTT; break;
		case RoundCap:  cap = BasicStroke.CAP_ROUND; break;
		case SquareCap: cap = BasicStroke.CAP_SQUARE; break;
		}

		int join = 0;
		switch (pen.getJoinStyle()) {
		case BevelJoin: join = BasicStroke.JOIN_BEVEL; break;
		case MiterJoin: join = BasicStroke.JOIN_MITER; break;
		case RoundJoin: join = BasicStroke.JOIN_ROUND; break;
		}

		float width = 0;
		if (pen.getStyle() != PenStyle.NoPen)
			width = (float) painter.normalizedPenWidth(pen.getWidth(), true).toPixels();

		switch (pen.getStyle()) {
		case DashLine:
		{
			float[] dash = {4, 2};
			return new BasicStroke(width, cap, join, MITER_LIMIT, dash, 0);
		}
		case DotLine:
		{
			float[] dash = {1, 2};
			return new BasicStroke(width, cap, join, MITER_LIMIT, dash, 0);
		}
		case DashDotLine:
		{
			float[] dash = {4, 2, 1, 2};
			return new BasicStroke(width, cap, join, MITER_LIMIT, dash, 0);
		}
		case DashDotDotLine:
		{
			float[] dash = {4, 2, 1, 2, 1, 2};
			return new BasicStroke(width, cap, join, MITER_LIMIT, dash, 0);
		}
		default:
			return new BasicStroke(width, cap, join);
		}
	}
	
	private Font createFont(WFont font) {
		String name = "";
		
		switch (font.getGenericFamily()) {
		case Default: break;
		case Cursive: break; // ??
		case Monospace: name = "Monospaced"; break;
		case Serif:     name = "Serif";      break;
		case SansSerif: name = "SansSerif"; break;
		}
		
		if (font.getSpecificFamilies().length() != 0) {
			if (name.length() != 0)
				name += " ";
			name += font.getSpecificFamilies().toString();
		}
	
		int style = Font.PLAIN;
		switch (font.getStyle()) {
		case Italic: style = Font.ITALIC;  break;
		case NormalStyle:                  break;
		case Oblique: style = Font.ITALIC; break; // ??
		}
		
		switch (font.getWeight()) {
		case NormalWeight: break;
		case Bolder:
		case Bold: style |= Font.BOLD; break;
		}
	
		int size = (int) (font.getSizeLength(16).toPixels() / 96.0 * 72.0);
		
		return new Font(name, style, size);
	}

	private void setTransform(WTransform t) {
		g2.setTransform(new AffineTransform(t.getM11(), t.getM12(), t.getM21(), t.getM22(), t.getM31(), t.getM32()));
	}
	
	/**
	 * Clears the image (resets the background to solid white).
	 */
	public void clear() {
		if (g2 != null) {
			g2.setBackground(new Color(255, 255, 255, 0));
			g2.clearRect(0, 0, (int)width.getValue(), (int)height.getValue());
		}
	}


	@Override
	public WFontMetrics getFontMetrics() {
		throw new UnsupportedOperationException("WFontMetrics.getFontMetrics() not yet supported");
	}


	@Override
	public WTextItem measureText(CharSequence text, double maxWidth, boolean wordWrap) {
		throw new UnsupportedOperationException("WFontMetrics.measureText() not yet supported");
	}


	@Override
	public WTextItem measureText(CharSequence text) {
		throw new UnsupportedOperationException("WFontMetrics.measureText() not yet supported");
	}


	@Override
	public WTextItem measureText(CharSequence text, double maxWidth) {
		throw new UnsupportedOperationException("WFontMetrics.measureText() not yet supported");
	}

	@Override
	public EnumSet<FeatureFlag> getFeatures() {
		// Later we can implement font metrics.
		return EnumSet.noneOf(FeatureFlag.class);
	}

}
