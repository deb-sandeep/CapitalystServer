capitalystNgApp.controller( 'MACDController', 
                            function( $scope, $http ) {
    
    // ---------------- Local variables ----------------------------------------

    // ---------------- Scope variables ----------------------------------------
    $scope.config = {
        minWindowSize    : 12,
        maxWindowSize    : 26,
        signalWindowSize : 9    
    } ;
    
    // -------------------------------------------------------------------------
    // --- [START] Scope functions ---------------------------------------------
    $scope.fetchMACDBands = function() {
        
        const symbol = $scope.$parent.graphParams.symbolNse ;
        
        $http.get( '/Equity/GraphData/Indicator/MACD' + 
                   '?symbolNse='  + symbol +
                   '&minWindowSize=' + $scope.config.minWindowSize +
                   '&maxWindowSize=' + $scope.config.maxWindowSize +
                   '&signalWindowSize=' + $scope.config.signalWindowSize )
        .then ( 
            function( response ){
                console.log( response.data ) ;         
            }
        ) ;
    }
    // --- [END] Scope functions

    // -------------------------------------------------------------------------
    // --- [START] Local functions ---------------------------------------------
    
    // ------------------- Server comm functions -------------------------------
    
} ) ;