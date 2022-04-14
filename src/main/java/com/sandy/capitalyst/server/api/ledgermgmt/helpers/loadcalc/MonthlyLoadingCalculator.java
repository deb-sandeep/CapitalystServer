package com.sandy.capitalyst.server.api.ledgermgmt.helpers.loadcalc;

import java.util.ArrayList ;
import java.util.List ;

import org.apache.log4j.Logger ;

import com.sandy.capitalyst.server.api.ledgermgmt.helpers.loadcalc.LoadRule.LoadAction ;
import com.sandy.common.util.StringUtil ;
import com.udojava.evalex.Expression.ExpressionException ;

import lombok.Getter ;

public class MonthlyLoadingCalculator {
    
    private static final Logger log = Logger.getLogger( MonthlyLoadingCalculator.class ) ;
    
    private LoadRuleCtx ctx = new LoadRuleCtx() ;
    private List<LoadRule> rules = new ArrayList<>() ;
    
    @Getter private int[] monthlyCap = new int[12] ;
    @Getter private int yearlyCap = 0 ;

    public MonthlyLoadingCalculator( String ruleSet ) {
        parseRuleSet( ruleSet ) ;
        aggregateLoading() ;
    }
    
    private void raiseException( String msg ) {
        throw new IllegalArgumentException( msg ) ;
    }
    
    private void raiseException( int lineNo, String msg ) {
        throw new IllegalArgumentException( "Line " + lineNo + ". " + msg ) ;
    }
    
    private void parseRuleSet( String ruleSet )
        throws IllegalArgumentException {
        
        if( StringUtil.isEmptyOrNull( ruleSet ) ) {
            raiseException( "Empty ruleset" ) ;
        }
        
        String[] ruleStrings = ruleSet.split( "\\R" ) ;
        
        for( int i=0; i<ruleStrings.length; i++ ) {
            
            int lineNo = i+1 ;
            String str = ruleStrings[i].trim() ;
            
            if( StringUtil.isEmptyOrNull( str ) || 
                str.startsWith( "//" ) ) {
                continue ;
            }
            
            try {
                if( str.contains( "//" ) ) {
                    str = str.substring( 0, str.indexOf( "//" ) ) ;
                    str = str.trim() ;
                }
                
                if( str.startsWith( "var" ) ) {
                    populateContext( lineNo, str ) ;
                }
                else {
                    rules.add( new LoadRule( lineNo, ctx, str ) ) ;
                }
            }
            catch( ExpressionException e ) {
                raiseException( lineNo, "Invalid expression - " + e.getMessage() ) ;
            }
        }
    }
    
    private void populateContext( int lineNo, String varDecl ) {
        
        // var <variableName> = <expr>
        String[] parts = varDecl.split( "=" ) ;
        if( parts.length != 2 ) {
            raiseException( lineNo, "Doesn't follow var decl syntax." ) ;
        }
        
        String var  = parts[0].trim() ;
        String expr = parts[1].trim() ;
        
        String[] varParts = var.split( "\\s+" ) ;
        if( varParts.length != 2 ) {
            raiseException( lineNo, "Doesn't follow var decl syntax." ) ;
        }
        
        String varName = varParts[1] ;
        ctx.addVariable( varName, expr ) ;
    }
    
    private void aggregateLoading() {
        
        for( LoadRule rule : rules ) {
            
            LoadAction loadAction = rule.getLoadAction() ;
            int[] mthLoads = rule.getMonthLoads() ;
            
            for( int i=0; i<mthLoads.length; i++ ) {
                switch( loadAction ) {
                    case REPLACE:
                        monthlyCap[i] = mthLoads[i] ;
                        break ;
                    case ADD:
                        monthlyCap[i] += mthLoads[i] ;
                        break ;
                    case SUBTRACT:
                        monthlyCap[i] -= mthLoads[i] ;
                        break ;
                }
            }
        }
        
        for( int i=0; i < monthlyCap.length; i++     ) {
            this.yearlyCap += monthlyCap[i] ;
        }
    }

    public static void main( String[] args ) {
        String ruleSet = 
                "var a = 15000\n" + 
                "var b = a*2\n" +
                "var c = (a^2 + b)/2\n" +
                "\n" + 
                "Jan-Dec = a/12\n" ;
        
        log.debug( "Rule set = \n" + ruleSet ) ;
        log.debug( "---------------------------------" ) ;
        
        MonthlyLoadingCalculator calc = null ;
        calc = new MonthlyLoadingCalculator( ruleSet ) ;
        
        int[] amt = calc.getMonthlyCap() ;
        for( int i=0; i<amt.length; i++ ) {
            log.debug( CalendarUtil.getMonthName( i ) + " = " + amt[i] );
        }
        
        log.debug( "\nYearly cap = " + calc.getYearlyCap() );
    }

}
