// Generated from Neon.g4 by ANTLR 4.0

package com.ncc.neon.query.parse;

import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class NeonParser extends Parser {
	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__4=1, T__3=2, T__2=3, T__1=4, T__0=5, GT=6, GTE=7, LT=8, LTE=9, EQ=10, 
		NE=11, LPAREN=12, RPAREN=13, USE=14, SELECT=15, FROM=16, WHERE=17, SORT_BY=18, 
		SORT_DIRECTION=19, STRING=20, WHITESPACE=21;
	public static final String[] tokenNames = {
		"<INVALID>", "'AND'", "'and'", "'OR'", "'or'", "';'", "'>'", "'>='", "'<'", 
		"'<='", "'='", "'!='", "'('", "')'", "USE", "SELECT", "FROM", "WHERE", 
		"SORT_BY", "SORT_DIRECTION", "STRING", "WHITESPACE"
	};
	public static final int
		RULE_statement = 0, RULE_database = 1, RULE_query = 2, RULE_where = 3, 
		RULE_whereClause = 4, RULE_simpleWhereClause = 5, RULE_parentheticalWhereClause = 6, 
		RULE_operator = 7, RULE_booleanOperator = 8, RULE_options = 9, RULE_sortBy = 10;
	public static final String[] ruleNames = {
		"statement", "database", "query", "where", "whereClause", "simpleWhereClause", 
		"parentheticalWhereClause", "operator", "booleanOperator", "options", 
		"sortBy"
	};

	@Override
	public String getGrammarFileName() { return "Neon.g4"; }

	@Override
	public String[] getTokenNames() { return tokenNames; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public NeonParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class StatementContext extends ParserRuleContext {
		public DatabaseContext database(int i) {
			return getRuleContext(DatabaseContext.class,i);
		}
		public List<QueryContext> query() {
			return getRuleContexts(QueryContext.class);
		}
		public QueryContext query(int i) {
			return getRuleContext(QueryContext.class,i);
		}
		public List<DatabaseContext> database() {
			return getRuleContexts(DatabaseContext.class);
		}
		public StatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NeonListener ) ((NeonListener)listener).enterStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NeonListener ) ((NeonListener)listener).exitStatement(this);
		}
	}

	public final StatementContext statement() throws RecognitionException {
		StatementContext _localctx = new StatementContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_statement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(28); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(24);
				switch (_input.LA(1)) {
				case SELECT:
					{
					setState(22); query();
					}
					break;
				case USE:
					{
					setState(23); database();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(26); match(5);
				}
				}
				setState(30); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==USE || _la==SELECT );
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

	public static class DatabaseContext extends ParserRuleContext {
		public TerminalNode USE() { return getToken(NeonParser.USE, 0); }
		public TerminalNode STRING() { return getToken(NeonParser.STRING, 0); }
		public DatabaseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_database; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NeonListener ) ((NeonListener)listener).enterDatabase(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NeonListener ) ((NeonListener)listener).exitDatabase(this);
		}
	}

	public final DatabaseContext database() throws RecognitionException {
		DatabaseContext _localctx = new DatabaseContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_database);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(32); match(USE);
			setState(33); match(STRING);
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

	public static class QueryContext extends ParserRuleContext {
		public TerminalNode FROM() { return getToken(NeonParser.FROM, 0); }
		public TerminalNode SELECT() { return getToken(NeonParser.SELECT, 0); }
		public OptionsContext options(int i) {
			return getRuleContext(OptionsContext.class,i);
		}
		public WhereContext where() {
			return getRuleContext(WhereContext.class,0);
		}
		public TerminalNode STRING() { return getToken(NeonParser.STRING, 0); }
		public List<OptionsContext> options() {
			return getRuleContexts(OptionsContext.class);
		}
		public QueryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_query; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NeonListener ) ((NeonListener)listener).enterQuery(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NeonListener ) ((NeonListener)listener).exitQuery(this);
		}
	}

	public final QueryContext query() throws RecognitionException {
		QueryContext _localctx = new QueryContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_query);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(35); match(SELECT);
			setState(36); match(FROM);
			setState(37); match(STRING);
			setState(39);
			_la = _input.LA(1);
			if (_la==WHERE) {
				{
				setState(38); where();
				}
			}

			setState(44);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==SORT_BY) {
				{
				{
				setState(41); options();
				}
				}
				setState(46);
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

	public static class WhereContext extends ParserRuleContext {
		public WhereClauseContext whereClause() {
			return getRuleContext(WhereClauseContext.class,0);
		}
		public TerminalNode WHERE() { return getToken(NeonParser.WHERE, 0); }
		public WhereContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_where; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NeonListener ) ((NeonListener)listener).enterWhere(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NeonListener ) ((NeonListener)listener).exitWhere(this);
		}
	}

	public final WhereContext where() throws RecognitionException {
		WhereContext _localctx = new WhereContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_where);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(47); match(WHERE);
			setState(48); whereClause();
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

	public static class WhereClauseContext extends ParserRuleContext {
		public ParentheticalWhereClauseContext parentheticalWhereClause() {
			return getRuleContext(ParentheticalWhereClauseContext.class,0);
		}
		public SimpleWhereClauseContext simpleWhereClause(int i) {
			return getRuleContext(SimpleWhereClauseContext.class,i);
		}
		public BooleanOperatorContext booleanOperator() {
			return getRuleContext(BooleanOperatorContext.class,0);
		}
		public List<SimpleWhereClauseContext> simpleWhereClause() {
			return getRuleContexts(SimpleWhereClauseContext.class);
		}
		public WhereClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_whereClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NeonListener ) ((NeonListener)listener).enterWhereClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NeonListener ) ((NeonListener)listener).exitWhereClause(this);
		}
	}

	public final WhereClauseContext whereClause() throws RecognitionException {
		WhereClauseContext _localctx = new WhereClauseContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_whereClause);
		try {
			setState(56);
			switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(50); simpleWhereClause();
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(51); parentheticalWhereClause();
				}
				break;

			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(52); simpleWhereClause();
				setState(53); booleanOperator();
				setState(54); simpleWhereClause();
				}
				break;
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

	public static class SimpleWhereClauseContext extends ParserRuleContext {
		public TerminalNode STRING(int i) {
			return getToken(NeonParser.STRING, i);
		}
		public OperatorContext operator() {
			return getRuleContext(OperatorContext.class,0);
		}
		public List<TerminalNode> STRING() { return getTokens(NeonParser.STRING); }
		public SimpleWhereClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_simpleWhereClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NeonListener ) ((NeonListener)listener).enterSimpleWhereClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NeonListener ) ((NeonListener)listener).exitSimpleWhereClause(this);
		}
	}

	public final SimpleWhereClauseContext simpleWhereClause() throws RecognitionException {
		SimpleWhereClauseContext _localctx = new SimpleWhereClauseContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_simpleWhereClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(58); match(STRING);
			setState(59); operator();
			setState(60); match(STRING);
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

	public static class ParentheticalWhereClauseContext extends ParserRuleContext {
		public WhereClauseContext whereClause() {
			return getRuleContext(WhereClauseContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(NeonParser.RPAREN, 0); }
		public TerminalNode LPAREN() { return getToken(NeonParser.LPAREN, 0); }
		public ParentheticalWhereClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_parentheticalWhereClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NeonListener ) ((NeonListener)listener).enterParentheticalWhereClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NeonListener ) ((NeonListener)listener).exitParentheticalWhereClause(this);
		}
	}

	public final ParentheticalWhereClauseContext parentheticalWhereClause() throws RecognitionException {
		ParentheticalWhereClauseContext _localctx = new ParentheticalWhereClauseContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_parentheticalWhereClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(62); match(LPAREN);
			setState(63); whereClause();
			setState(64); match(RPAREN);
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
		public TerminalNode GT() { return getToken(NeonParser.GT, 0); }
		public TerminalNode LT() { return getToken(NeonParser.LT, 0); }
		public TerminalNode EQ() { return getToken(NeonParser.EQ, 0); }
		public TerminalNode LTE() { return getToken(NeonParser.LTE, 0); }
		public TerminalNode GTE() { return getToken(NeonParser.GTE, 0); }
		public TerminalNode NE() { return getToken(NeonParser.NE, 0); }
		public OperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_operator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NeonListener ) ((NeonListener)listener).enterOperator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NeonListener ) ((NeonListener)listener).exitOperator(this);
		}
	}

	public final OperatorContext operator() throws RecognitionException {
		OperatorContext _localctx = new OperatorContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_operator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(66);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << GT) | (1L << GTE) | (1L << LT) | (1L << LTE) | (1L << EQ) | (1L << NE))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			consume();
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

	public static class BooleanOperatorContext extends ParserRuleContext {
		public BooleanOperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_booleanOperator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NeonListener ) ((NeonListener)listener).enterBooleanOperator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NeonListener ) ((NeonListener)listener).exitBooleanOperator(this);
		}
	}

	public final BooleanOperatorContext booleanOperator() throws RecognitionException {
		BooleanOperatorContext _localctx = new BooleanOperatorContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_booleanOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(68);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << 1) | (1L << 2) | (1L << 3) | (1L << 4))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			consume();
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

	public static class OptionsContext extends ParserRuleContext {
		public SortByContext sortBy() {
			return getRuleContext(SortByContext.class,0);
		}
		public OptionsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_options; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NeonListener ) ((NeonListener)listener).enterOptions(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NeonListener ) ((NeonListener)listener).exitOptions(this);
		}
	}

	public final OptionsContext options() throws RecognitionException {
		OptionsContext _localctx = new OptionsContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_options);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(70); sortBy();
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

	public static class SortByContext extends ParserRuleContext {
		public TerminalNode SORT_DIRECTION() { return getToken(NeonParser.SORT_DIRECTION, 0); }
		public TerminalNode STRING() { return getToken(NeonParser.STRING, 0); }
		public TerminalNode SORT_BY() { return getToken(NeonParser.SORT_BY, 0); }
		public SortByContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sortBy; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NeonListener ) ((NeonListener)listener).enterSortBy(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NeonListener ) ((NeonListener)listener).exitSortBy(this);
		}
	}

	public final SortByContext sortBy() throws RecognitionException {
		SortByContext _localctx = new SortByContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_sortBy);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(72); match(SORT_BY);
			setState(73); match(STRING);
			setState(74); match(SORT_DIRECTION);
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
		"\2\3\27O\4\2\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t"+
		"\t\4\n\t\n\4\13\t\13\4\f\t\f\3\2\3\2\5\2\33\n\2\3\2\3\2\6\2\37\n\2\r\2"+
		"\16\2 \3\3\3\3\3\3\3\4\3\4\3\4\3\4\5\4*\n\4\3\4\7\4-\n\4\f\4\16\4\60\13"+
		"\4\3\5\3\5\3\5\3\6\3\6\3\6\3\6\3\6\3\6\5\6;\n\6\3\7\3\7\3\7\3\7\3\b\3"+
		"\b\3\b\3\b\3\t\3\t\3\n\3\n\3\13\3\13\3\f\3\f\3\f\3\f\3\f\2\r\2\4\6\b\n"+
		"\f\16\20\22\24\26\2\4\3\b\r\3\3\6I\2\36\3\2\2\2\4\"\3\2\2\2\6%\3\2\2\2"+
		"\b\61\3\2\2\2\n:\3\2\2\2\f<\3\2\2\2\16@\3\2\2\2\20D\3\2\2\2\22F\3\2\2"+
		"\2\24H\3\2\2\2\26J\3\2\2\2\30\33\5\6\4\2\31\33\5\4\3\2\32\30\3\2\2\2\32"+
		"\31\3\2\2\2\33\34\3\2\2\2\34\35\7\7\2\2\35\37\3\2\2\2\36\32\3\2\2\2\37"+
		" \3\2\2\2 \36\3\2\2\2 !\3\2\2\2!\3\3\2\2\2\"#\7\20\2\2#$\7\26\2\2$\5\3"+
		"\2\2\2%&\7\21\2\2&\'\7\22\2\2\')\7\26\2\2(*\5\b\5\2)(\3\2\2\2)*\3\2\2"+
		"\2*.\3\2\2\2+-\5\24\13\2,+\3\2\2\2-\60\3\2\2\2.,\3\2\2\2./\3\2\2\2/\7"+
		"\3\2\2\2\60.\3\2\2\2\61\62\7\23\2\2\62\63\5\n\6\2\63\t\3\2\2\2\64;\5\f"+
		"\7\2\65;\5\16\b\2\66\67\5\f\7\2\678\5\22\n\289\5\f\7\29;\3\2\2\2:\64\3"+
		"\2\2\2:\65\3\2\2\2:\66\3\2\2\2;\13\3\2\2\2<=\7\26\2\2=>\5\20\t\2>?\7\26"+
		"\2\2?\r\3\2\2\2@A\7\16\2\2AB\5\n\6\2BC\7\17\2\2C\17\3\2\2\2DE\t\2\2\2"+
		"E\21\3\2\2\2FG\t\3\2\2G\23\3\2\2\2HI\5\26\f\2I\25\3\2\2\2JK\7\24\2\2K"+
		"L\7\26\2\2LM\7\25\2\2M\27\3\2\2\2\7\32 ).:";
	public static final ATN _ATN =
		ATNSimulator.deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
	}
}