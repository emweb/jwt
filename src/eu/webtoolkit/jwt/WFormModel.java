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
 * A basic model class for forms.
 *
 * <p>This implements field data and validation handling for (simple) form-based views. It provides
 * a standard way for views to perform field validation, and react to validation results.
 *
 * <p>All fields are uniquely identified using a string literal (which is the Field type). For each
 * field, its value, the visibility, whether the field is read-only, and its current validation
 * status is managed by the model. In addition, you will typically specialize the class to customize
 * the validation and application logic.
 *
 * <p>Although it can be setup to use {@link WValidator} objects for individual fields, also other
 * validation where more entered information needs to be considered simultaneously can be
 * implemented.
 *
 * <p>A model is typically used by a View which renders the fields configured in the model, updates
 * the model values, invokes and reflects the validation status.
 *
 * <p>Example (a bit contrived since you will usually not use the model directly):
 *
 * <pre>{@code
 * String NameField = "name";
 * String TelField = "telephone";
 *
 * WFormModel model = new WFormModel();
 * model.addField(NameField, "Enter your name");
 * model.addField(TelField, "Phone number");
 *
 * model.setValue(NameField, "John Doe");
 *
 * if (model.validate()) {
 * ...
 * } else {
 * WValidator.Result rname = model.getValidation(NameField);
 * if (rname.getState() != WValidator.State.Valid) {
 * System.err.println("Invalid name: " + rname.getMessage());
 * }
 * ...
 * }
 *
 * }</pre>
 */
public class WFormModel extends WObject {
  private static Logger logger = LoggerFactory.getLogger(WFormModel.class);

  /**
   * Constructor.
   *
   * <p>Creates a new form model.
   */
  public WFormModel() {
    super();
    this.fields_ = new HashMap<String, WFormModel.FieldData>();
  }
  /**
   * Adds a field.
   *
   * <p>The <code>field</code> is added to the model, with an optional short informational message
   * that can be used by views to provide a hint on the value that needs to be entered. The message
   * is set as the validation message as long as the field has not yet been validated.
   *
   * <p>If the <code>field</code> was already in the model, its data is reset.
   */
  public void addField(String field, final CharSequence info) {
    this.fields_.put(field, new WFormModel.FieldData());
    this.fields_.get(field).validation = new WValidator.Result(ValidationState.Invalid, info);
  }
  /**
   * Adds a field.
   *
   * <p>Calls {@link #addField(String field, CharSequence info) addField(field, WString.Empty)}
   */
  public final void addField(String field) {
    addField(field, WString.Empty);
  }
  /**
   * Removes a field.
   *
   * <p>The <code>field</code> is removed from the model.
   */
  public void removeField(String field) {
    this.fields_.remove(field);
  }
  /**
   * Returns the fields.
   *
   * <p>This returns the fields currently configured in the model (added with {@link
   * WFormModel#addField(String field, CharSequence info) addField()} or for which a value or
   * property has been set).
   */
  public List<String> getFields() {
    List<String> result = new ArrayList<String>();
    for (Iterator<Map.Entry<String, WFormModel.FieldData>> i_it =
            this.fields_.entrySet().iterator();
        i_it.hasNext(); ) {
      Map.Entry<String, WFormModel.FieldData> i = i_it.next();
      result.add(i.getKey());
    }
    return result;
  }
  /**
   * Resets the model.
   *
   * <p>The default implementation clears the value of all fields, and resets the validation state
   * to not validated.
   */
  public void reset() {
    for (Iterator<Map.Entry<String, WFormModel.FieldData>> i_it =
            this.fields_.entrySet().iterator();
        i_it.hasNext(); ) {
      Map.Entry<String, WFormModel.FieldData> i = i_it.next();
      i.getValue().value = null;
      i.getValue().validated = false;
    }
  }
  /**
   * Validates the current input.
   *
   * <p>The default implementation calls {@link WFormModel#validateField(String field)
   * validateField()} for each field and returns <code>true</code> if all fields validated.
   *
   * <p>
   *
   * @see WFormModel#validateField(String field)
   */
  public boolean validate() {
    boolean result = true;
    for (Iterator<Map.Entry<String, WFormModel.FieldData>> i_it =
            this.fields_.entrySet().iterator();
        i_it.hasNext(); ) {
      Map.Entry<String, WFormModel.FieldData> i = i_it.next();
      if (!this.validateField(i.getKey())) {
        result = false;
      }
    }
    return result;
  }
  /**
   * Returns the current overall validation state.
   *
   * <p>This checks the {@link WFormModel#getValidation(String field) getValidation()} of all
   * fields, and returns <code>true</code> if all all fields have been validated and are valid.
   *
   * <p>
   *
   * @see WFormModel#validate()
   */
  public boolean isValid() {
    for (Iterator<Map.Entry<String, WFormModel.FieldData>> i_it =
            this.fields_.entrySet().iterator();
        i_it.hasNext(); ) {
      Map.Entry<String, WFormModel.FieldData> i = i_it.next();
      final WFormModel.FieldData fd = i.getValue();
      if (!fd.visible) {
        continue;
      }
      if (!fd.validated || fd.validation.getState() != ValidationState.Valid) {
        return false;
      }
    }
    return true;
  }
  /**
   * Sets whether a field is visible.
   *
   * <p>Fields are visible by default. An invisible field will be ignored during validation (i.e.
   * will be considered as valid).
   *
   * <p>
   *
   * @see WFormModel#isVisible(String field)
   */
  public void setVisible(String field, boolean visible) {
    WFormModel.FieldData i = this.fields_.get(field);
    if (i != null) {
      i.visible = visible;
    } else {
      logger.error(
          new StringWriter()
              .append("setVisible(): ")
              .append(field)
              .append(" not in model")
              .toString());
    }
  }
  /**
   * Returns whether a field is visible.
   *
   * <p>In some cases not all fields of the model need to be shown. This may depend on values input
   * for certain fields, and thus change dynamically. You may specialize this method to indicate
   * that a certain field should be invisible.
   *
   * <p>The default implementation returns the value set by {@link WFormModel#setVisible(String
   * field, boolean visible) setVisible()}.
   */
  public boolean isVisible(String field) {
    WFormModel.FieldData i = this.fields_.get(field);
    if (i != null) {
      return i.visible;
    } else {
      return true;
    }
  }
  /**
   * Sets whether a field is read-only.
   *
   * <p>Fields are read-write by default.
   *
   * <p>
   *
   * @see WFormModel#isReadOnly(String field)
   */
  public void setReadOnly(String field, boolean readOnly) {
    WFormModel.FieldData i = this.fields_.get(field);
    if (i != null) {
      i.readOnly = readOnly;
    } else {
      logger.error(
          new StringWriter()
              .append("setReadOnly(): ")
              .append(field)
              .append(" not in model")
              .toString());
    }
  }
  /**
   * Returns whether a field is read only.
   *
   * <p>The default implementation returns the value set by {@link WFormModel#setReadOnly(String
   * field, boolean readOnly) setReadOnly()}
   */
  public boolean isReadOnly(String field) {
    WFormModel.FieldData i = this.fields_.get(field);
    if (i != null) {
      return i.readOnly;
    } else {
      return false;
    }
  }
  /**
   * Returns a field label.
   *
   * <p>The default implementation returns the WString::tr(field)
   */
  public WString label(String field) {
    return WString.tr(field);
  }
  /**
   * Sets the field value.
   *
   * <p>
   *
   * @see WFormModel#getValue(String field)
   * @see WFormModel#valueText(String field)
   */
  public void setValue(String field, final Object value) {
    this.fields_.get(field).value = value;
  }
  /**
   * Returns the field value.
   *
   * <p>
   *
   * @see WFormModel#valueText(String field)
   * @see WFormModel#setValue(String field, Object value)
   */
  public Object getValue(String field) {
    WFormModel.FieldData i = this.fields_.get(field);
    if (i != null) {
      return i.value;
    } else {
      return NoValue;
    }
  }
  /**
   * Returns the field value text.
   *
   * <p>
   *
   * @see WFormModel#getValue(String field)
   */
  public String valueText(String field) {
    WValidator v = this.getValidator(field);
    return StringUtils.asString(this.getValue(field), v != null ? v.getFormat() : "").toString();
  }
  /** Sets a validator. */
  public void setValidator(String field, final WValidator validator) {
    WFormModel.FieldData i = this.fields_.get(field);
    if (i != null) {
      final WFormModel.FieldData d = i;
      d.validator = validator;
    } else {
      logger.error(
          new StringWriter()
              .append("setValidator(): ")
              .append(field)
              .append(" not in model")
              .toString());
    }
  }
  /**
   * Returns a validator.
   *
   * <p>Returns the validator for the field.
   */
  public WValidator getValidator(String field) {
    WFormModel.FieldData i = this.fields_.get(field);
    if (i != null) {
      final WFormModel.FieldData d = i;
      return d.validator;
    }
    return null;
  }
  /**
   * Validates a field.
   *
   * <p>The default implementation uses the validator configured for the field to validate the field
   * contents, or if no validator has been configured assumes that the field is valid.
   *
   * <p>You will typically customize this method for more complex validation cases.
   *
   * <p>
   *
   * @see WFormModel#validate()
   */
  public boolean validateField(String field) {
    if (!this.isVisible(field)) {
      return true;
    }
    WFormModel.FieldData i = this.fields_.get(field);
    if (i != null) {
      final WFormModel.FieldData d = i;
      if (d.validator != null) {
        this.setValidation(
            field, d.validator.validate(StringUtils.asString(this.valueText(field)).toString()));
      } else {
        this.setValidation(field, Valid);
      }
      return d.validation.getState() == ValidationState.Valid;
    } else {
      return true;
    }
  }
  /**
   * Sets whether a field has been validated.
   *
   * <p>This is usually not used directly, but invoked by {@link WFormModel#setValidation(String
   * field, WValidator.Result result) setValidation()}
   *
   * <p>A field is initially (or after {@link WFormModel#reset() reset()}), not validated.
   */
  public void setValidated(String field, boolean validated) {
    WFormModel.FieldData i = this.fields_.get(field);
    if (i != null) {
      i.validated = validated;
    } else {
      logger.error(
          new StringWriter()
              .append("setValidated(): ")
              .append(field)
              .append(" not in model")
              .toString());
    }
  }
  /**
   * Returns whether the field has been validated yet.
   *
   * <p>This is initially <code>false</code>, and set to <code>true</code> by {@link
   * WFormModel#setValidation(String field, WValidator.Result result) setValidation()}.
   *
   * <p>
   *
   * @see WFormModel#setValidated(String field, boolean validated)
   */
  public boolean isValidated(String field) {
    WFormModel.FieldData i = this.fields_.get(field);
    if (i != null) {
      return i.validated;
    } else {
      return false;
    }
  }
  /**
   * Returns the result of a validation.
   *
   * <p>
   *
   * @see WFormModel#validateField(String field)
   */
  public WValidator.Result getValidation(String field) {
    WFormModel.FieldData i = this.fields_.get(field);
    if (i != null) {
      return i.validation;
    } else {
      return Valid;
    }
  }
  /**
   * Sets the validation result for a field.
   *
   * <p>This will also set the field as validated.
   *
   * <p>
   *
   * @see WFormModel#getValidation(String field)
   * @see WFormModel#isValidated(String field)
   */
  public void setValidation(String field, final WValidator.Result result) {
    WFormModel.FieldData i = this.fields_.get(field);
    if (i != null) {
      i.validation = result;
      this.setValidated(field, true);
    } else {
      logger.error(
          new StringWriter()
              .append("setValidation(): ")
              .append(field)
              .append(" not in model")
              .toString());
    }
  }

  static class FieldData {
    private static Logger logger = LoggerFactory.getLogger(FieldData.class);

    public FieldData() {
      this.validator = (WValidator) null;
      this.value = new Object();
      this.validation = new WValidator.Result();
      this.visible = true;
      this.readOnly = false;
      this.validated = false;
      this.value = null;
    }

    public WValidator validator;
    public Object value;
    public WValidator.Result validation;
    public boolean visible;
    public boolean readOnly;
    public boolean validated;
  }

  private Map<String, WFormModel.FieldData> fields_;
  private static final WValidator.Result Valid =
      new WValidator.Result(ValidationState.Valid, new WString());
  private static final Object NoValue = new Object();
}
