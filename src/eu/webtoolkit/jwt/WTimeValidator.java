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
 * A time validator.
 *
 * <p>
 *
 * @see WTimeEdit
 * @see WTime
 */
public class WTimeValidator extends WRegExpValidator {
  private static Logger logger = LoggerFactory.getLogger(WTimeValidator.class);

  /** Creates a new {@link WTimeValidator}. */
  public WTimeValidator() {
    super();
    this.formats_ = new ArrayList<String>();
    this.bottom_ = null;
    this.top_ = null;
    this.step_ = Duration.ZERO;
    this.tooEarlyText_ = new WString();
    this.tooLateText_ = new WString();
    this.notATimeText_ = new WString();
    this.wrongStepText_ = new WString();
    this.setFormat(WTime.getDefaultFormat());
  }
  /** Creates a new {@link WTimeValidator}. */
  public WTimeValidator(final String format) {
    super();
    this.formats_ = new ArrayList<String>();
    this.bottom_ = null;
    this.top_ = null;
    this.step_ = Duration.ZERO;
    this.tooEarlyText_ = new WString();
    this.tooLateText_ = new WString();
    this.notATimeText_ = new WString();
    this.wrongStepText_ = new WString();
    this.setFormat(format);
  }
  /**
   * Creates a new {@link WTimeValidator}.
   *
   * <p>The validator will accept only times within the indicated range <i>bottom</i> to <i>top</i>,
   * in the time formate <code>format</code>
   */
  public WTimeValidator(final String format, final WTime bottom, final WTime top) {
    super();
    this.formats_ = new ArrayList<String>();
    this.bottom_ = bottom;
    this.top_ = top;
    this.step_ = Duration.ZERO;
    this.tooEarlyText_ = new WString();
    this.tooLateText_ = new WString();
    this.notATimeText_ = new WString();
    this.wrongStepText_ = new WString();
    this.setFormat(format);
  }
  /** Sets the validator format. */
  public void setFormat(final String format) {
    if (this.formats_.isEmpty() || !this.formats_.get(0).equals(format)) {
      this.formats_.clear();
      this.formats_.add(format);
      this.repaint();
    }
  }
  /** Returns the validator current format. */
  public String getFormat() {
    return this.formats_.get(0);
  }
  /** Sets the time formats used to parse time strings. */
  public void setFormats(final List<String> formats) {
    Utils.copyList(formats, this.formats_);
    this.repaint();
  }
  /** Returns the time formats used to parse time strings. */
  public List<String> getFormats() {
    return this.formats_;
  }
  /**
   * Sets the lower limit of the valid time range.
   *
   * <p>The default is a null time constructed using WTime()
   */
  public void setBottom(final WTime bottom) {
    if (!(this.bottom_ == bottom || (this.bottom_ != null && this.bottom_.equals(bottom)))) {
      this.bottom_ = bottom;
      this.repaint();
    }
  }
  /** Returns the lower limit of the valid time range. */
  public WTime getBottom() {
    return this.bottom_;
  }
  /**
   * Sets the upper limit of the valid time range.
   *
   * <p>The default is a null time constructed using WTime()
   */
  public void setTop(final WTime top) {
    if (!(this.top_ == top || (this.top_ != null && this.top_.equals(top)))) {
      this.top_ = top;
      this.repaint();
    }
  }
  /** Returns the upper limit of the valid time range. */
  public WTime getTop() {
    return this.top_;
  }
  /**
   * Sets the step (in seconds) between two valid values.
   *
   * <p>The default value is 0 seconds, meaning any step is accepted.
   *
   * <p>When the native HTML5 control is used, this sets the step to 1 or 60 automatically,
   * denepding on the format, respectively HH:mm, or HH:mm:ss. Changing this value has no effect.
   */
  public void setStep(final Duration step) {
    if (step.compareTo(Duration.ofSeconds(0)) < 0) {
      logger.error(
          new StringWriter()
              .append("WTimeValidator::setStep(): ignoring call, value should not be negative")
              .toString());
      return;
    }
    if (!this.step_.equals(step)) {
      this.step_ = step;
      this.repaint();
    }
  }
  /** Returns the step (in seconds) between two valid values. */
  public Duration getStep() {
    return this.step_;
  }
  /** Sets the message to display when the input is not a time. */
  public void setInvalidNotATimeText(final CharSequence text) {
    this.notATimeText_ = WString.toWString(text);
  }
  /** Returns the message displayed when the input is not a time. */
  public WString getInvalidNotATimeText() {
    if (!(this.notATimeText_.length() == 0)) {
      return this.notATimeText_.clone().arg(this.formats_.get(0));
    } else {
      return WString.tr("Wt.WTimeValidator.WrongFormat").arg(this.formats_.get(0));
    }
  }
  /** Sets the message to display when the time is earlier than bottom. */
  public void setInvalidTooEarlyText(final CharSequence text) {
    this.tooEarlyText_ = WString.toWString(text);
    this.repaint();
  }
  /** Returns the message displayed when time is too early. */
  public WString getInvalidTooEarlyText() {
    if (!(this.tooEarlyText_.length() == 0)) {
      return this.tooEarlyText_
          .clone()
          .arg(this.bottom_.toString(this.formats_.get(0)))
          .arg(this.top_.toString(this.formats_.get(0)));
    } else {
      if ((this.bottom_ == null)) {
        return new WString();
      } else {
        if ((this.top_ == null)) {
          return WString.tr("Wt.WTimeValidator.TimeTooEarly")
              .arg(this.bottom_.toString(this.formats_.get(0)));
        } else {
          return WString.tr("Wt.WTimeValidator.WrongTimeRange")
              .arg(this.bottom_.toString(this.formats_.get(0)))
              .arg(this.top_.toString(this.formats_.get(0)));
        }
      }
    }
  }
  /** Sets the message to display when the time is later than top. */
  public void setInvalidTooLateText(final CharSequence text) {
    this.tooLateText_ = WString.toWString(text);
    this.repaint();
  }
  /** Returns the message displayed when time is too late. */
  public WString getInvalidTooLateText() {
    if (!(this.tooLateText_.length() == 0)) {
      return this.tooLateText_
          .clone()
          .arg(this.bottom_.toString(this.formats_.get(0)))
          .arg(this.top_.toString(this.formats_.get(0)));
    } else {
      if ((this.top_ == null)) {
        return new WString();
      } else {
        if ((this.bottom_ == null)) {
          return WString.tr("Wt.WTimeValidator.TimeTooLate")
              .arg(this.top_.toString(this.formats_.get(0)));
        } else {
          return WString.tr("Wt.WTimeValidator.WrongTimeRange")
              .arg(this.bottom_.toString(this.formats_.get(0)))
              .arg(this.top_.toString(this.formats_.get(0)));
        }
      }
    }
  }
  /** Sets the message to display when the time increment is invalid. */
  public void setInvalidWrongStepText(final CharSequence text) {
    this.wrongStepText_ = WString.toWString(text);
    this.repaint();
  }
  /** Returns the message displayed when the time increment is invalid. */
  public WString getInvalidWrongStepText() {
    if (this.step_.compareTo(Duration.ofSeconds(0)) <= 0) {
      return WString.Empty;
    } else {
      if (!(this.wrongStepText_.length() == 0)) {
        return this.wrongStepText_;
      } else {
        if (this.step_.getSeconds() % 60 == 0) {
          return WString.tr("Wt.WTimeValidator.WrongStep-minutes")
              .arg(this.step_.getSeconds() / 60);
        } else {
          if (this.step_.getSeconds() % 1 == 0) {
            return WString.tr("Wt.WTimeValidator.WrongStep-seconds")
                .arg(this.step_.getSeconds() / 1);
          } else {
            return WString.tr("Wt.WTimeValidator.WrongStep");
          }
        }
      }
    }
  }
  /**
   * Validates the given input.
   *
   * <p>The input is considered valid only when it is blank for a non-mandatory field, or represents
   * a time in the given format, and within the valid range.
   */
  public WValidator.Result validate(final String input) {
    if (input.length() == 0) {
      return super.validate(input);
    }
    for (int i = 0; i < this.formats_.size(); i++) {
      try {
        WTime t = WTime.fromString(input, this.formats_.get(i));
        if ((t != null && t.isValid())) {
          if (!(this.bottom_ == null) && t.before(this.bottom_)) {
            return new WValidator.Result(ValidationState.Invalid, this.getInvalidTooEarlyText());
          }
          if (!(this.top_ == null) && t.after(this.top_)) {
            return new WValidator.Result(ValidationState.Invalid, this.getInvalidTooLateText());
          }
          if (this.step_.compareTo(Duration.ofSeconds(0)) > 0) {
            WTime start = !(this.bottom_ == null) ? this.bottom_ : new WTime(0, 0);
            long secs = start.secsTo(t);
            if (secs % this.step_.getSeconds() != 0) {
              return new WValidator.Result(ValidationState.Invalid, this.getInvalidWrongStepText());
            }
          }
          return new WValidator.Result(ValidationState.Valid);
        }
      } catch (final RuntimeException e) {
        logger.warn(new StringWriter().append("validate(): ").append(e.toString()).toString());
      }
    }
    return new WValidator.Result(ValidationState.Invalid, this.getInvalidNotATimeText());
  }

  public String getJavaScriptValidate() {
    loadJavaScript(WApplication.getInstance());
    StringBuilder js = new StringBuilder();
    js.append("new Wt4_12_1.WTimeValidator(").append(this.isMandatory()).append(",[");
    for (int i = 0; i < this.formats_.size(); ++i) {
      WTime.RegExpInfo r = WTime.formatToRegExp(this.formats_.get(i));
      if (i != 0) {
        js.append(',');
      }
      js.append("{")
          .append("regexp:")
          .append(WWebWidget.jsStringLiteral(r.regexp))
          .append(',')
          .append("getHour:function(results){")
          .append(r.hourGetJS)
          .append(";},")
          .append("getMinutes:function(results){")
          .append(r.minuteGetJS)
          .append(";},")
          .append("getSeconds:function(results){")
          .append(r.secGetJS)
          .append(";},")
          .append("getMilliseconds:function(results){")
          .append(r.msecGetJS)
          .append(";},")
          .append("}");
    }
    js.append("],");
    if (!(this.bottom_ == null)) {
      js.append("new Date(0,0,0,")
          .append(this.bottom_.getHour())
          .append(",")
          .append(this.bottom_.getMinute())
          .append(",")
          .append(this.bottom_.getSecond())
          .append(",")
          .append(this.bottom_.getMsec())
          .append(")");
    } else {
      js.append("null");
    }
    js.append(',');
    if (!(this.top_ == null)) {
      js.append("new Date(0,0,0,")
          .append(this.top_.getHour())
          .append(",")
          .append(this.top_.getMinute())
          .append(",")
          .append(this.top_.getSecond())
          .append(",")
          .append(this.top_.getMsec())
          .append(")");
    } else {
      js.append("null");
    }
    js.append(',');
    if (this.step_.compareTo(Duration.ofSeconds(0)) > 0) {
      js.append((int) this.step_.getSeconds());
    } else {
      js.append("null");
    }
    js.append(',')
        .append(WString.toWString(this.getInvalidBlankText()).getJsStringLiteral())
        .append(',')
        .append(WString.toWString(this.getInvalidNotATimeText()).getJsStringLiteral())
        .append(',')
        .append(WString.toWString(this.getInvalidTooEarlyText()).getJsStringLiteral())
        .append(',')
        .append(WString.toWString(this.getInvalidTooLateText()).getJsStringLiteral())
        .append(',')
        .append(WString.toWString(this.getInvalidWrongStepText()).getJsStringLiteral())
        .append(");");
    return js.toString();
  }

  private List<String> formats_;
  private WTime bottom_;
  private WTime top_;
  private Duration step_ = Duration.ofSeconds(0);
  private WString tooEarlyText_;
  private WString tooLateText_;
  private WString notATimeText_;
  private WString wrongStepText_;

  private static void loadJavaScript(WApplication app) {
    app.loadJavaScript("js/WTimeValidator.js", wtjs1());
  }

  static WJavaScriptPreamble wtjs1() {
    return new WJavaScriptPreamble(
        JavaScriptScope.WtClassScope,
        JavaScriptObjectType.JavaScriptConstructor,
        "WTimeValidator",
        "(function(e,t,i,n,s,a,r,g,l,o){this.validate=function(u){if(0===u.length)return e?{valid:!1,message:a}:{valid:!0};let d=null,f=-1,m=-1,c=-1,v=-1;for(const e of t){d=new RegExp(\"^\"+e.regexp+\"$\").exec(u);if(null!==d){f=e.getHour(d);u.toUpperCase().indexOf(\"P\")>-1&&f<12?f+=12:u.toUpperCase().indexOf(\"A\")>-1&&12===f&&(f=0);m=e.getMinutes(d);c=e.getSeconds(d);v=e.getMilliseconds(d);break}}if(null===d)return{valid:!1,message:r};if(f<0||f>23||m<0||m>59||c<0||c>59||v<0||v>999)return{valid:!1,message:r};const T=new Date(0,0,0,f,m,c,v);if(T.getHours()!==f||T.getMinutes()!==m||T.getSeconds()!==c||T.getMilliseconds()!==v)return{valid:!1,message:r};if(i&&T.getTime()<i.getTime())return{valid:!1,message:g};if(n&&T.getTime()>n.getTime())return{valid:!1,message:l};if(s){const e=i?i.getTime():new Date(0,0,0).getTime();if(Math.round((T.getTime()-e)/1e3)%s!=0)return{valid:!1,message:o}}return{valid:!0}}})");
  }
}
