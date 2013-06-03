// Generated from Neon.g4 by ANTLR 4.0

package com.ncc.neon.query.parse;


import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.ErrorNode;

public class NeonBaseListener implements NeonListener {
	@Override public void enterStatement(NeonParser.StatementContext ctx) { }
	@Override public void exitStatement(NeonParser.StatementContext ctx) { }

	@Override public void enterWhereClause(NeonParser.WhereClauseContext ctx) { }
	@Override public void exitWhereClause(NeonParser.WhereClauseContext ctx) { }

	@Override public void enterParentheticalWhereClause(NeonParser.ParentheticalWhereClauseContext ctx) { }
	@Override public void exitParentheticalWhereClause(NeonParser.ParentheticalWhereClauseContext ctx) { }

	@Override public void enterSortBy(NeonParser.SortByContext ctx) { }
	@Override public void exitSortBy(NeonParser.SortByContext ctx) { }

	@Override public void enterQuery(NeonParser.QueryContext ctx) { }
	@Override public void exitQuery(NeonParser.QueryContext ctx) { }

	@Override public void enterBooleanOperator(NeonParser.BooleanOperatorContext ctx) { }
	@Override public void exitBooleanOperator(NeonParser.BooleanOperatorContext ctx) { }

	@Override public void enterWhere(NeonParser.WhereContext ctx) { }
	@Override public void exitWhere(NeonParser.WhereContext ctx) { }

	@Override public void enterOperator(NeonParser.OperatorContext ctx) { }
	@Override public void exitOperator(NeonParser.OperatorContext ctx) { }

	@Override public void enterDatabase(NeonParser.DatabaseContext ctx) { }
	@Override public void exitDatabase(NeonParser.DatabaseContext ctx) { }

	@Override public void enterOptions(NeonParser.OptionsContext ctx) { }
	@Override public void exitOptions(NeonParser.OptionsContext ctx) { }

	@Override public void enterSimpleWhereClause(NeonParser.SimpleWhereClauseContext ctx) { }
	@Override public void exitSimpleWhereClause(NeonParser.SimpleWhereClauseContext ctx) { }

	@Override public void enterEveryRule(ParserRuleContext ctx) { }
	@Override public void exitEveryRule(ParserRuleContext ctx) { }
	@Override public void visitTerminal(TerminalNode node) { }
	@Override public void visitErrorNode(ErrorNode node) { }
}