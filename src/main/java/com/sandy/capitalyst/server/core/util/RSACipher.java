package com.sandy.capitalyst.server.core.util;

import java.math.BigInteger ;
import java.security.Key ;
import java.security.KeyFactory ;
import java.security.spec.KeySpec ;
import java.security.spec.RSAPrivateKeySpec ;
import java.security.spec.RSAPublicKeySpec ;

import javax.crypto.BadPaddingException ;
import javax.crypto.Cipher ;
import javax.crypto.IllegalBlockSizeException ;

import org.apache.log4j.Logger ;

public class RSACipher {
    
    public static final Logger log = Logger.getLogger( RSACipher.class ) ;
    
    private static final String CIPHER_TRANSFORMATION = "RSA/ECB/PKCS1Padding" ;
    
    public static class RSACipherException extends Exception {
        
        private static final long serialVersionUID = 1L ;

        public RSACipherException( String msg ) {
            super( msg ) ;
        }
        
        public RSACipherException( String msg, Exception rootCause ) {
            super( msg, rootCause ) ;
        }
    }

    private Cipher encryptor = null ;     
    private Cipher decryptor = null ;  
    
    private Key publicKey  = null ;  
    private Key privateKey = null ;

    public RSACipher( String modulusStr, String publicExpStr, int radix ) 
        throws RSACipherException {
        
        initPublicKey( modulusStr, publicExpStr, radix ) ;
    }
    
    public RSACipher( String modulusStr, String publicExpStr, 
                      String privateExpStr, int radix ) 
        throws RSACipherException {
        
        this( modulusStr, publicExpStr, radix ) ;
        initPrivateKey( modulusStr, privateExpStr, radix ) ;
    }
    
    private void initPublicKey( String modulusStr, String publicExpStr, int radix ) 
        throws RSACipherException {
        
        try {
            KeyFactory keyFactory = KeyFactory.getInstance( "RSA" ) ;
            BigInteger modulus    = new BigInteger( modulusStr, radix ) ;
            BigInteger exp        = new BigInteger( publicExpStr, radix ) ;
            KeySpec    keySpec    = new RSAPublicKeySpec( modulus, exp ) ;
            
            publicKey = keyFactory.generatePublic( keySpec ) ;
            encryptor = Cipher.getInstance( CIPHER_TRANSFORMATION ) ;
            
            encryptor.init( Cipher.ENCRYPT_MODE, publicKey ) ;
        }
        catch( Exception e ) {
            throw new RSACipherException( e.getMessage(), e ) ;
        }
    }
    
    private void initPrivateKey( String modulusStr, String privateExpStr, int radix ) 
            throws RSACipherException {
        
        try {
            KeyFactory keyFactory = KeyFactory.getInstance( "RSA" ) ;
            BigInteger modulus    = new BigInteger( modulusStr, radix ) ;
            BigInteger exp        = new BigInteger( privateExpStr, radix ) ;
            KeySpec    keySpec    = new RSAPrivateKeySpec( modulus, exp ) ;
            
            privateKey = keyFactory.generatePrivate( keySpec ) ;
            decryptor  = Cipher.getInstance( CIPHER_TRANSFORMATION ) ;
            
            decryptor.init( Cipher.DECRYPT_MODE, privateKey ) ;
        }
        catch( Exception e ) {
            throw new RSACipherException( e.getMessage(), e ) ;
        }
    }
    
    public byte[] encrypt( byte[] indata ) throws RSACipherException {
        
        byte[] result = null ;
        try {
            result = encryptor.doFinal( indata ) ;
        }
        catch( IllegalBlockSizeException | BadPaddingException e ) {
            throw new RSACipherException( e.getMessage(), e ) ;
        }
        return result ;
    }
    
    public byte[] decrypt( byte[] indata ) throws RSACipherException {
        
        byte[] result = null ;
        try {
            if( decryptor == null ) {
                throw new IllegalStateException( "Decryptor not initialized" ) ;
            }
            result = decryptor.doFinal( indata ) ;
        }
        catch( IllegalBlockSizeException | BadPaddingException e ) {
            throw new RSACipherException( e.getMessage(), e ) ;
        }
        return result ;
    }
}
