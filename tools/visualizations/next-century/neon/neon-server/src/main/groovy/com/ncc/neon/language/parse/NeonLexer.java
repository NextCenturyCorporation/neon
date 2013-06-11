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
		T__12=1, T__11=2, T__10=3, T__9=4, T__8=5, T__7=6, T__6=7, T__5=8, T__4=9, 
		T__3=10, T__2=11, T__1=12, T__0=13, AND=14, OR=15, GT=16, GTE=17, LT=18, 
		LTE=19, EQ=20, NE=21, USE=22, SELECT=23, FROM=24, WHERE=25, GROUP=26, 
		LIMIT=27, SORT=28, SORT_DIRECTION=29, STRING=30, WHITESPACE=31;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] tokenNames = {
		"<INVALID>",
		"'avg'", "'max'", "'last'", "')'", "','", "'min'", "'push'", "'('", "'addToSet'", 
		"';'", "'first'", "'sum'", "'count'", "AND", "OR", "'>'", "'>='", "'<'", 
		"'<='", "'='", "'!='", "USE", "SELECT", "FROM", "WHERE", "GROUP", "LIMIT", 
		"SORT", "SORT_DIRECTION", "STRING", "WHITESPACE"
	};
	public static final String[] ruleNames = {
		"T__12", "T__11", "T__10", "T__9", "T__8", "T__7", "T__6", "T__5", "T__4", 
		"T__3", "T__2", "T__1", "T__0", "AND", "OR", "GT", "GTE", "LT", "LTE", 
		"EQ", "NE", "USE", "SELECT", "FROM", "WHERE", "GROUP", "LIMIT", "SORT", 
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
		case 31: WHITESPACE_action((RuleContext)_localctx, actionIndex); break;
		}
	}
	private void WHITESPACE_action(RuleContext _localctx, int actionIndex) {
		switch (actionIndex) {
		case 0: skip();  break;
		}
	}

	public static final String _serializedATN =
		"\2\4!\u0103\b\1\4\2\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t"+
		"\b\4\t\t\t\4\n\t\n\4\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20"+
		"\t\20\4\21\t\21\4\22\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27"+
		"\t\27\4\30\t\30\4\31\t\31\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36"+
		"\t\36\4\37\t\37\4 \t \4!\t!\3\2\3\2\3\2\3\2\3\3\3\3\3\3\3\3\3\4\3\4\3"+
		"\4\3\4\3\4\3\5\3\5\3\6\3\6\3\7\3\7\3\7\3\7\3\b\3\b\3\b\3\b\3\b\3\t\3\t"+
		"\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\13\3\13\3\f\3\f\3\f\3\f\3\f\3\f"+
		"\3\r\3\r\3\r\3\r\3\16\3\16\3\16\3\16\3\16\3\16\3\17\3\17\3\17\3\17\3\17"+
		"\3\17\5\17\u0081\n\17\3\20\3\20\3\20\3\20\5\20\u0087\n\20\3\21\3\21\3"+
		"\22\3\22\3\22\3\23\3\23\3\24\3\24\3\24\3\25\3\25\3\26\3\26\3\26\3\27\3"+
		"\27\3\27\3\27\3\27\3\27\5\27\u009e\n\27\3\30\3\30\3\30\3\30\3\30\3\30"+
		"\3\30\3\30\3\30\3\30\3\30\3\30\5\30\u00ac\n\30\3\31\3\31\3\31\3\31\3\31"+
		"\3\31\3\31\3\31\5\31\u00b6\n\31\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32"+
		"\3\32\3\32\5\32\u00c2\n\32\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33"+
		"\3\33\5\33\u00ce\n\33\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34"+
		"\5\34\u00da\n\34\3\35\3\35\3\35\3\35\3\35\3\35\3\35\3\35\5\35\u00e4\n"+
		"\35\3\36\3\36\3\36\3\36\3\36\3\36\3\36\3\36\3\36\3\36\3\36\3\36\3\36\3"+
		"\36\5\36\u00f4\n\36\3\37\6\37\u00f7\n\37\r\37\16\37\u00f8\3 \3 \3!\6!"+
		"\u00fe\n!\r!\16!\u00ff\3!\3!\2\"\3\3\1\5\4\1\7\5\1\t\6\1\13\7\1\r\b\1"+
		"\17\t\1\21\n\1\23\13\1\25\f\1\27\r\1\31\16\1\33\17\1\35\20\1\37\21\1!"+
		"\22\1#\23\1%\24\1\'\25\1)\26\1+\27\1-\30\1/\31\1\61\32\1\63\33\1\65\34"+
		"\1\67\35\19\36\1;\37\1= \1?\2\1A!\2\3\2\4\7/\60\62;C\\aac|\5\13\f\17\17"+
		"\"\"\u010f\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2"+
		"\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27"+
		"\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2"+
		"\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3\2\2\2\2)\3\2\2\2\2+\3\2\2\2\2-\3\2\2\2"+
		"\2/\3\2\2\2\2\61\3\2\2\2\2\63\3\2\2\2\2\65\3\2\2\2\2\67\3\2\2\2\29\3\2"+
		"\2\2\2;\3\2\2\2\2=\3\2\2\2\2A\3\2\2\2\3C\3\2\2\2\5G\3\2\2\2\7K\3\2\2\2"+
		"\tP\3\2\2\2\13R\3\2\2\2\rT\3\2\2\2\17X\3\2\2\2\21]\3\2\2\2\23_\3\2\2\2"+
		"\25h\3\2\2\2\27j\3\2\2\2\31p\3\2\2\2\33t\3\2\2\2\35\u0080\3\2\2\2\37\u0086"+
		"\3\2\2\2!\u0088\3\2\2\2#\u008a\3\2\2\2%\u008d\3\2\2\2\'\u008f\3\2\2\2"+
		")\u0092\3\2\2\2+\u0094\3\2\2\2-\u009d\3\2\2\2/\u00ab\3\2\2\2\61\u00b5"+
		"\3\2\2\2\63\u00c1\3\2\2\2\65\u00cd\3\2\2\2\67\u00d9\3\2\2\29\u00e3\3\2"+
		"\2\2;\u00f3\3\2\2\2=\u00f6\3\2\2\2?\u00fa\3\2\2\2A\u00fd\3\2\2\2CD\7c"+
		"\2\2DE\7x\2\2EF\7i\2\2F\4\3\2\2\2GH\7o\2\2HI\7c\2\2IJ\7z\2\2J\6\3\2\2"+
		"\2KL\7n\2\2LM\7c\2\2MN\7u\2\2NO\7v\2\2O\b\3\2\2\2PQ\7+\2\2Q\n\3\2\2\2"+
		"RS\7.\2\2S\f\3\2\2\2TU\7o\2\2UV\7k\2\2VW\7p\2\2W\16\3\2\2\2XY\7r\2\2Y"+
		"Z\7w\2\2Z[\7u\2\2[\\\7j\2\2\\\20\3\2\2\2]^\7*\2\2^\22\3\2\2\2_`\7c\2\2"+
		"`a\7f\2\2ab\7f\2\2bc\7V\2\2cd\7q\2\2de\7U\2\2ef\7g\2\2fg\7v\2\2g\24\3"+
		"\2\2\2hi\7=\2\2i\26\3\2\2\2jk\7h\2\2kl\7k\2\2lm\7t\2\2mn\7u\2\2no\7v\2"+
		"\2o\30\3\2\2\2pq\7u\2\2qr\7w\2\2rs\7o\2\2s\32\3\2\2\2tu\7e\2\2uv\7q\2"+
		"\2vw\7w\2\2wx\7p\2\2xy\7v\2\2y\34\3\2\2\2z{\7C\2\2{|\7P\2\2|\u0081\7F"+
		"\2\2}~\7c\2\2~\177\7p\2\2\177\u0081\7f\2\2\u0080z\3\2\2\2\u0080}\3\2\2"+
		"\2\u0081\36\3\2\2\2\u0082\u0083\7Q\2\2\u0083\u0087\7T\2\2\u0084\u0085"+
		"\7q\2\2\u0085\u0087\7t\2\2\u0086\u0082\3\2\2\2\u0086\u0084\3\2\2\2\u0087"+
		" \3\2\2\2\u0088\u0089\7@\2\2\u0089\"\3\2\2\2\u008a\u008b\7@\2\2\u008b"+
		"\u008c\7?\2\2\u008c$\3\2\2\2\u008d\u008e\7>\2\2\u008e&\3\2\2\2\u008f\u0090"+
		"\7>\2\2\u0090\u0091\7?\2\2\u0091(\3\2\2\2\u0092\u0093\7?\2\2\u0093*\3"+
		"\2\2\2\u0094\u0095\7#\2\2\u0095\u0096\7?\2\2\u0096,\3\2\2\2\u0097\u0098"+
		"\7W\2\2\u0098\u0099\7U\2\2\u0099\u009e\7G\2\2\u009a\u009b\7w\2\2\u009b"+
		"\u009c\7u\2\2\u009c\u009e\7g\2\2\u009d\u0097\3\2\2\2\u009d\u009a\3\2\2"+
		"\2\u009e.\3\2\2\2\u009f\u00a0\7U\2\2\u00a0\u00a1\7G\2\2\u00a1\u00a2\7"+
		"N\2\2\u00a2\u00a3\7G\2\2\u00a3\u00a4\7E\2\2\u00a4\u00ac\7V\2\2\u00a5\u00a6"+
		"\7u\2\2\u00a6\u00a7\7g\2\2\u00a7\u00a8\7n\2\2\u00a8\u00a9\7g\2\2\u00a9"+
		"\u00aa\7e\2\2\u00aa\u00ac\7v\2\2\u00ab\u009f\3\2\2\2\u00ab\u00a5\3\2\2"+
		"\2\u00ac\60\3\2\2\2\u00ad\u00ae\7H\2\2\u00ae\u00af\7T\2\2\u00af\u00b0"+
		"\7Q\2\2\u00b0\u00b6\7O\2\2\u00b1\u00b2\7h\2\2\u00b2\u00b3\7t\2\2\u00b3"+
		"\u00b4\7q\2\2\u00b4\u00b6\7o\2\2\u00b5\u00ad\3\2\2\2\u00b5\u00b1\3\2\2"+
		"\2\u00b6\62\3\2\2\2\u00b7\u00b8\7Y\2\2\u00b8\u00b9\7J\2\2\u00b9\u00ba"+
		"\7G\2\2\u00ba\u00bb\7T\2\2\u00bb\u00c2\7G\2\2\u00bc\u00bd\7y\2\2\u00bd"+
		"\u00be\7j\2\2\u00be\u00bf\7g\2\2\u00bf\u00c0\7t\2\2\u00c0\u00c2\7g\2\2"+
		"\u00c1\u00b7\3\2\2\2\u00c1\u00bc\3\2\2\2\u00c2\64\3\2\2\2\u00c3\u00c4"+
		"\7I\2\2\u00c4\u00c5\7T\2\2\u00c5\u00c6\7Q\2\2\u00c6\u00c7\7W\2\2\u00c7"+
		"\u00ce\7R\2\2\u00c8\u00c9\7i\2\2\u00c9\u00ca\7t\2\2\u00ca\u00cb\7q\2\2"+
		"\u00cb\u00cc\7w\2\2\u00cc\u00ce\7r\2\2\u00cd\u00c3\3\2\2\2\u00cd\u00c8"+
		"\3\2\2\2\u00ce\66\3\2\2\2\u00cf\u00d0\7N\2\2\u00d0\u00d1\7K\2\2\u00d1"+
		"\u00d2\7O\2\2\u00d2\u00d3\7K\2\2\u00d3\u00da\7V\2\2\u00d4\u00d5\7n\2\2"+
		"\u00d5\u00d6\7k\2\2\u00d6\u00d7\7o\2\2\u00d7\u00d8\7k\2\2\u00d8\u00da"+
		"\7v\2\2\u00d9\u00cf\3\2\2\2\u00d9\u00d4\3\2\2\2\u00da8\3\2\2\2\u00db\u00dc"+
		"\7U\2\2\u00dc\u00dd\7Q\2\2\u00dd\u00de\7T\2\2\u00de\u00e4\7V\2\2\u00df"+
		"\u00e0\7u\2\2\u00e0\u00e1\7q\2\2\u00e1\u00e2\7t\2\2\u00e2\u00e4\7v\2\2"+
		"\u00e3\u00db\3\2\2\2\u00e3\u00df\3\2\2\2\u00e4:\3\2\2\2\u00e5\u00e6\7"+
		"C\2\2\u00e6\u00e7\7U\2\2\u00e7\u00f4\7E\2\2\u00e8\u00e9\7c\2\2\u00e9\u00ea"+
		"\7u\2\2\u00ea\u00f4\7e\2\2\u00eb\u00ec\7F\2\2\u00ec\u00ed\7G\2\2\u00ed"+
		"\u00ee\7U\2\2\u00ee\u00f4\7E\2\2\u00ef\u00f0\7f\2\2\u00f0\u00f1\7g\2\2"+
		"\u00f1\u00f2\7u\2\2\u00f2\u00f4\7e\2\2\u00f3\u00e5\3\2\2\2\u00f3\u00e8"+
		"\3\2\2\2\u00f3\u00eb\3\2\2\2\u00f3\u00ef\3\2\2\2\u00f4<\3\2\2\2\u00f5"+
		"\u00f7\5? \2\u00f6\u00f5\3\2\2\2\u00f7\u00f8\3\2\2\2\u00f8\u00f6\3\2\2"+
		"\2\u00f8\u00f9\3\2\2\2\u00f9>\3\2\2\2\u00fa\u00fb\t\2\2\2\u00fb@\3\2\2"+
		"\2\u00fc\u00fe\t\3\2\2\u00fd\u00fc\3\2\2\2\u00fe\u00ff\3\2\2\2\u00ff\u00fd"+
		"\3\2\2\2\u00ff\u0100\3\2\2\2\u0100\u0101\3\2\2\2\u0101\u0102\b!\2\2\u0102"+
		"B\3\2\2\2\17\2\u0080\u0086\u009d\u00ab\u00b5\u00c1\u00cd\u00d9\u00e3\u00f3"+
		"\u00f8\u00ff";
	public static final ATN _ATN =
		ATNSimulator.deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
	}
}