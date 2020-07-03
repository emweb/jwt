package eu.webtoolkit.jwt.render;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ConsoleErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CssParser {
	private static final Logger logger = LoggerFactory.getLogger(CssParser.class);

	private ErrorListener errorListener_ = new ErrorListener();

	static final class Listener extends Css22BaseListener {
		private SimpleSelectorImpl currentSimpleSelector = new SimpleSelectorImpl();
		private SelectorImpl currentSelector = new SelectorImpl();
		private List<SelectorImpl> currentSelectorList = new ArrayList<>();
		private StyleSheetImpl currentStylesheet = new StyleSheetImpl();

		Listener() {
		}

		StyleSheetImpl getCurrentStylesheet() {
			return currentStylesheet;
		}

		@Override
		public void exitSelector(Css22Parser.SelectorContext ctx) {
			currentSelectorList.add(currentSelector);
			currentSelector = new SelectorImpl();
		}

		@Override
		public void exitSimpleSelector(Css22Parser.SimpleSelectorContext ctx) {
			currentSelector.addSimpleSelector(currentSimpleSelector);
			currentSimpleSelector = new SimpleSelectorImpl();
		}

		@Override
		public void exitElementName(Css22Parser.ElementNameContext ctx) {
			currentSimpleSelector.setElementName(ctx.getText());
		}

		@Override
		public void exitClassName(Css22Parser.ClassNameContext ctx) {
			currentSimpleSelector.getClasses().add(ctx.IDENT().getText());
		}

		@Override
		public void exitId(Css22Parser.IdContext ctx) {
			String h = ctx.getText().substring(1);
			if(currentSimpleSelector.getHashId().isEmpty())
				currentSimpleSelector.setHash(h);
		}

		@Override
		public void exitDeclarationBlock(Css22Parser.DeclarationBlockContext ctx) {
			for (SelectorImpl selector : currentSelectorList) {
				RulesetImpl r = new RulesetImpl();
				r.block_.declarationString_ = ctx.getText();
				r.selector_ = selector;
				currentStylesheet.rulesetArray_.add(r);
			}
			currentSelectorList.clear();
		}
	}

	static final class ErrorListener extends ConsoleErrorListener {
		private String lastError = "";

		@Override
		public void syntaxError(
				Recognizer<?,?> recognizer,
				Object offendingSymbol,
				int line,
				int charPositionInLine,
				String msg,
				RecognitionException e) {
			lastError = "line " + line + ":" + charPositionInLine + " " + msg;
		}

		public void reset() {
			lastError = "";
		}

		public String getLastError() {
			return lastError;
		}
	}

	public StyleSheet parseFile(CharSequence fileName)
	{
		try {
			CharStream stream = CharStreams.fromFileName(fileName.toString());
			StyleSheet result = parse(stream);
			if (result == null) {
				logger.info("Error parsing stylesheet: {}", getLastError());
				logger.trace("stylesheet was: {}", fileName);
			}
			return result;
		} catch (IOException e) {
			logger.info("Exception parsing stylesheet", e);
			logger.trace("stylesheet was: {}", fileName);
			return null;
		}
	}

	public StyleSheet parse(CharSequence stylesheetContents) {
		StyleSheet result = parse(CharStreams.fromString(stylesheetContents.toString()));
		if (result == null) {
			logger.info("Error parsing stylesheet: {}", getLastError());
			logger.trace("stylesheet was: {}", stylesheetContents);
		}
		return result;
	}

	private StyleSheet parse(CharStream stream) throws RecognitionException, ParseCancellationException {
		errorListener_.reset();

		Listener listener = new Listener();

		Css22Lexer lex = new Css22Lexer(stream);
		lex.removeErrorListener(ConsoleErrorListener.INSTANCE);
		lex.addErrorListener(errorListener_);

		CommonTokenStream tokens = new CommonTokenStream(lex);
		Css22Parser parser = new Css22Parser(tokens);
		parser.addParseListener(listener);
		parser.removeErrorListener(ConsoleErrorListener.INSTANCE);
		parser.addErrorListener(errorListener_);

		parser.styleSheet();

		if (!getLastError().isEmpty()) {
			return null;
		} else {
			return listener.getCurrentStylesheet();
		}
	}
	
	String getLastError()
	{
		return errorListener_.getLastError();
	}

}
