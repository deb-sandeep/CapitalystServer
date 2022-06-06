package com.sandy.capitalyst.server.api.account.helper;

import static com.sandy.capitalyst.server.CapitalystServer.getConfig ;
import static com.sandy.capitalyst.server.api.account.stmtparser.AccountStmtParserFactory.getParser ;

import java.io.File ;
import java.text.SimpleDateFormat ;
import java.util.Date ;
import java.util.List ;

import org.apache.commons.io.FileUtils ;
import org.apache.log4j.Logger ;
import org.springframework.web.multipart.MultipartFile ;

import com.sandy.capitalyst.server.api.account.stmtparser.AccountStmtParser ;
import com.sandy.capitalyst.server.core.ledger.importer.LedgerImportResult ;
import com.sandy.capitalyst.server.core.ledger.importer.LedgerImporter ;
import com.sandy.capitalyst.server.dao.account.Account ;
import com.sandy.capitalyst.server.dao.ledger.LedgerEntry ;

public class StatementFileProcessor {

    private static final Logger log = Logger.getLogger( StatementFileProcessor.class ) ;
    private static final SimpleDateFormat SDF = new SimpleDateFormat( "YYYYMMdd" ) ;

    public LedgerImportResult processMultipartFile( MultipartFile file,
                                                    Account account ) 
        throws Exception {
        
        File savedFile = saveMultipartFile( file, account ) ;
        return importLedgerEntries( savedFile, account ) ;
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
        File uploadDir = new File( getConfig().getWorkspaceDir(), 
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
        
        log.debug( "\nImporting ledger entries from " + file.getAbsolutePath() ) ;
        
        entries = stmtParser.parseLedgerEntries( account, file ) ;
        
        result = importer.importLedgerEntries( entries ) ;
        result.setFileName( file.getName() ) ;
        
        return result ;
    }
}
