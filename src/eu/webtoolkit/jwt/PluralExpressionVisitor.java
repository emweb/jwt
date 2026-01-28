// Generated from PluralExpression.g4 by ANTLR 4.7.2
package eu.webtoolkit.jwt;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link PluralExpressionParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface PluralExpressionVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link PluralExpressionParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement(PluralExpressionParser.StatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link PluralExpressionParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(PluralExpressionParser.ExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link PluralExpressionParser#orExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrExpression(PluralExpressionParser.OrExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link PluralExpressionParser#andExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAndExpression(PluralExpressionParser.AndExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link PluralExpressionParser#eqExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEqExpression(PluralExpressionParser.EqExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link PluralExpressionParser#relationalExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelationalExpression(PluralExpressionParser.RelationalExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link PluralExpressionParser#additiveExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAdditiveExpression(PluralExpressionParser.AdditiveExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link PluralExpressionParser#term}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTerm(PluralExpressionParser.TermContext ctx);
	/**
	 * Visit a parse tree produced by {@link PluralExpressionParser#factor}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFactor(PluralExpressionParser.FactorContext ctx);
	/**
	 * Visit a parse tree produced by {@link PluralExpressionParser#literal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLiteral(PluralExpressionParser.LiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link PluralExpressionParser#group}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroup(PluralExpressionParser.GroupContext ctx);
	/**
	 * Visit a parse tree produced by {@link PluralExpressionParser#variable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariable(PluralExpressionParser.VariableContext ctx);
	/**
	 * Visit a parse tree produced by {@link PluralExpressionParser#eqOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEqOperator(PluralExpressionParser.EqOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link PluralExpressionParser#cmpOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCmpOperator(PluralExpressionParser.CmpOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link PluralExpressionParser#sumOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSumOperator(PluralExpressionParser.SumOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link PluralExpressionParser#prodOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProdOperator(PluralExpressionParser.ProdOperatorContext ctx);
}