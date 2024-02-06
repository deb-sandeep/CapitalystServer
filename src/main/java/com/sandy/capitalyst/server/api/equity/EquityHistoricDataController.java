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
import com.sandy.capitalyst.server.api.equity.helper.bhavcopy.BSEBhavcopyImporter ;
import com.sandy.capitalyst.server.api.equity.helper.bhavcopy.BhavcopyImportResult ;
import com.sandy.capitalyst.server.api.equity.helper.bhavcopy.NSEBhavcopyImporter ;
import com.sandy.capitalyst.server.core.log.IndentUtil ;
import com.sandy.capitalyst.server.core.nvpconfig.NVPConfig ;
import com.sandy.capitalyst.server.core.nvpconfig.NVPManager ;
import com.sandy.capitalyst.server.dao.equity.HistoricEQDataMeta ;
import com.sandy.capitalyst.server.dao.equity.repo.HistoricEQDataMetaRepo ;
import com.sandy.capitalyst.server.dao.equity.repo.HistoricEQDataRepo ;

@RestController
public class EquityHistoricDataController {

    private static final Logger log = Logger.getLogger( EquityHistoricDataController.class ) ;
    
    public static final String CFG_GRP_NAME = "EquityHistDataExporter" ;
    public static final String CFG_EXPORT_PERIOD = "export_period" ;

    private HistoricEQDataRepo hedRepo = null ;
    private HistoricEQDataMetaRepo hedmRepo = null ;

    @Autowired
    public void setHistoricEQDataMetaRepo( HistoricEQDataMetaRepo repo ) {
        this.hedmRepo = repo ;
    }

    @Autowired
    public void setHistoricEQDataRepo( HistoricEQDataRepo repo ) {
        this.hedRepo = repo ;
    }

    @GetMapping( "/Equity/HistoricData/Meta/{symbol}" )
    public ResponseEntity<List<HistoricEQDataMeta>> getGraphData(
                                                @PathVariable String symbol ) {

        try {
            List<HistoricEQDataMeta> results ;

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
    public ResponseEntity<Resource> getHistoricData( @PathVariable String symbol ) {

        EquityHistoricDataCSVGenerator csvGen ;
        ByteArrayResource resource ;
        String csvContent ;
        String fileName ;
        
        try {
            
            NVPManager nvpMgr = NVPManager.instance() ;
            NVPConfig exportPeriod = nvpMgr.getConfig( CFG_GRP_NAME, CFG_EXPORT_PERIOD, "2y" ) ;
            String period = exportPeriod.getValue() ;
            
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

    @PostMapping( "/Equity/EquityHistoricEODData/FileUpload" ) 
    public ResponseEntity<List<ImportResult>> uploadEquityHistoricEODRecords( 
                    @RequestParam( "files" ) MultipartFile[] multipartFiles ) {

        try {
            log.debug( "!! Uploading equity EOD records" ) ;
            
            List<ImportResult> results = new ArrayList<>() ;
            ImportResult result ;
            
            for( MultipartFile file : multipartFiles ) {
                
                log.debug( "!> Processing file : " + file.getOriginalFilename() + " >" ) ;
                String fileContent = new String( file.getBytes() ) ;

                result = new EquityHistDataImporter( hedRepo ).importCSVData( fileContent ) ;
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
    
    @PostMapping( "/Equity/NSEBhavcopy/FileUpload" ) 
    public ResponseEntity<List<BhavcopyImportResult>> uploadNSEBhavcopies( 
                    @RequestParam( "files" ) MultipartFile[] multipartFiles ) {
        
        List<BhavcopyImportResult> results ;
        
        try {
            results = importBhavcopies( multipartFiles, "NSE" ) ;
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
    
    @PostMapping( "/Equity/BSEBhavcopy/FileUpload" ) 
    public ResponseEntity<List<BhavcopyImportResult>> uploadBSEBhavcopies( 
            @RequestParam( "files" ) MultipartFile[] multipartFiles ) {
        
        List<BhavcopyImportResult> results ;
        
        try {
            results = importBhavcopies( multipartFiles, "BSE" ) ;
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
    
    private List<BhavcopyImportResult> importBhavcopies( 
                            MultipartFile[] multipartFiles, String exchange ) 
        throws Exception {
        
        log.debug( "!! Uploading " + exchange + " Bhavcopy files" ) ;
        
        String fileContents ;
        BhavcopyImportResult result ;
        List<BhavcopyImportResult> results = new ArrayList<>() ;
        
        for( MultipartFile file : multipartFiles ) {
            
            log.debug( "!> Processing file : " + file.getOriginalFilename() + " >" ) ;
            
            fileContents = new String( file.getBytes() ) ;
            
            if( exchange.equals( "NSE" ) ) {
                result = new NSEBhavcopyImporter().importContents( fileContents ) ;
            }
            else {
                result = new BSEBhavcopyImporter().importContents( fileContents ) ;
            }
            result.setFileName( file.getOriginalFilename() ) ;
            
            results.add( result ) ;
            
            log.debug( "- Processed symbol     = " + result.getFileName() ) ;
            log.debug( "- Num records found    = " + result.getNumRecordsFound() ) ;
            log.debug( "- Num records imported = " + result.getNumRecordsImported() ) ;
            
            log.debug( "- Done! <<" ) ;
        }
        
        return results ;
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
