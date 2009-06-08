package eu.webtoolkit.jwt;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;

public class FileServe {
	public FileServe(String contents) {
		this.template_ = contents;
		this.currentPos_ = 0;
		this.vars_ = new HashMap<String, String>();
	}

	public void setVar(String name, String value) {
		this.vars_.put(name, value);
	}

	public void setVar(String name, boolean value) {
		setVar(name, value ? "true" : "false");
	}

	public void setVar(String name, int i) {
		setVar(name, String.valueOf(i));
	}

	public void stream(Writer out) throws IOException {
		this.streamUntil(out, "");
	}

	public void streamUntil(Writer out, String until) throws IOException {
		String currentVar = "";
		boolean readingVar = false;
		int start = currentPos_;

		for (; currentPos_ < template_.length(); ++currentPos_) {
			if (readingVar) {
				if (template_.startsWith("_$_", currentPos_)) {
					if (currentVar.equals(until)) {
						this.currentPos_ += 3;
						return;
					}

					String v = vars_.get(currentVar);
					if (v == null) {
						throw new WtException("Internal error: could not find variable: " + currentVar);
					}

					out.append(v);

					readingVar = false;
					start = this.currentPos_ + 3;
					this.currentPos_ += 2;
				} else {
					currentVar += template_.charAt(currentPos_);
				}
			} else {
				if (template_.startsWith("_$_", currentPos_)) {
					if (currentPos_ - start > 0) {
						out.append(this.template_.substring(start, this.currentPos_));
					}
					this.currentPos_ += 2;
					readingVar = true;
					currentVar = "";
				}
			}
		}
		if (this.currentPos_ - start > 0) {
			out.append(this.template_.substring(start, this.currentPos_));
		}
	}

	private final String template_;
	private int currentPos_;
	private HashMap<String, String> vars_;
}
