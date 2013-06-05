// Generated from Neon.g4 by ANTLR 4.0

package com.ncc.neon.query.parse;

import org.antlr.v4.runtime.tree.*;
import org.antlr.v4.runtime.Token;

public interface NeonListener extends ParseTreeListener {
	void enterStatement(NeonParser.StatementContext ctx);
	void exitStatement(NeonParser.StatementContext ctx);

	void enterWhereClause(NeonParser.WhereClauseContext ctx);
	void exitWhereClause(NeonParser.WhereClauseContext ctx);

	void enterSortBy(NeonParser.SortByContext ctx);
	void exitSortBy(NeonParser.SortByContext ctx);

	void enterQuery(NeonParser.QueryContext ctx);
	void exitQuery(NeonParser.QueryContext ctx);

	void enterWhere(NeonParser.WhereContext ctx);
	void exitWhere(NeonParser.WhereContext ctx);

	void enterOperator(NeonParser.OperatorContext ctx);
	void exitOperator(NeonParser.OperatorContext ctx);

	void enterDatabase(NeonParser.DatabaseContext ctx);
	void exitDatabase(NeonParser.DatabaseContext ctx);

	void enterOptions(NeonParser.OptionsContext ctx);
	void exitOptions(NeonParser.OptionsContext ctx);

	void enterSimpleWhereClause(NeonParser.SimpleWhereClauseContext ctx);
	void exitSimpleWhereClause(NeonParser.SimpleWhereClauseContext ctx);
}