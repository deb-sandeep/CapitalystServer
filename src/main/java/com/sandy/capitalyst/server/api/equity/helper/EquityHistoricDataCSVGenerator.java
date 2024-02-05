package com.sandy.capitalyst.server.api.equity.helper;

import static com.sandy.capitalyst.server.CapitalystServer.getBean ;

import java.io.StringWriter ;
import java.text.SimpleDateFormat ;
import java.util.Date ;
import java.util.List ;

import org.apache.commons.lang3.time.DateUtils ;

import com.sandy.capitalyst.server.dao.equity.HistoricEQData ;
import com.sandy.capitalyst.server.dao.equity.repo.HistoricEQDataRepo ;
import com.univocity.parsers.csv.CsvWriter ;
import com.univocity.parsers.csv.CsvWriterSettings ;

public class EquityHistoricDataCSVGenerator {

    private static SimpleDateFormat SDF = new SimpleDateFormat( "dd-MMM-yyyy" ) ;

    private String symbolNse = null ;
    private Date   fromDate  = null ;
    private Date   toDate    = null ;
    
    private HistoricEQDataRepo hedRepo  = null ;
    
    public EquityHistoricDataCSVGenerator( String symbolNse, String period ) {
        
        this.symbolNse = symbolNse ;
        this.toDate    = new Date() ;
        this.fromDate  = getFromDate( period ) ;
        
        this.hedRepo  = getBean( HistoricEQDataRepo.class   ) ;
    }
    
    public String getCsv() {
        
        List<HistoricEQData> histData = null ;
        histData = hedRepo.getHistoricData( symbolNse, fromDate, toDate ) ;
        return getCsvContent( histData ) ;
    }
    
    private String getCsvContent( List<HistoricEQData> histData ) {
        
        StringWriter output = new StringWriter() ;
        CsvWriter csvWriter = new CsvWriter( output, new CsvWriterSettings() ) ;
        
        csvWriter.writeHeaders( 
                "Symbol", "Date", "Open", "High", "Low", "Close", "Vol" 
        ) ;
        
        for( HistoricEQData candle : histData ) {
            
            csvWriter.addValue( this.symbolNse ) ;
            csvWriter.addValue( SDF.format( candle.getDate() ) ) ;
            csvWriter.addValue( candle.getOpen()          ) ;
            csvWriter.addValue( candle.getHigh()          ) ;
            csvWriter.addValue( candle.getLow()           ) ;
            csvWriter.addValue( candle.getClose()         ) ;
            csvWriter.addValue( candle.getTotalTradeQty() ) ;
            
            csvWriter.writeValuesToRow() ;
        }
        
        csvWriter.close() ;
        return output.toString() ;
    }

    private Date getFromDate( String period ) {
        
        Date   fromDate = null ;
        int    amount   = Integer.parseInt( period.substring( 0, 1 ) ) ;
        String duration = period.substring( 1 ) ;
        
        if( duration.equalsIgnoreCase( "m" ) ) {
            fromDate = DateUtils.addMonths( toDate, -1*amount ) ;
        }
        else if( duration.equalsIgnoreCase( "y" ) ) {
            fromDate = DateUtils.addYears( toDate, -1*amount ) ;
        }
        return fromDate ;
    }
}
