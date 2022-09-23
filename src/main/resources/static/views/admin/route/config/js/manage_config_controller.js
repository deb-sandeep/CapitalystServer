capitalystNgApp.controller( 'ManageConfigController', 
    function( $scope, $http, $ngConfirm ) {
    
    // ---------------- Local variables --------------------------------------
    const NO_GROUP = "-- NO GROUP --" ;
    
    // ---------------- Scope variables --------------------------------------
    $scope.$parent.navBarTitle = "Manage system configuration" ;
    $scope.$parent.activeModuleId = "manage_config" ;
    
    $scope.allCfgs = {} ;
    $scope.cfgGroupNames = [] ;
    $scope.selectedCfgGroup = NO_GROUP ;
    $scope.groupedCfgs = null ;
    
    // -----------------------------------------------------------------------
    // --- [START] Controller initialization ---------------------------------
    console.log( "Loading ManageConfigController" ) ;
    initializeController() ;
    // --- [END] Controller initialization -----------------------------------
    
    // -----------------------------------------------------------------------
    // --- [START] Scope functions -------------------------------------------
    $scope.groupChanged = function() {
        $scope.groupedCfgs = $scope.allCfgs[ $scope.selectedCfgGroup ] ;
    }
    // --- [END] Scope functions

    // -----------------------------------------------------------------------
    // --- [START] Local functions -------------------------------------------
    
    function initializeController() {
        fetchClassificationCategories() ;
    }
    
    
    // ------------------- Server comm functions -----------------------------
    function fetchClassificationCategories() {
        
        $scope.$emit( 'interactingWithServer', { isStart : true } ) ;
        $http.get( '/AllConfig' )
        .then ( 
            function( response ){
                console.log( response.data ) ;
                
                $scope.cfgGroupNames.length = 0 ;
                $scope.selectedCfgGroup = NO_GROUP ;
                
                $scope.allCfgs = response.data ;
                for( const groupName in $scope.allCfgs ) {
                    $scope.cfgGroupNames.push( groupName ) ;
                }
                $scope.groupChanged() ;
            }, 
            function( error ){
                $scope.$parent.addErrorAlert( "Could not fetch classification categories.\n" +
                                              error.data.message ) ;
            }
        )
        .finally(function() {
            $scope.$emit( 'interactingWithServer', { isStart : false } ) ;
        }) ;
    }
    
    // ------------------- Server response processors ------------------------
    
} ) ;