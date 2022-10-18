capitalystNgApp.controller( 'BollingerCfgController', 
                            function( $scope, $http ) {
    
    // ---------------- Local variables --------------------------------------

    // ---------------- Scope variables --------------------------------------
    $scope.config = {
        windowSize : 10,
        numStdDev : 2    
    } ;
    
    // -----------------------------------------------------------------------
    // --- [START] Scope functions -------------------------------------------
    $scope.fetchBollingerBands = function() {
        
        const symbol = $scope.$parent.graphParams.symbolNse ;
        console.log( "Fetching bollinger bands for " + symbol ) ;
        
        $http.get( '/Equity/GraphData/Indicator/BollingerBands' + 
                   '?symbolNse='  + $scope.graphParams.symbolNse +
                   '&windowSize=' + $scope.config.windowSize +
                   '&numStdDev='  + $scope.config.numStdDev  )
        .then ( 
            function( response ){
                console.log( response.data ) ;                 
            }
        ) ;
    }
    
    // --- [END] Scope functions

    // -----------------------------------------------------------------------
    // --- [START] Local functions -------------------------------------------
    
    
    // ------------------- Server comm functions -------------------------------
    
} ) ;