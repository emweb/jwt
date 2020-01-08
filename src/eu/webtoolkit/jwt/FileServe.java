/*
 * Copyright (C) 2009 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;

class FileServe {
	public FileServe(String contents) {
		this.template_ = contents;
		this.currentPos_ = 0;
		this.vars_ = new HashMap<String, String>();
		this.conditions_ = new HashMap<String, Boolean>();
	}

	public void setCondition(String name, boolean value) {
		this.conditions_.put(name, value);
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

	public void stream(StringBuilder out) throws IOException {
		this.streamUntil(out, "");
	}

	public void streamUntil(StringBuilder out, String until) throws IOException {
		String currentVar = "";
		boolean readingVar = false;
		int start = currentPos_;
		int noMatchConditions = 0;

		for (; currentPos_ < template_.length(); ++currentPos_) {
			if (readingVar) {
				if (template_.startsWith("_$_", currentPos_)) {
					if (currentVar.charAt(0) == '$') {
						int _pos = currentVar.indexOf('_');
						String fname = _pos == -1 ? currentVar.substring(1) : currentVar.substring(1, _pos);

						currentPos_ += 2; // skip ()
						
						if (fname.equals("endif")) {
							if (noMatchConditions > 0)
								--noMatchConditions;
						} else {
							String farg = currentVar.substring(_pos + 1);

							Boolean i = conditions_.get(farg);

							if (i == null)
								throw new WtException("Internal error: could not find condition: " + farg);

							boolean c = i;
							if (fname.equals("if"))
								;
							else if (fname.equals("ifnot"))
								c = !c;

							if (!c || noMatchConditions > 0)
								++noMatchConditions;
						}
					} else {
						if (currentVar.equals(until)) {
							this.currentPos_ += 3;
							return;
						}

						String v = vars_.get(currentVar);
						if (v == null) {
							throw new WtException("Internal error: could not find variable: " + currentVar);
						}

						if (noMatchConditions == 0)
							out.append(v);					
					}

					readingVar = false;
					start = this.currentPos_ + 3;
					this.currentPos_ += 2;
				} else {
					currentVar += template_.charAt(currentPos_);
				}
			} else {
				if (template_.startsWith("_$_", currentPos_)) {
					if (noMatchConditions == 0 && (currentPos_ - start > 0)) {
						out.append(this.template_.substring(start, this.currentPos_));
					}
					this.currentPos_ += 2;
					readingVar = true;
					currentVar = "";
				}
			}
		}
		if (noMatchConditions == 0 && (currentPos_ - start > 0)) {
			out.append(this.template_.substring(start, this.currentPos_));
		}
	}

	private final String template_;
	private int currentPos_;
	private HashMap<String, String> vars_;
	private HashMap<String, Boolean> conditions_;
}
