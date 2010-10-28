package eu.webtoolkit.jwt.examples.simplechat;

public class ChatEvent {

	/**
	 * Enumeration for the event type.
	 */
	public enum Type {
		Login, Logout, Message
	};

	/**
	 * Get the event type.
	 */
	public Type type() {
		return type_;
	}

	/**
	 * Get the user who caused the event.
	 */
	public final String user() {
		return user_;
	}

	/**
	 * Get the message of the event.
	 */
	public final String message() {
		return message_;
	}

	/**
	 * Get the message formatted as HTML, rendered for the given user.
	 */
	public final CharSequence formattedHTML(final String user) {
		switch (type_) {
		case Login:
			return "<span class='chat-info'>" + user_
					+ " joined the conversation.</span>";
		case Logout:
			return "<span class='chat-info'>"
					+ ((user.equals(user_)) ? "You" : user_)
					+ " logged out.</span>";
		case Message: {
			String result;

			result = "<span class='"
					+ ((user.equals(user_)) ? "chat-self" : "chat-user") + "'>"
					+ user_ + ":</span>";

			if (message_.toString().contains(user.toString()))
				return result + "<span class='chat-highlight'>" + message_
						+ "</span>";
			else
				return result + message_;
		}
		default:
			return "";
		}
	}

	private Type type_;
	private String user_;
	private String message_;

	/*
	 * Both user and html will be formatted as html
	 */
	ChatEvent(final String user, final String message)

	{
		type_ = Type.Message;
		user_ = user;
		message_ = message;
	}

	ChatEvent(Type type, final String user)

	{
		type_ = type;
		user_ = user;
	}

}
