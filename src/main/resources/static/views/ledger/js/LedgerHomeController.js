capitalystNgApp.controller( 'LedgerHomeController', 
    function( $scope, $http, $rootScope, $location, $window ) {
    
    // ---------------- Local variables --------------------------------------
    
    // ---------------- Scope variables --------------------------------------
    $scope.$parent.navBarTitle = "LedgerHomeController" ;

    // -----------------------------------------------------------------------
    // --- [START] Controller initialization ---------------------------------
    console.log( "Loading LedgerHomeController" ) ;
    initializeController() ;
    // --- [END] Controller initialization -----------------------------------
    
    
    // -----------------------------------------------------------------------
    // --- [START] Scope functions -------------------------------------------
    // --- [END] Scope functions

    // -----------------------------------------------------------------------
    // --- [START] Local functions -------------------------------------------
    
    function initializeController() {
    }
    
    // ------------------- Server comm functions -----------------------------
    /* Template server communication function
    function <serverComm>() {
        
        $scope.$emit( 'interactingWithServer', { isStart : true } ) ;
        $http.post( '/<API endpoint>', {
            'eventId'       : eventId,
        } )
        .then ( 
            function( response ){
                var data = response.data ;
                // TODO: Server data processing logic
            }, 
            function( error ){
                $scope.$parent.addErrorAlert( "<Error Message>" ) ;
            }
        )
        .finally(function() {
            $scope.$emit( 'interactingWithServer', { isStart : false } ) ;
        }) ;
    }
    */
} ) ;