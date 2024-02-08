package com.sandy.capitalyst.server.dao.mf;

import java.util.Date ;

import jakarta.persistence.Entity ;
import jakarta.persistence.GeneratedValue ;
import jakarta.persistence.GenerationType ;
import jakarta.persistence.Id ;
import jakarta.persistence.Table ;

@Entity
@Table( name = "mf_master" )
public class MutualFundMaster {

    @Id
    @GeneratedValue( strategy=GenerationType.IDENTITY )
    private Integer id = null ;
    
    private String fundGroupId = null ;
    private String fundMgmtCoName = null ;
    private String isin = null ;
    private String fundName = null ;
    private String category = null ;
    private String distributionType = null ;
    private float latestNav = 0.0f ;
    private Date lastUpdate = null ;

    public MutualFundMaster() {}
    
    public void setId( Integer val ) {
        this.id = val ;
    }
        
    public Integer getId() {
        return this.id ;
    }

    public void setFundName( String val ) {
        this.fundName = val ;
    }
        
    public String getFundName() {
        return this.fundName ;
    }

    public void setIsin( String val ) {
        this.isin = val ;
    }
        
    public String getIsin() {
        return this.isin ;
    }

    public void setCategory( String val ) {
        this.category = val ;
    }
        
    public String getCategory() {
        return this.category ;
    }

    public void setDistributionType( String val ) {
        this.distributionType = val ;
    }
        
    public String getDistributionType() {
        return this.distributionType ;
    }

    public void setLatestNav( float val ) {
        this.latestNav = val ;
    }
        
    public float getLatestNav() {
        return this.latestNav ;
    }

    public void setLastUpdate( Date val ) {
        this.lastUpdate = val ;
    }
        
    public Date getLastUpdate() {
        return this.lastUpdate ;
    }
    
    public String getFundGroupId() {
        return fundGroupId ;
    }

    public void setFundGroupId( String fundGroupId ) {
        this.fundGroupId = fundGroupId ;
    }

    public String getFundMgmtCoName() {
        return fundMgmtCoName ;
    }

    public void setFundMgmtCoName( String fundMgmgCoName ) {
        this.fundMgmtCoName = fundMgmgCoName ;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder( "MutualFund [\n" ) ; 
        
        builder.append( "   id = " + this.id + "\n" ) ;
        builder.append( "   fundGroupId = " + this.fundGroupId + "\n" ) ;
        builder.append( "   fundMgmtCoName = " + this.fundMgmtCoName + "\n" ) ;
        builder.append( "   fundName = " + this.fundName + "\n" ) ;
        builder.append( "   isin = " + this.isin + "\n" ) ;
        builder.append( "   category = " + this.category + "\n" ) ;
        builder.append( "   distributionType = " + this.distributionType + "\n" ) ;
        builder.append( "   latestNav = " + this.latestNav + "\n" ) ;
        builder.append( "   lastUpdate = " + this.lastUpdate + "\n" ) ;
        builder.append( "]" ) ;
        
        return builder.toString() ;
    }
}
