package eu.webtoolkit.jwt.examples.simplechat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WObject;
import eu.webtoolkit.jwt.WtServlet;

/**
 * A simple chat server
 */
public class SimpleChatServer extends WObject {
	private WtServlet servlet;

	static class ClientInfo {
		ClientInfo(WApplication app) {
			this.app = app;
		}

		WApplication app;
	}
	
	/**
	 * Create a new chat server.
	 */
	public SimpleChatServer(WtServlet servlet) {
		this.servlet = servlet;
	}

	public synchronized boolean connect(ChatClient client) {
		if (!clients.containsKey(client)) {
			ClientInfo clientInfo = new ClientInfo(WApplication.getInstance());
			clients.put(client, clientInfo);
			return true;
		} else
			return false;
	}
	
	public synchronized boolean disconnect(ChatClient client) {
		return clients.remove(client) != null;
	}
	
	/**
	 * Try to login with given user name.
	 * 
	 * Returns false if the login was not successful.
	 */
	public synchronized boolean login(final String user) {
		if (!users_.contains(user)) {
			users_.add(user);

			postChatEvent(new ChatEvent(ChatEvent.Type.Login, user));

			return true;
		} else
			return false;
	}

	/**
	 * Logout from the server.
	 */
	public synchronized void logout(final String user) {
		if (users_.contains(user)) {
			users_.remove(user);

			postChatEvent(new ChatEvent(ChatEvent.Type.Logout, user));
		}
	}

	/**
	 * Get a suggestion for a guest user name.
	 */
	public synchronized String suggestGuest() {
		for (int i = 1;; ++i) {
			String s = "guest " + i;

			if (!users_.contains(s))
				return s;
		}
	}

	/**
	 * Send a message on behalve of a user.
	 */
	public synchronized void sendMessage(final String user, final String message) {
		postChatEvent(new ChatEvent(user, message));
	}

	private void postChatEvent(final ChatEvent chatEvent) {
		for (Entry<ChatClient, ClientInfo> c : clients.entrySet()) {
			final ChatClient client = c.getKey();
			servlet.post(c.getValue().app, new Runnable() {
				@Override
				public void run() {
					client.processChatEvent(chatEvent);
				}
			}, null);
		}
	}

	/**
	 * Get the users currently logged in.
	 */
	public Set<String> users() {
		return users_;
	}

	private Set<String> users_ = new HashSet<String>();
	private Map<ChatClient, ClientInfo> clients = new HashMap<ChatClient, ClientInfo>();
}
