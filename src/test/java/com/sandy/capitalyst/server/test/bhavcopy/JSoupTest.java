package com.sandy.capitalyst.server.test.bhavcopy;

import java.io.File ;

import org.apache.commons.io.FileUtils ;
import org.apache.log4j.Logger ;
import org.jsoup.Jsoup ;
import org.jsoup.nodes.Document ;
import org.jsoup.nodes.Element ;
import org.jsoup.select.Elements ;

public class JSoupTest {
    
    private static final Logger log = Logger.getLogger( JSoupTest.class ) ;
    
    public JSoupTest() {
    }
    
    public void test() throws Exception {
        String filePath = "/Users/sandeep/temp/nsdl.txt" ;
        String contents = FileUtils.readFileToString( new File( filePath ) ) ;
        
        Document doc = Jsoup.parse( contents ) ;
        Elements elements = doc.select( "td[class^=tablecontent]" ) ;
        
        log.debug( elements.size() ) ;
        
//        for( Element element : elements ) {
//            String text = element.text() ;
//            log.debug( text ) ;
//        }
        
    }

    public static void main( String[] args ) throws Exception {
        new JSoupTest().test() ;
    }
}
