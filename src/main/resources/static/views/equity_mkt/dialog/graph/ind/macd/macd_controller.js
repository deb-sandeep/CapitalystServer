capitalystNgApp.controller( 'MACDController', 
                            function( $scope, $http ) {
    
    // ---------------- Local variables ----------------------------------------

    // ---------------- Scope variables ----------------------------------------
    $scope.config = {
        minWindowSize : 12,
        maxWindowSize : 26,
        sigWindowSize : 9    
    } ;
    
    // -------------------------------------------------------------------------
    // --- [START] Scope functions ---------------------------------------------
    $scope.hideMACDChart = function() {
        
        $scope.$parent.hideFooterChart( 'macd' ) ;
    }
    
    $scope.fetchMACDBands = function() {
        
        const symbol = $scope.$parent.graphParams.symbolNse ;
        
        $http.get( '/Equity/GraphData/Indicator/MACD' + 
                   '?symbolNse=' + symbol +
                   '&minWindowSize=' + $scope.config.minWindowSize +
                   '&maxWindowSize=' + $scope.config.maxWindowSize +
                   '&sigWindowSize=' + $scope.config.sigWindowSize )
        .then ( 
            function( response ){
                console.log( response.data ) ;
                
                $scope.$parent.showFooterChart( 'macd' ) ;
            }
        ) ;
    }
    // --- [END] Scope functions

    // -------------------------------------------------------------------------
    // --- [START] Local functions ---------------------------------------------
    
    // ------------------- Server comm functions -------------------------------
    
} ) ;