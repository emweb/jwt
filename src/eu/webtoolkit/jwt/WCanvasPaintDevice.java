package eu.webtoolkit.jwt;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.http.*;
import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.utils.*;
import eu.webtoolkit.jwt.servlet.*;

/**
 * A paint device for rendering using the HTML 5 &lt;canvas&gt; element.
 * 
 * 
 * The WCanvasPaintDevice is used by {@link WPaintedWidget} to render to the
 * browser using the HTML 5 &lt;canvas&gt; element. You usually will not use the
 * device directly, but rather rely on {@link WPaintedWidget} to use this device
 * when appropriate.
 * <p>
 * <p>
 * <i><b>Note:</b>Because of the lack for text support in the current HTML 5
 * &lt;canvas&gt; specification, there is only limited support for text. Text is
 * rendered in an overlayed DIV and a consequence text is not subject to
 * rotation and scaling components of the current transformation (but does take
 * into account translation). This will be fixed in the future (some way, some
 * how!). On most browser you can use the {@link WSvgImage} or {@link WVmlImage}
 * paint devices which do support text natively. </i>
 * </p>
 */
public class WCanvasPaintDevice extends WObject implements WPaintDevice {
	/**
	 * Create a canvas paint device.
	 */
	public WCanvasPaintDevice(WLength width, WLength height, WObject parent) {
		super(parent);
		this.width_ = width;
		this.height_ = height;
		this.painter_ = null;
		this.changeFlags_ = EnumSet.noneOf(WPaintDevice.ChangeFlag.class);
		this.paintFlags_ = EnumSet.noneOf(PaintFlag.class);
		this.js_ = "";
		this.textElements_ = new ArrayList<DomElement>();
		this.images_ = new ArrayList<String>();
	}

	public WCanvasPaintDevice(WLength width, WLength height) {
		this(width, height, (WObject) null);
	}

	public void setChanged(EnumSet<WPaintDevice.ChangeFlag> flags) {
		this.changeFlags_.addAll(flags);
	}

	public final void setChanged(WPaintDevice.ChangeFlag flag,
			WPaintDevice.ChangeFlag... flags) {
		setChanged(EnumSet.of(flag, flags));
	}

	public void drawArc(WRectF rect, double startAngle, double spanAngle) {
		if (rect.getWidth() < MathUtils.EPSILON
				|| rect.getHeight() < MathUtils.EPSILON) {
			return;
		}
		this.renderStateChanges();
		StringWriter tmp = new StringWriter();
		WPointF ra = normalizedDegreesToRadians(startAngle, spanAngle);
		double sx;
		double sy;
		double r;
		double lw;
		if (rect.getWidth() > rect.getHeight()) {
			sx = 1;
			sy = Math.max(0.005, rect.getHeight() / rect.getWidth());
			r = rect.getWidth() / 2;
		} else {
			sx = Math.max(0.005, rect.getWidth() / rect.getHeight());
			sy = 1;
			lw = this.getPainter().normalizedPenWidth(
					this.getPainter().getPen().getWidth(), true).getValue()
					* 1 / Math.min(sx, sy);
			r = rect.getHeight() / 2;
		}
		WPen pen = this.getPainter().getPen();
		if (pen.getStyle() != PenStyle.NoPen) {
			lw = this.getPainter().normalizedPenWidth(pen.getWidth(), true)
					.getValue()
					* 1 / Math.min(sx, sy);
		} else {
			lw = 0;
		}
		r = Math.max(0.005, r - lw / 2);
		tmp.append("ctx.save();").append("ctx.translate(").append(
				String.valueOf(rect.getCenter().getX())).append(",").append(
				String.valueOf(rect.getCenter().getY())).append(");").append(
				"ctx.scale(").append(String.valueOf(sx)).append(",").append(
				String.valueOf(sy)).append(");").append("ctx.lineWidth = ")
				.append(String.valueOf(lw)).append(";").append(
						"ctx.beginPath();").append("ctx.arc(0,0,").append(
						String.valueOf(r)).append(',').append(
						String.valueOf(ra.getX())).append(",").append(
						String.valueOf(ra.getY())).append(",true);");
		if (this.getPainter().getBrush().getStyle() != WBrushStyle.NoBrush) {
			tmp.append("ctx.fill();");
		}
		if (this.getPainter().getPen().getStyle() != PenStyle.NoPen) {
			tmp.append("ctx.stroke();");
		}
		tmp.append("ctx.restore();");
		this.js_ += tmp.toString();
	}

	public void drawImage(WRectF rect, String imgUri, int imgWidth,
			int imgHeight, WRectF sourceRect) {
		this.renderStateChanges();
		int imageIndex = this.createImage(imgUri);
		char[] buf = new char[30];
		StringWriter tmp = new StringWriter();
		tmp.append("ctx.drawImage(images[").append(String.valueOf(imageIndex))
				.append("],").append(MathUtils.round(sourceRect.getX(), 3));
		tmp.append(',').append(MathUtils.round(sourceRect.getY(), 3));
		tmp.append(',').append(MathUtils.round(sourceRect.getWidth(), 3));
		tmp.append(',').append(MathUtils.round(sourceRect.getHeight(), 3));
		tmp.append(',').append(MathUtils.round(rect.getX(), 3));
		tmp.append(',').append(MathUtils.round(rect.getY(), 3));
		tmp.append(',').append(MathUtils.round(rect.getWidth(), 3));
		tmp.append(',').append(MathUtils.round(rect.getHeight(), 3)).append(
				");");
		this.js_ += tmp.toString();
	}

	public void drawLine(double x1, double y1, double x2, double y2) {
		this.renderStateChanges();
		char[] buf = new char[30];
		StringWriter tmp = new StringWriter();
		tmp.append("ctx.beginPath();").append("ctx.moveTo(").append(
				MathUtils.round(x1, 3)).append(',');
		tmp.append(MathUtils.round(y1, 3)).append(");");
		tmp.append("ctx.lineTo(").append(MathUtils.round(x2, 3)).append(',');
		tmp.append(MathUtils.round(y2, 3)).append(");ctx.stroke();");
		this.js_ += tmp.toString();
	}

	public void drawPath(WPainterPath path) {
		this.renderStateChanges();
		StringWriter tmp = new StringWriter();
		this.drawPlainPath(tmp, path);
		if (this.getPainter().getBrush().getStyle() != WBrushStyle.NoBrush) {
			tmp.append("ctx.fill();");
		}
		if (this.getPainter().getPen().getStyle() != PenStyle.NoPen) {
			tmp.append("ctx.stroke();");
		}
		this.js_ += tmp.toString() + '\n';
	}

	public void drawText(WRectF rect, EnumSet<AlignmentFlag> flags,
			CharSequence text) {
		WPointF pos = this.getPainter().getCombinedTransform().map(
				rect.getTopLeft());
		DomElement e = DomElement.createNew(DomElementType.DomElement_DIV);
		e.setProperty(Property.PropertyStylePosition, "absolute");
		e.setProperty(Property.PropertyStyleTop, String.valueOf(pos.getY())
				+ "px");
		e.setProperty(Property.PropertyStyleLeft, String.valueOf(pos.getX())
				+ "px");
		e.setProperty(Property.PropertyStyleWidth, String.valueOf(rect
				.getWidth())
				+ "px");
		e.setProperty(Property.PropertyStyleHeight, String.valueOf(rect
				.getHeight())
				+ "px");
		AlignmentFlag horizontalAlign = EnumUtils.enumFromSet(EnumUtils.mask(
				flags, AlignmentFlag.AlignHorizontalMask));
		AlignmentFlag verticalAlign = EnumUtils.enumFromSet(EnumUtils.mask(
				flags, AlignmentFlag.AlignVerticalMask));
		DomElement t = e;
		if (verticalAlign != AlignmentFlag.AlignTop) {
			t = DomElement.createNew(DomElementType.DomElement_DIV);
			if (verticalAlign == AlignmentFlag.AlignMiddle) {
				e.setProperty(Property.PropertyStyleDisplay, "table");
				t.setProperty(Property.PropertyStyleDisplay, "table-cell");
				t.setProperty(Property.PropertyStyleVerticalAlign, "middle");
			} else {
				if (verticalAlign == AlignmentFlag.AlignBottom) {
					t.setProperty(Property.PropertyStylePosition, "absolute");
					t.setProperty(Property.PropertyStyleWidth, "100%");
					t.setProperty(Property.PropertyStyleBottom, "0px");
				}
			}
		}
		t.setProperty(Property.PropertyInnerHTML, WWebWidget.escapeText(text,
				true).toString());
		WFont f = this.getPainter().getFont();
		f.updateDomElement(t, false, true);
		t.setProperty(Property.PropertyStyleColor, this.getPainter().getPen()
				.getColor().getCssText());
		if (horizontalAlign == AlignmentFlag.AlignRight) {
			t.setProperty(Property.PropertyStyleTextAlign, "right");
		} else {
			if (horizontalAlign == AlignmentFlag.AlignCenter) {
				t.setProperty(Property.PropertyStyleTextAlign, "center");
			} else {
				t.setProperty(Property.PropertyStyleTextAlign, "left");
			}
		}
		if (t != e) {
			e.addChild(t);
		}
		this.textElements_.add(e);
	}

	public void init() {
		this.changeFlags_.clear();
	}

	public void done() {
	}

	public boolean isPaintActive() {
		return this.painter_ != null;
	}

	public void render(String canvasId, DomElement text) {
		String canvasVar = "Wt2_99_2.getElement('" + canvasId + "')";
		StringWriter tmp = new StringWriter();
		tmp.append("if(").append(canvasVar).append(
				".getContext){new Wt._p_.ImagePreloader([");
		for (int i = 0; i < this.images_.size(); ++i) {
			if (i != 0) {
				tmp.append(',');
			}
			tmp.append('\'').append(this.images_.get(i)).append('\'');
		}
		tmp.append("],function(images) {var ctx=").append(canvasVar).append(
				".getContext('2d');");
		if (!!EnumUtils.mask(this.paintFlags_, PaintFlag.PaintUpdate).isEmpty()) {
			tmp.append("ctx.clearRect(0,0,").append(
					String.valueOf(this.getWidth().getValue())).append(",")
					.append(String.valueOf(this.getHeight().getValue()))
					.append(");");
		}
		tmp.append("ctx.save();ctx.save();").append(this.js_).append(
				"ctx.restore();ctx.restore();});}");
		text.callJavaScript(tmp.toString());
		for (int i = 0; i < this.textElements_.size(); ++i) {
			text.addChild(this.textElements_.get(i));
		}
	}

	public WLength getWidth() {
		return this.width_;
	}

	public WLength getHeight() {
		return this.height_;
	}

	public EnumSet<PaintFlag> getPaintFlags() {
		return this.paintFlags_;
	}

	public WPainter getPainter() {
		return this.painter_;
	}

	public void setPainter(WPainter painter) {
		this.painter_ = painter;
	}

	public void setPaintFlags(EnumSet<PaintFlag> paintFlags) {
		this.paintFlags_.clear();
	}

	public final void setPaintFlags(PaintFlag paintFlag,
			PaintFlag... paintFlags) {
		setPaintFlags(EnumSet.of(paintFlag, paintFlags));
	}

	private WLength width_;
	private WLength height_;
	private WPainter painter_;
	private EnumSet<WPaintDevice.ChangeFlag> changeFlags_;
	private EnumSet<PaintFlag> paintFlags_;
	private String js_;
	private List<DomElement> textElements_;
	private List<String> images_;

	private void renderTransform(StringWriter s, WTransform t, boolean invert) {
		if (!t.isIdentity()) {
			char[] buf = new char[30];
			WTransform.TRSRDecomposition d = new WTransform.TRSRDecomposition();
			t.decomposeTranslateRotateScaleRotate(d);
			if (!invert) {
				if (Math.abs(d.dx) > MathUtils.EPSILON
						|| Math.abs(d.dy) > MathUtils.EPSILON) {
					s.append("ctx.translate(").append(MathUtils.round(d.dx, 3))
							.append(',');
					s.append(MathUtils.round(d.dy, 3)).append(");");
				}
				if (Math.abs(d.alpha1) > MathUtils.EPSILON) {
					s.append("ctx.rotate(").append(String.valueOf(d.alpha1))
							.append(");");
				}
				if (Math.abs(d.sx - 1) > MathUtils.EPSILON
						|| Math.abs(d.sy - 1) > MathUtils.EPSILON) {
					s.append("ctx.scale(").append(MathUtils.round(d.sx, 3))
							.append(',');
					s.append(MathUtils.round(d.sy, 3)).append(");");
				}
				if (Math.abs(d.alpha2) > MathUtils.EPSILON) {
					s.append("ctx.rotate(").append(String.valueOf(d.alpha2))
							.append(");");
				}
			} else {
				if (Math.abs(d.alpha2) > MathUtils.EPSILON) {
					s.append("ctx.rotate(").append(String.valueOf(-d.alpha2))
							.append(");");
				}
				if (Math.abs(d.sx - 1) > MathUtils.EPSILON
						|| Math.abs(d.sy - 1) > MathUtils.EPSILON) {
					s.append("ctx.scale(").append(MathUtils.round(1 / d.sx, 3))
							.append(',');
					s.append(MathUtils.round(1 / d.sy, 3)).append(");");
				}
				if (Math.abs(d.alpha1) > MathUtils.EPSILON) {
					s.append("ctx.rotate(").append(String.valueOf(-d.alpha1))
							.append(");");
				}
				if (Math.abs(d.dx) > MathUtils.EPSILON
						|| Math.abs(d.dy) > MathUtils.EPSILON) {
					s.append("ctx.translate(")
							.append(MathUtils.round(-d.dx, 3)).append(',');
					s.append(MathUtils.round(-d.dy, 3)).append(");");
				}
			}
		}
	}

	private final void renderTransform(StringWriter s, WTransform t) {
		renderTransform(s, t, false);
	}

	private void renderStateChanges() {
		if (this.changeFlags_.equals(0)) {
			return;
		}
		StringWriter s = new StringWriter();
		if (!EnumUtils.mask(
				this.changeFlags_,
				EnumSet.of(WPaintDevice.ChangeFlag.Transform,
						WPaintDevice.ChangeFlag.Clipping)).isEmpty()) {
			if (!EnumUtils.mask(this.changeFlags_,
					WPaintDevice.ChangeFlag.Clipping).isEmpty()) {
				s.append("ctx.restore();ctx.restore();ctx.save();");
				WTransform t = this.getPainter().getClipPathTransform();
				this.renderTransform(s, t);
				if (this.getPainter().hasClipping()) {
					this.drawPlainPath(s, this.getPainter().getClipPath());
					s.append("ctx.clip();");
				}
				this.renderTransform(s, t, true);
				s.append("ctx.save();");
			} else {
				s.append("ctx.restore();ctx.save();");
			}
			WTransform t = this.getPainter().getCombinedTransform();
			this.renderTransform(s, t);
			this.changeFlags_.addAll(EnumSet.of(WPaintDevice.ChangeFlag.Pen,
					WPaintDevice.ChangeFlag.Brush));
		}
		if (!EnumUtils.mask(this.changeFlags_, WPaintDevice.ChangeFlag.Pen)
				.isEmpty()) {
			WPen pen = this.getPainter().getPen();
			s.append("ctx.strokeStyle=\"" + pen.getColor().getCssText(true))
					.append("\";ctx.lineWidth=").append(
							String.valueOf(this.getPainter()
									.normalizedPenWidth(pen.getWidth(), true)
									.getValue())).append(';');
			switch (pen.getCapStyle()) {
			case FlatCap:
				s.append("ctx.lineCap='butt';");
				break;
			case SquareCap:
				s.append("ctx.lineCap='square';");
				break;
			case RoundCap:
				s.append("ctx.lineCap='round';");
			}
			switch (pen.getJoinStyle()) {
			case MiterJoin:
				s.append("ctx.lineJoin='miter';");
				break;
			case BevelJoin:
				s.append("ctx.lineJoin='bevel';");
				break;
			case RoundJoin:
				s.append("ctx.lineJoin='round';");
			}
		}
		if (!EnumUtils.mask(this.changeFlags_, WPaintDevice.ChangeFlag.Brush)
				.isEmpty()) {
			s.append("ctx.fillStyle=\""
					+ this.getPainter().getBrush().getColor().getCssText(true)
					+ "\";");
		}
		this.js_ += s.toString();
		this.changeFlags_.clear();
	}

	private void drawPlainPath(StringWriter out, WPainterPath path) {
		char[] buf = new char[30];
		List<WPainterPath.Segment> segments = path.getSegments();
		out.append("ctx.beginPath();");
		if (segments.size() > 0
				&& segments.get(0).getType() != WPainterPath.Segment.Type.MoveTo) {
			out.append("ctx.moveTo(0,0);");
		}
		for (int i = 0; i < segments.size(); ++i) {
			final WPainterPath.Segment s = segments.get(i);
			switch (s.getType()) {
			case MoveTo:
				out.append("ctx.moveTo(").append(MathUtils.round(s.getX(), 3));
				out.append(',').append(MathUtils.round(s.getY(), 3)).append(
						");");
				break;
			case LineTo:
				out.append("ctx.lineTo(").append(MathUtils.round(s.getX(), 3));
				out.append(',').append(MathUtils.round(s.getY(), 3)).append(
						");");
				break;
			case CubicC1:
				out.append("ctx.bezierCurveTo(").append(
						MathUtils.round(s.getX(), 3));
				out.append(',').append(MathUtils.round(s.getY(), 3));
				break;
			case CubicC2:
				out.append(',').append(MathUtils.round(s.getX(), 3))
						.append(',');
				out.append(MathUtils.round(s.getY(), 3));
				break;
			case CubicEnd:
				out.append(',').append(MathUtils.round(s.getX(), 3))
						.append(',');
				out.append(MathUtils.round(s.getY(), 3)).append(");");
				break;
			case ArcC:
				out.append("ctx.arc(").append(MathUtils.round(s.getX(), 3))
						.append(',');
				out.append(MathUtils.round(s.getY(), 3));
				break;
			case ArcR:
				out.append(',').append(MathUtils.round(s.getX(), 3));
				break;
			case ArcAngleSweep: {
				WPointF r = normalizedDegreesToRadians(s.getX(), s.getY());
				out.append(',').append(String.valueOf(r.getX())).append(',')
						.append(String.valueOf(r.getY())).append(',').append(
								s.getY() > 0 ? "true" : "false").append(");");
			}
				break;
			case QuadC: {
				WPointF current = path.getPositionAtSegment(i);
				final double cpx = s.getX();
				final double cpy = s.getY();
				final double x = segments.get(i + 1).getX();
				final double y = segments.get(i + 1).getY();
				final double cp1x = current.getX() + 2.0 / 3.0
						* (cpx - current.getX());
				final double cp1y = current.getY() + 2.0 / 3.0
						* (cpy - current.getY());
				final double cp2x = cp1x + (x - current.getX()) / 3.0;
				final double cp2y = cp1y + (y - current.getY()) / 3.0;
				out.append("ctx.bezierCurveTo(").append(
						MathUtils.round(cp1x, 3)).append(',');
				out.append(MathUtils.round(cp1y, 3)).append(',');
				out.append(MathUtils.round(cp2x, 3)).append(',');
				out.append(MathUtils.round(cp2y, 3));
				break;
			}
			case QuadEnd:
				out.append(',').append(MathUtils.round(s.getX(), 3))
						.append(',');
				out.append(MathUtils.round(s.getY(), 3)).append(");");
			}
		}
	}

	private int createImage(String imgUri) {
		this.images_.add(imgUri);
		return this.images_.size() - 1;
	}

	static WPointF normalizedDegreesToRadians(double angle, double sweep) {
		angle = 360 - angle;
		int i = (int) angle / 360;
		angle -= i * 360;
		double r1 = WTransform.degreesToRadians(angle);
		if (Math.abs(sweep - 360) < 0.01) {
			sweep = 359.9;
		} else {
			if (Math.abs(sweep + 360) < 0.01) {
				sweep = -359.9;
			}
		}
		double a2 = angle - sweep;
		double r2 = WTransform.degreesToRadians(a2);
		return new WPointF(r1, r2);
	}
}
