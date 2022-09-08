package com.sandy.capitalyst.server.core.util;

import java.math.BigInteger ;
import java.security.Key ;
import java.security.KeyFactory ;
import java.security.spec.KeySpec ;
import java.security.spec.RSAPrivateKeySpec ;
import java.security.spec.RSAPublicKeySpec ;

import javax.crypto.Cipher ;

import org.apache.log4j.Logger ;

public class RSACipher {
    
    public static final Logger log = Logger.getLogger( RSACipher.class ) ;
    
    private static final String CIPHER_TRANSFORMATION = "RSA/ECB/PKCS1Padding" ;

    private Cipher encryptor = null ;     
    private Cipher decryptor = null ;  
    
    private Key publicKey  = null ;  
    private Key privateKey = null ;

    public RSACipher( String modulusStr, String publicExpStr, int radix ) 
        throws Exception {
        
        initPublicKey( modulusStr, publicExpStr, radix ) ;
    }
    
    public RSACipher( String modulusStr, String publicExpStr, 
                      String privateExpStr, int radix ) 
        throws Exception {
        
        this( modulusStr, publicExpStr, radix ) ;
        initPrivateKey( modulusStr, privateExpStr, radix ) ;
    }
    
    private void initPublicKey( String modulusStr, String publicExpStr, int radix ) 
            throws Exception {
        
        KeyFactory keyFactory = KeyFactory.getInstance( "RSA" ) ;
        BigInteger modulus    = new BigInteger( modulusStr, radix ) ;
        BigInteger exp        = new BigInteger( publicExpStr, radix ) ;
        KeySpec    keySpec    = new RSAPublicKeySpec( modulus, exp ) ;
        
        publicKey = keyFactory.generatePublic( keySpec ) ;
        encryptor = Cipher.getInstance( CIPHER_TRANSFORMATION ) ;
        
        encryptor.init( Cipher.ENCRYPT_MODE, publicKey ) ;
    }
    
    private void initPrivateKey( String modulusStr, String privateExpStr, int radix ) 
            throws Exception {
        
        KeyFactory keyFactory = KeyFactory.getInstance( "RSA" ) ;
        BigInteger modulus    = new BigInteger( modulusStr, radix ) ;
        BigInteger exp        = new BigInteger( privateExpStr, radix ) ;
        KeySpec    keySpec    = new RSAPrivateKeySpec( modulus, exp ) ;
        
        privateKey = keyFactory.generatePrivate( keySpec ) ;
        decryptor  = Cipher.getInstance( CIPHER_TRANSFORMATION ) ;
        
        decryptor.init( Cipher.DECRYPT_MODE, privateKey ) ;
    }
    
    public byte[] encrypt( byte[] indata ) throws Exception {
        return encryptor.doFinal( indata ) ;
    }
    
    public byte[] decrypt( byte[] indata ) throws Exception {
        if( decryptor == null ) {
            throw new IllegalStateException( "Decryptor not initialized" ) ;
        }
        return decryptor.doFinal( indata ) ;
    }
}
