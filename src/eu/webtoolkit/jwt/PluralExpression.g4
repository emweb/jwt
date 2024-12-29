/*
 * Copyright (C) 2019 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
grammar PluralExpression;

statement
    : expression SEMICOLON? EOF
    ;

expression
    : orExpression (QMARK expression COLON expression)?
    ;

orExpression
    : andExpression (OR andExpression)*
    ;

andExpression
    : eqExpression (AND eqExpression)*
    ;

eqExpression
    : relationalExpression (eqOperator relationalExpression)*
    ;

relationalExpression
    : additiveExpression (cmpOperator additiveExpression)*
    ;

additiveExpression
    : term (sumOperator term)*
    ;

term
    : factor (prodOperator factor)*
    ;

factor
    : literal
    | group
    | variable
    ;

literal
    : INTEGER
    ;

group
    : LPAREN expression RPAREN
    ;

variable
    : N
    ;

eqOperator
    : EQ
    | NEQ
    ;

cmpOperator
    : GT
    | GTE
    | LT
    | LTE
    ;

sumOperator
    : PLUS
    | MINUS
    ;

prodOperator
    : MULT
    | DIV
    | MOD
    ;

OR
    : '||'
    ;

AND
    : '&&'
    ;

EQ
    : '=='
    ;

NEQ
    : '!='
    ;

GT
    : '>'
    ;

GTE
    : '>='
    ;

LT
    : '<'
    ;

LTE
    : '<='
    ;

PLUS
    : '+'
    ;

MINUS
    : '-'
    ;

MULT
    : '*'
    ;

DIV
    : '/'
    ;

MOD
    : '%'
    ;

N
    : 'n'
    ;

LPAREN
    : '('
    ;

RPAREN
    : ')'
    ;

COLON
    : ':'
    ;

SEMICOLON
    : ';'
    ;

QMARK
    : '?'
    ;

INTEGER
    : [0-9]+
    ;

WS
    : [ \t\r\n]+ -> skip
    ;
