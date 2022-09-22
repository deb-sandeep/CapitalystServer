package com.sandy.capitalyst.server.api.ota.action.tradeupdater;

import static com.sandy.capitalyst.server.CapitalystServer.getBean ;
import static java.util.Calendar.DAY_OF_MONTH ;
import static org.apache.commons.lang.StringUtils.leftPad ;
import static org.apache.commons.lang.StringUtils.rightPad ;

import java.text.SimpleDateFormat ;
import java.util.Collections ;
import java.util.Date ;
import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;

import org.apache.commons.lang.time.DateUtils ;

import com.sandy.capitalyst.server.api.ota.action.OTA ;
import com.sandy.capitalyst.server.breeze.Breeze ;
import com.sandy.capitalyst.server.breeze.BreezeCred ;
import com.sandy.capitalyst.server.breeze.api.BreezeGetPortfolioHoldingsAPI ;
import com.sandy.capitalyst.server.breeze.api.BreezeGetPortfolioHoldingsAPI.PortfolioHolding ;
import com.sandy.capitalyst.server.breeze.api.BreezeGetTradeDetailAPI ;
import com.sandy.capitalyst.server.breeze.api.BreezeGetTradeDetailAPI.TradeDetail ;
import com.sandy.capitalyst.server.breeze.api.BreezeGetTradeListAPI ;
import com.sandy.capitalyst.server.breeze.api.BreezeGetTradeListAPI.Trade ;
import com.sandy.capitalyst.server.breeze.internal.BreezeAPIResponse ;
import com.sandy.capitalyst.server.core.nvpconfig.NVPConfigGroup ;
import com.sandy.capitalyst.server.core.nvpconfig.NVPManager ;
import com.sandy.capitalyst.server.dao.equity.EquityHolding ;
import com.sandy.capitalyst.server.dao.equity.EquityMaster ;
import com.sandy.capitalyst.server.dao.equity.EquityTrade ;
import com.sandy.capitalyst.server.dao.equity.EquityTxn ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityHoldingRepo ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityMasterRepo ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityTradeRepo ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityTxnRepo ;
import com.sandy.capitalyst.server.dao.nvp.NVP ;

public class EquityTradeUpdater extends OTA {
    
    private static final SimpleDateFormat SDF = new SimpleDateFormat( "dd-MMM-yyyy" ) ;
    
    public static final String NAME = "EquityTradeUpdater" ;
    
    private EquityMasterRepo  emRepo    = null ;
    private EquityTradeRepo   etrdRepo  = null ;
    private EquityTxnRepo     etxnRepo  = null ;
    private EquityHoldingRepo ehRepo    = null ;
    
    private Map<String, EquityHolding>    localHoldingsMap  = new HashMap<>() ;
    private Map<String, PortfolioHolding> breezeHoldingsMap = new HashMap<>() ;
    
    private NVPConfigGroup cfg = null ;
    
    public EquityTradeUpdater() {
        super( NAME ) ;
        etrdRepo = getBean( EquityTradeRepo.class   ) ;
        etxnRepo = getBean( EquityTxnRepo.class     ) ;
        ehRepo   = getBean( EquityHoldingRepo.class ) ;
        emRepo   = getBean( EquityMasterRepo.class  ) ;
        
        cfg = NVPManager.instance().getConfigGroup( NAME ) ;
    }

    @Override
    protected void execute() throws Exception {
        
        // LTU - Last Trade Update
        NVP  ltuCfg  = getLastTradeUpdateDate() ;
        Date ltuDate = null ;
        
        ltuDate = ltuCfg.getDateValue() ;
        
        super.addResult( "  Last trade update = " + SDF.format( ltuDate ) ) ;
        
        Breeze breeze = Breeze.instance() ;
        try {
            for( BreezeCred cred : breeze.getAllCreds() ) {
                addResult( "  Updating trades for " + cred.getUserName() ) ;
                
                addResult( "    Loading holdings" ) ;
                loadHoldings( cred ) ;
                
                addResult( "    Updating trades" ) ;
                updateTradesFor( cred, ltuDate ) ;
                
                addResult( "    Updating holdings" ) ;
                updateEquityHoldings( cred ) ;
            }
            
            //addResult( "  Updating last update timestamp." ) ;
            //ltuCfg.setValue( new Date() ) ;
            //nvpRepo.save( ltuCfg ) ;
        }
        catch( Exception e ) {
            super.addResult( e ) ;
        }
    }
    
    private void loadHoldings( BreezeCred cred ) throws Exception {
        
        localHoldingsMap.clear() ;
        breezeHoldingsMap.clear() ;
        
        List<EquityHolding> lHoldings = ehRepo.findByOwnerName( cred.getUserName() ) ;
        for( EquityHolding h : lHoldings ) {
            localHoldingsMap.put( h.getSymbolIcici(), h ) ;
        }
        addResult( "      DB holdings loaded" ) ;
        
        BreezeGetPortfolioHoldingsAPI api = new BreezeGetPortfolioHoldingsAPI() ;
        BreezeAPIResponse<PortfolioHolding> response = api.execute( cred ) ;
        List<PortfolioHolding> bHoldings = response.getEntities() ;
        
        for( PortfolioHolding ph : bHoldings ) {
            this.breezeHoldingsMap.put( ph.getSymbol(), ph ) ;
        }
        addResult( "      Breeze holdings loaded" ) ;
    }
    
    private void updateTradesFor( BreezeCred cred, Date fromDate ) throws Exception {
        
        List<Trade> trades = getBreezeTrades( cred, fromDate ) ;
        if( trades != null && !trades.isEmpty() ) {
            
            // Start with the oldest trade first.
            Collections.reverse( trades ) ;
            
            for( Trade trade : trades ) {
                
                String msg = SDF.format( trade.getTradeDate()           ) + " | " + 
                                rightPad  ( trade.getSymbolIcici(),   6 ) + " | " +
                                rightPad  ( trade.getAction(),        4 ) + " | " + 
                                leftPad   ( "" + trade.getQuantity(), 4 ) ;
                
                addResult( "     " + msg ) ;
                
                EquityHolding    eh = null ;
                PortfolioHolding ph = null ;
                
                eh = localHoldingsMap.get( trade.getSymbolIcici() ) ;
                
                if( eh == null ) {
                    addResult( "    Creating portfolio holding  " + 
                               trade.getSymbolIcici() ) ;
                    
                    ph = breezeHoldingsMap.get( trade.getSymbolIcici() ) ;
                    eh = createNewEquityHolding( ph, cred ) ;
                }
                
                EquityTrade et = etrdRepo.findByOrderId( trade.getOrderId() ) ;
                if( et == null ) {
                    et = processNewTrade( eh, trade, cred ) ;
                }
                processTxnsForTrade( eh, et, cred );
            }
        }
    }
    
    private EquityTrade processNewTrade( EquityHolding eh, Trade trade, BreezeCred cred ) 
        throws Exception {
        
        addResult( "      Creating new trade - " + trade.getOrderId() ) ;
        
        EquityTrade et = null ;
        
        et = new EquityTrade() ;
        et.setOwnerName  ( cred.getUserName()     ) ;
        et.setHoldingId  ( eh.getId()             ) ;
        et.setTradeDate  ( trade.getTradeDate()   ) ;
        et.setSymbolIcici( trade.getSymbolIcici() ) ;
        et.setAction     ( trade.getAction()      ) ;
        et.setQuantity   ( trade.getQuantity()    ) ;
        et.setOrderId    ( trade.getOrderId()     ) ;
        et.setValueAtCost( trade.getValueAtCost() ) ;
        et.setBrokerage  ( trade.getBrokerage()   ) ;
        et.setTax        ( trade.getTax()         ) ;
        
        et = etrdRepo.save( et ) ;
        
        return et ;
    }
    
    /** 
     * Fetches all the trade transactions (trade details) for the given trade
     * (order) and inserts updates them into the {@link EquityTxnRepo}.
     */
    private void processTxnsForTrade( EquityHolding eh, EquityTrade trade, 
                                      BreezeCred cred ) 
        throws Exception {
    
        List<TradeDetail> breezeTxns = null ;
        BreezeGetTradeDetailAPI api = null ;
        BreezeAPIResponse<TradeDetail> response = null ;
        
        api = new BreezeGetTradeDetailAPI() ;
        api.setOrderId( trade.getOrderId() ) ;
        
        response = api.execute( cred ) ;
        breezeTxns = response.getEntities() ;

        for( TradeDetail breezeTxn : breezeTxns ) {
            
            EquityTxn txn = etxnRepo.findByOrderIdAndTradeId( 
                                  trade.getOrderId(), breezeTxn.getTradeId() ) ;
            
            // If there is a transaction in the database whose orderId has 
            // been populated, it implies that we have already captured it.
            // If such a transaction does not exist, we do the elaborate 
            // act of getting all the transactions for the holding id, date
            // and quantity. This would imply that the transaction was captured
            // but via the CSV file upload. We should now update the orderId
            // tradeId, settlement details etc.
            if( txn == null ) {
                txn = findClosestMatchingTxn( breezeTxn, eh ) ;
            }
            
            if( txn == null ) {
                txn = createNewEquityTxn( trade, breezeTxn, eh ) ;
            }
            else {
                updateExistingTxn( trade, breezeTxn, txn ) ;
            }
            
            etxnRepo.save( txn ) ;
        }
    }
    
    private EquityTxn createNewEquityTxn( EquityTrade trade, 
                                          TradeDetail breezeTxn, 
                                          EquityHolding eh ) {
        
        EquityTxn txn = new EquityTxn() ;
        
        txn.setHoldingId       ( eh.getId()                     ) ;
        txn.setOrderId         ( trade.getOrderId()             ) ;
        txn.setTradeId         ( breezeTxn.getTradeId()         ) ;
        txn.setSymbolIcici     ( trade.getSymbolIcici()         ) ;
        txn.setAction          ( breezeTxn.getAction()          ) ;
        txn.setQuantity        ( breezeTxn.getQuantity()        ) ;
        txn.setTxnDate         ( breezeTxn.getTxnDate()         ) ;
        txn.setSettlementId    ( breezeTxn.getSettlementId()    ) ;
        txn.setExchangeTradeId ( breezeTxn.getExchangeTradeId() ) ;
        txn.setTxnPrice        ( breezeTxn.getTxnPrice()        ) ;
        txn.setBrokerage       ( breezeTxn.getBrokerage()       ) ;
        txn.setTxnCharges      ( breezeTxn.getTxnCharges()      ) ;
        txn.setStampDuty       ( breezeTxn.getStampDuty()       ) ;
        
        return txn ;
    }
    
    private void updateExistingTxn( EquityTrade trade,
                                    TradeDetail breezeTxn,
                                    EquityTxn txn ) {
        
        txn.setOrderId         ( trade.getOrderId()             ) ;
        txn.setTradeId         ( breezeTxn.getTradeId()         ) ;
        txn.setSymbolIcici     ( trade.getSymbolIcici()         ) ;
        txn.setTxnDate         ( breezeTxn.getTxnDate()         ) ;
        txn.setSettlementId    ( breezeTxn.getSettlementId()    ) ;
        txn.setExchangeTradeId ( breezeTxn.getExchangeTradeId() ) ;
    }
    
    private EquityTxn findClosestMatchingTxn( TradeDetail breezeTxn, EquityHolding eh ) {
        
        EquityTxn txn = null ;
        Date txnDate = breezeTxn.getTxnDate() ;
        
        Date start = DateUtils.truncate( txnDate, DAY_OF_MONTH ) ;
        Date end   = DateUtils.truncate( txnDate, DAY_OF_MONTH ) ;
        
        end = DateUtils.addHours  ( end, 23 ) ;
        end = DateUtils.addMinutes( end, 59 ) ;
        end = DateUtils.addSeconds( end, 59 ) ;
        
        List<EquityTxn> txns = null ;
        
        txns = etxnRepo.findMatchingTxns( eh.getId(), start, end, 
                                          breezeTxn.getAction(), 
                                          breezeTxn.getQuantity() ) ;
        
        if( !txns.isEmpty() ) {
            txn = txns.get( 0 ) ;
        }
        
        return txn ;
    }

    private List<Trade> getBreezeTrades( BreezeCred cred, Date fromDate ) 
        throws Exception {
        
        BreezeGetTradeListAPI api = null ;
        BreezeAPIResponse<Trade> response = null ;
        
        api = new BreezeGetTradeListAPI() ;
        api.setFromDate( fromDate ) ;
        
        response = api.execute( cred ) ;
        
        if( response.getStatus() != 200 ) {
            throw new Exception( "BreezeGetTradeListAPI server error. " + 
                                 "Status = " + response.getStatus() + ". " + 
                                 "Error = " + response.getError() ) ;
        }
        
        return response.getEntities() ;
    }
    
    private NVP getLastTradeUpdateDate() {
        
        NVP nvp = null ;
        
        /*
        nvpRepo.findByName( NVP_KEY_LAST_TRADE_UPDATE_DATE ) ;
        if( nvp == null ) {
            nvp = new NVP() ;
            nvp.setName( NVP_KEY_LAST_TRADE_UPDATE_DATE ) ;
            nvp.setValue( DateUtils.addYears( new Date(), -20 ) ) ;
        }
        */
        
        return nvp ;
    }
    
    private EquityHolding createNewEquityHolding( PortfolioHolding ph, 
                                                  BreezeCred cred ) {
        
        addResult( "      New holding for " + cred.getUserName() + 
                   " and " + ph.getSymbol() ) ;
        
        EquityMaster em = emRepo.findBySymbolIcici( ph.getSymbol() ) ;
        
        EquityHolding eh = new EquityHolding() ;
        
        eh.setOwnerName          ( cred.getUserName()                ) ;
        eh.setSymbolIcici        ( ph.getSymbol()                    ) ;
        eh.setSymbolNse          ( em.getSymbolIcici()               ) ;
        eh.setCompanyName        ( em.getName()                      ) ;
        eh.setIsin               ( em.getIsin()                      ) ;
        eh.setQuantity           ( ph.getQuantity()                  ) ;
        eh.setAvgCostPrice       ( ph.getAveragePrice()              ) ;
        eh.setCurrentMktPrice    ( ph.getCurrentMktPrice()           ) ;
        eh.setRealizedProfitLoss ( 0                                 ) ;
        eh.setDayGain            ( ph.getChange() * ph.getQuantity() ) ;
        eh.setLastUpdate         ( new Date()                        ) ;
        
        eh = ehRepo.save( eh ) ;
        
        return eh ;
    }
    
    private void updateEquityHoldings( BreezeCred cred ) {
        //TODO
    }
}
