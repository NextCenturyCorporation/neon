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
		T__3=1, T__2=2, T__1=3, T__0=4, AND=5, OR=6, GT=7, GTE=8, LT=9, LTE=10, 
		EQ=11, NE=12, USE=13, SELECT=14, FROM=15, WHERE=16, SORT=17, SORT_DIRECTION=18, 
		STRING=19, WHITESPACE=20;
	public static final String[] tokenNames = {
		"<INVALID>", "')'", "','", "'('", "';'", "AND", "OR", "'>'", "'>='", "'<'", 
		"'<='", "'='", "'!='", "USE", "SELECT", "FROM", "WHERE", "SORT", "SORT_DIRECTION", 
		"STRING", "WHITESPACE"
	};
	public static final int
		RULE_statement = 0, RULE_database = 1, RULE_query = 2, RULE_where = 3, 
		RULE_whereClause = 4, RULE_simpleWhereClause = 5, RULE_operator = 6, RULE_sort = 7, 
		RULE_sortClause = 8;
	public static final String[] ruleNames = {
		"statement", "database", "query", "where", "whereClause", "simpleWhereClause", 
		"operator", "sort", "sortClause"
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
			setState(25); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(20);
				switch (_input.LA(1)) {
				case SELECT:
					{
					setState(18); query();
					}
					break;
				case USE:
					{
					setState(19); database();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(23);
				_la = _input.LA(1);
				if (_la==4) {
					{
					setState(22); match(4);
					}
				}

				}
				}
				setState(27); 
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
			setState(29); match(USE);
			setState(30); match(STRING);
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
		public SortContext sort() {
			return getRuleContext(SortContext.class,0);
		}
		public TerminalNode FROM() { return getToken(NeonParser.FROM, 0); }
		public TerminalNode SELECT() { return getToken(NeonParser.SELECT, 0); }
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
			setState(32); match(SELECT);
			setState(33); match(FROM);
			setState(34); match(STRING);
			setState(36);
			_la = _input.LA(1);
			if (_la==WHERE) {
				{
				setState(35); where();
				}
			}

			setState(39);
			_la = _input.LA(1);
			if (_la==SORT) {
				{
				setState(38); sort();
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
			setState(41); match(WHERE);
			setState(42); whereClause(0);
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
			setState(50);
			switch (_input.LA(1)) {
			case 3:
				{
				setState(45); match(3);
				setState(46); whereClause(0);
				setState(47); match(1);
				}
				break;
			case STRING:
				{
				setState(49); simpleWhereClause();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			_ctx.stop = _input.LT(-1);
			setState(60);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,7,_ctx);
			while ( _alt!=2 && _alt!=-1 ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(58);
					switch ( getInterpreter().adaptivePredict(_input,6,_ctx) ) {
					case 1:
						{
						_localctx = new WhereClauseContext(_parentctx, _parentState, _p);
						pushNewRecursionContext(_localctx, _startState, RULE_whereClause);
						setState(52);
						if (!(3 >= _localctx._p)) throw new FailedPredicateException(this, "3 >= $_p");
						setState(53); match(AND);
						setState(54); whereClause(4);
						}
						break;

					case 2:
						{
						_localctx = new WhereClauseContext(_parentctx, _parentState, _p);
						pushNewRecursionContext(_localctx, _startState, RULE_whereClause);
						setState(55);
						if (!(2 >= _localctx._p)) throw new FailedPredicateException(this, "2 >= $_p");
						setState(56); match(OR);
						setState(57); whereClause(3);
						}
						break;
					}
					} 
				}
				setState(62);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,7,_ctx);
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
			setState(63); match(STRING);
			setState(64); operator();
			setState(65); match(STRING);
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
			setState(67);
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
			setState(69); match(SORT);
			setState(70); sortClause();
			setState(75);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==2) {
				{
				{
				setState(71); match(2);
				setState(72); sortClause();
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
			setState(78); match(STRING);
			setState(80);
			_la = _input.LA(1);
			if (_la==SORT_DIRECTION) {
				{
				setState(79); match(SORT_DIRECTION);
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
		"\2\3\26U\4\2\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t"+
		"\t\4\n\t\n\3\2\3\2\5\2\27\n\2\3\2\5\2\32\n\2\6\2\34\n\2\r\2\16\2\35\3"+
		"\3\3\3\3\3\3\4\3\4\3\4\3\4\5\4\'\n\4\3\4\5\4*\n\4\3\5\3\5\3\5\3\6\3\6"+
		"\3\6\3\6\3\6\3\6\5\6\65\n\6\3\6\3\6\3\6\3\6\3\6\3\6\7\6=\n\6\f\6\16\6"+
		"@\13\6\3\7\3\7\3\7\3\7\3\b\3\b\3\t\3\t\3\t\3\t\7\tL\n\t\f\t\16\tO\13\t"+
		"\3\n\3\n\5\nS\n\n\3\n\2\13\2\4\6\b\n\f\16\20\22\2\3\3\t\16U\2\33\3\2\2"+
		"\2\4\37\3\2\2\2\6\"\3\2\2\2\b+\3\2\2\2\n\64\3\2\2\2\fA\3\2\2\2\16E\3\2"+
		"\2\2\20G\3\2\2\2\22P\3\2\2\2\24\27\5\6\4\2\25\27\5\4\3\2\26\24\3\2\2\2"+
		"\26\25\3\2\2\2\27\31\3\2\2\2\30\32\7\6\2\2\31\30\3\2\2\2\31\32\3\2\2\2"+
		"\32\34\3\2\2\2\33\26\3\2\2\2\34\35\3\2\2\2\35\33\3\2\2\2\35\36\3\2\2\2"+
		"\36\3\3\2\2\2\37 \7\17\2\2 !\7\25\2\2!\5\3\2\2\2\"#\7\20\2\2#$\7\21\2"+
		"\2$&\7\25\2\2%\'\5\b\5\2&%\3\2\2\2&\'\3\2\2\2\')\3\2\2\2(*\5\20\t\2)("+
		"\3\2\2\2)*\3\2\2\2*\7\3\2\2\2+,\7\22\2\2,-\5\n\6\2-\t\3\2\2\2./\b\6\1"+
		"\2/\60\7\5\2\2\60\61\5\n\6\2\61\62\7\3\2\2\62\65\3\2\2\2\63\65\5\f\7\2"+
		"\64.\3\2\2\2\64\63\3\2\2\2\65>\3\2\2\2\66\67\6\6\2\3\678\7\7\2\28=\5\n"+
		"\6\29:\6\6\3\3:;\7\b\2\2;=\5\n\6\2<\66\3\2\2\2<9\3\2\2\2=@\3\2\2\2><\3"+
		"\2\2\2>?\3\2\2\2?\13\3\2\2\2@>\3\2\2\2AB\7\25\2\2BC\5\16\b\2CD\7\25\2"+
		"\2D\r\3\2\2\2EF\t\2\2\2F\17\3\2\2\2GH\7\23\2\2HM\5\22\n\2IJ\7\4\2\2JL"+
		"\5\22\n\2KI\3\2\2\2LO\3\2\2\2MK\3\2\2\2MN\3\2\2\2N\21\3\2\2\2OM\3\2\2"+
		"\2PR\7\25\2\2QS\7\24\2\2RQ\3\2\2\2RS\3\2\2\2S\23\3\2\2\2\f\26\31\35&)"+
		"\64<>MR";
	public static final ATN _ATN =
		ATNSimulator.deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
	}
}