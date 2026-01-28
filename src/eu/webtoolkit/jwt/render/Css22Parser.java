// Generated from Css22.g4 by ANTLR 4.7.2
package eu.webtoolkit.jwt.render;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class Css22Parser extends Parser {
	static { RuntimeMetaData.checkVersion("4.7.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		WSP=1, COMMENT=2, BADCOMMENT=3, CDO=4, CDC=5, INCLUDES=6, DASHMATCH=7, 
		STRING=8, BADSTRING=9, IDENT=10, HASH=11, IMPORT_SYM=12, PAGE_SYM=13, 
		MEDIA_SYM=14, CHARSET_SYM=15, IMPORTANT_SYM=16, EMS=17, EXS=18, LENGTH=19, 
		ANGLE=20, TIME=21, FREQ=22, DIMENSION=23, PERCENTAGE=24, NUMBER=25, URI=26, 
		BADURI=27, FUNCTION=28, ASTERISK=29, EQ=30, DOT=31, PLUS=32, GT=33, SLASH=34, 
		COMMA=35, COLON=36, SEMICOLON=37, LPAREN=38, RPAREN=39, LBRACE=40, RBRACE=41, 
		LBRACKET=42, RBRACKET=43;
	public static final int
		RULE_styleSheet = 0, RULE_importStmt = 1, RULE_media = 2, RULE_mediaList = 3, 
		RULE_medium = 4, RULE_page = 5, RULE_pseudoPage = 6, RULE_operator = 7, 
		RULE_combinator = 8, RULE_property = 9, RULE_ruleset = 10, RULE_declarationBlock = 11, 
		RULE_selector = 12, RULE_simpleSelector = 13, RULE_id = 14, RULE_className = 15, 
		RULE_elementName = 16, RULE_attrib = 17, RULE_pseudo = 18, RULE_declaration = 19, 
		RULE_prio = 20, RULE_expr = 21, RULE_term = 22, RULE_function = 23, RULE_hexcolor = 24;
	private static String[] makeRuleNames() {
		return new String[] {
			"styleSheet", "importStmt", "media", "mediaList", "medium", "page", "pseudoPage", 
			"operator", "combinator", "property", "ruleset", "declarationBlock", 
			"selector", "simpleSelector", "id", "className", "elementName", "attrib", 
			"pseudo", "declaration", "prio", "expr", "term", "function", "hexcolor"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, null, null, null, "'<!--'", "'-->'", "'~='", "'|='", null, null, 
			null, null, null, null, null, "'@charset'", null, null, null, null, null, 
			null, null, null, null, null, null, null, null, "'*'", "'='", "'.'", 
			"'+'", "'>'", "'/'", "','", "':'", "';'", "'('", "')'", "'{'", "'}'", 
			"'['", "']'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "WSP", "COMMENT", "BADCOMMENT", "CDO", "CDC", "INCLUDES", "DASHMATCH", 
			"STRING", "BADSTRING", "IDENT", "HASH", "IMPORT_SYM", "PAGE_SYM", "MEDIA_SYM", 
			"CHARSET_SYM", "IMPORTANT_SYM", "EMS", "EXS", "LENGTH", "ANGLE", "TIME", 
			"FREQ", "DIMENSION", "PERCENTAGE", "NUMBER", "URI", "BADURI", "FUNCTION", 
			"ASTERISK", "EQ", "DOT", "PLUS", "GT", "SLASH", "COMMA", "COLON", "SEMICOLON", 
			"LPAREN", "RPAREN", "LBRACE", "RBRACE", "LBRACKET", "RBRACKET"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "Css22.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public Css22Parser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class StyleSheetContext extends ParserRuleContext {
		public TerminalNode EOF() { return getToken(Css22Parser.EOF, 0); }
		public TerminalNode CHARSET_SYM() { return getToken(Css22Parser.CHARSET_SYM, 0); }
		public TerminalNode STRING() { return getToken(Css22Parser.STRING, 0); }
		public TerminalNode SEMICOLON() { return getToken(Css22Parser.SEMICOLON, 0); }
		public List<ImportStmtContext> importStmt() {
			return getRuleContexts(ImportStmtContext.class);
		}
		public ImportStmtContext importStmt(int i) {
			return getRuleContext(ImportStmtContext.class,i);
		}
		public List<TerminalNode> WSP() { return getTokens(Css22Parser.WSP); }
		public TerminalNode WSP(int i) {
			return getToken(Css22Parser.WSP, i);
		}
		public List<TerminalNode> CDO() { return getTokens(Css22Parser.CDO); }
		public TerminalNode CDO(int i) {
			return getToken(Css22Parser.CDO, i);
		}
		public List<TerminalNode> CDC() { return getTokens(Css22Parser.CDC); }
		public TerminalNode CDC(int i) {
			return getToken(Css22Parser.CDC, i);
		}
		public List<RulesetContext> ruleset() {
			return getRuleContexts(RulesetContext.class);
		}
		public RulesetContext ruleset(int i) {
			return getRuleContext(RulesetContext.class,i);
		}
		public List<MediaContext> media() {
			return getRuleContexts(MediaContext.class);
		}
		public MediaContext media(int i) {
			return getRuleContext(MediaContext.class,i);
		}
		public List<PageContext> page() {
			return getRuleContexts(PageContext.class);
		}
		public PageContext page(int i) {
			return getRuleContext(PageContext.class,i);
		}
		public StyleSheetContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_styleSheet; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof Css22Listener ) ((Css22Listener)listener).enterStyleSheet(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof Css22Listener ) ((Css22Listener)listener).exitStyleSheet(this);
		}
	}

	public final StyleSheetContext styleSheet() throws RecognitionException {
		StyleSheetContext _localctx = new StyleSheetContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_styleSheet);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(53);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==CHARSET_SYM) {
				{
				setState(50);
				match(CHARSET_SYM);
				setState(51);
				match(STRING);
				setState(52);
				match(SEMICOLON);
				}
			}

			setState(58);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << WSP) | (1L << CDO) | (1L << CDC))) != 0)) {
				{
				{
				setState(55);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << WSP) | (1L << CDO) | (1L << CDC))) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				}
				setState(60);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(83);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==IMPORT_SYM) {
				{
				{
				setState(61);
				importStmt();
				setState(78);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CDO || _la==CDC) {
					{
					setState(76);
					_errHandler.sync(this);
					switch (_input.LA(1)) {
					case CDO:
						{
						setState(62);
						match(CDO);
						setState(66);
						_errHandler.sync(this);
						_la = _input.LA(1);
						while (_la==WSP) {
							{
							{
							setState(63);
							match(WSP);
							}
							}
							setState(68);
							_errHandler.sync(this);
							_la = _input.LA(1);
						}
						}
						break;
					case CDC:
						{
						setState(69);
						match(CDC);
						setState(73);
						_errHandler.sync(this);
						_la = _input.LA(1);
						while (_la==WSP) {
							{
							{
							setState(70);
							match(WSP);
							}
							}
							setState(75);
							_errHandler.sync(this);
							_la = _input.LA(1);
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					}
					setState(80);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				}
				setState(85);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(112);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << IDENT) | (1L << HASH) | (1L << PAGE_SYM) | (1L << MEDIA_SYM) | (1L << ASTERISK) | (1L << DOT) | (1L << COLON) | (1L << LBRACKET))) != 0)) {
				{
				{
				setState(89);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case IDENT:
				case HASH:
				case ASTERISK:
				case DOT:
				case COLON:
				case LBRACKET:
					{
					setState(86);
					ruleset();
					}
					break;
				case MEDIA_SYM:
					{
					setState(87);
					media();
					}
					break;
				case PAGE_SYM:
					{
					setState(88);
					page();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(107);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CDO || _la==CDC) {
					{
					setState(105);
					_errHandler.sync(this);
					switch (_input.LA(1)) {
					case CDO:
						{
						setState(91);
						match(CDO);
						setState(95);
						_errHandler.sync(this);
						_la = _input.LA(1);
						while (_la==WSP) {
							{
							{
							setState(92);
							match(WSP);
							}
							}
							setState(97);
							_errHandler.sync(this);
							_la = _input.LA(1);
						}
						}
						break;
					case CDC:
						{
						setState(98);
						match(CDC);
						setState(102);
						_errHandler.sync(this);
						_la = _input.LA(1);
						while (_la==WSP) {
							{
							{
							setState(99);
							match(WSP);
							}
							}
							setState(104);
							_errHandler.sync(this);
							_la = _input.LA(1);
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					}
					setState(109);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				}
				setState(114);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(115);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ImportStmtContext extends ParserRuleContext {
		public TerminalNode IMPORT_SYM() { return getToken(Css22Parser.IMPORT_SYM, 0); }
		public TerminalNode SEMICOLON() { return getToken(Css22Parser.SEMICOLON, 0); }
		public TerminalNode STRING() { return getToken(Css22Parser.STRING, 0); }
		public TerminalNode URI() { return getToken(Css22Parser.URI, 0); }
		public List<TerminalNode> WSP() { return getTokens(Css22Parser.WSP); }
		public TerminalNode WSP(int i) {
			return getToken(Css22Parser.WSP, i);
		}
		public MediaListContext mediaList() {
			return getRuleContext(MediaListContext.class,0);
		}
		public ImportStmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_importStmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof Css22Listener ) ((Css22Listener)listener).enterImportStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof Css22Listener ) ((Css22Listener)listener).exitImportStmt(this);
		}
	}

	public final ImportStmtContext importStmt() throws RecognitionException {
		ImportStmtContext _localctx = new ImportStmtContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_importStmt);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(117);
			match(IMPORT_SYM);
			setState(121);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==WSP) {
				{
				{
				setState(118);
				match(WSP);
				}
				}
				setState(123);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(124);
			_la = _input.LA(1);
			if ( !(_la==STRING || _la==URI) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(128);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==WSP) {
				{
				{
				setState(125);
				match(WSP);
				}
				}
				setState(130);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(132);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==IDENT) {
				{
				setState(131);
				mediaList();
				}
			}

			setState(134);
			match(SEMICOLON);
			setState(138);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==WSP) {
				{
				{
				setState(135);
				match(WSP);
				}
				}
				setState(140);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class MediaContext extends ParserRuleContext {
		public TerminalNode MEDIA_SYM() { return getToken(Css22Parser.MEDIA_SYM, 0); }
		public MediaListContext mediaList() {
			return getRuleContext(MediaListContext.class,0);
		}
		public TerminalNode LBRACE() { return getToken(Css22Parser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(Css22Parser.RBRACE, 0); }
		public List<TerminalNode> WSP() { return getTokens(Css22Parser.WSP); }
		public TerminalNode WSP(int i) {
			return getToken(Css22Parser.WSP, i);
		}
		public List<RulesetContext> ruleset() {
			return getRuleContexts(RulesetContext.class);
		}
		public RulesetContext ruleset(int i) {
			return getRuleContext(RulesetContext.class,i);
		}
		public MediaContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_media; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof Css22Listener ) ((Css22Listener)listener).enterMedia(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof Css22Listener ) ((Css22Listener)listener).exitMedia(this);
		}
	}

	public final MediaContext media() throws RecognitionException {
		MediaContext _localctx = new MediaContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_media);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(141);
			match(MEDIA_SYM);
			setState(145);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==WSP) {
				{
				{
				setState(142);
				match(WSP);
				}
				}
				setState(147);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(148);
			mediaList();
			setState(149);
			match(LBRACE);
			setState(153);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==WSP) {
				{
				{
				setState(150);
				match(WSP);
				}
				}
				setState(155);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(159);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << IDENT) | (1L << HASH) | (1L << ASTERISK) | (1L << DOT) | (1L << COLON) | (1L << LBRACKET))) != 0)) {
				{
				{
				setState(156);
				ruleset();
				}
				}
				setState(161);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(162);
			match(RBRACE);
			setState(166);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==WSP) {
				{
				{
				setState(163);
				match(WSP);
				}
				}
				setState(168);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class MediaListContext extends ParserRuleContext {
		public List<MediumContext> medium() {
			return getRuleContexts(MediumContext.class);
		}
		public MediumContext medium(int i) {
			return getRuleContext(MediumContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(Css22Parser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(Css22Parser.COMMA, i);
		}
		public List<TerminalNode> WSP() { return getTokens(Css22Parser.WSP); }
		public TerminalNode WSP(int i) {
			return getToken(Css22Parser.WSP, i);
		}
		public MediaListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_mediaList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof Css22Listener ) ((Css22Listener)listener).enterMediaList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof Css22Listener ) ((Css22Listener)listener).exitMediaList(this);
		}
	}

	public final MediaListContext mediaList() throws RecognitionException {
		MediaListContext _localctx = new MediaListContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_mediaList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(169);
			medium();
			setState(180);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(170);
				match(COMMA);
				setState(174);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==WSP) {
					{
					{
					setState(171);
					match(WSP);
					}
					}
					setState(176);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(177);
				medium();
				}
				}
				setState(182);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class MediumContext extends ParserRuleContext {
		public TerminalNode IDENT() { return getToken(Css22Parser.IDENT, 0); }
		public List<TerminalNode> WSP() { return getTokens(Css22Parser.WSP); }
		public TerminalNode WSP(int i) {
			return getToken(Css22Parser.WSP, i);
		}
		public MediumContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_medium; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof Css22Listener ) ((Css22Listener)listener).enterMedium(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof Css22Listener ) ((Css22Listener)listener).exitMedium(this);
		}
	}

	public final MediumContext medium() throws RecognitionException {
		MediumContext _localctx = new MediumContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_medium);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(183);
			match(IDENT);
			setState(187);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==WSP) {
				{
				{
				setState(184);
				match(WSP);
				}
				}
				setState(189);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PageContext extends ParserRuleContext {
		public TerminalNode PAGE_SYM() { return getToken(Css22Parser.PAGE_SYM, 0); }
		public TerminalNode LBRACE() { return getToken(Css22Parser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(Css22Parser.RBRACE, 0); }
		public List<TerminalNode> WSP() { return getTokens(Css22Parser.WSP); }
		public TerminalNode WSP(int i) {
			return getToken(Css22Parser.WSP, i);
		}
		public PseudoPageContext pseudoPage() {
			return getRuleContext(PseudoPageContext.class,0);
		}
		public List<DeclarationContext> declaration() {
			return getRuleContexts(DeclarationContext.class);
		}
		public DeclarationContext declaration(int i) {
			return getRuleContext(DeclarationContext.class,i);
		}
		public List<TerminalNode> SEMICOLON() { return getTokens(Css22Parser.SEMICOLON); }
		public TerminalNode SEMICOLON(int i) {
			return getToken(Css22Parser.SEMICOLON, i);
		}
		public PageContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_page; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof Css22Listener ) ((Css22Listener)listener).enterPage(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof Css22Listener ) ((Css22Listener)listener).exitPage(this);
		}
	}

	public final PageContext page() throws RecognitionException {
		PageContext _localctx = new PageContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_page);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(190);
			match(PAGE_SYM);
			setState(194);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==WSP) {
				{
				{
				setState(191);
				match(WSP);
				}
				}
				setState(196);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(198);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COLON) {
				{
				setState(197);
				pseudoPage();
				}
			}

			setState(200);
			match(LBRACE);
			setState(204);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==WSP) {
				{
				{
				setState(201);
				match(WSP);
				}
				}
				setState(206);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(208);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==IDENT) {
				{
				setState(207);
				declaration();
				}
			}

			setState(222);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==SEMICOLON) {
				{
				{
				setState(210);
				match(SEMICOLON);
				setState(214);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==WSP) {
					{
					{
					setState(211);
					match(WSP);
					}
					}
					setState(216);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(218);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==IDENT) {
					{
					setState(217);
					declaration();
					}
				}

				}
				}
				setState(224);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(225);
			match(RBRACE);
			setState(229);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==WSP) {
				{
				{
				setState(226);
				match(WSP);
				}
				}
				setState(231);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PseudoPageContext extends ParserRuleContext {
		public TerminalNode COLON() { return getToken(Css22Parser.COLON, 0); }
		public TerminalNode IDENT() { return getToken(Css22Parser.IDENT, 0); }
		public List<TerminalNode> WSP() { return getTokens(Css22Parser.WSP); }
		public TerminalNode WSP(int i) {
			return getToken(Css22Parser.WSP, i);
		}
		public PseudoPageContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pseudoPage; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof Css22Listener ) ((Css22Listener)listener).enterPseudoPage(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof Css22Listener ) ((Css22Listener)listener).exitPseudoPage(this);
		}
	}

	public final PseudoPageContext pseudoPage() throws RecognitionException {
		PseudoPageContext _localctx = new PseudoPageContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_pseudoPage);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(232);
			match(COLON);
			setState(233);
			match(IDENT);
			setState(237);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==WSP) {
				{
				{
				setState(234);
				match(WSP);
				}
				}
				setState(239);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class OperatorContext extends ParserRuleContext {
		public TerminalNode SLASH() { return getToken(Css22Parser.SLASH, 0); }
		public List<TerminalNode> WSP() { return getTokens(Css22Parser.WSP); }
		public TerminalNode WSP(int i) {
			return getToken(Css22Parser.WSP, i);
		}
		public TerminalNode COMMA() { return getToken(Css22Parser.COMMA, 0); }
		public OperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_operator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof Css22Listener ) ((Css22Listener)listener).enterOperator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof Css22Listener ) ((Css22Listener)listener).exitOperator(this);
		}
	}

	public final OperatorContext operator() throws RecognitionException {
		OperatorContext _localctx = new OperatorContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_operator);
		int _la;
		try {
			setState(254);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SLASH:
				enterOuterAlt(_localctx, 1);
				{
				setState(240);
				match(SLASH);
				setState(244);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==WSP) {
					{
					{
					setState(241);
					match(WSP);
					}
					}
					setState(246);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			case COMMA:
				enterOuterAlt(_localctx, 2);
				{
				setState(247);
				match(COMMA);
				setState(251);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==WSP) {
					{
					{
					setState(248);
					match(WSP);
					}
					}
					setState(253);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CombinatorContext extends ParserRuleContext {
		public TerminalNode PLUS() { return getToken(Css22Parser.PLUS, 0); }
		public List<TerminalNode> WSP() { return getTokens(Css22Parser.WSP); }
		public TerminalNode WSP(int i) {
			return getToken(Css22Parser.WSP, i);
		}
		public TerminalNode GT() { return getToken(Css22Parser.GT, 0); }
		public CombinatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_combinator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof Css22Listener ) ((Css22Listener)listener).enterCombinator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof Css22Listener ) ((Css22Listener)listener).exitCombinator(this);
		}
	}

	public final CombinatorContext combinator() throws RecognitionException {
		CombinatorContext _localctx = new CombinatorContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_combinator);
		int _la;
		try {
			setState(270);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case PLUS:
				enterOuterAlt(_localctx, 1);
				{
				setState(256);
				match(PLUS);
				setState(260);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==WSP) {
					{
					{
					setState(257);
					match(WSP);
					}
					}
					setState(262);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			case GT:
				enterOuterAlt(_localctx, 2);
				{
				setState(263);
				match(GT);
				setState(267);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==WSP) {
					{
					{
					setState(264);
					match(WSP);
					}
					}
					setState(269);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PropertyContext extends ParserRuleContext {
		public TerminalNode IDENT() { return getToken(Css22Parser.IDENT, 0); }
		public List<TerminalNode> WSP() { return getTokens(Css22Parser.WSP); }
		public TerminalNode WSP(int i) {
			return getToken(Css22Parser.WSP, i);
		}
		public PropertyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_property; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof Css22Listener ) ((Css22Listener)listener).enterProperty(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof Css22Listener ) ((Css22Listener)listener).exitProperty(this);
		}
	}

	public final PropertyContext property() throws RecognitionException {
		PropertyContext _localctx = new PropertyContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_property);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(272);
			match(IDENT);
			setState(276);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==WSP) {
				{
				{
				setState(273);
				match(WSP);
				}
				}
				setState(278);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class RulesetContext extends ParserRuleContext {
		public List<SelectorContext> selector() {
			return getRuleContexts(SelectorContext.class);
		}
		public SelectorContext selector(int i) {
			return getRuleContext(SelectorContext.class,i);
		}
		public TerminalNode LBRACE() { return getToken(Css22Parser.LBRACE, 0); }
		public DeclarationBlockContext declarationBlock() {
			return getRuleContext(DeclarationBlockContext.class,0);
		}
		public TerminalNode RBRACE() { return getToken(Css22Parser.RBRACE, 0); }
		public List<TerminalNode> COMMA() { return getTokens(Css22Parser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(Css22Parser.COMMA, i);
		}
		public List<TerminalNode> WSP() { return getTokens(Css22Parser.WSP); }
		public TerminalNode WSP(int i) {
			return getToken(Css22Parser.WSP, i);
		}
		public RulesetContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ruleset; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof Css22Listener ) ((Css22Listener)listener).enterRuleset(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof Css22Listener ) ((Css22Listener)listener).exitRuleset(this);
		}
	}

	public final RulesetContext ruleset() throws RecognitionException {
		RulesetContext _localctx = new RulesetContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_ruleset);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(279);
			selector();
			setState(290);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(280);
				match(COMMA);
				setState(284);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==WSP) {
					{
					{
					setState(281);
					match(WSP);
					}
					}
					setState(286);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(287);
				selector();
				}
				}
				setState(292);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(293);
			match(LBRACE);
			setState(297);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==WSP) {
				{
				{
				setState(294);
				match(WSP);
				}
				}
				setState(299);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(300);
			declarationBlock();
			setState(301);
			match(RBRACE);
			setState(305);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==WSP) {
				{
				{
				setState(302);
				match(WSP);
				}
				}
				setState(307);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DeclarationBlockContext extends ParserRuleContext {
		public List<DeclarationContext> declaration() {
			return getRuleContexts(DeclarationContext.class);
		}
		public DeclarationContext declaration(int i) {
			return getRuleContext(DeclarationContext.class,i);
		}
		public List<TerminalNode> SEMICOLON() { return getTokens(Css22Parser.SEMICOLON); }
		public TerminalNode SEMICOLON(int i) {
			return getToken(Css22Parser.SEMICOLON, i);
		}
		public List<TerminalNode> WSP() { return getTokens(Css22Parser.WSP); }
		public TerminalNode WSP(int i) {
			return getToken(Css22Parser.WSP, i);
		}
		public DeclarationBlockContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_declarationBlock; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof Css22Listener ) ((Css22Listener)listener).enterDeclarationBlock(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof Css22Listener ) ((Css22Listener)listener).exitDeclarationBlock(this);
		}
	}

	public final DeclarationBlockContext declarationBlock() throws RecognitionException {
		DeclarationBlockContext _localctx = new DeclarationBlockContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_declarationBlock);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(309);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==IDENT) {
				{
				setState(308);
				declaration();
				}
			}

			setState(323);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==SEMICOLON) {
				{
				{
				setState(311);
				match(SEMICOLON);
				setState(315);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==WSP) {
					{
					{
					setState(312);
					match(WSP);
					}
					}
					setState(317);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(319);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==IDENT) {
					{
					setState(318);
					declaration();
					}
				}

				}
				}
				setState(325);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SelectorContext extends ParserRuleContext {
		public List<SimpleSelectorContext> simpleSelector() {
			return getRuleContexts(SimpleSelectorContext.class);
		}
		public SimpleSelectorContext simpleSelector(int i) {
			return getRuleContext(SimpleSelectorContext.class,i);
		}
		public List<CombinatorContext> combinator() {
			return getRuleContexts(CombinatorContext.class);
		}
		public CombinatorContext combinator(int i) {
			return getRuleContext(CombinatorContext.class,i);
		}
		public List<TerminalNode> WSP() { return getTokens(Css22Parser.WSP); }
		public TerminalNode WSP(int i) {
			return getToken(Css22Parser.WSP, i);
		}
		public SelectorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_selector; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof Css22Listener ) ((Css22Listener)listener).enterSelector(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof Css22Listener ) ((Css22Listener)listener).exitSelector(this);
		}
	}

	public final SelectorContext selector() throws RecognitionException {
		SelectorContext _localctx = new SelectorContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_selector);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(326);
			simpleSelector();
			setState(341);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,51,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					setState(339);
					_errHandler.sync(this);
					switch (_input.LA(1)) {
					case PLUS:
					case GT:
						{
						setState(327);
						combinator();
						setState(328);
						simpleSelector();
						}
						break;
					case WSP:
						{
						setState(331); 
						_errHandler.sync(this);
						_la = _input.LA(1);
						do {
							{
							{
							setState(330);
							match(WSP);
							}
							}
							setState(333); 
							_errHandler.sync(this);
							_la = _input.LA(1);
						} while ( _la==WSP );
						setState(336);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if (_la==PLUS || _la==GT) {
							{
							setState(335);
							combinator();
							}
						}

						setState(338);
						simpleSelector();
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					} 
				}
				setState(343);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,51,_ctx);
			}
			setState(347);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==WSP) {
				{
				{
				setState(344);
				match(WSP);
				}
				}
				setState(349);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SimpleSelectorContext extends ParserRuleContext {
		public ElementNameContext elementName() {
			return getRuleContext(ElementNameContext.class,0);
		}
		public List<IdContext> id() {
			return getRuleContexts(IdContext.class);
		}
		public IdContext id(int i) {
			return getRuleContext(IdContext.class,i);
		}
		public List<ClassNameContext> className() {
			return getRuleContexts(ClassNameContext.class);
		}
		public ClassNameContext className(int i) {
			return getRuleContext(ClassNameContext.class,i);
		}
		public List<AttribContext> attrib() {
			return getRuleContexts(AttribContext.class);
		}
		public AttribContext attrib(int i) {
			return getRuleContext(AttribContext.class,i);
		}
		public List<PseudoContext> pseudo() {
			return getRuleContexts(PseudoContext.class);
		}
		public PseudoContext pseudo(int i) {
			return getRuleContext(PseudoContext.class,i);
		}
		public SimpleSelectorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_simpleSelector; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof Css22Listener ) ((Css22Listener)listener).enterSimpleSelector(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof Css22Listener ) ((Css22Listener)listener).exitSimpleSelector(this);
		}
	}

	public final SimpleSelectorContext simpleSelector() throws RecognitionException {
		SimpleSelectorContext _localctx = new SimpleSelectorContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_simpleSelector);
		int _la;
		try {
			setState(368);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IDENT:
			case ASTERISK:
				enterOuterAlt(_localctx, 1);
				{
				setState(350);
				elementName();
				setState(357);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << HASH) | (1L << DOT) | (1L << COLON) | (1L << LBRACKET))) != 0)) {
					{
					setState(355);
					_errHandler.sync(this);
					switch (_input.LA(1)) {
					case HASH:
						{
						setState(351);
						id();
						}
						break;
					case DOT:
						{
						setState(352);
						className();
						}
						break;
					case LBRACKET:
						{
						setState(353);
						attrib();
						}
						break;
					case COLON:
						{
						setState(354);
						pseudo();
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					}
					setState(359);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			case HASH:
			case DOT:
			case COLON:
			case LBRACKET:
				enterOuterAlt(_localctx, 2);
				{
				setState(364); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					setState(364);
					_errHandler.sync(this);
					switch (_input.LA(1)) {
					case HASH:
						{
						setState(360);
						id();
						}
						break;
					case DOT:
						{
						setState(361);
						className();
						}
						break;
					case LBRACKET:
						{
						setState(362);
						attrib();
						}
						break;
					case COLON:
						{
						setState(363);
						pseudo();
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					}
					setState(366); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << HASH) | (1L << DOT) | (1L << COLON) | (1L << LBRACKET))) != 0) );
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class IdContext extends ParserRuleContext {
		public TerminalNode HASH() { return getToken(Css22Parser.HASH, 0); }
		public IdContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_id; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof Css22Listener ) ((Css22Listener)listener).enterId(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof Css22Listener ) ((Css22Listener)listener).exitId(this);
		}
	}

	public final IdContext id() throws RecognitionException {
		IdContext _localctx = new IdContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_id);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(370);
			match(HASH);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ClassNameContext extends ParserRuleContext {
		public TerminalNode DOT() { return getToken(Css22Parser.DOT, 0); }
		public TerminalNode IDENT() { return getToken(Css22Parser.IDENT, 0); }
		public ClassNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_className; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof Css22Listener ) ((Css22Listener)listener).enterClassName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof Css22Listener ) ((Css22Listener)listener).exitClassName(this);
		}
	}

	public final ClassNameContext className() throws RecognitionException {
		ClassNameContext _localctx = new ClassNameContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_className);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(372);
			match(DOT);
			setState(373);
			match(IDENT);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ElementNameContext extends ParserRuleContext {
		public TerminalNode IDENT() { return getToken(Css22Parser.IDENT, 0); }
		public TerminalNode ASTERISK() { return getToken(Css22Parser.ASTERISK, 0); }
		public ElementNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_elementName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof Css22Listener ) ((Css22Listener)listener).enterElementName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof Css22Listener ) ((Css22Listener)listener).exitElementName(this);
		}
	}

	public final ElementNameContext elementName() throws RecognitionException {
		ElementNameContext _localctx = new ElementNameContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_elementName);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(375);
			_la = _input.LA(1);
			if ( !(_la==IDENT || _la==ASTERISK) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AttribContext extends ParserRuleContext {
		public TerminalNode LBRACKET() { return getToken(Css22Parser.LBRACKET, 0); }
		public List<TerminalNode> IDENT() { return getTokens(Css22Parser.IDENT); }
		public TerminalNode IDENT(int i) {
			return getToken(Css22Parser.IDENT, i);
		}
		public TerminalNode RBRACKET() { return getToken(Css22Parser.RBRACKET, 0); }
		public List<TerminalNode> WSP() { return getTokens(Css22Parser.WSP); }
		public TerminalNode WSP(int i) {
			return getToken(Css22Parser.WSP, i);
		}
		public TerminalNode EQ() { return getToken(Css22Parser.EQ, 0); }
		public TerminalNode INCLUDES() { return getToken(Css22Parser.INCLUDES, 0); }
		public TerminalNode DASHMATCH() { return getToken(Css22Parser.DASHMATCH, 0); }
		public TerminalNode STRING() { return getToken(Css22Parser.STRING, 0); }
		public AttribContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_attrib; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof Css22Listener ) ((Css22Listener)listener).enterAttrib(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof Css22Listener ) ((Css22Listener)listener).exitAttrib(this);
		}
	}

	public final AttribContext attrib() throws RecognitionException {
		AttribContext _localctx = new AttribContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_attrib);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(377);
			match(LBRACKET);
			setState(381);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==WSP) {
				{
				{
				setState(378);
				match(WSP);
				}
				}
				setState(383);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(384);
			match(IDENT);
			setState(388);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==WSP) {
				{
				{
				setState(385);
				match(WSP);
				}
				}
				setState(390);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(405);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << INCLUDES) | (1L << DASHMATCH) | (1L << EQ))) != 0)) {
				{
				setState(391);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << INCLUDES) | (1L << DASHMATCH) | (1L << EQ))) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(395);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==WSP) {
					{
					{
					setState(392);
					match(WSP);
					}
					}
					setState(397);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(398);
				_la = _input.LA(1);
				if ( !(_la==STRING || _la==IDENT) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(402);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==WSP) {
					{
					{
					setState(399);
					match(WSP);
					}
					}
					setState(404);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
			}

			setState(407);
			match(RBRACKET);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PseudoContext extends ParserRuleContext {
		public TerminalNode COLON() { return getToken(Css22Parser.COLON, 0); }
		public TerminalNode IDENT() { return getToken(Css22Parser.IDENT, 0); }
		public TerminalNode FUNCTION() { return getToken(Css22Parser.FUNCTION, 0); }
		public TerminalNode RPAREN() { return getToken(Css22Parser.RPAREN, 0); }
		public List<TerminalNode> WSP() { return getTokens(Css22Parser.WSP); }
		public TerminalNode WSP(int i) {
			return getToken(Css22Parser.WSP, i);
		}
		public PseudoContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pseudo; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof Css22Listener ) ((Css22Listener)listener).enterPseudo(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof Css22Listener ) ((Css22Listener)listener).exitPseudo(this);
		}
	}

	public final PseudoContext pseudo() throws RecognitionException {
		PseudoContext _localctx = new PseudoContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_pseudo);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(409);
			match(COLON);
			setState(428);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IDENT:
				{
				setState(410);
				match(IDENT);
				}
				break;
			case FUNCTION:
				{
				setState(411);
				match(FUNCTION);
				setState(415);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==WSP) {
					{
					{
					setState(412);
					match(WSP);
					}
					}
					setState(417);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(425);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==IDENT) {
					{
					setState(418);
					match(IDENT);
					setState(422);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==WSP) {
						{
						{
						setState(419);
						match(WSP);
						}
						}
						setState(424);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
				}

				setState(427);
				match(RPAREN);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DeclarationContext extends ParserRuleContext {
		public PropertyContext property() {
			return getRuleContext(PropertyContext.class,0);
		}
		public TerminalNode COLON() { return getToken(Css22Parser.COLON, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public List<TerminalNode> WSP() { return getTokens(Css22Parser.WSP); }
		public TerminalNode WSP(int i) {
			return getToken(Css22Parser.WSP, i);
		}
		public PrioContext prio() {
			return getRuleContext(PrioContext.class,0);
		}
		public DeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_declaration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof Css22Listener ) ((Css22Listener)listener).enterDeclaration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof Css22Listener ) ((Css22Listener)listener).exitDeclaration(this);
		}
	}

	public final DeclarationContext declaration() throws RecognitionException {
		DeclarationContext _localctx = new DeclarationContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_declaration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(430);
			property();
			setState(431);
			match(COLON);
			setState(435);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==WSP) {
				{
				{
				setState(432);
				match(WSP);
				}
				}
				setState(437);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(438);
			expr();
			setState(440);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==IMPORTANT_SYM) {
				{
				setState(439);
				prio();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PrioContext extends ParserRuleContext {
		public TerminalNode IMPORTANT_SYM() { return getToken(Css22Parser.IMPORTANT_SYM, 0); }
		public List<TerminalNode> WSP() { return getTokens(Css22Parser.WSP); }
		public TerminalNode WSP(int i) {
			return getToken(Css22Parser.WSP, i);
		}
		public PrioContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_prio; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof Css22Listener ) ((Css22Listener)listener).enterPrio(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof Css22Listener ) ((Css22Listener)listener).exitPrio(this);
		}
	}

	public final PrioContext prio() throws RecognitionException {
		PrioContext _localctx = new PrioContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_prio);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(442);
			match(IMPORTANT_SYM);
			setState(446);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==WSP) {
				{
				{
				setState(443);
				match(WSP);
				}
				}
				setState(448);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ExprContext extends ParserRuleContext {
		public List<TermContext> term() {
			return getRuleContexts(TermContext.class);
		}
		public TermContext term(int i) {
			return getRuleContext(TermContext.class,i);
		}
		public List<OperatorContext> operator() {
			return getRuleContexts(OperatorContext.class);
		}
		public OperatorContext operator(int i) {
			return getRuleContext(OperatorContext.class,i);
		}
		public ExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof Css22Listener ) ((Css22Listener)listener).enterExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof Css22Listener ) ((Css22Listener)listener).exitExpr(this);
		}
	}

	public final ExprContext expr() throws RecognitionException {
		ExprContext _localctx = new ExprContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_expr);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(449);
			term();
			setState(456);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << STRING) | (1L << IDENT) | (1L << HASH) | (1L << EMS) | (1L << EXS) | (1L << LENGTH) | (1L << ANGLE) | (1L << TIME) | (1L << FREQ) | (1L << PERCENTAGE) | (1L << NUMBER) | (1L << URI) | (1L << FUNCTION) | (1L << SLASH) | (1L << COMMA))) != 0)) {
				{
				{
				setState(451);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==SLASH || _la==COMMA) {
					{
					setState(450);
					operator();
					}
				}

				setState(453);
				term();
				}
				}
				setState(458);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TermContext extends ParserRuleContext {
		public TerminalNode NUMBER() { return getToken(Css22Parser.NUMBER, 0); }
		public TerminalNode PERCENTAGE() { return getToken(Css22Parser.PERCENTAGE, 0); }
		public TerminalNode LENGTH() { return getToken(Css22Parser.LENGTH, 0); }
		public TerminalNode EMS() { return getToken(Css22Parser.EMS, 0); }
		public TerminalNode EXS() { return getToken(Css22Parser.EXS, 0); }
		public TerminalNode ANGLE() { return getToken(Css22Parser.ANGLE, 0); }
		public TerminalNode TIME() { return getToken(Css22Parser.TIME, 0); }
		public TerminalNode FREQ() { return getToken(Css22Parser.FREQ, 0); }
		public List<TerminalNode> WSP() { return getTokens(Css22Parser.WSP); }
		public TerminalNode WSP(int i) {
			return getToken(Css22Parser.WSP, i);
		}
		public TerminalNode STRING() { return getToken(Css22Parser.STRING, 0); }
		public TerminalNode IDENT() { return getToken(Css22Parser.IDENT, 0); }
		public TerminalNode URI() { return getToken(Css22Parser.URI, 0); }
		public HexcolorContext hexcolor() {
			return getRuleContext(HexcolorContext.class,0);
		}
		public FunctionContext function() {
			return getRuleContext(FunctionContext.class,0);
		}
		public TermContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_term; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof Css22Listener ) ((Css22Listener)listener).enterTerm(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof Css22Listener ) ((Css22Listener)listener).exitTerm(this);
		}
	}

	public final TermContext term() throws RecognitionException {
		TermContext _localctx = new TermContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_term);
		int _la;
		try {
			setState(540);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case EMS:
			case EXS:
			case LENGTH:
			case ANGLE:
			case TIME:
			case FREQ:
			case PERCENTAGE:
			case NUMBER:
				enterOuterAlt(_localctx, 1);
				{
				setState(515);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case NUMBER:
					{
					setState(459);
					match(NUMBER);
					setState(463);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==WSP) {
						{
						{
						setState(460);
						match(WSP);
						}
						}
						setState(465);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
					break;
				case PERCENTAGE:
					{
					setState(466);
					match(PERCENTAGE);
					setState(470);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==WSP) {
						{
						{
						setState(467);
						match(WSP);
						}
						}
						setState(472);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
					break;
				case LENGTH:
					{
					setState(473);
					match(LENGTH);
					setState(477);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==WSP) {
						{
						{
						setState(474);
						match(WSP);
						}
						}
						setState(479);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
					break;
				case EMS:
					{
					setState(480);
					match(EMS);
					setState(484);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==WSP) {
						{
						{
						setState(481);
						match(WSP);
						}
						}
						setState(486);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
					break;
				case EXS:
					{
					setState(487);
					match(EXS);
					setState(491);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==WSP) {
						{
						{
						setState(488);
						match(WSP);
						}
						}
						setState(493);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
					break;
				case ANGLE:
					{
					setState(494);
					match(ANGLE);
					setState(498);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==WSP) {
						{
						{
						setState(495);
						match(WSP);
						}
						}
						setState(500);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
					break;
				case TIME:
					{
					setState(501);
					match(TIME);
					setState(505);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==WSP) {
						{
						{
						setState(502);
						match(WSP);
						}
						}
						setState(507);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
					break;
				case FREQ:
					{
					setState(508);
					match(FREQ);
					setState(512);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==WSP) {
						{
						{
						setState(509);
						match(WSP);
						}
						}
						setState(514);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				break;
			case STRING:
				enterOuterAlt(_localctx, 2);
				{
				setState(517);
				match(STRING);
				setState(521);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==WSP) {
					{
					{
					setState(518);
					match(WSP);
					}
					}
					setState(523);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			case IDENT:
				enterOuterAlt(_localctx, 3);
				{
				setState(524);
				match(IDENT);
				setState(528);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==WSP) {
					{
					{
					setState(525);
					match(WSP);
					}
					}
					setState(530);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			case URI:
				enterOuterAlt(_localctx, 4);
				{
				setState(531);
				match(URI);
				setState(535);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==WSP) {
					{
					{
					setState(532);
					match(WSP);
					}
					}
					setState(537);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			case HASH:
				enterOuterAlt(_localctx, 5);
				{
				setState(538);
				hexcolor();
				}
				break;
			case FUNCTION:
				enterOuterAlt(_localctx, 6);
				{
				setState(539);
				function();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FunctionContext extends ParserRuleContext {
		public TerminalNode FUNCTION() { return getToken(Css22Parser.FUNCTION, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(Css22Parser.RPAREN, 0); }
		public List<TerminalNode> WSP() { return getTokens(Css22Parser.WSP); }
		public TerminalNode WSP(int i) {
			return getToken(Css22Parser.WSP, i);
		}
		public FunctionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_function; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof Css22Listener ) ((Css22Listener)listener).enterFunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof Css22Listener ) ((Css22Listener)listener).exitFunction(this);
		}
	}

	public final FunctionContext function() throws RecognitionException {
		FunctionContext _localctx = new FunctionContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_function);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(542);
			match(FUNCTION);
			setState(546);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==WSP) {
				{
				{
				setState(543);
				match(WSP);
				}
				}
				setState(548);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(549);
			expr();
			setState(550);
			match(RPAREN);
			setState(554);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==WSP) {
				{
				{
				setState(551);
				match(WSP);
				}
				}
				setState(556);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class HexcolorContext extends ParserRuleContext {
		public TerminalNode HASH() { return getToken(Css22Parser.HASH, 0); }
		public List<TerminalNode> WSP() { return getTokens(Css22Parser.WSP); }
		public TerminalNode WSP(int i) {
			return getToken(Css22Parser.WSP, i);
		}
		public HexcolorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_hexcolor; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof Css22Listener ) ((Css22Listener)listener).enterHexcolor(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof Css22Listener ) ((Css22Listener)listener).exitHexcolor(this);
		}
	}

	public final HexcolorContext hexcolor() throws RecognitionException {
		HexcolorContext _localctx = new HexcolorContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_hexcolor);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(557);
			match(HASH);
			setState(561);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==WSP) {
				{
				{
				setState(558);
				match(WSP);
				}
				}
				setState(563);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3-\u0237\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\3\2\3\2\3\2\5\28\n\2\3\2\7\2;\n\2\f\2\16\2>\13\2\3\2\3\2\3"+
		"\2\7\2C\n\2\f\2\16\2F\13\2\3\2\3\2\7\2J\n\2\f\2\16\2M\13\2\7\2O\n\2\f"+
		"\2\16\2R\13\2\7\2T\n\2\f\2\16\2W\13\2\3\2\3\2\3\2\5\2\\\n\2\3\2\3\2\7"+
		"\2`\n\2\f\2\16\2c\13\2\3\2\3\2\7\2g\n\2\f\2\16\2j\13\2\7\2l\n\2\f\2\16"+
		"\2o\13\2\7\2q\n\2\f\2\16\2t\13\2\3\2\3\2\3\3\3\3\7\3z\n\3\f\3\16\3}\13"+
		"\3\3\3\3\3\7\3\u0081\n\3\f\3\16\3\u0084\13\3\3\3\5\3\u0087\n\3\3\3\3\3"+
		"\7\3\u008b\n\3\f\3\16\3\u008e\13\3\3\4\3\4\7\4\u0092\n\4\f\4\16\4\u0095"+
		"\13\4\3\4\3\4\3\4\7\4\u009a\n\4\f\4\16\4\u009d\13\4\3\4\7\4\u00a0\n\4"+
		"\f\4\16\4\u00a3\13\4\3\4\3\4\7\4\u00a7\n\4\f\4\16\4\u00aa\13\4\3\5\3\5"+
		"\3\5\7\5\u00af\n\5\f\5\16\5\u00b2\13\5\3\5\7\5\u00b5\n\5\f\5\16\5\u00b8"+
		"\13\5\3\6\3\6\7\6\u00bc\n\6\f\6\16\6\u00bf\13\6\3\7\3\7\7\7\u00c3\n\7"+
		"\f\7\16\7\u00c6\13\7\3\7\5\7\u00c9\n\7\3\7\3\7\7\7\u00cd\n\7\f\7\16\7"+
		"\u00d0\13\7\3\7\5\7\u00d3\n\7\3\7\3\7\7\7\u00d7\n\7\f\7\16\7\u00da\13"+
		"\7\3\7\5\7\u00dd\n\7\7\7\u00df\n\7\f\7\16\7\u00e2\13\7\3\7\3\7\7\7\u00e6"+
		"\n\7\f\7\16\7\u00e9\13\7\3\b\3\b\3\b\7\b\u00ee\n\b\f\b\16\b\u00f1\13\b"+
		"\3\t\3\t\7\t\u00f5\n\t\f\t\16\t\u00f8\13\t\3\t\3\t\7\t\u00fc\n\t\f\t\16"+
		"\t\u00ff\13\t\5\t\u0101\n\t\3\n\3\n\7\n\u0105\n\n\f\n\16\n\u0108\13\n"+
		"\3\n\3\n\7\n\u010c\n\n\f\n\16\n\u010f\13\n\5\n\u0111\n\n\3\13\3\13\7\13"+
		"\u0115\n\13\f\13\16\13\u0118\13\13\3\f\3\f\3\f\7\f\u011d\n\f\f\f\16\f"+
		"\u0120\13\f\3\f\7\f\u0123\n\f\f\f\16\f\u0126\13\f\3\f\3\f\7\f\u012a\n"+
		"\f\f\f\16\f\u012d\13\f\3\f\3\f\3\f\7\f\u0132\n\f\f\f\16\f\u0135\13\f\3"+
		"\r\5\r\u0138\n\r\3\r\3\r\7\r\u013c\n\r\f\r\16\r\u013f\13\r\3\r\5\r\u0142"+
		"\n\r\7\r\u0144\n\r\f\r\16\r\u0147\13\r\3\16\3\16\3\16\3\16\3\16\6\16\u014e"+
		"\n\16\r\16\16\16\u014f\3\16\5\16\u0153\n\16\3\16\7\16\u0156\n\16\f\16"+
		"\16\16\u0159\13\16\3\16\7\16\u015c\n\16\f\16\16\16\u015f\13\16\3\17\3"+
		"\17\3\17\3\17\3\17\7\17\u0166\n\17\f\17\16\17\u0169\13\17\3\17\3\17\3"+
		"\17\3\17\6\17\u016f\n\17\r\17\16\17\u0170\5\17\u0173\n\17\3\20\3\20\3"+
		"\21\3\21\3\21\3\22\3\22\3\23\3\23\7\23\u017e\n\23\f\23\16\23\u0181\13"+
		"\23\3\23\3\23\7\23\u0185\n\23\f\23\16\23\u0188\13\23\3\23\3\23\7\23\u018c"+
		"\n\23\f\23\16\23\u018f\13\23\3\23\3\23\7\23\u0193\n\23\f\23\16\23\u0196"+
		"\13\23\5\23\u0198\n\23\3\23\3\23\3\24\3\24\3\24\3\24\7\24\u01a0\n\24\f"+
		"\24\16\24\u01a3\13\24\3\24\3\24\7\24\u01a7\n\24\f\24\16\24\u01aa\13\24"+
		"\5\24\u01ac\n\24\3\24\5\24\u01af\n\24\3\25\3\25\3\25\7\25\u01b4\n\25\f"+
		"\25\16\25\u01b7\13\25\3\25\3\25\5\25\u01bb\n\25\3\26\3\26\7\26\u01bf\n"+
		"\26\f\26\16\26\u01c2\13\26\3\27\3\27\5\27\u01c6\n\27\3\27\7\27\u01c9\n"+
		"\27\f\27\16\27\u01cc\13\27\3\30\3\30\7\30\u01d0\n\30\f\30\16\30\u01d3"+
		"\13\30\3\30\3\30\7\30\u01d7\n\30\f\30\16\30\u01da\13\30\3\30\3\30\7\30"+
		"\u01de\n\30\f\30\16\30\u01e1\13\30\3\30\3\30\7\30\u01e5\n\30\f\30\16\30"+
		"\u01e8\13\30\3\30\3\30\7\30\u01ec\n\30\f\30\16\30\u01ef\13\30\3\30\3\30"+
		"\7\30\u01f3\n\30\f\30\16\30\u01f6\13\30\3\30\3\30\7\30\u01fa\n\30\f\30"+
		"\16\30\u01fd\13\30\3\30\3\30\7\30\u0201\n\30\f\30\16\30\u0204\13\30\5"+
		"\30\u0206\n\30\3\30\3\30\7\30\u020a\n\30\f\30\16\30\u020d\13\30\3\30\3"+
		"\30\7\30\u0211\n\30\f\30\16\30\u0214\13\30\3\30\3\30\7\30\u0218\n\30\f"+
		"\30\16\30\u021b\13\30\3\30\3\30\5\30\u021f\n\30\3\31\3\31\7\31\u0223\n"+
		"\31\f\31\16\31\u0226\13\31\3\31\3\31\3\31\7\31\u022b\n\31\f\31\16\31\u022e"+
		"\13\31\3\32\3\32\7\32\u0232\n\32\f\32\16\32\u0235\13\32\3\32\2\2\33\2"+
		"\4\6\b\n\f\16\20\22\24\26\30\32\34\36 \"$&(*,.\60\62\2\7\4\2\3\3\6\7\4"+
		"\2\n\n\34\34\4\2\f\f\37\37\4\2\b\t  \4\2\n\n\f\f\2\u0284\2\67\3\2\2\2"+
		"\4w\3\2\2\2\6\u008f\3\2\2\2\b\u00ab\3\2\2\2\n\u00b9\3\2\2\2\f\u00c0\3"+
		"\2\2\2\16\u00ea\3\2\2\2\20\u0100\3\2\2\2\22\u0110\3\2\2\2\24\u0112\3\2"+
		"\2\2\26\u0119\3\2\2\2\30\u0137\3\2\2\2\32\u0148\3\2\2\2\34\u0172\3\2\2"+
		"\2\36\u0174\3\2\2\2 \u0176\3\2\2\2\"\u0179\3\2\2\2$\u017b\3\2\2\2&\u019b"+
		"\3\2\2\2(\u01b0\3\2\2\2*\u01bc\3\2\2\2,\u01c3\3\2\2\2.\u021e\3\2\2\2\60"+
		"\u0220\3\2\2\2\62\u022f\3\2\2\2\64\65\7\21\2\2\65\66\7\n\2\2\668\7\'\2"+
		"\2\67\64\3\2\2\2\678\3\2\2\28<\3\2\2\29;\t\2\2\2:9\3\2\2\2;>\3\2\2\2<"+
		":\3\2\2\2<=\3\2\2\2=U\3\2\2\2><\3\2\2\2?P\5\4\3\2@D\7\6\2\2AC\7\3\2\2"+
		"BA\3\2\2\2CF\3\2\2\2DB\3\2\2\2DE\3\2\2\2EO\3\2\2\2FD\3\2\2\2GK\7\7\2\2"+
		"HJ\7\3\2\2IH\3\2\2\2JM\3\2\2\2KI\3\2\2\2KL\3\2\2\2LO\3\2\2\2MK\3\2\2\2"+
		"N@\3\2\2\2NG\3\2\2\2OR\3\2\2\2PN\3\2\2\2PQ\3\2\2\2QT\3\2\2\2RP\3\2\2\2"+
		"S?\3\2\2\2TW\3\2\2\2US\3\2\2\2UV\3\2\2\2Vr\3\2\2\2WU\3\2\2\2X\\\5\26\f"+
		"\2Y\\\5\6\4\2Z\\\5\f\7\2[X\3\2\2\2[Y\3\2\2\2[Z\3\2\2\2\\m\3\2\2\2]a\7"+
		"\6\2\2^`\7\3\2\2_^\3\2\2\2`c\3\2\2\2a_\3\2\2\2ab\3\2\2\2bl\3\2\2\2ca\3"+
		"\2\2\2dh\7\7\2\2eg\7\3\2\2fe\3\2\2\2gj\3\2\2\2hf\3\2\2\2hi\3\2\2\2il\3"+
		"\2\2\2jh\3\2\2\2k]\3\2\2\2kd\3\2\2\2lo\3\2\2\2mk\3\2\2\2mn\3\2\2\2nq\3"+
		"\2\2\2om\3\2\2\2p[\3\2\2\2qt\3\2\2\2rp\3\2\2\2rs\3\2\2\2su\3\2\2\2tr\3"+
		"\2\2\2uv\7\2\2\3v\3\3\2\2\2w{\7\16\2\2xz\7\3\2\2yx\3\2\2\2z}\3\2\2\2{"+
		"y\3\2\2\2{|\3\2\2\2|~\3\2\2\2}{\3\2\2\2~\u0082\t\3\2\2\177\u0081\7\3\2"+
		"\2\u0080\177\3\2\2\2\u0081\u0084\3\2\2\2\u0082\u0080\3\2\2\2\u0082\u0083"+
		"\3\2\2\2\u0083\u0086\3\2\2\2\u0084\u0082\3\2\2\2\u0085\u0087\5\b\5\2\u0086"+
		"\u0085\3\2\2\2\u0086\u0087\3\2\2\2\u0087\u0088\3\2\2\2\u0088\u008c\7\'"+
		"\2\2\u0089\u008b\7\3\2\2\u008a\u0089\3\2\2\2\u008b\u008e\3\2\2\2\u008c"+
		"\u008a\3\2\2\2\u008c\u008d\3\2\2\2\u008d\5\3\2\2\2\u008e\u008c\3\2\2\2"+
		"\u008f\u0093\7\20\2\2\u0090\u0092\7\3\2\2\u0091\u0090\3\2\2\2\u0092\u0095"+
		"\3\2\2\2\u0093\u0091\3\2\2\2\u0093\u0094\3\2\2\2\u0094\u0096\3\2\2\2\u0095"+
		"\u0093\3\2\2\2\u0096\u0097\5\b\5\2\u0097\u009b\7*\2\2\u0098\u009a\7\3"+
		"\2\2\u0099\u0098\3\2\2\2\u009a\u009d\3\2\2\2\u009b\u0099\3\2\2\2\u009b"+
		"\u009c\3\2\2\2\u009c\u00a1\3\2\2\2\u009d\u009b\3\2\2\2\u009e\u00a0\5\26"+
		"\f\2\u009f\u009e\3\2\2\2\u00a0\u00a3\3\2\2\2\u00a1\u009f\3\2\2\2\u00a1"+
		"\u00a2\3\2\2\2\u00a2\u00a4\3\2\2\2\u00a3\u00a1\3\2\2\2\u00a4\u00a8\7+"+
		"\2\2\u00a5\u00a7\7\3\2\2\u00a6\u00a5\3\2\2\2\u00a7\u00aa\3\2\2\2\u00a8"+
		"\u00a6\3\2\2\2\u00a8\u00a9\3\2\2\2\u00a9\7\3\2\2\2\u00aa\u00a8\3\2\2\2"+
		"\u00ab\u00b6\5\n\6\2\u00ac\u00b0\7%\2\2\u00ad\u00af\7\3\2\2\u00ae\u00ad"+
		"\3\2\2\2\u00af\u00b2\3\2\2\2\u00b0\u00ae\3\2\2\2\u00b0\u00b1\3\2\2\2\u00b1"+
		"\u00b3\3\2\2\2\u00b2\u00b0\3\2\2\2\u00b3\u00b5\5\n\6\2\u00b4\u00ac\3\2"+
		"\2\2\u00b5\u00b8\3\2\2\2\u00b6\u00b4\3\2\2\2\u00b6\u00b7\3\2\2\2\u00b7"+
		"\t\3\2\2\2\u00b8\u00b6\3\2\2\2\u00b9\u00bd\7\f\2\2\u00ba\u00bc\7\3\2\2"+
		"\u00bb\u00ba\3\2\2\2\u00bc\u00bf\3\2\2\2\u00bd\u00bb\3\2\2\2\u00bd\u00be"+
		"\3\2\2\2\u00be\13\3\2\2\2\u00bf\u00bd\3\2\2\2\u00c0\u00c4\7\17\2\2\u00c1"+
		"\u00c3\7\3\2\2\u00c2\u00c1\3\2\2\2\u00c3\u00c6\3\2\2\2\u00c4\u00c2\3\2"+
		"\2\2\u00c4\u00c5\3\2\2\2\u00c5\u00c8\3\2\2\2\u00c6\u00c4\3\2\2\2\u00c7"+
		"\u00c9\5\16\b\2\u00c8\u00c7\3\2\2\2\u00c8\u00c9\3\2\2\2\u00c9\u00ca\3"+
		"\2\2\2\u00ca\u00ce\7*\2\2\u00cb\u00cd\7\3\2\2\u00cc\u00cb\3\2\2\2\u00cd"+
		"\u00d0\3\2\2\2\u00ce\u00cc\3\2\2\2\u00ce\u00cf\3\2\2\2\u00cf\u00d2\3\2"+
		"\2\2\u00d0\u00ce\3\2\2\2\u00d1\u00d3\5(\25\2\u00d2\u00d1\3\2\2\2\u00d2"+
		"\u00d3\3\2\2\2\u00d3\u00e0\3\2\2\2\u00d4\u00d8\7\'\2\2\u00d5\u00d7\7\3"+
		"\2\2\u00d6\u00d5\3\2\2\2\u00d7\u00da\3\2\2\2\u00d8\u00d6\3\2\2\2\u00d8"+
		"\u00d9\3\2\2\2\u00d9\u00dc\3\2\2\2\u00da\u00d8\3\2\2\2\u00db\u00dd\5("+
		"\25\2\u00dc\u00db\3\2\2\2\u00dc\u00dd\3\2\2\2\u00dd\u00df\3\2\2\2\u00de"+
		"\u00d4\3\2\2\2\u00df\u00e2\3\2\2\2\u00e0\u00de\3\2\2\2\u00e0\u00e1\3\2"+
		"\2\2\u00e1\u00e3\3\2\2\2\u00e2\u00e0\3\2\2\2\u00e3\u00e7\7+\2\2\u00e4"+
		"\u00e6\7\3\2\2\u00e5\u00e4\3\2\2\2\u00e6\u00e9\3\2\2\2\u00e7\u00e5\3\2"+
		"\2\2\u00e7\u00e8\3\2\2\2\u00e8\r\3\2\2\2\u00e9\u00e7\3\2\2\2\u00ea\u00eb"+
		"\7&\2\2\u00eb\u00ef\7\f\2\2\u00ec\u00ee\7\3\2\2\u00ed\u00ec\3\2\2\2\u00ee"+
		"\u00f1\3\2\2\2\u00ef\u00ed\3\2\2\2\u00ef\u00f0\3\2\2\2\u00f0\17\3\2\2"+
		"\2\u00f1\u00ef\3\2\2\2\u00f2\u00f6\7$\2\2\u00f3\u00f5\7\3\2\2\u00f4\u00f3"+
		"\3\2\2\2\u00f5\u00f8\3\2\2\2\u00f6\u00f4\3\2\2\2\u00f6\u00f7\3\2\2\2\u00f7"+
		"\u0101\3\2\2\2\u00f8\u00f6\3\2\2\2\u00f9\u00fd\7%\2\2\u00fa\u00fc\7\3"+
		"\2\2\u00fb\u00fa\3\2\2\2\u00fc\u00ff\3\2\2\2\u00fd\u00fb\3\2\2\2\u00fd"+
		"\u00fe\3\2\2\2\u00fe\u0101\3\2\2\2\u00ff\u00fd\3\2\2\2\u0100\u00f2\3\2"+
		"\2\2\u0100\u00f9\3\2\2\2\u0101\21\3\2\2\2\u0102\u0106\7\"\2\2\u0103\u0105"+
		"\7\3\2\2\u0104\u0103\3\2\2\2\u0105\u0108\3\2\2\2\u0106\u0104\3\2\2\2\u0106"+
		"\u0107\3\2\2\2\u0107\u0111\3\2\2\2\u0108\u0106\3\2\2\2\u0109\u010d\7#"+
		"\2\2\u010a\u010c\7\3\2\2\u010b\u010a\3\2\2\2\u010c\u010f\3\2\2\2\u010d"+
		"\u010b\3\2\2\2\u010d\u010e\3\2\2\2\u010e\u0111\3\2\2\2\u010f\u010d\3\2"+
		"\2\2\u0110\u0102\3\2\2\2\u0110\u0109\3\2\2\2\u0111\23\3\2\2\2\u0112\u0116"+
		"\7\f\2\2\u0113\u0115\7\3\2\2\u0114\u0113\3\2\2\2\u0115\u0118\3\2\2\2\u0116"+
		"\u0114\3\2\2\2\u0116\u0117\3\2\2\2\u0117\25\3\2\2\2\u0118\u0116\3\2\2"+
		"\2\u0119\u0124\5\32\16\2\u011a\u011e\7%\2\2\u011b\u011d\7\3\2\2\u011c"+
		"\u011b\3\2\2\2\u011d\u0120\3\2\2\2\u011e\u011c\3\2\2\2\u011e\u011f\3\2"+
		"\2\2\u011f\u0121\3\2\2\2\u0120\u011e\3\2\2\2\u0121\u0123\5\32\16\2\u0122"+
		"\u011a\3\2\2\2\u0123\u0126\3\2\2\2\u0124\u0122\3\2\2\2\u0124\u0125\3\2"+
		"\2\2\u0125\u0127\3\2\2\2\u0126\u0124\3\2\2\2\u0127\u012b\7*\2\2\u0128"+
		"\u012a\7\3\2\2\u0129\u0128\3\2\2\2\u012a\u012d\3\2\2\2\u012b\u0129\3\2"+
		"\2\2\u012b\u012c\3\2\2\2\u012c\u012e\3\2\2\2\u012d\u012b\3\2\2\2\u012e"+
		"\u012f\5\30\r\2\u012f\u0133\7+\2\2\u0130\u0132\7\3\2\2\u0131\u0130\3\2"+
		"\2\2\u0132\u0135\3\2\2\2\u0133\u0131\3\2\2\2\u0133\u0134\3\2\2\2\u0134"+
		"\27\3\2\2\2\u0135\u0133\3\2\2\2\u0136\u0138\5(\25\2\u0137\u0136\3\2\2"+
		"\2\u0137\u0138\3\2\2\2\u0138\u0145\3\2\2\2\u0139\u013d\7\'\2\2\u013a\u013c"+
		"\7\3\2\2\u013b\u013a\3\2\2\2\u013c\u013f\3\2\2\2\u013d\u013b\3\2\2\2\u013d"+
		"\u013e\3\2\2\2\u013e\u0141\3\2\2\2\u013f\u013d\3\2\2\2\u0140\u0142\5("+
		"\25\2\u0141\u0140\3\2\2\2\u0141\u0142\3\2\2\2\u0142\u0144\3\2\2\2\u0143"+
		"\u0139\3\2\2\2\u0144\u0147\3\2\2\2\u0145\u0143\3\2\2\2\u0145\u0146\3\2"+
		"\2\2\u0146\31\3\2\2\2\u0147\u0145\3\2\2\2\u0148\u0157\5\34\17\2\u0149"+
		"\u014a\5\22\n\2\u014a\u014b\5\34\17\2\u014b\u0156\3\2\2\2\u014c\u014e"+
		"\7\3\2\2\u014d\u014c\3\2\2\2\u014e\u014f\3\2\2\2\u014f\u014d\3\2\2\2\u014f"+
		"\u0150\3\2\2\2\u0150\u0152\3\2\2\2\u0151\u0153\5\22\n\2\u0152\u0151\3"+
		"\2\2\2\u0152\u0153\3\2\2\2\u0153\u0154\3\2\2\2\u0154\u0156\5\34\17\2\u0155"+
		"\u0149\3\2\2\2\u0155\u014d\3\2\2\2\u0156\u0159\3\2\2\2\u0157\u0155\3\2"+
		"\2\2\u0157\u0158\3\2\2\2\u0158\u015d\3\2\2\2\u0159\u0157\3\2\2\2\u015a"+
		"\u015c\7\3\2\2\u015b\u015a\3\2\2\2\u015c\u015f\3\2\2\2\u015d\u015b\3\2"+
		"\2\2\u015d\u015e\3\2\2\2\u015e\33\3\2\2\2\u015f\u015d\3\2\2\2\u0160\u0167"+
		"\5\"\22\2\u0161\u0166\5\36\20\2\u0162\u0166\5 \21\2\u0163\u0166\5$\23"+
		"\2\u0164\u0166\5&\24\2\u0165\u0161\3\2\2\2\u0165\u0162\3\2\2\2\u0165\u0163"+
		"\3\2\2\2\u0165\u0164\3\2\2\2\u0166\u0169\3\2\2\2\u0167\u0165\3\2\2\2\u0167"+
		"\u0168\3\2\2\2\u0168\u0173\3\2\2\2\u0169\u0167\3\2\2\2\u016a\u016f\5\36"+
		"\20\2\u016b\u016f\5 \21\2\u016c\u016f\5$\23\2\u016d\u016f\5&\24\2\u016e"+
		"\u016a\3\2\2\2\u016e\u016b\3\2\2\2\u016e\u016c\3\2\2\2\u016e\u016d\3\2"+
		"\2\2\u016f\u0170\3\2\2\2\u0170\u016e\3\2\2\2\u0170\u0171\3\2\2\2\u0171"+
		"\u0173\3\2\2\2\u0172\u0160\3\2\2\2\u0172\u016e\3\2\2\2\u0173\35\3\2\2"+
		"\2\u0174\u0175\7\r\2\2\u0175\37\3\2\2\2\u0176\u0177\7!\2\2\u0177\u0178"+
		"\7\f\2\2\u0178!\3\2\2\2\u0179\u017a\t\4\2\2\u017a#\3\2\2\2\u017b\u017f"+
		"\7,\2\2\u017c\u017e\7\3\2\2\u017d\u017c\3\2\2\2\u017e\u0181\3\2\2\2\u017f"+
		"\u017d\3\2\2\2\u017f\u0180\3\2\2\2\u0180\u0182\3\2\2\2\u0181\u017f\3\2"+
		"\2\2\u0182\u0186\7\f\2\2\u0183\u0185\7\3\2\2\u0184\u0183\3\2\2\2\u0185"+
		"\u0188\3\2\2\2\u0186\u0184\3\2\2\2\u0186\u0187\3\2\2\2\u0187\u0197\3\2"+
		"\2\2\u0188\u0186\3\2\2\2\u0189\u018d\t\5\2\2\u018a\u018c\7\3\2\2\u018b"+
		"\u018a\3\2\2\2\u018c\u018f\3\2\2\2\u018d\u018b\3\2\2\2\u018d\u018e\3\2"+
		"\2\2\u018e\u0190\3\2\2\2\u018f\u018d\3\2\2\2\u0190\u0194\t\6\2\2\u0191"+
		"\u0193\7\3\2\2\u0192\u0191\3\2\2\2\u0193\u0196\3\2\2\2\u0194\u0192\3\2"+
		"\2\2\u0194\u0195\3\2\2\2\u0195\u0198\3\2\2\2\u0196\u0194\3\2\2\2\u0197"+
		"\u0189\3\2\2\2\u0197\u0198\3\2\2\2\u0198\u0199\3\2\2\2\u0199\u019a\7-"+
		"\2\2\u019a%\3\2\2\2\u019b\u01ae\7&\2\2\u019c\u01af\7\f\2\2\u019d\u01a1"+
		"\7\36\2\2\u019e\u01a0\7\3\2\2\u019f\u019e\3\2\2\2\u01a0\u01a3\3\2\2\2"+
		"\u01a1\u019f\3\2\2\2\u01a1\u01a2\3\2\2\2\u01a2\u01ab\3\2\2\2\u01a3\u01a1"+
		"\3\2\2\2\u01a4\u01a8\7\f\2\2\u01a5\u01a7\7\3\2\2\u01a6\u01a5\3\2\2\2\u01a7"+
		"\u01aa\3\2\2\2\u01a8\u01a6\3\2\2\2\u01a8\u01a9\3\2\2\2\u01a9\u01ac\3\2"+
		"\2\2\u01aa\u01a8\3\2\2\2\u01ab\u01a4\3\2\2\2\u01ab\u01ac\3\2\2\2\u01ac"+
		"\u01ad\3\2\2\2\u01ad\u01af\7)\2\2\u01ae\u019c\3\2\2\2\u01ae\u019d\3\2"+
		"\2\2\u01af\'\3\2\2\2\u01b0\u01b1\5\24\13\2\u01b1\u01b5\7&\2\2\u01b2\u01b4"+
		"\7\3\2\2\u01b3\u01b2\3\2\2\2\u01b4\u01b7\3\2\2\2\u01b5\u01b3\3\2\2\2\u01b5"+
		"\u01b6\3\2\2\2\u01b6\u01b8\3\2\2\2\u01b7\u01b5\3\2\2\2\u01b8\u01ba\5,"+
		"\27\2\u01b9\u01bb\5*\26\2\u01ba\u01b9\3\2\2\2\u01ba\u01bb\3\2\2\2\u01bb"+
		")\3\2\2\2\u01bc\u01c0\7\22\2\2\u01bd\u01bf\7\3\2\2\u01be\u01bd\3\2\2\2"+
		"\u01bf\u01c2\3\2\2\2\u01c0\u01be\3\2\2\2\u01c0\u01c1\3\2\2\2\u01c1+\3"+
		"\2\2\2\u01c2\u01c0\3\2\2\2\u01c3\u01ca\5.\30\2\u01c4\u01c6\5\20\t\2\u01c5"+
		"\u01c4\3\2\2\2\u01c5\u01c6\3\2\2\2\u01c6\u01c7\3\2\2\2\u01c7\u01c9\5."+
		"\30\2\u01c8\u01c5\3\2\2\2\u01c9\u01cc\3\2\2\2\u01ca\u01c8\3\2\2\2\u01ca"+
		"\u01cb\3\2\2\2\u01cb-\3\2\2\2\u01cc\u01ca\3\2\2\2\u01cd\u01d1\7\33\2\2"+
		"\u01ce\u01d0\7\3\2\2\u01cf\u01ce\3\2\2\2\u01d0\u01d3\3\2\2\2\u01d1\u01cf"+
		"\3\2\2\2\u01d1\u01d2\3\2\2\2\u01d2\u0206\3\2\2\2\u01d3\u01d1\3\2\2\2\u01d4"+
		"\u01d8\7\32\2\2\u01d5\u01d7\7\3\2\2\u01d6\u01d5\3\2\2\2\u01d7\u01da\3"+
		"\2\2\2\u01d8\u01d6\3\2\2\2\u01d8\u01d9\3\2\2\2\u01d9\u0206\3\2\2\2\u01da"+
		"\u01d8\3\2\2\2\u01db\u01df\7\25\2\2\u01dc\u01de\7\3\2\2\u01dd\u01dc\3"+
		"\2\2\2\u01de\u01e1\3\2\2\2\u01df\u01dd\3\2\2\2\u01df\u01e0\3\2\2\2\u01e0"+
		"\u0206\3\2\2\2\u01e1\u01df\3\2\2\2\u01e2\u01e6\7\23\2\2\u01e3\u01e5\7"+
		"\3\2\2\u01e4\u01e3\3\2\2\2\u01e5\u01e8\3\2\2\2\u01e6\u01e4\3\2\2\2\u01e6"+
		"\u01e7\3\2\2\2\u01e7\u0206\3\2\2\2\u01e8\u01e6\3\2\2\2\u01e9\u01ed\7\24"+
		"\2\2\u01ea\u01ec\7\3\2\2\u01eb\u01ea\3\2\2\2\u01ec\u01ef\3\2\2\2\u01ed"+
		"\u01eb\3\2\2\2\u01ed\u01ee\3\2\2\2\u01ee\u0206\3\2\2\2\u01ef\u01ed\3\2"+
		"\2\2\u01f0\u01f4\7\26\2\2\u01f1\u01f3\7\3\2\2\u01f2\u01f1\3\2\2\2\u01f3"+
		"\u01f6\3\2\2\2\u01f4\u01f2\3\2\2\2\u01f4\u01f5\3\2\2\2\u01f5\u0206\3\2"+
		"\2\2\u01f6\u01f4\3\2\2\2\u01f7\u01fb\7\27\2\2\u01f8\u01fa\7\3\2\2\u01f9"+
		"\u01f8\3\2\2\2\u01fa\u01fd\3\2\2\2\u01fb\u01f9\3\2\2\2\u01fb\u01fc\3\2"+
		"\2\2\u01fc\u0206\3\2\2\2\u01fd\u01fb\3\2\2\2\u01fe\u0202\7\30\2\2\u01ff"+
		"\u0201\7\3\2\2\u0200\u01ff\3\2\2\2\u0201\u0204\3\2\2\2\u0202\u0200\3\2"+
		"\2\2\u0202\u0203\3\2\2\2\u0203\u0206\3\2\2\2\u0204\u0202\3\2\2\2\u0205"+
		"\u01cd\3\2\2\2\u0205\u01d4\3\2\2\2\u0205\u01db\3\2\2\2\u0205\u01e2\3\2"+
		"\2\2\u0205\u01e9\3\2\2\2\u0205\u01f0\3\2\2\2\u0205\u01f7\3\2\2\2\u0205"+
		"\u01fe\3\2\2\2\u0206\u021f\3\2\2\2\u0207\u020b\7\n\2\2\u0208\u020a\7\3"+
		"\2\2\u0209\u0208\3\2\2\2\u020a\u020d\3\2\2\2\u020b\u0209\3\2\2\2\u020b"+
		"\u020c\3\2\2\2\u020c\u021f\3\2\2\2\u020d\u020b\3\2\2\2\u020e\u0212\7\f"+
		"\2\2\u020f\u0211\7\3\2\2\u0210\u020f\3\2\2\2\u0211\u0214\3\2\2\2\u0212"+
		"\u0210\3\2\2\2\u0212\u0213\3\2\2\2\u0213\u021f\3\2\2\2\u0214\u0212\3\2"+
		"\2\2\u0215\u0219\7\34\2\2\u0216\u0218\7\3\2\2\u0217\u0216\3\2\2\2\u0218"+
		"\u021b\3\2\2\2\u0219\u0217\3\2\2\2\u0219\u021a\3\2\2\2\u021a\u021f\3\2"+
		"\2\2\u021b\u0219\3\2\2\2\u021c\u021f\5\62\32\2\u021d\u021f\5\60\31\2\u021e"+
		"\u0205\3\2\2\2\u021e\u0207\3\2\2\2\u021e\u020e\3\2\2\2\u021e\u0215\3\2"+
		"\2\2\u021e\u021c\3\2\2\2\u021e\u021d\3\2\2\2\u021f/\3\2\2\2\u0220\u0224"+
		"\7\36\2\2\u0221\u0223\7\3\2\2\u0222\u0221\3\2\2\2\u0223\u0226\3\2\2\2"+
		"\u0224\u0222\3\2\2\2\u0224\u0225\3\2\2\2\u0225\u0227\3\2\2\2\u0226\u0224"+
		"\3\2\2\2\u0227\u0228\5,\27\2\u0228\u022c\7)\2\2\u0229\u022b\7\3\2\2\u022a"+
		"\u0229\3\2\2\2\u022b\u022e\3\2\2\2\u022c\u022a\3\2\2\2\u022c\u022d\3\2"+
		"\2\2\u022d\61\3\2\2\2\u022e\u022c\3\2\2\2\u022f\u0233\7\r\2\2\u0230\u0232"+
		"\7\3\2\2\u0231\u0230\3\2\2\2\u0232\u0235\3\2\2\2\u0233\u0231\3\2\2\2\u0233"+
		"\u0234\3\2\2\2\u0234\63\3\2\2\2\u0235\u0233\3\2\2\2Z\67<DKNPU[ahkmr{\u0082"+
		"\u0086\u008c\u0093\u009b\u00a1\u00a8\u00b0\u00b6\u00bd\u00c4\u00c8\u00ce"+
		"\u00d2\u00d8\u00dc\u00e0\u00e7\u00ef\u00f6\u00fd\u0100\u0106\u010d\u0110"+
		"\u0116\u011e\u0124\u012b\u0133\u0137\u013d\u0141\u0145\u014f\u0152\u0155"+
		"\u0157\u015d\u0165\u0167\u016e\u0170\u0172\u017f\u0186\u018d\u0194\u0197"+
		"\u01a1\u01a8\u01ab\u01ae\u01b5\u01ba\u01c0\u01c5\u01ca\u01d1\u01d8\u01df"+
		"\u01e6\u01ed\u01f4\u01fb\u0202\u0205\u020b\u0212\u0219\u021e\u0224\u022c"+
		"\u0233";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}