package eu.webtoolkit.jwt;

/**
 * Abstract base class for signals that may be triggered from the browser with
 * a JavaScript call.
 */
public abstract class AbstractJSignal extends AbstractEventSignal {

	private String name;

	protected AbstractJSignal(WObject sender, String name) {
		super(name, sender);

		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	protected String getEncodeCmd() {
		return getSender().getFormName() + "." + name;
	}

	protected String createUserEventCall(String jsObject, String jsEvent, String arg1, String arg2, String arg3, String arg4, String arg5, String arg6) {

		return createUserEventCall(jsObject, jsEvent, name, arg1, arg2, arg3, arg4, arg5, arg6);
	}

	@SuppressWarnings("unchecked")
	protected Object unMarshal(JavaScriptEvent jse, int index, Class toClass) {
		try {
			if (toClass == String.class)
				return jse.userEventArgs.get(index);
			else if (toClass == Integer.class)
				return Integer.valueOf(jse.userEventArgs.get(index));
			else if (toClass == WString.class)
				return new WString(jse.userEventArgs.get(index));
			else if (toClass == Double.class)
				return Double.valueOf(jse.userEventArgs.get(index));
			else if (toClass == WMouseEvent.class)
				return new WMouseEvent(jse);
			else if (toClass == WKeyEvent.class)
				return new WKeyEvent(jse);
			else
				System.err.println("Unsupported JSignal type: " + toClass.getName());
		} catch (NumberFormatException e) {
			throw new WtException("JSignal: could not interpret argument " + index + " ('" + jse.userEventArgs.get(index) + "') as type " + toClass.getName(),
					e);
		} catch (IndexOutOfBoundsException e) {
			throw new WtException("Jsignal: not enough arguments in JavaScript call", e);
		}

		return null;
	}
}