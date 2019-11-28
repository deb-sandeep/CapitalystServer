package com.sandy.capitalyst.server.core.ledger.classifier;

import org.antlr.v4.runtime.ANTLRErrorListener ;
import org.antlr.v4.runtime.ANTLRInputStream ;
import org.antlr.v4.runtime.CommonTokenStream ;
import org.antlr.v4.runtime.tree.ParseTree ;
import org.apache.log4j.Logger ;

import com.sandy.capitalyst.server.core.ledger.classifier.LEClassifierAmtMatchRule.OpType ;
import com.sandy.capitalyst.server.rules.LedgerEntryClassifierBaseListener ;
import com.sandy.capitalyst.server.rules.LedgerEntryClassifierLexer ;
import com.sandy.capitalyst.server.rules.LedgerEntryClassifierParser ;
import com.sandy.capitalyst.server.rules.LedgerEntryClassifierParser.Amt_bw_stmtContext ;
import com.sandy.capitalyst.server.rules.LedgerEntryClassifierParser.Amt_eq_stmtContext ;
import com.sandy.capitalyst.server.rules.LedgerEntryClassifierParser.Amt_gt_stmtContext ;
import com.sandy.capitalyst.server.rules.LedgerEntryClassifierParser.Amt_lt_stmtContext ;
import com.sandy.capitalyst.server.rules.LedgerEntryClassifierParser.Amt_matchContext ;
import com.sandy.capitalyst.server.rules.LedgerEntryClassifierParser.Binary_opContext ;
import com.sandy.capitalyst.server.rules.LedgerEntryClassifierParser.Le_group_stmtContext ;
import com.sandy.capitalyst.server.rules.LedgerEntryClassifierParser.Neg_opContext ;
import com.sandy.capitalyst.server.rules.LedgerEntryClassifierParser.Remark_matchContext ;

public class LEClassifierRuleBuilder 
    extends LedgerEntryClassifierBaseListener {
    
    static final Logger log = Logger.getLogger( LEClassifierRuleBuilder.class ) ;

    private LedgerEntryClassifierParser parser = null ; 
    private ANTLRErrorListener errorListener = null ;
    
    public void setErrorListener( ANTLRErrorListener errList ) {
        this.errorListener = errList ;
    }
    
    public LEClassifierRule buildClassifier( String input ) {
        
        ANTLRInputStream  ais = new ANTLRInputStream( input ) ;
        LedgerEntryClassifierLexer lexer  = new LedgerEntryClassifierLexer( ais ) ;
        CommonTokenStream tokens = new CommonTokenStream( lexer ) ;
        
        parser = new LedgerEntryClassifierParser( tokens ) ;
        parser.removeErrorListeners() ;
        if( errorListener != null ) {
            parser.addErrorListener( errorListener ) ;
        }
        
        ParseTree tree   = parser.le_classifier() ;
        LEClassifierRule rule = buildRule( tree, 0 ) ;
        return rule ;
    }
    
    private LEClassifierRule buildRule( ParseTree tree, int fromChildIndex ) {
        
        LEClassifierRule rule = null ;
        Binary_opContext binOp = null ; 
        LEClassifierRule rightStmt = null ;
        LEClassifierBinaryOpRule opRule = null ; 
        
        int numChildrenLeft = tree.getChildCount() - fromChildIndex ;
        
        if( numChildrenLeft > 0 ) {
            rule = buildLEStatement( tree.getChild( fromChildIndex ) ) ;
            if( numChildrenLeft >= 3 ) {
                binOp = ( Binary_opContext )tree.getChild( fromChildIndex + 1 ) ;
                rightStmt = buildRule( tree, fromChildIndex+2 ) ;
                
                opRule = new LEClassifierBinaryOpRule( binOp.getText() ) ;
                opRule.setLeftRule( rule ) ;
                opRule.setRightRule( rightStmt ) ;
                
                rule = opRule ;
            }
        }
        return rule ;
    }
    
    private LEClassifierRule buildLEStatement( ParseTree tree ) {
        
        int numChildren = tree.getChildCount() ;
        boolean negOpFound = false ;
        LEClassifierRule rule = null ;
        
        for( int i=0; i<numChildren; i++ ) {
            ParseTree child = tree.getChild( i ) ;
            if( child instanceof Neg_opContext ) {
                negOpFound = true ;
            }
            else if( child instanceof Le_group_stmtContext ) {
                rule = buildRule( child.getChild( 1 ), 0 ) ; 
            }
            else if( child instanceof Remark_matchContext ) {
                rule = buildRemarkMatchRule( child ) ; 
            }
            else if( child instanceof Amt_matchContext ) {
                rule = buildAmountMatchRule( child ) ;
            }
        }
        
        if( negOpFound ) {
            return new LEClassifierNegOpRule( rule ) ;
        }
        return rule ;
    }
    
    private LEClassifierRule buildRemarkMatchRule( ParseTree tree ) {
        
        Remark_matchContext ctx = ( Remark_matchContext )tree ;
        String regex = ctx.Value().getText() ;
        return new LEClassifierRemarkMatchRule( regex.replace( "\"", "" ) ) ;
    }
    
    private LEClassifierRule buildAmountMatchRule( ParseTree tree ) {
        
        Amt_eq_stmtContext eqStmt = null ;
        Amt_gt_stmtContext gtStmt = null ;
        Amt_lt_stmtContext ltStmt = null ;
        Amt_bw_stmtContext bwStmt = null ;
        
        LEClassifierAmtMatchRule rule = null ;
        Amt_matchContext ctx = ( Amt_matchContext )tree ;
        
        ParseTree stmt = ctx.getChild( 0 ) ;
        if( stmt instanceof Amt_eq_stmtContext ) {
            eqStmt = ( Amt_eq_stmtContext )stmt ;
            rule = new LEClassifierAmtMatchRule( OpType.EQ ) ;
            rule.setAmt( Float.parseFloat( eqStmt.Amount().getText() ) ) ;
        }
        else if( stmt instanceof Amt_lt_stmtContext ) {
            ltStmt = ( Amt_lt_stmtContext )stmt ;
            rule = new LEClassifierAmtMatchRule( OpType.LT ) ;
            rule.setAmt( Float.parseFloat( ltStmt.Amount().getText() ) ) ;
        }
        else if( stmt instanceof Amt_gt_stmtContext ) {
            gtStmt = ( Amt_gt_stmtContext )stmt ;
            rule = new LEClassifierAmtMatchRule( OpType.GT ) ;
            rule.setAmt( Float.parseFloat( gtStmt.Amount().getText() ) ) ;
        }
        else if( stmt instanceof Amt_bw_stmtContext ) {
            bwStmt = ( Amt_bw_stmtContext )stmt ;
            rule = new LEClassifierAmtMatchRule( OpType.BW ) ;
            rule.setMinAmt( Float.parseFloat( bwStmt.Amount( 0 ).getText() ) ) ;
            rule.setMaxAmt( Float.parseFloat( bwStmt.Amount( 1 ).getText() ) ) ;
        }
        return rule ;
    }
}
