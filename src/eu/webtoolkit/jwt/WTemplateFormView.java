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
 * A template-based View class form form models.
 * <p>
 * 
 * This implements a View to be used in conjunction with {@link WFormModel}
 * models to implement forms.
 * <p>
 * For a model field, it uses a number of conventional variable names to
 * represent the label, editor, and validation messages in the template:
 * <ul>
 * <li>&apos;field&apos;: the form widget which contains the value</li>
 * <li>&apos;field-label&apos;: the label</li>
 * <li>&apos;field-info&apos;: a text that contains the validation message</li>
 * <li>&apos;if:field&apos;: condition for the visibility of the field</li>
 * </ul>
 * <p>
 * For a field with name &apos;field&apos;, a typical template uses blocks of
 * the following-format:
 * <p>
 * <div class="fragment">
 * 
 * <pre class="fragment">
 *   ${&lt;if:field&gt;}
 *     &lt;label for=&quot;${id:field}&quot;&gt;${field-label}&lt;/label&gt;
 *     ${field} ${field-info}
 *   ${&lt;/if:field&gt;}
 * </pre>
 * 
 * </div>
 * <p>
 * The View may render fields of more than one model, and does not necessarily
 * need to render all information of each model: you can call the
 * {@link WTemplateFormView#updateViewField(WFormModel model, String field)
 * updateViewField()} and
 * {@link WTemplateFormView#updateModelField(WFormModel model, String field)
 * updateModelField()} for individual model fields.
 * <p>
 * The {@link WTemplateFormView#updateView(WFormModel model) updateView()}
 * method updates the view based on a model (e.g. to propagate changed values or
 * validation), while the {@link WTemplateFormView#updateModel(WFormModel model)
 * updateModel()} method updates a model with values entered in the View.
 * <p>
 * The view is passive: it will not perform any updates by itself of either the
 * View or Model. You will typically bind a method to the Ok button and do:
 * <p>
 * 
 * <pre>
 * {@code
 *  void okClicked()
 *  {
 *    updateModel(this.model);
 *    if (this.model.validate()) {
 *      ...
 *    } else {
 *      updateView(this.model);
 *    }
 *  }
 * }
 * </pre>
 */
public class WTemplateFormView extends WTemplate {
	private static Logger logger = LoggerFactory
			.getLogger(WTemplateFormView.class);

	/**
	 * Constructor.
	 */
	public WTemplateFormView(WContainerWidget parent) {
		super(parent);
		this.fields_ = new HashMap<String, WTemplateFormView.FieldData>();
		this.init();
	}

	/**
	 * Constructor.
	 * <p>
	 * Calls {@link #WTemplateFormView(WContainerWidget parent)
	 * this((WContainerWidget)null)}
	 */
	public WTemplateFormView() {
		this((WContainerWidget) null);
	}

	/**
	 * Constructor.
	 */
	public WTemplateFormView(CharSequence text, WContainerWidget parent) {
		super(text, parent);
		this.fields_ = new HashMap<String, WTemplateFormView.FieldData>();
		this.init();
	}

	/**
	 * Constructor.
	 * <p>
	 * Calls
	 * {@link #WTemplateFormView(CharSequence text, WContainerWidget parent)
	 * this(text, (WContainerWidget)null)}
	 */
	public WTemplateFormView(CharSequence text) {
		this(text, (WContainerWidget) null);
	}

	/**
	 * Sets the form widget for a given field.
	 */
	public void setFormWidget(String field, WFormWidget formWidget) {
		WTemplateFormView.FieldData i = this.fields_.get(field);
		if (i == null) {
			this.fields_.put(field, new WTemplateFormView.FieldData());
		}
		this.fields_.get(field).formWidget = formWidget;
		this.bindWidget(field, formWidget);
	}

	// public void setFormWidget(String field, WFormWidget formWidget, F1
	// updateViewValue, F2 updateModelValue) ;
	/**
	 * Interface for custom update methods for field data.
	 */
	public static interface FieldView {
		/**
		 * Update the widget&apos;s value based on the model&apos;s data.
		 */
		public void updateViewValue();

		/**
		 * Update the model&apos;s data based on the widget&apos;s value.
		 */
		public void updateModelValue();
	}

	/**
	 * Sets the form widget for a given field.
	 * <p>
	 * This also defines the functions to update the view respectively the model
	 */
	public void setFormWidget(String field, WFormWidget formWidget,
			WTemplateFormView.FieldView fieldView) {
		WTemplateFormView.FieldData i = this.fields_.get(field);
		if (i == null) {
			this.fields_.put(field, new WTemplateFormView.FieldData());
		}
		this.fields_.get(field).formWidget = formWidget;
		this.fields_.get(field).updateFunctions = fieldView;
		this.bindWidget(field, formWidget);
	}

	/**
	 * Creates or updates a field in the View.
	 * <p>
	 * This will update or create and bind widgets in the template to represent
	 * the field. To create the form widget that implements the editing, it
	 * calls {@link WTemplateFormView#createFormWidget(String field)
	 * createFormWidget()}.
	 * <p>
	 * <p>
	 * <i><b>Note: </b>It&apos;s usually more convenient to reimplement
	 * {@link WTemplateFormView#updateViewValue(WFormModel model, String field, WFormWidget edit)
	 * updateViewValue()} to support a non-textual value in the model. </i>
	 * </p>
	 */
	public void updateViewField(WFormModel model, String field) {
		final String var = field;
		if (model.isVisible(field)) {
			this.setCondition("if:" + var, true);
			WFormWidget edit = (WFormWidget) this.resolveWidget(var);
			if (!(edit != null)) {
				edit = this.createFormWidget(field);
				if (!(edit != null)) {
					logger.error(new StringWriter().append(
							"updateViewField: createFormWidget('")
							.append(field).append("') returned 0").toString());
					return;
				}
				this.bindWidget(var, edit);
			}
			if (edit.getValidator() != model.getValidator(field)) {
				edit.setValidator(model.getValidator(field));
			}
			this.updateViewValue(model, field, edit);
			WText info = (WText) this.resolveWidget(var + "-info");
			if (!(info != null)) {
				info = new WText();
				this.bindWidget(var + "-info", info);
			}
			this.bindString(var + "-label", model.label(field));
			WValidator.Result v = model.getValidation(field);
			info.setText(v.getMessage());
			this.indicateValidation(field, model.isValidated(field), info,
					edit, v);
			edit.setDisabled(model.isReadOnly(field));
		} else {
			this.setCondition("if:" + var, false);
			this.bindEmpty(var);
			this.bindEmpty(var + "-info");
		}
	}

	/**
	 * Updates a field in the Model (<b>Deprecated</b>).
	 * <p>
	 * Calls
	 * {@link WTemplateFormView#updateViewValue(WFormModel model, String field, WFormWidget edit)
	 * updateViewValue()}
	 * <p>
	 * 
	 * @deprecated Reimplement
	 *             {@link WTemplateFormView#updateViewValue(WFormModel model, String field, WFormWidget edit)
	 *             updateViewValue()} instead.
	 */
	public void updateModelField(WFormModel model, String field) {
		WFormWidget edit = (WFormWidget) this.resolveWidget(field);
		if (edit != null) {
			WTemplateFormView.FieldData fi = this.fields_.get(field);
			if (fi != null) {
				if (fi.updateFunctions != null) {
					fi.updateFunctions.updateModelValue();
					return;
				}
			}
			this.updateModelValue(model, field, edit);
		}
	}

	/**
	 * Updates the value in the View.
	 * <p>
	 * The default implementation sets {@link WFormModel#valueText(String field)
	 * WFormModel#valueText()} into
	 * {@link WFormWidget#setValueText(String value) WFormWidget#setValueText()}
	 */
	public void updateViewValue(WFormModel model, String field, WFormWidget edit) {
		WTemplateFormView.FieldData fi = this.fields_.get(field);
		if (fi != null) {
			if (fi.updateFunctions != null) {
				fi.updateFunctions.updateViewValue();
				return;
			}
		}
		WAbstractToggleButton b = ((edit) instanceof WAbstractToggleButton ? (WAbstractToggleButton) (edit)
				: null);
		if (b != null) {
			Object v = model.getValue(field);
			if ((v == null) || (Boolean) v == false) {
				b.setChecked(false);
			} else {
				b.setChecked(true);
			}
		} else {
			edit.setValueText(model.valueText(field));
		}
	}

	/**
	 * Updates a value in the Model.
	 * <p>
	 * The default implementation sets
	 * {@link WFormModel#setValue(String field, Object value)
	 * WFormModel#setValue()} with {@link WFormWidget#getValueText()
	 * WFormWidget#getValueText()}.
	 */
	public void updateModelValue(WFormModel model, String field,
			WFormWidget edit) {
		WAbstractToggleButton b = ((edit) instanceof WAbstractToggleButton ? (WAbstractToggleButton) (edit)
				: null);
		if (b != null) {
			model.setValue(field, b.isChecked());
		} else {
			model.setValue(field, edit.getValueText());
		}
	}

	/**
	 * Updates the View.
	 * <p>
	 * This creates or updates all fields in the view.
	 * <p>
	 * 
	 * @see WTemplateFormView#updateViewField(WFormModel model, String field)
	 * @see WFormModel#getFields()
	 */
	public void updateView(WFormModel model) {
		List<String> fields = model.getFields();
		for (int i = 0; i < fields.size(); ++i) {
			String field = fields.get(i);
			this.updateViewField(model, field);
		}
	}

	/**
	 * Updates the Model.
	 * <p>
	 * This creates or updates all field values in the model.
	 * <p>
	 * 
	 * @see WTemplateFormView#updateModelField(WFormModel model, String field)
	 * @see WFormModel#getFields()
	 */
	public void updateModel(WFormModel model) {
		List<String> fields = model.getFields();
		for (int i = 0; i < fields.size(); ++i) {
			String field = fields.get(i);
			this.updateModelField(model, field);
		}
	}

	/**
	 * Creates a form widget.
	 * <p>
	 * This method is called by
	 * {@link WTemplateFormView#updateViewField(WFormModel model, String field)
	 * updateViewField()} when it needs to create a form widget for a field, and
	 * none was specified using
	 * {@link WTemplateFormView#setFormWidget(String field, WFormWidget formWidget)
	 * setFormWidget()}.
	 */
	protected WFormWidget createFormWidget(String field) {
		return null;
	}

	/**
	 * Indicates the validation result.
	 * <p>
	 * The default implementation will set &quot;Wt-valid&quot; or
	 * &quot;Wt-invalid&quot; on the form widget, and &quot;Wt-error&quot; on
	 * the info text.
	 */
	protected void indicateValidation(String field, boolean validated,
			WText info, WFormWidget edit, WValidator.Result validation) {
		info.setText(validation.getMessage());
		if (validated) {
			WApplication.getInstance().getTheme().applyValidationStyle(edit,
					validation, ValidationStyleFlag.ValidationAllStyles);
			info.toggleStyleClass("Wt-error",
					validation.getState() != WValidator.State.Valid, true);
		} else {
			WApplication.getInstance().getTheme().applyValidationStyle(edit,
					validation, ValidationStyleFlag.ValidationNoStyle);
			info.removeStyleClass("Wt-error", true);
		}
	}

	static class FieldData {
		private static Logger logger = LoggerFactory.getLogger(FieldData.class);

		public FieldData() {
			this.formWidget = null;
		}

		public WFormWidget formWidget;
		public WTemplateFormView.FieldView updateFunctions;
	}

	private Map<String, WTemplateFormView.FieldData> fields_;

	private void init() {
		this.addFunction("id", Functions.id);
		this.addFunction("tr", Functions.tr);
		this.addFunction("block", Functions.block);
	}
}
