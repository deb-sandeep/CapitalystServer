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
        
        document.getElementById( "eodChartDiv"  ).style.height  = "600px";
        document.getElementById( "macdChartDiv" ).style.display = "none";
        
        $scope.$parent.repaintChart() ;
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
                
                $scope.$parent.displayChartDiv( 'macdChartDiv' ) ;
                
                document.getElementById( "eodChartDiv"  ).style.height  = "500px";
                document.getElementById( "macdChartDiv" ).style.display = "block";
                
                $scope.$parent.repaintChart() ;
            }
        ) ;
    }
    // --- [END] Scope functions

    // -------------------------------------------------------------------------
    // --- [START] Local functions ---------------------------------------------
    
    // ------------------- Server comm functions -------------------------------
    
} ) ;