package eu.webtoolkit.jwt.render;

import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.TokenStream;

public class Css21ParserExt extends Css21Parser {
	String filename = "";
	String lastError_ = "";
	
	public Css21ParserExt(TokenStream input/*, String filename = ""*/) {
		super(input);
		//this.filename = filename;
	}
	public Css21ParserExt(TokenStream input, RecognizerSharedState state) {
		super(input, state);
	}
	
	public boolean hasError_ = false;
	public void displayRecognitionError(String[] tokenNames, RecognitionException e)
	{
		hasError_ = true;
		lastError_ += "stylesheetText(): " + getErrorHeader(e) + " " + getErrorMessage(e, tokenNames) + "\n";
	}
}
