package eu.webtoolkit.jwt;

import java.util.ArrayList;
import java.util.List;

public class WString implements Comparable<WString>, CharSequence {
	public static final WString Empty = new WString();

	private String key;
	private String value;
	private ArrayList<String> arguments;

	public WString() {
		this.value = "";
		this.key = null;
		this.arguments = null;
	}

	public WString(String s) {
		set(s);
	}
	
	public WString(CharSequence cs) {
		set(cs.toString());
	}

	void set(String s) {
		this.value = s;
		this.key = null;
		this.arguments = null;
	}

	public WString(char c) {
		this(c + "");
	}

	private WString(String key, boolean b) {
		this.value = "";
		this.key = key;
		this.arguments = null;
	}

	public WString(char[] buf) {
		this(new String(buf));
	}

	public boolean equals(WString rhs) {
		return getValue().equals(rhs.getValue());
	}

	public boolean lessThen(WString rhs) {
		return getValue().compareTo(rhs.getValue()) > 0;
	}

	public WString append(WString rhs) {
		makeLiteral();
		value += rhs.getValue();
		return this;
	}

	public WString append(String rhs) {
		makeLiteral();
		value += rhs;
		return this;
	}

	public void append(char[] buf) {
		this.append(new String(buf));
	}

	public boolean isEmpty() {
		return getValue().length() == 0;
	}

	public String getValue() {
		String result = value;

		if (key != null)
			result = WApplication.getInstance().getLocalizedStrings().resolveKey(key);

		if (arguments != null) {
			for (int i = 0; i < arguments.size(); ++i) {
				String key = '{' + String.valueOf(i + 1) + '}';
				String arg = arguments.get(i);
				if (arg == null)
					arg = "null";
				result = result.replace(key, arg);
			}
		}
		return result;
	}

	public int compareTo(WString arg0) {
		return getValue().compareTo(arg0.getValue());
	}

	public static WString tr(String key) {
		return new WString(key, true);
	}

	public boolean isLiteral() {
		return key == null;
	}

	public String getKey() {
		return key == null ? "" : key;
	}

	public WString arg(String value) {
		if (arguments == null)
			arguments = new ArrayList<String>();
		arguments.add(value == null ? "" : value);

		return this;
	}

	public WString arg(WString value) {
		if (arguments == null)
			arguments = new ArrayList<String>();
		arguments.add(value.getValue());

		return this;
	}

	public WString arg(int value) {
		if (arguments == null)
			arguments = new ArrayList<String>();
		arguments.add(String.valueOf(value));

		return this;
	}

	public WString arg(double value) {
		if (arguments == null)
			arguments = new ArrayList<String>();
		arguments.add(String.valueOf(value));

		return this;
	}

	public List<String> getArgs() {
		return arguments == null ? stArguments : arguments;
	}

	public boolean refresh() {
		if (isLiteral())
			return false;
		else
			return true;
	}

	public String getJsStringLiteral(char delimiter) {
		return WWebWidget.jsStringLiteral(delimiter + "");
	}

	public String getJsStringLiteral() {
		return getJsStringLiteral('\'');
	}

	private void makeLiteral() {
		if (key != null) {
			value = WApplication.getInstance().getLocalizedStrings().resolveKey(key);
			key = null;
		}
	}

	public void changeArg(int argIndex, String value) {
		arguments.set(argIndex, value);
	}

	public void changeArg(int argIndex, int value) {
		this.changeArg(argIndex, value + "");
	}

	public void changeArg(int argIndex, double value) {
		this.changeArg(argIndex, value + "");
	}
	
	public String toString() {
		return getValue();
	}

	private static List<String> stArguments;
	
	public static WString toWString(CharSequence cs) {
		return cs instanceof WString ? (WString)cs : new WString(cs);
	}

	public char charAt(int index) {
		return getValue().charAt(index);
	}

	public int length() {
		return getValue().length();
	}
	
	public int getLength() {
		return length();
	}

	public CharSequence subSequence(int start, int end) {
		return getValue().subSequence(start, end);
	}
}
