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
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
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
	enum Format { PngFormat }

	private Format format;
	private WLength width;
	private WLength height;

	private WPainter painter;
	private EnumSet<ChangeFlag> changeFlags;
	private EnumSet<PaintFlag> paintFlags;

	private BufferedImage image;
	private Graphics2D g2;
	private Paint penPaint, brushPaint;
	private boolean fontChanged;

	public WRasterPaintDevice(Format format, WLength width, WLength height) {
		this.width = width;
		this.height = height;
		this.format = format;
		this.changeFlags = EnumSet.noneOf(ChangeFlag.class);
		this.paintFlags = EnumSet.noneOf(PaintFlag.class);
	}

	@Override
	protected void handleRequest(WebRequest request, WebResponse response) throws IOException {
		response.setContentType("image/png");
		if (image != null)
			ImageIO.write(image, "png", response.getOutputStream());
	}

	@Override
	public void done() {
	}

	@Override
	public void drawArc(WRectF rect, double startAngle, double spanAngle) {
		drawShape(new Arc2D.Double(rect.getLeft(), rect.getTop(), rect.getWidth(), rect.getHeight(),
			startAngle, spanAngle, Arc2D.OPEN));
	}

	@Override
	public void drawImage(WRectF rect, String imageUri, int imgWidth, int imgHeight, WRectF sourceRect) {
		// TODO Auto-generated method stub
	}

	@Override
	public void drawLine(double x1, double y1, double x2, double y2) {
		drawShape(new Line2D.Double(x1, y1, x2, y2));
	}

	@Override
	public void drawPath(WPainterPath path) {
		drawShape(createPath2D(path));
	}

	private static Path2D createPath2D(WPainterPath path) {
		Path2D p = new Path2D.Double();

		for (Iterator<Segment> i = path.getSegments().iterator(); i.hasNext();) {
			Segment s = i.next();
			switch (s.getType()) {
			case MoveTo:
				p.moveTo(s.getX(), s.getY());
				break;
			case LineTo:
				p.lineTo(s.getX(), s.getY());
				break;
			case QuadC:
				double x1 = s.getX();
				double y1 = s.getY();
				s = i.next();
				p.quadTo(x1, y1, s.getX(), s.getY());
				break;
			case CubicC1:
				x1 = s.getX();
				y1 = s.getY();
				s = i.next();
				double x2 = s.getX();
				double y2 = s.getY();
				s = i.next();
				p.curveTo(x1, y1, x2, y2, s.getX(), s.getY());
				break;
			case ArcC:
				double cx = s.getX();
				double cy = s.getY();
				s = i.next();
				double rx = s.getX();
				double ry = s.getY();
				s = i.next();
				Arc2D arc = new Arc2D.Double(cx - rx, cy - ry, rx * 2, ry * 2, s.getX(), s.getY(), Arc2D.OPEN);
				p.append(arc, true);
				break;
			default:
				throw new RuntimeException("Unexpected segment type: " + s.getType());
			}
		}
		return p;
	}

	@Override
	public void drawText(WRectF rect, EnumSet<AlignmentFlag> flags, CharSequence text) {
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

	@Override
	public WLength getHeight() {
		return height;
	}

	@Override
	public EnumSet<PaintFlag> getPaintFlags() {
		return paintFlags;
	}

	@Override
	public WPainter getPainter() {
		return painter;
	}

	@Override
	public WLength getWidth() {
		return width;
	}

	@Override
	public void init() {
		this.image = new BufferedImage((int)width.toPixels(), (int)height.toPixels(), BufferedImage.TYPE_INT_ARGB);
		this.g2 = image.createGraphics();
		
		changeFlags.add(ChangeFlag.Pen);
		changeFlags.add(ChangeFlag.Brush);
		changeFlags.add(ChangeFlag.Font);
	}

	@Override
	public boolean isPaintActive() {
		return painter != null;
	}

	@Override
	public void setChanged(EnumSet<ChangeFlag> flags) {
		this.changeFlags.addAll(flags);
	}

	@Override
	public void setChanged(ChangeFlag flag, ChangeFlag... flags) {
		setChanged(EnumSet.of(flag, flags));
	}

	@Override
	public void setPaintFlags(EnumSet<PaintFlag> paintFlags) {
		this.paintFlags = paintFlags; // ??
	}

	@Override
	public final void setPaintFlags(PaintFlag paintFlag, PaintFlag... paintFlags) {
		setPaintFlags(EnumSet.of(paintFlag, paintFlags));
	}

	@Override
	public void setPainter(WPainter painter) {
		this.painter = painter;
	}

	private void drawShape(Shape shape) {
		processChangeFlags();

		if (painter.getPen().getStyle() != PenStyle.NoPen) {
			g2.setPaint(penPaint);
			g2.draw(shape);
		}

		if (painter.getBrush().getStyle() != WBrushStyle.NoBrush) {
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
				g2.setClip(createPath2D(painter.getClipPath()));
			resetTransform = true;
		}

		if (resetTransform)
			setTransform(painter.getCombinedTransform());

		if (changeFlags.contains(ChangeFlag.Pen)) {
			g2.setStroke(createStroke(painter.getPen()));
			penPaint = createPaint(painter.getPen().getColor());
		}

		if (changeFlags.contains(ChangeFlag.Brush))
			brushPaint = createPaint(painter.getBrush().getColor());
		
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

	private static Paint createPaint(WColor color) {
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
	}

	private Stroke createStroke(WPen pen) {
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

		float width = (float) painter.normalizedPenWidth(pen.getWidth(), true).toPixels();
		return new BasicStroke(width, cap, join);
	}
	
	private Font createFont(WFont font) {
		String name = "";
		
		switch (font.getGenericFamily()) {
		case Default: break;
		case Cursive: break; // ??
		case Monospace: name = Font.MONOSPACED; break;
		case Serif:     name = Font.SERIF;      break;
		case SansSerif: name = Font.SANS_SERIF; break;
		}
		
		if (!font.getSpecificFamilies().isEmpty()) {
			if (!name.isEmpty())
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
		
		int size = 12;
		switch (font.getSize()) {
		case FixedSize:
			// Java assumes 72dpi, while on the web we have 96dpi, this cancels the pixel -> point calculation
			size = (int) (font.getFixedSize().toPixels());
			break;
		}
		
		return new Font(name, style, size);
	}

	private void setTransform(WTransform t) {
		g2.setTransform(new AffineTransform(t.getM11(), t.getM12(), t.getM21(), t.getM22(), t.getM31(), t.getM32()));
	}
}
