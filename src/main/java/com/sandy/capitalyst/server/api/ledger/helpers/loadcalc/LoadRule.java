package com.sandy.capitalyst.server.api.ledger.helpers.loadcalc;

import static com.sandy.capitalyst.server.api.ledger.helpers.loadcalc.CalendarUtil.MONTH_NAMES ;
import static com.sandy.capitalyst.server.api.ledger.helpers.loadcalc.CalendarUtil.getMonthIndex ;

import org.apache.log4j.Logger ;
import org.dom4j.IllegalAddException ;

import lombok.Getter ;

class LoadRule {
    
    private static Logger log = Logger.getLogger( LoadRule.class ) ;
    
    public static enum LoadAction { REPLACE, ADD, SUBTRACT } ;
    
    private LoadRuleCtx ctx     = null ;
    private String      ruleSrc = null ;
    private int         lineNo  = 0 ;
    
    @Getter private LoadAction loadAction = null ;
    @Getter private int        loadAmt    = 0 ;
    @Getter private int[]      monthLoads = new int[12] ;
    
    public LoadRule( int lineNo, LoadRuleCtx ctx, String ruleSrc ) {
        
        this.ctx = ctx ;
        this.ruleSrc = ruleSrc ;
        this.lineNo = lineNo ;
        
        parseRule() ;
        //printLoadings() ;
    }
    
    void printLoadings() {
        StringBuilder sb = new StringBuilder( "Loading for " + ruleSrc + "\n   " ) ;
        for( int i=0; i<MONTH_NAMES.length; i++ ) {
            if( monthLoads[i] > 0 ) {
                sb.append( MONTH_NAMES[i] + "=" +
                           monthLoads[i] + "," ) ;
            }
        }
        log.debug( sb ) ; 
    }
    
    private void parseRule() {
        
        String[] ruleParts = parseLoadAction() ;
        if( ruleParts.length != 2 ) {
            throw new IllegalAddException( "Line " + lineNo + 
                                           ". RHS of rule missing." ) ;
        }
        this.loadAmt = ctx.evaluate( ruleParts[1].trim() ) ;
        parseMonths( ruleParts[0] ) ;
    }
    
    private String[] parseLoadAction() {
        
        String[] parts = null ;
        
        if( ruleSrc.contains( "-=" ) ) {
            loadAction = LoadAction.SUBTRACT ;
            parts = ruleSrc.split( "-=" ) ;
        }
        else if( ruleSrc.contains( "+=" ) ) {
            loadAction = LoadAction.ADD ;
            parts = ruleSrc.split( "\\+=" ) ;
        }
        else if( ruleSrc.contains( "=" ) ) {
            loadAction = LoadAction.REPLACE ;
            parts = ruleSrc.split( "=" ) ;
        }
        else {
            throw new IllegalArgumentException( "Line " + lineNo + 
                                                ". Invalid load action." ) ;
        }
        return parts ;
    }
    
    private void parseMonths( String monthsRule ) {
        
        String[] parts = monthsRule.split( "," ) ;
        
        for( int i=0; i<parts.length; i++ ) {
            
            String part = parts[i] ;
            
            if( part.contains( "-" ) ) {
                String[] rangeParts = part.split( "-" ) ;
                String startMonth = rangeParts[0].trim() ;
                String endMonth   = rangeParts[1].trim() ;
                
                int startMthIdx = getMonthIndex( startMonth ) ;
                int endMthIdx   = getMonthIndex( endMonth ) ;
                
                if( endMthIdx >= startMthIdx ) {
                    for( int j=startMthIdx; j<=endMthIdx; j++ ) {
                        this.monthLoads[j] = this.loadAmt ;
                    }
                }
                else {
                    for( int j=startMthIdx; j<MONTH_NAMES.length; j++ ) {
                        this.monthLoads[j] = this.loadAmt ;
                    }
                    for( int j=0; j<=endMthIdx; j++ ) {
                        this.monthLoads[j] = this.loadAmt ;
                    }
                }
            }
            else {
                int mthIdx = getMonthIndex( part.trim() ) ;
                this.monthLoads[mthIdx] = this.loadAmt ;
            }
        }
    }
    
}
