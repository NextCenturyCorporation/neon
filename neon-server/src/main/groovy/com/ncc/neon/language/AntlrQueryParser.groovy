package com.ncc.neon.language

import com.ncc.neon.query.Query
import org.antlr.v4.runtime.ANTLRInputStream
import org.antlr.v4.runtime.CommonTokenStream
import org.springframework.stereotype.Component



/**
 * Parses a text query by invoking Antlr generated code.
 */

@Component
class AntlrQueryParser implements QueryParser {

    /**
     * Create a Query object by parsing a text query
     * @param text The text query
     * @return A query object
     */

    @Override
    Query parse(String text) {
        QueryCreator queryCreator = new QueryCreator()
        NeonLexer lexer = new NeonLexer(new ANTLRInputStream(text))
        NeonParser parser = new NeonParser(new CommonTokenStream(lexer))

        parseInput(parser, queryCreator)

        return queryCreator.createQuery()
    }

    private void parseInput(NeonParser parser, QueryCreator queryCreator) {
        parser.setBuildParseTree(true)
        parser.addParseListener(queryCreator)
        parser.addErrorListener(new NeonParsingErrorListener())
        parser.statement()
    }

}
