package com.sandy.capitalyst.server.api.equity ;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR ;
import static org.springframework.http.HttpStatus.OK ;
import static org.springframework.http.ResponseEntity.status ;

import java.util.ArrayList ;
import java.util.List ;

import org.apache.log4j.Logger ;
import org.springframework.beans.factory.annotation.Autowired ;
import org.springframework.http.HttpStatus ;
import org.springframework.http.ResponseEntity ;
import org.springframework.web.bind.annotation.GetMapping ;
import org.springframework.web.bind.annotation.PathVariable ;
import org.springframework.web.bind.annotation.PostMapping ;
import org.springframework.web.bind.annotation.RequestParam ;
import org.springframework.web.bind.annotation.RestController ;
import org.springframework.web.multipart.MultipartFile ;

import com.sandy.capitalyst.server.core.ledger.importer.LedgerImportResult ;
import com.sandy.capitalyst.server.dao.equity.HistoricEQDataMeta ;
import com.sandy.capitalyst.server.dao.equity.repo.HistoricEQDataMetaRepo ;

@RestController
public class EquityHistoricDataController {

    private static final Logger log = Logger.getLogger( EquityHistoricDataController.class ) ;
    
    @Autowired
    private HistoricEQDataMetaRepo hedmRepo = null ;
    
    @GetMapping( "/Equity/HistoricData/Meta/{symbol}" ) 
    public ResponseEntity<List<HistoricEQDataMeta>> getGraphData( 
                                                @PathVariable String symbol ) {

        try {
            List<HistoricEQDataMeta> results = null ;
            
            if( symbol.equalsIgnoreCase( "All" ) ) {
                results = hedmRepo.findAll() ;
            }
            else {
                results = new ArrayList<>() ;
                results.add( hedmRepo.findBySymbolNse( symbol ) ) ;
            }
            
            return ResponseEntity.status( OK ).body( results ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Getting equity portfolio.", e ) ;
            return ResponseEntity.status( INTERNAL_SERVER_ERROR ).body( null ) ;
        }
    }

    @PostMapping( "/Equity/HistoricData/FileUpload" ) 
    public ResponseEntity<List<LedgerImportResult>> uploadAccountStatements( 
                    @RequestParam( "files" ) MultipartFile[] multipartFiles ) {
        
        try {
            log.debug( "Uploading equity EOD records" ) ;
            
            for( MultipartFile file : multipartFiles ) {
                log.debug( file.getOriginalFilename() ) ;
            }

            return status( HttpStatus.OK ).body( null ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Saving account data.", e ) ;
            return status( HttpStatus.INTERNAL_SERVER_ERROR ).body( null ) ;
        }
    }
}