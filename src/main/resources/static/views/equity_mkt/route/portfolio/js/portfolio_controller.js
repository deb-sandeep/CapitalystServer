capitalystNgApp.controller( 'PortfolioController', 
    function( $scope, $http ) {
    
    // ---------------- Local variables --------------------------------------
    
    // ---------------- Scope variables --------------------------------------
    $scope.$parent.navBarTitle = "Equity Portfolio" ;
    $scope.$parent.activeModuleId = "portfolio" ;
    
    // -----------------------------------------------------------------------
    // --- [START] Controller initialization ---------------------------------
    console.log( "Loading PortfolioController" ) ;
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

    
    // ------------------- Server response processors ------------------------
    
} ) ;