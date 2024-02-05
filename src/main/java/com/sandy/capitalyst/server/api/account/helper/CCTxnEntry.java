package com.sandy.capitalyst.server.api.account.helper;

import java.text.DecimalFormat ;
import java.text.SimpleDateFormat ;
import java.util.Date ;

import com.sandy.capitalyst.server.dao.account.Account ;
import com.sandy.capitalyst.server.dao.account.repo.AccountRepo ;
import com.sandy.capitalyst.server.dao.ledger.LedgerEntry ;

import lombok.Data ;
import org.apache.commons.lang3.StringUtils;

import static com.sandy.capitalyst.server.CapitalystServer.getBean ;

@Data
public class CCTxnEntry {
    
    public static final SimpleDateFormat SDF = new SimpleDateFormat( "dd/MM/yyyy" ) ;
    public static final DecimalFormat DF = new DecimalFormat( "#.00" ) ;

    private String creditCardNumber = null ;
    private Date valueDate = null ;
    private String remarks = null ;
    private String txnRefNum = null ;
    private float amount = 0 ;
    private float balance = 0 ;
    
    public LedgerEntry convertToLedgerEntry() {
        
        AccountRepo accountRepo = getBean( AccountRepo.class ) ;
        
        LedgerEntry ledgerEntry = null ;
        Account account = accountRepo.findByAccountNumber( creditCardNumber ) ;
        
        ledgerEntry = new LedgerEntry() ;
        ledgerEntry.setAccount( account ) ;
        ledgerEntry.setRemarks( getEnrichedRemark() ) ;
        ledgerEntry.setValueDate( valueDate ) ;
        ledgerEntry.setAmount( amount ) ;
        
        ledgerEntry.generateHash() ;
        
        ledgerEntry.setBalance( balance ) ;
        
        return ledgerEntry ;
    }
    
    private String getEnrichedRemark() {
        
        String rawRemark = remarks ;
        
        if( rawRemark.endsWith( ", IN" ) ) {
            int lastIndex = rawRemark.lastIndexOf( ',' ) ;
            lastIndex = rawRemark.lastIndexOf( ',', lastIndex-1 ) ;
            rawRemark = rawRemark.substring( 0, lastIndex ) ;
        }
        return "[" + txnRefNum + "] " + rawRemark ;
    }
    
    public String toString() {
        StringBuilder builder = new StringBuilder() ;
        builder.append( StringUtils.rightPad( creditCardNumber, 20 ) ) ;
        builder.append( StringUtils.rightPad( SDF.format( valueDate ), 12 ) ) ;
        builder.append( StringUtils.rightPad( remarks, 45 ) ) ;
        builder.append( StringUtils.rightPad( txnRefNum, 10 ) ) ;
        builder.append( StringUtils.leftPad( DF.format( amount ), 15 ) ) ;
        builder.append( StringUtils.leftPad( DF.format( balance ), 15 ) ) ;
        return builder.toString() ;
    }
}
