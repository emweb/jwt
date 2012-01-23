package eu.webtoolkit.jwt.auth;

import edu.vt.middleware.password.Password;
import edu.vt.middleware.password.PasswordData;
import edu.vt.middleware.password.PasswordValidator;
import edu.vt.middleware.password.RuleResult;
import eu.webtoolkit.jwt.auth.AbstractPasswordService.StrengthValidatorResult;

/** The default implementation for password strength validation.
*
* This implementation uses http://code.google.com/p/vt-middleware/wiki/vtpassword.
*/
public class PasswordStrengthValidator extends AbstractPasswordService.AbstractStrengthValidator {
	/**
	 * Constructor, accepts a configured instance of {@link PasswordValidator}.
	 */
	public PasswordStrengthValidator(PasswordValidator validator) {
		this.validator = validator;
	}

	@Override
	public StrengthValidatorResult evaluateStrength(String password, String loginName, String email) {
		PasswordData passwordData = new PasswordData(new Password(password));
		passwordData.setUsername(loginName);
		
		RuleResult result = validator.validate(passwordData);
		
		String m = "";
		if (validator.getMessages(result).size() > 0)
			m = validator.getMessages(result).get(0);
		
		return new StrengthValidatorResult(result.isValid(), m, result.isValid() ? 5 : 0);
	}
	
	/**
	 * Returns the {@link PasswordValidator} instance.
	 */
	public PasswordValidator getValidator() {
		return validator;
	}
	
	private PasswordValidator validator;
}
