capitalystNgApp.controller( 'LedgerController', 
    function( $scope, $http, $rootScope, $location, $window ) {
    
    // ---------------- Local variables --------------------------------------
    
    // ---------------- Scope variables --------------------------------------

    // Common framework variables. 
    $scope.alerts = [] ;
    $scope.navBarTitle = "Ledger" ;
    
    // -----------------------------------------------------------------------
    // --- [START] Controller initialization ---------------------------------
    console.log( "Loading LedgerController" ) ;
    initializeController() ;
    // --- [END] Controller initialization -----------------------------------
    
    
    // -----------------------------------------------------------------------
    // --- [START] Scope functions -------------------------------------------
    $scope.$on( 'interactingWithServer', function( event, args ) {
        if( args.isStart ) {
            $( '#serverInteractionLoader' ).modal( 'show' ) ;
        }
        else {
            $( '#serverInteractionLoader' ).modal( 'hide' ) ;
        }
    } ) ;

    // ----------- UI related scope functions --------------------------------
    $scope.addErrorAlert = function( msgString ) {
        console.log( msgString ) ;
        $scope.alerts.push( { type: 'danger', msg: msgString } ) ;
    } ;
    
    $scope.dismissAlert = function( index ) {
        $scope.alerts.splice( index, 1 ) ;
    }
    // --- [END] Scope functions

    // -----------------------------------------------------------------------
    // --- [START] Local functions -------------------------------------------
    
    function initializeController() {
    }
    
    // ------------------- Server comm functions -----------------------------
    /* Template server communication function
    function <serverComm>() {
        
        $scope.interactingWithServer = true ;
        $http.post( '/<API endpoint>', {
            'eventId'       : eventId,
        } )
        .then ( 
            function( response ){
                var data = response.data ;
                // TODO: Server data processing logic
            }, 
            function( error ){
                var errMsg = "<Error Message>" ;
                console.log( errMsg ) ;
                $scope.addErrorAlert( errMsg ) ;
            }
        )
        .finally(function() {
            $scope.interactingWithServer = false ;
        }) ;
    }
    */
} ) ;