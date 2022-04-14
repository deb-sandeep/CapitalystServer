grammar LedgerEntryClassifier ;

le_classifier  : 
    le_stmt 
    ( binary_op le_stmt )* ; 

le_stmt : 
    ( neg_op )? 
    ( le_group_stmt | remark_match | amt_match | l1cat_match | l2cat_match | note_match ) ;
      
le_group_stmt :
    '(' le_classifier ')' ;
                          
remark_match : 
     single_remark_match | multi_remark_match ;
    
single_remark_match :
    'remark' 'like' value_with_alias ;
    
multi_remark_match :
    'remark' 'like' '(' (value_with_alias '|')* value_with_alias ')' ;
    
value_with_alias :
    Value ( 'alias' Value )? ;
    
l1cat_match :
    'l1Cat' 'like' Value ;
    
l2cat_match :
    'l2Cat' 'like' Value ;
    
note_match :
    'note' 'like' Value ;
    
amt_match :
    amt_gt_stmt | amt_lt_stmt | amt_eq_stmt | amt_bw_stmt ;

amt_gt_stmt :
    'amt' '>' Amount ;
    
amt_lt_stmt :
    'amt' '<' Amount ;
    
amt_eq_stmt :
    'amt' '=' Amount ;
    
amt_bw_stmt :
    'amt' '<>' Amount ':' Amount ;

neg_op : 'NOT' ;

binary_op : 'AND' | 'OR' ;

Value              : STRING ;
Amount             : FLOAT ;

ID                 : (LETTER|DIGIT)+ ;
INT                : (DIGIT)+ ;
LETTER             : [\-a-zA-Z\u0080-\u00FF\u002e\u007e\u0040\u0023\u0024\u0025\u005e\u0026\u002a\u005f\u002b\u003f\u003a\u0027\u002f] ;
DIGIT              : [0-9] ;
STRING             : '"' ('\\"'|~'"')*? '"' ;
FLOAT
                   :   ('0'..'9')+ '.' ('0'..'9')*
                   |   '.' ('0'..'9')+
                   |   ('0'..'9')+
                   |   '-' FLOAT
                   ;

WS                 : [ \t\n\r]+                     -> skip ;
COMMENT            : '/*' .*? '*/'                  -> skip ;
LINE_COMMENT       : '//' .*? '\r'? '\n'            -> skip ;