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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * <i><b>Note: </b>Older browsers do not have text support in &lt;canvas&gt;.
 * Text is then rendered in an overlayed DIV and a consequence text is not
 * subject to rotation and scaling components of the current transformation (but
 * does take into account translation). On most browser you can use the
 * {@link WSvgImage} or {@link WVmlImage} paint devices which do support text
 * natively. </i>
 * </p>
 */
public class WCanvasPaintDevice extends WObject implements WPaintDevice {
	private static Logger logger = LoggerFactory
			.getLogger(WCanvasPaintDevice.class);

	/**
	 * Create a canvas paint device.
	 */
	public WCanvasPaintDevice(final WLength width, final WLength height,
			WObject parent, boolean paintUpdate) {
		super(parent);
		this.width_ = width;
		this.height_ = height;
		this.painter_ = null;
		this.changeFlags_ = EnumSet.noneOf(WPaintDevice.ChangeFlag.class);
		this.paintUpdate_ = paintUpdate;
		this.busyWithPath_ = false;
		this.currentTransform_ = new WTransform();
		this.currentBrush_ = new WBrush();
		this.currentPen_ = new WPen();
		this.currentShadow_ = new WShadow();
		this.currentFont_ = new WFont();
		this.pathTranslation_ = new WPointF();
		this.fontMetrics_ = null;
		this.js_ = new StringWriter();
		this.recordedJs_ = new StringWriter();
		this.textElements_ = new ArrayList<DomElement>();
		this.images_ = new ArrayList<String>();
		this.textMethod_ = WCanvasPaintDevice.TextMethod.Html5Text;
		WApplication app = WApplication.getInstance();
		if (app != null) {
			if (app.getEnvironment().agentIsChrome()) {
				if (app.getEnvironment().getAgent().getValue() <= WEnvironment.UserAgent.Chrome2
						.getValue()) {
					this.textMethod_ = WCanvasPaintDevice.TextMethod.DomText;
				}
			} else {
				if (app.getEnvironment().agentIsGecko()) {
					if (app.getEnvironment().getAgent().getValue() < WEnvironment.UserAgent.Firefox3_0
							.getValue()) {
						this.textMethod_ = WCanvasPaintDevice.TextMethod.DomText;
					} else {
						if (app.getEnvironment().getAgent().getValue() < WEnvironment.UserAgent.Firefox3_5
								.getValue()) {
							this.textMethod_ = WCanvasPaintDevice.TextMethod.MozText;
						}
					}
				} else {
					if (app.getEnvironment().agentIsSafari()) {
						if (app.getEnvironment().getAgent() == WEnvironment.UserAgent.Safari3) {
							this.textMethod_ = WCanvasPaintDevice.TextMethod.DomText;
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
	 * {@link #WCanvasPaintDevice(WLength width, WLength height, WObject parent, boolean paintUpdate)
	 * this(width, height, (WObject)null, false)}
	 */
	public WCanvasPaintDevice(final WLength width, final WLength height) {
		this(width, height, (WObject) null, false);
	}

	/**
	 * Create a canvas paint device.
	 * <p>
	 * Calls
	 * {@link #WCanvasPaintDevice(WLength width, WLength height, WObject parent, boolean paintUpdate)
	 * this(width, height, parent, false)}
	 */
	public WCanvasPaintDevice(final WLength width, final WLength height,
			WObject parent) {
		this(width, height, parent, false);
	}

	public EnumSet<WPaintDevice.FeatureFlag> getFeatures() {
		if (ServerSideFontMetrics.isAvailable()) {
			return EnumSet.of(WPaintDevice.FeatureFlag.HasFontMetrics);
		} else {
			return EnumSet.noneOf(WPaintDevice.FeatureFlag.class);
		}
	}

	public void setChanged(EnumSet<WPaintDevice.ChangeFlag> flags) {
		this.changeFlags_.addAll(flags);
	}

	public final void setChanged(WPaintDevice.ChangeFlag flag,
			WPaintDevice.ChangeFlag... flags) {
		setChanged(EnumSet.of(flag, flags));
	}

	public void drawArc(final WRectF rect, double startAngle, double spanAngle) {
		this.finishPath();
		if (rect.getWidth() < EPSILON || rect.getHeight() < EPSILON) {
			return;
		}
		this.renderStateChanges(true);
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
			lw = this
					.getPainter()
					.normalizedPenWidth(this.getPainter().getPen().getWidth(),
							true).getValue()
					* 1 / Math.min(sx, sy);
			r = rect.getHeight() / 2;
		}
		final WPen pen = this.getPainter().getPen();
		if (pen.getStyle() != PenStyle.NoPen) {
			lw = this.getPainter().normalizedPenWidth(pen.getWidth(), true)
					.getValue()
					* 1 / Math.min(sx, sy);
		} else {
			lw = 0;
		}
		r = Math.max(0.005, r - lw / 2);
		char[] buf = new char[30];
		this.js_.append("ctx.save();").append("ctx.translate(")
				.append(MathUtils.roundJs(rect.getCenter().getX(), 3));
		this.js_.append(",").append(
				MathUtils.roundJs(rect.getCenter().getY(), 3));
		this.js_.append(");").append("ctx.scale(")
				.append(MathUtils.roundJs(sx, 3));
		this.js_.append(",").append(MathUtils.roundJs(sy, 3)).append(");");
		this.js_.append("ctx.lineWidth = ").append(MathUtils.roundJs(lw, 3))
				.append(";").append("ctx.beginPath();");
		this.js_.append("ctx.arc(0,0,").append(MathUtils.roundJs(r, 3));
		this.js_.append(',').append(MathUtils.roundJs(ra.getX(), 3));
		this.js_.append(",").append(MathUtils.roundJs(ra.getY(), 3))
				.append(",true);");
		this.js_.append("ctx.restore();");
		if (this.painter_.getBrush().getStyle() != BrushStyle.NoBrush) {
			this.js_.append("ctx.fill();");
		}
		if (this.painter_.getPen().getStyle() != PenStyle.NoPen) {
			this.js_.append("ctx.stroke();");
		}
	}

	public void drawImage(final WRectF rect, final String imageUri,
			int imgWidth, int imgHeight, final WRectF sourceRect) {
		this.finishPath();
		this.renderStateChanges(true);
		WApplication app = WApplication.getInstance();
		String imgUri = "";
		if (app != null) {
			imgUri = app.resolveRelativeUrl(imageUri);
		}
		int imageIndex = this.createImage(imgUri);
		char[] buf = new char[30];
		this.js_.append("ctx.drawImage(images[")
				.append(String.valueOf(imageIndex)).append("],")
				.append(MathUtils.roundJs(sourceRect.getX(), 3));
		this.js_.append(',').append(MathUtils.roundJs(sourceRect.getY(), 3));
		this.js_.append(',')
				.append(MathUtils.roundJs(sourceRect.getWidth(), 3));
		this.js_.append(',').append(
				MathUtils.roundJs(sourceRect.getHeight(), 3));
		this.js_.append(',').append(MathUtils.roundJs(rect.getX(), 3));
		this.js_.append(',').append(MathUtils.roundJs(rect.getY(), 3));
		this.js_.append(',').append(MathUtils.roundJs(rect.getWidth(), 3));
		this.js_.append(',').append(MathUtils.roundJs(rect.getHeight(), 3))
				.append(");");
	}

	public void drawLine(double x1, double y1, double x2, double y2) {
		WPainterPath path = new WPainterPath();
		path.moveTo(x1, y1);
		path.lineTo(x2, y2);
		this.drawPath(path);
	}

	public void drawPath(final WPainterPath path) {
		this.renderStateChanges(false);
		if (path.isJavaScriptBound()) {
			this.finishPath();
			this.js_.append("Wt3_3_4.gfxUtils.drawPath(ctx,")
					.append(path.getJsRef()).append(",")
					.append(this.currentNoBrush_ ? "false" : "true")
					.append(",").append(this.currentNoPen_ ? "false" : "true")
					.append(");");
		} else {
			this.drawPlainPath(this.js_, path);
			if (this.painter_.getBrush().getColor().getAlpha() != 255
					|| this.painter_.getBrush().isJavaScriptBound()
					|| this.painter_.getPen().getColor().getAlpha() != 255
					|| this.painter_.getPen().isJavaScriptBound()) {
				this.finishPath();
			}
		}
	}

	public void drawRect(final WRectF rectangle) {
		this.renderStateChanges(false);
		this.js_.append("Wt3_3_4").append(".gfxUtils.drawRect(ctx,")
				.append(rectangle.getJsRef()).append(",")
				.append(this.currentNoBrush_ ? "false" : "true").append(",")
				.append(this.currentNoPen_ ? "false" : "true").append(");");
	}

	public void drawText(final WRectF rect, EnumSet<AlignmentFlag> flags,
			TextFlag textFlag, final CharSequence text, WPointF clipPoint) {
		if (textFlag == TextFlag.TextWordWrap) {
			throw new WException(
					"WCanvasPaintDevice::drawText() TextWordWrap is not supported");
		}
		AlignmentFlag horizontalAlign = EnumUtils.enumFromSet(EnumUtils.mask(
				flags, AlignmentFlag.AlignHorizontalMask));
		AlignmentFlag verticalAlign = EnumUtils.enumFromSet(EnumUtils.mask(
				flags, AlignmentFlag.AlignVerticalMask));
		if (this.textMethod_ != WCanvasPaintDevice.TextMethod.DomText) {
			this.finishPath();
			this.renderStateChanges(true);
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
			if (this.currentPen_.isJavaScriptBound()) {
				this.js_.append("ctx.fillStyle=Wt3_3_4.gfxUtils.css_text(")
						.append(this.currentPen_.getJsRef()).append(".color);");
			} else {
				if (!this.currentBrush_.getColor().equals(
						this.currentPen_.getColor())
						|| this.currentBrush_.isJavaScriptBound()) {
					this.js_.append("ctx.fillStyle=")
							.append(WWebWidget.jsStringLiteral(this.currentPen_
									.getColor().getCssText(true))).append(";");
				}
			}
			char[] buf = new char[30];
			if (rect.isJavaScriptBound()) {
				String s_x = "";
				String s_y = "";
				switch (horizontalAlign) {
				case AlignLeft:
					s_x = "Wt3_3_4.gfxUtils.rect_left(" + rect.getJsRef() + ')';
					break;
				case AlignRight:
					s_x = "Wt3_3_4.gfxUtils.rect_right(" + rect.getJsRef()
							+ ')';
					break;
				case AlignCenter:
					s_x = "Wt3_3_4.gfxUtils.rect_center(" + rect.getJsRef()
							+ ").x";
					break;
				default:
					break;
				}
				switch (verticalAlign) {
				case AlignTop:
					s_y = "Wt3_3_4.gfxUtils.rect_top(" + rect.getJsRef() + ')';
					break;
				case AlignBottom:
					s_y = "Wt3_3_4.gfxUtils.rect_bottom(" + rect.getJsRef()
							+ ')';
					break;
				case AlignMiddle:
					s_y = "Wt3_3_4.gfxUtils.rect_center(" + rect.getJsRef()
							+ ").y";
					break;
				default:
					break;
				}
				if (clipPoint != null && this.getPainter() != null) {
					this.js_.append("if (Wt3_3_4.gfxUtils.pnpoly(")
							.append(this.getPainter().getWorldTransform()
									.map(clipPoint).getJsRef())
							.append(",")
							.append(this.getPainter().getClipPathTransform()
									.map(this.getPainter().getClipPath())
									.getJsRef()).append(")) {");
				}
				this.js_.append("ctx.fillText(")
						.append(WString.toWString(text).getJsStringLiteral())
						.append(',').append(s_x).append(',').append(s_y)
						.append(");");
				if (clipPoint != null && this.getPainter() != null) {
					this.js_.append("}");
				}
			} else {
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
				if (clipPoint != null && this.getPainter() != null) {
					this.js_.append("if (Wt3_3_4.gfxUtils.pnpoly(")
							.append(this.getPainter().getWorldTransform()
									.map(clipPoint).getJsRef())
							.append(",")
							.append(this.getPainter().getClipPathTransform()
									.map(this.getPainter().getClipPath())
									.getJsRef()).append(")) {");
				}
				this.js_.append("ctx.fillText(")
						.append(WString.toWString(text).getJsStringLiteral())
						.append(',').append(MathUtils.roundJs(x, 3))
						.append(',');
				this.js_.append(MathUtils.roundJs(y, 3)).append(");");
				if (clipPoint != null && this.getPainter() != null) {
					this.js_.append("}");
				}
			}
			if (this.currentBrush_.isJavaScriptBound()) {
				this.js_.append("ctx.fillStyle=Wt3_3_4.gfxUtils.css_text(")
						.append(this.currentBrush_.getJsRef())
						.append(".color);");
			} else {
				if (!this.currentBrush_.getColor().equals(
						this.currentPen_.getColor())
						|| this.currentPen_.isJavaScriptBound()) {
					this.js_.append("ctx.fillStyle=")
							.append(WWebWidget
									.jsStringLiteral(this.currentBrush_
											.getColor().getCssText(true)))
							.append(";");
				}
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
			this.js_.append("ctx.translate(").append(x).append(", ")
					.append(String.valueOf(y)).append(");");
			if (this.currentPen_.isJavaScriptBound()) {
				this.js_.append("ctx.fillStyle=Wt3_3_4.gfxUtils.css_text(")
						.append(this.currentPen_.getJsRef()).append(".color);");
			} else {
				if (!this.currentBrush_.getColor().equals(
						this.currentPen_.getColor())
						|| this.currentBrush_.isJavaScriptBound()) {
					this.js_.append("ctx.fillStyle=")
							.append(WWebWidget.jsStringLiteral(this.currentPen_
									.getColor().getCssText(true))).append(";");
				}
			}
			this.js_.append("ctx.mozDrawText(")
					.append(WString.toWString(text).getJsStringLiteral())
					.append(");");
			this.js_.append("ctx.restore();");
		}
			break;
		case DomText: {
			WPointF pos = this.getPainter().getCombinedTransform()
					.map(rect.getTopLeft());
			DomElement e = DomElement.createNew(DomElementType.DomElement_DIV);
			e.setProperty(Property.PropertyStylePosition, "absolute");
			e.setProperty(Property.PropertyStyleTop, String.valueOf(pos.getY())
					+ "px");
			e.setProperty(Property.PropertyStyleLeft,
					String.valueOf(pos.getX()) + "px");
			e.setProperty(Property.PropertyStyleWidth,
					String.valueOf(rect.getWidth()) + "px");
			e.setProperty(Property.PropertyStyleHeight,
					String.valueOf(rect.getHeight()) + "px");
			DomElement t = e;
			if (verticalAlign != AlignmentFlag.AlignTop) {
				t = DomElement.createNew(DomElementType.DomElement_DIV);
				if (verticalAlign == AlignmentFlag.AlignMiddle) {
					e.setProperty(Property.PropertyStyleDisplay, "table");
					t.setProperty(Property.PropertyStyleDisplay, "table-cell");
					t.setProperty(Property.PropertyStyleVerticalAlign, "middle");
				} else {
					if (verticalAlign == AlignmentFlag.AlignBottom) {
						t.setProperty(Property.PropertyStylePosition,
								"absolute");
						t.setProperty(Property.PropertyStyleWidth, "100%");
						t.setProperty(Property.PropertyStyleBottom, "0px");
					}
				}
			}
			t.setProperty(Property.PropertyInnerHTML,
					WWebWidget.escapeText(text, true).toString());
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

	public WTextItem measureText(final CharSequence text, double maxWidth,
			boolean wordWrap) {
		if (!(this.fontMetrics_ != null)) {
			this.fontMetrics_ = new ServerSideFontMetrics();
		}
		return this.fontMetrics_.measureText(this.getPainter().getFont(), text,
				maxWidth, wordWrap);
	}

	public final WTextItem measureText(final CharSequence text) {
		return measureText(text, -1, false);
	}

	public final WTextItem measureText(final CharSequence text, double maxWidth) {
		return measureText(text, maxWidth, false);
	}

	public WFontMetrics getFontMetrics() {
		if (!(this.fontMetrics_ != null)) {
			this.fontMetrics_ = new ServerSideFontMetrics();
		}
		return this.fontMetrics_.fontMetrics(this.getPainter().getFont());
	}

	public void init() {
		this.currentBrush_ = new WBrush();
		this.currentNoBrush_ = false;
		this.currentPen_ = new WPen();
		this.currentNoPen_ = false;
		this.currentPen_.setCapStyle(PenCapStyle.FlatCap);
		this.currentShadow_ = new WShadow();
		this.currentFont_ = new WFont();
		this.currentTextVAlign_ = this.currentTextHAlign_ = AlignmentFlag.AlignSuper;
		this.changeFlags_.clear();
	}

	public void done() {
		this.finishPath();
	}

	public boolean isPaintActive() {
		return this.painter_ != null;
	}

	void render(final String canvasId, DomElement text) {
		String canvasVar = "Wt3_3_4.getElement('" + canvasId + "')";
		this.recordedJs_ = new StringWriter();
		final StringWriter tmp = this.recordedJs_;
		tmp.append("if(").append(canvasVar).append(".getContext){");
		if (!this.images_.isEmpty()) {
			tmp.append("new Wt._p_.ImagePreloader([");
			for (int i = 0; i < this.images_.size(); ++i) {
				if (i != 0) {
					tmp.append(',');
				}
				tmp.append('\'').append(this.images_.get(i)).append('\'');
			}
			tmp.append("],function(images)");
		}
		tmp.append("{var ctx=").append(canvasVar).append(".getContext('2d');");
		tmp.append("if (!ctx.setLineDash) {ctx.setLineDash = function(a){};}");
		if (!this.paintUpdate_) {
			tmp.append("ctx.clearRect(0,0,")
					.append(String.valueOf(this.getWidth().getValue()))
					.append(",")
					.append(String.valueOf(this.getHeight().getValue()))
					.append(");");
		}
		tmp.append("ctx.save();ctx.save();").append(this.js_.toString())
				.append("ctx.restore();ctx.restore();}");
		if (!this.images_.isEmpty()) {
			tmp.append(");");
		}
		tmp.append("}");
		for (int i = 0; i < this.textElements_.size(); ++i) {
			text.addChild(this.textElements_.get(i));
		}
	}

	public void renderPaintCommands(final StringWriter js_target,
			final String canvasElement) {
		js_target.append("var ctx=").append(canvasElement)
				.append(".getContext('2d');");
		js_target
				.append("if (!ctx.setLineDash) {ctx.setLineDash = function(a){};}");
		js_target.append("ctx.save();ctx.save();").append(this.js_.toString())
				.append("ctx.restore();ctx.restore();");
	}

	public WLength getWidth() {
		return this.width_;
	}

	public WLength getHeight() {
		return this.height_;
	}

	public WPainter getPainter() {
		return this.painter_;
	}

	public void setPainter(WPainter painter) {
		this.painter_ = painter;
	}

	enum TextMethod {
		MozText, Html5Text, DomText;

		/**
		 * Returns the numerical representation of this enum.
		 */
		public int getValue() {
			return ordinal();
		}
	}

	private WLength width_;
	private WLength height_;
	private WPainter painter_;
	private EnumSet<WPaintDevice.ChangeFlag> changeFlags_;
	private boolean paintUpdate_;
	private WCanvasPaintDevice.TextMethod textMethod_;
	private boolean busyWithPath_;
	private boolean currentNoPen_;
	private boolean currentNoBrush_;
	private WTransform currentTransform_;
	private WBrush currentBrush_;
	private WPen currentPen_;
	private WShadow currentShadow_;
	private WFont currentFont_;
	private WPointF pathTranslation_;
	private AlignmentFlag currentTextHAlign_;
	private AlignmentFlag currentTextVAlign_;
	private ServerSideFontMetrics fontMetrics_;
	private StringWriter js_;
	StringWriter recordedJs_;
	private List<DomElement> textElements_;
	private List<String> images_;

	private void finishPath() {
		if (this.busyWithPath_) {
			if (!this.currentNoBrush_) {
				this.js_.append("ctx.fill();");
			}
			if (!this.currentNoPen_) {
				this.js_.append("ctx.stroke();");
			}
			this.js_.append('\n');
			this.busyWithPath_ = false;
		}
	}

	private void renderTransform(final StringWriter s, final WTransform t) {
		if (t.isJavaScriptBound()) {
			s.append("ctx.transform.apply(ctx, ").append(t.getJsRef())
					.append(");");
		} else {
			if (!t.isIdentity()) {
				char[] buf = new char[30];
				s.append("ctx.transform(")
						.append(MathUtils.roundJs(t.getM11(), 3)).append(",");
				s.append(MathUtils.roundJs(t.getM12(), 3)).append(",");
				s.append(MathUtils.roundJs(t.getM21(), 3)).append(",");
				s.append(MathUtils.roundJs(t.getM22(), 3)).append(",");
				s.append(MathUtils.roundJs(t.getM31(), 3)).append(",");
				s.append(MathUtils.roundJs(t.getM32(), 3)).append(");");
			}
		}
	}

	private void renderStateChanges(boolean resetPathTranslation) {
		if (resetPathTranslation
				&& (!fequal(this.pathTranslation_.getX(), 0) || !fequal(
						this.pathTranslation_.getY(), 0))) {
			this.changeFlags_.add(WPaintDevice.ChangeFlag.Transform);
		}
		if (!!this.changeFlags_.isEmpty()) {
			return;
		}
		WApplication app = WApplication.getInstance();
		boolean slowFirefox = app != null
				&& app.getEnvironment().agentIsGecko();
		if (slowFirefox
				&& app.getEnvironment().getUserAgent().indexOf("Linux") == -1) {
			slowFirefox = false;
		}
		boolean brushChanged = !EnumUtils.mask(this.changeFlags_,
				WPaintDevice.ChangeFlag.Brush).isEmpty()
				&& !this.currentBrush_.equals(this.getPainter().getBrush())
				&& (slowFirefox || this.getPainter().getBrush().getStyle() != BrushStyle.NoBrush);
		boolean penChanged = !EnumUtils.mask(this.changeFlags_,
				WPaintDevice.ChangeFlag.Pen).isEmpty()
				&& !this.currentPen_.equals(this.getPainter().getPen())
				&& (slowFirefox || this.getPainter().getPen().getStyle() != PenStyle.NoPen);
		boolean penColorChanged = penChanged
				&& (this.getPainter().getPen().isJavaScriptBound()
						|| !this.currentPen_.getColor().equals(
								this.getPainter().getPen().getColor()) || !this.currentPen_
						.getGradient().equals(
								this.getPainter().getPen().getGradient()));
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
				final WTransform t = this.getPainter().getClipPathTransform();
				this.renderTransform(this.js_, t);
				if (this.getPainter().hasClipping()) {
					this.pathTranslation_.setX(0);
					this.pathTranslation_.setY(0);
					if (this.getPainter().getClipPath().isJavaScriptBound()) {
						this.js_.append("Wt3_3_4")
								.append(".gfxUtils.drawPath(ctx,")
								.append(this.getPainter().getClipPath()
										.getJsRef())
								.append(",false,false,true);");
					} else {
						this.drawPlainPath(this.js_, this.getPainter()
								.getClipPath());
						this.js_.append("ctx.clip();");
					}
					this.busyWithPath_ = false;
				}
				this.renderTransform(this.js_, t.getInverted());
				this.js_.append("ctx.save();");
				resetTransform = true;
			} else {
				if (!EnumUtils.mask(this.changeFlags_,
						WPaintDevice.ChangeFlag.Transform).isEmpty()) {
					WTransform f = this.getPainter().getCombinedTransform();
					resetTransform = !this.currentTransform_.equals(f);
					if (this.busyWithPath_) {
						if (!this.getPainter().getBrush().getGradient()
								.isEmpty()
								|| !this.getPainter().getPen().getGradient()
										.isEmpty()) {
							resetTransform = true;
						} else {
							if (!this.currentTransform_.isJavaScriptBound()
									&& !f.isJavaScriptBound()
									&& fequal(f.getM11(),
											this.currentTransform_.getM11())
									&& fequal(f.getM12(),
											this.currentTransform_.getM12())
									&& fequal(f.getM21(),
											this.currentTransform_.getM21())
									&& fequal(f.getM22(),
											this.currentTransform_.getM22())) {
								double det = f.getM11() * f.getM22()
										- f.getM12() * f.getM21();
								double a11 = f.getM22() / det;
								double a12 = -f.getM12() / det;
								double a21 = -f.getM21() / det;
								double a22 = f.getM11() / det;
								double fdx = f.getDx() * a11 + f.getDy() * a21;
								double fdy = f.getDx() * a12 + f.getDy() * a22;
								final WTransform g = this.currentTransform_;
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
					} else {
						if (!resetTransform) {
							if (!fequal(this.pathTranslation_.getX(), 0)
									|| !fequal(this.pathTranslation_.getY(), 0)) {
								resetTransform = true;
							}
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
				this.currentTextHAlign_ = this.currentTextVAlign_ = AlignmentFlag.AlignSuper;
				this.init();
			}
		}
		boolean newNoPen = this.getPainter().getPen().getStyle() == PenStyle.NoPen;
		boolean newNoBrush = this.getPainter().getBrush().getStyle() == BrushStyle.NoBrush;
		if (penChanged || brushChanged || shadowChanged
				|| newNoBrush != this.currentNoBrush_
				|| newNoPen != this.currentNoPen_) {
			this.finishPath();
		}
		this.currentNoPen_ = this.getPainter().getPen().getStyle() == PenStyle.NoPen;
		this.currentNoBrush_ = this.getPainter().getBrush().getStyle() == BrushStyle.NoBrush;
		if (penChanged) {
			if (penColorChanged) {
				WPen tmpPen = new WPen();
				tmpPen.setColor(this.getPainter().getPen().getColor());
				tmpPen.setGradient(this.getPainter().getPen().getGradient());
				this.currentPen_ = tmpPen;
				if (!this.getPainter().getPen().getGradient().isEmpty()) {
					String gradientName = defineGradient(this.getPainter()
							.getPen().getGradient(), this.js_);
					this.js_.append("ctx.strokeStyle=").append(gradientName)
							.append(";");
					this.renderStateChanges(true);
				} else {
					if (this.getPainter().getPen().isJavaScriptBound()) {
						this.js_.append(
								"ctx.strokeStyle=Wt3_3_4.gfxUtils.css_text(")
								.append(this.getPainter().getPen().getJsRef())
								.append(".color);");
					} else {
						this.js_.append("ctx.strokeStyle=")
								.append(WWebWidget.jsStringLiteral(this
										.getPainter().getPen().getColor()
										.getCssText(true))).append(";");
					}
				}
			}
			switch (this.getPainter().getPen().getStyle()) {
			case SolidLine:
				this.js_.append("ctx.setLineDash([]);");
				break;
			case DashLine:
				this.js_.append("ctx.setLineDash([4,2]);");
				break;
			case DotLine:
				this.js_.append("ctx.setLineDash([1,2]);");
				break;
			case DashDotLine:
				this.js_.append("ctx.setLineDash([4,2,1,2]);");
				break;
			case DashDotDotLine:
				this.js_.append("ctx.setLineDash([4,2,1,2,1,2]);");
				break;
			case NoPen:
				break;
			}
			this.js_.append("ctx.lineWidth=")
					.append(String
							.valueOf(this
									.getPainter()
									.normalizedPenWidth(
											this.getPainter().getPen()
													.getWidth(), true)
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
			if (!this.currentBrush_.getGradient().isEmpty()) {
				String gradientName = defineGradient(
						this.currentBrush_.getGradient(), this.js_);
				this.js_.append("ctx.fillStyle=").append(gradientName)
						.append(";");
				this.renderStateChanges(true);
			} else {
				if (this.currentBrush_.isJavaScriptBound()) {
					this.js_.append("ctx.fillStyle=Wt3_3_4.gfxUtils.css_text(")
							.append(this.currentBrush_.getJsRef())
							.append(".color);");
				} else {
					this.js_.append("ctx.fillStyle=")
							.append(WWebWidget
									.jsStringLiteral(this.currentBrush_
											.getColor().getCssText(true)))
							.append(";");
				}
			}
		}
		if (shadowChanged) {
			this.currentShadow_ = this.painter_.getShadow();
			this.js_.append("ctx.shadowOffsetX=")
					.append(String.valueOf(this.currentShadow_.getOffsetX()))
					.append(';')
					.append("ctx.shadowOffsetY=")
					.append(String.valueOf(this.currentShadow_.getOffsetY()))
					.append(';')
					.append("ctx.shadowBlur=")
					.append(String.valueOf(this.currentShadow_.getBlur()))
					.append(';')
					.append("ctx.shadowColor=")
					.append(WWebWidget.jsStringLiteral(this.currentShadow_
							.getColor().getCssText(true))).append(";");
		}
		if (fontChanged) {
			this.currentFont_ = this.painter_.getFont();
			switch (this.textMethod_) {
			case Html5Text:
				this.js_.append("ctx.font=")
						.append(WWebWidget.jsStringLiteral(this.getPainter()
								.getFont().getCssText())).append(";");
				break;
			case MozText:
				this.js_.append("ctx.mozTextStyle = ")
						.append(WWebWidget.jsStringLiteral(this.getPainter()
								.getFont().getCssText())).append(";");
				break;
			case DomText:
				break;
			}
		}
		this.changeFlags_.clear();
	}

	// private void resetPathTranslation() ;
	private void drawPlainPath(final StringWriter out, final WPainterPath path) {
		char[] buf = new char[30];
		if (!this.busyWithPath_) {
			out.append("ctx.beginPath();");
			this.busyWithPath_ = true;
		}
		final List<WPainterPath.Segment> segments = path.getSegments();
		if (segments.size() > 0
				&& segments.get(0).getType() != WPainterPath.Segment.Type.MoveTo) {
			out.append("ctx.moveTo(0,0);");
		}
		for (int i = 0; i < segments.size(); ++i) {
			final WPainterPath.Segment s = segments.get(i);
			switch (s.getType()) {
			case MoveTo:
				out.append("ctx.moveTo(").append(
						MathUtils.roundJs(
								s.getX() + this.pathTranslation_.getX(), 3));
				out.append(',')
						.append(MathUtils.roundJs(s.getY()
								+ this.pathTranslation_.getY(), 3))
						.append(");");
				break;
			case LineTo:
				out.append("ctx.lineTo(").append(
						MathUtils.roundJs(
								s.getX() + this.pathTranslation_.getX(), 3));
				out.append(',')
						.append(MathUtils.roundJs(s.getY()
								+ this.pathTranslation_.getY(), 3))
						.append(");");
				break;
			case CubicC1:
				out.append("ctx.bezierCurveTo(").append(
						MathUtils.roundJs(
								s.getX() + this.pathTranslation_.getX(), 3));
				out.append(',').append(
						MathUtils.roundJs(
								s.getY() + this.pathTranslation_.getY(), 3));
				break;
			case CubicC2:
				out.append(',')
						.append(MathUtils.roundJs(s.getX()
								+ this.pathTranslation_.getX(), 3)).append(',');
				out.append(MathUtils.roundJs(
						s.getY() + this.pathTranslation_.getY(), 3));
				break;
			case CubicEnd:
				out.append(',')
						.append(MathUtils.roundJs(s.getX()
								+ this.pathTranslation_.getX(), 3)).append(',');
				out.append(
						MathUtils.roundJs(
								s.getY() + this.pathTranslation_.getY(), 3))
						.append(");");
				break;
			case ArcC:
				out.append("ctx.arc(")
						.append(MathUtils.roundJs(s.getX()
								+ this.pathTranslation_.getX(), 3)).append(',');
				out.append(MathUtils.roundJs(
						s.getY() + this.pathTranslation_.getY(), 3));
				break;
			case ArcR:
				out.append(',').append(
						MathUtils.roundJs(Math.max(0.0, s.getX()), 3));
				break;
			case ArcAngleSweep: {
				WPointF r = normalizedDegreesToRadians(s.getX(), s.getY());
				out.append(',').append(MathUtils.roundJs(r.getX(), 3));
				out.append(',').append(MathUtils.roundJs(r.getY(), 3));
				out.append(',').append(s.getY() > 0 ? "true" : "false")
						.append(");");
			}
				break;
			case QuadC: {
				final double cpx = s.getX();
				final double cpy = s.getY();
				out.append("ctx.quadraticCurveTo(")
						.append(MathUtils.roundJs(
								cpx + this.pathTranslation_.getX(), 3))
						.append(',');
				out.append(MathUtils.roundJs(
						cpy + this.pathTranslation_.getY(), 3));
				break;
			}
			case QuadEnd:
				out.append(',')
						.append(MathUtils.roundJs(s.getX()
								+ this.pathTranslation_.getX(), 3)).append(',');
				out.append(
						MathUtils.roundJs(
								s.getY() + this.pathTranslation_.getY(), 3))
						.append(");");
			}
		}
	}

	private int createImage(final String imgUri) {
		this.images_.add(imgUri);
		return this.images_.size() - 1;
	}

	WCanvasPaintDevice.TextMethod getTextMethod() {
		return this.textMethod_;
	}

	private static final double EPSILON = 1E-5;

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

	static String defineGradient(final WGradient gradient, final StringWriter js) {
		String jsRef = "grad";
		if (gradient.getStyle() == GradientStyle.LinearGradient) {
			final WLineF gradVec = gradient.getLinearGradientVector();
			js.append("var ").append(jsRef)
					.append(" = ctx.createLinearGradient(")
					.append(String.valueOf(gradVec.getX1())).append(", ")
					.append(String.valueOf(gradVec.getY1())).append(", ")
					.append(String.valueOf(gradVec.getX2())).append(", ")
					.append(String.valueOf(gradVec.getY2())).append(");");
		} else {
			if (gradient.getStyle() == GradientStyle.RadialGradient) {
				js.append("var ")
						.append(jsRef)
						.append(" = ctx.createRadialGradient(")
						.append(String.valueOf(gradient.getRadialFocalPoint()
								.getX()))
						.append(", ")
						.append(String.valueOf(gradient.getRadialFocalPoint()
								.getY()))
						.append(",")
						.append("0, ")
						.append(String.valueOf(gradient.getRadialCenterPoint()
								.getX()))
						.append(", ")
						.append(String.valueOf(gradient.getRadialCenterPoint()
								.getY())).append(", ")
						.append(String.valueOf(gradient.getRadialRadius()))
						.append(");");
			}
		}
		for (int i = 0; i < gradient.getColorstops().size(); i++) {
			js.append(jsRef)
					.append(".addColorStop(")
					.append(String.valueOf(gradient.getColorstops().get(i)
							.getPosition()))
					.append(",")
					.append(WWebWidget.jsStringLiteral(gradient.getColorstops()
							.get(i).getColor().getCssText(true))).append(");");
		}
		return jsRef;
	}
}
