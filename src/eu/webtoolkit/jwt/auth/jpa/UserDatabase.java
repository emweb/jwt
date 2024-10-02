package eu.webtoolkit.jwt.auth.jpa;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.webtoolkit.jwt.WDate;
import eu.webtoolkit.jwt.auth.AbstractUserDatabase;
import eu.webtoolkit.jwt.auth.AccountStatus;
import eu.webtoolkit.jwt.auth.AuthService;
import eu.webtoolkit.jwt.auth.EmailTokenRole;
import eu.webtoolkit.jwt.auth.IdentityPolicy;
import eu.webtoolkit.jwt.auth.PasswordHash;
import eu.webtoolkit.jwt.auth.Token;
import eu.webtoolkit.jwt.auth.User;

/**
 * A JPA implementation for user authentication data.
 */
public class UserDatabase extends AbstractUserDatabase {
	private int openTransactions = 0;
	private boolean commitTransaction = true;
	
	private static Logger logger = LoggerFactory.getLogger(UserDatabase.class);

	/**
	 * Constructor
	 */
	public UserDatabase(EntityManager entityManager) {
		this(entityManager, null);
	}

	/**
	 * Constructor
	 */
	public UserDatabase(EntityManager entityManager, AuthService authService) {
		entityManager_ = entityManager;
		authService_ = authService;
		maxAuthTokensPerUser_ = 50;
	}

	@Override
	public Transaction startTransaction() {
		return new TransactionImpl(this);
	}

	/**
	 * Returns the {@link AuthInfo} object corresponding to an {@link User}.
	 */
	public AuthInfo find(User user) {
		return findAuthInfo(user.getId());
	}

	private AuthInfo findAuthInfo(String id) {
		long id_long = Long.parseLong(id);
		return entityManager_.find(AuthInfo.class, id_long);
	}

	/**
	 * Returns the {@link User} corresponding to an {@link AuthInfo} object.
	 */
	public User find(AuthInfo user) {
		return new User(user.getId() + "", this);
	}

	public User findWithId(String id) {
		AuthInfo ai = findAuthInfo(id);
		if (ai != null)
			return new User(id, this);
		else
			return new User();
	}

	@Override
	public User findWithIdentity(String provider, String identity) {
		String q = 
			"select a_info " +
			"	from AuthInfo a_info, AuthIdentity a_id " +
			"	where a_id.provider = :provider" +
			"		and a_id.authInfo.id = a_info.id";
		if (authService_ != null && authService_.getIdentityPolicy() == IdentityPolicy.EmailAddress) {
			q += " and LOWER(a_id.identity) = LOWER(:identity)";
		} else {
			q += " and a_id.identity = :identity";
		}

		Query query = entityManager_.createQuery(q);
		query.setParameter("provider", provider);
		query.setParameter("identity", identity);
		List<AuthInfo> ai = (List<AuthInfo>) query.getResultList();

		if (ai.size() == 1)
			return new User(ai.get(0).getId() + "", this);
		else
			return new User();
	}

	@Override
	public String getIdentity(User user, String provider) {
		String q = "select a_id from AuthIdentity a_id "
				+ "	where a_id.authInfo.id = :user_id"
				+ "		and a_id.provider = :provider";

		Query query = entityManager_.createQuery(q);
		query.setParameter("user_id", Long.parseLong(user.getId()));
		query.setParameter("provider", provider);

		List<AuthIdentity> result = (List<AuthIdentity>) query.getResultList();
		if (result.size() == 1)
			return result.get(0).getIdentity();
		else
			return "";
	}

	@Override
	public void removeIdentity(User user, String provider) {
		String q = "delete from AuthIdentity a_id"
				+ "	where a_id.authInfo.id = :user_id"
				+ "		and a_id.provider = :provider";

		Query query = entityManager_.createQuery(q);
		query.setParameter("user_id", Long.parseLong(user.getId()));
		query.setParameter("provider", provider);
		query.executeUpdate();
	}

	@Override
	public User registerNew() {
		AuthInfo ai = new AuthInfo();
		entityManager_.persist(ai);

		return new User(ai.getId() + "", this);
	}

	@Override
	public AccountStatus getStatus(User user) {
		AuthInfo ai = findAuthInfo(user.getId());
		return ai.getStatus();
	}

	@Override
	public void setPassword(User user, PasswordHash password) {
		AuthInfo ai = findAuthInfo(user.getId());
		ai.setPassword(password.getValue(), password.getFunction(),
				password.getSalt());
	}

	@Override
	public PasswordHash getPassword(User user) {
		AuthInfo ai = findAuthInfo(user.getId());
		return new PasswordHash(ai.getPasswordMethod(), ai.getPasswordSalt(),
				ai.getPasswordHash());
	}

	@Override
	public void addIdentity(User user, String provider, String identity) {
		Transaction t = startTransaction();
		User u = findWithIdentity(provider, identity);

		if (u.isValid()) {
			logger.error("cannot add identity " + provider + ":'" + identity
					+ "': already exists");
		} else {
			AuthInfo ai = findAuthInfo(user.getId());
			AuthIdentity a_id = new AuthIdentity(provider, identity);
			ai.getAuthIdentities().add(a_id);
			a_id.setAuthInfo(ai);
			entityManager_.persist(ai);
		}
    t.commit();
	}

	@Override
	public boolean setEmail(User user, String address) {
		Query query = entityManager_
				.createQuery("select a_info from AuthInfo a_info where LOWER(email) = LOWER(:email)");
		query.setParameter("email", address);
		if (query.getResultList().size() != 0) {
			return false;
		} else {
			AuthInfo ai = findAuthInfo(user.getId());
			ai.setEmail(address);
			return true;
		}
	}

	@Override
	public String getEmail(User user) {
		AuthInfo ai = findAuthInfo(user.getId());
		return ai.getEmail();
	}

	@Override
	public void setUnverifiedEmail(User user, String address) {
		AuthInfo ai = findAuthInfo(user.getId());
		ai.setUnverifiedEmail(address);
	}

	@Override
	public String getUnverifiedEmail(User user) {
		AuthInfo ai = findAuthInfo(user.getId());
		return ai.getUnverifiedEmail();
	}

	@Override
	public User findWithEmail(String address) {
		Query query = entityManager_
				.createQuery("select a_info from AuthInfo a_info where LOWER(email) = LOWER(:email)");
		query.setParameter("email", address);
		List<AuthInfo> result = (List<AuthInfo>) query.getResultList();
		if (result.size() == 1)
			return new User(result.get(0).getId() + "", this);
		else
			return new User();
	}

	@Override
	public void setEmailToken(User user, Token token, EmailTokenRole role) {
		Transaction t = startTransaction();
		AuthInfo ai = findAuthInfo(user.getId());
		WDate expirationTime = token.getExpirationTime();
		ai.setEmailToken(token.getHash(), expirationTime == null ? null : expirationTime.getDate(),	role);
		t.commit();
	}

	@Override
	public Token getEmailToken(User user) {
		AuthInfo ai = findAuthInfo(user.getId());
		return new Token(ai.getEmailToken(), new WDate(
				ai.getEmailTokenExpires()));
	}

	@Override
	public EmailTokenRole getEmailTokenRole(User user) {
		AuthInfo ai = findAuthInfo(user.getId());
		return ai.getEmailTokenRole();
	}

	@Override
	public User findWithEmailToken(String token) {
		Query query = entityManager_
				.createQuery("select a_info from AuthInfo a_info where email_token = :token");
		query.setParameter("token", token);
		List<AuthInfo> result = (List<AuthInfo>) query.getResultList();
		if (result.size() == 1)
			return new User(result.get(0).getId() + "", this);
		else
			return new User();
	}

	@Override
	public void addAuthToken(User user, Token token) {
		// This should be statistically very unlikely but also a big
		// security problem if we do not detect it ...
		Query query = entityManager_
				.createQuery("select a_token from AuthToken a_token where value = :token_hash");
		query.setParameter("token_hash", token.getHash());
		if (query.getResultList().size() > 0)
			throw new RuntimeException("Token hash collision");

		// Prevent a user from piling up the database with tokens
		AuthInfo ai = find(user);
		if (ai.getAuthTokens().size() > maxAuthTokensPerUser_)
			return;

		AuthToken at = new AuthToken(token.getHash(), token.getExpirationTime()
				.getDate());
		ai.getAuthTokens().add(at);
		at.setAuthInfo(ai);
		entityManager_.persist(at);
	}
	
	@Override
	public void removeAuthToken(User user, String hash) {
		String q = "delete from AuthToken a_token"
				+ "	where a_token.authInfo.id = :user_id"
				+ "		and a_token.value = :hash";

		Query query = entityManager_.createQuery(q);
		query.setParameter("user_id", Long.parseLong(user.getId()));
		query.setParameter("hash", hash);
		query.executeUpdate();
	}

	@Override
	public User findWithAuthToken(String hash) {
		String q = "select a_info " 
				+ " from AuthToken a_token,  AuthInfo a_info"
				+ "	where a_token.value = :hash"
				+ "		and a_token.expiryDate > :expiryDate"
				+ "	 	and a_token.authInfo.id = a_info.id";

		Query query = entityManager_.createQuery(q);
		query.setParameter("hash", hash);
		query.setParameter("expiryDate", WDate.getCurrentDate().getDate());
		List<AuthInfo> ai = (List<AuthInfo>) query.getResultList();

		if (ai.size() == 1)
			return new User(ai.get(0).getId() + "", this);
		else
			return new User();
	}

	@Override
	public void setFailedLoginAttempts(User user, int count) {
		AuthInfo ai = findAuthInfo(user.getId());
		ai.setFailedLoginAttempts(count);
	}

	@Override
	public int getFailedLoginAttempts(User user) {
		AuthInfo ai = findAuthInfo(user.getId());
		return ai.getFailedLoginAttempts();
	}

	@Override
	public void setLastLoginAttempt(User user, WDate d) {
		AuthInfo ai = findAuthInfo(user.getId());
		ai.setLastLoginAttempt(d.getDate());
	}

	@Override
	public WDate getLastLoginAttempt(User user) {
		AuthInfo ai = findAuthInfo(user.getId());
		return new WDate(ai.getLastLoginAttempt());
	}

	private EntityManager entityManager_;
	private AuthService authService_;
	private int maxAuthTokensPerUser_;

	private class TransactionImpl implements Transaction {
		private UserDatabase userDatabase;
		private boolean done = false;
		
		public TransactionImpl(UserDatabase userDatabase) {
			this.userDatabase = userDatabase;
			
			if (userDatabase.openTransactions == 0) 
				beginTransaction();
			
			++userDatabase.openTransactions;
		}

		public void commit() {
			done = true;

			--userDatabase.openTransactions;
			
			if (userDatabase.openTransactions == 0) 
				endTransaction();
		}

		public void rollback() {
			done = true;

			--userDatabase.openTransactions;
			userDatabase.commitTransaction = false;
			
			if (userDatabase.openTransactions == 0) 
				endTransaction();
		}
		
		private void beginTransaction() {
			entityManager_.getTransaction().begin();
			userDatabase.commitTransaction = true;
		}
		
		private void endTransaction() {
			if (userDatabase.commitTransaction)
				entityManager_.getTransaction().commit();
			else
				entityManager_.getTransaction().rollback();
		}

		public void close() {
			if (!done) {
				rollback();
			}
		}
	}
}
