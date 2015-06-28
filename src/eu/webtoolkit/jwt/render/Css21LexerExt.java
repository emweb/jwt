package eu.webtoolkit.jwt.render;

import org.antlr.runtime.CharStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;

public class Css21LexerExt extends Css21Lexer {
	public String lastError_ = "";
	
	public Css21LexerExt() {} 
	public Css21LexerExt(CharStream input) {
		super(input);
	}
	public Css21LexerExt(CharStream input, RecognizerSharedState state) {
		super(input,state);
	}
	
	
	public boolean hasError_ = false;
	public void displayRecognitionError(String[] tokenNames, RecognitionException e)
	{
		hasError_ = true;
		lastError_ += "stylesheetText(): " + getErrorHeader(e) + " " + getErrorMessage(e, tokenNames) + "\n";
	}
}
