package com.sandy.capitalyst.server.core.ledger.classifier;

import java.util.ArrayList ;
import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;

import org.apache.log4j.Logger ;
import org.springframework.context.ApplicationContext ;

import com.sandy.capitalyst.server.CapitalystServer ;
import com.sandy.capitalyst.server.config.CapitalystConfig ;
import com.sandy.capitalyst.server.dao.ledger.LedgerEntry ;
import com.sandy.capitalyst.server.dao.ledger.LedgerEntryClassificationRule ;
import com.sandy.capitalyst.server.dao.ledger.LedgerEntryClassificationRuleRepo ;
import com.sandy.capitalyst.server.dao.ledger.LedgerRepo ;
import com.sandy.capitalyst.server.util.StringUtil ;

public class LEClassifier {
    
    private static final Logger log = Logger.getLogger( LEClassifier.class ) ;
    
    private static class RuleData {
        public LEClassifierRule rule = null ;
        public LedgerEntryClassificationRule ruleMeta = null ;
        
        public RuleData( LEClassifierRule rule, 
                         LedgerEntryClassificationRule ruleMeta ) {
            this.rule = rule ;
            this.ruleMeta = ruleMeta ;
        }
    }

    private LedgerEntryClassificationRuleRepo lecrRepo = null ;
    private LedgerRepo lRepo = null ;
    private CapitalystConfig config = null ;
    
    private LEClassifierRuleBuilder ruleBuilder = new LEClassifierRuleBuilder() ;
    
    private Map<String, RuleData> debitRules = new HashMap<>() ;
    private Map<String, RuleData> creditRules = new HashMap<>() ;
    
    public LEClassifier() {
        ApplicationContext appCtx = CapitalystServer.getAppContext() ;
        lecrRepo = appCtx.getBean( LedgerEntryClassificationRuleRepo.class ) ;
        lRepo = appCtx.getBean( LedgerRepo.class ) ;
        config = CapitalystServer.getConfig() ;
        
        for( LedgerEntryClassificationRule lecr : lecrRepo.findAll() ) {
            LEClassifierRule rule = ruleBuilder.buildClassifier( lecr.getRuleText() ) ;
            RuleData data = new RuleData( rule, lecr ) ;
            if( lecr.isICreditClassifier() ) {
                creditRules.put( lecr.getRuleName(), data ) ;
            }
            else {
                debitRules.put( lecr.getRuleName(), data ) ;
            }
        }
    }
    
    public void runClassification() {
        log.debug( "Running Ledger Classifier" ) ;
        List<LedgerEntry> entriesToSave = new ArrayList<>() ;
        
        log.debug( "Loading ledger entries" ) ;
        Iterable<LedgerEntry> entries = lRepo.findAll() ;
        log.debug( "Ledger entries loaded." );
        
        for( LedgerEntry entry : entries ) {
            if( config.isClassifyOnlyUnclassifiedEntries() ) {
                if( StringUtil.isEmptyOrNull( entry.getL1Cat() ) || 
                    StringUtil.isEmptyOrNull( entry.getL2Cat() ) ||
                    StringUtil.isEmptyOrNull( entry.getNotes() ) ) {
                    classifyEntry( entry, entriesToSave ) ;
                }
            }
            else {
                classifyEntry( entry, entriesToSave ) ;
            }
        }
        
        if( !entriesToSave.isEmpty() ) {
            log.debug( "Classified " + entriesToSave.size() + " entries." ) ;
            lRepo.saveAll( entriesToSave ) ;
        }
    }
    
    private void classifyEntry( LedgerEntry entry, 
                                List<LedgerEntry> entriesToSave ) {
        
        Map<String, RuleData> ruleSet = null ;
        ruleSet = ( entry.getAmount() < 0 ) ? debitRules : creditRules ;
        for( String key : ruleSet.keySet() ) {
            RuleData ruleData = ruleSet.get( key ) ;
            if( ruleData.rule.isRuleMatched( entry ) ) {
                entry.setL1Cat( ruleData.ruleMeta.getL1Category() ) ;
                entry.setL2Cat( ruleData.ruleMeta.getL2Category() ) ;
                entry.setNotes( ruleData.ruleMeta.getRuleName() ) ;
                entriesToSave.add( entry ) ;
                
                log.debug( "Rule : " + entry.getNotes() + " matched " + entry ) ;
                
                break ;
            }
        }
    }
}
