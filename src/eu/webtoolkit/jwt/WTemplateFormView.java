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
 * validation), while the updateModel() method updates a model with values
 * entered in the View.
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
		this.addFunction("id", WTemplate.Functions.id);
		this.addFunction("tr", WTemplate.Functions.tr);
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
		this.addFunction("id", WTemplate.Functions.id);
		this.addFunction("tr", WTemplate.Functions.tr);
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
	 * Creates or updates a field in the View.
	 * <p>
	 * This will update or create and bind widgets in the template to represent
	 * the field. To create the form widget that implements the editing, it
	 * calls {@link WTemplateFormView#createFormWidget(String field)
	 * createFormWidget()}.
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
			edit.toggleStyleClass("Wt-disabled", edit.isDisabled());
		} else {
			this.setCondition("if:" + var, false);
			this.bindEmpty(var);
			this.bindEmpty(var + "-info");
		}
	}

	/**
	 * Updates a field value in the Model.
	 * <p>
	 * This propagates data entered in the form widget to the model with
	 * {@link WFormModel#setValue(String field, Object value)
	 * WFormModel#setValue()}
	 */
	public void updateModelField(WFormModel model, String field) {
		WFormWidget edit = (WFormWidget) this.resolveWidget(field);
		if (edit != null) {
			WAbstractToggleButton b = ((edit) instanceof WAbstractToggleButton ? (WAbstractToggleButton) (edit)
					: null);
			if (b != null) {
				model.setValue(field, b.isChecked());
			} else {
				model.setValue(field, edit.getValueText());
			}
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
			this.updateViewField(model, fields.get(i));
		}
	}

	public void updateModel(WFormModel model) {
		List<String> fields = model.getFields();
		for (int i = 0; i < fields.size(); ++i) {
			this.updateModelField(model, fields.get(i));
		}
	}

	/**
	 * Creates a form widget.
	 * <p>
	 * This method is called by
	 * {@link WTemplateFormView#updateViewField(WFormModel model, String field)
	 * updateViewField()} when it needs to create a form widget for a field. You
	 * either need to make sure these widgets have been created and bound before
	 * calling {@link WTemplateFormView#updateView(WFormModel model)
	 * updateView()}, or you need to specialize this method to do it on-demand.
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
			switch (validation.getState()) {
			case InvalidEmpty:
			case Invalid:
				edit.removeStyleClass("Wt-valid");
				edit.addStyleClass("Wt-invalid");
				info.addStyleClass("Wt-error");
				break;
			case Valid:
				edit.removeStyleClass("Wt-invalid");
				edit.addStyleClass("Wt-valid");
				info.removeStyleClass("Wt-error");
				break;
			}
		} else {
			edit.removeStyleClass("Wt-valid");
			edit.removeStyleClass("Wt-invalid");
			info.removeStyleClass("Wt-error");
		}
	}
}
