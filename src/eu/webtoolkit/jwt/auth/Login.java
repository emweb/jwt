/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.auth;

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
 * A class that manages the current login state.
 * <p>
 * 
 * This is a model class which is typically associated with a single session,
 * for the duration of the session.
 * <p>
 * Widgets that implement authentication (and thus produce authentication
 * changes), will indicate their result in this object using the
 * {@link Login#login(User user, LoginState state) login()} or
 * {@link Login#logout() logout()} methods.
 * <p>
 * Widgets that want to react to login state changes (typically, as user logging
 * in or out) should listen to the {@link Login#changed() changed()} signal of
 * this object.
 * <p>
 * 
 * @see AuthWidget
 */
public class Login extends WObject {
	private static Logger logger = LoggerFactory.getLogger(Login.class);

	/**
	 * Default constructor.
	 * <p>
	 * Creates a login object in the LoggedOut state.
	 */
	public Login() {
		super();
		this.changed_ = new Signal(this);
		this.user_ = new User();
	}

	/**
	 * Logs a user in.
	 * <p>
	 * A user can be logged in using either a DisabledLogin, WeakLogin or
	 * StrongLogin <code>state</code>. The login state is forced to
	 * DisabledLogin if {@link User#getStatus() User#getStatus()} returns
	 * Disabled.
	 * <p>
	 * 
	 * @see Login#logout()
	 * @see Login#isLoggedIn()
	 */
	public void login(User user, LoginState state) {
		boolean weakLogin = state == LoginState.WeakLogin;
		if (!user.equals(this.user_)) {
			this.user_ = user;
			this.weakLogin_ = weakLogin;
			this.changed_.trigger();
		} else {
			if (this.user_.isValid() && weakLogin != this.weakLogin_) {
				this.weakLogin_ = weakLogin;
				this.changed_.trigger();
			}
		}
	}

	/**
	 * Logs a user in.
	 * <p>
	 * Calls {@link #login(User user, LoginState state) login(user,
	 * LoginState.StrongLogin)}
	 */
	public final void login(User user) {
		login(user, LoginState.StrongLogin);
	}

	/**
	 * Logs the current user out.
	 * <p>
	 * Sets the state to LoggedOut.
	 */
	public void logout() {
		if (this.user_.isValid()) {
			this.user_ = new User();
			this.changed_.trigger();
		}
	}

	/**
	 * Returns the current login state.
	 * <p>
	 * 
	 * @see Login#login(User user, LoginState state)
	 * @see Login#logout()
	 */
	public LoginState getState() {
		if (this.user_.isValid()) {
			if (this.user_.getStatus() == User.Status.Normal) {
				if (this.weakLogin_) {
					return LoginState.WeakLogin;
				} else {
					return LoginState.StrongLogin;
				}
			} else {
				return LoginState.DisabledLogin;
			}
		} else {
			return LoginState.LoggedOut;
		}
	}

	/**
	 * Returns whether a user has successfully logged in.
	 * <p>
	 * This returns <code>true</code> only if the state is WeakLogin or
	 * StrongLogin.
	 * <p>
	 * 
	 * @see Login#getState()
	 */
	public boolean isLoggedIn() {
		return this.user_.isValid()
				&& this.user_.getStatus() == User.Status.Normal;
	}

	/**
	 * Returns the user currently identified.
	 * <p>
	 * Returns the user currently identified.
	 * <p>
	 * <p>
	 * <i><b>Note: </b>This may also be a user whose account is currently
	 * disabled. </i>
	 * </p>
	 */
	public User getUser() {
		return this.user_;
	}

	/**
	 * Signal that indicates login changes.
	 * <p>
	 * This signal is emitted as a result of
	 * {@link Login#login(User user, LoginState state) login()} or
	 * {@link Login#logout() logout()}.
	 */
	public Signal changed() {
		return this.changed_;
	}

	private Signal changed_;
	private User user_;
	private boolean weakLogin_;
}
