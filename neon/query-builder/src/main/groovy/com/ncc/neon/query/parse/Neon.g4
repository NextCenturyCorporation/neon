grammar Neon;

@header
{
package com.ncc.neon.query.parse;
}

options {
    language = Java;
}

// parser rules
statement : ((query|database) ';')+;
database : USE STRING;
query: SELECT FROM STRING (where)? (options)*;

where: WHERE whereClause;

whereClause : simpleWhereClause
            | parentheticalWhereClause
            | simpleWhereClause booleanOperator simpleWhereClause;

simpleWhereClause : STRING operator STRING;
parentheticalWhereClause: LPAREN whereClause RPAREN;


operator  : GT
          | GTE
          | LT
          | LTE
          | EQ
          | NE;

booleanOperator : 'AND'
                | 'and'
                | 'OR'
                | 'or';


//options is any clause that is not part of the where (sort, aggregate, etc)
options: sortBy;

sortBy: SORT_BY STRING SORT_DIRECTION;

// lexer rules
GT: '>';
GTE: '>=';
LT: '<';
LTE: '<=';
EQ: '=';
NE: '!=';
LPAREN: '(';
RPAREN: ')';

USE: 'USE' | 'use';
SELECT: 'SELECT' | 'select';
FROM: 'FROM' | 'from';
WHERE: 'WHERE' | 'where';
SORT_BY: 'SORT' | 'sort';
SORT_DIRECTION : 'ASC'
               | 'asc'
               | 'DESC'
               | 'desc';

STRING: CHAR+;

fragment CHAR : 'a'..'z'
              | 'A'..'Z'
              | '0'..'9'
              | '_'
              | '-';

WHITESPACE : [ \t\r\n]+ -> skip;

