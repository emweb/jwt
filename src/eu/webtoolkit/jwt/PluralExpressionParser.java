// Generated from PluralExpression.g4 by ANTLR 4.7.2
package eu.webtoolkit.jwt;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class PluralExpressionParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.7.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		OR=1, AND=2, EQ=3, NEQ=4, GT=5, GTE=6, LT=7, LTE=8, PLUS=9, MINUS=10, 
		MULT=11, DIV=12, MOD=13, N=14, LPAREN=15, RPAREN=16, COLON=17, SEMICOLON=18, 
		QMARK=19, INTEGER=20, WS=21;
	public static final int
		RULE_statement = 0, RULE_expression = 1, RULE_orExpression = 2, RULE_andExpression = 3, 
		RULE_eqExpression = 4, RULE_relationalExpression = 5, RULE_additiveExpression = 6, 
		RULE_term = 7, RULE_factor = 8, RULE_literal = 9, RULE_group = 10, RULE_variable = 11, 
		RULE_eqOperator = 12, RULE_cmpOperator = 13, RULE_sumOperator = 14, RULE_prodOperator = 15;
	private static String[] makeRuleNames() {
		return new String[] {
			"statement", "expression", "orExpression", "andExpression", "eqExpression", 
			"relationalExpression", "additiveExpression", "term", "factor", "literal", 
			"group", "variable", "eqOperator", "cmpOperator", "sumOperator", "prodOperator"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'||'", "'&&'", "'=='", "'!='", "'>'", "'>='", "'<'", "'<='", "'+'", 
			"'-'", "'*'", "'/'", "'%'", "'n'", "'('", "')'", "':'", "';'", "'?'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "OR", "AND", "EQ", "NEQ", "GT", "GTE", "LT", "LTE", "PLUS", "MINUS", 
			"MULT", "DIV", "MOD", "N", "LPAREN", "RPAREN", "COLON", "SEMICOLON", 
			"QMARK", "INTEGER", "WS"
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
	public String getGrammarFileName() { return "PluralExpression.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public PluralExpressionParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class StatementContext extends ParserRuleContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode EOF() { return getToken(PluralExpressionParser.EOF, 0); }
		public TerminalNode SEMICOLON() { return getToken(PluralExpressionParser.SEMICOLON, 0); }
		public StatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_statement; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PluralExpressionVisitor ) return ((PluralExpressionVisitor<? extends T>)visitor).visitStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StatementContext statement() throws RecognitionException {
		StatementContext _localctx = new StatementContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_statement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(32);
			expression();
			setState(34);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SEMICOLON) {
				{
				setState(33);
				match(SEMICOLON);
				}
			}

			setState(36);
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

	public static class ExpressionContext extends ParserRuleContext {
		public OrExpressionContext orExpression() {
			return getRuleContext(OrExpressionContext.class,0);
		}
		public TerminalNode QMARK() { return getToken(PluralExpressionParser.QMARK, 0); }
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode COLON() { return getToken(PluralExpressionParser.COLON, 0); }
		public ExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expression; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PluralExpressionVisitor ) return ((PluralExpressionVisitor<? extends T>)visitor).visitExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExpressionContext expression() throws RecognitionException {
		ExpressionContext _localctx = new ExpressionContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_expression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(38);
			orExpression();
			setState(44);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==QMARK) {
				{
				setState(39);
				match(QMARK);
				setState(40);
				expression();
				setState(41);
				match(COLON);
				setState(42);
				expression();
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

	public static class OrExpressionContext extends ParserRuleContext {
		public List<AndExpressionContext> andExpression() {
			return getRuleContexts(AndExpressionContext.class);
		}
		public AndExpressionContext andExpression(int i) {
			return getRuleContext(AndExpressionContext.class,i);
		}
		public List<TerminalNode> OR() { return getTokens(PluralExpressionParser.OR); }
		public TerminalNode OR(int i) {
			return getToken(PluralExpressionParser.OR, i);
		}
		public OrExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_orExpression; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PluralExpressionVisitor ) return ((PluralExpressionVisitor<? extends T>)visitor).visitOrExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OrExpressionContext orExpression() throws RecognitionException {
		OrExpressionContext _localctx = new OrExpressionContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_orExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(46);
			andExpression();
			setState(51);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==OR) {
				{
				{
				setState(47);
				match(OR);
				setState(48);
				andExpression();
				}
				}
				setState(53);
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

	public static class AndExpressionContext extends ParserRuleContext {
		public List<EqExpressionContext> eqExpression() {
			return getRuleContexts(EqExpressionContext.class);
		}
		public EqExpressionContext eqExpression(int i) {
			return getRuleContext(EqExpressionContext.class,i);
		}
		public List<TerminalNode> AND() { return getTokens(PluralExpressionParser.AND); }
		public TerminalNode AND(int i) {
			return getToken(PluralExpressionParser.AND, i);
		}
		public AndExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_andExpression; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PluralExpressionVisitor ) return ((PluralExpressionVisitor<? extends T>)visitor).visitAndExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AndExpressionContext andExpression() throws RecognitionException {
		AndExpressionContext _localctx = new AndExpressionContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_andExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(54);
			eqExpression();
			setState(59);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==AND) {
				{
				{
				setState(55);
				match(AND);
				setState(56);
				eqExpression();
				}
				}
				setState(61);
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

	public static class EqExpressionContext extends ParserRuleContext {
		public List<RelationalExpressionContext> relationalExpression() {
			return getRuleContexts(RelationalExpressionContext.class);
		}
		public RelationalExpressionContext relationalExpression(int i) {
			return getRuleContext(RelationalExpressionContext.class,i);
		}
		public List<EqOperatorContext> eqOperator() {
			return getRuleContexts(EqOperatorContext.class);
		}
		public EqOperatorContext eqOperator(int i) {
			return getRuleContext(EqOperatorContext.class,i);
		}
		public EqExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_eqExpression; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PluralExpressionVisitor ) return ((PluralExpressionVisitor<? extends T>)visitor).visitEqExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EqExpressionContext eqExpression() throws RecognitionException {
		EqExpressionContext _localctx = new EqExpressionContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_eqExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(62);
			relationalExpression();
			setState(68);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==EQ || _la==NEQ) {
				{
				{
				setState(63);
				eqOperator();
				setState(64);
				relationalExpression();
				}
				}
				setState(70);
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

	public static class RelationalExpressionContext extends ParserRuleContext {
		public List<AdditiveExpressionContext> additiveExpression() {
			return getRuleContexts(AdditiveExpressionContext.class);
		}
		public AdditiveExpressionContext additiveExpression(int i) {
			return getRuleContext(AdditiveExpressionContext.class,i);
		}
		public List<CmpOperatorContext> cmpOperator() {
			return getRuleContexts(CmpOperatorContext.class);
		}
		public CmpOperatorContext cmpOperator(int i) {
			return getRuleContext(CmpOperatorContext.class,i);
		}
		public RelationalExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_relationalExpression; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PluralExpressionVisitor ) return ((PluralExpressionVisitor<? extends T>)visitor).visitRelationalExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RelationalExpressionContext relationalExpression() throws RecognitionException {
		RelationalExpressionContext _localctx = new RelationalExpressionContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_relationalExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(71);
			additiveExpression();
			setState(77);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << GT) | (1L << GTE) | (1L << LT) | (1L << LTE))) != 0)) {
				{
				{
				setState(72);
				cmpOperator();
				setState(73);
				additiveExpression();
				}
				}
				setState(79);
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

	public static class AdditiveExpressionContext extends ParserRuleContext {
		public List<TermContext> term() {
			return getRuleContexts(TermContext.class);
		}
		public TermContext term(int i) {
			return getRuleContext(TermContext.class,i);
		}
		public List<SumOperatorContext> sumOperator() {
			return getRuleContexts(SumOperatorContext.class);
		}
		public SumOperatorContext sumOperator(int i) {
			return getRuleContext(SumOperatorContext.class,i);
		}
		public AdditiveExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_additiveExpression; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PluralExpressionVisitor ) return ((PluralExpressionVisitor<? extends T>)visitor).visitAdditiveExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AdditiveExpressionContext additiveExpression() throws RecognitionException {
		AdditiveExpressionContext _localctx = new AdditiveExpressionContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_additiveExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(80);
			term();
			setState(86);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==PLUS || _la==MINUS) {
				{
				{
				setState(81);
				sumOperator();
				setState(82);
				term();
				}
				}
				setState(88);
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
		public List<FactorContext> factor() {
			return getRuleContexts(FactorContext.class);
		}
		public FactorContext factor(int i) {
			return getRuleContext(FactorContext.class,i);
		}
		public List<ProdOperatorContext> prodOperator() {
			return getRuleContexts(ProdOperatorContext.class);
		}
		public ProdOperatorContext prodOperator(int i) {
			return getRuleContext(ProdOperatorContext.class,i);
		}
		public TermContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_term; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PluralExpressionVisitor ) return ((PluralExpressionVisitor<? extends T>)visitor).visitTerm(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TermContext term() throws RecognitionException {
		TermContext _localctx = new TermContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_term);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(89);
			factor();
			setState(95);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << MULT) | (1L << DIV) | (1L << MOD))) != 0)) {
				{
				{
				setState(90);
				prodOperator();
				setState(91);
				factor();
				}
				}
				setState(97);
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

	public static class FactorContext extends ParserRuleContext {
		public LiteralContext literal() {
			return getRuleContext(LiteralContext.class,0);
		}
		public GroupContext group() {
			return getRuleContext(GroupContext.class,0);
		}
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public FactorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_factor; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PluralExpressionVisitor ) return ((PluralExpressionVisitor<? extends T>)visitor).visitFactor(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FactorContext factor() throws RecognitionException {
		FactorContext _localctx = new FactorContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_factor);
		try {
			setState(101);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case INTEGER:
				enterOuterAlt(_localctx, 1);
				{
				setState(98);
				literal();
				}
				break;
			case LPAREN:
				enterOuterAlt(_localctx, 2);
				{
				setState(99);
				group();
				}
				break;
			case N:
				enterOuterAlt(_localctx, 3);
				{
				setState(100);
				variable();
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

	public static class LiteralContext extends ParserRuleContext {
		public TerminalNode INTEGER() { return getToken(PluralExpressionParser.INTEGER, 0); }
		public LiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_literal; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PluralExpressionVisitor ) return ((PluralExpressionVisitor<? extends T>)visitor).visitLiteral(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LiteralContext literal() throws RecognitionException {
		LiteralContext _localctx = new LiteralContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_literal);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(103);
			match(INTEGER);
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

	public static class GroupContext extends ParserRuleContext {
		public TerminalNode LPAREN() { return getToken(PluralExpressionParser.LPAREN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(PluralExpressionParser.RPAREN, 0); }
		public GroupContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_group; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PluralExpressionVisitor ) return ((PluralExpressionVisitor<? extends T>)visitor).visitGroup(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GroupContext group() throws RecognitionException {
		GroupContext _localctx = new GroupContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_group);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(105);
			match(LPAREN);
			setState(106);
			expression();
			setState(107);
			match(RPAREN);
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

	public static class VariableContext extends ParserRuleContext {
		public TerminalNode N() { return getToken(PluralExpressionParser.N, 0); }
		public VariableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_variable; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PluralExpressionVisitor ) return ((PluralExpressionVisitor<? extends T>)visitor).visitVariable(this);
			else return visitor.visitChildren(this);
		}
	}

	public final VariableContext variable() throws RecognitionException {
		VariableContext _localctx = new VariableContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_variable);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(109);
			match(N);
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

	public static class EqOperatorContext extends ParserRuleContext {
		public TerminalNode EQ() { return getToken(PluralExpressionParser.EQ, 0); }
		public TerminalNode NEQ() { return getToken(PluralExpressionParser.NEQ, 0); }
		public EqOperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_eqOperator; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PluralExpressionVisitor ) return ((PluralExpressionVisitor<? extends T>)visitor).visitEqOperator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EqOperatorContext eqOperator() throws RecognitionException {
		EqOperatorContext _localctx = new EqOperatorContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_eqOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(111);
			_la = _input.LA(1);
			if ( !(_la==EQ || _la==NEQ) ) {
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

	public static class CmpOperatorContext extends ParserRuleContext {
		public TerminalNode GT() { return getToken(PluralExpressionParser.GT, 0); }
		public TerminalNode GTE() { return getToken(PluralExpressionParser.GTE, 0); }
		public TerminalNode LT() { return getToken(PluralExpressionParser.LT, 0); }
		public TerminalNode LTE() { return getToken(PluralExpressionParser.LTE, 0); }
		public CmpOperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_cmpOperator; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PluralExpressionVisitor ) return ((PluralExpressionVisitor<? extends T>)visitor).visitCmpOperator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CmpOperatorContext cmpOperator() throws RecognitionException {
		CmpOperatorContext _localctx = new CmpOperatorContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_cmpOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(113);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << GT) | (1L << GTE) | (1L << LT) | (1L << LTE))) != 0)) ) {
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

	public static class SumOperatorContext extends ParserRuleContext {
		public TerminalNode PLUS() { return getToken(PluralExpressionParser.PLUS, 0); }
		public TerminalNode MINUS() { return getToken(PluralExpressionParser.MINUS, 0); }
		public SumOperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sumOperator; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PluralExpressionVisitor ) return ((PluralExpressionVisitor<? extends T>)visitor).visitSumOperator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SumOperatorContext sumOperator() throws RecognitionException {
		SumOperatorContext _localctx = new SumOperatorContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_sumOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(115);
			_la = _input.LA(1);
			if ( !(_la==PLUS || _la==MINUS) ) {
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

	public static class ProdOperatorContext extends ParserRuleContext {
		public TerminalNode MULT() { return getToken(PluralExpressionParser.MULT, 0); }
		public TerminalNode DIV() { return getToken(PluralExpressionParser.DIV, 0); }
		public TerminalNode MOD() { return getToken(PluralExpressionParser.MOD, 0); }
		public ProdOperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_prodOperator; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PluralExpressionVisitor ) return ((PluralExpressionVisitor<? extends T>)visitor).visitProdOperator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ProdOperatorContext prodOperator() throws RecognitionException {
		ProdOperatorContext _localctx = new ProdOperatorContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_prodOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(117);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << MULT) | (1L << DIV) | (1L << MOD))) != 0)) ) {
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

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\27z\4\2\t\2\4\3\t"+
		"\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t\13\4"+
		"\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\3\2\3\2\5\2%\n"+
		"\2\3\2\3\2\3\3\3\3\3\3\3\3\3\3\3\3\5\3/\n\3\3\4\3\4\3\4\7\4\64\n\4\f\4"+
		"\16\4\67\13\4\3\5\3\5\3\5\7\5<\n\5\f\5\16\5?\13\5\3\6\3\6\3\6\3\6\7\6"+
		"E\n\6\f\6\16\6H\13\6\3\7\3\7\3\7\3\7\7\7N\n\7\f\7\16\7Q\13\7\3\b\3\b\3"+
		"\b\3\b\7\bW\n\b\f\b\16\bZ\13\b\3\t\3\t\3\t\3\t\7\t`\n\t\f\t\16\tc\13\t"+
		"\3\n\3\n\3\n\5\nh\n\n\3\13\3\13\3\f\3\f\3\f\3\f\3\r\3\r\3\16\3\16\3\17"+
		"\3\17\3\20\3\20\3\21\3\21\3\21\2\2\22\2\4\6\b\n\f\16\20\22\24\26\30\32"+
		"\34\36 \2\6\3\2\5\6\3\2\7\n\3\2\13\f\3\2\r\17\2s\2\"\3\2\2\2\4(\3\2\2"+
		"\2\6\60\3\2\2\2\b8\3\2\2\2\n@\3\2\2\2\fI\3\2\2\2\16R\3\2\2\2\20[\3\2\2"+
		"\2\22g\3\2\2\2\24i\3\2\2\2\26k\3\2\2\2\30o\3\2\2\2\32q\3\2\2\2\34s\3\2"+
		"\2\2\36u\3\2\2\2 w\3\2\2\2\"$\5\4\3\2#%\7\24\2\2$#\3\2\2\2$%\3\2\2\2%"+
		"&\3\2\2\2&\'\7\2\2\3\'\3\3\2\2\2(.\5\6\4\2)*\7\25\2\2*+\5\4\3\2+,\7\23"+
		"\2\2,-\5\4\3\2-/\3\2\2\2.)\3\2\2\2./\3\2\2\2/\5\3\2\2\2\60\65\5\b\5\2"+
		"\61\62\7\3\2\2\62\64\5\b\5\2\63\61\3\2\2\2\64\67\3\2\2\2\65\63\3\2\2\2"+
		"\65\66\3\2\2\2\66\7\3\2\2\2\67\65\3\2\2\28=\5\n\6\29:\7\4\2\2:<\5\n\6"+
		"\2;9\3\2\2\2<?\3\2\2\2=;\3\2\2\2=>\3\2\2\2>\t\3\2\2\2?=\3\2\2\2@F\5\f"+
		"\7\2AB\5\32\16\2BC\5\f\7\2CE\3\2\2\2DA\3\2\2\2EH\3\2\2\2FD\3\2\2\2FG\3"+
		"\2\2\2G\13\3\2\2\2HF\3\2\2\2IO\5\16\b\2JK\5\34\17\2KL\5\16\b\2LN\3\2\2"+
		"\2MJ\3\2\2\2NQ\3\2\2\2OM\3\2\2\2OP\3\2\2\2P\r\3\2\2\2QO\3\2\2\2RX\5\20"+
		"\t\2ST\5\36\20\2TU\5\20\t\2UW\3\2\2\2VS\3\2\2\2WZ\3\2\2\2XV\3\2\2\2XY"+
		"\3\2\2\2Y\17\3\2\2\2ZX\3\2\2\2[a\5\22\n\2\\]\5 \21\2]^\5\22\n\2^`\3\2"+
		"\2\2_\\\3\2\2\2`c\3\2\2\2a_\3\2\2\2ab\3\2\2\2b\21\3\2\2\2ca\3\2\2\2dh"+
		"\5\24\13\2eh\5\26\f\2fh\5\30\r\2gd\3\2\2\2ge\3\2\2\2gf\3\2\2\2h\23\3\2"+
		"\2\2ij\7\26\2\2j\25\3\2\2\2kl\7\21\2\2lm\5\4\3\2mn\7\22\2\2n\27\3\2\2"+
		"\2op\7\20\2\2p\31\3\2\2\2qr\t\2\2\2r\33\3\2\2\2st\t\3\2\2t\35\3\2\2\2"+
		"uv\t\4\2\2v\37\3\2\2\2wx\t\5\2\2x!\3\2\2\2\13$.\65=FOXag";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}