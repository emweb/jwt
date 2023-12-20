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
import java.time.*;
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
  InnerHTML,
  AddedInnerHTML,
  Value,
  Disabled,
  Checked,
  Selected,
  SelectedIndex,
  Multiple,
  Target,
  Download,
  Indeterminate,
  Src,
  ColSpan,
  RowSpan,
  ReadOnly,
  TabIndex,
  Label,
  Class,
  Placeholder,
  Orient,
  Style,
  StyleWidthExpression,
  StylePosition,
  StyleZIndex,
  StyleFloat,
  StyleClear,
  StyleWidth,
  StyleHeight,
  StyleLineHeight,
  StyleMinWidth,
  StyleMinHeight,
  StyleMaxWidth,
  StyleMaxHeight,
  StyleLeft,
  StyleRight,
  StyleTop,
  StyleBottom,
  StyleVerticalAlign,
  StyleTextAlign,
  StylePadding,
  StylePaddingTop,
  StylePaddingRight,
  StylePaddingBottom,
  StylePaddingLeft,
  StyleMargin,
  StyleMarginTop,
  StyleMarginRight,
  StyleMarginBottom,
  StyleMarginLeft,
  StyleCursor,
  StyleBorderTop,
  StyleBorderRight,
  StyleBorderBottom,
  StyleBorderLeft,
  StyleBorderColorTop,
  StyleBorderColorRight,
  StyleBorderColorBottom,
  StyleBorderColorLeft,
  StyleBorderWidthTop,
  StyleBorderWidthRight,
  StyleBorderWidthBottom,
  StyleBorderWidthLeft,
  StyleColor,
  StyleOverflowX,
  StyleOverflowY,
  StyleOpacity,
  StyleFontFamily,
  StyleFontStyle,
  StyleFontVariant,
  StyleFontWeight,
  StyleFontSize,
  StyleBackgroundColor,
  StyleBackgroundImage,
  StyleBackgroundRepeat,
  StyleBackgroundAttachment,
  StyleBackgroundPosition,
  StyleTextDecoration,
  StyleWhiteSpace,
  StyleTableLayout,
  StyleBorderSpacing,
  StyleBorderCollapse,
  StylePageBreakBefore,
  StylePageBreakAfter,
  StyleZoom,
  StyleVisibility,
  StyleDisplay,
  StyleWebkitAppearance,
  StyleBoxSizing,
  StyleFlex,
  StyleFlexDirection,
  StyleFlexFlow,
  StyleAlignSelf,
  StyleJustifyContent,
  LastPlusOne;

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return ordinal();
  }
}
