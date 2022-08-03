package com.sandy.capitalyst.server.api.equity ;

import java.util.ArrayList ;
import java.util.Comparator ;
import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;

import org.apache.log4j.Logger ;
import org.springframework.beans.factory.annotation.Autowired ;
import org.springframework.http.HttpStatus ;
import org.springframework.http.ResponseEntity ;
import org.springframework.web.bind.annotation.GetMapping ;
import org.springframework.web.bind.annotation.RestController ;

import com.sandy.capitalyst.server.api.equity.helper.EquityHoldingVOBuilder ;
import com.sandy.capitalyst.server.api.equity.vo.FamilyEquityHoldingVO ;
import com.sandy.capitalyst.server.api.equity.vo.IndividualEquityHoldingVO ;
import com.sandy.capitalyst.server.dao.equity.EquityHolding ;
import com.sandy.capitalyst.server.dao.equity.EquityTxn ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityHoldingRepo ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityTxnRepo ;

// @Get - /Equity/Holding 

@RestController
public class EquityHoldingsQueryController {

    private static final Logger log = Logger.getLogger( EquityHoldingsQueryController.class ) ;
    
    @Autowired
    private EquityHoldingRepo ehRepo = null ;
    
    @Autowired
    private EquityTxnRepo etRepo = null ;
    
    @GetMapping( "/Equity/IndividualHolding" ) 
    public ResponseEntity<List<IndividualEquityHoldingVO>> 
    getIndividualEquityHoldings() {
        
        log.debug( "Getting equity portfolio" ) ;
        EquityHoldingVOBuilder voBuilder = new EquityHoldingVOBuilder() ;

        List<IndividualEquityHoldingVO> holdings = new ArrayList<>() ;
        
        try {
            for( EquityHolding dbHolding : ehRepo.findAll() ) {
                
                List<EquityTxn> txns = null ;
                IndividualEquityHoldingVO vo = null ;
                
                if( dbHolding.getQuantity() > 0 ) {
                    
                    txns = etRepo.findByHoldingIdOrderByTxnDateAscActionAsc( dbHolding.getId() ) ;
                    vo = voBuilder.buildVO( dbHolding, txns ) ;
                    
                    holdings.add( vo ) ;
                }
            }
            
            holdings.sort( new Comparator<IndividualEquityHoldingVO>() {
                public int compare( IndividualEquityHoldingVO o1, IndividualEquityHoldingVO o2 ) {
                    return o1.getCompanyName().compareTo( o2.getCompanyName() ) ;
                }
            } ) ;
            
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( holdings ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Getting equity portfolio.", e ) ;
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                                 .body( null ) ;
        }
    }

    @GetMapping( "/Equity/FamilyHolding" ) 
    public ResponseEntity<List<FamilyEquityHoldingVO>> 
    getFamilyEquityHoldings() {
        
        log.debug( "Getting equity portfolio" ) ;
        EquityHoldingVOBuilder voBuilder = new EquityHoldingVOBuilder() ;
        
        List<FamilyEquityHoldingVO> holdings = new ArrayList<>() ;
        Map<String, FamilyEquityHoldingVO> map = new HashMap<>() ;
        
        try {
            for( EquityHolding dbHolding : ehRepo.findAll() ) {
                
                List<EquityTxn>           txns  = null ;
                IndividualEquityHoldingVO indVO = null ;
                FamilyEquityHoldingVO     famVO = null ;
                
                if( dbHolding.getQuantity() > 0 ) {
                    
                    txns = etRepo.findByHoldingIdOrderByTxnDateAscActionAsc( dbHolding.getId() ) ;
                    indVO = voBuilder.buildVO( dbHolding, txns ) ;
                    
                    famVO = map.get( indVO.getIsin() ) ;
                    if( famVO == null ) {
                        famVO = new FamilyEquityHoldingVO( indVO ) ;
                        
                        holdings.add( famVO ) ;
                        map.put( indVO.getIsin(), famVO ) ;
                    }
                    else {
                        famVO.addIndividualHoldingVO( indVO ) ;
                    }
                }
            }
            
            holdings.sort( new Comparator<FamilyEquityHoldingVO>() {
                public int compare( FamilyEquityHoldingVO o1, FamilyEquityHoldingVO o2 ) {
                    return o1.getCompanyName().compareTo( o2.getCompanyName() ) ;
                }
            } ) ;
            
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( holdings ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Getting equity portfolio.", e ) ;
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                                 .body( null ) ;
        }
    }

}
