package com.sandy.capitalyst.server.core.ledger.classifier;

import java.util.ArrayList ;
import java.util.List ;

import org.antlr.v4.runtime.ANTLRErrorListener ;
import org.antlr.v4.runtime.ANTLRInputStream ;
import org.antlr.v4.runtime.CommonTokenStream ;
import org.antlr.v4.runtime.tree.ParseTree ;
import org.apache.log4j.Logger ;

import com.sandy.capitalyst.server.core.ledger.classifier.LEClassifierAmtMatchRule.OpType ;
import com.sandy.capitalyst.server.core.ledger.classifier.LEClassifierRemarkMatchRule.MatchValueWithAlias ;
import com.sandy.capitalyst.server.rules.LedgerEntryClassifierBaseListener ;
import com.sandy.capitalyst.server.rules.LedgerEntryClassifierLexer ;
import com.sandy.capitalyst.server.rules.LedgerEntryClassifierParser ;
import com.sandy.capitalyst.server.rules.LedgerEntryClassifierParser.Amt_bw_stmtContext ;
import com.sandy.capitalyst.server.rules.LedgerEntryClassifierParser.Amt_eq_stmtContext ;
import com.sandy.capitalyst.server.rules.LedgerEntryClassifierParser.Amt_gt_stmtContext ;
import com.sandy.capitalyst.server.rules.LedgerEntryClassifierParser.Amt_lt_stmtContext ;
import com.sandy.capitalyst.server.rules.LedgerEntryClassifierParser.Amt_matchContext ;
import com.sandy.capitalyst.server.rules.LedgerEntryClassifierParser.Binary_opContext ;
import com.sandy.capitalyst.server.rules.LedgerEntryClassifierParser.L1cat_matchContext ;
import com.sandy.capitalyst.server.rules.LedgerEntryClassifierParser.L2cat_matchContext ;
import com.sandy.capitalyst.server.rules.LedgerEntryClassifierParser.Le_group_stmtContext ;
import com.sandy.capitalyst.server.rules.LedgerEntryClassifierParser.Multi_remark_matchContext ;
import com.sandy.capitalyst.server.rules.LedgerEntryClassifierParser.Neg_opContext ;
import com.sandy.capitalyst.server.rules.LedgerEntryClassifierParser.Note_matchContext ;
import com.sandy.capitalyst.server.rules.LedgerEntryClassifierParser.Remark_matchContext ;
import com.sandy.capitalyst.server.rules.LedgerEntryClassifierParser.Single_remark_matchContext ;
import com.sandy.capitalyst.server.rules.LedgerEntryClassifierParser.Value_with_aliasContext ;

public class LEClassifierRuleBuilder 
    extends LedgerEntryClassifierBaseListener {
    
    static final Logger log = Logger.getLogger( LEClassifierRuleBuilder.class ) ;

    private LedgerEntryClassifierParser parser = null ; 
    private ANTLRErrorListener errorListener = null ;
    private String ruleName = null ;
    
    public void setErrorListener( ANTLRErrorListener errList ) {
        this.errorListener = errList ;
    }
    
    public LEClassifierRule buildClassifier( String ruleName, String input ) 
        throws IllegalArgumentException {
        
        this.ruleName = ruleName ;
        
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
                
                opRule = new LEClassifierBinaryOpRule( ruleName, binOp.getText() ) ;
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
            else if( child instanceof L1cat_matchContext ) {
                rule = buildL1CatMatchRule( child ) ; 
            }
            else if( child instanceof L2cat_matchContext ) {
                rule = buildL2CatMatchRule( child ) ; 
            }
            else if( child instanceof Note_matchContext ) {
                rule = buildNoteMatchRule( child ) ; 
            }
            else if( child instanceof Amt_matchContext ) {
                rule = buildAmountMatchRule( child ) ;
            }
        }
        
        if( negOpFound ) {
            return new LEClassifierNegOpRule( ruleName, rule ) ;
        }
        return rule ;
    }
    
    private LEClassifierRule buildRemarkMatchRule( ParseTree tree ) {
        
        MatchValueWithAlias mva = null ;
        List<MatchValueWithAlias> values = new ArrayList<>() ;
        
        Remark_matchContext ctx = ( Remark_matchContext )tree ;
        ParseTree child = ctx.getChild( 0 ) ;
        
        if( child instanceof Single_remark_matchContext ) {
            
            Single_remark_matchContext singleMatch = null ;
            singleMatch = ctx.single_remark_match() ;
            mva = createMatchValueWithAlias( singleMatch.value_with_alias() ) ;
            
            values.add( mva ) ;
        }
        else {
            
            Multi_remark_matchContext multiMatch = null ;
            multiMatch = ctx.multi_remark_match() ;
            
            for( Value_with_aliasContext vwaCtx : multiMatch.value_with_alias() ) {
                
                mva = createMatchValueWithAlias( vwaCtx ) ;
                values.add( mva ) ;
            }
        }
            
        return new LEClassifierRemarkMatchRule( ruleName, values ) ;
    }
    
    private MatchValueWithAlias createMatchValueWithAlias( 
                                              Value_with_aliasContext vwaCtx ) {
        
        int numChildren = vwaCtx.getChildCount() ;
        MatchValueWithAlias mva = new MatchValueWithAlias() ;
        
        String regex = vwaCtx.Value( 0 ).getText().trim() ;
        mva.setRegex( regex.replace( "\"", "" ) ) ;
        
        if( numChildren > 1 ) {
            String alias = vwaCtx.Value( 1 ).getText().trim() ;
            mva.setAlias( alias.replace( "\"", "" ) ) ;
        }
        
        return mva ;
    }
    
    private LEClassifierRule buildL1CatMatchRule( ParseTree tree ) {
        L1cat_matchContext ctx = ( L1cat_matchContext )tree ;
        String regex = ctx.Value().getText() ;
        return new LEClassifierL1CatMatchRule( ruleName, regex.replace( "\"", "" ) ) ;
    }
    
    private LEClassifierRule buildL2CatMatchRule( ParseTree tree ) {
        L2cat_matchContext ctx = ( L2cat_matchContext )tree ;
        String regex = ctx.Value().getText() ;
        return new LEClassifierL2CatMatchRule( ruleName, regex.replace( "\"", "" ) ) ;
    }
    
    private LEClassifierRule buildNoteMatchRule( ParseTree tree ) {
        Note_matchContext ctx = ( Note_matchContext )tree ;
        String regex = ctx.Value().getText() ;
        return new LEClassifierNoteMatchRule( ruleName, regex.replace( "\"", "" ) ) ;
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
            rule = new LEClassifierAmtMatchRule( ruleName, OpType.EQ ) ;
            rule.setAmt( Float.parseFloat( eqStmt.Amount().getText() ) ) ;
        }
        else if( stmt instanceof Amt_lt_stmtContext ) {
            ltStmt = ( Amt_lt_stmtContext )stmt ;
            rule = new LEClassifierAmtMatchRule( ruleName, OpType.LT ) ;
            rule.setAmt( Float.parseFloat( ltStmt.Amount().getText() ) ) ;
        }
        else if( stmt instanceof Amt_gt_stmtContext ) {
            gtStmt = ( Amt_gt_stmtContext )stmt ;
            rule = new LEClassifierAmtMatchRule( ruleName, OpType.GT ) ;
            rule.setAmt( Float.parseFloat( gtStmt.Amount().getText() ) ) ;
        }
        else if( stmt instanceof Amt_bw_stmtContext ) {
            bwStmt = ( Amt_bw_stmtContext )stmt ;
            rule = new LEClassifierAmtMatchRule( ruleName, OpType.BW ) ;
            rule.setMinAmt( Float.parseFloat( bwStmt.Amount( 0 ).getText() ) ) ;
            rule.setMaxAmt( Float.parseFloat( bwStmt.Amount( 1 ).getText() ) ) ;
        }
        return rule ;
    }
}
