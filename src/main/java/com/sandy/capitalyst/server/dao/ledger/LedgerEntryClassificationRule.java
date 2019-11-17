package com.sandy.capitalyst.server.dao.ledger;

import java.sql.Date ;

import javax.persistence.Entity ;
import javax.persistence.GeneratedValue ;
import javax.persistence.GenerationType ;
import javax.persistence.Id ;
import javax.persistence.Table ;

@Entity
@Table( name = "ledger_entry_classification_rules" )
public class LedgerEntryClassificationRule {
    
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id ;
    
    private String ruleName   = null ;
    private String ruleText   = null ;
    private String l0Category = null ;
    private String l1Category = null ;
    private String l2Category = null ;
    private Date   lastUpdate = null ;
    private boolean isIncomeClassifierRule = false ;
    
    public Integer getId() {
        return id ;
    }
    
    public void setId( Integer id ) {
        this.id = id ;
    }

    public String getRuleName() {
        return ruleName ;
    }

    public void setRuleName( String ruleName ) {
        this.ruleName = ruleName ;
    }

    public String getRuleText() {
        return ruleText ;
    }

    public void setRuleText( String ruleText ) {
        this.ruleText = ruleText ;
    }

    public String getL0Category() {
        return l0Category ;
    }

    public void setL0Category( String l0Category ) {
        this.l0Category = l0Category ;
    }

    public String getL1Category() {
        return l1Category ;
    }

    public void setL1Category( String l1Category ) {
        this.l1Category = l1Category ;
    }

    public String getL2Category() {
        return l2Category ;
    }

    public void setL2Category( String l2Category ) {
        this.l2Category = l2Category ;
    }

    public Date getLastUpdate() {
        return lastUpdate ;
    }

    public void setLastUpdate( Date lastUpdate ) {
        this.lastUpdate = lastUpdate ;
    }
    
    public boolean isIncomeClassifierRule() {
        return isIncomeClassifierRule ;
    }

    public void setIncomeClassifierRule( boolean isIncomeClassifierRule ) {
        this.isIncomeClassifierRule = isIncomeClassifierRule ;
    }

    public String toString() {
        
        StringBuffer buffer = new StringBuffer() ;
        buffer.append( "LedgerEntryClassificationRule [" ).append( "\n" )
              .append( "  Name         = " + ruleName ).append( "\n" )
              .append( "  isIncomeRule = " + isIncomeClassifierRule ).append( "\n" )
              .append( "  l0Category   = " + l0Category ).append( "\n" )
              .append( "  l1Category   = " + l1Category ).append( "\n" )
              .append( "  l2Category   = " + l2Category ).append( "\n" )
              .append( "  LastUpdate   = " + lastUpdate.toString() ).append( "\n" )
              .append( "  rule         = " + ruleText ).append( "\n" )
              .append( "]" ) ;
        return buffer.toString() ;
    }
}
