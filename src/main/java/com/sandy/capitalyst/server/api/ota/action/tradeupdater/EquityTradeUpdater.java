package com.sandy.capitalyst.server.api.ota.action.tradeupdater;

import static com.sandy.capitalyst.server.CapitalystServer.getBean ;
import static java.util.Calendar.DAY_OF_MONTH ;
import static org.apache.commons.lang.StringUtils.leftPad ;
import static org.apache.commons.lang.StringUtils.rightPad ;

import java.text.SimpleDateFormat ;
import java.util.ArrayList ;
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
import com.sandy.common.util.StringUtil ;

public class EquityTradeUpdater extends OTA {
    
    private static SimpleDateFormat SDF = new SimpleDateFormat( "dd-MMM-yyyy HH:mm:ss" ) ;
    
    public static final String NAME                 = "EquityTradeUpdater" ;
    public static final String CFG_GRP_NAME         = NAME ;
    public static final String CFG_LAST_UPDATE_DATE = "last_update_date" ;
    public static final String CFG_ITER_DUR_IN_DAYS = "iteration_duration_days" ;
    
    public static final String CFG_INCL_PORTFOLIO_STOCKS = "incl_portfolio_stocks" ;
    public static final String CFG_EXCL_PORTFOLIO_STOCKS = "excl_portfolio_stocks" ;
    
    public static final String CFG_UPDATE_AVG_COST_PRICE = "update_avg_cost_price" ;

    public static final String CFG_DEF_LAST_UPDATE_DATE = "01-01-2014" ;
    public static final int    CFG_DEF_ITER_DUR_IN_DAYS = 7 ;

    private EquityMasterRepo  emRepo    = null ;
    private EquityTradeRepo   etrdRepo  = null ;
    private EquityTxnRepo     etxnRepo  = null ;
    private EquityHoldingRepo ehRepo    = null ;
    
    private Map<String, EquityHolding>    localHoldingsMap  = new HashMap<>() ;
    private Map<String, PortfolioHolding> breezeHoldingsMap = new HashMap<>() ;
    
    private NVPConfigGroup cfg = null ;
    
    private Date    lastUpdateDate     = null ;
    private Date    iterToDate         = null ;
    private int     iterDurationDays   = CFG_DEF_ITER_DUR_IN_DAYS ;
    private boolean updateAvgCostPrice = true ;
    
    private List<String> inclPortfolioStocks = new ArrayList<>() ;
    private List<String> exclPortfolioStocks = new ArrayList<>() ;
    
    private int numHoldingsCreated = 0 ;
    private int numHoldingsUpdated = 0 ;
    private int numTradesCreated   = 0 ;
    private int numTradesProcessed = 0 ;
    private int numTxnCreated      = 0 ;
    private int numTxnUpdated      = 0 ;
    
    public EquityTradeUpdater() {
        
        super( CFG_GRP_NAME ) ;
        
        etrdRepo = getBean( EquityTradeRepo.class   ) ;
        etxnRepo = getBean( EquityTxnRepo.class     ) ;
        ehRepo   = getBean( EquityHoldingRepo.class ) ;
        emRepo   = getBean( EquityMasterRepo.class  ) ;
        
        cfg = NVPManager.instance().getConfigGroup( CFG_GRP_NAME ) ;
        
        lastUpdateDate = cfg.getDateValue( CFG_LAST_UPDATE_DATE, 
                                           CFG_DEF_LAST_UPDATE_DATE ) ;
        
        iterDurationDays = cfg.getIntValue( CFG_ITER_DUR_IN_DAYS, 
                                            CFG_DEF_ITER_DUR_IN_DAYS ) ;
        
        inclPortfolioStocks = cfg.getListValue( CFG_INCL_PORTFOLIO_STOCKS, "" ) ;
        
        exclPortfolioStocks = cfg.getListValue( CFG_EXCL_PORTFOLIO_STOCKS, "" ) ;

        iterToDate = DateUtils.addDays( lastUpdateDate, iterDurationDays ) ;
        
        updateAvgCostPrice = cfg.getBoolValue( CFG_UPDATE_AVG_COST_PRICE, 
                                               updateAvgCostPrice ) ;
        
        Date now = new Date() ;
        if( iterToDate.after( now ) ) {
            iterToDate = now ;
        }
    }

    @Override
    protected void execute() throws Exception {
        
        addResult( "Date Range:" ) ;
        addResult( "  From = " + SDF.format( lastUpdateDate ) ) ;
        addResult( "  To   = " + SDF.format( iterToDate     ) ) ;
        
        Breeze breeze = Breeze.instance() ;
        try {
            for( BreezeCred cred : breeze.getAllCreds() ) {
                addResult( "------------------------------------" ) ; 
                addResult( "Updating trades for " + cred.getUserName() ) ;
                
                addResult( "  Loading holdings" ) ;
                loadHoldings( cred ) ;
                
                addResult( "  Updating trades" ) ;
                updateTrades( cred, lastUpdateDate, iterToDate ) ;
                
                addResult( "  Updating holdings" ) ;
                updateEquityHoldings( cred ) ;
            }
            
            addResult( "------------------------------------" ) ; 
            addResult( "Statistics:" ) ;
            addResult( "  numHoldingsCreated = " + numHoldingsCreated ) ;
            addResult( "  numHoldingsUpdated = " + numHoldingsUpdated ) ;
            addResult( "  numTradesCreated   = " + numTradesCreated   ) ;
            addResult( "  numTradesProcessed = " + numTradesProcessed ) ;
            addResult( "  numTxnCreated      = " + numTxnCreated      ) ;
            addResult( "  numTxnUpdated      = " + numTxnUpdated      ) ;
            
            addResult( "" ) ;
            addResult( "Updating last update timestamp. " +
                       SDF.format( iterToDate ) ) ;

            cfg.setValue( CFG_LAST_UPDATE_DATE, iterToDate ) ;
        }
        catch( Exception e ) {
            super.addResult( e ) ;
        }
    }
    
    private void loadHoldings( BreezeCred cred ) throws Exception {
        
        List<EquityHolding>                 lHoldings = null ;
        List<PortfolioHolding>              bHoldings = null ;
        BreezeGetPortfolioHoldingsAPI       api       = null ;
        BreezeAPIResponse<PortfolioHolding> response  = null ;
        
        localHoldingsMap.clear() ;
        breezeHoldingsMap.clear() ;
        
        // ---------------------------------------------------------------------
        addResult( "    Loading DB holdings" ) ;
        lHoldings = ehRepo.findByOwnerName( cred.getUserName() ) ;
        
        lHoldings.forEach( h -> 
            localHoldingsMap.put( h.getSymbolIcici(), h ) 
        ) ;
        
        // ---------------------------------------------------------------------
        addResult( "    Loading Breeze holdings" ) ;
        api       = new BreezeGetPortfolioHoldingsAPI() ;
        response  = api.execute( cred ) ;
        
        if( !response.isError() ) {
            bHoldings = response.getEntities() ;
            bHoldings.forEach( ph -> {
                this.breezeHoldingsMap.put( ph.getSymbol(), ph ) ;
            } ) ;
        }
        else {
            addResult( "     ERROR loading breeze holdings." ) ; 
            addResult( "      Msg: " + response.getError() ) ; 
            throw new Exception( "Error loading breeze holdings." ) ;
        }
    }
    
    private void updateTrades( BreezeCred cred, Date fromDate, Date toDate ) 
            throws Exception {
        
        addResult( "    Getting breeze trades" ) ;
        List<Trade> trades = getBreezeTrades( cred, fromDate, toDate ) ;
        addResult( "    " + trades.size() + " Breeze trades obtained." ) ;
        
        if( trades != null && !trades.isEmpty() ) {
            
            // The trades returned by Breeze are in reverse chronological
            // order. We reverse the list to start with the oldest trade first.
            Collections.reverse( trades ) ;
            
            numTradesProcessed = trades.size() ;
            
            for( Trade trade : trades ) {
                
                String symbolIcici = trade.getSymbolIcici() ;
                
                String msg = "> " + SDF.format  ( trade.getTradeDate()        ) + 
                        " | " + rightPad ( symbolIcici,              6 ) + 
                        " | " + rightPad ( trade.getAction(),        4 ) + 
                        " | " + leftPad  ( "" + trade.getQuantity(), 4 ) + 
                        " | " + trade.getOrderId() ;
                
                addResult( "" ) ;
                addResult( "    " + msg ) ;
                
                EquityHolding    eh = null ;
                PortfolioHolding ph = null ;
                
                // If we find a local portfolio holding, we use it to associate
                // the trade with.
                eh = localHoldingsMap.get( symbolIcici ) ;
                
                if( eh == null ) {
                    // In case there is no local holding, we see if we have
                    // a breeze portfolio holding. Note that breeze portfolio
                    // holding is only for stocks with quantity > 0. 
                    addResult( "    Creating holding  " + symbolIcici ) ;
                    
                    // In case the quantity is zero (can happen if we are
                    // processing trades for a historic range), we create a 
                    // token holding.
                    ph = breezeHoldingsMap.get( symbolIcici ) ;
                    
                    eh = createNewEquityHolding( symbolIcici, ph, cred ) ;
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
        
        EquityTrade et = new EquityTrade() ;
        
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
        
        numTradesCreated++ ;
        
        return et ;
    }
    
    /** 
     * Fetches all the trade transactions (trade details) for the given trade
     * (order) and inserts updates them into the {@link EquityTxnRepo}.
     */
    private void processTxnsForTrade( EquityHolding eh, EquityTrade trade, 
                                      BreezeCred cred ) 
        throws Exception {
    
        List<TradeDetail>              breezeTxns = null ;
        BreezeGetTradeDetailAPI        api        = null ;
        BreezeAPIResponse<TradeDetail> response   = null ;
        
        addResult( "      Loading breeze transactions" ) ;
        api = new BreezeGetTradeDetailAPI() ;
        api.setOrderId( trade.getOrderId() ) ;
        
        response = api.execute( cred ) ;
        if( response.isError() ) {
            addResult( "      ERROR loading breeze transactions." ) ; 
            addResult( "       Msg: " + response.getError() ) ; 
            throw new Exception( "Error loading breeze holdings." ) ;
        }
        
        breezeTxns = response.getEntities() ;
        addResult( "        " + breezeTxns.size() + " breeze txns found." ) ;

        for( TradeDetail breezeTxn : breezeTxns ) {
            
            addResult( "      Processing txn " + breezeTxn.getTradeId() ) ;
            
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
            else {
                addResult( "        txn exists." ) ;
            }
            
            boolean txnNeedsSaving = false ;
            
            if( txn == null ) {
                addResult( "       creating new txn." ) ;
                txn = createNewEquityTxn( trade, breezeTxn, eh ) ;
                txnNeedsSaving = true ;
            }
            else {
                if( StringUtil.isEmptyOrNull( txn.getOrderId() ) ) {
                    addResult( "        updating existing txn." ) ;
                    updateExistingTxn( trade, breezeTxn, txn ) ;
                    txnNeedsSaving = true ;
                }
            }

            if( txnNeedsSaving ) {
                etxnRepo.save( txn ) ;
            }
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
        
        numTxnCreated++ ;
        
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
        
        numTxnUpdated++ ;
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

    private List<Trade> getBreezeTrades( BreezeCred cred, 
                                         Date fromDate, Date toDate ) 
        throws Exception {
        
        BreezeGetTradeListAPI api = null ;
        BreezeAPIResponse<Trade> response = null ;
        
        api = new BreezeGetTradeListAPI() ;
        api.setFromDate( fromDate ) ;
        api.setToDate( toDate ) ;
        
        response = api.execute( cred ) ;
        
        if( response.isError()) {
            throw new Exception( "BreezeGetTradeListAPI server error. " + 
                                 "Status = " + response.getStatus() + ". " + 
                                 "Error = " + response.getError() ) ;
        }
        
        return response.getEntities() ;
    }
    
    // Map<String, EquityHolding>    localHoldingsMap 
    // Map<String, PortfolioHolding> breezeHoldingsMap
    private void updateEquityHoldings( BreezeCred cred ) {
        
        EquityHolding    capitalystHolding = null ;
        PortfolioHolding breezeHolding     = null ;
        
        for( String symbolIcici : localHoldingsMap.keySet() ) {
            
            if( !shouldProcessHolding( symbolIcici ) ) {
                addResult( "    Ignoring " + symbolIcici + " based on config." ) ;
                breezeHoldingsMap.remove( symbolIcici ) ;
                continue ;
            }
            
            capitalystHolding = localHoldingsMap.get( symbolIcici ) ;
            breezeHolding     = breezeHoldingsMap.get( symbolIcici ) ;

            // EH (local holding) is guaranteed to be not null, however
            // PH (breeze holding) can be null. This can lead to the following
            // scenarios.
            if( breezeHolding == null ) {
                // A Capitalyst holding exists, but a breeze holding does 
                // not exist. This implies that the DMAT quantity is zero.
                if( capitalystHolding.getQuantity() != 0 ) {
                    setCapitalystHoldingQuantityToZero( capitalystHolding ) ;
                }
            }
            else {
                // If both capitalyst and breeze holdings exist, we check
                // if the quantities differ. If so, sync with breeze, else
                // ignore.
                if( capitalystHolding.getQuantity() != breezeHolding.getQuantity() ) {
                    syncLocalHoldingWithBreeze( capitalystHolding, breezeHolding ) ;
                }
                
                // Remove the breeze holding
                breezeHoldingsMap.remove( symbolIcici ) ;
            }
        }

        // After the bidirectional matching if there are still some
        // items remaining in the breeze portfolio, it implies these are
        // holdings which don't exist in local. We create new holdings
        // for each of them
        if( !breezeHoldingsMap.isEmpty() ) {
            
            for( PortfolioHolding bh : breezeHoldingsMap.values() ) {
                
                String symbolIcici = bh.getSymbol() ;
                
                createNewEquityHolding( symbolIcici, bh, cred ) ;
                breezeHoldingsMap.remove( symbolIcici ) ;
            }
        }
    }
    
    private boolean shouldProcessHolding( String symbol ) {
        
        // If no include stocks are specified, we include all, else any 
        // stock not in the include list is ignored.
        if( !inclPortfolioStocks.isEmpty() ) {
            if( !inclPortfolioStocks.contains( symbol ) ) {
                return false ;
            }
        }
        
        // If no exclude stocks are specified, we include all, else any
        // stock in the exclude stock is rejected
        if( !exclPortfolioStocks.isEmpty() ) {
            if( exclPortfolioStocks.contains( symbol ) ) {
                return false ;
            }
        }
        
        return true ;
    }
    
    private void setCapitalystHoldingQuantityToZero( EquityHolding eh ) {
        
        addResult( "     Updating quantity of " + 
                   eh.getSymbolIcici() + " to zero." );
        
        eh.setQuantity( 0 ) ;
        eh.setAvgCostPrice( 0 ) ;
        eh.setRealizedProfitLoss( 0 ) ;
        eh.setDayGain( 0 ) ;
        // We don't care about the current market price for zero holdings.
        
        numHoldingsUpdated++ ;

        ehRepo.save( eh ) ;
    }

    private void syncLocalHoldingWithBreeze( EquityHolding eh,
                                             PortfolioHolding ph ) {
        
         String symbolIcici = eh.getSymbolIcici() ;

         addResult( "     Updating quantity of " + 
                    symbolIcici + " to " + ph.getQuantity() ) ;
    
         int   quantity = ph.getQuantity() ;
         float avgPrice = ph.getAveragePrice() ;
         float dayGain  = ph.getChange() * quantity ;
         float curPrice = ph.getCurrentMktPrice() ;
    
         eh.setQuantity        ( quantity   ) ;
         eh.setCurrentMktPrice ( curPrice   ) ;
         eh.setDayGain         ( dayGain    ) ;
         eh.setLastUpdate      ( new Date() ) ;
         
         if( updateAvgCostPrice ) {
             eh.setAvgCostPrice( avgPrice ) ;
         }
         
         numHoldingsUpdated++ ;
         
         ehRepo.save( eh ) ;
    }
    
    // Note that breeze portfolio can be null. This is possible if we are 
    // processing an old trade for which there is no active quantity in the 
    // current portfolio.
    private EquityHolding createNewEquityHolding( String symbolIcici,
                                                  PortfolioHolding ph,
                                                  BreezeCred cred ) {

        addResult( "      New holding for " 
                   + cred.getUserName() + " and " + symbolIcici ) ;

        EquityMaster em = emRepo.findBySymbolIcici( symbolIcici ) ;
        
        int   quantity = (ph == null) ? 0 : ph.getQuantity() ;
        float avgPrice = (ph == null) ? 0 : ph.getAveragePrice() ;
        float dayGain  = (ph == null) ? 0 : ph.getChange() * quantity ;

        EquityHolding eh = new EquityHolding() ;

        eh.setOwnerName          ( cred.getUserName() ) ;
        eh.setSymbolIcici        ( symbolIcici        ) ;
        eh.setSymbolNse          ( em.getSymbol()     ) ;
        eh.setCompanyName        ( em.getName()       ) ;
        eh.setIsin               ( em.getIsin()       ) ;
        eh.setQuantity           ( quantity           ) ;
        eh.setCurrentMktPrice    ( em.getClose()      ) ;
        eh.setRealizedProfitLoss ( 0                  ) ;
        eh.setDayGain            ( dayGain            ) ;
        eh.setLastUpdate         ( new Date()         ) ;
        
        if( updateAvgCostPrice ) {
            eh.setAvgCostPrice( avgPrice ) ;
        }
        
        eh = ehRepo.save( eh ) ;

        numHoldingsCreated++ ;
        
        return eh ;
    }    
}
