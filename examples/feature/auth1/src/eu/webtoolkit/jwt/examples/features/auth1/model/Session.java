package eu.webtoolkit.jwt.examples.features.auth1.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import edu.vt.middleware.password.CharacterCharacteristicsRule;
import edu.vt.middleware.password.DigitCharacterRule;
import edu.vt.middleware.password.LengthRule;
import edu.vt.middleware.password.LowercaseCharacterRule;
import edu.vt.middleware.password.NonAlphanumericCharacterRule;
import edu.vt.middleware.password.PasswordValidator;
import edu.vt.middleware.password.Rule;
import edu.vt.middleware.password.UppercaseCharacterRule;
import edu.vt.middleware.password.WhitespaceRule;
import eu.webtoolkit.jwt.auth.AbstractUserDatabase;
import eu.webtoolkit.jwt.auth.AuthService;
import eu.webtoolkit.jwt.auth.BCryptHashFunction;
import eu.webtoolkit.jwt.auth.FacebookService;
import eu.webtoolkit.jwt.auth.GoogleService;
import eu.webtoolkit.jwt.auth.Login;
import eu.webtoolkit.jwt.auth.OAuthService;
import eu.webtoolkit.jwt.auth.PasswordService;
import eu.webtoolkit.jwt.auth.PasswordStrengthValidator;
import eu.webtoolkit.jwt.auth.PasswordVerifier;
import eu.webtoolkit.jwt.auth.jpa.AuthInfo;
import eu.webtoolkit.jwt.auth.jpa.UserDatabase;

public class Session {

	static AuthService myAuthService;
	static PasswordService myPasswordService;
	static List<OAuthService> myOAuthServices;

	public static void configureAuth() {
		if (myAuthService != null) 
			return;
		
		myAuthService = new AuthService();
		myPasswordService = new PasswordService(myAuthService);
		myOAuthServices = new ArrayList<OAuthService>();
		
		myAuthService.setAuthTokensEnabled(true, "logincookie");
		myAuthService.setEmailVerificationEnabled(true);

		PasswordVerifier verifier = new PasswordVerifier();
		verifier.addHashFunction(new BCryptHashFunction());
		myPasswordService.setVerifier(verifier);
		myPasswordService.setAttemptThrottlingEnabled(true);
		
		myPasswordService.setStrengthValidator(new PasswordStrengthValidator(createStrengthValidator()));
		
		if (GoogleService.configured())
			myOAuthServices.add(new GoogleService(myAuthService));
		
		if (FacebookService.configured())
			myOAuthServices.add(new FacebookService(myAuthService));
	}
	
	private static PasswordValidator createStrengthValidator() {
		List<Rule> ruleList = new ArrayList<Rule>();
		
		// password must be between 8 and 16 chars long
		LengthRule lengthRule = new LengthRule(8, 16);
		ruleList.add(lengthRule);

		// don't allow whitespace
		WhitespaceRule whitespaceRule = new WhitespaceRule();
		ruleList.add(whitespaceRule);

		// control allowed characters
		CharacterCharacteristicsRule charRule = new CharacterCharacteristicsRule();
		// require at least 1 digit in passwords
		charRule.getRules().add(new DigitCharacterRule(1));
		// require at least 1 non-alphanumeric char
		charRule.getRules().add(new NonAlphanumericCharacterRule(1));
		// require at least 1 upper case char
		charRule.getRules().add(new UppercaseCharacterRule(1));
		// require at least 1 lower case char
		charRule.getRules().add(new LowercaseCharacterRule(1));
		// require at least 3 of the previous rules be met
		charRule.setNumberOfCharacteristics(3);
		ruleList.add(charRule);
		
		return new PasswordValidator(ruleList);
	}

	public Session(EntityManager entityManager) {
		entityManager_ = entityManager;
		login_ = new Login();
		userDatabase_ = new UserDatabase(entityManager_);
	}

	public AuthInfo getAuthInfo() {
		return userDatabase_.find(login_.getUser());
	}

	public AbstractUserDatabase getUserDatabase() {
		return userDatabase_;
	}

	public Login getLogin() {
		return login_;
	}

	public static AuthService getAuth() {
		return myAuthService;
	}

	public static PasswordService getPasswordAuth() {
		return myPasswordService;
	}

	public static List<OAuthService> getOAuth() {
		return myOAuthServices;
	}

	private EntityManager entityManager_;
	private UserDatabase userDatabase_;
	private Login login_;
}
