package com.sandy.capitalyst.server.breeze;

import java.io.Serializable ;

import lombok.Data ;

@Data
public class BreezeCred implements Serializable {

    private static final long serialVersionUID = -6351654433432755439L ;
    
    private String userName    = null ;
    private String userId      = null ;
    private String password    = null ;
    private String dob         = null ;
    private String appName     = null ;
    private String appKey      = null ;
    private String secretKey   = null ;
}
