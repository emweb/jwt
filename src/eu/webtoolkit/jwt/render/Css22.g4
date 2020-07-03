/*
 * Copyright (C) 2019 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
// Straightforward parser implementation based on CSS 2.2 spec appendix G:
// https://www.w3.org/TR/CSS22/grammar.html

grammar Css22;

styleSheet
    : ( CHARSET_SYM STRING SEMICOLON )?
      ( WSP | CDO | CDC )* ( importStmt ( CDO WSP* | CDC WSP* )* )*
      ( ( ruleset | media | page ) ( CDO WSP* | CDC WSP* )* )*
      EOF
    ;

importStmt
    : IMPORT_SYM WSP*
      ( STRING | URI) WSP* mediaList? SEMICOLON WSP*
    ;

media
    : MEDIA_SYM WSP* mediaList LBRACE WSP* ruleset* RBRACE WSP*
    ;

mediaList
    : medium ( COMMA WSP* medium )*
    ;

medium
    : IDENT WSP*
    ;

page
    : PAGE_SYM WSP* pseudoPage?
      LBRACE WSP* declaration? ( SEMICOLON WSP* declaration? )* RBRACE WSP*
    ;

pseudoPage
    : COLON IDENT WSP*
    ;

operator
    : SLASH WSP* | COMMA WSP*
    ;

combinator
    : PLUS WSP*
    | GT WSP*
    ;

property
    : IDENT WSP*
    ;

ruleset
    : selector ( COMMA WSP* selector )*
      LBRACE WSP* declarationBlock RBRACE WSP*
    ;

declarationBlock
    : declaration? ( SEMICOLON WSP* declaration? )*
    ;

selector
    : simpleSelector ( combinator simpleSelector | WSP+ combinator? simpleSelector )*
      WSP*
    ;

simpleSelector
    : elementName ( id | className | attrib | pseudo )*
    | ( id | className | attrib | pseudo )+
    ;

id
    : HASH
    ;

className
    : DOT IDENT
    ;

elementName
    : IDENT | ASTERISK
    ;

attrib
    : LBRACKET WSP* IDENT WSP* ( ( EQ | INCLUDES | DASHMATCH ) WSP*
      ( IDENT | STRING ) WSP* )? RBRACKET
    ;

pseudo
    : COLON ( IDENT | FUNCTION WSP* ( IDENT WSP* )? RPAREN )
    ;

declaration
    : property COLON WSP* expr prio?
    ;

prio
    : IMPORTANT_SYM WSP*
    ;

expr
    : term ( operator? term )*
    ;

term
    : ( NUMBER WSP* | PERCENTAGE WSP* | LENGTH WSP* | EMS WSP* | EXS WSP* | ANGLE WSP* | TIME WSP* | FREQ WSP* )
    | STRING WSP* | IDENT WSP* | URI WSP* | hexcolor | function
    ;

function
    : FUNCTION WSP* expr RPAREN WSP*
    ;

/*
 * There is a constraint on the color that it must
 * have either 3 or 6 hex digits (i.e., [0-9a-fA-F])
 * after the "#"; e.g., "#000" is OK, but "#abcd" is not.
 */
hexcolor
    : HASH WSP*
    ;

fragment HEX         : [0-9a-fA-F] ;
fragment NONASCII    : '\u00A0'..'\u00FF' ;
fragment UNICODE     : '\\' ( ( ( ( HEX? HEX )? HEX )? HEX)? HEX )? HEX ( '\r\n' | [ \t\r\n\f] )? ;
fragment ESCAPE      : UNICODE | '\\' ~[\r\n\f0-9a-f] ;
fragment NMSTART     : [_a-zA-Z] | NONASCII | ESCAPE ;
fragment NMCHAR      : [_a-zA-Z0-9-] | NONASCII | ESCAPE ;
fragment STRING1     : '"' ( ~[\n\r\f\\"] | '\\' NL | ESCAPE )* '"' ;
fragment STRING2     : '\'' ( ~[\n\r\f\\'] | '\\' NL | ESCAPE )* '\'' ;
fragment BADSTRING1  : '"' ( ~[\n\r\f\\"] | '\\' NL | ESCAPE )* '\\'? ;
fragment BADSTRING2  : '\'' ( ~[\n\r\f\\'] | '\\' NL | ESCAPE )* '\\'? ;
fragment BADCOMMENT1 : '/*' ~[*]* '*'+ ( ~[/*]~[*]* '*'+ )* ;
fragment BADCOMMENT2 : '/*' ~[*]* ( '*'+ ~[/*]~[*]* )* ;
fragment BADURI1     : U R L '(' WS ( ( [!#$%&*--~] | '[' | ']' ) | NONASCII | ESCAPE )* WS ;
fragment BADURI2     : U R L '(' WS STRING WS ;
fragment BADURI3     : U R L '(' WS BADSTRING ;
fragment NAME        : NMCHAR+ ;
fragment NUM         : [-+]? [0-9]+ | [-+]? [0-9]* '.' [0-9]+ ;
fragment URL         : ( [!#$%&*-~] | NONASCII | ESCAPE )* ;
fragment SP          : [ \t\r\n\f]+ ;
fragment WS          : SP? ;
fragment NL          : '\n' | '\r\n' | '\r' | '\f' ;

fragment A  : 'a' | 'A' | '\\' ('0000'|'000'|'00'|'0') ( [46]  '1' ) ( '\r\n' | [ \t\r\n\f] )? ;
fragment C  : 'c' | 'C' | '\\' ('0000'|'000'|'00'|'0') ( [46]  '3' ) ( '\r\n' | [ \t\r\n\f] )? ;
fragment D  : 'd' | 'D' | '\\' ('0000'|'000'|'00'|'0') ( [46]  '4' ) ( '\r\n' | [ \t\r\n\f] )? ;
fragment E  : 'e' | 'E' | '\\' ('0000'|'000'|'00'|'0') ( [46]  '5' ) ( '\r\n' | [ \t\r\n\f] )? ;
fragment G  : 'g' | 'G' | '\\' ('0000'|'000'|'00'|'0') ( [46]  '7' ) ( '\r\n' | [ \t\r\n\f] )? ;
fragment H  : 'h' | 'H' | '\\' ('0000'|'000'|'00'|'0') ( [46]  '8' ) ( '\r\n' | [ \t\r\n\f] )? ;
fragment I  : 'i' | 'I' | '\\' ('0000'|'000'|'00'|'0') ( [46]  '9' ) ( '\r\n' | [ \t\r\n\f] )? ;
fragment K  : 'k' | 'K' | '\\' ('0000'|'000'|'00'|'0') ( [46] [bB] ) ( '\r\n' | [ \t\r\n\f] )? ;
fragment L  : 'l' | 'L' | '\\' ('0000'|'000'|'00'|'0') ( [46] [cC] ) ( '\r\n' | [ \t\r\n\f] )? ;
fragment M  : 'm' | 'M' | '\\' ('0000'|'000'|'00'|'0') ( [46] [dD] ) ( '\r\n' | [ \t\r\n\f] )? ;
fragment N  : 'n' | 'N' | '\\' ('0000'|'000'|'00'|'0') ( [46] [eE] ) ( '\r\n' | [ \t\r\n\f] )? ;
fragment O  : 'o' | 'O' | '\\' ('0000'|'000'|'00'|'0') ( [46] [fF] ) ( '\r\n' | [ \t\r\n\f] )? ;
fragment P  : 'p' | 'P' | '\\' ('0000'|'000'|'00'|'0') ( [57]  '0' ) ( '\r\n' | [ \t\r\n\f] )? ;
fragment R  : 'r' | 'R' | '\\' ('0000'|'000'|'00'|'0') ( [57]  '2' ) ( '\r\n' | [ \t\r\n\f] )? ;
fragment S  : 's' | 'S' | '\\' ('0000'|'000'|'00'|'0') ( [57]  '3' ) ( '\r\n' | [ \t\r\n\f] )? ;
fragment T  : 't' | 'T' | '\\' ('0000'|'000'|'00'|'0') ( [57]  '4' ) ( '\r\n' | [ \t\r\n\f] )? ;
fragment U  : 'u' | 'U' | '\\' ('0000'|'000'|'00'|'0') ( [57]  '5' ) ( '\r\n' | [ \t\r\n\f] )? ;
fragment X  : 'x' | 'X' | '\\' ('0000'|'000'|'00'|'0') ( [57]  '8' ) ( '\r\n' | [ \t\r\n\f] )? ;
fragment Z  : 'z' | 'Z' | '\\' ('0000'|'000'|'00'|'0') ( [57] [aA] ) ( '\r\n' | [ \t\r\n\f] )? ;

WSP           : SP ;

COMMENT       : '/*' ~[*]* '*'+ ( ~[/*]~[*]* '*'+ )* '/' -> skip ;
BADCOMMENT    : ( BADCOMMENT1 | BADCOMMENT2 ) -> skip ;

CDO           : '<!--' ;
CDC           : '-->' ;
INCLUDES      : '~=' ;
DASHMATCH     : '|=' ;

STRING        : STRING1 | STRING2 ;
BADSTRING     : BADSTRING1 | BADSTRING2 ;

IDENT         : '-'? NMSTART NMCHAR* ;

HASH          : '#' NAME ;

IMPORT_SYM    : '@' I M P O R T ;
PAGE_SYM      : '@' P A G E ;
MEDIA_SYM     : '@' M E D I A ;
CHARSET_SYM   : '@charset' ;

IMPORTANT_SYM : '!' ( WS | COMMENT )* I M P O R T A N T ;

EMS           : NUM E M ;
EXS           : NUM E X ;
LENGTH        : ( NUM P X | NUM C M | NUM M M | NUM I N | NUM P T | NUM P C ) ;
ANGLE         : ( NUM D E G | NUM R A D | NUM G R A D ) ;
TIME          : ( NUM M S ) | ( NUM S ) ;
FREQ          : ( NUM H Z ) | ( NUM K H Z ) ;
DIMENSION     : NUM IDENT ;

PERCENTAGE    : NUM '%' ;
NUMBER        : NUM ;

URI           : U R L '(' WS STRING WS ')'
              | U R L '(' WS URL WS ')' ;

BADURI        : BADURI1 | BADURI2 | BADURI3 ;

FUNCTION      : IDENT '(' ;

ASTERISK      : '*' ;
EQ            : '=' ;
DOT           : '.' ;
PLUS          : '+' ;
GT            : '>' ;
SLASH         : '/' ;
COMMA         : ',' ;
COLON         : ':' ;
SEMICOLON     : ';' ;
LPAREN        : '(' ;
RPAREN        : ')' ;
LBRACE        : '{' ;
RBRACE        : '}' ;
LBRACKET      : '[' ;
RBRACKET      : ']' ;
