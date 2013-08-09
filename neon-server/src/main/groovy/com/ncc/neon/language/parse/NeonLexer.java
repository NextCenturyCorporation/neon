// Generated from Neon.g4 by ANTLR 4.0

package com.ncc.neon.language.parse;

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
		T__10=1, T__9=2, T__8=3, T__7=4, T__6=5, T__5=6, T__4=7, T__3=8, T__2=9, 
		T__1=10, T__0=11, ALL_FIELDS=12, AND=13, OR=14, GT=15, GTE=16, LT=17, 
		LTE=18, EQ=19, NE=20, USE=21, SELECT=22, FROM=23, WHERE=24, GROUP=25, 
		LIMIT=26, SORT=27, SORT_DIRECTION=28, STRING=29, WHITESPACE=30;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] tokenNames = {
		"<INVALID>",
		"'max'", "'avg'", "'last'", "'sum'", "')'", "','", "'min'", "'('", "'count'", 
		"';'", "'first'", "'*'", "AND", "OR", "'>'", "'>='", "'<'", "'<='", "'='", 
		"'!='", "USE", "SELECT", "FROM", "WHERE", "GROUP", "LIMIT", "SORT", "SORT_DIRECTION", 
		"STRING", "WHITESPACE"
	};
	public static final String[] ruleNames = {
		"T__10", "T__9", "T__8", "T__7", "T__6", "T__5", "T__4", "T__3", "T__2", 
		"T__1", "T__0", "ALL_FIELDS", "AND", "OR", "GT", "GTE", "LT", "LTE", "EQ", 
		"NE", "USE", "SELECT", "FROM", "WHERE", "GROUP", "LIMIT", "SORT", "SORT_DIRECTION", 
		"STRING", "CHAR", "WHITESPACE"
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
		case 30: WHITESPACE_action((RuleContext)_localctx, actionIndex); break;
		}
	}
	private void WHITESPACE_action(RuleContext _localctx, int actionIndex) {
		switch (actionIndex) {
		case 0: skip();  break;
		}
	}

	public static final String _serializedATN =
		"\2\4 \u00f5\b\1\4\2\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t"+
		"\b\4\t\t\t\4\n\t\n\4\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20"+
		"\t\20\4\21\t\21\4\22\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27"+
		"\t\27\4\30\t\30\4\31\t\31\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36"+
		"\t\36\4\37\t\37\4 \t \3\2\3\2\3\2\3\2\3\3\3\3\3\3\3\3\3\4\3\4\3\4\3\4"+
		"\3\4\3\5\3\5\3\5\3\5\3\6\3\6\3\7\3\7\3\b\3\b\3\b\3\b\3\t\3\t\3\n\3\n\3"+
		"\n\3\n\3\n\3\n\3\13\3\13\3\f\3\f\3\f\3\f\3\f\3\f\3\r\3\r\3\16\3\16\3\16"+
		"\3\16\3\16\3\16\5\16s\n\16\3\17\3\17\3\17\3\17\5\17y\n\17\3\20\3\20\3"+
		"\21\3\21\3\21\3\22\3\22\3\23\3\23\3\23\3\24\3\24\3\25\3\25\3\25\3\26\3"+
		"\26\3\26\3\26\3\26\3\26\5\26\u0090\n\26\3\27\3\27\3\27\3\27\3\27\3\27"+
		"\3\27\3\27\3\27\3\27\3\27\3\27\5\27\u009e\n\27\3\30\3\30\3\30\3\30\3\30"+
		"\3\30\3\30\3\30\5\30\u00a8\n\30\3\31\3\31\3\31\3\31\3\31\3\31\3\31\3\31"+
		"\3\31\3\31\5\31\u00b4\n\31\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32"+
		"\3\32\5\32\u00c0\n\32\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33"+
		"\5\33\u00cc\n\33\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\5\34\u00d6\n"+
		"\34\3\35\3\35\3\35\3\35\3\35\3\35\3\35\3\35\3\35\3\35\3\35\3\35\3\35\3"+
		"\35\5\35\u00e6\n\35\3\36\6\36\u00e9\n\36\r\36\16\36\u00ea\3\37\3\37\3"+
		" \6 \u00f0\n \r \16 \u00f1\3 \3 \2!\3\3\1\5\4\1\7\5\1\t\6\1\13\7\1\r\b"+
		"\1\17\t\1\21\n\1\23\13\1\25\f\1\27\r\1\31\16\1\33\17\1\35\20\1\37\21\1"+
		"!\22\1#\23\1%\24\1\'\25\1)\26\1+\27\1-\30\1/\31\1\61\32\1\63\33\1\65\34"+
		"\1\67\35\19\36\1;\37\1=\2\1? \2\3\2\4\b$$/\60\62;C\\aac|\5\13\f\17\17"+
		"\"\"\u0101\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2"+
		"\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27"+
		"\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2"+
		"\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3\2\2\2\2)\3\2\2\2\2+\3\2\2\2\2-\3\2\2\2"+
		"\2/\3\2\2\2\2\61\3\2\2\2\2\63\3\2\2\2\2\65\3\2\2\2\2\67\3\2\2\2\29\3\2"+
		"\2\2\2;\3\2\2\2\2?\3\2\2\2\3A\3\2\2\2\5E\3\2\2\2\7I\3\2\2\2\tN\3\2\2\2"+
		"\13R\3\2\2\2\rT\3\2\2\2\17V\3\2\2\2\21Z\3\2\2\2\23\\\3\2\2\2\25b\3\2\2"+
		"\2\27d\3\2\2\2\31j\3\2\2\2\33r\3\2\2\2\35x\3\2\2\2\37z\3\2\2\2!|\3\2\2"+
		"\2#\177\3\2\2\2%\u0081\3\2\2\2\'\u0084\3\2\2\2)\u0086\3\2\2\2+\u008f\3"+
		"\2\2\2-\u009d\3\2\2\2/\u00a7\3\2\2\2\61\u00b3\3\2\2\2\63\u00bf\3\2\2\2"+
		"\65\u00cb\3\2\2\2\67\u00d5\3\2\2\29\u00e5\3\2\2\2;\u00e8\3\2\2\2=\u00ec"+
		"\3\2\2\2?\u00ef\3\2\2\2AB\7o\2\2BC\7c\2\2CD\7z\2\2D\4\3\2\2\2EF\7c\2\2"+
		"FG\7x\2\2GH\7i\2\2H\6\3\2\2\2IJ\7n\2\2JK\7c\2\2KL\7u\2\2LM\7v\2\2M\b\3"+
		"\2\2\2NO\7u\2\2OP\7w\2\2PQ\7o\2\2Q\n\3\2\2\2RS\7+\2\2S\f\3\2\2\2TU\7."+
		"\2\2U\16\3\2\2\2VW\7o\2\2WX\7k\2\2XY\7p\2\2Y\20\3\2\2\2Z[\7*\2\2[\22\3"+
		"\2\2\2\\]\7e\2\2]^\7q\2\2^_\7w\2\2_`\7p\2\2`a\7v\2\2a\24\3\2\2\2bc\7="+
		"\2\2c\26\3\2\2\2de\7h\2\2ef\7k\2\2fg\7t\2\2gh\7u\2\2hi\7v\2\2i\30\3\2"+
		"\2\2jk\7,\2\2k\32\3\2\2\2lm\7C\2\2mn\7P\2\2ns\7F\2\2op\7c\2\2pq\7p\2\2"+
		"qs\7f\2\2rl\3\2\2\2ro\3\2\2\2s\34\3\2\2\2tu\7Q\2\2uy\7T\2\2vw\7q\2\2w"+
		"y\7t\2\2xt\3\2\2\2xv\3\2\2\2y\36\3\2\2\2z{\7@\2\2{ \3\2\2\2|}\7@\2\2}"+
		"~\7?\2\2~\"\3\2\2\2\177\u0080\7>\2\2\u0080$\3\2\2\2\u0081\u0082\7>\2\2"+
		"\u0082\u0083\7?\2\2\u0083&\3\2\2\2\u0084\u0085\7?\2\2\u0085(\3\2\2\2\u0086"+
		"\u0087\7#\2\2\u0087\u0088\7?\2\2\u0088*\3\2\2\2\u0089\u008a\7W\2\2\u008a"+
		"\u008b\7U\2\2\u008b\u0090\7G\2\2\u008c\u008d\7w\2\2\u008d\u008e\7u\2\2"+
		"\u008e\u0090\7g\2\2\u008f\u0089\3\2\2\2\u008f\u008c\3\2\2\2\u0090,\3\2"+
		"\2\2\u0091\u0092\7U\2\2\u0092\u0093\7G\2\2\u0093\u0094\7N\2\2\u0094\u0095"+
		"\7G\2\2\u0095\u0096\7E\2\2\u0096\u009e\7V\2\2\u0097\u0098\7u\2\2\u0098"+
		"\u0099\7g\2\2\u0099\u009a\7n\2\2\u009a\u009b\7g\2\2\u009b\u009c\7e\2\2"+
		"\u009c\u009e\7v\2\2\u009d\u0091\3\2\2\2\u009d\u0097\3\2\2\2\u009e.\3\2"+
		"\2\2\u009f\u00a0\7H\2\2\u00a0\u00a1\7T\2\2\u00a1\u00a2\7Q\2\2\u00a2\u00a8"+
		"\7O\2\2\u00a3\u00a4\7h\2\2\u00a4\u00a5\7t\2\2\u00a5\u00a6\7q\2\2\u00a6"+
		"\u00a8\7o\2\2\u00a7\u009f\3\2\2\2\u00a7\u00a3\3\2\2\2\u00a8\60\3\2\2\2"+
		"\u00a9\u00aa\7Y\2\2\u00aa\u00ab\7J\2\2\u00ab\u00ac\7G\2\2\u00ac\u00ad"+
		"\7T\2\2\u00ad\u00b4\7G\2\2\u00ae\u00af\7y\2\2\u00af\u00b0\7j\2\2\u00b0"+
		"\u00b1\7g\2\2\u00b1\u00b2\7t\2\2\u00b2\u00b4\7g\2\2\u00b3\u00a9\3\2\2"+
		"\2\u00b3\u00ae\3\2\2\2\u00b4\62\3\2\2\2\u00b5\u00b6\7I\2\2\u00b6\u00b7"+
		"\7T\2\2\u00b7\u00b8\7Q\2\2\u00b8\u00b9\7W\2\2\u00b9\u00c0\7R\2\2\u00ba"+
		"\u00bb\7i\2\2\u00bb\u00bc\7t\2\2\u00bc\u00bd\7q\2\2\u00bd\u00be\7w\2\2"+
		"\u00be\u00c0\7r\2\2\u00bf\u00b5\3\2\2\2\u00bf\u00ba\3\2\2\2\u00c0\64\3"+
		"\2\2\2\u00c1\u00c2\7N\2\2\u00c2\u00c3\7K\2\2\u00c3\u00c4\7O\2\2\u00c4"+
		"\u00c5\7K\2\2\u00c5\u00cc\7V\2\2\u00c6\u00c7\7n\2\2\u00c7\u00c8\7k\2\2"+
		"\u00c8\u00c9\7o\2\2\u00c9\u00ca\7k\2\2\u00ca\u00cc\7v\2\2\u00cb\u00c1"+
		"\3\2\2\2\u00cb\u00c6\3\2\2\2\u00cc\66\3\2\2\2\u00cd\u00ce\7U\2\2\u00ce"+
		"\u00cf\7Q\2\2\u00cf\u00d0\7T\2\2\u00d0\u00d6\7V\2\2\u00d1\u00d2\7u\2\2"+
		"\u00d2\u00d3\7q\2\2\u00d3\u00d4\7t\2\2\u00d4\u00d6\7v\2\2\u00d5\u00cd"+
		"\3\2\2\2\u00d5\u00d1\3\2\2\2\u00d68\3\2\2\2\u00d7\u00d8\7C\2\2\u00d8\u00d9"+
		"\7U\2\2\u00d9\u00e6\7E\2\2\u00da\u00db\7c\2\2\u00db\u00dc\7u\2\2\u00dc"+
		"\u00e6\7e\2\2\u00dd\u00de\7F\2\2\u00de\u00df\7G\2\2\u00df\u00e0\7U\2\2"+
		"\u00e0\u00e6\7E\2\2\u00e1\u00e2\7f\2\2\u00e2\u00e3\7g\2\2\u00e3\u00e4"+
		"\7u\2\2\u00e4\u00e6\7e\2\2\u00e5\u00d7\3\2\2\2\u00e5\u00da\3\2\2\2\u00e5"+
		"\u00dd\3\2\2\2\u00e5\u00e1\3\2\2\2\u00e6:\3\2\2\2\u00e7\u00e9\5=\37\2"+
		"\u00e8\u00e7\3\2\2\2\u00e9\u00ea\3\2\2\2\u00ea\u00e8\3\2\2\2\u00ea\u00eb"+
		"\3\2\2\2\u00eb<\3\2\2\2\u00ec\u00ed\t\2\2\2\u00ed>\3\2\2\2\u00ee\u00f0"+
		"\t\3\2\2\u00ef\u00ee\3\2\2\2\u00f0\u00f1\3\2\2\2\u00f1\u00ef\3\2\2\2\u00f1"+
		"\u00f2\3\2\2\2\u00f2\u00f3\3\2\2\2\u00f3\u00f4\b \2\2\u00f4@\3\2\2\2\17"+
		"\2rx\u008f\u009d\u00a7\u00b3\u00bf\u00cb\u00d5\u00e5\u00ea\u00f1";
	public static final ATN _ATN =
		ATNSimulator.deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
	}
}