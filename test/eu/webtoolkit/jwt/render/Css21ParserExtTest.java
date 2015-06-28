package eu.webtoolkit.jwt.render;

import static org.junit.Assert.*;

import org.junit.Test;

import eu.webtoolkit.jwt.render.CssParser;

public class Css21ParserExtTest {
	private void localAssert(StyleSheet s)
	{
		assertTrue("", s != null);
	}
	
	private void localAssert(boolean b)
	{
		assertTrue("", b);
	}
	
	@Test
	public void testCss21Parser() throws Exception {
		CssParser parser = new CssParser();
		
        assertTrue( "", parser.parse("h1{test:\"bla\"}")  != null );
        assertTrue( "", parser.parse("h1{test;:\"bla\"}") == null );
        StyleSheet s  = parser.parse(
              "h1 { color: green }  h1 h2, h1 h3{color: red}");
        localAssert( s );
        
        localAssert( s.getRulesetSize() == 3 );
        localAssert( s.rulesetAt(1).getSelector().getSize() == 2 );
        localAssert( s.rulesetAt(2).getSelector().getSize() == 2 );

        StyleSheet s2 = parser.parse("h1 h2 h3 h4 {color: green}");
        localAssert( s2 );
        localAssert( s2.getRulesetSize() == 1 );
        localAssert( s2.rulesetAt(0).getSelector().getSize()  == 4 );

        localAssert(  parser.parse("h1 h2 h3 & h4 {inside: ok}") == null );
        localAssert(  parser.parse("h1{}") );
        localAssert(  parser.parse("1h{}") == null );
        localAssert(  parser.parse("h 1{}") == null );
        localAssert(  parser.parse(".class1{}") );
        localAssert(  parser.parse(".1class{}") == null );
        localAssert( !parser.getLastError().isEmpty());
        localAssert(  parser.parse("#id1{}") );
        localAssert(  parser.getLastError().isEmpty());
        //localAssert(  parser.parse("#1id{}") == null ); // Not possible using a Lexer
        localAssert(  parser.parse("{}") == null );
        localAssert( !parser.getLastError().isEmpty());
        localAssert(  parser.parse("id_{id_:boo}") );
        StyleSheet s6 = parser.parse("a{inside:\"}b{\"}");
        localAssert(  s6 );
        localAssert(  s6.getRulesetSize() == 1 );
        localAssert(  parser.parse("h1{ a: a; b: b }") );
        localAssert(  parser.parse("h1{ a: a; b: b; }") );
        localAssert(  parser.parse("h1{ a: 2em }") );

        StyleSheet s3 = parser.parse(".class1.class2{}");
        localAssert( s3 );
        localAssert( s3.getRulesetSize() == 1 );
        localAssert( s3.rulesetAt(0).getSelector().getSize() == 1 );
        
        localAssert( s3.rulesetAt(0).getSelector().at(0).getClasses().get(0).equals("class1") );
        localAssert( s3.rulesetAt(0).getSelector().at(0).getClasses().get(1).equals("class2") );
        StyleSheet s4 = parser.parse(".class1 .class2{}");
        localAssert( s4 );
        localAssert( s4.getRulesetSize() == 1 );
        localAssert( s4.rulesetAt(0).getSelector().getSize() == 2 );
        localAssert( s4.rulesetAt(0).getSelector().at(0).getClasses().get(0).equals("class1") );
        localAssert( s4.rulesetAt(0).getSelector().at(1).getClasses().get(0).equals("class2") );

        StyleSheet s5 = parser.parse(
              "h1{color: 20px; something: blue; something_else: \"bla\" }");
        localAssert( s5 );
        localAssert( s5.getRulesetSize() == 1 );

        // Test hex color
        localAssert(  parser.parse("h1{color:#123}") );
        localAssert(  parser.parse("h1{color:#a11}") );
        //localAssert(  parser.parse("h1{color:#11}") == null ); // Not possible using a Lexer
        localAssert(  parser.parse("h1{color:#123456}") );
        //localAssert(  parser.parse("h1{color:#ggg}") == null ); // Not possible using a Lexer

        // Test multi term expressions
        localAssert( parser.parse("h1{test: .1px}") );
        localAssert( parser.parse("hr{border: 1px 1px}") );
        localAssert( parser.parse("hr{outline: thin dotted invert}") );

        // Test comments
        localAssert( parser.parse("/*bla*/ h1{}") );
        localAssert( parser.parse("h1{/*bla*/ test: 1px}") );
        localAssert( parser.parse("h1{test: /*bla*/1px}") );
        localAssert( parser.parse("h1{test: /*bla* */1px}") );
        localAssert( parser.parse("<!--bla--> h1{test: 1px}") );
        //localAssert( parser.parse("h1{test: 1px <!--bla--> }") ); // This is not allowed?
        //localAssert( parser.parse("h1{test: 1px <!--b-l-a--> }") ); // This is not allowed?

        // Test white spaces
        localAssert( parser.parse("h1{test:\t\n\r\f 1px}") );

        // Test URI's
        localAssert( parser.parse("h1{test:url(\'bla\')}") );
        localAssert( parser.parse("h1{test:url(\"bla\")}") );
        localAssert( parser.parse("h1{test:url(bla)}") );
        localAssert( parser.parse("h1{test:url( \"bla\" )}") );
        localAssert( parser.parse("h1{test:url( \t\n\f\r \"bla\" )}") );
        localAssert( parser.parse("h1{test:url(\"folder\\image.gif\" )}") );
        
        // Test an actual file
        localAssert( parser.parseFile("") == null );
        localAssert( parser.parseFile("bin/eu/webtoolkit/jwt/wt-resources/html4_default.css") );
        
        // Test this
        StyleSheet sheet = parser.parse(
        		".list-bom {\n" +
        		"   border-collapse: separate;\n" +
        		"  	font-size: 16px;\n" +
        		"  	width: 100%;\n" +
        		"  	border: 2px solid black;\n" +
        		"}\n" +
        		"\n" +
        		".list-bom td, .list-bom th {\n" +
        		"  	padding: 3px;\n" +
        		"}\n" +
        		"\n" +
        		".list-bom td {\n" +
        		"   border-left: 1px solid #c0c0c0;\n" +
        		"}\n" +
        		"\n" +
        		".list-bom td.totals {\n" +
        		"  	border-top: 2px solid black;\n" +
        		"}\n");
        if (sheet == null)
        	System.err.println(parser.getLastError());
        localAssert(sheet);
	}

}