// Generated from Css22.g4 by ANTLR 4.7.2
package eu.webtoolkit.jwt.render;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link Css22Parser}.
 */
public interface Css22Listener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link Css22Parser#styleSheet}.
	 * @param ctx the parse tree
	 */
	void enterStyleSheet(Css22Parser.StyleSheetContext ctx);
	/**
	 * Exit a parse tree produced by {@link Css22Parser#styleSheet}.
	 * @param ctx the parse tree
	 */
	void exitStyleSheet(Css22Parser.StyleSheetContext ctx);
	/**
	 * Enter a parse tree produced by {@link Css22Parser#importStmt}.
	 * @param ctx the parse tree
	 */
	void enterImportStmt(Css22Parser.ImportStmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link Css22Parser#importStmt}.
	 * @param ctx the parse tree
	 */
	void exitImportStmt(Css22Parser.ImportStmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link Css22Parser#media}.
	 * @param ctx the parse tree
	 */
	void enterMedia(Css22Parser.MediaContext ctx);
	/**
	 * Exit a parse tree produced by {@link Css22Parser#media}.
	 * @param ctx the parse tree
	 */
	void exitMedia(Css22Parser.MediaContext ctx);
	/**
	 * Enter a parse tree produced by {@link Css22Parser#mediaList}.
	 * @param ctx the parse tree
	 */
	void enterMediaList(Css22Parser.MediaListContext ctx);
	/**
	 * Exit a parse tree produced by {@link Css22Parser#mediaList}.
	 * @param ctx the parse tree
	 */
	void exitMediaList(Css22Parser.MediaListContext ctx);
	/**
	 * Enter a parse tree produced by {@link Css22Parser#medium}.
	 * @param ctx the parse tree
	 */
	void enterMedium(Css22Parser.MediumContext ctx);
	/**
	 * Exit a parse tree produced by {@link Css22Parser#medium}.
	 * @param ctx the parse tree
	 */
	void exitMedium(Css22Parser.MediumContext ctx);
	/**
	 * Enter a parse tree produced by {@link Css22Parser#page}.
	 * @param ctx the parse tree
	 */
	void enterPage(Css22Parser.PageContext ctx);
	/**
	 * Exit a parse tree produced by {@link Css22Parser#page}.
	 * @param ctx the parse tree
	 */
	void exitPage(Css22Parser.PageContext ctx);
	/**
	 * Enter a parse tree produced by {@link Css22Parser#pseudoPage}.
	 * @param ctx the parse tree
	 */
	void enterPseudoPage(Css22Parser.PseudoPageContext ctx);
	/**
	 * Exit a parse tree produced by {@link Css22Parser#pseudoPage}.
	 * @param ctx the parse tree
	 */
	void exitPseudoPage(Css22Parser.PseudoPageContext ctx);
	/**
	 * Enter a parse tree produced by {@link Css22Parser#operator}.
	 * @param ctx the parse tree
	 */
	void enterOperator(Css22Parser.OperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link Css22Parser#operator}.
	 * @param ctx the parse tree
	 */
	void exitOperator(Css22Parser.OperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link Css22Parser#combinator}.
	 * @param ctx the parse tree
	 */
	void enterCombinator(Css22Parser.CombinatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link Css22Parser#combinator}.
	 * @param ctx the parse tree
	 */
	void exitCombinator(Css22Parser.CombinatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link Css22Parser#property}.
	 * @param ctx the parse tree
	 */
	void enterProperty(Css22Parser.PropertyContext ctx);
	/**
	 * Exit a parse tree produced by {@link Css22Parser#property}.
	 * @param ctx the parse tree
	 */
	void exitProperty(Css22Parser.PropertyContext ctx);
	/**
	 * Enter a parse tree produced by {@link Css22Parser#ruleset}.
	 * @param ctx the parse tree
	 */
	void enterRuleset(Css22Parser.RulesetContext ctx);
	/**
	 * Exit a parse tree produced by {@link Css22Parser#ruleset}.
	 * @param ctx the parse tree
	 */
	void exitRuleset(Css22Parser.RulesetContext ctx);
	/**
	 * Enter a parse tree produced by {@link Css22Parser#declarationBlock}.
	 * @param ctx the parse tree
	 */
	void enterDeclarationBlock(Css22Parser.DeclarationBlockContext ctx);
	/**
	 * Exit a parse tree produced by {@link Css22Parser#declarationBlock}.
	 * @param ctx the parse tree
	 */
	void exitDeclarationBlock(Css22Parser.DeclarationBlockContext ctx);
	/**
	 * Enter a parse tree produced by {@link Css22Parser#selector}.
	 * @param ctx the parse tree
	 */
	void enterSelector(Css22Parser.SelectorContext ctx);
	/**
	 * Exit a parse tree produced by {@link Css22Parser#selector}.
	 * @param ctx the parse tree
	 */
	void exitSelector(Css22Parser.SelectorContext ctx);
	/**
	 * Enter a parse tree produced by {@link Css22Parser#simpleSelector}.
	 * @param ctx the parse tree
	 */
	void enterSimpleSelector(Css22Parser.SimpleSelectorContext ctx);
	/**
	 * Exit a parse tree produced by {@link Css22Parser#simpleSelector}.
	 * @param ctx the parse tree
	 */
	void exitSimpleSelector(Css22Parser.SimpleSelectorContext ctx);
	/**
	 * Enter a parse tree produced by {@link Css22Parser#id}.
	 * @param ctx the parse tree
	 */
	void enterId(Css22Parser.IdContext ctx);
	/**
	 * Exit a parse tree produced by {@link Css22Parser#id}.
	 * @param ctx the parse tree
	 */
	void exitId(Css22Parser.IdContext ctx);
	/**
	 * Enter a parse tree produced by {@link Css22Parser#className}.
	 * @param ctx the parse tree
	 */
	void enterClassName(Css22Parser.ClassNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Css22Parser#className}.
	 * @param ctx the parse tree
	 */
	void exitClassName(Css22Parser.ClassNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Css22Parser#elementName}.
	 * @param ctx the parse tree
	 */
	void enterElementName(Css22Parser.ElementNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Css22Parser#elementName}.
	 * @param ctx the parse tree
	 */
	void exitElementName(Css22Parser.ElementNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Css22Parser#attrib}.
	 * @param ctx the parse tree
	 */
	void enterAttrib(Css22Parser.AttribContext ctx);
	/**
	 * Exit a parse tree produced by {@link Css22Parser#attrib}.
	 * @param ctx the parse tree
	 */
	void exitAttrib(Css22Parser.AttribContext ctx);
	/**
	 * Enter a parse tree produced by {@link Css22Parser#pseudo}.
	 * @param ctx the parse tree
	 */
	void enterPseudo(Css22Parser.PseudoContext ctx);
	/**
	 * Exit a parse tree produced by {@link Css22Parser#pseudo}.
	 * @param ctx the parse tree
	 */
	void exitPseudo(Css22Parser.PseudoContext ctx);
	/**
	 * Enter a parse tree produced by {@link Css22Parser#declaration}.
	 * @param ctx the parse tree
	 */
	void enterDeclaration(Css22Parser.DeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Css22Parser#declaration}.
	 * @param ctx the parse tree
	 */
	void exitDeclaration(Css22Parser.DeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link Css22Parser#prio}.
	 * @param ctx the parse tree
	 */
	void enterPrio(Css22Parser.PrioContext ctx);
	/**
	 * Exit a parse tree produced by {@link Css22Parser#prio}.
	 * @param ctx the parse tree
	 */
	void exitPrio(Css22Parser.PrioContext ctx);
	/**
	 * Enter a parse tree produced by {@link Css22Parser#expr}.
	 * @param ctx the parse tree
	 */
	void enterExpr(Css22Parser.ExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link Css22Parser#expr}.
	 * @param ctx the parse tree
	 */
	void exitExpr(Css22Parser.ExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link Css22Parser#term}.
	 * @param ctx the parse tree
	 */
	void enterTerm(Css22Parser.TermContext ctx);
	/**
	 * Exit a parse tree produced by {@link Css22Parser#term}.
	 * @param ctx the parse tree
	 */
	void exitTerm(Css22Parser.TermContext ctx);
	/**
	 * Enter a parse tree produced by {@link Css22Parser#function}.
	 * @param ctx the parse tree
	 */
	void enterFunction(Css22Parser.FunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Css22Parser#function}.
	 * @param ctx the parse tree
	 */
	void exitFunction(Css22Parser.FunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link Css22Parser#hexcolor}.
	 * @param ctx the parse tree
	 */
	void enterHexcolor(Css22Parser.HexcolorContext ctx);
	/**
	 * Exit a parse tree produced by {@link Css22Parser#hexcolor}.
	 * @param ctx the parse tree
	 */
	void exitHexcolor(Css22Parser.HexcolorContext ctx);
}