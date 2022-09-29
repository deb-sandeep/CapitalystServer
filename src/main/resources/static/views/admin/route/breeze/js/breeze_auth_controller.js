capitalystNgApp.controller( 'BreezeAuthController', 
    function( $scope, $http, $sce ) {
    
    // ---------------- Local variables --------------------------------------
    
    // ---------------- Scope variables --------------------------------------
    $scope.$parent.navBarTitle = "Breeze Authentication" ;
    $scope.$parent.activeModuleId = "breeze_auth" ;
    
    $scope.sessions = [] ;
    
    $scope.testResultsVisible = false ;
    $scope.testResults = "" ;
    
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
    
    $scope.runTest = function( session ) {
        runTestOnServer( session.cred.userId ) ;
    }
    
    $scope.hideTestResults = function() {
        $scope.testResultsVisible = false ;
    }
    
    $scope.clearTestResults = function() {
        $scope.testResults = null ;
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
                $scope.sessions = response.data ;
            }, 
            function( error ){
                $scope.$parent.addErrorAlert( "Could not fetch sessions.\n" +
                                              error.data.message ) ;
            }
        )
    }
    
    function runTestOnServer( userId ) {
        
        $scope.testResults = null ;
        
        $http.post( '/Breeze/Test/' + userId )
        .then ( 
            function( response ){
                $scope.testResultsVisible = true ;
                $scope.testResults = response.data.logs ;
            }, 
            function( error ){
                $scope.$parent.addErrorAlert( "Could not get test results." ) ;
            }
        )
    }
    
} ) ;