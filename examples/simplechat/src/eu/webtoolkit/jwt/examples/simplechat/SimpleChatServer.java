package eu.webtoolkit.jwt.examples.simplechat;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import eu.webtoolkit.jwt.Signal1;
import eu.webtoolkit.jwt.WObject;

/**
 * A simple chat server
 */
public class SimpleChatServer extends WObject {

	/**
	 * Create a new chat server.
	 */
	public SimpleChatServer() {
		chatEvent_ = new Signal1<ChatEvent>();
	}

	/**
	 * Try to login with given user name.
	 * 
	 * Returns false if the login was not successfull.
	 */
	public boolean login(final String user) {
		mutex_.lock();
		try {
			if (!users_.contains(user)) {
				users_.add(user);

				chatEvent_.trigger(new ChatEvent(ChatEvent.Type.Login, user));

				return true;
			} else
				return false;
		} finally {
			mutex_.unlock();
		}
	}

	/**
	 * Logout from the server.
	 */
	public void logout(final String user) {
		mutex_.lock();

		if (users_.contains(user)) {
			users_.remove(user);

			chatEvent_.trigger(new ChatEvent(ChatEvent.Type.Logout, user));
		}

		mutex_.unlock();
	}

	/**
	 * Get a suggestion for a guest user name.
	 */
	public String suggestGuest() {
		mutex_.lock();
		try {
			for (int i = 1;; ++i) {
				String s = "guest " + i;

				if (!users_.contains(s))
					return s;
			}
		} finally {
			mutex_.unlock();
		}
	}

	/**
	 * Send a message on behalve of a user.
	 */
	public void sendMessage(final String user, final String message) {
		mutex_.lock();
		chatEvent_.trigger(new ChatEvent(user, message));
		mutex_.unlock();
	}

	/**
	 * %Signal that will convey chat events.
	 * 
	 * Every client should connect to this signal, and process events.
	 */
	public Signal1<ChatEvent> chatEvent() {
		return chatEvent_;
	}

	/**
	 * Get the users currently logged in.
	 */
	public Set<String> users() {
		return users_;
	}

	private Signal1<ChatEvent> chatEvent_;
	private ReentrantLock mutex_ = new ReentrantLock();

	private Set<String> users_ = new HashSet<String>();
}
