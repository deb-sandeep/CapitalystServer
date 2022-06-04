package com.sandy.capitalyst.server.api.account;

import static com.sandy.capitalyst.server.api.account.stmtparser.AccountStmtParserFactory.getParser ;
import static org.springframework.http.ResponseEntity.status ;

import java.io.File ;
import java.text.SimpleDateFormat ;
import java.util.ArrayList ;
import java.util.Date ;
import java.util.List ;

import org.apache.commons.io.FileUtils ;
import org.apache.log4j.Logger ;
import org.springframework.beans.factory.annotation.Autowired ;
import org.springframework.http.HttpStatus ;
import org.springframework.http.ResponseEntity ;
import org.springframework.web.bind.annotation.PostMapping ;
import org.springframework.web.bind.annotation.RequestParam ;
import org.springframework.web.bind.annotation.RestController ;
import org.springframework.web.multipart.MultipartFile ;

import com.sandy.capitalyst.server.api.account.stmtparser.AccountStmtParser ;
import com.sandy.capitalyst.server.core.CapitalystConfig ;
import com.sandy.capitalyst.server.core.ledger.importer.LedgerImportResult ;
import com.sandy.capitalyst.server.core.ledger.importer.LedgerImporter ;
import com.sandy.capitalyst.server.dao.account.Account ;
import com.sandy.capitalyst.server.dao.account.repo.AccountRepo ;
import com.sandy.capitalyst.server.dao.ledger.LedgerEntry ;

@RestController
public class AccountStmtUploadController {

    private static final Logger log = Logger.getLogger( AccountStmtUploadController.class ) ;
    
    private static final SimpleDateFormat SDF = new SimpleDateFormat( "YYYYMMdd" ) ;

    @Autowired
    private CapitalystConfig config = null ;
    
    @Autowired
    private AccountRepo accountRepo = null ;
    
    @PostMapping( "/Account/Statement/Upload" ) 
    public ResponseEntity<List<LedgerImportResult>> uploadAccountStatements( 
            @RequestParam( "files" ) MultipartFile[] multipartFiles,
            @RequestParam( "accountId" ) Integer accountId ) {
        
        try {
            log.debug( "Uploading stmts for accountId = " + accountId ) ;
            
            List<LedgerImportResult> results = new ArrayList<>() ;

            Account account = accountRepo.findById( accountId ).get() ; 
            
            for( MultipartFile file : multipartFiles ) {
                File savedFile = saveMultipartFile( file, account ) ;
                results.add( importLedgerEntries( savedFile, account ) ) ;
            }

            return status( HttpStatus.OK ).body( results ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Saving account data.", e ) ;
            return status( HttpStatus.INTERNAL_SERVER_ERROR ).body( null ) ;
        }
    }
    
    private File saveMultipartFile( MultipartFile mpFile, Account account ) 
        throws Exception {
        
        String destFileName = SDF.format( new Date() ) + "_" + 
                              mpFile.getOriginalFilename() ;
        
        File uploadDir = getUploadFileStorageDir( account ) ;
        File destFile = new File( uploadDir, destFileName ) ;
        
        FileUtils.writeByteArrayToFile( destFile, mpFile.getBytes() ) ;
        
        return destFile ;
    }
    
    private File getUploadFileStorageDir( Account account ) {
        
        String accountNumber = account.getAccountNumber() ;
        
        File wkspDir = config.getWorkspaceDir() ;
        File uploadDir = new File( wkspDir, 
                                   "account_stmts/" + accountNumber ) ;
        
        if( !uploadDir.exists() ) {
            uploadDir.mkdirs() ;
        }
        
        return uploadDir ;
    }
    
    private LedgerImportResult importLedgerEntries( File file, Account account ) 
        throws Exception {
        
        AccountStmtParser  stmtParser = null ;
        LedgerImporter     importer   = null ;
        LedgerImportResult result     = null ;
        List<LedgerEntry>  entries    = null ;
        
        stmtParser = getParser( account ) ;
        importer   = new LedgerImporter() ;
        
        // TODO: Populate all the fields of the ledger import result
        //       fileName
        
        log.debug( "\nImporting ledger entries from " + file.getAbsolutePath() ) ;
        
        entries = stmtParser.parseLedgerEntries( account, file ) ;
        result = importer.importLedgerEntries( account, entries ) ;
        
        return result ;
    }
}
