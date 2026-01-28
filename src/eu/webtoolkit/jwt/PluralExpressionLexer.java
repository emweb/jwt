// Generated from PluralExpression.g4 by ANTLR 4.7.2
package eu.webtoolkit.jwt;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class PluralExpressionLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.7.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		OR=1, AND=2, EQ=3, NEQ=4, GT=5, GTE=6, LT=7, LTE=8, PLUS=9, MINUS=10, 
		MULT=11, DIV=12, MOD=13, N=14, LPAREN=15, RPAREN=16, COLON=17, SEMICOLON=18, 
		QMARK=19, INTEGER=20, WS=21;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"OR", "AND", "EQ", "NEQ", "GT", "GTE", "LT", "LTE", "PLUS", "MINUS", 
			"MULT", "DIV", "MOD", "N", "LPAREN", "RPAREN", "COLON", "SEMICOLON", 
			"QMARK", "INTEGER", "WS"
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


	public PluralExpressionLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "PluralExpression.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\27e\b\1\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\3\2\3\2\3\2\3\3\3\3\3\3\3\4\3"+
		"\4\3\4\3\5\3\5\3\5\3\6\3\6\3\7\3\7\3\7\3\b\3\b\3\t\3\t\3\t\3\n\3\n\3\13"+
		"\3\13\3\f\3\f\3\r\3\r\3\16\3\16\3\17\3\17\3\20\3\20\3\21\3\21\3\22\3\22"+
		"\3\23\3\23\3\24\3\24\3\25\6\25[\n\25\r\25\16\25\\\3\26\6\26`\n\26\r\26"+
		"\16\26a\3\26\3\26\2\2\27\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f"+
		"\27\r\31\16\33\17\35\20\37\21!\22#\23%\24\'\25)\26+\27\3\2\4\3\2\62;\5"+
		"\2\13\f\17\17\"\"\2f\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2"+
		"\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3"+
		"\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2"+
		"\2\2!\3\2\2\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3\2\2\2\2)\3\2\2\2\2+\3\2\2\2"+
		"\3-\3\2\2\2\5\60\3\2\2\2\7\63\3\2\2\2\t\66\3\2\2\2\139\3\2\2\2\r;\3\2"+
		"\2\2\17>\3\2\2\2\21@\3\2\2\2\23C\3\2\2\2\25E\3\2\2\2\27G\3\2\2\2\31I\3"+
		"\2\2\2\33K\3\2\2\2\35M\3\2\2\2\37O\3\2\2\2!Q\3\2\2\2#S\3\2\2\2%U\3\2\2"+
		"\2\'W\3\2\2\2)Z\3\2\2\2+_\3\2\2\2-.\7~\2\2./\7~\2\2/\4\3\2\2\2\60\61\7"+
		"(\2\2\61\62\7(\2\2\62\6\3\2\2\2\63\64\7?\2\2\64\65\7?\2\2\65\b\3\2\2\2"+
		"\66\67\7#\2\2\678\7?\2\28\n\3\2\2\29:\7@\2\2:\f\3\2\2\2;<\7@\2\2<=\7?"+
		"\2\2=\16\3\2\2\2>?\7>\2\2?\20\3\2\2\2@A\7>\2\2AB\7?\2\2B\22\3\2\2\2CD"+
		"\7-\2\2D\24\3\2\2\2EF\7/\2\2F\26\3\2\2\2GH\7,\2\2H\30\3\2\2\2IJ\7\61\2"+
		"\2J\32\3\2\2\2KL\7\'\2\2L\34\3\2\2\2MN\7p\2\2N\36\3\2\2\2OP\7*\2\2P \3"+
		"\2\2\2QR\7+\2\2R\"\3\2\2\2ST\7<\2\2T$\3\2\2\2UV\7=\2\2V&\3\2\2\2WX\7A"+
		"\2\2X(\3\2\2\2Y[\t\2\2\2ZY\3\2\2\2[\\\3\2\2\2\\Z\3\2\2\2\\]\3\2\2\2]*"+
		"\3\2\2\2^`\t\3\2\2_^\3\2\2\2`a\3\2\2\2a_\3\2\2\2ab\3\2\2\2bc\3\2\2\2c"+
		"d\b\26\2\2d,\3\2\2\2\5\2\\a\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}