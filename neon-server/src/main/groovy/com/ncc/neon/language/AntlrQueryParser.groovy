/*
 * Copyright 2013 Next Century Corporation
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
