package com.ncc.neon.language

import org.antlr.v4.runtime.ANTLRErrorListener
import org.antlr.v4.runtime.Parser
import org.antlr.v4.runtime.RecognitionException
import org.antlr.v4.runtime.Recognizer
import org.antlr.v4.runtime.atn.ATNConfigSet
import org.antlr.v4.runtime.dfa.DFA
import org.antlr.v4.runtime.misc.NotNull
import org.antlr.v4.runtime.misc.Nullable


/**
 * Throws a parsing exception if parsing fails.
 */

class NeonParsingErrorListener implements ANTLRErrorListener {


    @Override
    void syntaxError(Recognizer<?, ?> recognizer, @Nullable Object offendingSymbol, int line, int charPositionInLine, String msg, @Nullable RecognitionException e) {
         throw new NeonParsingException(msg)
    }

    @Override
    void reportAmbiguity(@NotNull Parser recognizer, DFA dfa, int startIndex, int stopIndex, @NotNull BitSet ambigAlts, @NotNull ATNConfigSet configs) {
        throw new NeonParsingException("Ambiguity error.")
    }

    @Override
    void reportAttemptingFullContext(@NotNull Parser recognizer, @NotNull DFA dfa, int startIndex, int stopIndex, @NotNull ATNConfigSet configs) {
        throw new NeonParsingException("Full context error.")
    }

    @Override
    void reportContextSensitivity(@NotNull Parser recognizer, @NotNull DFA dfa, int startIndex, int stopIndex, @NotNull ATNConfigSet configs) {
        throw new NeonParsingException("Context sensitivity error.")
    }
}
