package eu.webtoolkit.jwt.render;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CssParser {
	private static final Logger logger = LoggerFactory.getLogger(CssParser.class);
	
	static SimpleSelectorImpl currentSimpleSelector = new SimpleSelectorImpl();
	static SelectorImpl currentSelector = new SelectorImpl();
	static List<SelectorImpl> currentSelectorList = new ArrayList<SelectorImpl>(); 
	static RulesetImpl currentRuleset = new RulesetImpl();
	public static StyleSheetImpl currentStylesheet = new StyleSheetImpl();
	String lastError_ = null;
	
	static void setSimpleSelectorElementName(String s)
	{
		currentSimpleSelector.setElementName(s);
	}
	
	static void setSimpleSelectorHash(String s)
	{
		s = s.substring(1);
		if(currentSimpleSelector.getHashId().isEmpty())
			currentSimpleSelector.setHash(s);
	}
	
	static void addSimpleSelectorClass(String s)
	{
		s = s.substring(1);
		currentSimpleSelector.getClasses().add(s);
	}
	
	static void pushCurrentSimpleSelector()
	{
		currentSelector.addSimpleSelector(currentSimpleSelector);
		currentSimpleSelector = new SimpleSelectorImpl();
	}
	
	static void pushCurrentSelector()
	{
		currentSelectorList.add(currentSelector);
		currentSelector = new SelectorImpl();
	}
	
	static void setAndPushDeclarationBlock(String s)
	{
		for (int i = 0; i < currentSelectorList.size(); ++i) {
			RulesetImpl r = new RulesetImpl();
			r.block_.declarationString_ = s;
			r.selector_ = currentSelectorList.get(i);
			currentStylesheet.rulesetArray_.add(r);
		}
		currentSelectorList.clear();
	}
	
	public StyleSheet parseFile(CharSequence stylesheetContents)
	{	
		Css21LexerExt lex = null;
		Css21ParserExt parser = null;
		try {
			lex = new Css21LexerExt(new ANTLRFileStream(stylesheetContents.toString()));
			CommonTokenStream tokens = new CommonTokenStream(lex);
	        parser = new Css21ParserExt(tokens);
	        currentStylesheet = new StyleSheetImpl();
        
            parser.styleSheet();
            lastError_ = lex.lastError_ + parser.lastError_;
            return !lex.hasError_ && !parser.hasError_ ? currentStylesheet : null;
        } catch (RecognitionException e)  {
            //e.printStackTrace();
        	lastError_ = lex.lastError_ + parser.lastError_;
            return null;
        } catch (IOException e) {
			//e.printStackTrace();
        	lastError_ = "file" + stylesheetContents + "not found.";
			return null;
		}
	}

	public StyleSheet parse(CharSequence stylesheetContents) {
		Css21LexerExt lex = new Css21LexerExt(new ANTLRStringStream(stylesheetContents.toString()));
		CommonTokenStream tokens = new CommonTokenStream(lex);
        Css21ParserExt parser = new Css21ParserExt(tokens);
        currentStylesheet = new StyleSheetImpl();
 
        try {
            parser.styleSheet();
            lastError_ = lex.lastError_ + parser.lastError_;
            return !lex.hasError_ && !parser.hasError_ ? currentStylesheet : null;
        } catch (RecognitionException e)  {
            logger.info("Exception parsing style sheet", e);
            logger.trace("stylesheet was: {}", stylesheetContents);
            lastError_ = lex.lastError_ + parser.lastError_;
            return null;
        }
	}
	
	String getLastError()
	{
		return lastError_;
	}

}
