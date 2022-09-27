capitalystNgApp.controller( 'BreezeAuthController', 
    function( $scope, $http ) {
    
    // ---------------- Local variables --------------------------------------
    
    // ---------------- Scope variables --------------------------------------
    $scope.$parent.navBarTitle = "Breeze Authentication" ;
    $scope.$parent.activeModuleId = "breeze_auth" ;
    
    // -----------------------------------------------------------------------
    // --- [START] Controller initialization ---------------------------------
    console.log( "Loading BreezeAuthController" ) ;
    initializeController() ;
    // --- [END] Controller initialization -----------------------------------
    
    // -----------------------------------------------------------------------
    // --- [START] Scope functions -------------------------------------------
    
    // --- [START] Scope functions dealilng with non UI logic ----------------
    
    // --- [END] Scope functions

    // -----------------------------------------------------------------------
    // --- [START] Local functions -------------------------------------------
    
    function initializeController() {
    }
    
    // ------------------- Server comm functions -----------------------------
} ) ;