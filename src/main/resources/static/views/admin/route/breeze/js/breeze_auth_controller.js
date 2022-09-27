capitalystNgApp.controller( 'BreezeAuthController', 
    function( $scope, $http ) {
    
    // ---------------- Local variables --------------------------------------
    
    // ---------------- Scope variables --------------------------------------
    $scope.$parent.navBarTitle = "Breeze Authentication" ;
    $scope.$parent.activeModuleId = "breeze_auth" ;
    
    $scope.sessions = [] ;
    
    // -----------------------------------------------------------------------
    // --- [START] Controller initialization ---------------------------------
    console.log( "Loading BreezeAuthController" ) ;
    fetchAllSessions() ;
    
    // --- [END] Controller initialization -----------------------------------
    
    // -----------------------------------------------------------------------
    // --- [START] Scope functions -------------------------------------------
    $scope.refresh = function() {
        fetchAllSessions() ;
    }
    
    $scope.getLoginURL = function( session ) {
        return "https://api.icicidirect.com/apiuser/login?api_key=" + 
               encodeURIComponent( session.cred.appKey ) ;
    }
    
    // --- [START] Scope functions dealilng with non UI logic ----------------
    
    // --- [END] Scope functions

    // -----------------------------------------------------------------------
    // --- [START] Local functions -------------------------------------------
    
    // ------------------- Server comm functions -----------------------------
    function fetchAllSessions() {
        $http.get( '/Breeze/Session' )
        .then ( 
            function( response ){
                console.log( response.data ) ;
                $scope.sessions = response.data ;
            }, 
            function( error ){
                $scope.$parent.addErrorAlert( "Could not fetch sessions.\n" +
                                              error.data.message ) ;
            }
        )
    }
    
} ) ;