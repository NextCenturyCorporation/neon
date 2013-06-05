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
		T__2=1, T__1=2, T__0=3, AND=4, OR=5, GT=6, GTE=7, LT=8, LTE=9, EQ=10, 
		NE=11, USE=12, SELECT=13, FROM=14, WHERE=15, SORT_BY=16, SORT_DIRECTION=17, 
		STRING=18, WHITESPACE=19;
	public static final String[] tokenNames = {
		"<INVALID>", "')'", "'('", "';'", "AND", "OR", "'>'", "'>='", "'<'", "'<='", 
		"'='", "'!='", "USE", "SELECT", "FROM", "WHERE", "SORT_BY", "SORT_DIRECTION", 
		"STRING", "WHITESPACE"
	};
	public static final int
		RULE_statement = 0, RULE_database = 1, RULE_query = 2, RULE_where = 3, 
		RULE_whereClause = 4, RULE_simpleWhereClause = 5, RULE_operator = 6, RULE_options = 7, 
		RULE_sortBy = 8;
	public static final String[] ruleNames = {
		"statement", "database", "query", "where", "whereClause", "simpleWhereClause", 
		"operator", "options", "sortBy"
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
				if (_la==3) {
					{
					setState(22); match(3);
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

			setState(41);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==SORT_BY) {
				{
				{
				setState(38); options();
				}
				}
				setState(43);
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
			setState(44); match(WHERE);
			setState(45); whereClause(0);
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
			setState(53);
			switch (_input.LA(1)) {
			case 2:
				{
				setState(48); match(2);
				setState(49); whereClause(0);
				setState(50); match(1);
				}
				break;
			case STRING:
				{
				setState(52); simpleWhereClause();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			_ctx.stop = _input.LT(-1);
			setState(63);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,7,_ctx);
			while ( _alt!=2 && _alt!=-1 ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(61);
					switch ( getInterpreter().adaptivePredict(_input,6,_ctx) ) {
					case 1:
						{
						_localctx = new WhereClauseContext(_parentctx, _parentState, _p);
						pushNewRecursionContext(_localctx, _startState, RULE_whereClause);
						setState(55);
						if (!(3 >= _localctx._p)) throw new FailedPredicateException(this, "3 >= $_p");
						setState(56); match(AND);
						setState(57); whereClause(4);
						}
						break;

					case 2:
						{
						_localctx = new WhereClauseContext(_parentctx, _parentState, _p);
						pushNewRecursionContext(_localctx, _startState, RULE_whereClause);
						setState(58);
						if (!(2 >= _localctx._p)) throw new FailedPredicateException(this, "2 >= $_p");
						setState(59); match(OR);
						setState(60); whereClause(3);
						}
						break;
					}
					} 
				}
				setState(65);
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
			setState(66); match(STRING);
			setState(67); operator();
			setState(68); match(STRING);
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
			setState(70);
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
		enterRule(_localctx, 14, RULE_options);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(72); sortBy();
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
		enterRule(_localctx, 16, RULE_sortBy);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(74); match(SORT_BY);
			setState(75); match(STRING);
			setState(77);
			_la = _input.LA(1);
			if (_la==SORT_DIRECTION) {
				{
				setState(76); match(SORT_DIRECTION);
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
		"\2\3\25R\4\2\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t"+
		"\t\4\n\t\n\3\2\3\2\5\2\27\n\2\3\2\5\2\32\n\2\6\2\34\n\2\r\2\16\2\35\3"+
		"\3\3\3\3\3\3\4\3\4\3\4\3\4\5\4\'\n\4\3\4\7\4*\n\4\f\4\16\4-\13\4\3\5\3"+
		"\5\3\5\3\6\3\6\3\6\3\6\3\6\3\6\5\68\n\6\3\6\3\6\3\6\3\6\3\6\3\6\7\6@\n"+
		"\6\f\6\16\6C\13\6\3\7\3\7\3\7\3\7\3\b\3\b\3\t\3\t\3\n\3\n\3\n\5\nP\n\n"+
		"\3\n\2\13\2\4\6\b\n\f\16\20\22\2\3\3\b\rQ\2\33\3\2\2\2\4\37\3\2\2\2\6"+
		"\"\3\2\2\2\b.\3\2\2\2\n\67\3\2\2\2\fD\3\2\2\2\16H\3\2\2\2\20J\3\2\2\2"+
		"\22L\3\2\2\2\24\27\5\6\4\2\25\27\5\4\3\2\26\24\3\2\2\2\26\25\3\2\2\2\27"+
		"\31\3\2\2\2\30\32\7\5\2\2\31\30\3\2\2\2\31\32\3\2\2\2\32\34\3\2\2\2\33"+
		"\26\3\2\2\2\34\35\3\2\2\2\35\33\3\2\2\2\35\36\3\2\2\2\36\3\3\2\2\2\37"+
		" \7\16\2\2 !\7\24\2\2!\5\3\2\2\2\"#\7\17\2\2#$\7\20\2\2$&\7\24\2\2%\'"+
		"\5\b\5\2&%\3\2\2\2&\'\3\2\2\2\'+\3\2\2\2(*\5\20\t\2)(\3\2\2\2*-\3\2\2"+
		"\2+)\3\2\2\2+,\3\2\2\2,\7\3\2\2\2-+\3\2\2\2./\7\21\2\2/\60\5\n\6\2\60"+
		"\t\3\2\2\2\61\62\b\6\1\2\62\63\7\4\2\2\63\64\5\n\6\2\64\65\7\3\2\2\65"+
		"8\3\2\2\2\668\5\f\7\2\67\61\3\2\2\2\67\66\3\2\2\28A\3\2\2\29:\6\6\2\3"+
		":;\7\6\2\2;@\5\n\6\2<=\6\6\3\3=>\7\7\2\2>@\5\n\6\2?9\3\2\2\2?<\3\2\2\2"+
		"@C\3\2\2\2A?\3\2\2\2AB\3\2\2\2B\13\3\2\2\2CA\3\2\2\2DE\7\24\2\2EF\5\16"+
		"\b\2FG\7\24\2\2G\r\3\2\2\2HI\t\2\2\2I\17\3\2\2\2JK\5\22\n\2K\21\3\2\2"+
		"\2LM\7\22\2\2MO\7\24\2\2NP\7\23\2\2ON\3\2\2\2OP\3\2\2\2P\23\3\2\2\2\13"+
		"\26\31\35&+\67?AO";
	public static final ATN _ATN =
		ATNSimulator.deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
	}
}