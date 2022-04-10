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
import com.sandy.capitalyst.server.dao.ledger.repo.LedgerEntryClassificationRuleRepo ;
import com.sandy.capitalyst.server.dao.ledger.repo.LedgerRepo ;
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
            
            LEClassifierRule rule = null ;
            
            rule = ruleBuilder.buildClassifier( lecr.getRuleName(), 
                                                lecr.getRuleText() ) ;
            
            RuleData data = new RuleData( rule, lecr ) ;
            
            if( lecr.isCreditClassifier() ) {
                creditRules.put( lecr.getRuleName(), data ) ;
            }
            else {
                debitRules.put( lecr.getRuleName(), data ) ;
            }
        }
    }
    
    public void runClassification( LedgerEntryClassificationRule lecr ) {
        
        String ruleName = lecr.getRuleName() ;
        log.debug( "Running Ledger Classifier for rule " + ruleName ) ;
        
        LEClassifierRule rule = null ;
        rule = ruleBuilder.buildClassifier( lecr.getRuleName(), 
                                            lecr.getRuleText() ) ;
        
        List<LedgerEntry> entriesToSave = new ArrayList<>() ;
        Iterable<LedgerEntry> entries = lRepo.findAll() ;
        
        boolean classifyEntry = false ;
        for( LedgerEntry entry : entries ) {
            classifyEntry = false ;
            if( config.isClassifyOnlyUnclassifiedEntries() ) {
                if( isUnclassifiedEntry( entry ) ) {
                    classifyEntry = true ;
                }
            }
            else {
                classifyEntry = true ;
            }
            
            if( classifyEntry ) {
                if( ( lecr.isCreditClassifier() && entry.isCredit() ) || 
                    ( !lecr.isCreditClassifier() && !entry.isCredit() ) ) {
                    
                    String matchResult = rule.getMatchResult( entry ) ;
                    
                    if( matchResult != null ) {
                        
                        entry.setL1Cat( lecr.getL1Category() ) ;
                        entry.setL2Cat( lecr.getL2Category() ) ;
                        entry.setNotes( matchResult ) ;
                        entriesToSave.add( entry ) ;
                    }
                }
            }
        }
        
        if( !entriesToSave.isEmpty() ) {
            log.debug( "Classified " + entriesToSave.size() + " entries." ) ;
            lRepo.saveAll( entriesToSave ) ;
        }
    }
    
    public void runClassification() {
        List<LedgerEntry> entriesToSave = new ArrayList<>() ;
        
        Iterable<LedgerEntry> entries = lRepo.findAll() ;
        
        for( LedgerEntry entry : entries ) {
            if( config.isClassifyOnlyUnclassifiedEntries() ) {
                if( isUnclassifiedEntry( entry ) ) {
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
    
    public void classifyEntry( LedgerEntry entry, 
                               List<LedgerEntry> entriesToSave ) {
        
        Map<String, RuleData> ruleSet = null ;
        ruleSet = ( entry.getAmount() < 0 ) ? debitRules : creditRules ;
        
        for( String key : ruleSet.keySet() ) {
            
            RuleData ruleData = ruleSet.get( key ) ;
            String matchResult = ruleData.rule.getMatchResult( entry ) ;
            
            if( matchResult != null ) {
                
                entry.setL1Cat( ruleData.ruleMeta.getL1Category() ) ;
                entry.setL2Cat( ruleData.ruleMeta.getL2Category() ) ;
                entry.setNotes( matchResult ) ;
                
                if( entriesToSave != null ) {
                    entriesToSave.add( entry ) ;
                }
                
                log.debug( "Rule : " + entry.getNotes() + " matched " + entry ) ;
                
                break ;
            }
        }
    }
    
    private boolean isUnclassifiedEntry( LedgerEntry entry ) {
        
        if( StringUtil.isEmptyOrNull( entry.getL1Cat() ) || 
            StringUtil.isEmptyOrNull( entry.getL2Cat() ) ) {
            return true ;
        }
        return false ;
    }

}
