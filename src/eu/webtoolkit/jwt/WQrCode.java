/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import eu.webtoolkit.jwt.auth.*;
import eu.webtoolkit.jwt.auth.mfa.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.servlet.*;
import eu.webtoolkit.jwt.thirdparty.qrcodegen.*;
import eu.webtoolkit.jwt.utils.*;
import java.io.*;
import java.lang.ref.*;
import java.time.*;
import java.util.*;
import java.util.regex.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A widget representing a QR code.
 *
 * <p>This widget allows to generate QR code from a string.
 *
 * <p>If you wish to alter the look of the QR code (to add a logo in the middle for instance), you
 * can override paintEvent. However, you will still need to call the {@link
 * WQrCode#paintEvent(WPaintDevice paintDevice) paintEvent()} to generate the QR code itself.
 *
 * <p>The content that is encoded is taken as-is. If the purpose of your QR code requires special
 * formatting, the formatted string needs to be supplied to the constructor.
 *
 * <p>By default, the generated QR code will use the lowest possible error correction, and a square
 * size of 5 pixels.
 *
 * <p>Usage example:
 *
 * <pre>{@code
 * String website = "https://webtoolkit.eu";
 * WApplication app = WApplication.getInstance();
 * app.getRoot().addWidget(new WQrCode(website));
 *
 * }</pre>
 */
public class WQrCode extends WPaintedWidget {
  private static Logger logger = LoggerFactory.getLogger(WQrCode.class);

  /**
   * Represents the different error correction levels for QR codes.
   *
   * <p>The error correction level impacts how much erroneous squares the QR code can contains
   * without becoming unreadable.
   *
   * <p>For most digital use, the default of LOW is sufficient. Higher levels can be considered for
   * printed out media.
   *
   * <p>
   *
   * @see WQrCode#setErrorCorrectionLevel(WQrCode.ErrorCorrectionLevel ecl)
   */
  public enum ErrorCorrectionLevel {
    /** About 7% of incorrect squares is tolerated. */
    LOW(0),
    /** About 15% of incorrect squares is tolerated. */
    MEDIUM(1),
    /** About 25% of incorrect squares is tolerated. */
    QUARTILE(2),
    /** About 30% of incorrect squares is tolerated. */
    HIGH(3);

    private int value;

    ErrorCorrectionLevel(int value) {
      this.value = value;
    }

    /** Returns the numerical representation of this enum. */
    public int getValue() {
      return value;
    }
  }
  /** Creates a default QR code with an empty message. */
  public WQrCode(WContainerWidget parentContainer) {
    super();
    this.errCorrLvl_ = WQrCode.ErrorCorrectionLevel.LOW;
    this.msg_ = "";
    this.code_ = null;
    this.squareSize_ = 5.0;
    this.brush_ = new WBrush();
    this.init();
    if (parentContainer != null) parentContainer.addWidget(this);
  }
  /**
   * Creates a default QR code with an empty message.
   *
   * <p>Calls {@link #WQrCode(WContainerWidget parentContainer) this((WContainerWidget)null)}
   */
  public WQrCode() {
    this((WContainerWidget) null);
  }
  /**
   * Creates a QR code.
   *
   * <p>Create a QR code with the given message, and square size.
   *
   * <p>
   *
   * @see WQrCode#setMessage(String message)
   * @see WQrCode#setSquareSize(double size)
   */
  public WQrCode(final String message, double squareSize, WContainerWidget parentContainer) {
    super();
    this.errCorrLvl_ = WQrCode.ErrorCorrectionLevel.LOW;
    this.msg_ = message;
    this.code_ = null;
    this.squareSize_ = squareSize;
    this.brush_ = new WBrush();
    this.init();
    if (parentContainer != null) parentContainer.addWidget(this);
  }
  /**
   * Creates a QR code.
   *
   * <p>Calls {@link #WQrCode(String message, double squareSize, WContainerWidget parentContainer)
   * this(message, squareSize, (WContainerWidget)null)}
   */
  public WQrCode(final String message, double squareSize) {
    this(message, squareSize, (WContainerWidget) null);
  }
  /**
   * Creates a QR code.
   *
   * <p>Create a QR code with the given message, error correction level and square size.
   *
   * <p>
   *
   * @see WQrCode#setMessage(String message)
   * @see WQrCode#setErrorCorrectionLevel(WQrCode.ErrorCorrectionLevel ecl)
   * @see WQrCode#setSquareSize(double size)
   */
  public WQrCode(
      final String message,
      WQrCode.ErrorCorrectionLevel ecl,
      double squareSize,
      WContainerWidget parentContainer) {
    super();
    this.errCorrLvl_ = ecl;
    this.msg_ = message;
    this.code_ = null;
    this.squareSize_ = squareSize;
    this.brush_ = new WBrush();
    this.init();
    if (parentContainer != null) parentContainer.addWidget(this);
  }
  /**
   * Creates a QR code.
   *
   * <p>Calls {@link #WQrCode(String message, WQrCode.ErrorCorrectionLevel ecl, double squareSize,
   * WContainerWidget parentContainer) this(message, WQrCode.ErrorCorrectionLevel.LOW, 5.0,
   * (WContainerWidget)null)}
   */
  public WQrCode(final String message) {
    this(message, WQrCode.ErrorCorrectionLevel.LOW, 5.0, (WContainerWidget) null);
  }
  /**
   * Creates a QR code.
   *
   * <p>Calls {@link #WQrCode(String message, WQrCode.ErrorCorrectionLevel ecl, double squareSize,
   * WContainerWidget parentContainer) this(message, ecl, 5.0, (WContainerWidget)null)}
   */
  public WQrCode(final String message, WQrCode.ErrorCorrectionLevel ecl) {
    this(message, ecl, 5.0, (WContainerWidget) null);
  }
  /**
   * Creates a QR code.
   *
   * <p>Calls {@link #WQrCode(String message, WQrCode.ErrorCorrectionLevel ecl, double squareSize,
   * WContainerWidget parentContainer) this(message, ecl, squareSize, (WContainerWidget)null)}
   */
  public WQrCode(final String message, WQrCode.ErrorCorrectionLevel ecl, double squareSize) {
    this(message, ecl, squareSize, (WContainerWidget) null);
  }
  /**
   * Set the error correction level of the QR code.
   *
   * <p>Increases the amount of information that can be lost before altering the encoded message. A
   * higher level of error correcting code, makes the QR code more robust to changes.
   *
   * <p>Increasing the error correction level also increases the amount of data needed to encode the
   * message, resulting in a visually bigger QR code than one with a lower error correcting level.
   *
   * <p>By default, {@link WQrCode.ErrorCorrectionLevel#LOW LOW} is used.
   *
   * <p>
   *
   * @see WQrCode#setMessage(String message)
   */
  public void setErrorCorrectionLevel(WQrCode.ErrorCorrectionLevel ecl) {
    if (this.errCorrLvl_ != ecl) {
      this.errCorrLvl_ = ecl;
      this.generateCode();
    }
  }
  /**
   * Returns the error correction level of the QR code.
   *
   * <p>
   *
   * @see WQrCode#setErrorCorrectionLevel(WQrCode.ErrorCorrectionLevel ecl)
   */
  public WQrCode.ErrorCorrectionLevel getErrorCorrectionLevel() {
    return this.errCorrLvl_;
  }
  /**
   * Set the message of the QR code.
   *
   * <p>This sets the message carried by the QR code. There is a limit to the size of the message
   * which depends on many factors. The most important one is the error correction level. Higher
   * error correction level diminish the maximum size the message can have. Roughly speaking when
   * using the highest level of error correction, the maximum number of allowed content is in the
   * area of 1Kb, whereas for the lowest level of error correction, this is around 3Kb.
   *
   * <p>The longer the message is, the more data needs to be encoded, and thus the bigger the
   * resulting QR code becomes.
   *
   * <p>If the message is to long, the QR code will not be generated.
   *
   * <p>
   *
   * @see WQrCode#setErrorCorrectionLevel(WQrCode.ErrorCorrectionLevel ecl)
   * @see WQrCode#isError()
   */
  public void setMessage(final String message) {
    if (!this.msg_.equals(message)) {
      this.msg_ = message;
      this.generateCode();
    }
  }
  /**
   * Returns the message of the QR code.
   *
   * <p>
   *
   * @see WQrCode#setMessage(String message)
   */
  public String getMessage() {
    return this.msg_;
  }
  /**
   * Set the size of the dots composing the QR code.
   *
   * <p>Sets the size (in pixels) that each dot of the QR code will have. A single square of the QR
   * code can be seen in any corner of the QR code. There a reference visual is always displayed
   * that contains exactly seven dots in width and height.
   *
   * <p>This allows the application to correctly resize the QR code.
   */
  public void setSquareSize(double size) {
    if (this.squareSize_ != size) {
      this.squareSize_ = size;
      this.updateSize();
    }
  }
  /**
   * Returns the size of the squares composing the QR code.
   *
   * <p>
   *
   * @see WQrCode#setSquareSize(double size)
   */
  public double getSquareSize() {
    return this.squareSize_;
  }
  /**
   * Returns whether an error stopped the generation of the QR code.
   *
   * <p>Returns true if the QR code could not be generated due to an error.
   *
   * <p>In case the QR code was not generated, it&apos;s size will be set to 0 and the QR code will
   * not be painted.
   */
  public boolean isError() {
    return !(this.getCode() != null);
  }
  /**
   * Sets the brush with which the QR code is painted.
   *
   * <p>This allows for the color of the QR code to be changed.
   */
  public void setBrush(final WBrush brush) {
    this.brush_ = brush;
    this.update();
  }
  /**
   * Returns the brush with which the QR code is painted.
   *
   * <p>
   *
   * @see WQrCode#setBrush(WBrush brush)
   */
  public WBrush getBrush() {
    return this.brush_;
  }

  protected void paintEvent(WPaintDevice paintDevice) {
    WPainter painter = new WPainter(paintDevice);
    if (this.getCode() != null) {
      for (int line = 0; line < this.getCode().getSize(); ++line) {
        for (int column = 0; column < this.getCode().getSize(); ++column) {
          if (this.getCode().getModule(column, line)) {
            painter.fillRect(
                line * this.getSquareSize(),
                column * this.getSquareSize(),
                this.getSquareSize(),
                this.getSquareSize(),
                this.brush_);
          }
        }
      }
    }
  }

  private WQrCode.ErrorCorrectionLevel errCorrLvl_;
  private String msg_;
  private QrCode code_;
  private double squareSize_;
  private WBrush brush_;

  private void init() {
    this.brush_ = new WBrush(StandardColor.Black);
    this.generateCode();
  }

  private QrCode getCode() {
    return this.code_;
  }

  private void generateCode() {
    try {
      QrCode code = QrCode.encodeText(this.msg_, this.getEcc());
      this.code_ = code;
    } catch (final RuntimeException e) {
      this.code_ = null;
      logger.error(
          new StringWriter()
              .append("Error while generating QR code: ")
              .append(e.toString())
              .toString());
    }
    this.updateSize();
  }

  private void updateSize() {
    double size = this.getCode() != null ? this.getCode().getSize() * this.squareSize_ : 0;
    this.resize(new WLength(size), new WLength(size));
    this.update();
  }

  private Ecc getEcc() {
    Ecc res;
    switch (this.errCorrLvl_) {
      case LOW:
        res = Ecc.LOW;
        break;
      case MEDIUM:
        res = Ecc.MEDIUM;
        break;
      case QUARTILE:
        res = Ecc.QUARTILE;
        break;
      case HIGH:
        res = Ecc.HIGH;
        break;
      default:
        throw new WException("Invalid ErrorCorrectionLevel");
    }
    return res;
  }
}
