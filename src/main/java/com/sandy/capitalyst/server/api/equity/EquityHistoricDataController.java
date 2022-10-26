package com.sandy.capitalyst.server.api.equity ;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR ;
import static org.springframework.http.HttpStatus.OK ;
import static org.springframework.http.ResponseEntity.status ;

import java.util.ArrayList ;
import java.util.Date ;
import java.util.List ;

import org.apache.log4j.Logger ;
import org.springframework.beans.factory.annotation.Autowired ;
import org.springframework.core.io.ByteArrayResource ;
import org.springframework.core.io.Resource ;
import org.springframework.http.HttpHeaders ;
import org.springframework.http.HttpStatus ;
import org.springframework.http.MediaType ;
import org.springframework.http.ResponseEntity ;
import org.springframework.web.bind.annotation.GetMapping ;
import org.springframework.web.bind.annotation.PathVariable ;
import org.springframework.web.bind.annotation.PostMapping ;
import org.springframework.web.bind.annotation.RequestParam ;
import org.springframework.web.bind.annotation.RestController ;
import org.springframework.web.multipart.MultipartFile ;

import com.sandy.capitalyst.server.api.equity.helper.EquityHistDataImporter ;
import com.sandy.capitalyst.server.api.equity.helper.EquityHistDataImporter.ImportResult ;
import com.sandy.capitalyst.server.api.equity.helper.EquityHistoricDataCSVGenerator ;
import com.sandy.capitalyst.server.core.log.IndentUtil ;
import com.sandy.capitalyst.server.dao.equity.HistoricEQDataMeta ;
import com.sandy.capitalyst.server.dao.equity.repo.HistoricEQDataMetaRepo ;
import com.sandy.capitalyst.server.dao.equity.repo.HistoricEQDataRepo ;

@RestController
public class EquityHistoricDataController {

    private static final Logger log = Logger.getLogger( EquityHistoricDataController.class ) ;
    
    @Autowired
    private HistoricEQDataMetaRepo hedmRepo = null ;
    
    @Autowired
    private HistoricEQDataRepo hedRepo = null ;
    
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
    
    @GetMapping( "/Equity/HistoricData/{symbol}" ) 
    public ResponseEntity<Resource> getHistoricData( 
        @PathVariable String symbol,
        @RequestParam( name="period", defaultValue="6m" ) String period ) {

        EquityHistoricDataCSVGenerator csvGen = null ;
        ByteArrayResource resource = null ;
        String csvContent = null ;
        String fileName = null ;
        
        try {
            
            csvGen     = new EquityHistoricDataCSVGenerator( symbol, period ) ;
            csvContent = csvGen.getCsv() ;
            resource   = new ByteArrayResource( csvContent.getBytes() ) ;
            fileName   = symbol + "-" + period + "-historic.csv" ; 
            
            return ResponseEntity
                    .ok()
                    .header( HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName )
                    .contentType( MediaType.parseMediaType("application/csv") )
                    .body( resource ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Getting historic data.", e ) ;
            return ResponseEntity.status( INTERNAL_SERVER_ERROR ).body( null ) ;
        }
    }

    @PostMapping( "/Equity/HistoricData/FileUpload" ) 
    public ResponseEntity<List<ImportResult>> uploadAccountStatements( 
                    @RequestParam( "files" ) MultipartFile[] multipartFiles ) {
        
        try {
            log.debug( "!! Uploading equity EOD records" ) ;
            
            List<ImportResult> results = new ArrayList<>() ;
            ImportResult result = null ;
            
            for( MultipartFile file : multipartFiles ) {
                
                log.debug( "!> Processing file : " + file.getOriginalFilename() + " >" ) ;
                
                result = new EquityHistDataImporter().importFile( file ) ;
                result.setFileName( file.getOriginalFilename() ) ;
                
                results.add( result ) ;
                
                log.debug( "- Processed symbol     = " + result.getSymbol() ) ;
                log.debug( "- Num records found    = " + result.getNumRecordsFounds() ) ;
                log.debug( "- Num records imported = " + result.getNumAdditions() ) ;
                log.debug( "- Num records modified = " + result.getNumModified() ) ;
                log.debug( "- Num dups deleted     = " + result.getNumDeletions() ) ;
                
                updateMeta( result.getSymbol() ) ;
                
                log.debug( "- Done! <<" ) ;
            }

            return status( HttpStatus.OK ).body( results ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Saving account data.", e ) ;
            return status( HttpStatus.INTERNAL_SERVER_ERROR ).body( null ) ;
        }
        finally {
            IndentUtil.i_clear() ;
        }
    }
    
    private void updateMeta( String symbol ) {
        
        HistoricEQDataMeta meta = hedmRepo.findBySymbolNse( symbol ) ;
        if( meta == null ) {
            meta = new HistoricEQDataMeta() ;
            meta.setSymbolNse( symbol ) ;
        }

        meta.setNumRecords( hedRepo.getNumRecords( symbol ) ) ;
        meta.setEarliestEodDate( hedRepo.getEarliestRecord( symbol ).getDate() ) ;
        meta.setLastUpdate( new Date() ) ;
        
        hedmRepo.saveAndFlush( meta ) ;
        
        log.debug( "- Num historic records = " + meta.getNumRecords() ) ;
    }
}