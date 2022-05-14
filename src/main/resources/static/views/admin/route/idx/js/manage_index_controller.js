capitalystNgApp.controller( 'ManageIndexController', 
    function( $scope, $http ) {
    
    // ---------------- Local variables --------------------------------------
    
    // ---------------- Scope variables --------------------------------------
    $scope.$parent.navBarTitle = "Manage indexes" ;
    $scope.$parent.activeModuleId = "idx_management" ;
    
    // -----------------------------------------------------------------------
    // --- [START] Controller initialization ---------------------------------
    console.log( "Loading ManageIndexController" ) ;
    initializeController() ;
    // --- [END] Controller initialization -----------------------------------
    
    // -----------------------------------------------------------------------
    // --- [START] Scope functions -------------------------------------------
    
    // --- [START] Scope functions dealilng with non UI logic ----------------
    // --- [END] Scope functions

    // -----------------------------------------------------------------------
    // --- [START] Local functions -------------------------------------------
    
    function initializeController() {
        fetchIndexMasters() ;
    }
    
    function clearState() {
    }
    
    // ------------------- Server comm functions -----------------------------
    function fetchIndexMasters() {
        
        clearState() ;
        
        $scope.$emit( 'interactingWithServer', { isStart : true } ) ;
        $http.get( '/IndexMaster' )
        .then ( 
            function( response ){
                console.log( response.data ) ;
            }, 
            function( error ){
                $scope.$parent.addErrorAlert( "Could not fetch index masters.\n" +
                                              error.data.message ) ;
            }
        )
        .finally(function() {
            $scope.$emit( 'interactingWithServer', { isStart : false } ) ;
        }) ;
    }
} ) ;