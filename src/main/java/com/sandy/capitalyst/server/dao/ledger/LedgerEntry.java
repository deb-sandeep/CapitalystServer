package com.sandy.capitalyst.server.dao.ledger;

import java.util.Date ;
import java.text.SimpleDateFormat ;

import javax.persistence.Column ;
import javax.persistence.Entity ;
import javax.persistence.GeneratedValue ;
import javax.persistence.GenerationType ;
import javax.persistence.Id ;
import javax.persistence.JoinColumn ;
import javax.persistence.ManyToOne ;
import javax.persistence.Table ;

import com.sandy.capitalyst.server.api.ledger.helpers.SplitDetails ;
import com.sandy.capitalyst.server.dao.account.Account ;
import com.sandy.common.util.StringUtil ;

import lombok.Data ;

@Data
@Entity
@Table( name = "account_ledger" )
public class LedgerEntry implements Cloneable {
    
    public static final SimpleDateFormat HASH_SDF = new SimpleDateFormat( "dd/MM/yyyy" ) ;

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id ;
    
    @ManyToOne
    @JoinColumn( name="account_id" )
    private Account account ;
    
    private Date valueDate = null ;
    private String remarks = null ;
    private float amount = 0 ;
    private String chequeNumber = null ;
    private float balance = 0 ;
    private String notes = null ;
    private String hash = null ;
    
    @Column( name = "l1_cat" )
    private String l1Cat = null ;
    
    @Column( name = "l2_cat" )
    private String l2Cat = null ;
    
    public String getRemarks() {
        return remarks == null ? "" : remarks ;
    }
    
    public void addToRemarks( String additionalRemark ) {
        remarks = getRemarks() + additionalRemark ;
    }

    public boolean isCredit() {
        return amount > 0 ;
    }
    
    public boolean isChequePayment() {
        return StringUtil.isNotEmptyOrNull( chequeNumber ) ;
    }
    
    public LedgerEntry split( SplitDetails splitDetails ) 
        throws Exception {
        
        LedgerEntry newEntry = ( LedgerEntry )this.clone() ;
        
        setAmount( getAmount() + splitDetails.getAmount() ) ;
        setBalance( getBalance() + splitDetails.getAmount() ) ;
        
        // Do not generate a new hash. Why? 
        // Because by generating a new hash, we are essentially making a 
        // new payment. This will break the traceability from the payment
        // source. Implying if we upload the original payment file, it will
        // end up creating a new 'before-split' payment.
        //
        //setHash( generateHash() ) ;
        
        newEntry.setRemarks( getRemarks() + " [s]" ) ;
        newEntry.setAmount( -1*splitDetails.getAmount() ) ;
        newEntry.setL1Cat( splitDetails.getL1Cat() ) ;
        newEntry.setL2Cat( splitDetails.getL2Cat() ) ;
        newEntry.setNotes( splitDetails.getNotes() ) ;
        newEntry.setHash( newEntry.generateHash() ) ;
        
        return newEntry ;
    }
    
    /** 
     * Note that clone does not clone the following fields:
     * 
     * 1. ID. 
     * 2. hash 
     */
    public Object clone() {
        
        LedgerEntry clone = new LedgerEntry() ;
        clone.setAccount( getAccount() ) ;
        clone.setValueDate( getValueDate() ) ;
        clone.setRemarks( getRemarks() ) ;
        clone.setAmount( getAmount() ) ;
        clone.setChequeNumber( getChequeNumber() ) ;
        clone.setBalance( getBalance() ) ;
        clone.setNotes( getNotes() ) ;
        clone.setL1Cat( getL1Cat() ) ;
        clone.setL2Cat( getL2Cat() ) ;
        
        return clone ;
    }
    
    public String generateHash() {
        
        StringBuffer buffer = new StringBuffer() ;
        
        if( account == null || account.getAccountNumber().equals( "CASH@HOME" ) ) {
            buffer.append( LedgerEntry.HASH_SDF.format( valueDate ) )
                  .append( l1Cat )
                  .append( l2Cat )
                  .append( amount ) ;
        }
        else {
            buffer.append( account.getAccountNumber() )
                  .append( HASH_SDF.format( valueDate ) )
                  .append( remarks )
                  .append( Float.toString( amount ) )
                  .append( Float.toString( getBalance() ) ) ;
        }
        
        this.hash = StringUtil.getHash( buffer.toString() ) ;
        return this.hash ;
    }
    
    public String toString() {
        
        StringBuffer buffer = new StringBuffer() ;
        buffer.append( "LedgerEntry [" ).append( "\n" )
              .append( "  Account = " + account.getShortName() ).append( "\n" )
              .append( "  Value date = " + HASH_SDF.format( valueDate ) ).append( "\n" )
              .append( "  Remarks = " + remarks ).append( "\n" )
              .append( "  Amount = " + amount ).append( "\n" )
              .append( "  balance = " + balance ).append( "\n" )
              .append( "  l1Cat = " + l1Cat ).append( "\n" )
              .append( "  l2Cat = " + l2Cat ).append( "\n" )
              .append( "  notes = " + notes ).append( "\n" )
              .append( "]" ) ;
        return buffer.toString() ;
    }
}
