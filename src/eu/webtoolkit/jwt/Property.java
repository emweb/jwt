/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.servlet.*;
import eu.webtoolkit.jwt.utils.*;
import java.io.*;
import java.lang.ref.*;
import java.util.*;
import java.util.regex.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * Enumeration for a DOM property.
 *
 * <p>This is an internal API, subject to change.
 */
public enum Property {
  PropertyInnerHTML,
  PropertyAddedInnerHTML,
  PropertyValue,
  PropertyDisabled,
  PropertyChecked,
  PropertySelected,
  PropertySelectedIndex,
  PropertyMultiple,
  PropertyTarget,
  PropertyDownload,
  PropertyIndeterminate,
  PropertySrc,
  PropertyColSpan,
  PropertyRowSpan,
  PropertyReadOnly,
  PropertyTabIndex,
  PropertyLabel,
  PropertyClass,
  PropertyPlaceholder,
  PropertyStyle,
  PropertyStyleWidthExpression,
  PropertyStylePosition,
  PropertyStyleZIndex,
  PropertyStyleFloat,
  PropertyStyleClear,
  PropertyStyleWidth,
  PropertyStyleHeight,
  PropertyStyleLineHeight,
  PropertyStyleMinWidth,
  PropertyStyleMinHeight,
  PropertyStyleMaxWidth,
  PropertyStyleMaxHeight,
  PropertyStyleLeft,
  PropertyStyleRight,
  PropertyStyleTop,
  PropertyStyleBottom,
  PropertyStyleVerticalAlign,
  PropertyStyleTextAlign,
  PropertyStylePadding,
  PropertyStylePaddingTop,
  PropertyStylePaddingRight,
  PropertyStylePaddingBottom,
  PropertyStylePaddingLeft,
  PropertyStyleMarginTop,
  PropertyStyleMarginRight,
  PropertyStyleMarginBottom,
  PropertyStyleMarginLeft,
  PropertyStyleCursor,
  PropertyStyleBorderTop,
  PropertyStyleBorderRight,
  PropertyStyleBorderBottom,
  PropertyStyleBorderLeft,
  PropertyStyleBorderColorTop,
  PropertyStyleBorderColorRight,
  PropertyStyleBorderColorBottom,
  PropertyStyleBorderColorLeft,
  PropertyStyleBorderWidthTop,
  PropertyStyleBorderWidthRight,
  PropertyStyleBorderWidthBottom,
  PropertyStyleBorderWidthLeft,
  PropertyStyleColor,
  PropertyStyleOverflowX,
  PropertyStyleOverflowY,
  PropertyStyleOpacity,
  PropertyStyleFontFamily,
  PropertyStyleFontStyle,
  PropertyStyleFontVariant,
  PropertyStyleFontWeight,
  PropertyStyleFontSize,
  PropertyStyleBackgroundColor,
  PropertyStyleBackgroundImage,
  PropertyStyleBackgroundRepeat,
  PropertyStyleBackgroundAttachment,
  PropertyStyleBackgroundPosition,
  PropertyStyleTextDecoration,
  PropertyStyleWhiteSpace,
  PropertyStyleTableLayout,
  PropertyStyleBorderSpacing,
  PropertyStyleBorderCollapse,
  PropertyStylePageBreakBefore,
  PropertyStylePageBreakAfter,
  PropertyStyleZoom,
  PropertyStyleVisibility,
  PropertyStyleDisplay,
  PropertyStyleBoxSizing,
  PropertyLastPlusOne;

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return ordinal();
  }
}
