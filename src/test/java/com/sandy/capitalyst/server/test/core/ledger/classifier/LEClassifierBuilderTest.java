package com.sandy.capitalyst.server.test.core.ledger.classifier;

import java.io.File ;

import org.apache.commons.io.FileUtils ;
import org.apache.log4j.Logger ;

import com.sandy.capitalyst.server.core.ledger.classifier.LEClassifierRule ;
import com.sandy.capitalyst.server.core.ledger.classifier.LEClassifierRuleBuilder ;

public class LEClassifierBuilderTest {
    
    static final Logger log = Logger.getLogger( LEClassifierBuilderTest.class ) ;
    
    private LEClassifierRuleBuilder builder = null ;
    
    public LEClassifierBuilderTest() {
        builder = new LEClassifierRuleBuilder() ;
    }
    
    private void execute() throws Exception {
        File file = new File( "src/test/resources/rule.txt" ) ;
        String ruleText = FileUtils.readFileToString( file ) ;
        LEClassifierRule rule = builder.buildClassifier( "Test Rule", ruleText ) ;
        log.debug( rule.getFormattedString( "" ) ) ;
    }

    public static void main( String[] args ) 
        throws Exception {
        LEClassifierBuilderTest main = new LEClassifierBuilderTest() ;
        main.execute() ;
    }
}
