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
		T__2=1, T__1=2, T__0=3, AND=4, OR=5, GT=6, GTE=7, LT=8, LTE=9, EQ=10, 
		NE=11, USE=12, SELECT=13, FROM=14, WHERE=15, SORT_BY=16, SORT_DIRECTION=17, 
		STRING=18, WHITESPACE=19;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] tokenNames = {
		"<INVALID>",
		"')'", "'('", "';'", "AND", "OR", "'>'", "'>='", "'<'", "'<='", "'='", 
		"'!='", "USE", "SELECT", "FROM", "WHERE", "SORT_BY", "SORT_DIRECTION", 
		"STRING", "WHITESPACE"
	};
	public static final String[] ruleNames = {
		"T__2", "T__1", "T__0", "AND", "OR", "GT", "GTE", "LT", "LTE", "EQ", "NE", 
		"USE", "SELECT", "FROM", "WHERE", "SORT_BY", "SORT_DIRECTION", "STRING", 
		"CHAR", "WHITESPACE"
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
		case 19: WHITESPACE_action((RuleContext)_localctx, actionIndex); break;
		}
	}
	private void WHITESPACE_action(RuleContext _localctx, int actionIndex) {
		switch (actionIndex) {
		case 0: skip();  break;
		}
	}

	public static final String _serializedATN =
		"\2\4\25\u00a2\b\1\4\2\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b"+
		"\t\b\4\t\t\t\4\n\t\n\4\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20"+
		"\t\20\4\21\t\21\4\22\t\22\4\23\t\23\4\24\t\24\4\25\t\25\3\2\3\2\3\3\3"+
		"\3\3\4\3\4\3\5\3\5\3\5\3\5\3\5\3\5\5\58\n\5\3\6\3\6\3\6\3\6\5\6>\n\6\3"+
		"\7\3\7\3\b\3\b\3\b\3\t\3\t\3\n\3\n\3\n\3\13\3\13\3\f\3\f\3\f\3\r\3\r\3"+
		"\r\3\r\3\r\3\r\5\rU\n\r\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3"+
		"\16\3\16\3\16\5\16c\n\16\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\5\17"+
		"m\n\17\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\5\20y\n\20\3"+
		"\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\5\21\u0083\n\21\3\22\3\22\3\22"+
		"\3\22\3\22\3\22\3\22\3\22\3\22\3\22\3\22\3\22\3\22\3\22\5\22\u0093\n\22"+
		"\3\23\6\23\u0096\n\23\r\23\16\23\u0097\3\24\3\24\3\25\6\25\u009d\n\25"+
		"\r\25\16\25\u009e\3\25\3\25\2\26\3\3\1\5\4\1\7\5\1\t\6\1\13\7\1\r\b\1"+
		"\17\t\1\21\n\1\23\13\1\25\f\1\27\r\1\31\16\1\33\17\1\35\20\1\37\21\1!"+
		"\22\1#\23\1%\24\1\'\2\1)\25\2\3\2\4\7//\62;C\\aac|\5\13\f\17\17\"\"\u00ac"+
		"\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2"+
		"\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2"+
		"\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2"+
		"\2\2\2%\3\2\2\2\2)\3\2\2\2\3+\3\2\2\2\5-\3\2\2\2\7/\3\2\2\2\t\67\3\2\2"+
		"\2\13=\3\2\2\2\r?\3\2\2\2\17A\3\2\2\2\21D\3\2\2\2\23F\3\2\2\2\25I\3\2"+
		"\2\2\27K\3\2\2\2\31T\3\2\2\2\33b\3\2\2\2\35l\3\2\2\2\37x\3\2\2\2!\u0082"+
		"\3\2\2\2#\u0092\3\2\2\2%\u0095\3\2\2\2\'\u0099\3\2\2\2)\u009c\3\2\2\2"+
		"+,\7+\2\2,\4\3\2\2\2-.\7*\2\2.\6\3\2\2\2/\60\7=\2\2\60\b\3\2\2\2\61\62"+
		"\7C\2\2\62\63\7P\2\2\638\7F\2\2\64\65\7c\2\2\65\66\7p\2\2\668\7f\2\2\67"+
		"\61\3\2\2\2\67\64\3\2\2\28\n\3\2\2\29:\7Q\2\2:>\7T\2\2;<\7q\2\2<>\7t\2"+
		"\2=9\3\2\2\2=;\3\2\2\2>\f\3\2\2\2?@\7@\2\2@\16\3\2\2\2AB\7@\2\2BC\7?\2"+
		"\2C\20\3\2\2\2DE\7>\2\2E\22\3\2\2\2FG\7>\2\2GH\7?\2\2H\24\3\2\2\2IJ\7"+
		"?\2\2J\26\3\2\2\2KL\7#\2\2LM\7?\2\2M\30\3\2\2\2NO\7W\2\2OP\7U\2\2PU\7"+
		"G\2\2QR\7w\2\2RS\7u\2\2SU\7g\2\2TN\3\2\2\2TQ\3\2\2\2U\32\3\2\2\2VW\7U"+
		"\2\2WX\7G\2\2XY\7N\2\2YZ\7G\2\2Z[\7E\2\2[c\7V\2\2\\]\7u\2\2]^\7g\2\2^"+
		"_\7n\2\2_`\7g\2\2`a\7e\2\2ac\7v\2\2bV\3\2\2\2b\\\3\2\2\2c\34\3\2\2\2d"+
		"e\7H\2\2ef\7T\2\2fg\7Q\2\2gm\7O\2\2hi\7h\2\2ij\7t\2\2jk\7q\2\2km\7o\2"+
		"\2ld\3\2\2\2lh\3\2\2\2m\36\3\2\2\2no\7Y\2\2op\7J\2\2pq\7G\2\2qr\7T\2\2"+
		"ry\7G\2\2st\7y\2\2tu\7j\2\2uv\7g\2\2vw\7t\2\2wy\7g\2\2xn\3\2\2\2xs\3\2"+
		"\2\2y \3\2\2\2z{\7U\2\2{|\7Q\2\2|}\7T\2\2}\u0083\7V\2\2~\177\7u\2\2\177"+
		"\u0080\7q\2\2\u0080\u0081\7t\2\2\u0081\u0083\7v\2\2\u0082z\3\2\2\2\u0082"+
		"~\3\2\2\2\u0083\"\3\2\2\2\u0084\u0085\7C\2\2\u0085\u0086\7U\2\2\u0086"+
		"\u0093\7E\2\2\u0087\u0088\7c\2\2\u0088\u0089\7u\2\2\u0089\u0093\7e\2\2"+
		"\u008a\u008b\7F\2\2\u008b\u008c\7G\2\2\u008c\u008d\7U\2\2\u008d\u0093"+
		"\7E\2\2\u008e\u008f\7f\2\2\u008f\u0090\7g\2\2\u0090\u0091\7u\2\2\u0091"+
		"\u0093\7e\2\2\u0092\u0084\3\2\2\2\u0092\u0087\3\2\2\2\u0092\u008a\3\2"+
		"\2\2\u0092\u008e\3\2\2\2\u0093$\3\2\2\2\u0094\u0096\5\'\24\2\u0095\u0094"+
		"\3\2\2\2\u0096\u0097\3\2\2\2\u0097\u0095\3\2\2\2\u0097\u0098\3\2\2\2\u0098"+
		"&\3\2\2\2\u0099\u009a\t\2\2\2\u009a(\3\2\2\2\u009b\u009d\t\3\2\2\u009c"+
		"\u009b\3\2\2\2\u009d\u009e\3\2\2\2\u009e\u009c\3\2\2\2\u009e\u009f\3\2"+
		"\2\2\u009f\u00a0\3\2\2\2\u00a0\u00a1\b\25\2\2\u00a1*\3\2\2\2\r\2\67=T"+
		"blx\u0082\u0092\u0097\u009e";
	public static final ATN _ATN =
		ATNSimulator.deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
	}
}