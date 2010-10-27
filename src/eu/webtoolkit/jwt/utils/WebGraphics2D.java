package eu.webtoolkit.jwt.utils;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.RenderingHints.Key;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.LineMetrics;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.text.CharacterIterator;
import java.util.EnumSet;
import java.util.Map;

import eu.webtoolkit.jwt.AlignmentFlag;
import eu.webtoolkit.jwt.PenCapStyle;
import eu.webtoolkit.jwt.PenJoinStyle;
import eu.webtoolkit.jwt.PenStyle;
import eu.webtoolkit.jwt.WBrush;
import eu.webtoolkit.jwt.WBrushStyle;
import eu.webtoolkit.jwt.WColor;
import eu.webtoolkit.jwt.WFont;
import eu.webtoolkit.jwt.WLength;
import eu.webtoolkit.jwt.WPainter;
import eu.webtoolkit.jwt.WPainterPath;
import eu.webtoolkit.jwt.WPen;
import eu.webtoolkit.jwt.WPointF;
import eu.webtoolkit.jwt.WRasterPaintDevice;
import eu.webtoolkit.jwt.WRectF;
import eu.webtoolkit.jwt.WTransform;
import eu.webtoolkit.jwt.WFont.GenericFamily;
import eu.webtoolkit.jwt.WFont.Size;
import eu.webtoolkit.jwt.WFont.Style;
import eu.webtoolkit.jwt.WFont.Weight;
import eu.webtoolkit.jwt.WLength.Unit;
import eu.webtoolkit.jwt.WPainter.RenderHint;

/**
 * An implementation of Graphics2D which uses a WPainter.
 * 
 * This is useful for moving existing Swing/AWT painting code into a WPaintedWidget.
 * 
 * @author koen
 */
public class WebGraphics2D extends Graphics2D {

	private WPainter painter;
	private PenStyle currentPenStyle;
	private Font font;
	private BufferedImage fontImage = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
	private Graphics2D fontGraphics = fontImage.createGraphics();
	private RenderingHints renderingHints;
	protected String lastStringMeasured;
	protected int lastStringWidth;

	/**
	 * Creates a Graphics2D context which paints on the given painter.
	 * 
	 * @param painter the painter to paint on.
	 */
	public WebGraphics2D(WPainter painter) {
		this.painter = painter;
		this.currentPenStyle = painter.getPen().getStyle();
		this.font = new Font("SansSerif", Font.PLAIN, 12);
		this.renderingHints = new RenderingHints(null);
		
		fontGraphics.setFont(font);
	}

	@Override
	public void addRenderingHints(Map<?, ?> v) {
		renderingHints.putAll(v);
		setRenderingHints(renderingHints);
	}

	@Override
	public void clip(Shape shape) {
		if (getClip() == null)
			setClip(shape);
		else
			throw new RuntimeException("JWtGraphics2D.clip() cannot create composite clip");
	}

	@Override
	public void draw(Shape shape) {
		painter.strokePath(toPath(shape), painter.getPen());
	}

	@Override
	public void drawGlyphVector(GlyphVector arg0, float arg1, float arg2) {
		throw new RuntimeException("JWtGraphics2D.drawGlypVector() not supported");
	}

	@Override
	public boolean drawImage(Image arg0, AffineTransform arg1, ImageObserver arg2) {
		// We should serialize the image to a PNG and draw that...
		throw new RuntimeException("JWtGraphics2D.drawImage() not yet implemented");
	}

	@Override
	public void drawImage(BufferedImage arg0, BufferedImageOp arg1, int arg2, int arg3) {
		// We should serialize the image to a PNG and draw that...
		throw new RuntimeException("JWtGraphics2D.drawImage() not yet implemented");
	}

	@Override
	public void drawRenderableImage(RenderableImage arg0, AffineTransform arg1) {
		// We should serialize the image to a PNG and draw that...
		throw new RuntimeException("JWtGraphics2D.drawImage() not yet implemented");
	}

	@Override
	public void drawRenderedImage(RenderedImage arg0, AffineTransform arg1) {
		// We should serialize the image to a PNG and draw that...
		throw new RuntimeException("JWtGraphics2D.drawImage() not yet implemented");
	}

	@Override
	public void drawString(String s, int x, int y) {
		drawInternalString(s, x, y);
	}

	private void drawInternalString(String s, float x, float y) {
		if (s == lastStringMeasured) {
			EnumSet<AlignmentFlag> flags = EnumSet.of(AlignmentFlag.AlignRight, AlignmentFlag.AlignTop);
			painter.drawText(x + lastStringWidth - 1000, y - getFontMetrics().getAscent(), 1000, 1000, flags, s);
		} else {
			EnumSet<AlignmentFlag> flags = EnumSet.of(AlignmentFlag.AlignLeft, AlignmentFlag.AlignTop);
			painter.drawText(x, y - getFontMetrics().getAscent(), 1000, 1000, flags, s);			
		}
	}

	@Override
	public void drawString(String s, float x, float y) {
		drawInternalString(s, x, y);
	}

	@Override
	public void drawString(AttributedCharacterIterator s, int x, int y) {
		throw new RuntimeException("JWtGraphics2D.drawString() not yet implemented");
	}

	@Override
	public void drawString(AttributedCharacterIterator arg0, float arg1, float arg2) {
		throw new RuntimeException("JWtGraphics2D.drawString() not yet implemented");
	}

	@Override
	public void fill(Shape shape) {
		painter.fillPath(toPath(shape), painter.getBrush());
	}

	@Override
	public Color getBackground() {
		throw new RuntimeException("JWtGraphics2D.getBackground() not yet implemented");
	}

	@Override
	public Composite getComposite() {
		throw new RuntimeException("JWtGraphics2D.getComposite() not supported");
	}

	@Override
	public GraphicsConfiguration getDeviceConfiguration() {
		throw new RuntimeException("JWtGraphics2D.getDeviceConfiguration() not yet implemented");
	}

	@Override
	public FontRenderContext getFontRenderContext() {
		return fontGraphics.getFontRenderContext();
	}

	@Override
	public Paint getPaint() {
		return getColor();
	}

	@Override
	public Object getRenderingHint(Key arg0) {
		throw new RuntimeException("JWtGraphics2D.getRenderingHint() not yet implemented");
	}

	@Override
	public RenderingHints getRenderingHints() {
		return renderingHints;
	}

	@Override
	public Stroke getStroke() {
		return WRasterPaintDevice.createStroke(painter, painter.getPen());
	}

	@Override
	public AffineTransform getTransform() {
		return toAffineTransform(painter.getWorldTransform());
	}

	private AffineTransform toAffineTransform(WTransform t) {
		return new AffineTransform(t.getM11(), t.getM12(), t.getM21(), t.getM22(), t.getM31(), t.getM32());
	}

	@Override
	public boolean hit(Rectangle rect, Shape s, boolean onStroke) {
		throw new RuntimeException("JWtGraphics2D.getRenderingHints() not supported");
	}

	@Override
	public void rotate(double angle) {
		painter.rotate(degreesToRadians(angle));
	}

	private double degreesToRadians(double angle) {
		return angle / 180.0 * Math.PI;
	}

	@Override
	public void rotate(double theta, double x, double y) {
		translate(x, y);
		rotate(theta);
		translate(-x, -y);
	}

	@Override
	public void scale(double sx, double sy) {
		painter.scale(sx, sy);
	}

	@Override
	public void setBackground(Color color) {
		throw new RuntimeException("JWtGraphics2D.setBackground() not yet implemented");
	}

	@Override
	public void setComposite(Composite arg0) {
		throw new RuntimeException("JWtGraphics2D.setComposited() not supported");
	}

	@Override
	public void setPaint(Paint paint) {
		if (paint == null || paint instanceof Color) {
			setColor((Color) paint);
		} else
			throw new RuntimeException("JWtGraphics2D.setPaint() only supports plain Color paints.");
	}

	private WColor toWColor(Color color) {
		return new WColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
	}

	@Override
	public void setRenderingHint(Key key, Object value) {
		this.renderingHints.put(key, value);
		if (key == RenderingHints.KEY_ANTIALIASING)
			painter.setRenderHint(RenderHint.Antialiasing, value == RenderingHints.VALUE_ANTIALIAS_ON);
	}

	@Override
	public void setRenderingHints(Map<?, ?> hints) {
		this.renderingHints = new RenderingHints(null);
		for (Map.Entry<?, ?> f : hints.entrySet())
			setRenderingHint((Key) f.getKey(), f.getValue());
	}

	@Override
	public void setStroke(Stroke stroke) {
		BasicStroke basicStroke = (BasicStroke) stroke;

		PenCapStyle capStyle = PenCapStyle.FlatCap;
		switch (basicStroke.getEndCap()) {
		case BasicStroke.CAP_BUTT:		capStyle = PenCapStyle.FlatCap; break;
		case BasicStroke.CAP_ROUND:		capStyle = PenCapStyle.RoundCap; break;
		case BasicStroke.CAP_SQUARE:	capStyle = PenCapStyle.SquareCap; break;
		}
		
		PenJoinStyle joinStyle = PenJoinStyle.BevelJoin;
		switch (basicStroke.getLineJoin()) {
		case BasicStroke.JOIN_BEVEL:	joinStyle = PenJoinStyle.BevelJoin; break;
		case BasicStroke.JOIN_MITER:	joinStyle = PenJoinStyle.MiterJoin; break;
		case BasicStroke.JOIN_ROUND:	joinStyle = PenJoinStyle.RoundJoin; break;
		}

		WPen pen = painter.getPen().clone();
		pen.setCapStyle(capStyle);
		pen.setJoinStyle(joinStyle);

		currentPenStyle = PenStyle.NoPen;
		if (basicStroke.getLineWidth() > 0) {
			float[] dashArray = basicStroke.getDashArray();
			if (dashArray != null) {
				if (dashArray.length < 2)
					currentPenStyle = PenStyle.SolidLine;
				else if (dashArray.length <= 3) {
					if (dashArray[0] >= dashArray[1])
						currentPenStyle = PenStyle.DashLine;
					else
						currentPenStyle = PenStyle.DotLine;
				} else if (dashArray.length <= 5)
					currentPenStyle = PenStyle.DashDotLine;
				else
					currentPenStyle = PenStyle.DashDotDotLine;
			} else
				currentPenStyle = PenStyle.SolidLine;
			
			WTransform t = painter.getCombinedTransform();
			float width = basicStroke.getLineWidth();
			if (!t.isIdentity()) {
				WTransform.TRSRDecomposition d = new WTransform.TRSRDecomposition();
				t.decomposeTranslateRotateScaleRotate(d);
				width /= (Math.abs(d.sx) + Math.abs(d.sy)) / 2.0;
			}
			pen.setWidth(new WLength(width));
		}
		
		pen.setStyle(currentPenStyle);
		painter.setPen(pen);
	}

	@Override
	public void setTransform(AffineTransform matrix) {
		painter.setWorldTransform(toWTransform(matrix));
	}

	private WTransform toWTransform(AffineTransform matrix) {
		double[] m = new double[6];
		matrix.getMatrix(m);
		return new WTransform(m[0], m[1], m[2], m[3], m[4], m[5]);
	}

	@Override
	public void shear(double sh, double sv) {
		painter.setWorldTransform(painter.getWorldTransform().shear(sh, sv));
	}

	@Override
	public void transform(AffineTransform matrix) {
		painter.setWorldTransform(toWTransform(matrix), true);
	}

	@Override
	public void translate(int x, int y) {
		painter.translate(x, y);
	}

	@Override
	public void translate(double x, double y) {
		painter.translate(x, y);
	}

	@Override
	public void clearRect(int x, int y, int width, int height) {
		throw new RuntimeException("JWtGraphics2D.drawImage() not yet implemented");
	}

	@Override
	public void clipRect(int x, int y, int width, int height) {
		if (!painter.hasClipping()) {
			WPainterPath rect = new WPainterPath();
			rect.addRect(x, y, width, height);
			painter.setClipPath(rect);
		} else
			throw new RuntimeException("JWtGraphics2D.setClip() cannot create composite clip");
	}

	@Override
	public void copyArea(int x, int y, int width, int height, int dx, int dy) {
		throw new RuntimeException("JWtGraphics2D does not support that.");
	}

	@Override
	public Graphics create() {
		return null;
	}

	@Override
	public void dispose() {
		this.painter = null;
	}

	@Override
	public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
		WBrush oldBrush = painter.getBrush();
		painter.setBrush(new WBrush(WBrushStyle.NoBrush));
		painter.drawArc(x, y, width, height, startAngle * 16, arcAngle * 16);
		painter.setBrush(oldBrush);
	}

	@Override
	public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
		// We should serialize the image to a PNG and draw that...
		throw new RuntimeException("JWtGraphics2D.drawImage() not yet implemented");
	}

	@Override
	public boolean drawImage(Image img, int x, int y, Color bgcolor,ImageObserver observer) {
		// We should serialize the image to a PNG and draw that...
		throw new RuntimeException("JWtGraphics2D.drawImage() not yet implemented");
	}

	@Override
	public boolean drawImage(Image img, int x, int y, int width, int height, ImageObserver observer) {
		// We should serialize the image to a PNG and draw that...
		throw new RuntimeException("JWtGraphics2D.drawImage() not yet implemented");
	}

	@Override
	public boolean drawImage(Image img, int x, int y, int width, int height, Color bgcolor, ImageObserver observer) {
		// We should serialize the image to a PNG and draw that...
		throw new RuntimeException("JWtGraphics2D.drawImage() not yet implemented");
	}

	@Override
	public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, ImageObserver observer) {
		// We should serialize the image to a PNG and draw that...
		throw new RuntimeException("JWtGraphics2D.drawImage() not yet implemented");
	}

	@Override
	public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2,	int sx1, int sy1, int sx2, int sy2, Color bgcolor, ImageObserver observer) {
		// We should serialize the image to a PNG and draw that...
		throw new RuntimeException("JWtGraphics2D.drawImage() not yet implemented");
	}

	@Override
	public void drawLine(int x1, int y1, int x2, int y2) {
		painter.drawLine(x1, y1, x2, y2);
	}

	@Override
	public void drawOval(int x, int y, int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {
		WBrush oldBrush = painter.getBrush();
		painter.setBrush(new WBrush(WBrushStyle.NoBrush));
		WPointF points[] = toPoints(xPoints, yPoints, nPoints);
		painter.drawPolygon(points, nPoints);
		painter.setBrush(oldBrush);
	}

	private WPointF[] toPoints(int[] xPoints, int[] yPoints, int nPoints) {
		WPointF[] result = new WPointF[nPoints];
		for (int i = 0; i < nPoints; ++i)
			result[i] = new WPointF(xPoints[i], yPoints[i]);
		return result;
	}

	@Override
	public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) {
		WBrush oldBrush = painter.getBrush();
		painter.setBrush(new WBrush(WBrushStyle.NoBrush));
		WPointF points[] = toPoints(xPoints, yPoints, nPoints);
		painter.drawPolyline(points, nPoints);
		painter.setBrush(oldBrush);
	}

	@Override
	public void drawRoundRect(int x, int y, int width, int height,
			int arcWidth, int arcHeight) {
		// TODO Auto-generated method stub

	}

	@Override
	public void fillArc(int x, int y, int width, int height, int startAngle,
			int arcAngle) {
		WPen oldPen = painter.getPen();
		painter.setPen(new WPen(PenStyle.NoPen));
		painter.drawArc(x, y, width, height, startAngle * 16, arcAngle * 16);
		painter.setPen(oldPen);
	}

	@Override
	public void fillOval(int x, int y, int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
		WPen oldPen = painter.getPen();
		painter.setPen(new WPen(PenStyle.NoPen));
		WPointF points[] = toPoints(xPoints, yPoints, nPoints);
		painter.drawPolyline(points, nPoints);
		painter.setPen(oldPen);
	}

	@Override
	public void fillRect(int x, int y, int width, int height) {
		WPen oldPen = painter.getPen();
		painter.setPen(new WPen(PenStyle.NoPen));
		painter.drawRect(x, y, width, height);
		painter.setPen(oldPen);
	}

	@Override
	public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
		// TODO Auto-generated method stub
	}

	@Override
	public Shape getClip() {
		if (painter.hasClipping())
			return toShape(painter.getClipPath());
		else
			return null;
	}

	private Shape toShape(WPainterPath path) {
		return WRasterPaintDevice.createShape(path);
	}

	@Override
	public Rectangle getClipBounds() {
		if (painter.hasClipping())
			return toRectangle(painter.getClipPath().getControlPointRect());
		else
			return null;
	}

	private Rectangle toRectangle(WRectF rect) {
		return new Rectangle((int)rect.getX(), (int)rect.getY(), (int)rect.getWidth(), (int)rect.getHeight());
	}

	@Override
	public Color getColor() {
		return WRasterPaintDevice.createColor(painter.getPen().getColor());
	}

	@Override
	public Font getFont() {
		return font;
	}

	private FontMetrics webFontMetrics = null;
	
	class WebFontMetrics extends FontMetrics {
		private static final long serialVersionUID = 1L;
		FontMetrics m;

		WebFontMetrics(Font f) {
			super(f);
			m = fontGraphics.getFontMetrics(f);
		}
		
		@Override
		public int stringWidth(String str) {
			lastStringMeasured = str;
			lastStringWidth = m.stringWidth(str);
			return lastStringWidth;
		}

		@Override
		public int bytesWidth(byte[] data, int off, int len) {
			return m.bytesWidth(data, off, len);
		}

		@Override
		public int charsWidth(char[] data, int off, int len) {
			return m.charsWidth(data, off, len);
		}

		@Override
		public int charWidth(char ch) {
			return m.charWidth(ch);
		}

		@Override
		public int charWidth(int codePoint) {
			return m.charWidth(codePoint);
		}

		@Override
		public int getAscent() {
			return m.getAscent();
		}

		@Override
		public int getDescent() {
			return m.getDescent();
		}

		@Override
		public Font getFont() {
			return m.getFont();
		}

		@Override
		public FontRenderContext getFontRenderContext() {
			return m.getFontRenderContext();
		}

		@Override
		public int getHeight() {
			return m.getHeight();
		}

		@Override
		public int getLeading() {
			return m.getLeading();
		}

		@Override
		public LineMetrics getLineMetrics(char[] chars, int beginIndex, int limit, Graphics context) {
			return m.getLineMetrics(chars, beginIndex, limit, context);
		}

		@Override
		public LineMetrics getLineMetrics(CharacterIterator ci, int beginIndex, int limit, Graphics context) {
			return m.getLineMetrics(ci, beginIndex, limit, context);
		}

		@Override
		public LineMetrics getLineMetrics(String str, Graphics context) {
			return m.getLineMetrics(str, context);
		}

		@Override
		public LineMetrics getLineMetrics(String str, int beginIndex, int limit, Graphics context) {
			return m.getLineMetrics(str, beginIndex, limit, context);
		}

		@Override
		public int getMaxAdvance() {
			return m.getMaxAdvance();
		}

		@Override
		public int getMaxAscent() {
			return m.getMaxAscent();
		}

		@Override
		public Rectangle2D getMaxCharBounds(Graphics context) {
			return m.getMaxCharBounds(context);
		}

		@Override
		public int getMaxDescent() {
			return m.getMaxDescent();
		}

		@Override
		public Rectangle2D getStringBounds(char[] chars, int beginIndex, int limit, Graphics context) {
			return m.getStringBounds(chars, beginIndex, limit, context);
		}

		@Override
		public Rectangle2D getStringBounds(CharacterIterator ci, int beginIndex, int limit, Graphics context) {
			return m.getStringBounds(ci, beginIndex, limit, context);
		}

		@Override
		public Rectangle2D getStringBounds(String str, Graphics context) {
			lastStringMeasured = str;
			Rectangle2D bounds = m.getStringBounds(str, context);
			lastStringWidth = (int) bounds.getWidth();
			return bounds;
		}

		@Override
		public Rectangle2D getStringBounds(String str, int beginIndex, int limit, Graphics context) {
			return m.getStringBounds(str, beginIndex, limit, context);
		}

		@Override
		public int[] getWidths() {
			return m.getWidths();
		}

		@Override
		public boolean hasUniformLineMetrics() {
			return m.hasUniformLineMetrics();
		}

		@Override
		public String toString() {
			return m.toString();
		}
	};
	
	@Override
	public FontMetrics getFontMetrics(final Font f) {
		fontGraphics.setTransform(getTransform());
		if (webFontMetrics == null || !webFontMetrics.getFont().equals(f))
			webFontMetrics = new WebFontMetrics(f);
		return webFontMetrics;
	}

	@Override
	public void setClip(Shape clip) {
		if (clip != null) {
			painter.setClipPath(toPath(clip));
		} else
			painter.setClipping(false);
	}

	private WPainterPath toPath(Shape shape) {
		WPainterPath path = new WPainterPath();
		
		PathIterator it = shape.getPathIterator(null);
		double coords[] = new double[6];
		while (!it.isDone()) {
			int type = it.currentSegment(coords);
			
			switch (type) {
			case PathIterator.SEG_MOVETO:
				path.moveTo(round(coords[0]), round(coords[1]));
				break;
			case PathIterator.SEG_LINETO:
				path.lineTo(round(coords[0]), round(coords[1]));
				break;
			case PathIterator.SEG_QUADTO:
				path.quadTo(coords[0], coords[1], coords[2], coords[3]);
				break;
			case PathIterator.SEG_CUBICTO:
				path.cubicTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
				break;				
			case PathIterator.SEG_CLOSE:
				path.closeSubPath();
			}
			
			it.next();
		}
		
		return path;
	}

	@Override
	public void setClip(int x, int y, int width, int height) {
		setClip(new Rectangle(x, y, width, height));
	}

	@Override
	public void setColor(Color color) {
		if (color == null) {
			painter.setBrush(new WBrush());
			WPen pen = painter.getPen().clone();
			pen.setStyle(PenStyle.NoPen);
			painter.setPen(pen);
		} else {
			WColor c = toWColor(color);
			painter.setBrush(new WBrush(c));
			WPen pen = painter.getPen().clone();
			pen.setStyle(currentPenStyle);
			pen.setColor(c);
			painter.setPen(pen);
		}
	}

	@Override
	public void setFont(Font font) {
		if (font != null) {
			lastStringMeasured = null;

			this.font = font;
			fontGraphics.setFont(font);

			WFont f = new WFont();
			if (font.getFamily().equalsIgnoreCase("serif"))
				f.setFamily(GenericFamily.Serif);
			else if (font.getFamily().equalsIgnoreCase("sansserif"))
				f.setFamily(GenericFamily.SansSerif);
			else if (font.getFamily().equalsIgnoreCase("monospaced"))
				f.setFamily(GenericFamily.Monospace);

			if (font.isBold())
				f.setWeight(Weight.Bold);
			if (font.isItalic())
				f.setStyle(Style.Italic);

			f.setSize(Size.FixedSize, new WLength(font.getSize(), Unit.Point));

			painter.setFont(f);
		}
	}

	@Override
	public void setPaintMode() {
		// TODO Auto-generated method stub
	}

	@Override
	public void setXORMode(Color c1) {
		// TODO Auto-generated method stub

	}
	
    private double round(double f) {
    	return Math.round(f) + 0.5;
    }
}
