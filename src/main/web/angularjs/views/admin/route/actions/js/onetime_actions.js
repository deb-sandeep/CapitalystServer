function OTAStat( id ) {
    this.id = id ;
    this.message = "" ;
    this.executing = false ;
    this.viewLog = false ;
    this.viewConfig = false ;
    this.parameters = {} ;
    this.editableParameters = null ;
    this.configError = null ;
    
    this.hasParams = function() {
        return this.parameters != null && 
               Object.keys( this.parameters ).length != 0 ;
    }
    
    this.toggleParamsEditor = function() {
        this.viewConfig = !this.viewConfig ;
    }
    
    this.viewOTALog = function() {
        this.viewLog = !this.viewLog ;
    }
    
    this.validateConfig = function() {

        this.configError = null ;
        try {
            if( this.editableParameters != null ) {
                this.parameters = JSON.parse( this.editableParameters ) ;
            }
            return true ;
        }
        catch( e ) {
            this.configError = e ;
            return false ;
        }
    }

    this.validConfigEditorClass = function() {
        return ( this.configError != null ) ? "invalid-rule" : "valid-rule" ;
    }
}

capitalystNgApp.controller( 'OneTimeActionsController', 
    function( $scope, $http ) {
    
    // ---------------- Local variables --------------------------------------
    
    // ---------------- Scope variables --------------------------------------
    $scope.$parent.navBarTitle = "One Time Actions" ;
    $scope.$parent.activeModuleId = "onetime_actions" ;
    
    $scope.otaStat = {
        'RefreshEquityMaster'    : new OTAStat( 'RefreshEquityMaster'    ),
        'RefreshIndexMaster'     : new OTAStat( 'RefreshIndexMaster'     ), 
        'MapICICIDirectSymbols'  : new OTAStat( 'MapICICIDirectSymbols'  ), 
        'HistPrevCloseUpdater'   : new OTAStat( 'HistPrevCloseUpdater'   ),
        'EquityTradeUpdater'     : new OTAStat( 'EquityTradeUpdater'     ),
        'HistoricEQDataImporter' : new OTAStat( 'HistoricEQDataImporter' ),
        'HistoricIdxDataImporter': new OTAStat( 'HistoricIdxDataImporter'),
        'IDCompressor'           : new OTAStat( 'IDCompressor'           ),
    } ;
    
    // -----------------------------------------------------------------------
    // --- [START] Controller initialization ---------------------------------
    console.log( "Loading OneTimeActionsController" ) ;
    initializeController() ;
    // --- [END] Controller initialization -----------------------------------
    
    // -----------------------------------------------------------------------
    // --- [START] Scope functions -------------------------------------------
    $scope.executeOTA = function( actionId ) {
        
        const ota = $scope.otaStat[ actionId ] ;
        
        if( ota.validateConfig() ) {
            $http.post( '/OTA/Execute/' + actionId, ota.parameters )
            .then ( 
                function(){
                    $scope.otaStat[ actionId ].executing = true ;
                    $scope.otaStat[ actionId ].message = "" ;
                    $scope.otaStat[ actionId ].viewLog = true ;
                    fetchOTAPartResults() ;
                }, 
                function(){
                    $scope.$parent.addErrorAlert( "Error triggering OTA." ) ;
                }
            )
        }
        else {
            $scope.$parent.addErrorAlert( "Configuration invalid. Msg : " + 
                                          ota.configError ) ;
        }
        
    }
    
    // --- [START] Scope functions dealilng with non UI logic ----------------

    // --- [END] Scope functions

    // -----------------------------------------------------------------------
    // --- [START] Local functions -------------------------------------------
    
    function initializeController() {
        
        console.log( "Initializing OTA controller." ) ;
        
        fetchOTADefaultConfigs( function(){
            setTimeout( function(){
                fetchOTAPartResults() ;
            }, 1000 ) ;
        }) ;
    }
    
    function fetchOTADefaultConfigs( successCallback ) {
        
        console.log( "Fetching default configurations received." ) ;
        
        $http.get( '/OTA/DefaultConfigs' )
        .then ( 
            function( response ){
                
                console.log( "Default configurations received." ) ;
                
                if( response.data.result == 'Success' ) {
                    
                    for( const actionId in response.data.body ) {
                        
                        const props = response.data.body[ actionId ] ;
                        console.log( "  Default properties for " + actionId ) ;
                        console.log( props ) ;
                        
                        const otaStat = $scope.otaStat[actionId] ;
                        
                        otaStat.parameters = props ;
                        if( props != null ) {
                            otaStat.editableParameters = JSON.stringify( props, null, 2 ) ;
                        }
                    }
                    successCallback() ;
                }
                else {
                    $scope.$parent.addErrorAlert( "Could not fetch OTA configs.\n" +
                                                  response.data.message ) ;
                }
            }, 
            function( error ){
                $scope.$parent.addErrorAlert( "Could not fetch OTA results.\n" +
                                              error.data.message ) ;
            }
        )
    }
    
    function fetchOTAPartResults() {
        
        if( anyOTAInExecution() ) {
            $http.get( '/OTA/PartResults' )
            .then ( 
                function( response ){
                    processOTAPartMessages( response.data ) ;
                    setTimeout( function(){
                        fetchOTAPartResults() ;
                    }, 1000 ) ;
                }, 
                function( error ){
                    $scope.$parent.addErrorAlert( "Could not fetch OTA results.\n" +
                                                  error.data.message ) ;
                }
            )
        }
    }
    
    function anyOTAInExecution() {
        
        for( var actionId in $scope.otaStat ) {
            if ( $scope.otaStat.hasOwnProperty( actionId ) ) {
                var ota = $scope.otaStat[ actionId ] ;
                if( ota.executing ) {
                    return true ;
                }
            }
        }
        return false ;
    }
    
    function processOTAPartMessages( msgMap ) {
        for( var actionId in msgMap ) {
            if ( msgMap.hasOwnProperty( actionId ) ) {
                var msgs = msgMap[ actionId ] ;
                for( var i=0; i<msgs.length; i++ ) {
                    processMessage( actionId, msgs[i] ) ;
                }
            }
        }
    }
    
    function processMessage( actionId, msg ) {
        
        var ota = $scope.otaStat[ actionId ] ;
        if( msg.resultType == 'Message' ) {
            ota.executing = true ;
            ota.message += msg.message + "\n" ;
        }
        else if( msg.resultType == 'Exception' ) {
            ota.executing = true ;
            ota.message += "ERROR: " + msg.message + "\n" ;  
        }
        else if( msg.resultType == 'EndOfProcessing' ) {
            ota.executing = false ;
            ota.message += "\n\n" ;
        }
        
        setTimeout( function() {
            var textarea = document.getElementById( actionId + "_ta" ) ;
            textarea.scrollTop = textarea.scrollHeight ;        
        }, 500 ) ;
    }
} ) ;