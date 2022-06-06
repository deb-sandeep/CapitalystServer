package com.sandy.capitalyst.server.api.account;

import static org.springframework.http.ResponseEntity.status ;

import java.util.ArrayList ;
import java.util.List ;

import org.apache.log4j.Logger ;
import org.springframework.beans.factory.annotation.Autowired ;
import org.springframework.http.HttpStatus ;
import org.springframework.http.ResponseEntity ;
import org.springframework.web.bind.annotation.PostMapping ;
import org.springframework.web.bind.annotation.RequestBody ;
import org.springframework.web.bind.annotation.RequestParam ;
import org.springframework.web.bind.annotation.RestController ;
import org.springframework.web.multipart.MultipartFile ;

import com.sandy.capitalyst.server.api.account.helper.CCTxnEntry ;
import com.sandy.capitalyst.server.api.account.helper.StatementFileProcessor ;
import com.sandy.capitalyst.server.core.ledger.importer.LedgerImportResult ;
import com.sandy.capitalyst.server.core.ledger.importer.LedgerImporter ;
import com.sandy.capitalyst.server.dao.account.Account ;
import com.sandy.capitalyst.server.dao.account.repo.AccountRepo ;
import com.sandy.capitalyst.server.dao.ledger.LedgerEntry ;

@RestController
public class AccountStmtUploadController {

    private static final Logger log = Logger.getLogger( AccountStmtUploadController.class ) ;
    
    @Autowired
    private AccountRepo accountRepo = null ;
    
    @PostMapping( "/Statement/AllAccounts/FileUpload" ) 
    public ResponseEntity<List<LedgerImportResult>> uploadAccountStatements( 
                        @RequestParam( "files" ) MultipartFile[] multipartFiles,
                        @RequestParam( "accountId" ) Integer accountId ) {
        try {
            log.debug( "Uploading stmts for accountId = " + accountId ) ;
            
            StatementFileProcessor processor = new StatementFileProcessor() ;
            List<LedgerImportResult> results = new ArrayList<>() ;
            
            Account account = accountRepo.findById( accountId ).get() ; 
            
            for( MultipartFile file : multipartFiles ) {
                LedgerImportResult result = null ;
                result = processor.processMultipartFile( file, account ) ;
                results.add( result ) ;
            }

            return status( HttpStatus.OK ).body( results ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Saving account data.", e ) ;
            return status( HttpStatus.INTERNAL_SERVER_ERROR ).body( null ) ;
        }
    }
    
    @PostMapping( "/Statement/ICICI/CCTxnEntry" )
    public ResponseEntity<LedgerImportResult> importCCTxnEntries( 
                                    @RequestBody List<CCTxnEntry> entries ) {
        try {
            log.debug( "Importing " + entries.size() + " ICICI CC entries." ) ;
            
            LedgerImportResult importResult = null ;
            LedgerImporter importer = new LedgerImporter() ;
            List<LedgerEntry> ledgerEntries = new ArrayList<>() ;
            
            for( CCTxnEntry entry : entries ) {
                ledgerEntries.add( entry.convertToLedgerEntry() ) ;
            }
            
            importResult = importer.importLedgerEntries( ledgerEntries ) ;
            return status( HttpStatus.OK ).body( importResult ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Saving account data.", e ) ;
            return status( HttpStatus.INTERNAL_SERVER_ERROR ).body( null ) ;
        }
    }
}
