package com.sandy.capitalyst.server.dao.ledger;

import java.sql.Date ;

import jakarta.persistence.Column ;
import jakarta.persistence.Entity ;
import jakarta.persistence.GeneratedValue ;
import jakarta.persistence.GenerationType ;
import jakarta.persistence.Id ;
import jakarta.persistence.Table ;

import lombok.Data ;

@Data
@Entity
@Table( name = "ledger_entry_classification_rules" )
public class LedgerEntryClassificationRule {
    
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer id ;
    
    private String ruleName   = null ;
    private String ruleText   = null ;
    private Date   lastUpdate = null ;
    private boolean creditClassifier = false ;
    
    @Column( name = "l1_category" )
    private String l1Category = null ;
    
    @Column( name = "l2_category" )
    private String l2Category = null ;
    
    public String toString() {
        
        StringBuffer buffer = new StringBuffer() ;
        buffer.append( "LedgerEntryClassificationRule [" ).append( "\n" )
              .append( "  Name         = " + ruleName ).append( "\n" )
              .append( "  isIncomeRule = " + creditClassifier ).append( "\n" )
              .append( "  l1Category   = " + l1Category ).append( "\n" )
              .append( "  l2Category   = " + l2Category ).append( "\n" )
              .append( "  LastUpdate   = " + lastUpdate.toString() ).append( "\n" )
              .append( "  rule         = " + ruleText ).append( "\n" )
              .append( "  creditClassifier = " + creditClassifier ).append( "\n" )
              .append( "]" ) ;
        return buffer.toString() ;
    }
}
