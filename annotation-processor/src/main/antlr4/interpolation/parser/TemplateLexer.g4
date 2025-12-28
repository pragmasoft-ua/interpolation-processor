lexer grammar TemplateLexer;

ESCAPE     : '$${' ;
EXPR_START : '${' -> pushMode(EXPRESSION) ;
TEXT       : ~[$]+ | '$' ;

mode EXPRESSION;
EXPR_END      : '}' -> popMode ;
ID            : [a-zA-Z_][a-zA-Z0-9_]* ;
WS            : [ \t\r\n]+ -> skip ;
BLOCK_COMMENT : '/*' .*? '*/' -> skip ;
