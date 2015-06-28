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
 * A template-based View class form models.
 * <p>
 * 
 * This implements a View to be used in conjunction with {@link WFormModel}
 * models to implement forms.
 * <p>
 * For each model field, it uses a number of conventional template placholder
 * variables to represent the label, editor, and validation messages in the
 * template. For a field name &apos;<i>field</i>&apos;, we have:
 * <ul>
 * <li>&apos;<i>field</i>&apos;: the actual (form) widget for viewing/editing
 * the value</li>
 * <li>&apos;<i>field</i>-label&apos;: the label text</li>
 * <li>&apos;<i>field</i>-info&apos;: a text that contains help or validation
 * messages</li>
 * <li>&apos;if:<i>field</i>&apos;: condition for the visibility of the field</li>
 * </ul>
 * <p>
 * A typical template uses blocks of the following-format (in the example below
 * illustrated for a field &apos;UserName&apos;):
 * <p>
 * <div class="fragment">
 * 
 * <pre class="fragment">
 * ${&lt;if:UserName&gt;}
 *      &lt;label for=&quot;${id:UserName}&quot;&gt;${UserName-label}&lt;/label&gt;
 *      ${UserName} ${UserName-info}
 *    ${&lt;/if:UserName&gt;}
 * </pre>
 * 
 * </div>
 * <p>
 * The View may render fields of more than one model, and does not necessarily
 * need to render all information of each model. The latter can be achieved by
 * either calling
 * {@link WTemplateFormView#updateViewField(WFormModel model, String field)
 * updateViewField()} and
 * {@link WTemplateFormView#updateModelField(WFormModel model, String field)
 * updateModelField()} for individual model fields, or by hiding fields in the
 * model that are not to be shown in the view.
 * <p>
 * The {@link WTemplateFormView#updateView(WFormModel model) updateView()}
 * method updates the view based on a model (e.g. to propagate changed values or
 * validation feed-back), while the
 * {@link WTemplateFormView#updateModel(WFormModel model) updateModel()} method
 * updates the model with values entered in the View.
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
	 * <p>
	 * For convenience, this initializes the template with:
	 * <p>
	 * 
	 * <pre>
	 * {@code
	 *    addFunction("id", Functions.id);
	 *    addFunction("tr", Functions.tr);
	 *    addFunction("block", Functions.block);
	 *   }
	 * </pre>
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
	 * <p>
	 * For convenience, this initializes the template with:
	 * <p>
	 * 
	 * <pre>
	 * {@code
	 *    addFunction("id", Functions.id);
	 *    addFunction("tr", Functions.tr);
	 *    addFunction("block", Functions.block);
	 *   }
	 * </pre>
	 */
	public WTemplateFormView(final CharSequence text, WContainerWidget parent) {
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
	public WTemplateFormView(final CharSequence text) {
		this(text, (WContainerWidget) null);
	}

	/**
	 * Sets the form widget for a given field.
	 * <p>
	 * When the <code>widget</code> is a form widget, then the View class will
	 * use {@link WFormWidget#setValueText(String value)
	 * WFormWidget#setValueText()} to update it with model values, and
	 * {@link WFormWidget#getValueText() WFormWidget#getValueText()} to update
	 * the model with view data.
	 * <p>
	 * You can override this default behaviour by either using the overloaded
	 * {@link WTemplateFormView#setFormWidget(String field, WWidget formWidget)
	 * setFormWidget()} that allows to specify these functions, or reimplement
	 * {@link WTemplateFormView#updateViewValue(WFormModel model, String field, WFormWidget edit)
	 * updateViewValue()} or
	 * {@link WTemplateFormView#updateModelValue(WFormModel model, String field, WFormWidget edit)
	 * updateModelValue()}.
	 */
	public void setFormWidget(String field, WWidget formWidget) {
		this.fields_.put(field, new WTemplateFormView.FieldData());
		this.fields_.get(field).formWidget = formWidget;
		this.bindWidget(field, formWidget);
	}

	// public void setFormWidget(String field, WWidget widget, F1
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
	 * This overloaded functions allows functions to be provided to update the
	 * view and model for this field.
	 */
	public void setFormWidget(String field, WWidget formWidget,
			WTemplateFormView.FieldView fieldView) {
		this.fields_.put(field, new WTemplateFormView.FieldData());
		this.fields_.get(field).formWidget = formWidget;
		this.fields_.get(field).updateFunctions = fieldView;
		this.bindWidget(field, formWidget);
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
	 * Creates or updates a field in the View.
	 * <p>
	 * This will update or create and bind widgets in the template to represent
	 * the field. To create the form widget that implements the editing, it
	 * calls {@link WTemplateFormView#createFormWidget(String field)
	 * createFormWidget()}.
	 * <p>
	 * The default behaviour interprets
	 * {@link WFormModel#isVisible(String field) WFormModel#isVisible()},
	 * {@link WFormModel#isReadOnly(String field) WFormModel#isReadOnly()},
	 * {@link WFormModel#label(String field) WFormModel#label()} and
	 * {@link WFormModel#getValidator(String field) WFormModel#getValidator()}
	 * to update the View, and calls
	 * {@link WTemplateFormView#updateViewValue(WFormModel model, String field, WFormWidget edit)
	 * updateViewValue()} to update the view value. If no form widget has been
	 * set for the given <code>field</code> using
	 * {@link WTemplateFormView#setFormWidget(String field, WWidget formWidget)
	 * setFormWidget()}, then it calls
	 * {@link WTemplateFormView#createFormWidget(String field)
	 * createFormWidget()} to try to create one.
	 * <p>
	 * It&apos;s usually more convenient to reimplement
	 * {@link WTemplateFormView#updateViewValue(WFormModel model, String field, WFormWidget edit)
	 * updateViewValue()} to override specifically how the value from the model
	 * should be used to update the form widget.
	 */
	public void updateViewField(WFormModel model, String field) {
		final String var = field;
		if (model.isVisible(field)) {
			this.setCondition("if:" + var, true);
			WWidget edit = this.resolveWidget(var);
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
			WFormWidget fedit = ((edit) instanceof WFormWidget ? (WFormWidget) (edit)
					: null);
			if (fedit != null) {
				if (fedit.getValidator() != model.getValidator(field)
						&& model.getValidator(field) != null) {
					fedit.setValidator(model.getValidator(field));
				}
				this.updateViewValue(model, field, fedit);
			} else {
				this.updateViewValue(model, field, edit);
			}
			WText info = (WText) this.resolveWidget(var + "-info");
			if (!(info != null)) {
				info = new WText();
				this.bindWidget(var + "-info", info);
			}
			this.bindString(var + "-label", model.label(field));
			final WValidator.Result v = model.getValidation(field);
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
	 * Updates the value in the View.
	 * <p>
	 * The default implementation calls updateViewValue({@link WFormModel} ,
	 * WFormField::Field, {@link WWidget} ). If this function returned
	 * <code>false</code>, it sets {@link WFormModel#valueText(String field)
	 * WFormModel#valueText()} into
	 * {@link WFormWidget#setValueText(String value) WFormWidget#setValueText()}.
	 */
	public void updateViewValue(WFormModel model, String field, WFormWidget edit) {
		if (this.updateViewValue(model, field, (WWidget) edit)) {
			return;
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
	 * Updates the value in the View.
	 * <p>
	 * The default implementation considers only a specialized update function
	 * that may have been configured in
	 * {@link WTemplateFormView#setFormWidget(String field, WWidget formWidget)
	 * setFormWidget()} and returns <code>false</code> if no such function was
	 * configured.
	 */
	public boolean updateViewValue(WFormModel model, String field, WWidget edit) {
		WTemplateFormView.FieldData fi = this.fields_.get(field);
		if (fi != null) {
			if (fi.updateFunctions != null) {
				fi.updateFunctions.updateViewValue();
				return true;
			}
		}
		return false;
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
	 * Updates a field in the Model.
	 * <p>
	 * This calls
	 * {@link WTemplateFormView#updateModelValue(WFormModel model, String field, WFormWidget edit)
	 * updateModelValue()} to update the model value.
	 */
	public void updateModelField(WFormModel model, String field) {
		WWidget edit = this.resolveWidget(field);
		WFormWidget fedit = ((edit) instanceof WFormWidget ? (WFormWidget) (edit)
				: null);
		if (fedit != null) {
			this.updateModelValue(model, field, fedit);
		} else {
			this.updateModelValue(model, field, edit);
		}
	}

	/**
	 * Updates a value in the Model.
	 * <p>
	 * The default implementation calls
	 * {@link WTemplateFormView#updateModelValue(WFormModel model, String field, WWidget edit)
	 * updateModelValue()}. If this function returned <code>false</code>, it
	 * calls {@link WFormModel#setValue(String field, Object value)
	 * WFormModel#setValue()} with {@link WFormWidget#getValueText()
	 * WFormWidget#getValueText()}.
	 */
	public void updateModelValue(WFormModel model, String field,
			WFormWidget edit) {
		if (this.updateModelValue(model, field, (WWidget) edit)) {
			return;
		}
		WAbstractToggleButton b = ((edit) instanceof WAbstractToggleButton ? (WAbstractToggleButton) (edit)
				: null);
		if (b != null) {
			model.setValue(field, b.isChecked());
		} else {
			model.setValue(field, edit.getValueText());
		}
	}

	/**
	 * Updates a value in the Model.
	 * <p>
	 * The default implementation considers only a specialized update function
	 * that may have been configured in
	 * {@link WTemplateFormView#setFormWidget(String field, WWidget formWidget)
	 * setFormWidget()} and returns <code>false</code> if no such function was
	 * configured.
	 */
	public boolean updateModelValue(WFormModel model, String field, WWidget edit) {
		WTemplateFormView.FieldData fi = this.fields_.get(field);
		if (fi != null) {
			if (fi.updateFunctions != null) {
				fi.updateFunctions.updateModelValue();
				return true;
			}
		}
		return false;
	}

	/**
	 * Creates a form widget.
	 * <p>
	 * This method is called by
	 * {@link WTemplateFormView#updateViewField(WFormModel model, String field)
	 * updateViewField()} when it needs to create a form widget for a field, and
	 * none was specified using
	 * {@link WTemplateFormView#setFormWidget(String field, WWidget formWidget)
	 * setFormWidget()}.
	 */
	protected WWidget createFormWidget(String field) {
		return null;
	}

	/**
	 * Indicates the validation result.
	 * <p>
	 * The default implementation calls
	 * {@link WTheme#applyValidationStyle(WWidget widget, WValidator.Result validation, EnumSet flags)
	 * WTheme#applyValidationStyle()}
	 * <p>
	 * <p>
	 * <i><b>Note: </b>We changed the signature to take an edit {@link WWidget}
	 * instead of {@link WFormWidget} in JWt 3.3.1! </i>
	 * </p>
	 */
	protected void indicateValidation(String field, boolean validated,
			WText info, WWidget edit, final WValidator.Result validation) {
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

		public WWidget formWidget;
		public WTemplateFormView.FieldView updateFunctions;
	}

	private Map<String, WTemplateFormView.FieldData> fields_;

	private void init() {
		this.addFunction("id", Functions.id);
		this.addFunction("tr", Functions.tr);
		this.addFunction("block", Functions.block);
	}
}
