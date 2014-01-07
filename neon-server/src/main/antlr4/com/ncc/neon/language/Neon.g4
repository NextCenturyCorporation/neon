grammar Neon;

options {
    language = Java;
}

// parser rules
statement : database (';')? query (';')?;
database : USE STRING;
query: select FROM STRING (where)? (additionalClauses)*;

select: SELECT selectFields;

selectFields
    : ALL_FIELDS
    | fieldList;

ALL_FIELDS : '*';

fieldList : STRING (',' STRING)*;

where: WHERE whereClause;

whereClause
     : '(' whereClause ')'
     | whereClause AND whereClause
     | whereClause OR whereClause
     | simpleWhereClause;

simpleWhereClause : STRING operator (WHOLE_NUMBER | NUMBER | STRING);

additionalClauses
     : sort
     | group
     | limit
     | offset;

operator  : GT
          | GTE
          | LT
          | LTE
          | EQ
          | NE;

AND : 'AND' | 'and';
OR : 'OR' | 'or';

sort: SORT sortClause (',' sortClause)*;

sortClause: STRING (SORT_DIRECTION)?;

group: GROUP groupClause (',' groupClause)*;

groupClause: STRING | function;

function: functionName '(' STRING ')';

functionName: 'first'
            | 'last'
            | 'max'
            | 'count'
            | 'min'
            | 'avg'
            | 'sum';

limit: LIMIT WHOLE_NUMBER;

offset: OFFSET WHOLE_NUMBER;

// lexer rules
GT: '>';
GTE: '>=';
LT: '<';
LTE: '<=';
EQ: '=';
NE: '!=';

USE: 'USE' | 'use';
SELECT: 'SELECT' | 'select';
FROM: 'FROM' | 'from';
WHERE: 'WHERE' | 'where';
GROUP: 'GROUP BY' | 'group by';
LIMIT: 'LIMIT' | 'limit';
OFFSET: 'OFFSET' | 'offset';
SORT: 'SORT BY' | 'sort by';
SORT_DIRECTION : 'ASC'
               | 'asc'
               | 'DESC'
               | 'desc';

WHOLE_NUMBER: [1-9] (DIGIT)*;
NUMBER: ('-')? DIGIT+ ('.' DIGIT+)?;
STRING: CHAR+;

fragment DIGIT : [0-9];
fragment CHAR : 'a'..'z'
              | 'A'..'Z'
              | '0'..'9'
              | '_' | '-' | '.' | '"';

WHITESPACE : [ \t\r\n]+ -> skip;

