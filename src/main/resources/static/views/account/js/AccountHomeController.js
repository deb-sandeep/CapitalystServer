capitalystNgApp.controller( 'AccountHomeController', 
    function( $scope, $http, $rootScope, $location, $window ) {
    
    // ---------------- Local variables --------------------------------------
    
    // ---------------- Scope variables --------------------------------------

    // -----------------------------------------------------------------------
    // --- [START] Controller initialization ---------------------------------
    console.log( "Loading AccountHomeController" ) ;
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
        
        $scope.$parent.interactingWithServer = true ;
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
                $scope.$parent.addErrorAlert( errMsg ) ;
            }
        )
        .finally(function() {
            $scope.$parent.interactingWithServer = false ;
        }) ;
    }
    */
} ) ;