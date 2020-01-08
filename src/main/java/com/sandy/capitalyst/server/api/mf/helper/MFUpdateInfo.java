package com.sandy.capitalyst.server.api.mf.helper;

public class MFUpdateInfo {
    
    private Integer id = null ;
    private String isin = null ;
    private String url = null ;
    private String purpose = null ;
    
    public Integer getId() {
        return id ;
    }
    public void setId( Integer id ) {
        this.id = id ;
    }
    
    public String getIsin() {
        return isin ;
    }
    public void setIsin( String isin ) {
        this.isin = isin ;
    }
    
    public String getUrl() {
        return url ;
    }
    public void setUrl( String url ) {
        this.url = url ;
    }

    public String getPurpose() {
        return purpose ;
    }
    public void setPurpose( String purpose ) {
        this.purpose = purpose ;
    }
}
