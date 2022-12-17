function OTAStat( id ) {
    this.id = id ;
    this.message = "" ;
    this.executing = false ;
    this.viewLog = false ;
    this.parameters = {} ;
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
        $http.post( '/OTA/Execute/' + actionId, 
                    $scope.otaStat[ actionId ].parameters )
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
    
    $scope.viewOTALog = function( actionId ) {
        var ota = $scope.otaStat[ actionId ] ; 
        ota.viewLog = !ota.viewLog ;
    }
    
    // --- [START] Scope functions dealilng with non UI logic ----------------

    // --- [END] Scope functions

    // -----------------------------------------------------------------------
    // --- [START] Local functions -------------------------------------------
    
    function initializeController() {
        setTimeout( function(){
            fetchOTAPartResults() ;
        }, 1000 ) ;
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