grammar LedgerEntryClassifier ;

le_classifier  : 
    le_stmt 
    ( binary_op le_stmt )* ; 

le_stmt : 
    ( neg_op )? 
    ( le_group_stmt | remark_match ) ;
      
le_group_stmt :
    '(' le_classifier ')' ;
                          
remark_match : 
    'remark' 'like' Value ;

neg_op : 'NOT' ;

binary_op : 'AND' | 'OR' ;

Value              : STRING ;

ID                 : (LETTER|DIGIT)+ ;
INT                : (DIGIT)+ ;
LETTER             : [\-a-zA-Z\u0080-\u00FF\u002e\u007e\u0040\u0023\u0024\u0025\u005e\u0026\u002a\u005f\u002b\u003f\u003a\u0027\u002f] ;
DIGIT              : [0-9] ;
STRING             : '"' ('\\"'|~'"')*? '"' ;

WS                 : [ \t\n\r]+                     -> skip ;
COMMENT            : '/*' .*? '*/'                  -> skip ;
LINE_COMMENT       : '//' .*? '\r'? '\n'            -> skip ;