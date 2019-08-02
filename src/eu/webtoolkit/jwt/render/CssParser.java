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
	
	private SimpleSelectorImpl currentSimpleSelector = new SimpleSelectorImpl();
	private SelectorImpl currentSelector = new SelectorImpl();
	private List<SelectorImpl> currentSelectorList = new ArrayList<SelectorImpl>(); 
	private RulesetImpl currentRuleset = new RulesetImpl();
	private StyleSheetImpl currentStylesheet = new StyleSheetImpl();
	private String lastError_ = null;
	
	void setSimpleSelectorElementName(String s)
	{
		currentSimpleSelector.setElementName(s);
	}
	
	void setSimpleSelectorHash(String s)
	{
		s = s.substring(1);
		if(currentSimpleSelector.getHashId().isEmpty())
			currentSimpleSelector.setHash(s);
	}
	
	void addSimpleSelectorClass(String s)
	{
		s = s.substring(1);
		currentSimpleSelector.getClasses().add(s);
	}
	
	void pushCurrentSimpleSelector()
	{
		currentSelector.addSimpleSelector(currentSimpleSelector);
		currentSimpleSelector = new SimpleSelectorImpl();
	}
	
	void pushCurrentSelector()
	{
		currentSelectorList.add(currentSelector);
		currentSelector = new SelectorImpl();
	}
	
	void setAndPushDeclarationBlock(String s)
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
		parser.setTarget(this);
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
	parser.setTarget(this);
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
