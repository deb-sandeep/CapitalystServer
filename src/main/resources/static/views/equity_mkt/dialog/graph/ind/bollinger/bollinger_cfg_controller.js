capitalystNgApp.controller( 'BollingerCfgController', 
                            function( $scope, $http ) {
    
    // ---------------- Local variables ----------------------------------------

    // ---------------- Scope variables ----------------------------------------
    $scope.config = {
        windowSize : 20,
        numStdDev : 2    
    } ;
    
    // -------------------------------------------------------------------------
    // --- [START] Scope functions ---------------------------------------------
    $scope.fetchBollingerBands = function() {
        
        const symbol = $scope.$parent.graphParams.symbolNse ;
        
        $http.get( '/Equity/GraphData/Indicator/BollingerBands' + 
                   '?symbolNse='  + symbol +
                   '&windowSize=' + $scope.config.windowSize +
                   '&numStdDev='  + $scope.config.numStdDev  )
        .then ( 
            function( response ){
                setSeries( "bollinger-upper",  response ) ;         
                setSeries( "bollinger-middle", response ) ;         
                setSeries( "bollinger-lower",  response ) ;
                
                $scope.$parent.plotBollingerBands() ;         
            }
        ) ;
    }
    // --- [END] Scope functions

    // -------------------------------------------------------------------------
    // --- [START] Local functions ---------------------------------------------
    function setSeries( seriesName, response ) {
        $scope.$parent.seriesCache.set( seriesName, response.data[ seriesName ] ) ;
    }
    
    // ------------------- Server comm functions -------------------------------
    
} ) ;