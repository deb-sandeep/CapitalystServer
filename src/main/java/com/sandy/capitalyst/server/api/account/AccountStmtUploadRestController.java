package com.sandy.capitalyst.server.api.account;

import static com.sandy.capitalyst.server.core.ledger.loader.LedgerImporterFactory.getLedgerImporter ;

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

import com.sandy.capitalyst.server.config.CapitalystConfig ;
import com.sandy.capitalyst.server.core.ledger.loader.LedgerImportResult ;
import com.sandy.capitalyst.server.core.ledger.loader.LedgerImporter ;
import com.sandy.capitalyst.server.dao.account.Account ;
import com.sandy.capitalyst.server.dao.account.AccountRepo ;

@RestController
public class AccountStmtUploadRestController {

    private static final Logger log = Logger.getLogger( AccountStmtUploadRestController.class ) ;
    
    private static final SimpleDateFormat SDF = new SimpleDateFormat( "YYYYMMdd" ) ;

    @Autowired
    private CapitalystConfig config = null ;
    
    @Autowired
    private AccountRepo aiRepo = null ;
    
    @PostMapping( "/Account/Statement/Upload" ) 
    public ResponseEntity<List<LedgerImportResult>> uploadAccountStatements( 
            @RequestParam( "files" ) MultipartFile[] files,
            @RequestParam( "accountId" ) Integer accountId ) {
        try {
            
            log.debug( "Uploading stmts for accountId = " + accountId ) ;
            
            List<LedgerImportResult> results = new ArrayList<>() ;
            Account account = aiRepo.findById( accountId ).get() ; 
            File uploadDir = getUploadFileStorageDir( account ) ;
            LedgerImporter importer = getLedgerImporter( account ) ;
            
            List<File> uploadedFiles = new ArrayList<>() ;
            for( MultipartFile file : files ) {
                uploadedFiles.add( saveMultipartFile( file, uploadDir ) ) ;
            }
            
            for( File file : uploadedFiles ) {
                results.add( importLedgerEntries( file, account, importer ) ) ;
            }

            importer.updateAccountBalance( account ) ;
            
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( results ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Saving account data.", e ) ;
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                                 .body( null ) ;
        }
    }
    
    private File getUploadFileStorageDir( Account account ) {
        File wkspDir = config.getWorkspaceDir() ;
        File uploadDir = new File( wkspDir, "account_stmts/" + account.getAccountNumber() ) ;
        if( !uploadDir.exists() ) {
            uploadDir.mkdirs() ;
        }
        return uploadDir ;
    }
    
    private File saveMultipartFile( MultipartFile mpFile, File uploadDir ) 
        throws Exception {
        
        String destFileName = SDF.format( new Date() ) + 
                              "_" + 
                              mpFile.getOriginalFilename() ;
        
        File destFile = new File( uploadDir, destFileName ) ;
        
        log.debug( "\tSaving file - " + destFile.getAbsolutePath() ) ;
        FileUtils.writeByteArrayToFile( destFile, mpFile.getBytes() ) ;
        return destFile ;
    }
    
    private LedgerImportResult importLedgerEntries( 
            File file, Account account, LedgerImporter importer ) 
        throws Exception {
        
        LedgerImportResult result = new LedgerImportResult() ;
        
        log.debug( "\nImporting ledger entries from " + file.getAbsolutePath() ) ;
        result = importer.importLedgerEntries( account, file ) ;
        result.setFileName( file.getName().substring( 9 ) ) ;
        
        return result ;
    }
}
