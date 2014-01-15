// Generated from Neon.g4 by ANTLR 4.0
package com.ncc.neon.language;
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
		T__10=1, T__9=2, T__8=3, T__7=4, T__6=5, T__5=6, T__4=7, T__3=8, T__2=9, 
		T__1=10, T__0=11, AND=12, OR=13, ALL_FIELDS=14, GT=15, GTE=16, LT=17, 
		LTE=18, EQ=19, NE=20, USE=21, SELECT=22, FROM=23, WHERE=24, GROUP=25, 
		LIMIT=26, OFFSET=27, SORT=28, SORT_DIRECTION=29, WHOLE_NUMBER=30, NUMBER=31, 
		STRING=32, WHITESPACE=33;
	public static final String[] tokenNames = {
		"<INVALID>", "'max'", "'avg'", "'last'", "'sum'", "')'", "','", "'min'", 
		"'('", "'count'", "';'", "'first'", "AND", "OR", "'*'", "'>'", "'>='", 
		"'<'", "'<='", "'='", "'!='", "USE", "SELECT", "FROM", "WHERE", "GROUP", 
		"LIMIT", "OFFSET", "SORT", "SORT_DIRECTION", "WHOLE_NUMBER", "NUMBER", 
		"STRING", "WHITESPACE"
	};
	public static final int
		RULE_statement = 0, RULE_database = 1, RULE_query = 2, RULE_select = 3, 
		RULE_selectFields = 4, RULE_fieldList = 5, RULE_where = 6, RULE_whereClause = 7, 
		RULE_simpleWhereClause = 8, RULE_additionalClauses = 9, RULE_operator = 10, 
		RULE_sort = 11, RULE_sortClause = 12, RULE_group = 13, RULE_groupClause = 14, 
		RULE_function = 15, RULE_functionName = 16, RULE_count = 17, RULE_limit = 18, 
		RULE_offset = 19;
	public static final String[] ruleNames = {
		"statement", "database", "query", "select", "selectFields", "fieldList", 
		"where", "whereClause", "simpleWhereClause", "additionalClauses", "operator", 
		"sort", "sortClause", "group", "groupClause", "function", "functionName", 
		"count", "limit", "offset"
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
		public QueryContext query() {
			return getRuleContext(QueryContext.class,0);
		}
		public DatabaseContext database() {
			return getRuleContext(DatabaseContext.class,0);
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
			setState(40); database();
			setState(42);
			_la = _input.LA(1);
			if (_la==10) {
				{
				setState(41); match(10);
				}
			}

			setState(44); query();
			setState(46);
			_la = _input.LA(1);
			if (_la==10) {
				{
				setState(45); match(10);
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
			setState(48); match(USE);
			setState(49); match(STRING);
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
		public SelectContext select() {
			return getRuleContext(SelectContext.class,0);
		}
		public AdditionalClausesContext additionalClauses(int i) {
			return getRuleContext(AdditionalClausesContext.class,i);
		}
		public TerminalNode FROM() { return getToken(NeonParser.FROM, 0); }
		public WhereContext where() {
			return getRuleContext(WhereContext.class,0);
		}
		public List<AdditionalClausesContext> additionalClauses() {
			return getRuleContexts(AdditionalClausesContext.class);
		}
		public TerminalNode STRING() { return getToken(NeonParser.STRING, 0); }
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
			setState(51); select();
			setState(52); match(FROM);
			setState(53); match(STRING);
			setState(55);
			_la = _input.LA(1);
			if (_la==WHERE) {
				{
				setState(54); where();
				}
			}

			setState(60);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << GROUP) | (1L << LIMIT) | (1L << OFFSET) | (1L << SORT))) != 0)) {
				{
				{
				setState(57); additionalClauses();
				}
				}
				setState(62);
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

	public static class SelectContext extends ParserRuleContext {
		public TerminalNode SELECT() { return getToken(NeonParser.SELECT, 0); }
		public SelectFieldsContext selectFields() {
			return getRuleContext(SelectFieldsContext.class,0);
		}
		public SelectContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_select; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NeonListener ) ((NeonListener)listener).enterSelect(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NeonListener ) ((NeonListener)listener).exitSelect(this);
		}
	}

	public final SelectContext select() throws RecognitionException {
		SelectContext _localctx = new SelectContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_select);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(63); match(SELECT);
			setState(64); selectFields();
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

	public static class SelectFieldsContext extends ParserRuleContext {
		public TerminalNode ALL_FIELDS() { return getToken(NeonParser.ALL_FIELDS, 0); }
		public FieldListContext fieldList() {
			return getRuleContext(FieldListContext.class,0);
		}
		public SelectFieldsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_selectFields; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NeonListener ) ((NeonListener)listener).enterSelectFields(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NeonListener ) ((NeonListener)listener).exitSelectFields(this);
		}
	}

	public final SelectFieldsContext selectFields() throws RecognitionException {
		SelectFieldsContext _localctx = new SelectFieldsContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_selectFields);
		try {
			setState(68);
			switch (_input.LA(1)) {
			case ALL_FIELDS:
				enterOuterAlt(_localctx, 1);
				{
				setState(66); match(ALL_FIELDS);
				}
				break;
			case STRING:
				enterOuterAlt(_localctx, 2);
				{
				setState(67); fieldList();
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

	public static class FieldListContext extends ParserRuleContext {
		public TerminalNode STRING(int i) {
			return getToken(NeonParser.STRING, i);
		}
		public List<TerminalNode> STRING() { return getTokens(NeonParser.STRING); }
		public FieldListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fieldList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NeonListener ) ((NeonListener)listener).enterFieldList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NeonListener ) ((NeonListener)listener).exitFieldList(this);
		}
	}

	public final FieldListContext fieldList() throws RecognitionException {
		FieldListContext _localctx = new FieldListContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_fieldList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(70); match(STRING);
			setState(75);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==6) {
				{
				{
				setState(71); match(6);
				setState(72); match(STRING);
				}
				}
				setState(77);
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
		enterRule(_localctx, 12, RULE_where);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(78); match(WHERE);
			setState(79); whereClause(0);
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
		public int _p;
		public List<WhereClauseContext> whereClause() {
			return getRuleContexts(WhereClauseContext.class);
		}
		public TerminalNode AND() { return getToken(NeonParser.AND, 0); }
		public TerminalNode OR() { return getToken(NeonParser.OR, 0); }
		public SimpleWhereClauseContext simpleWhereClause() {
			return getRuleContext(SimpleWhereClauseContext.class,0);
		}
		public WhereClauseContext whereClause(int i) {
			return getRuleContext(WhereClauseContext.class,i);
		}
		public WhereClauseContext(ParserRuleContext parent, int invokingState) { super(parent, invokingState); }
		public WhereClauseContext(ParserRuleContext parent, int invokingState, int _p) {
			super(parent, invokingState);
			this._p = _p;
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

	public final WhereClauseContext whereClause(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		WhereClauseContext _localctx = new WhereClauseContext(_ctx, _parentState, _p);
		WhereClauseContext _prevctx = _localctx;
		int _startState = 14;
		enterRecursionRule(_localctx, RULE_whereClause);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(87);
			switch (_input.LA(1)) {
			case 8:
				{
				setState(82); match(8);
				setState(83); whereClause(0);
				setState(84); match(5);
				}
				break;
			case STRING:
				{
				setState(86); simpleWhereClause();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			_ctx.stop = _input.LT(-1);
			setState(97);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,8,_ctx);
			while ( _alt!=2 && _alt!=-1 ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(95);
					switch ( getInterpreter().adaptivePredict(_input,7,_ctx) ) {
					case 1:
						{
						_localctx = new WhereClauseContext(_parentctx, _parentState, _p);
						pushNewRecursionContext(_localctx, _startState, RULE_whereClause);
						setState(89);
						if (!(3 >= _localctx._p)) throw new FailedPredicateException(this, "3 >= $_p");
						setState(90); match(AND);
						setState(91); whereClause(4);
						}
						break;

					case 2:
						{
						_localctx = new WhereClauseContext(_parentctx, _parentState, _p);
						pushNewRecursionContext(_localctx, _startState, RULE_whereClause);
						setState(92);
						if (!(2 >= _localctx._p)) throw new FailedPredicateException(this, "2 >= $_p");
						setState(93); match(OR);
						setState(94); whereClause(3);
						}
						break;
					}
					} 
				}
				setState(99);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,8,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	public static class SimpleWhereClauseContext extends ParserRuleContext {
		public TerminalNode WHOLE_NUMBER() { return getToken(NeonParser.WHOLE_NUMBER, 0); }
		public TerminalNode STRING(int i) {
			return getToken(NeonParser.STRING, i);
		}
		public OperatorContext operator() {
			return getRuleContext(OperatorContext.class,0);
		}
		public List<TerminalNode> STRING() { return getTokens(NeonParser.STRING); }
		public TerminalNode NUMBER() { return getToken(NeonParser.NUMBER, 0); }
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
		enterRule(_localctx, 16, RULE_simpleWhereClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(100); match(STRING);
			setState(101); operator();
			setState(102);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << WHOLE_NUMBER) | (1L << NUMBER) | (1L << STRING))) != 0)) ) {
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

	public static class AdditionalClausesContext extends ParserRuleContext {
		public LimitContext limit() {
			return getRuleContext(LimitContext.class,0);
		}
		public SortContext sort() {
			return getRuleContext(SortContext.class,0);
		}
		public OffsetContext offset() {
			return getRuleContext(OffsetContext.class,0);
		}
		public GroupContext group() {
			return getRuleContext(GroupContext.class,0);
		}
		public AdditionalClausesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_additionalClauses; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NeonListener ) ((NeonListener)listener).enterAdditionalClauses(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NeonListener ) ((NeonListener)listener).exitAdditionalClauses(this);
		}
	}

	public final AdditionalClausesContext additionalClauses() throws RecognitionException {
		AdditionalClausesContext _localctx = new AdditionalClausesContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_additionalClauses);
		try {
			setState(108);
			switch (_input.LA(1)) {
			case SORT:
				enterOuterAlt(_localctx, 1);
				{
				setState(104); sort();
				}
				break;
			case GROUP:
				enterOuterAlt(_localctx, 2);
				{
				setState(105); group();
				}
				break;
			case LIMIT:
				enterOuterAlt(_localctx, 3);
				{
				setState(106); limit();
				}
				break;
			case OFFSET:
				enterOuterAlt(_localctx, 4);
				{
				setState(107); offset();
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
		enterRule(_localctx, 20, RULE_operator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(110);
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

	public static class SortContext extends ParserRuleContext {
		public TerminalNode SORT() { return getToken(NeonParser.SORT, 0); }
		public SortClauseContext sortClause(int i) {
			return getRuleContext(SortClauseContext.class,i);
		}
		public List<SortClauseContext> sortClause() {
			return getRuleContexts(SortClauseContext.class);
		}
		public SortContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sort; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NeonListener ) ((NeonListener)listener).enterSort(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NeonListener ) ((NeonListener)listener).exitSort(this);
		}
	}

	public final SortContext sort() throws RecognitionException {
		SortContext _localctx = new SortContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_sort);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(112); match(SORT);
			setState(113); sortClause();
			setState(118);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==6) {
				{
				{
				setState(114); match(6);
				setState(115); sortClause();
				}
				}
				setState(120);
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

	public static class SortClauseContext extends ParserRuleContext {
		public TerminalNode SORT_DIRECTION() { return getToken(NeonParser.SORT_DIRECTION, 0); }
		public TerminalNode STRING() { return getToken(NeonParser.STRING, 0); }
		public SortClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sortClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NeonListener ) ((NeonListener)listener).enterSortClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NeonListener ) ((NeonListener)listener).exitSortClause(this);
		}
	}

	public final SortClauseContext sortClause() throws RecognitionException {
		SortClauseContext _localctx = new SortClauseContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_sortClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(121); match(STRING);
			setState(123);
			_la = _input.LA(1);
			if (_la==SORT_DIRECTION) {
				{
				setState(122); match(SORT_DIRECTION);
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

	public static class GroupContext extends ParserRuleContext {
		public TerminalNode GROUP() { return getToken(NeonParser.GROUP, 0); }
		public GroupClauseContext groupClause(int i) {
			return getRuleContext(GroupClauseContext.class,i);
		}
		public List<GroupClauseContext> groupClause() {
			return getRuleContexts(GroupClauseContext.class);
		}
		public GroupContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_group; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NeonListener ) ((NeonListener)listener).enterGroup(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NeonListener ) ((NeonListener)listener).exitGroup(this);
		}
	}

	public final GroupContext group() throws RecognitionException {
		GroupContext _localctx = new GroupContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_group);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(125); match(GROUP);
			setState(126); groupClause();
			setState(131);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==6) {
				{
				{
				setState(127); match(6);
				setState(128); groupClause();
				}
				}
				setState(133);
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

	public static class GroupClauseContext extends ParserRuleContext {
		public TerminalNode STRING() { return getToken(NeonParser.STRING, 0); }
		public FunctionContext function() {
			return getRuleContext(FunctionContext.class,0);
		}
		public GroupClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_groupClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NeonListener ) ((NeonListener)listener).enterGroupClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NeonListener ) ((NeonListener)listener).exitGroupClause(this);
		}
	}

	public final GroupClauseContext groupClause() throws RecognitionException {
		GroupClauseContext _localctx = new GroupClauseContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_groupClause);
		try {
			setState(136);
			switch (_input.LA(1)) {
			case STRING:
				enterOuterAlt(_localctx, 1);
				{
				setState(134); match(STRING);
				}
				break;
			case 1:
			case 2:
			case 3:
			case 4:
			case 7:
			case 9:
			case 11:
				enterOuterAlt(_localctx, 2);
				{
				setState(135); function();
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
		public TerminalNode ALL_FIELDS() { return getToken(NeonParser.ALL_FIELDS, 0); }
		public CountContext count() {
			return getRuleContext(CountContext.class,0);
		}
		public FunctionNameContext functionName() {
			return getRuleContext(FunctionNameContext.class,0);
		}
		public TerminalNode STRING() { return getToken(NeonParser.STRING, 0); }
		public FunctionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_function; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NeonListener ) ((NeonListener)listener).enterFunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NeonListener ) ((NeonListener)listener).exitFunction(this);
		}
	}

	public final FunctionContext function() throws RecognitionException {
		FunctionContext _localctx = new FunctionContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_function);
		try {
			setState(148);
			switch ( getInterpreter().adaptivePredict(_input,14,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(138); functionName();
				setState(139); match(8);
				setState(140); match(STRING);
				setState(141); match(5);
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(143); count();
				setState(144); match(8);
				setState(145); match(ALL_FIELDS);
				setState(146); match(5);
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

	public static class FunctionNameContext extends ParserRuleContext {
		public CountContext count() {
			return getRuleContext(CountContext.class,0);
		}
		public FunctionNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_functionName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NeonListener ) ((NeonListener)listener).enterFunctionName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NeonListener ) ((NeonListener)listener).exitFunctionName(this);
		}
	}

	public final FunctionNameContext functionName() throws RecognitionException {
		FunctionNameContext _localctx = new FunctionNameContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_functionName);
		try {
			setState(157);
			switch (_input.LA(1)) {
			case 11:
				enterOuterAlt(_localctx, 1);
				{
				setState(150); match(11);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 2);
				{
				setState(151); match(3);
				}
				break;
			case 1:
				enterOuterAlt(_localctx, 3);
				{
				setState(152); match(1);
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 4);
				{
				setState(153); count();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 5);
				{
				setState(154); match(7);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 6);
				{
				setState(155); match(2);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 7);
				{
				setState(156); match(4);
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

	public static class CountContext extends ParserRuleContext {
		public CountContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_count; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NeonListener ) ((NeonListener)listener).enterCount(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NeonListener ) ((NeonListener)listener).exitCount(this);
		}
	}

	public final CountContext count() throws RecognitionException {
		CountContext _localctx = new CountContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_count);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(159); match(9);
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

	public static class LimitContext extends ParserRuleContext {
		public TerminalNode WHOLE_NUMBER() { return getToken(NeonParser.WHOLE_NUMBER, 0); }
		public TerminalNode LIMIT() { return getToken(NeonParser.LIMIT, 0); }
		public LimitContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_limit; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NeonListener ) ((NeonListener)listener).enterLimit(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NeonListener ) ((NeonListener)listener).exitLimit(this);
		}
	}

	public final LimitContext limit() throws RecognitionException {
		LimitContext _localctx = new LimitContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_limit);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(161); match(LIMIT);
			setState(162); match(WHOLE_NUMBER);
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

	public static class OffsetContext extends ParserRuleContext {
		public TerminalNode WHOLE_NUMBER() { return getToken(NeonParser.WHOLE_NUMBER, 0); }
		public TerminalNode OFFSET() { return getToken(NeonParser.OFFSET, 0); }
		public OffsetContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_offset; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NeonListener ) ((NeonListener)listener).enterOffset(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NeonListener ) ((NeonListener)listener).exitOffset(this);
		}
	}

	public final OffsetContext offset() throws RecognitionException {
		OffsetContext _localctx = new OffsetContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_offset);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(164); match(OFFSET);
			setState(165); match(WHOLE_NUMBER);
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

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 7: return whereClause_sempred((WhereClauseContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean whereClause_sempred(WhereClauseContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0: return 3 >= _localctx._p;

		case 1: return 2 >= _localctx._p;
		}
		return true;
	}

	public static final String _serializedATN =
		"\2\3#\u00aa\4\2\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4"+
		"\t\t\t\4\n\t\n\4\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20"+
		"\4\21\t\21\4\22\t\22\4\23\t\23\4\24\t\24\4\25\t\25\3\2\3\2\5\2-\n\2\3"+
		"\2\3\2\5\2\61\n\2\3\3\3\3\3\3\3\4\3\4\3\4\3\4\5\4:\n\4\3\4\7\4=\n\4\f"+
		"\4\16\4@\13\4\3\5\3\5\3\5\3\6\3\6\5\6G\n\6\3\7\3\7\3\7\7\7L\n\7\f\7\16"+
		"\7O\13\7\3\b\3\b\3\b\3\t\3\t\3\t\3\t\3\t\3\t\5\tZ\n\t\3\t\3\t\3\t\3\t"+
		"\3\t\3\t\7\tb\n\t\f\t\16\te\13\t\3\n\3\n\3\n\3\n\3\13\3\13\3\13\3\13\5"+
		"\13o\n\13\3\f\3\f\3\r\3\r\3\r\3\r\7\rw\n\r\f\r\16\rz\13\r\3\16\3\16\5"+
		"\16~\n\16\3\17\3\17\3\17\3\17\7\17\u0084\n\17\f\17\16\17\u0087\13\17\3"+
		"\20\3\20\5\20\u008b\n\20\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21"+
		"\3\21\5\21\u0097\n\21\3\22\3\22\3\22\3\22\3\22\3\22\3\22\5\22\u00a0\n"+
		"\22\3\23\3\23\3\24\3\24\3\24\3\25\3\25\3\25\3\25\2\26\2\4\6\b\n\f\16\20"+
		"\22\24\26\30\32\34\36 \"$&(\2\4\3 \"\3\21\26\u00ac\2*\3\2\2\2\4\62\3\2"+
		"\2\2\6\65\3\2\2\2\bA\3\2\2\2\nF\3\2\2\2\fH\3\2\2\2\16P\3\2\2\2\20Y\3\2"+
		"\2\2\22f\3\2\2\2\24n\3\2\2\2\26p\3\2\2\2\30r\3\2\2\2\32{\3\2\2\2\34\177"+
		"\3\2\2\2\36\u008a\3\2\2\2 \u0096\3\2\2\2\"\u009f\3\2\2\2$\u00a1\3\2\2"+
		"\2&\u00a3\3\2\2\2(\u00a6\3\2\2\2*,\5\4\3\2+-\7\f\2\2,+\3\2\2\2,-\3\2\2"+
		"\2-.\3\2\2\2.\60\5\6\4\2/\61\7\f\2\2\60/\3\2\2\2\60\61\3\2\2\2\61\3\3"+
		"\2\2\2\62\63\7\27\2\2\63\64\7\"\2\2\64\5\3\2\2\2\65\66\5\b\5\2\66\67\7"+
		"\31\2\2\679\7\"\2\28:\5\16\b\298\3\2\2\29:\3\2\2\2:>\3\2\2\2;=\5\24\13"+
		"\2<;\3\2\2\2=@\3\2\2\2><\3\2\2\2>?\3\2\2\2?\7\3\2\2\2@>\3\2\2\2AB\7\30"+
		"\2\2BC\5\n\6\2C\t\3\2\2\2DG\7\20\2\2EG\5\f\7\2FD\3\2\2\2FE\3\2\2\2G\13"+
		"\3\2\2\2HM\7\"\2\2IJ\7\b\2\2JL\7\"\2\2KI\3\2\2\2LO\3\2\2\2MK\3\2\2\2M"+
		"N\3\2\2\2N\r\3\2\2\2OM\3\2\2\2PQ\7\32\2\2QR\5\20\t\2R\17\3\2\2\2ST\b\t"+
		"\1\2TU\7\n\2\2UV\5\20\t\2VW\7\7\2\2WZ\3\2\2\2XZ\5\22\n\2YS\3\2\2\2YX\3"+
		"\2\2\2Zc\3\2\2\2[\\\6\t\2\3\\]\7\16\2\2]b\5\20\t\2^_\6\t\3\3_`\7\17\2"+
		"\2`b\5\20\t\2a[\3\2\2\2a^\3\2\2\2be\3\2\2\2ca\3\2\2\2cd\3\2\2\2d\21\3"+
		"\2\2\2ec\3\2\2\2fg\7\"\2\2gh\5\26\f\2hi\t\2\2\2i\23\3\2\2\2jo\5\30\r\2"+
		"ko\5\34\17\2lo\5&\24\2mo\5(\25\2nj\3\2\2\2nk\3\2\2\2nl\3\2\2\2nm\3\2\2"+
		"\2o\25\3\2\2\2pq\t\3\2\2q\27\3\2\2\2rs\7\36\2\2sx\5\32\16\2tu\7\b\2\2"+
		"uw\5\32\16\2vt\3\2\2\2wz\3\2\2\2xv\3\2\2\2xy\3\2\2\2y\31\3\2\2\2zx\3\2"+
		"\2\2{}\7\"\2\2|~\7\37\2\2}|\3\2\2\2}~\3\2\2\2~\33\3\2\2\2\177\u0080\7"+
		"\33\2\2\u0080\u0085\5\36\20\2\u0081\u0082\7\b\2\2\u0082\u0084\5\36\20"+
		"\2\u0083\u0081\3\2\2\2\u0084\u0087\3\2\2\2\u0085\u0083\3\2\2\2\u0085\u0086"+
		"\3\2\2\2\u0086\35\3\2\2\2\u0087\u0085\3\2\2\2\u0088\u008b\7\"\2\2\u0089"+
		"\u008b\5 \21\2\u008a\u0088\3\2\2\2\u008a\u0089\3\2\2\2\u008b\37\3\2\2"+
		"\2\u008c\u008d\5\"\22\2\u008d\u008e\7\n\2\2\u008e\u008f\7\"\2\2\u008f"+
		"\u0090\7\7\2\2\u0090\u0097\3\2\2\2\u0091\u0092\5$\23\2\u0092\u0093\7\n"+
		"\2\2\u0093\u0094\7\20\2\2\u0094\u0095\7\7\2\2\u0095\u0097\3\2\2\2\u0096"+
		"\u008c\3\2\2\2\u0096\u0091\3\2\2\2\u0097!\3\2\2\2\u0098\u00a0\7\r\2\2"+
		"\u0099\u00a0\7\5\2\2\u009a\u00a0\7\3\2\2\u009b\u00a0\5$\23\2\u009c\u00a0"+
		"\7\t\2\2\u009d\u00a0\7\4\2\2\u009e\u00a0\7\6\2\2\u009f\u0098\3\2\2\2\u009f"+
		"\u0099\3\2\2\2\u009f\u009a\3\2\2\2\u009f\u009b\3\2\2\2\u009f\u009c\3\2"+
		"\2\2\u009f\u009d\3\2\2\2\u009f\u009e\3\2\2\2\u00a0#\3\2\2\2\u00a1\u00a2"+
		"\7\13\2\2\u00a2%\3\2\2\2\u00a3\u00a4\7\34\2\2\u00a4\u00a5\7 \2\2\u00a5"+
		"\'\3\2\2\2\u00a6\u00a7\7\35\2\2\u00a7\u00a8\7 \2\2\u00a8)\3\2\2\2\22,"+
		"\609>FMYacnx}\u0085\u008a\u0096\u009f";
	public static final ATN _ATN =
		ATNSimulator.deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
	}
}