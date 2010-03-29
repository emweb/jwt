/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
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
 * <i><b>Note: </b>Because of the lack for text support in the current HTML 5
 * &lt;canvas&gt; specification, there is only limited support for text. Text is
 * rendered in an overlayed DIV and a consequence text is not subject to
 * rotation and scaling components of the current transformation (but does take
 * into account translation). This will be fixed in the future (some way, some
 * how!). On most browser you can use the {@link WSvgImage} or {@link WVmlImage}
 * paint devices which do support text natively. </i>
 * </p>
 */
public class WCanvasPaintDevice extends WObject implements WPaintDevice {
	enum TextMethod {
		MozText, Html5Text, DomText;

		/**
		 * Returns the numerical representation of this enum.
		 */
		public int getValue() {
			return ordinal();
		}
	}

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
		this.busyWithPath_ = false;
		this.currentTransform_ = new WTransform();
		this.currentBrush_ = new WBrush();
		this.currentPen_ = new WPen();
		this.currentShadow_ = new WShadow();
		this.currentFont_ = new WFont();
		this.pathTranslation_ = new WPointF();
		this.js_ = new StringWriter();
		this.textElements_ = new ArrayList<DomElement>();
		this.images_ = new ArrayList<String>();
		this.textMethod_ = WCanvasPaintDevice.TextMethod.DomText;
		WApplication app = WApplication.getInstance();
		if (app != null) {
			if (app.getEnvironment().agentIsChrome()) {
				if (app.getEnvironment().getAgent().getValue() >= WEnvironment.UserAgent.Chrome2
						.getValue()) {
					this.textMethod_ = WCanvasPaintDevice.TextMethod.Html5Text;
				}
			} else {
				if (app.getEnvironment().agentIsGecko()) {
					if (app.getEnvironment().getAgent().getValue() >= WEnvironment.UserAgent.Firefox3_5
							.getValue()) {
						this.textMethod_ = WCanvasPaintDevice.TextMethod.Html5Text;
					} else {
						if (app.getEnvironment().getAgent().getValue() >= WEnvironment.UserAgent.Firefox3_0
								.getValue()) {
							this.textMethod_ = WCanvasPaintDevice.TextMethod.MozText;
						}
					}
				} else {
					if (app.getEnvironment().agentIsSafari()) {
						if (app.getEnvironment().getAgent().getValue() >= WEnvironment.UserAgent.Safari4
								.getValue()) {
							this.textMethod_ = WCanvasPaintDevice.TextMethod.Html5Text;
						}
					}
				}
			}
		}
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
		this.finishPath();
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
		char[] buf = new char[30];
		this.js_.append("ctx.save();").append("ctx.translate(").append(
				MathUtils.round(rect.getCenter().getX(), 3));
		this.js_.append(",")
				.append(MathUtils.round(rect.getCenter().getY(), 3));
		this.js_.append(");").append("ctx.scale(").append(
				MathUtils.round(sx, 3));
		this.js_.append(",").append(MathUtils.round(sy, 3)).append(");");
		this.js_.append("ctx.lineWidth = ").append(MathUtils.round(lw, 3))
				.append(";").append("ctx.beginPath();");
		this.js_.append("ctx.arc(0,0,").append(MathUtils.round(r, 3));
		this.js_.append(',').append(MathUtils.round(ra.getX(), 3));
		this.js_.append(",").append(MathUtils.round(ra.getY(), 3)).append(
				",true);");
		if (this.currentPen_.getStyle() != PenStyle.NoPen) {
			this.js_.append("ctx.stroke();");
		}
		if (this.currentBrush_.getStyle() != WBrushStyle.NoBrush) {
			this.js_.append("ctx.fill();");
		}
		this.js_.append("ctx.restore();");
	}

	public void drawImage(WRectF rect, String imgUri, int imgWidth,
			int imgHeight, WRectF sourceRect) {
		this.finishPath();
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
		WPainterPath path = new WPainterPath();
		path.moveTo(x1, y1);
		path.lineTo(x2, y2);
		this.drawPath(path);
	}

	public void drawPath(WPainterPath path) {
		this.renderStateChanges();
		this.drawPlainPath(this.js_, path);
	}

	public void drawText(WRectF rect, EnumSet<AlignmentFlag> flags,
			CharSequence text) {
		AlignmentFlag horizontalAlign = EnumUtils.enumFromSet(EnumUtils.mask(
				flags, AlignmentFlag.AlignHorizontalMask));
		AlignmentFlag verticalAlign = EnumUtils.enumFromSet(EnumUtils.mask(
				flags, AlignmentFlag.AlignVerticalMask));
		if (this.textMethod_ != WCanvasPaintDevice.TextMethod.DomText) {
			this.finishPath();
			this.renderStateChanges();
		}
		switch (this.textMethod_) {
		case Html5Text: {
			double x = 0;
			double y = 0;
			if (horizontalAlign != this.currentTextHAlign_) {
				this.js_.append("ctx.textAlign='");
				switch (horizontalAlign) {
				case AlignLeft:
					this.js_.append("left");
					break;
				case AlignRight:
					this.js_.append("right");
					break;
				case AlignCenter:
					this.js_.append("center");
					break;
				default:
					break;
				}
				this.js_.append("';");
				this.currentTextHAlign_ = horizontalAlign;
			}
			switch (horizontalAlign) {
			case AlignLeft:
				x = rect.getLeft();
				break;
			case AlignRight:
				x = rect.getRight();
				break;
			case AlignCenter:
				x = rect.getCenter().getX();
				break;
			default:
				break;
			}
			if (verticalAlign != this.currentTextVAlign_) {
				this.js_.append("ctx.textBaseline='");
				switch (verticalAlign) {
				case AlignTop:
					this.js_.append("top");
					break;
				case AlignBottom:
					this.js_.append("bottom");
					break;
				case AlignMiddle:
					this.js_.append("middle");
					break;
				default:
					break;
				}
				this.js_.append("';");
				this.currentTextVAlign_ = verticalAlign;
			}
			switch (verticalAlign) {
			case AlignTop:
				y = rect.getTop();
				break;
			case AlignBottom:
				y = rect.getBottom();
				break;
			case AlignMiddle:
				y = rect.getCenter().getY();
				break;
			default:
				break;
			}
			if (!this.currentBrush_.getColor().equals(
					this.currentPen_.getColor())) {
				this.js_.append("ctx.fillStyle=\"").append(
						this.currentPen_.getColor().getCssText(true)).append(
						"\";");
			}
			this.js_.append("ctx.fillText(").append(
					WString.toWString(text).getJsStringLiteral()).append(',')
					.append(String.valueOf(x)).append(',').append(
							String.valueOf(y)).append(");");
			if (!this.currentBrush_.getColor().equals(
					this.currentPen_.getColor())) {
				this.js_.append("ctx.fillStyle=\"").append(
						this.currentBrush_.getColor().getCssText(true)).append(
						"\";");
			}
		}
			break;
		case MozText: {
			String x = "";
			switch (horizontalAlign) {
			case AlignLeft:
				x = String.valueOf(rect.getLeft());
				break;
			case AlignRight:
				x = String.valueOf(rect.getRight()) + " - ctx.mozMeasureText("
						+ WString.toWString(text).getJsStringLiteral() + ")";
				break;
			case AlignCenter:
				x = String.valueOf(rect.getCenter().getX())
						+ " - ctx.mozMeasureText("
						+ WString.toWString(text).getJsStringLiteral() + ")/2";
				break;
			default:
				break;
			}
			double fontSize;
			switch (this.getPainter().getFont().getSize()) {
			case FixedSize:
				fontSize = this.getPainter().getFont().getFixedSize()
						.toPixels();
				break;
			default:
				fontSize = 16;
			}
			double y = 0;
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
			this.js_.append("ctx.save();");
			this.js_.append("ctx.translate(").append(x).append(", ").append(
					String.valueOf(y)).append(");");
			if (!this.currentBrush_.getColor().equals(
					this.currentPen_.getColor())) {
				this.js_.append("ctx.fillStyle=\"").append(
						this.currentPen_.getColor().getCssText(true)).append(
						"\";");
			}
			this.js_.append("ctx.mozDrawText(").append(
					WString.toWString(text).getJsStringLiteral()).append(");");
			this.js_.append("ctx.restore();");
		}
			break;
		case DomText: {
			WPointF pos = this.getPainter().getCombinedTransform().map(
					rect.getTopLeft());
			DomElement e = DomElement.createNew(DomElementType.DomElement_DIV);
			e.setProperty(Property.PropertyStylePosition, "absolute");
			e.setProperty(Property.PropertyStyleTop, String.valueOf(pos.getY())
					+ "px");
			e.setProperty(Property.PropertyStyleLeft, String
					.valueOf(pos.getX())
					+ "px");
			e.setProperty(Property.PropertyStyleWidth, String.valueOf(rect
					.getWidth())
					+ "px");
			e.setProperty(Property.PropertyStyleHeight, String.valueOf(rect
					.getHeight())
					+ "px");
			DomElement t = e;
			if (verticalAlign != AlignmentFlag.AlignTop) {
				t = DomElement.createNew(DomElementType.DomElement_DIV);
				if (verticalAlign == AlignmentFlag.AlignMiddle) {
					e.setProperty(Property.PropertyStyleDisplay, "table");
					t.setProperty(Property.PropertyStyleDisplay, "table-cell");
					t
							.setProperty(Property.PropertyStyleVerticalAlign,
									"middle");
				} else {
					if (verticalAlign == AlignmentFlag.AlignBottom) {
						t.setProperty(Property.PropertyStylePosition,
								"absolute");
						t.setProperty(Property.PropertyStyleWidth, "100%");
						t.setProperty(Property.PropertyStyleBottom, "0px");
					}
				}
			}
			t.setProperty(Property.PropertyInnerHTML, WWebWidget.escapeText(
					text, true).toString());
			WFont f = this.getPainter().getFont();
			f.updateDomElement(t, false, true);
			t.setProperty(Property.PropertyStyleColor, this.getPainter()
					.getPen().getColor().getCssText());
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
		}
	}

	public void init() {
		this.currentBrush_ = this.getPainter().getBrush();
		this.currentPen_ = this.getPainter().getPen();
		this.currentShadow_ = this.getPainter().getShadow();
		this.currentFont_ = this.getPainter().getFont();
		this.currentTextVAlign_ = this.currentTextHAlign_ = AlignmentFlag.AlignLength;
		this.changeFlags_.clear();
	}

	public void done() {
		this.finishPath();
	}

	public boolean isPaintActive() {
		return this.painter_ != null;
	}

	public WCanvasPaintDevice.TextMethod getTextMethod() {
		return this.textMethod_;
	}

	void render(String canvasId, DomElement text) {
		String canvasVar = "Wt3_1_2.getElement('" + canvasId + "')";
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
	private WCanvasPaintDevice.TextMethod textMethod_;
	private boolean busyWithPath_;
	private WTransform currentTransform_;
	private WBrush currentBrush_;
	private WPen currentPen_;
	private WShadow currentShadow_;
	private WFont currentFont_;
	private WPointF pathTranslation_;
	private AlignmentFlag currentTextHAlign_;
	private AlignmentFlag currentTextVAlign_;
	private StringWriter js_;
	private List<DomElement> textElements_;
	private List<String> images_;

	private void finishPath() {
		if (this.busyWithPath_) {
			if (this.currentPen_.getStyle() != PenStyle.NoPen) {
				this.js_.append("ctx.stroke();");
			}
			if (this.currentBrush_.getStyle() != WBrushStyle.NoBrush) {
				this.js_.append("ctx.fill();");
			}
			this.js_.append('\n');
			this.busyWithPath_ = false;
		}
	}

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
		if (!!this.changeFlags_.isEmpty()) {
			return;
		}
		boolean brushChanged = !EnumUtils.mask(this.changeFlags_,
				WPaintDevice.ChangeFlag.Brush).isEmpty()
				&& !this.currentBrush_.equals(this.getPainter().getBrush());
		boolean penChanged = !EnumUtils.mask(this.changeFlags_,
				WPaintDevice.ChangeFlag.Pen).isEmpty()
				&& !this.currentPen_.equals(this.getPainter().getPen());
		boolean penColorChanged = penChanged
				&& !this.currentPen_.getColor().equals(
						this.getPainter().getPen().getColor());
		boolean shadowChanged = !EnumUtils.mask(this.changeFlags_,
				WPaintDevice.ChangeFlag.Shadow).isEmpty()
				&& !this.currentShadow_.equals(this.getPainter().getShadow());
		boolean fontChanged = !EnumUtils.mask(this.changeFlags_,
				WPaintDevice.ChangeFlag.Font).isEmpty()
				&& !this.currentFont_.equals(this.getPainter().getFont());
		if (!EnumUtils.mask(
				this.changeFlags_,
				EnumSet.of(WPaintDevice.ChangeFlag.Transform,
						WPaintDevice.ChangeFlag.Clipping)).isEmpty()) {
			boolean resetTransform = false;
			if (!EnumUtils.mask(this.changeFlags_,
					WPaintDevice.ChangeFlag.Clipping).isEmpty()) {
				this.finishPath();
				this.js_.append("ctx.restore();ctx.restore();ctx.save();");
				WTransform t = this.getPainter().getClipPathTransform();
				this.renderTransform(this.js_, t);
				if (this.getPainter().hasClipping()) {
					this.drawPlainPath(this.js_, this.getPainter()
							.getClipPath());
					this.js_.append("ctx.clip();");
					this.busyWithPath_ = false;
				}
				this.renderTransform(this.js_, t, true);
				this.js_.append("ctx.save();");
				resetTransform = true;
			} else {
				if (!EnumUtils.mask(this.changeFlags_,
						WPaintDevice.ChangeFlag.Transform).isEmpty()) {
					WTransform f = this.getPainter().getCombinedTransform();
					resetTransform = !this.currentTransform_.equals(f);
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
							resetTransform = false;
						}
					}
					if (resetTransform) {
						this.finishPath();
						this.js_.append("ctx.restore();ctx.save();");
					}
				}
			}
			if (resetTransform) {
				this.currentTransform_.assign(this.getPainter()
						.getCombinedTransform());
				this.renderTransform(this.js_, this.currentTransform_);
				this.pathTranslation_.setX(0);
				this.pathTranslation_.setY(0);
				penChanged = true;
				penColorChanged = true;
				brushChanged = true;
				shadowChanged = true;
				fontChanged = true;
				this.currentTextHAlign_ = this.currentTextVAlign_ = AlignmentFlag.AlignLength;
			}
		}
		if (penChanged || brushChanged || shadowChanged) {
			this.finishPath();
		}
		if (penChanged) {
			if (penColorChanged) {
				this.js_.append("ctx.strokeStyle=\"").append(
						this.getPainter().getPen().getColor().getCssText(true))
						.append("\";");
			}
			this.js_.append("ctx.lineWidth=").append(
					String.valueOf(this.getPainter().normalizedPenWidth(
							this.getPainter().getPen().getWidth(), true)
							.getValue())).append(';');
			if (this.currentPen_.getCapStyle() != this.getPainter().getPen()
					.getCapStyle()) {
				switch (this.getPainter().getPen().getCapStyle()) {
				case FlatCap:
					this.js_.append("ctx.lineCap='butt';");
					break;
				case SquareCap:
					this.js_.append("ctx.lineCap='square';");
					break;
				case RoundCap:
					this.js_.append("ctx.lineCap='round';");
				}
			}
			if (this.currentPen_.getJoinStyle() != this.getPainter().getPen()
					.getJoinStyle()) {
				switch (this.getPainter().getPen().getJoinStyle()) {
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
			this.currentPen_ = this.getPainter().getPen();
		}
		if (brushChanged) {
			this.currentBrush_ = this.painter_.getBrush();
			this.js_.append("ctx.fillStyle=\"").append(
					this.currentBrush_.getColor().getCssText(true)).append(
					"\";");
		}
		if (shadowChanged) {
			this.currentShadow_ = this.painter_.getShadow();
			this.js_.append("ctx.shadowOffsetX=").append(
					String.valueOf(this.currentShadow_.getOffsetX())).append(
					';').append("ctx.shadowOffsetY=").append(
					String.valueOf(this.currentShadow_.getOffsetY())).append(
					';').append("ctx.shadowBlur=").append(
					String.valueOf(this.currentShadow_.getBlur())).append(';')
					.append("ctx.shadowColor=\"").append(
							this.currentShadow_.getColor().getCssText(true))
					.append("\";");
		}
		if (fontChanged) {
			this.currentFont_ = this.painter_.getFont();
			switch (this.textMethod_) {
			case Html5Text:
				this.js_.append("ctx.font='").append(
						this.getPainter().getFont().getCssText()).append("';");
				break;
			case MozText:
				this.js_.append("ctx.mozTextStyle = '").append(
						this.getPainter().getFont().getCssText()).append("';");
				break;
			case DomText:
				break;
			}
		}
		this.changeFlags_.clear();
	}

	private void drawPlainPath(StringWriter out, WPainterPath path) {
		char[] buf = new char[30];
		if (!this.busyWithPath_) {
			out.append("ctx.beginPath();");
			this.busyWithPath_ = true;
		}
		List<WPainterPath.Segment> segments = path.getSegments();
		if (segments.size() > 0
				&& segments.get(0).getType() != WPainterPath.Segment.Type.MoveTo) {
			out.append("ctx.moveTo(0,0);");
		}
		for (int i = 0; i < segments.size(); ++i) {
			final WPainterPath.Segment s = segments.get(i);
			switch (s.getType()) {
			case MoveTo:
				out.append("ctx.moveTo(").append(
						MathUtils.round(
								s.getX() + this.pathTranslation_.getX(), 3));
				out.append(',').append(
						MathUtils.round(
								s.getY() + this.pathTranslation_.getY(), 3))
						.append(");");
				break;
			case LineTo:
				out.append("ctx.lineTo(").append(
						MathUtils.round(
								s.getX() + this.pathTranslation_.getX(), 3));
				out.append(',').append(
						MathUtils.round(
								s.getY() + this.pathTranslation_.getY(), 3))
						.append(");");
				break;
			case CubicC1:
				out.append("ctx.bezierCurveTo(").append(
						MathUtils.round(
								s.getX() + this.pathTranslation_.getX(), 3));
				out.append(',').append(
						MathUtils.round(
								s.getY() + this.pathTranslation_.getY(), 3));
				break;
			case CubicC2:
				out.append(',').append(
						MathUtils.round(
								s.getX() + this.pathTranslation_.getX(), 3))
						.append(',');
				out.append(MathUtils.round(s.getY()
						+ this.pathTranslation_.getY(), 3));
				break;
			case CubicEnd:
				out.append(',').append(
						MathUtils.round(
								s.getX() + this.pathTranslation_.getX(), 3))
						.append(',');
				out.append(
						MathUtils.round(
								s.getY() + this.pathTranslation_.getY(), 3))
						.append(");");
				break;
			case ArcC:
				out.append("ctx.arc(").append(
						MathUtils.round(
								s.getX() + this.pathTranslation_.getX(), 3))
						.append(',');
				out.append(MathUtils.round(s.getY()
						+ this.pathTranslation_.getY(), 3));
				break;
			case ArcR:
				out.append(',').append(MathUtils.round(s.getX(), 3));
				break;
			case ArcAngleSweep: {
				WPointF r = normalizedDegreesToRadians(s.getX(), s.getY());
				out.append(',').append(MathUtils.round(r.getX(), 3));
				out.append(',').append(MathUtils.round(r.getY(), 3));
				out.append(',').append(s.getY() > 0 ? "true" : "false").append(
						");");
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
				out.append("ctx.bezierCurveTo(")
						.append(
								MathUtils.round(cp1x
										+ this.pathTranslation_.getX(), 3))
						.append(',');
				out
						.append(
								MathUtils.round(cp1y
										+ this.pathTranslation_.getY(), 3))
						.append(',');
				out
						.append(
								MathUtils.round(cp2x
										+ this.pathTranslation_.getX(), 3))
						.append(',');
				out.append(MathUtils.round(cp2y + this.pathTranslation_.getY(),
						3));
				break;
			}
			case QuadEnd:
				out.append(',').append(
						MathUtils.round(
								s.getX() + this.pathTranslation_.getX(), 3))
						.append(',');
				out.append(
						MathUtils.round(
								s.getY() + this.pathTranslation_.getY(), 3))
						.append(");");
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

	static boolean fequal(double d1, double d2) {
		return Math.abs(d1 - d2) < 1E-5;
	}
}
