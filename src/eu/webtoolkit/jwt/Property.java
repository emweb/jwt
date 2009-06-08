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

enum Property {
	PropertyInnerHTML, PropertyAddedInnerHTML, PropertyValue, PropertyDisabled, PropertyChecked, PropertySelected, PropertySelectedIndex, PropertyMultiple, PropertyTarget, PropertyIndeterminate, PropertySrc, PropertyText, PropertyScript, PropertyColSpan, PropertyRowSpan, PropertyReadOnly, PropertyStylePosition, PropertyStyleZIndex, PropertyStyleFloat, PropertyStyleClear, PropertyStyleWidth, PropertyStyleHeight, PropertyStyleLineHeight, PropertyStyleMinWidth, PropertyStyleMinHeight, PropertyStyleMaxWidth, PropertyStyleMaxHeight, PropertyStyleLeft, PropertyStyleRight, PropertyStyleTop, PropertyStyleBottom, PropertyStyleVerticalAlign, PropertyStyleTextAlign, PropertyStylePadding, PropertyStyleMarginTop, PropertyStyleMarginRight, PropertyStyleMarginBottom, PropertyStyleMarginLeft, PropertyStyleCursor, PropertyStyleBorderTop, PropertyStyleBorderRight, PropertyStyleBorderBottom, PropertyStyleBorderLeft, PropertyStyleColor, PropertyStyleOverflowX, PropertyStyleOverflowY, PropertyStyleFontFamily, PropertyStyleFontStyle, PropertyStyleFontVariant, PropertyStyleFontWeight, PropertyStyleFontSize, PropertyStyleBackgroundColor, PropertyStyleBackgroundImage, PropertyStyleBackgroundRepeat, PropertyStyleBackgroundAttachment, PropertyStyleBackgroundPosition, PropertyStyleTextDecoration, PropertyStyleWhiteSpace, PropertyStyleTableLayout, PropertyStyleBorderSpacing, PropertyStyleVisibility, PropertyStyleDisplay;

	public int getValue() {
		return ordinal();
	}
}
