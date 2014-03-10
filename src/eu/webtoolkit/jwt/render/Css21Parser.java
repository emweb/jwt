// $ANTLR 3.5 src/eu/webtoolkit/jwt/render/Css21.g 2014-03-10 11:55:29
package eu.webtoolkit.jwt.render;

import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

@SuppressWarnings("all")
public class Css21Parser extends Parser {
	public static final String[] tokenNames = new String[] {
		"<invalid>", "<EOR>", "<DOWN>", "<UP>", "A", "ANGLE", "B", "C", "CDC", 
		"CDO", "COLON", "COMMA", "COMMENT", "D", "DASHMATCH", "DIMENSION", "DOT", 
		"E", "EMS", "ESCAPE", "EXS", "F", "FREQ", "G", "GREATER", "H", "HASH", 
		"HEXCHAR", "I", "IDENT", "INCLUDES", "INVALID", "J", "K", "L", "LBRACE", 
		"LBRACKET", "LENGTH", "LPAREN", "M", "MINUS", "N", "NAME", "NMCHAR", "NMSTART", 
		"NONASCII", "NUMBER", "O", "OPEQ", "P", "PERCENTAGE", "PLUS", "Q", "R", 
		"RBRACE", "RBRACKET", "RPAREN", "S", "SEMI", "SOLIDUS", "STAR", "STRING", 
		"T", "TIME", "U", "URI", "URL", "V", "W", "WS", "X", "Y", "Z"
	};
	public static final int EOF=-1;
	public static final int A=4;
	public static final int ANGLE=5;
	public static final int B=6;
	public static final int C=7;
	public static final int CDC=8;
	public static final int CDO=9;
	public static final int COLON=10;
	public static final int COMMA=11;
	public static final int COMMENT=12;
	public static final int D=13;
	public static final int DASHMATCH=14;
	public static final int DIMENSION=15;
	public static final int DOT=16;
	public static final int E=17;
	public static final int EMS=18;
	public static final int ESCAPE=19;
	public static final int EXS=20;
	public static final int F=21;
	public static final int FREQ=22;
	public static final int G=23;
	public static final int GREATER=24;
	public static final int H=25;
	public static final int HASH=26;
	public static final int HEXCHAR=27;
	public static final int I=28;
	public static final int IDENT=29;
	public static final int INCLUDES=30;
	public static final int INVALID=31;
	public static final int J=32;
	public static final int K=33;
	public static final int L=34;
	public static final int LBRACE=35;
	public static final int LBRACKET=36;
	public static final int LENGTH=37;
	public static final int LPAREN=38;
	public static final int M=39;
	public static final int MINUS=40;
	public static final int N=41;
	public static final int NAME=42;
	public static final int NMCHAR=43;
	public static final int NMSTART=44;
	public static final int NONASCII=45;
	public static final int NUMBER=46;
	public static final int O=47;
	public static final int OPEQ=48;
	public static final int P=49;
	public static final int PERCENTAGE=50;
	public static final int PLUS=51;
	public static final int Q=52;
	public static final int R=53;
	public static final int RBRACE=54;
	public static final int RBRACKET=55;
	public static final int RPAREN=56;
	public static final int S=57;
	public static final int SEMI=58;
	public static final int SOLIDUS=59;
	public static final int STAR=60;
	public static final int STRING=61;
	public static final int T=62;
	public static final int TIME=63;
	public static final int U=64;
	public static final int URI=65;
	public static final int URL=66;
	public static final int V=67;
	public static final int W=68;
	public static final int WS=69;
	public static final int X=70;
	public static final int Y=71;
	public static final int Z=72;

	// delegates
	public Parser[] getDelegates() {
		return new Parser[] {};
	}

	// delegators


	public Css21Parser(TokenStream input) {
		this(input, new RecognizerSharedState());
	}
	public Css21Parser(TokenStream input, RecognizerSharedState state) {
		super(input, state);
	}

	@Override public String[] getTokenNames() { return Css21Parser.tokenNames; }
	@Override public String getGrammarFileName() { return "src/eu/webtoolkit/jwt/render/Css21.g"; }



	// $ANTLR start "styleSheet"
	// src/eu/webtoolkit/jwt/render/Css21.g:33:1: styleSheet : ( bodyset )* EOF ;
	public final void styleSheet() throws RecognitionException {
		try {
			// src/eu/webtoolkit/jwt/render/Css21.g:34:5: ( ( bodyset )* EOF )
			// src/eu/webtoolkit/jwt/render/Css21.g:34:9: ( bodyset )* EOF
			{
			// src/eu/webtoolkit/jwt/render/Css21.g:34:9: ( bodyset )*
			loop1:
			while (true) {
				int alt1=2;
				int LA1_0 = input.LA(1);
				if ( (LA1_0==DOT||LA1_0==HASH||LA1_0==IDENT||LA1_0==STAR) ) {
					alt1=1;
				}

				switch (alt1) {
				case 1 :
					// src/eu/webtoolkit/jwt/render/Css21.g:34:9: bodyset
					{
					pushFollow(FOLLOW_bodyset_in_styleSheet59);
					bodyset();
					state._fsp--;
					if (state.failed) return;
					}
					break;

				default :
					break loop1;
				}
			}

			match(input,EOF,FOLLOW_EOF_in_styleSheet67); if (state.failed) return;
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "styleSheet"



	// $ANTLR start "bodyset"
	// src/eu/webtoolkit/jwt/render/Css21.g:38:1: bodyset : ruleSet ;
	public final void bodyset() throws RecognitionException {
		try {
			// src/eu/webtoolkit/jwt/render/Css21.g:39:5: ( ruleSet )
			// src/eu/webtoolkit/jwt/render/Css21.g:39:7: ruleSet
			{
			pushFollow(FOLLOW_ruleSet_in_bodyset88);
			ruleSet();
			state._fsp--;
			if (state.failed) return;
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "bodyset"



	// $ANTLR start "operator"
	// src/eu/webtoolkit/jwt/render/Css21.g:42:1: operator : ( SOLIDUS | COMMA |);
	public final void operator() throws RecognitionException {
		try {
			// src/eu/webtoolkit/jwt/render/Css21.g:43:5: ( SOLIDUS | COMMA |)
			int alt2=3;
			switch ( input.LA(1) ) {
			case SOLIDUS:
				{
				alt2=1;
				}
				break;
			case COMMA:
				{
				alt2=2;
				}
				break;
			case ANGLE:
			case EMS:
			case EXS:
			case FREQ:
			case HASH:
			case IDENT:
			case LENGTH:
			case MINUS:
			case NUMBER:
			case PERCENTAGE:
			case PLUS:
			case STRING:
			case TIME:
			case URI:
			case WS:
				{
				alt2=3;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 2, 0, input);
				throw nvae;
			}
			switch (alt2) {
				case 1 :
					// src/eu/webtoolkit/jwt/render/Css21.g:43:7: SOLIDUS
					{
					match(input,SOLIDUS,FOLLOW_SOLIDUS_in_operator109); if (state.failed) return;
					}
					break;
				case 2 :
					// src/eu/webtoolkit/jwt/render/Css21.g:44:7: COMMA
					{
					match(input,COMMA,FOLLOW_COMMA_in_operator117); if (state.failed) return;
					}
					break;
				case 3 :
					// src/eu/webtoolkit/jwt/render/Css21.g:46:5: 
					{
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "operator"



	// $ANTLR start "combinator"
	// src/eu/webtoolkit/jwt/render/Css21.g:49:1: protected combinator : ( PLUS | GREATER | WS |);
	public final void combinator() throws RecognitionException {
		try {
			// src/eu/webtoolkit/jwt/render/Css21.g:50:5: ( PLUS | GREATER | WS |)
			int alt3=4;
			switch ( input.LA(1) ) {
			case PLUS:
				{
				alt3=1;
				}
				break;
			case GREATER:
				{
				alt3=2;
				}
				break;
			case WS:
				{
				alt3=3;
				}
				break;
			case DOT:
			case HASH:
			case IDENT:
			case STAR:
				{
				alt3=4;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 3, 0, input);
				throw nvae;
			}
			switch (alt3) {
				case 1 :
					// src/eu/webtoolkit/jwt/render/Css21.g:50:7: PLUS
					{
					match(input,PLUS,FOLLOW_PLUS_in_combinator142); if (state.failed) return;
					}
					break;
				case 2 :
					// src/eu/webtoolkit/jwt/render/Css21.g:51:7: GREATER
					{
					match(input,GREATER,FOLLOW_GREATER_in_combinator150); if (state.failed) return;
					}
					break;
				case 3 :
					// src/eu/webtoolkit/jwt/render/Css21.g:52:7: WS
					{
					match(input,WS,FOLLOW_WS_in_combinator158); if (state.failed) return;
					}
					break;
				case 4 :
					// src/eu/webtoolkit/jwt/render/Css21.g:54:5: 
					{
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "combinator"



	// $ANTLR start "unaryOperator"
	// src/eu/webtoolkit/jwt/render/Css21.g:56:1: unaryOperator : ( MINUS | PLUS );
	public final void unaryOperator() throws RecognitionException {
		try {
			// src/eu/webtoolkit/jwt/render/Css21.g:57:5: ( MINUS | PLUS )
			// src/eu/webtoolkit/jwt/render/Css21.g:
			{
			if ( input.LA(1)==MINUS||input.LA(1)==PLUS ) {
				input.consume();
				state.errorRecovery=false;
				state.failed=false;
			}
			else {
				if (state.backtracking>0) {state.failed=true; return;}
				MismatchedSetException mse = new MismatchedSetException(null,input);
				throw mse;
			}
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "unaryOperator"



	// $ANTLR start "property"
	// src/eu/webtoolkit/jwt/render/Css21.g:61:1: property : IDENT ;
	public final void property() throws RecognitionException {
		try {
			// src/eu/webtoolkit/jwt/render/Css21.g:62:5: ( IDENT )
			// src/eu/webtoolkit/jwt/render/Css21.g:62:7: IDENT
			{
			match(input,IDENT,FOLLOW_IDENT_in_property216); if (state.failed) return;
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "property"



	// $ANTLR start "ruleSet"
	// src/eu/webtoolkit/jwt/render/Css21.g:65:1: ruleSet : selector ( COMMA ( WS )? selector )* ( WS )? LBRACE declarationBlock RBRACE ( WS )? ;
	public final void ruleSet() throws RecognitionException {
		ParserRuleReturnScope declarationBlock1 =null;

		try {
			// src/eu/webtoolkit/jwt/render/Css21.g:66:5: ( selector ( COMMA ( WS )? selector )* ( WS )? LBRACE declarationBlock RBRACE ( WS )? )
			// src/eu/webtoolkit/jwt/render/Css21.g:66:8: selector ( COMMA ( WS )? selector )* ( WS )? LBRACE declarationBlock RBRACE ( WS )?
			{
			pushFollow(FOLLOW_selector_in_ruleSet238);
			selector();
			state._fsp--;
			if (state.failed) return;
			if ( state.backtracking==0 ) {CssParser.pushCurrentSelector();}
			// src/eu/webtoolkit/jwt/render/Css21.g:67:8: ( COMMA ( WS )? selector )*
			loop5:
			while (true) {
				int alt5=2;
				int LA5_0 = input.LA(1);
				if ( (LA5_0==COMMA) ) {
					alt5=1;
				}

				switch (alt5) {
				case 1 :
					// src/eu/webtoolkit/jwt/render/Css21.g:67:9: COMMA ( WS )? selector
					{
					match(input,COMMA,FOLLOW_COMMA_in_ruleSet262); if (state.failed) return;
					// src/eu/webtoolkit/jwt/render/Css21.g:67:15: ( WS )?
					int alt4=2;
					int LA4_0 = input.LA(1);
					if ( (LA4_0==WS) ) {
						alt4=1;
					}
					switch (alt4) {
						case 1 :
							// src/eu/webtoolkit/jwt/render/Css21.g:67:15: WS
							{
							match(input,WS,FOLLOW_WS_in_ruleSet264); if (state.failed) return;
							}
							break;

					}

					pushFollow(FOLLOW_selector_in_ruleSet267);
					selector();
					state._fsp--;
					if (state.failed) return;
					if ( state.backtracking==0 ) {CssParser.pushCurrentSelector();}
					}
					break;

				default :
					break loop5;
				}
			}

			// src/eu/webtoolkit/jwt/render/Css21.g:68:8: ( WS )?
			int alt6=2;
			int LA6_0 = input.LA(1);
			if ( (LA6_0==WS) ) {
				alt6=1;
			}
			switch (alt6) {
				case 1 :
					// src/eu/webtoolkit/jwt/render/Css21.g:68:8: WS
					{
					match(input,WS,FOLLOW_WS_in_ruleSet285); if (state.failed) return;
					}
					break;

			}

			match(input,LBRACE,FOLLOW_LBRACE_in_ruleSet288); if (state.failed) return;
			pushFollow(FOLLOW_declarationBlock_in_ruleSet299);
			declarationBlock1=declarationBlock();
			state._fsp--;
			if (state.failed) return;
			if ( state.backtracking==0 ) {CssParser.setAndPushDeclarationBlock((declarationBlock1!=null?input.toString(declarationBlock1.start,declarationBlock1.stop):null));}
			match(input,RBRACE,FOLLOW_RBRACE_in_ruleSet314); if (state.failed) return;
			// src/eu/webtoolkit/jwt/render/Css21.g:71:8: ( WS )?
			int alt7=2;
			int LA7_0 = input.LA(1);
			if ( (LA7_0==WS) ) {
				alt7=1;
			}
			switch (alt7) {
				case 1 :
					// src/eu/webtoolkit/jwt/render/Css21.g:71:8: WS
					{
					match(input,WS,FOLLOW_WS_in_ruleSet323); if (state.failed) return;
					}
					break;

			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "ruleSet"


	public static class declarationBlock_return extends ParserRuleReturnScope {
	};


	// $ANTLR start "declarationBlock"
	// src/eu/webtoolkit/jwt/render/Css21.g:74:1: declarationBlock : ( declaration )? ( SEMI declaration )* ( SEMI )? ;
	public final Css21Parser.declarationBlock_return declarationBlock() throws RecognitionException {
		Css21Parser.declarationBlock_return retval = new Css21Parser.declarationBlock_return();
		retval.start = input.LT(1);

		try {
			// src/eu/webtoolkit/jwt/render/Css21.g:75:5: ( ( declaration )? ( SEMI declaration )* ( SEMI )? )
			// src/eu/webtoolkit/jwt/render/Css21.g:75:7: ( declaration )? ( SEMI declaration )* ( SEMI )?
			{
			// src/eu/webtoolkit/jwt/render/Css21.g:75:7: ( declaration )?
			int alt8=2;
			int LA8_0 = input.LA(1);
			if ( (LA8_0==IDENT) ) {
				alt8=1;
			}
			switch (alt8) {
				case 1 :
					// src/eu/webtoolkit/jwt/render/Css21.g:75:7: declaration
					{
					pushFollow(FOLLOW_declaration_in_declarationBlock341);
					declaration();
					state._fsp--;
					if (state.failed) return retval;
					}
					break;

			}

			// src/eu/webtoolkit/jwt/render/Css21.g:76:7: ( SEMI declaration )*
			loop9:
			while (true) {
				int alt9=2;
				int LA9_0 = input.LA(1);
				if ( (LA9_0==SEMI) ) {
					int LA9_1 = input.LA(2);
					if ( (LA9_1==IDENT) ) {
						alt9=1;
					}

				}

				switch (alt9) {
				case 1 :
					// src/eu/webtoolkit/jwt/render/Css21.g:76:8: SEMI declaration
					{
					match(input,SEMI,FOLLOW_SEMI_in_declarationBlock351); if (state.failed) return retval;
					pushFollow(FOLLOW_declaration_in_declarationBlock353);
					declaration();
					state._fsp--;
					if (state.failed) return retval;
					}
					break;

				default :
					break loop9;
				}
			}

			// src/eu/webtoolkit/jwt/render/Css21.g:77:7: ( SEMI )?
			int alt10=2;
			int LA10_0 = input.LA(1);
			if ( (LA10_0==SEMI) ) {
				alt10=1;
			}
			switch (alt10) {
				case 1 :
					// src/eu/webtoolkit/jwt/render/Css21.g:77:7: SEMI
					{
					match(input,SEMI,FOLLOW_SEMI_in_declarationBlock363); if (state.failed) return retval;
					}
					break;

			}

			}

			retval.stop = input.LT(-1);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "declarationBlock"



	// $ANTLR start "selector"
	// src/eu/webtoolkit/jwt/render/Css21.g:80:1: selector : s1= simpleSelector ( combinator s2= simpleSelector )* ;
	public final void selector() throws RecognitionException {
		try {
			// src/eu/webtoolkit/jwt/render/Css21.g:81:5: (s1= simpleSelector ( combinator s2= simpleSelector )* )
			// src/eu/webtoolkit/jwt/render/Css21.g:81:7: s1= simpleSelector ( combinator s2= simpleSelector )*
			{
			pushFollow(FOLLOW_simpleSelector_in_selector387);
			simpleSelector();
			state._fsp--;
			if (state.failed) return;
			if ( state.backtracking==0 ) { CssParser.pushCurrentSimpleSelector(); }
			// src/eu/webtoolkit/jwt/render/Css21.g:82:7: ( combinator s2= simpleSelector )*
			loop11:
			while (true) {
				int alt11=2;
				int LA11_0 = input.LA(1);
				if ( (LA11_0==WS) ) {
					int LA11_2 = input.LA(2);
					if ( (LA11_2==DOT||LA11_2==HASH||LA11_2==IDENT||LA11_2==STAR) ) {
						alt11=1;
					}

				}
				else if ( (LA11_0==DOT||LA11_0==GREATER||LA11_0==HASH||LA11_0==IDENT||LA11_0==PLUS||LA11_0==STAR) ) {
					alt11=1;
				}

				switch (alt11) {
				case 1 :
					// src/eu/webtoolkit/jwt/render/Css21.g:82:8: combinator s2= simpleSelector
					{
					pushFollow(FOLLOW_combinator_in_selector411);
					combinator();
					state._fsp--;
					if (state.failed) return;
					pushFollow(FOLLOW_simpleSelector_in_selector415);
					simpleSelector();
					state._fsp--;
					if (state.failed) return;
					if ( state.backtracking==0 ) { CssParser.pushCurrentSimpleSelector(); }
					}
					break;

				default :
					break loop11;
				}
			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "selector"



	// $ANTLR start "simpleSelector"
	// src/eu/webtoolkit/jwt/render/Css21.g:86:1: simpleSelector : ( ( elementName ( ( esPred )=> elementSubsequent )* ) | ( ( esPred )=> (s5= elementSubsequent ) )+ );
	public final void simpleSelector() throws RecognitionException {
		ParserRuleReturnScope elementName2 =null;

		try {
			// src/eu/webtoolkit/jwt/render/Css21.g:87:5: ( ( elementName ( ( esPred )=> elementSubsequent )* ) | ( ( esPred )=> (s5= elementSubsequent ) )+ )
			int alt14=2;
			int LA14_0 = input.LA(1);
			if ( (LA14_0==IDENT||LA14_0==STAR) ) {
				alt14=1;
			}
			else if ( (LA14_0==DOT||LA14_0==HASH) ) {
				alt14=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 14, 0, input);
				throw nvae;
			}

			switch (alt14) {
				case 1 :
					// src/eu/webtoolkit/jwt/render/Css21.g:88:3: ( elementName ( ( esPred )=> elementSubsequent )* )
					{
					// src/eu/webtoolkit/jwt/render/Css21.g:88:3: ( elementName ( ( esPred )=> elementSubsequent )* )
					// src/eu/webtoolkit/jwt/render/Css21.g:89:3: elementName ( ( esPred )=> elementSubsequent )*
					{
					pushFollow(FOLLOW_elementName_in_simpleSelector449);
					elementName2=elementName();
					state._fsp--;
					if (state.failed) return;
					if ( state.backtracking==0 ) { CssParser.setSimpleSelectorElementName((elementName2!=null?input.toString(elementName2.start,elementName2.stop):null)); }
					// src/eu/webtoolkit/jwt/render/Css21.g:90:3: ( ( esPred )=> elementSubsequent )*
					loop12:
					while (true) {
						int alt12=2;
						int LA12_0 = input.LA(1);
						if ( (LA12_0==HASH) ) {
							int LA12_2 = input.LA(2);
							if ( (synpred1_Css21()) ) {
								alt12=1;
							}

						}
						else if ( (LA12_0==DOT) ) {
							int LA12_3 = input.LA(2);
							if ( (LA12_3==IDENT) ) {
								int LA12_5 = input.LA(3);
								if ( (synpred1_Css21()) ) {
									alt12=1;
								}

							}

						}

						switch (alt12) {
						case 1 :
							// src/eu/webtoolkit/jwt/render/Css21.g:90:4: ( esPred )=> elementSubsequent
							{
							pushFollow(FOLLOW_elementSubsequent_in_simpleSelector472);
							elementSubsequent();
							state._fsp--;
							if (state.failed) return;
							}
							break;

						default :
							break loop12;
						}
					}

					}

					}
					break;
				case 2 :
					// src/eu/webtoolkit/jwt/render/Css21.g:93:3: ( ( esPred )=> (s5= elementSubsequent ) )+
					{
					// src/eu/webtoolkit/jwt/render/Css21.g:93:3: ( ( esPred )=> (s5= elementSubsequent ) )+
					int cnt13=0;
					loop13:
					while (true) {
						int alt13=2;
						int LA13_0 = input.LA(1);
						if ( (LA13_0==HASH) ) {
							int LA13_2 = input.LA(2);
							if ( (synpred2_Css21()) ) {
								alt13=1;
							}

						}
						else if ( (LA13_0==DOT) ) {
							int LA13_3 = input.LA(2);
							if ( (synpred2_Css21()) ) {
								alt13=1;
							}

						}

						switch (alt13) {
						case 1 :
							// src/eu/webtoolkit/jwt/render/Css21.g:94:3: ( esPred )=> (s5= elementSubsequent )
							{
							// src/eu/webtoolkit/jwt/render/Css21.g:94:13: (s5= elementSubsequent )
							// src/eu/webtoolkit/jwt/render/Css21.g:94:14: s5= elementSubsequent
							{
							pushFollow(FOLLOW_elementSubsequent_in_simpleSelector495);
							elementSubsequent();
							state._fsp--;
							if (state.failed) return;
							}

							}
							break;

						default :
							if ( cnt13 >= 1 ) break loop13;
							if (state.backtracking>0) {state.failed=true; return;}
							EarlyExitException eee = new EarlyExitException(13, input);
							throw eee;
						}
						cnt13++;
					}

					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "simpleSelector"



	// $ANTLR start "esPred"
	// src/eu/webtoolkit/jwt/render/Css21.g:98:1: esPred : ( HASH | DOT );
	public final void esPred() throws RecognitionException {
		try {
			// src/eu/webtoolkit/jwt/render/Css21.g:99:5: ( HASH | DOT )
			// src/eu/webtoolkit/jwt/render/Css21.g:
			{
			if ( input.LA(1)==DOT||input.LA(1)==HASH ) {
				input.consume();
				state.errorRecovery=false;
				state.failed=false;
			}
			else {
				if (state.backtracking>0) {state.failed=true; return;}
				MismatchedSetException mse = new MismatchedSetException(null,input);
				throw mse;
			}
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "esPred"



	// $ANTLR start "elementSubsequent"
	// src/eu/webtoolkit/jwt/render/Css21.g:102:1: elementSubsequent : (h= HASH | cssClass );
	public final void elementSubsequent() throws RecognitionException {
		Token h=null;
		ParserRuleReturnScope cssClass3 =null;

		try {
			// src/eu/webtoolkit/jwt/render/Css21.g:103:5: (h= HASH | cssClass )
			int alt15=2;
			int LA15_0 = input.LA(1);
			if ( (LA15_0==HASH) ) {
				alt15=1;
			}
			else if ( (LA15_0==DOT) ) {
				alt15=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 15, 0, input);
				throw nvae;
			}

			switch (alt15) {
				case 1 :
					// src/eu/webtoolkit/jwt/render/Css21.g:103:7: h= HASH
					{
					h=(Token)match(input,HASH,FOLLOW_HASH_in_elementSubsequent547); if (state.failed) return;
					if ( state.backtracking==0 ) { CssParser.setSimpleSelectorHash (h       .getText());}
					}
					break;
				case 2 :
					// src/eu/webtoolkit/jwt/render/Css21.g:104:7: cssClass
					{
					pushFollow(FOLLOW_cssClass_in_elementSubsequent559);
					cssClass3=cssClass();
					state._fsp--;
					if (state.failed) return;
					if ( state.backtracking==0 ) { CssParser.addSimpleSelectorClass((cssClass3!=null?input.toString(cssClass3.start,cssClass3.stop):null));}
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "elementSubsequent"


	public static class cssClass_return extends ParserRuleReturnScope {
	};


	// $ANTLR start "cssClass"
	// src/eu/webtoolkit/jwt/render/Css21.g:107:1: cssClass : DOT IDENT ;
	public final Css21Parser.cssClass_return cssClass() throws RecognitionException {
		Css21Parser.cssClass_return retval = new Css21Parser.cssClass_return();
		retval.start = input.LT(1);

		try {
			// src/eu/webtoolkit/jwt/render/Css21.g:108:5: ( DOT IDENT )
			// src/eu/webtoolkit/jwt/render/Css21.g:108:7: DOT IDENT
			{
			match(input,DOT,FOLLOW_DOT_in_cssClass582); if (state.failed) return retval;
			match(input,IDENT,FOLLOW_IDENT_in_cssClass584); if (state.failed) return retval;
			}

			retval.stop = input.LT(-1);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "cssClass"


	public static class elementName_return extends ParserRuleReturnScope {
	};


	// $ANTLR start "elementName"
	// src/eu/webtoolkit/jwt/render/Css21.g:111:1: elementName : ( IDENT | STAR );
	public final Css21Parser.elementName_return elementName() throws RecognitionException {
		Css21Parser.elementName_return retval = new Css21Parser.elementName_return();
		retval.start = input.LT(1);

		try {
			// src/eu/webtoolkit/jwt/render/Css21.g:112:5: ( IDENT | STAR )
			// src/eu/webtoolkit/jwt/render/Css21.g:
			{
			if ( input.LA(1)==IDENT||input.LA(1)==STAR ) {
				input.consume();
				state.errorRecovery=false;
				state.failed=false;
			}
			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				MismatchedSetException mse = new MismatchedSetException(null,input);
				throw mse;
			}
			}

			retval.stop = input.LT(-1);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "elementName"



	// $ANTLR start "declaration"
	// src/eu/webtoolkit/jwt/render/Css21.g:116:1: declaration : property COLON expr ;
	public final void declaration() throws RecognitionException {
		try {
			// src/eu/webtoolkit/jwt/render/Css21.g:117:5: ( property COLON expr )
			// src/eu/webtoolkit/jwt/render/Css21.g:117:7: property COLON expr
			{
			pushFollow(FOLLOW_property_in_declaration634);
			property();
			state._fsp--;
			if (state.failed) return;
			match(input,COLON,FOLLOW_COLON_in_declaration636); if (state.failed) return;
			pushFollow(FOLLOW_expr_in_declaration638);
			expr();
			state._fsp--;
			if (state.failed) return;
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "declaration"



	// $ANTLR start "expr"
	// src/eu/webtoolkit/jwt/render/Css21.g:120:1: expr : term ( operator term )* ;
	public final void expr() throws RecognitionException {
		try {
			// src/eu/webtoolkit/jwt/render/Css21.g:121:5: ( term ( operator term )* )
			// src/eu/webtoolkit/jwt/render/Css21.g:121:7: term ( operator term )*
			{
			pushFollow(FOLLOW_term_in_expr663);
			term();
			state._fsp--;
			if (state.failed) return;
			// src/eu/webtoolkit/jwt/render/Css21.g:121:12: ( operator term )*
			loop16:
			while (true) {
				int alt16=2;
				int LA16_0 = input.LA(1);
				if ( (LA16_0==ANGLE||LA16_0==COMMA||LA16_0==EMS||LA16_0==EXS||LA16_0==FREQ||LA16_0==HASH||LA16_0==IDENT||LA16_0==LENGTH||LA16_0==MINUS||LA16_0==NUMBER||(LA16_0 >= PERCENTAGE && LA16_0 <= PLUS)||LA16_0==SOLIDUS||LA16_0==STRING||LA16_0==TIME||LA16_0==URI||LA16_0==WS) ) {
					alt16=1;
				}

				switch (alt16) {
				case 1 :
					// src/eu/webtoolkit/jwt/render/Css21.g:121:13: operator term
					{
					pushFollow(FOLLOW_operator_in_expr666);
					operator();
					state._fsp--;
					if (state.failed) return;
					pushFollow(FOLLOW_term_in_expr668);
					term();
					state._fsp--;
					if (state.failed) return;
					}
					break;

				default :
					break loop16;
				}
			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "expr"



	// $ANTLR start "term"
	// src/eu/webtoolkit/jwt/render/Css21.g:124:1: term : ( ( unaryOperator )? ( WS )? ( NUMBER | PERCENTAGE | LENGTH | EMS | EXS | ANGLE | TIME | FREQ ) | STRING | IDENT | URI | hexColor );
	public final void term() throws RecognitionException {
		try {
			// src/eu/webtoolkit/jwt/render/Css21.g:125:5: ( ( unaryOperator )? ( WS )? ( NUMBER | PERCENTAGE | LENGTH | EMS | EXS | ANGLE | TIME | FREQ ) | STRING | IDENT | URI | hexColor )
			int alt19=5;
			switch ( input.LA(1) ) {
			case ANGLE:
			case EMS:
			case EXS:
			case FREQ:
			case LENGTH:
			case MINUS:
			case NUMBER:
			case PERCENTAGE:
			case PLUS:
			case TIME:
			case WS:
				{
				alt19=1;
				}
				break;
			case STRING:
				{
				alt19=2;
				}
				break;
			case IDENT:
				{
				alt19=3;
				}
				break;
			case URI:
				{
				alt19=4;
				}
				break;
			case HASH:
				{
				alt19=5;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 19, 0, input);
				throw nvae;
			}
			switch (alt19) {
				case 1 :
					// src/eu/webtoolkit/jwt/render/Css21.g:125:7: ( unaryOperator )? ( WS )? ( NUMBER | PERCENTAGE | LENGTH | EMS | EXS | ANGLE | TIME | FREQ )
					{
					// src/eu/webtoolkit/jwt/render/Css21.g:125:7: ( unaryOperator )?
					int alt17=2;
					int LA17_0 = input.LA(1);
					if ( (LA17_0==MINUS||LA17_0==PLUS) ) {
						alt17=1;
					}
					switch (alt17) {
						case 1 :
							// src/eu/webtoolkit/jwt/render/Css21.g:125:7: unaryOperator
							{
							pushFollow(FOLLOW_unaryOperator_in_term691);
							unaryOperator();
							state._fsp--;
							if (state.failed) return;
							}
							break;

					}

					// src/eu/webtoolkit/jwt/render/Css21.g:126:7: ( WS )?
					int alt18=2;
					int LA18_0 = input.LA(1);
					if ( (LA18_0==WS) ) {
						alt18=1;
					}
					switch (alt18) {
						case 1 :
							// src/eu/webtoolkit/jwt/render/Css21.g:126:7: WS
							{
							match(input,WS,FOLLOW_WS_in_term700); if (state.failed) return;
							}
							break;

					}

					if ( input.LA(1)==ANGLE||input.LA(1)==EMS||input.LA(1)==EXS||input.LA(1)==FREQ||input.LA(1)==LENGTH||input.LA(1)==NUMBER||input.LA(1)==PERCENTAGE||input.LA(1)==TIME ) {
						input.consume();
						state.errorRecovery=false;
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					}
					break;
				case 2 :
					// src/eu/webtoolkit/jwt/render/Css21.g:137:7: STRING
					{
					match(input,STRING,FOLLOW_STRING_in_term857); if (state.failed) return;
					}
					break;
				case 3 :
					// src/eu/webtoolkit/jwt/render/Css21.g:138:7: IDENT
					{
					match(input,IDENT,FOLLOW_IDENT_in_term865); if (state.failed) return;
					}
					break;
				case 4 :
					// src/eu/webtoolkit/jwt/render/Css21.g:139:7: URI
					{
					match(input,URI,FOLLOW_URI_in_term873); if (state.failed) return;
					}
					break;
				case 5 :
					// src/eu/webtoolkit/jwt/render/Css21.g:140:7: hexColor
					{
					pushFollow(FOLLOW_hexColor_in_term881);
					hexColor();
					state._fsp--;
					if (state.failed) return;
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "term"



	// $ANTLR start "hexColor"
	// src/eu/webtoolkit/jwt/render/Css21.g:143:1: hexColor : HASH ;
	public final void hexColor() throws RecognitionException {
		try {
			// src/eu/webtoolkit/jwt/render/Css21.g:144:5: ( HASH )
			// src/eu/webtoolkit/jwt/render/Css21.g:144:7: HASH
			{
			match(input,HASH,FOLLOW_HASH_in_hexColor902); if (state.failed) return;
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "hexColor"

	// $ANTLR start synpred1_Css21
	public final void synpred1_Css21_fragment() throws RecognitionException {
		// src/eu/webtoolkit/jwt/render/Css21.g:90:4: ( esPred )
		// src/eu/webtoolkit/jwt/render/Css21.g:90:5: esPred
		{
		pushFollow(FOLLOW_esPred_in_synpred1_Css21469);
		esPred();
		state._fsp--;
		if (state.failed) return;
		}

	}
	// $ANTLR end synpred1_Css21

	// $ANTLR start synpred2_Css21
	public final void synpred2_Css21_fragment() throws RecognitionException {
		// src/eu/webtoolkit/jwt/render/Css21.g:94:3: ( esPred )
		// src/eu/webtoolkit/jwt/render/Css21.g:94:4: esPred
		{
		pushFollow(FOLLOW_esPred_in_synpred2_Css21489);
		esPred();
		state._fsp--;
		if (state.failed) return;
		}

	}
	// $ANTLR end synpred2_Css21

	// Delegated rules

	public final boolean synpred1_Css21() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred1_Css21_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred2_Css21() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred2_Css21_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}



	public static final BitSet FOLLOW_bodyset_in_styleSheet59 = new BitSet(new long[]{0x1000000024010000L});
	public static final BitSet FOLLOW_EOF_in_styleSheet67 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ruleSet_in_bodyset88 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_SOLIDUS_in_operator109 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_COMMA_in_operator117 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_PLUS_in_combinator142 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_GREATER_in_combinator150 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_WS_in_combinator158 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IDENT_in_property216 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_selector_in_ruleSet238 = new BitSet(new long[]{0x0000000800000800L,0x0000000000000020L});
	public static final BitSet FOLLOW_COMMA_in_ruleSet262 = new BitSet(new long[]{0x1000000024010000L,0x0000000000000020L});
	public static final BitSet FOLLOW_WS_in_ruleSet264 = new BitSet(new long[]{0x1000000024010000L});
	public static final BitSet FOLLOW_selector_in_ruleSet267 = new BitSet(new long[]{0x0000000800000800L,0x0000000000000020L});
	public static final BitSet FOLLOW_WS_in_ruleSet285 = new BitSet(new long[]{0x0000000800000000L});
	public static final BitSet FOLLOW_LBRACE_in_ruleSet288 = new BitSet(new long[]{0x0440000020000000L});
	public static final BitSet FOLLOW_declarationBlock_in_ruleSet299 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_RBRACE_in_ruleSet314 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000020L});
	public static final BitSet FOLLOW_WS_in_ruleSet323 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_declaration_in_declarationBlock341 = new BitSet(new long[]{0x0400000000000002L});
	public static final BitSet FOLLOW_SEMI_in_declarationBlock351 = new BitSet(new long[]{0x0000000020000000L});
	public static final BitSet FOLLOW_declaration_in_declarationBlock353 = new BitSet(new long[]{0x0400000000000002L});
	public static final BitSet FOLLOW_SEMI_in_declarationBlock363 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_simpleSelector_in_selector387 = new BitSet(new long[]{0x1008000025010002L,0x0000000000000020L});
	public static final BitSet FOLLOW_combinator_in_selector411 = new BitSet(new long[]{0x1000000024010000L});
	public static final BitSet FOLLOW_simpleSelector_in_selector415 = new BitSet(new long[]{0x1008000025010002L,0x0000000000000020L});
	public static final BitSet FOLLOW_elementName_in_simpleSelector449 = new BitSet(new long[]{0x0000000004010002L});
	public static final BitSet FOLLOW_elementSubsequent_in_simpleSelector472 = new BitSet(new long[]{0x0000000004010002L});
	public static final BitSet FOLLOW_elementSubsequent_in_simpleSelector495 = new BitSet(new long[]{0x0000000004010002L});
	public static final BitSet FOLLOW_HASH_in_elementSubsequent547 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_cssClass_in_elementSubsequent559 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_DOT_in_cssClass582 = new BitSet(new long[]{0x0000000020000000L});
	public static final BitSet FOLLOW_IDENT_in_cssClass584 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_property_in_declaration634 = new BitSet(new long[]{0x0000000000000400L});
	public static final BitSet FOLLOW_COLON_in_declaration636 = new BitSet(new long[]{0xA00C412024540020L,0x0000000000000022L});
	public static final BitSet FOLLOW_expr_in_declaration638 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_term_in_expr663 = new BitSet(new long[]{0xA80C412024540822L,0x0000000000000022L});
	public static final BitSet FOLLOW_operator_in_expr666 = new BitSet(new long[]{0xA00C412024540020L,0x0000000000000022L});
	public static final BitSet FOLLOW_term_in_expr668 = new BitSet(new long[]{0xA80C412024540822L,0x0000000000000022L});
	public static final BitSet FOLLOW_unaryOperator_in_term691 = new BitSet(new long[]{0x8004402000540020L,0x0000000000000020L});
	public static final BitSet FOLLOW_WS_in_term700 = new BitSet(new long[]{0x8004402000540020L});
	public static final BitSet FOLLOW_set_in_term711 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_STRING_in_term857 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IDENT_in_term865 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_URI_in_term873 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_hexColor_in_term881 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_HASH_in_hexColor902 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_esPred_in_synpred1_Css21469 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_esPred_in_synpred2_Css21489 = new BitSet(new long[]{0x0000000000000002L});
}
