package com.sandy.capitalyst.server.job.corpus;

import static com.sandy.capitalyst.server.CapitalystServer.getBean ;

import java.text.SimpleDateFormat ;
import java.util.Date ;
import java.util.LinkedHashSet ;
import java.util.Objects;
import java.util.Set ;

import org.apache.log4j.Logger ;
import org.quartz.DisallowConcurrentExecution ;
import org.quartz.JobExecutionContext ;

import com.sandy.capitalyst.server.api.equity.EquityHoldingsQueryController ;
import com.sandy.capitalyst.server.api.equity.EquitySellTxnQueryController ;
import com.sandy.capitalyst.server.core.CapitalystConstants.AccountType ;
import com.sandy.capitalyst.server.core.scheduler.CapitalystJob ;
import com.sandy.capitalyst.server.core.scheduler.JobState ;
import com.sandy.capitalyst.server.dao.account.CorpusSnapshot ;
import com.sandy.capitalyst.server.dao.account.repo.AccountRepo ;
import com.sandy.capitalyst.server.dao.account.repo.CorpusSnapshotRepo ;
import com.sandy.capitalyst.server.dao.fixed_deposit.repo.FixedDepositRepo ;

/**
 * This job takes the total corpus snapshot for today. If a snapshot already
 * exists, the record is updated.
 */
@DisallowConcurrentExecution
public class CorpusSnapshotJob extends CapitalystJob {
    
    private static final Logger log = Logger.getLogger( CorpusSnapshotJob.class ) ;
    
    public static final SimpleDateFormat SDF = new SimpleDateFormat( "dd-MMM-yyyy" ) ;
    
    private CorpusSnapshotRepo csRepo = null ;
    private AccountRepo        acRepo = null ;
    private FixedDepositRepo   fdRepo = null ;

    @Override
    public String executeJob( JobExecutionContext context,
                               JobState state ) 
        throws Exception {
        
        csRepo = getBean( CorpusSnapshotRepo.class ) ;
        acRepo = getBean( AccountRepo.class        ) ;
        fdRepo = getBean( FixedDepositRepo.class   ) ;

        CorpusSnapshot todaySnapshot = getTodaySnapshot();
        
        log.debug( "- Populating Saving Account corpus" ) ;
        populateSavingAccountsCorpus(todaySnapshot) ;
        
        log.debug( "- Populating Fixed Deposit corpus" ) ;
        populateFixedDepositCorpus(todaySnapshot) ;
        
        log.debug( "- Populating Equity corpus" ) ;
        populateEquityCorpus(todaySnapshot) ;
        
        log.debug( "- Saving todays corpus snapshot" ) ;
        csRepo.save(todaySnapshot) ;

        return "Total corpus = " + Integer.toString( (int)todaySnapshot.getTotalCorpus() ) ;
    }
    
    private CorpusSnapshot getTodaySnapshot() 
        throws Exception {
        
        Date today = SDF.parse( SDF.format( new Date() ) ) ;
        log.debug( "- Getting existing snapshot for " + SDF.format( today ) ) ;
        
        CorpusSnapshot cs = csRepo.findByDate( today ) ;
        
        if( cs == null ) {
            cs = new CorpusSnapshot() ;
            cs.setDate( today ) ;
        }
        else {
            cs.setSavingAccount( 0 ) ;
            cs.setFixedDeposit( 0 ) ;  
            cs.setEquityInvested( 0 ) ;
            cs.setEquityMktValue( 0 ) ;
            cs.setEquityDailyGain( 0 ) ;  
            cs.setEquityUnrealizedPat( 0 ) ;
            cs.setEquityRealizedPat( 0 ) ;
            cs.setTaxOnRealizedProfit( 0 ) ;
        }
        return cs ;
    }

    private void populateSavingAccountsCorpus( CorpusSnapshot s ) {
        
        Set<String> accountTypes = new LinkedHashSet<>() ;
        accountTypes.add( AccountType.SAVING.name() ) ;
        accountTypes.add( AccountType.CREDIT.name() ) ;
        accountTypes.add( AccountType.CURRENT.name() ) ;
        
        acRepo.findByAccountTypeIn( accountTypes )
              .stream()
              .filter( a -> !a.isDeleted() )
              .forEach( a -> {
        
            s.setSavingAccount( s.getSavingAccount() + a.getBalance() ) ;
        } ) ;
        
        log.debug( "-> Saving Account balance : " + 
                   String.format( "%d", (long)s.getSavingAccount() ) ) ;
    }

    private void populateFixedDepositCorpus( CorpusSnapshot s ) {
        
        fdRepo.findAllActiveDeposits().forEach( fd -> {
            s.setFixedDeposit( s.getFixedDeposit() +
                               fd.getBaseAccount().getBalance() ) ;
        } ) ;
        
        log.debug( "-> Fixed Deposit balance : " + 
                   String.format( "%d", (long)s.getFixedDeposit() ) ) ;
    }
    
    private void populateEquityCorpus( CorpusSnapshot s ) {

        EquityHoldingsQueryController ehc ;
        EquitySellTxnQueryController stc ;
        
        ehc = getBean( EquityHoldingsQueryController.class ) ;
        stc = getBean( EquitySellTxnQueryController.class ) ;
        
        Objects.requireNonNull(ehc.getFamilyEquityHoldings().getBody()).forEach(h -> {

            s.setEquityInvested( s.getEquityInvested() + h.getValueAtCost() ) ;
            s.setEquityMktValue( s.getEquityMktValue() + h.getValueAtMktPrice() ) ;
            s.setEquityDailyGain( s.getEquityDailyGain() + h.getDayGain() ) ;
            s.setEquityUnrealizedPat( s.getEquityUnrealizedPat() + h.getPat() ) ;
        } ) ;
        
        Objects.requireNonNull(stc.getEquitySellTxns(null).getBody()).forEach(stxn -> {
            
            s.setEquityRealizedPat( s.getEquityRealizedPat() + stxn.getPat() ) ;
            s.setTaxOnRealizedProfit( s.getTaxOnRealizedProfit() + stxn.getTaxAmount() ) ;
        } ) ;
        
        log.debug( "-> Equity invested amount : " + 
                   String.format( "%d", (long)s.getEquityInvested() ) ) ;
        
        log.debug( "-> Equity market value : " + 
                   String.format( "%d", (long)s.getEquityMktValue() ) ) ;
        
        log.debug( "-> Equity daily gain : " + 
                   String.format( "%d", (long)s.getEquityDailyGain() ) ) ;
        
        log.debug( "-> Equity unrealized PAT : " + 
                   String.format( "%d", (long)s.getEquityUnrealizedPat() ) ) ;
        
        log.debug( "-> Equity realized PAT FY : " + 
                   String.format( "%d", (long)s.getEquityRealizedPat() ) ) ;
        
        log.debug( "-> Equity tax on realized profit FY : " + 
                   String.format( "%d", (long)s.getTaxOnRealizedProfit() ) ) ;
    }
}
