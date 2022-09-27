package com.sandy.capitalyst.server.breeze.internal;

import java.io.File ;
import java.io.FileInputStream ;
import java.io.FileOutputStream ;
import java.io.IOException ;
import java.io.ObjectInputStream ;
import java.io.ObjectOutputStream ;
import java.io.Serializable ;
import java.util.Calendar ;
import java.util.Date ;
import java.util.HashMap ;
import java.util.Map ;

import org.apache.commons.lang.time.DateUtils ;
import org.apache.log4j.Logger ;

import com.fasterxml.jackson.databind.JsonNode ;
import com.fasterxml.jackson.databind.ObjectMapper ;
import com.sandy.capitalyst.server.breeze.Breeze ;
import com.sandy.capitalyst.server.breeze.BreezeCred ;
import com.sandy.capitalyst.server.breeze.BreezeException ;
import com.sandy.capitalyst.server.core.util.StringUtil ;

import lombok.Getter ;

public class BreezeSessionManager {

    private static final Logger log = Logger.getLogger( BreezeSessionManager.class ) ;
    
    public static class BreezeSession implements Serializable {
        
        private static final long serialVersionUID = -1427026265072334302L ;

        @Getter private String  userId          = null ;
        @Getter private String  sessionId       = null ;
        @Getter private String  sessionToken    = null ;
        @Getter private Date    creationTime    = null ;
        @Getter private boolean dayLimitReached = false ;
        
        @Getter private BreezeCred cred = null ;
        
        private BreezeSession( BreezeCred cred ) {
            this.cred = cred ;
        }
        
        public void eraseState() {
            this.creationTime = null ;
            this.dayLimitReached = false ;
            this.sessionId = null ;
            this.sessionToken = null ;
        }
        
        public boolean isInitializationRequired() {
            
            if( this.creationTime != null ) {
                Date todayStart = new Date() ;
                todayStart = DateUtils.truncate( todayStart, Calendar.DAY_OF_MONTH ) ;
                if( this.creationTime.before( todayStart ) ) {
                    return true ;
                }
            }
            
            if( this.dayLimitReached ) {
                return false ;
            }
            
            if( StringUtil.isEmptyOrNull( this.sessionToken ) || 
                this.creationTime == null ) {
                return true ;
            } 
            
            return false ;
        }
    }
    
    private static BreezeSessionManager instance = null ;
    
    private Map<String, BreezeSession> sessionMap = new HashMap<>() ;
    
    public static BreezeSessionManager instance() {
        
        if( instance == null ) {
            instance = new BreezeSessionManager() ;
        }
        return instance ;
    }
    
    private BreezeSessionManager() {}
    
    public void invalidateAllSessions() {
        
        sessionMap.values().forEach( session -> {
            deletePersistedSession( session ) ;
        } ) ;
        sessionMap.clear() ;
    }
    
    public void invalidateSession( BreezeCred cred ) {
        
        BreezeSession session = sessionMap.remove( cred.getUserId() ) ;
        if( session != null ) {
            deletePersistedSession( session ) ;
        }
    }
    
    public void setDayLimitReached( BreezeCred cred ) {
        
        BreezeSession session = sessionMap.get( cred.getUserId() ) ;
        
        if( session != null ) {
            session.dayLimitReached = true ;
        }
        else {
            session = new BreezeSession( cred ) ;
            
            session.userId          = cred.getUserId() ;
            session.creationTime    = new Date() ;
            session.dayLimitReached = true ;
            
            sessionMap.put( cred.getUserId(), session ) ;
        }
        serializeSession( session ) ;
    }
    
    public boolean isWithinDayRateLimit( BreezeCred cred ) {
        
        BreezeSession session = sessionMap.get( cred.getUserId() ) ;
        if( session != null ) {
            if( session.isDayLimitReached() ) {
                return false ;
            }
        }
        return true ;
    }
    
    public boolean hasActiveSession( BreezeCred cred ) {
        return !getSession( cred ).isInitializationRequired() ;
    }

    public BreezeSession getSession( BreezeCred cred ) {
        
        String uid = cred.getUserId() ;
        BreezeSession session = sessionMap.get( uid ) ;
        
        if( session == null ) {
            session = deserializeSession( uid ) ;
        }
        
        if( session == null ) {
            session = new BreezeSession( cred ) ;
            session.userId = cred.getUserId() ;
        }
        else if( session.isInitializationRequired() ) {
            session.eraseState() ;
        }
        
        sessionMap.put( uid, session ) ;
        serializeSession( session ) ;

        return session ;
    }
    
    public void activateSession( String userId, String sessionId ) 
        throws BreezeException {

        log.debug( "Activating session for " + userId + 
                   " and sessionId = " + sessionId ) ;
        
        BreezeCred cred = Breeze.instance().getCred( userId ) ;
        if( cred == null ) {
            throw BreezeException.appException( "Invalid user ID " + userId ) ;
        }
        
        String sessionToken = generateSessionToken( cred, sessionId ) ;
        if( StringUtil.isEmptyOrNull( sessionToken ) ) {
            throw BreezeException.appException( "Session token is null." ) ;
        }
        else {

            BreezeSession session = getSession( cred ) ;
            
            session.creationTime    = new Date() ;
            session.dayLimitReached = false ;
            session.sessionId       = sessionId ;
            session.sessionToken    = sessionToken ;
            
            serializeSession( session ) ;
        }
    }

    private String generateSessionToken( BreezeCred cred, String apiSessionId ) 
        throws BreezeException {

        log.debug( "  Generating session token" ) ;

        String sessionToken = null ;
        
        try {
            String url = Breeze.BRZ_API_BASEURL + "/customerdetails" ;
            BreezeNetworkClient netClient = BreezeNetworkClient.instance() ;
            
            Map<String, String> params = new HashMap<>() ;
            params.put( "SessionToken", apiSessionId ) ;
            params.put( "AppKey", cred.getAppKey() ) ;
            
            String resBody = netClient.get( url, params, cred ) ;
            
            ObjectMapper mapper = new ObjectMapper() ;
            JsonNode root = mapper.readTree( resBody ) ;
            JsonNode successNode = root.get( "Success" ) ;
            
            if( successNode != null ) {
                JsonNode tokenNode = successNode.get( "session_token" ) ;
                
                if( tokenNode == null ) {
                    throw BreezeException.sessionError( cred.getUserName(), 
                                                        "Getting session token", 
                                                        resBody ) ;
                }
                sessionToken = tokenNode.asText() ;
            }
            
            log.debug( "    BreezeSession Token = " + sessionToken ) ;
        }
        catch( IOException e ) {
            throw BreezeException.appException( e ) ;
        }
        
        return sessionToken ;
    }

    private BreezeSession deserializeSession( String userId ) {
        
        File serFile = getSessionSerFile( userId ) ;
        FileInputStream fIs = null ;
        ObjectInputStream oIs = null ;
        BreezeSession session = null ;
        
        try {
            if( serFile.exists() ) {
                fIs = new FileInputStream( serFile ) ;
                oIs = new ObjectInputStream( fIs ) ;
                
                session = ( BreezeSession )oIs.readObject() ; 
            }
        }
        catch( Exception e ) {
            log.error( "Error serializing session.", e ) ;
        }
        finally {
            if( oIs != null ) {
                try {
                    oIs.close() ;
                }
                catch( Exception e ) {
                    log.error( "Error closing streams.", e ) ;
                }
            }
        }
        
        return session ;
    }
    
    private void serializeSession( BreezeSession session ) {
        
        File serFile = getSessionSerFile( session.userId ) ;
        ObjectOutputStream oOs = null ;
        FileOutputStream fOs = null ;
        
        try {
            if( session != null ) {
                fOs = new FileOutputStream( serFile ) ;
                oOs = new ObjectOutputStream( fOs ) ;
                
                oOs.writeObject( session ) ;
            }
        }
        catch( Exception e ) {
            log.error( "Error deserializing session.", e ) ;
        }
        finally {
            if( oOs != null ) {
                try {
                    oOs.flush() ;
                    oOs.close() ;
                }
                catch( IOException e ) {
                    log.error( "Error closing streams.", e ) ;
                }
            }
        }    
    }
    
    private void deletePersistedSession( BreezeSession session ) {
        
        File serFile = getSessionSerFile( session.userId ) ;
        if( serFile.exists() ) {
            serFile.delete() ;
        }
    }
    
    private File getSessionSerFile( String userId ) {
        
        String fileName = "session-" + userId.toLowerCase() + ".ser" ;
        File   serDir   = Breeze.instance().getSerializationDir() ;
        File   serFile  = new File( serDir, fileName ) ;

        return serFile ;
    }
}
