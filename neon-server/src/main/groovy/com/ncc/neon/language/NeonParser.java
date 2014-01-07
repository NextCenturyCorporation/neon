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
		T__1=10, T__0=11, ALL_FIELDS=12, AND=13, OR=14, GT=15, GTE=16, LT=17, 
		LTE=18, EQ=19, NE=20, USE=21, SELECT=22, FROM=23, WHERE=24, GROUP=25, 
		LIMIT=26, OFFSET=27, SORT=28, SORT_DIRECTION=29, WHOLE_NUMBER=30, NUMBER=31, 
		STRING=32, WHITESPACE=33;
	public static final String[] tokenNames = {
		"<INVALID>", "'max'", "'avg'", "'last'", "'sum'", "')'", "','", "'min'", 
		"'('", "'count'", "';'", "'first'", "'*'", "AND", "OR", "'>'", "'>='", 
		"'<'", "'<='", "'='", "'!='", "USE", "SELECT", "FROM", "WHERE", "GROUP", 
		"LIMIT", "OFFSET", "SORT", "SORT_DIRECTION", "WHOLE_NUMBER", "NUMBER", 
		"STRING", "WHITESPACE"
	};
	public static final int
		RULE_statement = 0, RULE_database = 1, RULE_query = 2, RULE_select = 3, 
		RULE_selectFields = 4, RULE_fieldList = 5, RULE_where = 6, RULE_whereClause = 7, 
		RULE_simpleWhereClause = 8, RULE_additionalClauses = 9, RULE_operator = 10, 
		RULE_sort = 11, RULE_sortClause = 12, RULE_group = 13, RULE_groupClause = 14, 
		RULE_function = 15, RULE_functionName = 16, RULE_limit = 17, RULE_offset = 18;
	public static final String[] ruleNames = {
		"statement", "database", "query", "select", "selectFields", "fieldList", 
		"where", "whereClause", "simpleWhereClause", "additionalClauses", "operator", 
		"sort", "sortClause", "group", "groupClause", "function", "functionName", 
		"limit", "offset"
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
			setState(38); database();
			setState(40);
			_la = _input.LA(1);
			if (_la==10) {
				{
				setState(39); match(10);
				}
			}

			setState(42); query();
			setState(44);
			_la = _input.LA(1);
			if (_la==10) {
				{
				setState(43); match(10);
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
			setState(46); match(USE);
			setState(47); match(STRING);
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
			setState(49); select();
			setState(50); match(FROM);
			setState(51); match(STRING);
			setState(53);
			_la = _input.LA(1);
			if (_la==WHERE) {
				{
				setState(52); where();
				}
			}

			setState(58);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << GROUP) | (1L << LIMIT) | (1L << OFFSET) | (1L << SORT))) != 0)) {
				{
				{
				setState(55); additionalClauses();
				}
				}
				setState(60);
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
			setState(61); match(SELECT);
			setState(62); selectFields();
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
			setState(66);
			switch (_input.LA(1)) {
			case ALL_FIELDS:
				enterOuterAlt(_localctx, 1);
				{
				setState(64); match(ALL_FIELDS);
				}
				break;
			case STRING:
				enterOuterAlt(_localctx, 2);
				{
				setState(65); fieldList();
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
			setState(68); match(STRING);
			setState(73);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==6) {
				{
				{
				setState(69); match(6);
				setState(70); match(STRING);
				}
				}
				setState(75);
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
			setState(76); match(WHERE);
			setState(77); whereClause(0);
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
			setState(85);
			switch (_input.LA(1)) {
			case 8:
				{
				setState(80); match(8);
				setState(81); whereClause(0);
				setState(82); match(5);
				}
				break;
			case STRING:
				{
				setState(84); simpleWhereClause();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			_ctx.stop = _input.LT(-1);
			setState(95);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,8,_ctx);
			while ( _alt!=2 && _alt!=-1 ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(93);
					switch ( getInterpreter().adaptivePredict(_input,7,_ctx) ) {
					case 1:
						{
						_localctx = new WhereClauseContext(_parentctx, _parentState, _p);
						pushNewRecursionContext(_localctx, _startState, RULE_whereClause);
						setState(87);
						if (!(3 >= _localctx._p)) throw new FailedPredicateException(this, "3 >= $_p");
						setState(88); match(AND);
						setState(89); whereClause(4);
						}
						break;

					case 2:
						{
						_localctx = new WhereClauseContext(_parentctx, _parentState, _p);
						pushNewRecursionContext(_localctx, _startState, RULE_whereClause);
						setState(90);
						if (!(2 >= _localctx._p)) throw new FailedPredicateException(this, "2 >= $_p");
						setState(91); match(OR);
						setState(92); whereClause(3);
						}
						break;
					}
					} 
				}
				setState(97);
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
			setState(98); match(STRING);
			setState(99); operator();
			setState(100);
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
			setState(106);
			switch (_input.LA(1)) {
			case SORT:
				enterOuterAlt(_localctx, 1);
				{
				setState(102); sort();
				}
				break;
			case GROUP:
				enterOuterAlt(_localctx, 2);
				{
				setState(103); group();
				}
				break;
			case LIMIT:
				enterOuterAlt(_localctx, 3);
				{
				setState(104); limit();
				}
				break;
			case OFFSET:
				enterOuterAlt(_localctx, 4);
				{
				setState(105); offset();
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
			setState(108);
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
			setState(110); match(SORT);
			setState(111); sortClause();
			setState(116);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==6) {
				{
				{
				setState(112); match(6);
				setState(113); sortClause();
				}
				}
				setState(118);
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
			setState(119); match(STRING);
			setState(121);
			_la = _input.LA(1);
			if (_la==SORT_DIRECTION) {
				{
				setState(120); match(SORT_DIRECTION);
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
			setState(123); match(GROUP);
			setState(124); groupClause();
			setState(129);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==6) {
				{
				{
				setState(125); match(6);
				setState(126); groupClause();
				}
				}
				setState(131);
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
			setState(134);
			switch (_input.LA(1)) {
			case STRING:
				enterOuterAlt(_localctx, 1);
				{
				setState(132); match(STRING);
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
				setState(133); function();
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
			enterOuterAlt(_localctx, 1);
			{
			setState(136); functionName();
			setState(137); match(8);
			setState(138); match(STRING);
			setState(139); match(5);
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
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(141);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << 1) | (1L << 2) | (1L << 3) | (1L << 4) | (1L << 7) | (1L << 9) | (1L << 11))) != 0)) ) {
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
		enterRule(_localctx, 34, RULE_limit);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(143); match(LIMIT);
			setState(144); match(WHOLE_NUMBER);
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
		enterRule(_localctx, 36, RULE_offset);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(146); match(OFFSET);
			setState(147); match(WHOLE_NUMBER);
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
		"\2\3#\u0098\4\2\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4"+
		"\t\t\t\4\n\t\n\4\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20"+
		"\4\21\t\21\4\22\t\22\4\23\t\23\4\24\t\24\3\2\3\2\5\2+\n\2\3\2\3\2\5\2"+
		"/\n\2\3\3\3\3\3\3\3\4\3\4\3\4\3\4\5\48\n\4\3\4\7\4;\n\4\f\4\16\4>\13\4"+
		"\3\5\3\5\3\5\3\6\3\6\5\6E\n\6\3\7\3\7\3\7\7\7J\n\7\f\7\16\7M\13\7\3\b"+
		"\3\b\3\b\3\t\3\t\3\t\3\t\3\t\3\t\5\tX\n\t\3\t\3\t\3\t\3\t\3\t\3\t\7\t"+
		"`\n\t\f\t\16\tc\13\t\3\n\3\n\3\n\3\n\3\13\3\13\3\13\3\13\5\13m\n\13\3"+
		"\f\3\f\3\r\3\r\3\r\3\r\7\ru\n\r\f\r\16\rx\13\r\3\16\3\16\5\16|\n\16\3"+
		"\17\3\17\3\17\3\17\7\17\u0082\n\17\f\17\16\17\u0085\13\17\3\20\3\20\5"+
		"\20\u0089\n\20\3\21\3\21\3\21\3\21\3\21\3\22\3\22\3\23\3\23\3\23\3\24"+
		"\3\24\3\24\3\24\2\25\2\4\6\b\n\f\16\20\22\24\26\30\32\34\36 \"$&\2\5\3"+
		" \"\3\21\26\6\3\6\t\t\13\13\r\r\u0094\2(\3\2\2\2\4\60\3\2\2\2\6\63\3\2"+
		"\2\2\b?\3\2\2\2\nD\3\2\2\2\fF\3\2\2\2\16N\3\2\2\2\20W\3\2\2\2\22d\3\2"+
		"\2\2\24l\3\2\2\2\26n\3\2\2\2\30p\3\2\2\2\32y\3\2\2\2\34}\3\2\2\2\36\u0088"+
		"\3\2\2\2 \u008a\3\2\2\2\"\u008f\3\2\2\2$\u0091\3\2\2\2&\u0094\3\2\2\2"+
		"(*\5\4\3\2)+\7\f\2\2*)\3\2\2\2*+\3\2\2\2+,\3\2\2\2,.\5\6\4\2-/\7\f\2\2"+
		".-\3\2\2\2./\3\2\2\2/\3\3\2\2\2\60\61\7\27\2\2\61\62\7\"\2\2\62\5\3\2"+
		"\2\2\63\64\5\b\5\2\64\65\7\31\2\2\65\67\7\"\2\2\668\5\16\b\2\67\66\3\2"+
		"\2\2\678\3\2\2\28<\3\2\2\29;\5\24\13\2:9\3\2\2\2;>\3\2\2\2<:\3\2\2\2<"+
		"=\3\2\2\2=\7\3\2\2\2><\3\2\2\2?@\7\30\2\2@A\5\n\6\2A\t\3\2\2\2BE\7\16"+
		"\2\2CE\5\f\7\2DB\3\2\2\2DC\3\2\2\2E\13\3\2\2\2FK\7\"\2\2GH\7\b\2\2HJ\7"+
		"\"\2\2IG\3\2\2\2JM\3\2\2\2KI\3\2\2\2KL\3\2\2\2L\r\3\2\2\2MK\3\2\2\2NO"+
		"\7\32\2\2OP\5\20\t\2P\17\3\2\2\2QR\b\t\1\2RS\7\n\2\2ST\5\20\t\2TU\7\7"+
		"\2\2UX\3\2\2\2VX\5\22\n\2WQ\3\2\2\2WV\3\2\2\2Xa\3\2\2\2YZ\6\t\2\3Z[\7"+
		"\17\2\2[`\5\20\t\2\\]\6\t\3\3]^\7\20\2\2^`\5\20\t\2_Y\3\2\2\2_\\\3\2\2"+
		"\2`c\3\2\2\2a_\3\2\2\2ab\3\2\2\2b\21\3\2\2\2ca\3\2\2\2de\7\"\2\2ef\5\26"+
		"\f\2fg\t\2\2\2g\23\3\2\2\2hm\5\30\r\2im\5\34\17\2jm\5$\23\2km\5&\24\2"+
		"lh\3\2\2\2li\3\2\2\2lj\3\2\2\2lk\3\2\2\2m\25\3\2\2\2no\t\3\2\2o\27\3\2"+
		"\2\2pq\7\36\2\2qv\5\32\16\2rs\7\b\2\2su\5\32\16\2tr\3\2\2\2ux\3\2\2\2"+
		"vt\3\2\2\2vw\3\2\2\2w\31\3\2\2\2xv\3\2\2\2y{\7\"\2\2z|\7\37\2\2{z\3\2"+
		"\2\2{|\3\2\2\2|\33\3\2\2\2}~\7\33\2\2~\u0083\5\36\20\2\177\u0080\7\b\2"+
		"\2\u0080\u0082\5\36\20\2\u0081\177\3\2\2\2\u0082\u0085\3\2\2\2\u0083\u0081"+
		"\3\2\2\2\u0083\u0084\3\2\2\2\u0084\35\3\2\2\2\u0085\u0083\3\2\2\2\u0086"+
		"\u0089\7\"\2\2\u0087\u0089\5 \21\2\u0088\u0086\3\2\2\2\u0088\u0087\3\2"+
		"\2\2\u0089\37\3\2\2\2\u008a\u008b\5\"\22\2\u008b\u008c\7\n\2\2\u008c\u008d"+
		"\7\"\2\2\u008d\u008e\7\7\2\2\u008e!\3\2\2\2\u008f\u0090\t\4\2\2\u0090"+
		"#\3\2\2\2\u0091\u0092\7\34\2\2\u0092\u0093\7 \2\2\u0093%\3\2\2\2\u0094"+
		"\u0095\7\35\2\2\u0095\u0096\7 \2\2\u0096\'\3\2\2\2\20*.\67<DKW_alv{\u0083"+
		"\u0088";
	public static final ATN _ATN =
		ATNSimulator.deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
	}
}