// Generated from Neon.g4 by ANTLR 4.0

package com.ncc.neon.language.parse;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNSimulator;
import org.antlr.v4.runtime.atn.ParserATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.List;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class NeonParser extends Parser {
	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__12=1, T__11=2, T__10=3, T__9=4, T__8=5, T__7=6, T__6=7, T__5=8, T__4=9, 
		T__3=10, T__2=11, T__1=12, T__0=13, AND=14, OR=15, GT=16, GTE=17, LT=18, 
		LTE=19, EQ=20, NE=21, USE=22, SELECT=23, FROM=24, WHERE=25, GROUP=26, 
		LIMIT=27, SORT=28, SORT_DIRECTION=29, STRING=30, WHITESPACE=31;
	public static final String[] tokenNames = {
		"<INVALID>", "'avg'", "'max'", "'last'", "')'", "','", "'min'", "'push'", 
		"'('", "'addToSet'", "';'", "'first'", "'sum'", "'count'", "AND", "OR", 
		"'>'", "'>='", "'<'", "'<='", "'='", "'!='", "USE", "SELECT", "FROM", 
		"WHERE", "GROUP", "LIMIT", "SORT", "SORT_DIRECTION", "STRING", "WHITESPACE"
	};
	public static final int
		RULE_statement = 0, RULE_database = 1, RULE_query = 2, RULE_where = 3, 
		RULE_whereClause = 4, RULE_simpleWhereClause = 5, RULE_operator = 6, RULE_sort = 7, 
		RULE_sortClause = 8, RULE_group = 9, RULE_groupClause = 10, RULE_function = 11, 
		RULE_functionName = 12, RULE_limit = 13;
	public static final String[] ruleNames = {
		"statement", "database", "query", "where", "whereClause", "simpleWhereClause", 
		"operator", "sort", "sortClause", "group", "groupClause", "function", 
		"functionName", "limit"
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
			setState(35); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(30);
				switch (_input.LA(1)) {
				case SELECT:
					{
					setState(28); query();
					}
					break;
				case USE:
					{
					setState(29); database();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				{
				setState(33);
				_la = _input.LA(1);
				if (_la==10) {
					{
					setState(32); match(10);
					}
				}

				}
				}
				}
				setState(37); 
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
			setState(39); match(USE);
			setState(40); match(STRING);
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
		public LimitContext limit() {
			return getRuleContext(LimitContext.class,0);
		}
		public SortContext sort() {
			return getRuleContext(SortContext.class,0);
		}
		public TerminalNode FROM() { return getToken(NeonParser.FROM, 0); }
		public TerminalNode SELECT() { return getToken(NeonParser.SELECT, 0); }
		public GroupContext group() {
			return getRuleContext(GroupContext.class,0);
		}
		public WhereContext where() {
			return getRuleContext(WhereContext.class,0);
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
			setState(42); match(SELECT);
			setState(43); match(FROM);
			setState(44); match(STRING);
			setState(46);
			_la = _input.LA(1);
			if (_la==WHERE) {
				{
				setState(45); where();
				}
			}

			setState(49);
			_la = _input.LA(1);
			if (_la==SORT) {
				{
				setState(48); sort();
				}
			}

			setState(52);
			_la = _input.LA(1);
			if (_la==GROUP) {
				{
				setState(51); group();
				}
			}

			setState(55);
			_la = _input.LA(1);
			if (_la==LIMIT) {
				{
				setState(54); limit();
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
			setState(57); match(WHERE);
			setState(58); whereClause(0);
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
		int _startState = 8;
		enterRecursionRule(_localctx, RULE_whereClause);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(66);
			switch (_input.LA(1)) {
			case 8:
				{
				setState(61); match(8);
				setState(62); whereClause(0);
				setState(63); match(4);
				}
				break;
			case STRING:
				{
				setState(65); simpleWhereClause();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			_ctx.stop = _input.LT(-1);
			setState(76);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,9,_ctx);
			while ( _alt!=2 && _alt!=-1 ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(74);
					switch ( getInterpreter().adaptivePredict(_input,8,_ctx) ) {
					case 1:
						{
						_localctx = new WhereClauseContext(_parentctx, _parentState, _p);
						pushNewRecursionContext(_localctx, _startState, RULE_whereClause);
						setState(68);
						if (!(3 >= _localctx._p)) throw new FailedPredicateException(this, "3 >= $_p");
						setState(69); match(AND);
						setState(70); whereClause(4);
						}
						break;

					case 2:
						{
						_localctx = new WhereClauseContext(_parentctx, _parentState, _p);
						pushNewRecursionContext(_localctx, _startState, RULE_whereClause);
						setState(71);
						if (!(2 >= _localctx._p)) throw new FailedPredicateException(this, "2 >= $_p");
						setState(72); match(OR);
						setState(73); whereClause(3);
						}
						break;
					}
					} 
				}
				setState(78);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,9,_ctx);
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
			setState(79); match(STRING);
			setState(80); operator();
			setState(81); match(STRING);
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
		enterRule(_localctx, 12, RULE_operator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(83);
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
		enterRule(_localctx, 14, RULE_sort);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(85); match(SORT);
			setState(86); sortClause();
			setState(91);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==5) {
				{
				{
				setState(87); match(5);
				setState(88); sortClause();
				}
				}
				setState(93);
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
		enterRule(_localctx, 16, RULE_sortClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(94); match(STRING);
			setState(96);
			_la = _input.LA(1);
			if (_la==SORT_DIRECTION) {
				{
				setState(95); match(SORT_DIRECTION);
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
		enterRule(_localctx, 18, RULE_group);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(98); match(GROUP);
			setState(99); groupClause();
			setState(104);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==5) {
				{
				{
				setState(100); match(5);
				setState(101); groupClause();
				}
				}
				setState(106);
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
		enterRule(_localctx, 20, RULE_groupClause);
		try {
			setState(109);
			switch (_input.LA(1)) {
			case STRING:
				enterOuterAlt(_localctx, 1);
				{
				setState(107); match(STRING);
				}
				break;
			case 1:
			case 2:
			case 3:
			case 6:
			case 7:
			case 9:
			case 11:
			case 12:
			case 13:
				enterOuterAlt(_localctx, 2);
				{
				setState(108); function();
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
		enterRule(_localctx, 22, RULE_function);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(111); functionName();
			setState(112); match(8);
			setState(113); match(STRING);
			setState(114); match(4);
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
		enterRule(_localctx, 24, RULE_functionName);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(116);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << 1) | (1L << 2) | (1L << 3) | (1L << 6) | (1L << 7) | (1L << 9) | (1L << 11) | (1L << 12) | (1L << 13))) != 0)) ) {
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
		public TerminalNode LIMIT() { return getToken(NeonParser.LIMIT, 0); }
		public TerminalNode STRING() { return getToken(NeonParser.STRING, 0); }
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
		enterRule(_localctx, 26, RULE_limit);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(118); match(LIMIT);
			setState(119); match(STRING);
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
		case 4: return whereClause_sempred((WhereClauseContext)_localctx, predIndex);
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
		"\2\3!|\4\2\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t"+
		"\4\n\t\n\4\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\3\2\3\2\5\2!\n"+
		"\2\3\2\5\2$\n\2\6\2&\n\2\r\2\16\2\'\3\3\3\3\3\3\3\4\3\4\3\4\3\4\5\4\61"+
		"\n\4\3\4\5\4\64\n\4\3\4\5\4\67\n\4\3\4\5\4:\n\4\3\5\3\5\3\5\3\6\3\6\3"+
		"\6\3\6\3\6\3\6\5\6E\n\6\3\6\3\6\3\6\3\6\3\6\3\6\7\6M\n\6\f\6\16\6P\13"+
		"\6\3\7\3\7\3\7\3\7\3\b\3\b\3\t\3\t\3\t\3\t\7\t\\\n\t\f\t\16\t_\13\t\3"+
		"\n\3\n\5\nc\n\n\3\13\3\13\3\13\3\13\7\13i\n\13\f\13\16\13l\13\13\3\f\3"+
		"\f\5\fp\n\f\3\r\3\r\3\r\3\r\3\r\3\16\3\16\3\17\3\17\3\17\3\17\2\20\2\4"+
		"\6\b\n\f\16\20\22\24\26\30\32\34\2\4\3\22\27\6\3\5\b\t\13\13\r\17{\2%"+
		"\3\2\2\2\4)\3\2\2\2\6,\3\2\2\2\b;\3\2\2\2\nD\3\2\2\2\fQ\3\2\2\2\16U\3"+
		"\2\2\2\20W\3\2\2\2\22`\3\2\2\2\24d\3\2\2\2\26o\3\2\2\2\30q\3\2\2\2\32"+
		"v\3\2\2\2\34x\3\2\2\2\36!\5\6\4\2\37!\5\4\3\2 \36\3\2\2\2 \37\3\2\2\2"+
		"!#\3\2\2\2\"$\7\f\2\2#\"\3\2\2\2#$\3\2\2\2$&\3\2\2\2% \3\2\2\2&\'\3\2"+
		"\2\2\'%\3\2\2\2\'(\3\2\2\2(\3\3\2\2\2)*\7\30\2\2*+\7 \2\2+\5\3\2\2\2,"+
		"-\7\31\2\2-.\7\32\2\2.\60\7 \2\2/\61\5\b\5\2\60/\3\2\2\2\60\61\3\2\2\2"+
		"\61\63\3\2\2\2\62\64\5\20\t\2\63\62\3\2\2\2\63\64\3\2\2\2\64\66\3\2\2"+
		"\2\65\67\5\24\13\2\66\65\3\2\2\2\66\67\3\2\2\2\679\3\2\2\28:\5\34\17\2"+
		"98\3\2\2\29:\3\2\2\2:\7\3\2\2\2;<\7\33\2\2<=\5\n\6\2=\t\3\2\2\2>?\b\6"+
		"\1\2?@\7\n\2\2@A\5\n\6\2AB\7\6\2\2BE\3\2\2\2CE\5\f\7\2D>\3\2\2\2DC\3\2"+
		"\2\2EN\3\2\2\2FG\6\6\2\3GH\7\20\2\2HM\5\n\6\2IJ\6\6\3\3JK\7\21\2\2KM\5"+
		"\n\6\2LF\3\2\2\2LI\3\2\2\2MP\3\2\2\2NL\3\2\2\2NO\3\2\2\2O\13\3\2\2\2P"+
		"N\3\2\2\2QR\7 \2\2RS\5\16\b\2ST\7 \2\2T\r\3\2\2\2UV\t\2\2\2V\17\3\2\2"+
		"\2WX\7\36\2\2X]\5\22\n\2YZ\7\7\2\2Z\\\5\22\n\2[Y\3\2\2\2\\_\3\2\2\2]["+
		"\3\2\2\2]^\3\2\2\2^\21\3\2\2\2_]\3\2\2\2`b\7 \2\2ac\7\37\2\2ba\3\2\2\2"+
		"bc\3\2\2\2c\23\3\2\2\2de\7\34\2\2ej\5\26\f\2fg\7\7\2\2gi\5\26\f\2hf\3"+
		"\2\2\2il\3\2\2\2jh\3\2\2\2jk\3\2\2\2k\25\3\2\2\2lj\3\2\2\2mp\7 \2\2np"+
		"\5\30\r\2om\3\2\2\2on\3\2\2\2p\27\3\2\2\2qr\5\32\16\2rs\7\n\2\2st\7 \2"+
		"\2tu\7\6\2\2u\31\3\2\2\2vw\t\3\2\2w\33\3\2\2\2xy\7\35\2\2yz\7 \2\2z\35"+
		"\3\2\2\2\20 #\'\60\63\669DLN]bjo";
	public static final ATN _ATN =
		ATNSimulator.deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
	}
}