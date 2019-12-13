// Grammar for Norn mailing list expressions



// In order of precedence highest to lowest
// *   !     ,      =      ;          |
// *  is set intersection
// !  is set difference
// ,  is set union
// =  is defineList
// ;  is sequence
// |  is pipe parallel

// a*b,c
// a,b=c

// what we want:
// list definition can be detected out of precedence when no other parsing is possible
// but 

@skip whitespace {
    expression ::= pipe;
    pipe ::= sequence ('|' sequence)*;
    sequence ::= listDefinition (';' listDefinition)*;
    listDefinition ::= (listname '=' listDefinition) | setUnion;
    setUnion ::= setDifference (',' setDifference)*;
    setDifference ::= setIntersection ('!' setIntersection)*;
    setIntersection ::= primitive ('*' primitive)*;
    primitive ::= emailAddress | listname | emptyExpression | '(' expression ')' | (listname '=' listDefinition);
}

listname ::= [A-Za-z0-9_\-.]+;
emptyExpression ::= ''; 
username ::= [A-Za-z0-9_\-.\+]+;
domain ::= [A-Za-z0-9_\-.]+;
emailAddress ::= username '@' domain;
whitespace ::= [ \t\r\n]+;
