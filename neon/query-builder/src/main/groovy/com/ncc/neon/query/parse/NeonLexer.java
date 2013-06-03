// Generated from Neon.g4 by ANTLR 4.0

package com.ncc.neon.query.parse;

import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class NeonLexer extends Lexer {
	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__4=1, T__3=2, T__2=3, T__1=4, T__0=5, GT=6, GTE=7, LT=8, LTE=9, EQ=10, 
		NE=11, LPAREN=12, RPAREN=13, USE=14, SELECT=15, FROM=16, WHERE=17, SORT_BY=18, 
		SORT_DIRECTION=19, STRING=20, WHITESPACE=21;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] tokenNames = {
		"<INVALID>",
		"'AND'", "'and'", "'OR'", "'or'", "';'", "'>'", "'>='", "'<'", "'<='", 
		"'='", "'!='", "'('", "')'", "USE", "SELECT", "FROM", "WHERE", "SORT_BY", 
		"SORT_DIRECTION", "STRING", "WHITESPACE"
	};
	public static final String[] ruleNames = {
		"T__4", "T__3", "T__2", "T__1", "T__0", "GT", "GTE", "LT", "LTE", "EQ", 
		"NE", "LPAREN", "RPAREN", "USE", "SELECT", "FROM", "WHERE", "SORT_BY", 
		"SORT_DIRECTION", "STRING", "CHAR", "WHITESPACE"
	};


	public NeonLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "Neon.g4"; }

	@Override
	public String[] getTokenNames() { return tokenNames; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	@Override
	public void action(RuleContext _localctx, int ruleIndex, int actionIndex) {
		switch (ruleIndex) {
		case 21: WHITESPACE_action((RuleContext)_localctx, actionIndex); break;
		}
	}
	private void WHITESPACE_action(RuleContext _localctx, int actionIndex) {
		switch (actionIndex) {
		case 0: skip();  break;
		}
	}

	public static final String _serializedATN =
		"\2\4\27\u00a6\b\1\4\2\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b"+
		"\t\b\4\t\t\t\4\n\t\n\4\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20"+
		"\t\20\4\21\t\21\4\22\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27"+
		"\t\27\3\2\3\2\3\2\3\2\3\3\3\3\3\3\3\3\3\4\3\4\3\4\3\5\3\5\3\5\3\6\3\6"+
		"\3\7\3\7\3\b\3\b\3\b\3\t\3\t\3\n\3\n\3\n\3\13\3\13\3\f\3\f\3\f\3\r\3\r"+
		"\3\16\3\16\3\17\3\17\3\17\3\17\3\17\3\17\5\17Y\n\17\3\20\3\20\3\20\3\20"+
		"\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\5\20g\n\20\3\21\3\21\3\21\3\21"+
		"\3\21\3\21\3\21\3\21\5\21q\n\21\3\22\3\22\3\22\3\22\3\22\3\22\3\22\3\22"+
		"\3\22\3\22\5\22}\n\22\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\5\23\u0087"+
		"\n\23\3\24\3\24\3\24\3\24\3\24\3\24\3\24\3\24\3\24\3\24\3\24\3\24\3\24"+
		"\3\24\5\24\u0097\n\24\3\25\6\25\u009a\n\25\r\25\16\25\u009b\3\26\3\26"+
		"\3\27\6\27\u00a1\n\27\r\27\16\27\u00a2\3\27\3\27\2\30\3\3\1\5\4\1\7\5"+
		"\1\t\6\1\13\7\1\r\b\1\17\t\1\21\n\1\23\13\1\25\f\1\27\r\1\31\16\1\33\17"+
		"\1\35\20\1\37\21\1!\22\1#\23\1%\24\1\'\25\1)\26\1+\2\1-\27\2\3\2\4\7/"+
		"/\62;C\\aac|\5\13\f\17\17\"\"\u00ae\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2"+
		"\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23"+
		"\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2"+
		"\2\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3\2\2\2\2)\3\2"+
		"\2\2\2-\3\2\2\2\3/\3\2\2\2\5\63\3\2\2\2\7\67\3\2\2\2\t:\3\2\2\2\13=\3"+
		"\2\2\2\r?\3\2\2\2\17A\3\2\2\2\21D\3\2\2\2\23F\3\2\2\2\25I\3\2\2\2\27K"+
		"\3\2\2\2\31N\3\2\2\2\33P\3\2\2\2\35X\3\2\2\2\37f\3\2\2\2!p\3\2\2\2#|\3"+
		"\2\2\2%\u0086\3\2\2\2\'\u0096\3\2\2\2)\u0099\3\2\2\2+\u009d\3\2\2\2-\u00a0"+
		"\3\2\2\2/\60\7C\2\2\60\61\7P\2\2\61\62\7F\2\2\62\4\3\2\2\2\63\64\7c\2"+
		"\2\64\65\7p\2\2\65\66\7f\2\2\66\6\3\2\2\2\678\7Q\2\289\7T\2\29\b\3\2\2"+
		"\2:;\7q\2\2;<\7t\2\2<\n\3\2\2\2=>\7=\2\2>\f\3\2\2\2?@\7@\2\2@\16\3\2\2"+
		"\2AB\7@\2\2BC\7?\2\2C\20\3\2\2\2DE\7>\2\2E\22\3\2\2\2FG\7>\2\2GH\7?\2"+
		"\2H\24\3\2\2\2IJ\7?\2\2J\26\3\2\2\2KL\7#\2\2LM\7?\2\2M\30\3\2\2\2NO\7"+
		"*\2\2O\32\3\2\2\2PQ\7+\2\2Q\34\3\2\2\2RS\7W\2\2ST\7U\2\2TY\7G\2\2UV\7"+
		"w\2\2VW\7u\2\2WY\7g\2\2XR\3\2\2\2XU\3\2\2\2Y\36\3\2\2\2Z[\7U\2\2[\\\7"+
		"G\2\2\\]\7N\2\2]^\7G\2\2^_\7E\2\2_g\7V\2\2`a\7u\2\2ab\7g\2\2bc\7n\2\2"+
		"cd\7g\2\2de\7e\2\2eg\7v\2\2fZ\3\2\2\2f`\3\2\2\2g \3\2\2\2hi\7H\2\2ij\7"+
		"T\2\2jk\7Q\2\2kq\7O\2\2lm\7h\2\2mn\7t\2\2no\7q\2\2oq\7o\2\2ph\3\2\2\2"+
		"pl\3\2\2\2q\"\3\2\2\2rs\7Y\2\2st\7J\2\2tu\7G\2\2uv\7T\2\2v}\7G\2\2wx\7"+
		"y\2\2xy\7j\2\2yz\7g\2\2z{\7t\2\2{}\7g\2\2|r\3\2\2\2|w\3\2\2\2}$\3\2\2"+
		"\2~\177\7U\2\2\177\u0080\7Q\2\2\u0080\u0081\7T\2\2\u0081\u0087\7V\2\2"+
		"\u0082\u0083\7u\2\2\u0083\u0084\7q\2\2\u0084\u0085\7t\2\2\u0085\u0087"+
		"\7v\2\2\u0086~\3\2\2\2\u0086\u0082\3\2\2\2\u0087&\3\2\2\2\u0088\u0089"+
		"\7C\2\2\u0089\u008a\7U\2\2\u008a\u0097\7E\2\2\u008b\u008c\7c\2\2\u008c"+
		"\u008d\7u\2\2\u008d\u0097\7e\2\2\u008e\u008f\7F\2\2\u008f\u0090\7G\2\2"+
		"\u0090\u0091\7U\2\2\u0091\u0097\7E\2\2\u0092\u0093\7f\2\2\u0093\u0094"+
		"\7g\2\2\u0094\u0095\7u\2\2\u0095\u0097\7e\2\2\u0096\u0088\3\2\2\2\u0096"+
		"\u008b\3\2\2\2\u0096\u008e\3\2\2\2\u0096\u0092\3\2\2\2\u0097(\3\2\2\2"+
		"\u0098\u009a\5+\26\2\u0099\u0098\3\2\2\2\u009a\u009b\3\2\2\2\u009b\u0099"+
		"\3\2\2\2\u009b\u009c\3\2\2\2\u009c*\3\2\2\2\u009d\u009e\t\2\2\2\u009e"+
		",\3\2\2\2\u009f\u00a1\t\3\2\2\u00a0\u009f\3\2\2\2\u00a1\u00a2\3\2\2\2"+
		"\u00a2\u00a0\3\2\2\2\u00a2\u00a3\3\2\2\2\u00a3\u00a4\3\2\2\2\u00a4\u00a5"+
		"\b\27\2\2\u00a5.\3\2\2\2\13\2Xfp|\u0086\u0096\u009b\u00a2";
	public static final ATN _ATN =
		ATNSimulator.deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
	}
}