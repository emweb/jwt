/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.lang.ref.*;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.http.*;
import javax.servlet.*;
import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.utils.*;
import eu.webtoolkit.jwt.servlet.*;

/**
 * A paint device for rendering using Scalable Vector Graphics (SVG).
 * <p>
 * 
 * The WSvgImage is primarily used by {@link WPaintedWidget} to render to the
 * browser in Support Vector Graphics (SVG) format.
 */
public class WSvgImage extends WResource implements WVectorImage {
	/**
	 * Create an SVG paint device.
	 */
	public WSvgImage(WLength width, WLength height, WObject parent) {
		super(parent);
		this.width_ = width;
		this.height_ = height;
		this.painter_ = null;
		this.paintUpdate_ = true;
		this.changeFlags_ = EnumSet.noneOf(WPaintDevice.ChangeFlag.class);
		this.newGroup_ = true;
		this.newClipPath_ = false;
		this.busyWithPath_ = false;
		this.currentClipId_ = -1;
		this.currentTransform_ = new WTransform();
		this.currentBrush_ = new WBrush();
		this.currentFont_ = new WFont();
		this.currentPen_ = new WPen();
		this.currentShadow_ = new WShadow();
		this.currentShadowId_ = -1;
		this.nextShadowId_ = 0;
		this.pathTranslation_ = new WPointF();
		this.shapes_ = new StringWriter();
		this.fillStyle_ = "";
		this.strokeStyle_ = "";
		this.fontStyle_ = "";
	}

	/**
	 * Create an SVG paint device.
	 * <p>
	 * Calls {@link #WSvgImage(WLength width, WLength height, WObject parent)
	 * this(width, height, (WObject)null)}
	 */
	public WSvgImage(WLength width, WLength height) {
		this(width, height, (WObject) null);
	}

	public void setChanged(EnumSet<WPaintDevice.ChangeFlag> flags) {
		if (!flags.isEmpty()) {
			this.newGroup_ = true;
		}
		if (!EnumUtils.mask(flags, WPaintDevice.ChangeFlag.Clipping).isEmpty()) {
			this.newClipPath_ = true;
		}
		this.changeFlags_.addAll(flags);
	}

	public final void setChanged(WPaintDevice.ChangeFlag flag,
			WPaintDevice.ChangeFlag... flags) {
		setChanged(EnumSet.of(flag, flags));
	}

	public void drawArc(WRectF rect, double startAngle, double spanAngle) {
		char[] buf = new char[30];
		if (Math.abs(spanAngle - 360.0) < 0.01) {
			this.finishPath();
			this.makeNewGroup();
			this.shapes_.append("<ellipse ").append(" cx=\"").append(
					MathUtils.round(rect.getCenter().getX(), 3));
			this.shapes_.append("\" cy=\"").append(
					MathUtils.round(rect.getCenter().getY(), 3));
			this.shapes_.append("\" rx=\"").append(
					MathUtils.round(rect.getWidth() / 2, 3));
			this.shapes_.append("\" ry=\"").append(
					MathUtils.round(rect.getHeight() / 2, 3)).append("\" />");
		} else {
			WPainterPath path = new WPainterPath();
			path.arcMoveTo(rect.getX(), rect.getY(), rect.getWidth(), rect
					.getHeight(), startAngle);
			path.arcTo(rect.getX(), rect.getY(), rect.getWidth(), rect
					.getHeight(), startAngle, spanAngle);
			this.drawPath(path);
		}
	}

	public void drawImage(WRectF rect, String imageUri, int imgWidth,
			int imgHeight, WRectF srect) {
		this.finishPath();
		this.makeNewGroup();
		WRectF drect = rect;
		char[] buf = new char[30];
		boolean transformed = false;
		if (drect.getWidth() != srect.getWidth()
				|| drect.getHeight() != srect.getHeight()) {
			this.shapes_.append("<g transform=\"matrix(").append(
					MathUtils.round(drect.getWidth() / srect.getWidth(), 3));
			this.shapes_.append(" 0 0 ").append(
					MathUtils.round(drect.getHeight() / srect.getHeight(), 3));
			this.shapes_.append(' ').append(MathUtils.round(drect.getX(), 3));
			this.shapes_.append(' ').append(MathUtils.round(drect.getY(), 3))
					.append(")\">");
			drect.assign(new WRectF(0, 0, srect.getWidth(), srect.getHeight()));
			transformed = true;
		}
		double scaleX = drect.getWidth() / srect.getWidth();
		double scaleY = drect.getHeight() / srect.getHeight();
		double x = drect.getX() - srect.getX() * scaleX;
		double y = drect.getY() - srect.getY() * scaleY;
		double width = imgWidth;
		double height = imgHeight;
		boolean useClipPath = false;
		int imgClipId = nextClipId_++;
		if (!new WRectF(x, y, width, height).equals(drect)) {
			this.shapes_.append("<clipPath id=\"imgClip").append(
					String.valueOf(imgClipId)).append("\">");
			this.shapes_.append("<rect x=\"").append(
					MathUtils.round(drect.getX(), 3)).append('"');
			this.shapes_.append(" y=\"").append(
					MathUtils.round(drect.getY(), 3)).append('"');
			this.shapes_.append(" width=\"").append(
					MathUtils.round(drect.getWidth(), 3)).append('"');
			this.shapes_.append(" height=\"").append(
					MathUtils.round(drect.getHeight(), 3)).append('"');
			this.shapes_.append(" /></clipPath>");
			useClipPath = true;
		}
		this.shapes_.append("<image xlink:href=\"").append(imageUri).append(
				"\"");
		this.shapes_.append(" x=\"").append(MathUtils.round(x, 3)).append('"');
		this.shapes_.append(" y=\"").append(MathUtils.round(y, 3)).append('"');
		this.shapes_.append(" width=\"").append(MathUtils.round(width, 3))
				.append('"');
		this.shapes_.append(" height=\"").append(MathUtils.round(height, 3))
				.append('"');
		if (useClipPath) {
			this.shapes_.append(" clip-path=\"url(#imgClip").append(
					String.valueOf(imgClipId)).append(")\"");
		}
		this.shapes_.append("/>");
		if (transformed) {
			this.shapes_.append("</g>");
		}
	}

	public void drawLine(double x1, double y1, double x2, double y2) {
		WPainterPath path = new WPainterPath();
		path.moveTo(x1, y1);
		path.lineTo(x2, y2);
		this.drawPath(path);
	}

	public void drawPath(WPainterPath path) {
		this.makeNewGroup();
		this.drawPlainPath(this.shapes_, path);
	}

	public void drawText(WRectF rect, EnumSet<AlignmentFlag> flags,
			CharSequence text) {
		this.finishPath();
		this.makeNewGroup();
		char[] buf = new char[30];
		this.shapes_.append("<text");
		this.shapes_.append(" style=\"stroke:none;");
		if (!this.getPainter().getPen().getColor().equals(
				this.getPainter().getBrush().getColor())
				|| this.getPainter().getBrush().getStyle() == BrushStyle.NoBrush) {
			WColor color = this.getPainter().getPen().getColor();
			this.shapes_.append("fill:" + color.getCssText()).append(';');
			if (color.getAlpha() != 255) {
				this.shapes_.append("fill-opacity:").append(
						MathUtils.round(color.getAlpha() / 255., 3))
						.append(';');
			}
		}
		this.shapes_.append('"');
		AlignmentFlag horizontalAlign = EnumUtils.enumFromSet(EnumUtils.mask(
				flags, AlignmentFlag.AlignHorizontalMask));
		AlignmentFlag verticalAlign = EnumUtils.enumFromSet(EnumUtils.mask(
				flags, AlignmentFlag.AlignVerticalMask));
		switch (horizontalAlign) {
		case AlignLeft:
			this.shapes_.append(" x=").append(quote(rect.getLeft()));
			break;
		case AlignRight:
			this.shapes_.append(" x=").append(quote(rect.getRight())).append(
					" text-anchor=\"end\"");
			break;
		case AlignCenter:
			this.shapes_.append(" x=").append(quote(rect.getCenter().getX()))
					.append(" text-anchor=\"middle\"");
			break;
		default:
			break;
		}
		double fontSize;
		switch (this.getPainter().getFont().getSize()) {
		case FixedSize:
			fontSize = this.getPainter().getFont().getFixedSize().toPixels();
			break;
		default:
			fontSize = 16;
		}
		double y = rect.getCenter().getY();
		switch (verticalAlign) {
		case AlignTop:
			y = rect.getTop() + fontSize * 0.75;
			break;
		case AlignMiddle:
			y = rect.getCenter().getY() + fontSize * 0.25;
			break;
		case AlignBottom:
			y = rect.getBottom() - fontSize * 0.25;
			break;
		default:
			break;
		}
		this.shapes_.append(" y=").append(quote(y));
		this.shapes_.append(">").append(
				WWebWidget.escapeText(text, false).toString())
				.append("</text>");
	}

	public WTextItem measureText(CharSequence text, double maxWidth,
			boolean wordWrap) {
		throw new WtLogicError("WSvgImage::measureText() not supported");
	}

	public final WTextItem measureText(CharSequence text) {
		return measureText(text, -1, false);
	}

	public final WTextItem measureText(CharSequence text, double maxWidth) {
		return measureText(text, maxWidth, false);
	}

	public WFontMetrics getFontMetrics() {
		throw new WtLogicError("WSvgImage::fontMetrics() not (yet?) supported");
	}

	public void init() {
		this.currentBrush_ = this.getPainter().getBrush();
		this.currentPen_ = this.getPainter().getPen();
		this.currentFont_ = this.getPainter().getFont();
		this.strokeStyle_ = this.getStrokeStyle();
		this.fillStyle_ = this.getFillStyle();
		this.fontStyle_ = this.getFontStyle();
		this.newClipPath_ = true;
	}

	public void done() {
		this.finishPath();
	}

	public boolean isPaintActive() {
		return this.painter_ != null;
	}

	public String getRendered() {
		try {
			StringWriter s = new StringWriter();
			this.streamResourceData(s);
			return s.toString();
		} catch (IOException ioe) {
			ioe.printStackTrace();
			return null;
		}
	}

	public WLength getWidth() {
		return this.width_;
	}

	public WLength getHeight() {
		return this.height_;
	}

	public void handleRequest(WebRequest request, WebResponse response)
			throws IOException {
		response.setContentType("image/svg+xml");
		Writer o = response.out();
		this.streamResourceData(o);
	}

	public WPainter getPainter() {
		return this.painter_;
	}

	public void setPainter(WPainter painter) {
		this.painter_ = painter;
	}

	public void clear() {
		this.paintUpdate_ = false;
		this.shapes_ = new StringWriter();
	}

	private WLength width_;
	private WLength height_;
	private WPainter painter_;
	private boolean paintUpdate_;
	private EnumSet<WPaintDevice.ChangeFlag> changeFlags_;
	private boolean newGroup_;
	private boolean newClipPath_;
	private boolean busyWithPath_;
	private int currentClipId_;
	private WTransform currentTransform_;
	private WBrush currentBrush_;
	private WFont currentFont_;
	private WPen currentPen_;
	private WShadow currentShadow_;
	private int currentShadowId_;
	private int nextShadowId_;
	private WPointF pathTranslation_;
	private StringWriter shapes_;

	private void finishPath() {
		if (this.busyWithPath_) {
			this.busyWithPath_ = false;
			this.shapes_.append("\" />");
		}
	}

	private void makeNewGroup() {
		if (!this.newGroup_) {
			return;
		}
		boolean brushChanged = !EnumUtils.mask(this.changeFlags_,
				WPaintDevice.ChangeFlag.Brush).isEmpty()
				&& !this.currentBrush_.equals(this.getPainter().getBrush());
		boolean penChanged = !EnumUtils.mask(this.changeFlags_,
				WPaintDevice.ChangeFlag.Hints).isEmpty()
				|| !EnumUtils.mask(this.changeFlags_,
						WPaintDevice.ChangeFlag.Pen).isEmpty()
				&& !this.currentPen_.equals(this.getPainter().getPen());
		boolean fontChanged = !EnumUtils.mask(this.changeFlags_,
				WPaintDevice.ChangeFlag.Font).isEmpty()
				&& !this.currentFont_.equals(this.getPainter().getFont());
		boolean shadowChanged = false;
		if (!EnumUtils.mask(this.changeFlags_, WPaintDevice.ChangeFlag.Shadow)
				.isEmpty()) {
			if (this.currentShadowId_ == -1) {
				shadowChanged = !this.getPainter().getShadow().isNone();
			} else {
				shadowChanged = !this.currentShadow_.equals(this.getPainter()
						.getShadow());
			}
		}
		if (shadowChanged) {
			this.newClipPath_ = true;
		}
		if (!this.newClipPath_) {
			if (!brushChanged && !penChanged) {
				WTransform f = this.getPainter().getCombinedTransform();
				if (this.busyWithPath_) {
					if (fequal(f.getM11(), this.currentTransform_.getM11())
							&& fequal(f.getM12(), this.currentTransform_
									.getM12())
							&& fequal(f.getM21(), this.currentTransform_
									.getM21())
							&& fequal(f.getM22(), this.currentTransform_
									.getM22())) {
						double det = f.getM11() * f.getM22() - f.getM12()
								* f.getM21();
						double a11 = f.getM22() / det;
						double a12 = -f.getM12() / det;
						double a21 = -f.getM21() / det;
						double a22 = f.getM11() / det;
						double fdx = f.getDx() * a11 + f.getDy() * a21;
						double fdy = f.getDx() * a12 + f.getDy() * a22;
						WTransform g = this.currentTransform_;
						double gdx = g.getDx() * a11 + g.getDy() * a21;
						double gdy = g.getDx() * a12 + g.getDy() * a22;
						double dx = fdx - gdx;
						double dy = fdy - gdy;
						this.pathTranslation_.setX(dx);
						this.pathTranslation_.setY(dy);
						this.changeFlags_.clear();
						return;
					}
				} else {
					if (!fontChanged && this.currentTransform_.equals(f)) {
						this.newGroup_ = false;
						this.changeFlags_.clear();
						return;
					}
				}
			}
		}
		this.newGroup_ = false;
		this.finishPath();
		char[] buf = new char[30];
		StringBuilder tmp = new StringBuilder();
		tmp.append("</g>");
		this.currentTransform_.assign(this.getPainter().getCombinedTransform());
		if (this.newClipPath_) {
			tmp.append("</g>");
			if (this.getPainter().hasClipping()) {
				this.currentClipId_ = nextClipId_++;
				tmp.append("<defs><clipPath id=\"clip").append(
						this.currentClipId_).append("\">");
				this.shapes_.append(tmp.toString());
				tmp.setLength(0);
				this.drawPlainPath(this.shapes_, this.getPainter()
						.getClipPath());
				tmp.append('"');
				this.busyWithPath_ = false;
				WTransform t = this.getPainter().getClipPathTransform();
				if (!t.isIdentity()) {
					tmp.append(" transform=\"matrix(").append(
							MathUtils.round(t.getM11(), 3));
					tmp.append(' ').append(MathUtils.round(t.getM12(), 3));
					tmp.append(' ').append(MathUtils.round(t.getM21(), 3));
					tmp.append(' ').append(MathUtils.round(t.getM22(), 3));
					tmp.append(' ').append(MathUtils.round(t.getM31(), 3));
					tmp.append(' ').append(MathUtils.round(t.getM32(), 3))
							.append(")\"");
				}
				tmp.append("/></clipPath></defs>");
			}
			this.newClipPath_ = false;
			if (shadowChanged) {
				if (!this.getPainter().getShadow().isNone()) {
					if (!this.getPainter().getShadow().equals(
							this.currentShadow_)) {
						this.currentShadow_ = this.getPainter().getShadow();
						this.currentShadowId_ = this.createShadowFilter(tmp);
					} else {
						this.currentShadowId_ = this.nextShadowId_;
					}
				} else {
					this.currentShadowId_ = -1;
				}
			}
			tmp.append("<g");
			if (this.getPainter().hasClipping()) {
				tmp.append(this.getClipPath());
			}
			if (this.currentShadowId_ != -1) {
				tmp.append(" filter=\"url(#f").append(this.currentShadowId_)
						.append(")\"");
			}
			tmp.append('>');
		}
		if (penChanged) {
			this.currentPen_ = this.getPainter().getPen();
			this.strokeStyle_ = this.getStrokeStyle();
		}
		if (brushChanged) {
			this.currentBrush_ = this.getPainter().getBrush();
			this.fillStyle_ = this.getFillStyle();
		}
		if (fontChanged) {
			this.currentFont_ = this.getPainter().getFont();
			this.fontStyle_ = this.getFontStyle();
		}
		tmp.append("<g style=\"").append(this.fillStyle_).append(
				this.strokeStyle_).append(this.fontStyle_).append('"');
		if (!this.currentTransform_.isIdentity()) {
			tmp.append(" transform=\"matrix(").append(
					MathUtils.round(this.currentTransform_.getM11(), 3));
			tmp.append(' ').append(
					MathUtils.round(this.currentTransform_.getM12(), 3));
			tmp.append(' ').append(
					MathUtils.round(this.currentTransform_.getM21(), 3));
			tmp.append(' ').append(
					MathUtils.round(this.currentTransform_.getM22(), 3));
			tmp.append(' ').append(
					MathUtils.round(this.currentTransform_.getM31(), 3));
			tmp.append(' ').append(
					MathUtils.round(this.currentTransform_.getM32(), 3))
					.append(")\"");
		}
		tmp.append('>');
		this.shapes_.append(tmp.toString());
		this.changeFlags_.clear();
	}

	private String getFillStyle() {
		char[] buf = new char[30];
		String result = "";
		switch (this.getPainter().getBrush().getStyle()) {
		case NoBrush:
			result += "fill:none;";
			break;
		case SolidPattern: {
			WColor color = this.getPainter().getBrush().getColor();
			result += "fill:" + color.getCssText() + ";";
			if (color.getAlpha() != 255) {
				result += "fill-opacity:";
				result += MathUtils.round(color.getAlpha() / 255., 3);
				result += ';';
			}
			break;
		}
		}
		return result;
	}

	private String getStrokeStyle() {
		StringBuilder result = new StringBuilder();
		String buf;
		WPen pen = this.getPainter().getPen();
		if (!((this.getPainter().getRenderHints() & WPainter.RenderHint.Antialiasing
				.getValue()) != 0)) {
			result.append("shape-rendering:optimizeSpeed;");
		}
		if (pen.getStyle() != PenStyle.NoPen) {
			WColor color = pen.getColor();
			result.append("stroke:").append(color.getCssText()).append(';');
			if (color.getAlpha() != 255) {
				result.append("stroke-opacity:").append(
						MathUtils.round(color.getAlpha() / 255., 2))
						.append(';');
			}
			WLength w = this.getPainter().normalizedPenWidth(pen.getWidth(),
					true);
			if (!w.equals(new WLength(1))) {
				result.append("stroke-width:").append(w.getCssText()).append(
						";");
			}
			switch (pen.getCapStyle()) {
			case FlatCap:
				break;
			case SquareCap:
				result.append("stroke-linecap:square;");
				break;
			case RoundCap:
				result.append("stroke-linecap:round;");
			}
			switch (pen.getJoinStyle()) {
			case MiterJoin:
				break;
			case BevelJoin:
				result.append("stroke-linejoin:bevel;");
				break;
			case RoundJoin:
				result.append("stroke-linejoin:round;");
			}
			switch (pen.getStyle()) {
			case NoPen:
				break;
			case SolidLine:
				break;
			case DashLine:
				result.append("stroke-dasharray:4,2;");
				break;
			case DotLine:
				result.append("stroke-dasharray:1,2;");
				break;
			case DashDotLine:
				result.append("stroke-dasharray:4,2,1,2;");
				break;
			case DashDotDotLine:
				result.append("stroke-dasharray:4,2,1,2,1,2;");
				break;
			}
		}
		return result.toString();
	}

	private String getFontStyle() {
		return this.getPainter().getFont().getCssText(false);
	}

	private String getClipPath() {
		if (this.getPainter().hasClipping()) {
			return " clip-path=\"url(#clip"
					+ String.valueOf(this.currentClipId_) + ")\"";
		} else {
			return "";
		}
	}

	private int createShadowFilter(StringBuilder out) {
		char[] buf = new char[30];
		int result = ++this.nextShadowId_;
		out
				.append("<filter id=\"f")
				.append(result)
				.append(
						"\" width=\"150%\" height=\"150%\"><feOffset result=\"offOut\" in=\"SourceAlpha\" dx=\"")
				.append(MathUtils.round(this.currentShadow_.getOffsetX(), 3))
				.append("\" dy=\"");
		out.append(MathUtils.round(this.currentShadow_.getOffsetY(), 3))
				.append("\" />");
		out
				.append("<feColorMatrix result=\"colorOut\" in=\"offOut\" type=\"matrix\" values=\"");
		double r = this.currentShadow_.getColor().getRed() / 255.;
		double g = this.currentShadow_.getColor().getGreen() / 255.;
		double b = this.currentShadow_.getColor().getBlue() / 255.;
		double a = this.currentShadow_.getColor().getAlpha() / 255.;
		out.append("0 0 0 ").append(MathUtils.round(r, 3)).append(" 0 ");
		out.append("0 0 0 ").append(MathUtils.round(g, 3)).append(" 0 ");
		out.append("0 0 0 ").append(MathUtils.round(b, 3)).append(" 0 ");
		out.append("0 0 0 ").append(MathUtils.round(a, 3)).append(" 0\"/>");
		out
				.append(
						"<feGaussianBlur result=\"blurOut\" in=\"colorOut\" stdDeviation=\"")
				.append(
						MathUtils.round(Math
								.sqrt(this.currentShadow_.getBlur()), 3))
				.append(
						"\" /><feBlend in=\"SourceGraphic\" in2=\"blurOut\" mode=\"normal\" /></filter>");
		return result;
	}

	private String fillStyle_;
	private String strokeStyle_;
	private String fontStyle_;

	private static String quote(double d) {
		char[] buf = new char[30];
		return quote(MathUtils.round(d, 3));
	}

	private static String quote(String s) {
		return '"' + s + '"';
	}

	private void drawPlainPath(StringWriter out, WPainterPath path) {
		char[] buf = new char[30];
		if (!this.busyWithPath_) {
			out.append("<path d=\"");
			this.busyWithPath_ = true;
			this.pathTranslation_.setX(0);
			this.pathTranslation_.setY(0);
		}
		List<WPainterPath.Segment> segments = path.getSegments();
		if (!segments.isEmpty()
				&& segments.get(0).getType() != WPainterPath.Segment.Type.MoveTo) {
			out.append("M0,0");
		}
		for (int i = 0; i < segments.size(); ++i) {
			final WPainterPath.Segment s = segments.get(i);
			if (s.getType() == WPainterPath.Segment.Type.ArcC) {
				WPointF current = path.getPositionAtSegment(i);
				final double cx = segments.get(i).getX();
				final double cy = segments.get(i).getY();
				final double rx = segments.get(i + 1).getX();
				final double ry = segments.get(i + 1).getY();
				final double theta1 = -WTransform.degreesToRadians(segments
						.get(i + 2).getX());
				final double deltaTheta = -WTransform
						.degreesToRadians(adjust360(segments.get(i + 2).getY()));
				i += 2;
				final double x1 = rx * Math.cos(theta1) + cx;
				final double y1 = ry * Math.sin(theta1) + cy;
				final double x2 = rx * Math.cos(theta1 + deltaTheta) + cx;
				final double y2 = ry * Math.sin(theta1 + deltaTheta) + cy;
				final int fa = Math.abs(deltaTheta) > 3.14159265358979323846 ? 1
						: 0;
				final int fs = deltaTheta > 0 ? 1 : 0;
				if (!fequal(current.getX(), x1) || !fequal(current.getY(), y1)) {
					out.append('L').append(
							MathUtils.round(x1 + this.pathTranslation_.getX(),
									3));
					out.append(',').append(
							MathUtils.round(y1 + this.pathTranslation_.getY(),
									3));
				}
				out.append('A').append(MathUtils.round(rx, 3));
				out.append(',').append(MathUtils.round(ry, 3));
				out.append(" 0 ").append(String.valueOf(fa)).append(",")
						.append(String.valueOf(fs));
				out.append(' ').append(
						MathUtils.round(x2 + this.pathTranslation_.getX(), 3));
				out.append(',').append(
						MathUtils.round(y2 + this.pathTranslation_.getY(), 3));
			} else {
				switch (s.getType()) {
				case MoveTo:
					out.append('M');
					break;
				case LineTo:
					out.append('L');
					break;
				case CubicC1:
					out.append('C');
					break;
				case CubicC2:
				case CubicEnd:
					out.append(' ');
					break;
				case QuadC:
					out.append('Q');
					break;
				case QuadEnd:
					out.append(' ');
					break;
				default:
					assert false;
				}
				out.append(MathUtils.round(s.getX()
						+ this.pathTranslation_.getX(), 3));
				out.append(',').append(
						MathUtils.round(
								s.getY() + this.pathTranslation_.getY(), 3));
			}
		}
	}

	private void streamResourceData(Writer stream) throws IOException {
		this.finishPath();
		if (this.paintUpdate_) {
			stream
					.append(
							"<g xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\"><g><g>")
					.append(this.shapes_.toString()).append("</g></g></g>");
		} else {
			stream
					.append(
							"<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" version=\"1.1\" baseProfile=\"full\" width=\"")
					.append(this.getWidth().getCssText())
					.append("\" height=\"").append(
							this.getHeight().getCssText()).append("\">")
					.append("<g><g>").append(this.shapes_.toString()).append(
							"</g></g></svg>");
		}
	}

	static double adjust360(double d) {
		if (Math.abs(d - 360) < 0.01) {
			return 359.5;
		} else {
			if (Math.abs(d + 360) < 0.01) {
				return -359.5;
			} else {
				return d;
			}
		}
	}

	static boolean fequal(double d1, double d2) {
		return Math.abs(d1 - d2) < 1E-5;
	}

	private static int nextClipId_ = 0;
}
