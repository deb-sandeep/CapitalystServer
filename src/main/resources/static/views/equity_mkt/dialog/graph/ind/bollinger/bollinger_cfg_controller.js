capitalystNgApp.controller( 'BollingerCfgController', 
                            function( $scope, $http ) {
    
    // ---------------- Local variables --------------------------------------

    // ---------------- Scope variables --------------------------------------
    $scope.config = {
        barCount : 10,
        numStdDev : 2    
    } ;
    
    // -----------------------------------------------------------------------
    // --- [START] Scope functions -------------------------------------------
    $scope.test = function() {
        console.log( "Test" ) ;
        $scope.$parent.bollinger() ;
    }
    
    // --- [END] Scope functions

    // -----------------------------------------------------------------------
    // --- [START] Local functions -------------------------------------------
    
    
    // ------------------- Server comm functions -------------------------------
    
} ) ;