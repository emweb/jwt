package eu.webtoolkit.jwt;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import eu.webtoolkit.jwt.utils.EnumUtils;
import eu.webtoolkit.jwt.utils.MathUtils;

/**
 * A paint device for rendering using the HTML 5 &lt;canvas&gt; element.
 * <p>
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
		this.js_ = new StringWriter();
		this.textElements_ = new ArrayList<DomElement>();
		this.images_ = new ArrayList<String>();
	}

	/**
	 * Create a canvas paint device.
	 * <p>
	 * Calls
	 * {@link #WCanvasPaintDevice(WLength width, WLength height, WObject parent)
	 * this(width, height, (WObject)null)}
	 */
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
		this.js_.append("ctx.save();").append("ctx.translate(").append(
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
			this.js_.append("ctx.fill();");
		}
		if (this.getPainter().getPen().getStyle() != PenStyle.NoPen) {
			this.js_.append("ctx.stroke();");
		}
		this.js_.append("ctx.restore();");
	}

	public void drawImage(WRectF rect, String imgUri, int imgWidth,
			int imgHeight, WRectF sourceRect) {
		this.renderStateChanges();
		int imageIndex = this.createImage(imgUri);
		char[] buf = new char[30];
		this.js_.append("ctx.drawImage(images[").append(
				String.valueOf(imageIndex)).append("],").append(
				MathUtils.round(sourceRect.getX(), 3));
		this.js_.append(',').append(MathUtils.round(sourceRect.getY(), 3));
		this.js_.append(',').append(MathUtils.round(sourceRect.getWidth(), 3));
		this.js_.append(',').append(MathUtils.round(sourceRect.getHeight(), 3));
		this.js_.append(',').append(MathUtils.round(rect.getX(), 3));
		this.js_.append(',').append(MathUtils.round(rect.getY(), 3));
		this.js_.append(',').append(MathUtils.round(rect.getWidth(), 3));
		this.js_.append(',').append(MathUtils.round(rect.getHeight(), 3))
				.append(");");
	}

	public void drawLine(double x1, double y1, double x2, double y2) {
		this.renderStateChanges();
		char[] buf = new char[30];
		this.js_.append("ctx.beginPath();").append("ctx.moveTo(").append(
				MathUtils.round(x1, 3)).append(',');
		this.js_.append(MathUtils.round(y1, 3)).append(");");
		this.js_.append("ctx.lineTo(").append(MathUtils.round(x2, 3)).append(
				',');
		this.js_.append(MathUtils.round(y2, 3)).append(");ctx.stroke();");
	}

	public void drawPath(WPainterPath path) {
		this.renderStateChanges();
		this.drawPlainPath(this.js_, path);
		if (this.getPainter().getBrush().getStyle() != WBrushStyle.NoBrush) {
			this.js_.append("ctx.fill();");
		}
		if (this.getPainter().getPen().getStyle() != PenStyle.NoPen) {
			this.js_.append("ctx.stroke();");
		}
		this.js_.append('\n');
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
		tmp.append("ctx.save();ctx.save();").append(this.js_.toString())
				.append("ctx.restore();ctx.restore();});}");
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
	private StringWriter js_;
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
		if (!EnumUtils.mask(
				this.changeFlags_,
				EnumSet.of(WPaintDevice.ChangeFlag.Transform,
						WPaintDevice.ChangeFlag.Clipping)).isEmpty()) {
			if (!EnumUtils.mask(this.changeFlags_,
					WPaintDevice.ChangeFlag.Clipping).isEmpty()) {
				this.js_.append("ctx.restore();ctx.restore();ctx.save();");
				WTransform t = this.getPainter().getClipPathTransform();
				this.renderTransform(this.js_, t);
				if (this.getPainter().hasClipping()) {
					this.drawPlainPath(this.js_, this.getPainter()
							.getClipPath());
					this.js_.append("ctx.clip();");
				}
				this.renderTransform(this.js_, t, true);
				this.js_.append("ctx.save();");
			} else {
				this.js_.append("ctx.restore();ctx.save();");
			}
			WTransform t = this.getPainter().getCombinedTransform();
			this.renderTransform(this.js_, t);
			this.changeFlags_.addAll(EnumSet.of(WPaintDevice.ChangeFlag.Pen,
					WPaintDevice.ChangeFlag.Brush));
		}
		if (!EnumUtils.mask(this.changeFlags_, WPaintDevice.ChangeFlag.Pen)
				.isEmpty()) {
			WPen pen = this.getPainter().getPen();
			this.js_.append(
					"ctx.strokeStyle=\"" + pen.getColor().getCssText(true))
					.append("\";ctx.lineWidth=").append(
							String.valueOf(this.getPainter()
									.normalizedPenWidth(pen.getWidth(), true)
									.getValue())).append(';');
			switch (pen.getCapStyle()) {
			case FlatCap:
				this.js_.append("ctx.lineCap='butt';");
				break;
			case SquareCap:
				this.js_.append("ctx.lineCap='square';");
				break;
			case RoundCap:
				this.js_.append("ctx.lineCap='round';");
			}
			switch (pen.getJoinStyle()) {
			case MiterJoin:
				this.js_.append("ctx.lineJoin='miter';");
				break;
			case BevelJoin:
				this.js_.append("ctx.lineJoin='bevel';");
				break;
			case RoundJoin:
				this.js_.append("ctx.lineJoin='round';");
			}
		}
		if (!EnumUtils.mask(this.changeFlags_, WPaintDevice.ChangeFlag.Brush)
				.isEmpty()) {
			this.js_.append("ctx.fillStyle=\"").append(
					this.getPainter().getBrush().getColor().getCssText(true))
					.append("\";");
		}
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
