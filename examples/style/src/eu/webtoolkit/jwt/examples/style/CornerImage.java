package eu.webtoolkit.jwt.examples.style;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.EnumSet;

import javax.imageio.ImageIO;

import eu.webtoolkit.jwt.WColor;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WImage;
import eu.webtoolkit.jwt.WMemoryResource;

/**
 * The CornerImage is an image to draw a rounded corner.
 * 
 * The CornerImage is a dynamically generated WImage, which draws an arc of 90°, to represent one of the four corners of
 * a widget.
 * 
 * The CornerImage is part of the JWt style example.
 * 
 * @see RoundedWidget
 */
public class CornerImage extends WImage {
	/**
	 * One of the four corners of a widget.
	 */
	public enum Position {
		Top, Bottom, Left, Right;
	}

	/**
	 * brief Construct a new CornerImage.
	 * 
	 * Construct a corner image, to draw the specified corner, with the given foreground and background color, and the
	 * specified radius.
	 * 
	 * The colors must be constructed using red/green/blue values, using {@link WColor#WColor(int, int, int)}.
	 */
	public CornerImage(WColor fg, WColor bg, int radius, WContainerWidget parent, Position corner, Position... corners) {
		super(parent);
		corner_ = EnumSet.of(corner, corners);
		fg_ = fg;
		bg_ = bg;
		radius_ = radius;
		resource_ = null;
		compute();
	}

	/**
	 * Change the corner radius (and image dimensions).
	 */
	public void setRadius(int radius) {
		if (radius != radius_) {
			radius_ = radius;
			compute();
		}
	}

	/**
	 * Get the corner radius.
	 */
	public int radius() {
		return radius_;
	}

	/**
	 * Change the foreground color.
	 */
	public void setForeground(WColor color) {
		if (!fg_.equals(color)) {
			fg_ = color;
			compute();
		}

	}

	/**
	 * Get the foreground color.
	 */
	public WColor foreground() {
		return fg_;
	}

	/**
	 * One of the four corners, which this image represents.
	 */
	private EnumSet<Position> corner_;

	/**
	 * Foreground color
	 */
	private WColor fg_;
	
	/**
	 * Background color
	 */
	private WColor bg_;

	/**
	 * Radius
	 */
	private int radius_;

	/**
	 * The resource which contains the generated image.
	 */
	private WMemoryResource resource_;

	/**
	 * Regenerate the image.
	 */
	private void compute() {
		BufferedImage imBig = new BufferedImage(radius_, radius_, BufferedImage.TYPE_INT_ARGB);

		Color foreGround = new Color(fg_.getRed(), fg_.getGreen(), fg_.getBlue());

		Graphics2D graphics = (Graphics2D) imBig.getGraphics();
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		//transparent background
		graphics.setColor(new Color(bg_.getRed(), bg_.getGreen(), bg_.getBlue()));
		graphics.fillRect(0, 0, radius_, radius_);

		int cx, cy;

		if (corner_.contains(Position.Top))
			cy = 0;
		else 
			cy = -radius_;

		if (corner_.contains(Position.Left)) {
			cx = 0;
			cy ++;
		}
		else
			cx = -radius_;

		graphics.setColor(foreGround);
		graphics.fillArc(cx, cy, (radius_ * 2), (radius_ * 2), 0, 360);

		ByteArrayOutputStream bas = new ByteArrayOutputStream();
		try {
			ImageIO.write(imBig, "png" , bas);
		} catch (IOException e) {
			e.printStackTrace();
		}
		byte[] vdata = bas.toByteArray();

		if (resource_==null) {
			resource_ = new WMemoryResource("image/png");
		} 
		resource_.setData(vdata);
		
		this.setResource(resource_);
	}
}
