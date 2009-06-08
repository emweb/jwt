package eu.webtoolkit.jwt.examples.style;

import java.util.EnumSet;

import eu.webtoolkit.jwt.PositionScheme;
import eu.webtoolkit.jwt.Side;
import eu.webtoolkit.jwt.WColor;
import eu.webtoolkit.jwt.WCompositeWidget;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WCssDecorationStyle;
import eu.webtoolkit.jwt.WLength;

/**
 * A widget with rounded corners.
 * 
 * This widgets represents a widget for which any combination of its four corners may be rounded. Although rounded
 * corners is not a standard part of the CSS specification, this widget will be rendered identical on all platforms.
 * 
 * The contents of the widget is managed inside a WContainerWidget, which is accessed using the contents() method.
 * 
 * The radius of the rounded corners, the background color of the image, and the surrounding color may be changed at all
 * times.
 * 
 * The RoundedWidget is part of the %Wt style example.
 * 
 * @see CornerImage
 */
public class RoundedWidget extends WCompositeWidget {

	public enum Corner {
		TopLeft, TopRight, BottomLeft, BottomRight;
	}

	public static EnumSet<Corner> allCorners = EnumSet.of(Corner.TopLeft, Corner.TopRight, Corner.BottomLeft, Corner.BottomRight);

	/**
	 * Construct a widget with any combination of its corners rounded.
	 */
	public RoundedWidget(EnumSet<Corner> corners, WContainerWidget parent) {
		super(parent);
		backgroundColor_ = new WColor(0xD4, 0xDD, 0xFF);
		surroundingColor_ = new WColor(0xFF, 0xFF, 0xFF);
		radius_ = 10;
		corners_ = corners;

		setImplementation(impl_ = new WContainerWidget());

		contents_ = new WContainerWidget(impl_);

		create();
	}

	/**
	 * Set the widget background color.
	 * 
	 * Because the background color also affects the color of the corner images, the background color cannot be set
	 * using the WCssDecorationStyle() of the widget.
	 */
	public void setBackgroundColor(WColor color) {
		backgroundColor_ = color;
		adjust();
	}

	/**
	 * Get the widget background color.
	 */
	public WColor backgroundColor() {
		return backgroundColor_;
	}

	/**
	 * Show or hide rounded corners.
	 */
	public void enableRoundedCorners(boolean how) {
		if (images_[0] != null)
			images_[0].setHidden(!how);
		if (images_[2] != null)
			images_[2].setHidden(!how);

		if (images_[1] != null) {
			images_[1].setHidden(!how);
			if (!how)
				top_.getDecorationStyle().setBackgroundImage("");
			else
				top_.getDecorationStyle().setBackgroundImage(images_[1].getImageRef(), WCssDecorationStyle.Repeat.NoRepeat, Side.Top, Side.Right);
		}

		if (images_[3] != null) {
			images_[3].setHidden(!how);
			if (!how)
				bottom_.getDecorationStyle().setBackgroundImage("");
			else
				bottom_.getDecorationStyle().setBackgroundImage(images_[3].getImageRef(), WCssDecorationStyle.Repeat.NoRepeat, Side.Top, Side.Right);
		}
	}

	/**
	 * Set the corner radius of the widget.
	 */
	public void setCornerRadius(int radius) {
		radius_ = radius;
		adjust();
	}

	/**
	 * Get the corner radius of the widget.
	 */
	public int cornerRadius() {
		return radius_;
	}

	/**
	 * Set the surrounding color of the widget.
	 * 
	 * This color will be used "outside" the corner, in each of the corner images.
	 */
	public void setSurroundingColor(WColor color) {
		surroundingColor_ = color;
		adjust();
	}

	/**
	 * Get the surrounding color of the widget.
	 */
	public WColor surroundingColor() {
		return surroundingColor_;
	}

	/**
	 * Access the contents container.
	 * 
	 * The contents WContainerWidget represents the contents inside the rounded widget.
	 */
	public WContainerWidget contents() {
		return contents_;
	}

	/**
	 * Background color
	 */
	private WColor backgroundColor_;

	/**
	 * "Surrounding" color -- maybe we can use a transparent color ?
	 */
	private WColor surroundingColor_;

	/**
	 * Radius
	 */
	private int radius_;

	/**
	 * EnumSet<Corner> of the corners which are to be rounded.
	 */
	private EnumSet<Corner> corners_;

	/**
	 * The container widget in which to store the contents.
	 */
	private WContainerWidget contents_;

	/**
	 * This composite widget is implemented as a WContainerWidget
	 */
	private WContainerWidget impl_;

	/**
	 * A container at the top which renders the top rounding
	 */
	private WContainerWidget top_;

	/**
	 * A container at the bottom renders the bottom rounding
	 */
	private WContainerWidget bottom_;

	/**
	 * Up to four CornerImages for each corner.
	 */
	private CornerImage[] images_ = new CornerImage[4];

	/**
	 * Create the implementation.
	 */
	private void create() {
		if (corners_.contains(Corner.TopLeft)) {
			images_[0] = new CornerImage(backgroundColor_, surroundingColor_, radius_, null, CornerImage.Position.Top, CornerImage.Position.Left);
			images_[0].setPositionScheme(PositionScheme.Absolute);
		} else
			images_[0] = null;

		if (corners_.contains(Corner.TopRight))
			images_[1] = new CornerImage(backgroundColor_, surroundingColor_, radius_, null, CornerImage.Position.Top, CornerImage.Position.Right);
		else
			images_[1] = null;

		if (corners_.contains(Corner.BottomLeft)) {
			images_[2] = new CornerImage(backgroundColor_, surroundingColor_, radius_, null, CornerImage.Position.Bottom, CornerImage.Position.Left);
			images_[2].setPositionScheme(PositionScheme.Absolute);
		} else
			images_[2] = null;

		if (corners_.contains(Corner.BottomRight))
			images_[3] = new CornerImage(backgroundColor_, surroundingColor_, radius_, null, CornerImage.Position.Bottom, CornerImage.Position.Right);
		else
			images_[3] = null;

		/*
		 * At the top: an image (top left corner) inside a container widget with background image top right.
		 */
		top_ = new WContainerWidget();
		top_.resize(new WLength(), new WLength(radius_));
		top_.setPositionScheme(PositionScheme.Relative);
		if (images_[1] != null)
			top_.getDecorationStyle().setBackgroundImage(images_[1].getImageRef(), WCssDecorationStyle.Repeat.NoRepeat, Side.Top, Side.Right);

		if (images_[0] != null)
			top_.addWidget(images_[0]);
		impl_.insertBefore(top_, contents_); // insert top before the contents

		/*
		 * At the bottom: an image (bottom left corner) inside a container widget with background image bottom right.
		 */
		bottom_ = new WContainerWidget();
		bottom_.setPositionScheme(PositionScheme.Relative);
		bottom_.resize(new WLength(), new WLength(radius_));
		if (images_[3] != null)
			bottom_.getDecorationStyle().setBackgroundImage(images_[3].getImageRef(), WCssDecorationStyle.Repeat.NoRepeat, Side.Bottom, Side.Right);
		if (images_[2] != null)
			bottom_.addWidget(images_[2]);
		impl_.addWidget(bottom_);

		getDecorationStyle().setBackgroundColor(backgroundColor_);

		contents_.setMargin(new WLength(radius_), Side.Left, Side.Right);
	}

	/**
	 * Adjust the image (colors and radius).
	 */
	private void adjust() {
		if (images_[0] != null && !images_[0].isHidden())
			images_[0].setRadius(radius_);
		if (images_[1] != null && !images_[1].isHidden())
			images_[1].setRadius(radius_);
		if (images_[2] != null && !images_[2].isHidden())
			images_[2].setRadius(radius_);
		if (images_[3] != null && !images_[3].isHidden())
			images_[3].setRadius(radius_);

		if (images_[0] != null && !images_[0].isHidden())
			images_[0].setForeground(backgroundColor_);
		if (images_[1] != null && !images_[1].isHidden())
			images_[1].setForeground(backgroundColor_);
		if (images_[2] != null && !images_[2].isHidden())
			images_[2].setForeground(backgroundColor_);
		if (images_[3] != null && !images_[3].isHidden())
			images_[3].setForeground(backgroundColor_);

		if (images_[1] != null)
			top_.getDecorationStyle().setBackgroundImage(images_[1].getImageRef(), WCssDecorationStyle.Repeat.NoRepeat, Side.Top, Side.Right);
		if (images_[3] != null)
			bottom_.getDecorationStyle().setBackgroundImage(images_[3].getImageRef(), WCssDecorationStyle.Repeat.NoRepeat, Side.Bottom, Side.Right);

		top_.resize(new WLength(), new WLength(radius_));
		bottom_.resize(new WLength(), new WLength(radius_));
		contents_.setMargin(radius_, Side.Left, Side.Right);

		getDecorationStyle().setBackgroundColor(backgroundColor_);
	}
}