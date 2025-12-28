parser grammar TemplateParser;

options { tokenVocab=TemplateLexer; }

template    : part* EOF ;
part        : text | escape | expression ;
text        : TEXT ;
escape      : ESCAPE ;
expression  : EXPR_START expr EXPR_END ;
expr        : ID ;
