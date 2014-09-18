// Generated from Neon.g4 by ANTLR 4.2
package com.ncc.neon.language;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link NeonParser}.
 */
public interface NeonListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link NeonParser#selectFields}.
	 * @param ctx the parse tree
	 */
	void enterSelectFields(@NotNull NeonParser.SelectFieldsContext ctx);
	/**
	 * Exit a parse tree produced by {@link NeonParser#selectFields}.
	 * @param ctx the parse tree
	 */
	void exitSelectFields(@NotNull NeonParser.SelectFieldsContext ctx);

	/**
	 * Enter a parse tree produced by {@link NeonParser#whereClause}.
	 * @param ctx the parse tree
	 */
	void enterWhereClause(@NotNull NeonParser.WhereClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link NeonParser#whereClause}.
	 * @param ctx the parse tree
	 */
	void exitWhereClause(@NotNull NeonParser.WhereClauseContext ctx);

	/**
	 * Enter a parse tree produced by {@link NeonParser#select}.
	 * @param ctx the parse tree
	 */
	void enterSelect(@NotNull NeonParser.SelectContext ctx);
	/**
	 * Exit a parse tree produced by {@link NeonParser#select}.
	 * @param ctx the parse tree
	 */
	void exitSelect(@NotNull NeonParser.SelectContext ctx);

	/**
	 * Enter a parse tree produced by {@link NeonParser#offset}.
	 * @param ctx the parse tree
	 */
	void enterOffset(@NotNull NeonParser.OffsetContext ctx);
	/**
	 * Exit a parse tree produced by {@link NeonParser#offset}.
	 * @param ctx the parse tree
	 */
	void exitOffset(@NotNull NeonParser.OffsetContext ctx);

	/**
	 * Enter a parse tree produced by {@link NeonParser#functionName}.
	 * @param ctx the parse tree
	 */
	void enterFunctionName(@NotNull NeonParser.FunctionNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link NeonParser#functionName}.
	 * @param ctx the parse tree
	 */
	void exitFunctionName(@NotNull NeonParser.FunctionNameContext ctx);

	/**
	 * Enter a parse tree produced by {@link NeonParser#query}.
	 * @param ctx the parse tree
	 */
	void enterQuery(@NotNull NeonParser.QueryContext ctx);
	/**
	 * Exit a parse tree produced by {@link NeonParser#query}.
	 * @param ctx the parse tree
	 */
	void exitQuery(@NotNull NeonParser.QueryContext ctx);

	/**
	 * Enter a parse tree produced by {@link NeonParser#count}.
	 * @param ctx the parse tree
	 */
	void enterCount(@NotNull NeonParser.CountContext ctx);
	/**
	 * Exit a parse tree produced by {@link NeonParser#count}.
	 * @param ctx the parse tree
	 */
	void exitCount(@NotNull NeonParser.CountContext ctx);

	/**
	 * Enter a parse tree produced by {@link NeonParser#sort}.
	 * @param ctx the parse tree
	 */
	void enterSort(@NotNull NeonParser.SortContext ctx);
	/**
	 * Exit a parse tree produced by {@link NeonParser#sort}.
	 * @param ctx the parse tree
	 */
	void exitSort(@NotNull NeonParser.SortContext ctx);

	/**
	 * Enter a parse tree produced by {@link NeonParser#operator}.
	 * @param ctx the parse tree
	 */
	void enterOperator(@NotNull NeonParser.OperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link NeonParser#operator}.
	 * @param ctx the parse tree
	 */
	void exitOperator(@NotNull NeonParser.OperatorContext ctx);

	/**
	 * Enter a parse tree produced by {@link NeonParser#database}.
	 * @param ctx the parse tree
	 */
	void enterDatabase(@NotNull NeonParser.DatabaseContext ctx);
	/**
	 * Exit a parse tree produced by {@link NeonParser#database}.
	 * @param ctx the parse tree
	 */
	void exitDatabase(@NotNull NeonParser.DatabaseContext ctx);

	/**
	 * Enter a parse tree produced by {@link NeonParser#groupClause}.
	 * @param ctx the parse tree
	 */
	void enterGroupClause(@NotNull NeonParser.GroupClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link NeonParser#groupClause}.
	 * @param ctx the parse tree
	 */
	void exitGroupClause(@NotNull NeonParser.GroupClauseContext ctx);

	/**
	 * Enter a parse tree produced by {@link NeonParser#simpleWhereClause}.
	 * @param ctx the parse tree
	 */
	void enterSimpleWhereClause(@NotNull NeonParser.SimpleWhereClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link NeonParser#simpleWhereClause}.
	 * @param ctx the parse tree
	 */
	void exitSimpleWhereClause(@NotNull NeonParser.SimpleWhereClauseContext ctx);

	/**
	 * Enter a parse tree produced by {@link NeonParser#function}.
	 * @param ctx the parse tree
	 */
	void enterFunction(@NotNull NeonParser.FunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link NeonParser#function}.
	 * @param ctx the parse tree
	 */
	void exitFunction(@NotNull NeonParser.FunctionContext ctx);

	/**
	 * Enter a parse tree produced by {@link NeonParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStatement(@NotNull NeonParser.StatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link NeonParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStatement(@NotNull NeonParser.StatementContext ctx);

	/**
	 * Enter a parse tree produced by {@link NeonParser#limit}.
	 * @param ctx the parse tree
	 */
	void enterLimit(@NotNull NeonParser.LimitContext ctx);
	/**
	 * Exit a parse tree produced by {@link NeonParser#limit}.
	 * @param ctx the parse tree
	 */
	void exitLimit(@NotNull NeonParser.LimitContext ctx);

	/**
	 * Enter a parse tree produced by {@link NeonParser#where}.
	 * @param ctx the parse tree
	 */
	void enterWhere(@NotNull NeonParser.WhereContext ctx);
	/**
	 * Exit a parse tree produced by {@link NeonParser#where}.
	 * @param ctx the parse tree
	 */
	void exitWhere(@NotNull NeonParser.WhereContext ctx);

	/**
	 * Enter a parse tree produced by {@link NeonParser#sortClause}.
	 * @param ctx the parse tree
	 */
	void enterSortClause(@NotNull NeonParser.SortClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link NeonParser#sortClause}.
	 * @param ctx the parse tree
	 */
	void exitSortClause(@NotNull NeonParser.SortClauseContext ctx);

	/**
	 * Enter a parse tree produced by {@link NeonParser#fieldList}.
	 * @param ctx the parse tree
	 */
	void enterFieldList(@NotNull NeonParser.FieldListContext ctx);
	/**
	 * Exit a parse tree produced by {@link NeonParser#fieldList}.
	 * @param ctx the parse tree
	 */
	void exitFieldList(@NotNull NeonParser.FieldListContext ctx);

	/**
	 * Enter a parse tree produced by {@link NeonParser#additionalClauses}.
	 * @param ctx the parse tree
	 */
	void enterAdditionalClauses(@NotNull NeonParser.AdditionalClausesContext ctx);
	/**
	 * Exit a parse tree produced by {@link NeonParser#additionalClauses}.
	 * @param ctx the parse tree
	 */
	void exitAdditionalClauses(@NotNull NeonParser.AdditionalClausesContext ctx);

	/**
	 * Enter a parse tree produced by {@link NeonParser#group}.
	 * @param ctx the parse tree
	 */
	void enterGroup(@NotNull NeonParser.GroupContext ctx);
	/**
	 * Exit a parse tree produced by {@link NeonParser#group}.
	 * @param ctx the parse tree
	 */
	void exitGroup(@NotNull NeonParser.GroupContext ctx);
}