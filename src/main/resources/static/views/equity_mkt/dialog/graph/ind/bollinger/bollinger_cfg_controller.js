capitalystNgApp.controller( 'BollingerCfgController', 
                            function( $scope, $http ) {
    
    // ---------------- Local variables ----------------------------------------

    // ---------------- Scope variables ----------------------------------------
    $scope.config = {
        windowSize : 20,
        numStdDev : 2,
        enableFlag : false 
    } ;
    
    // -------------------------------------------------------------------------
    // --- [START] Scope functions ---------------------------------------------
    $scope.$on( "eodGraphPreDestroy", function( _event, args ) {
    } ) ;
    
    $scope.$on( "eodGraphPostRender", function( _event, args ) {
        $scope.fetchBollingerBands() ;
    } ) ;
    
    $scope.enableFlagUpdated = function() {
        if( $scope.config.enableFlag ) {
            $scope.fetchBollingerBands() ;
        }
        else {
            $scope.hideBollingerBands() ;
        }
    }
    
    $scope.hideBollingerBands = function() {
        enableBollingerBands( false ) ;         
    }
    
    $scope.fetchBollingerBands = function() {
        
        const symbol = $scope.$parent.graphParams.symbolNse ;
        
        $http.get( '/Equity/GraphData/Indicator/BollingerBands' + 
                   '?symbolNse='  + encodeURIComponent( symbol ) +
                   '&windowSize=' + $scope.config.windowSize +
                   '&numStdDev='  + $scope.config.numStdDev  )
        .then ( 
            function( response ){
                setSeries( "bollinger-upper",  response ) ;         
                setSeries( "bollinger-middle", response ) ;         
                setSeries( "bollinger-lower",  response ) ;
                
                enableBollingerBands( true ) ;         
            }
        ) ;
    }
    // --- [END] Scope functions

    // -------------------------------------------------------------------------
    // --- [START] Local functions ---------------------------------------------
    function setSeries( seriesName, response ) {
        $scope.$parent.seriesCache.set( seriesName, response.data[ seriesName ] ) ;
    }
    
    function isIndicatorVisible() {
        return ( $scope.$parent.isSeriesVisible( "bollinger-upper" ) || 
                 $scope.$parent.isSeriesVisible( "bollinger-middle" ) || 
                 $scope.$parent.isSeriesVisible( "bollinger-lower" ) ) ;
    }
    
    function enableBollingerBands( enable ) {
        
        $scope.$parent.bollingerOptions.upper.enabled  = enable ;
        $scope.$parent.bollingerOptions.middle.enabled = enable ;
        $scope.$parent.bollingerOptions.lower.enabled  = enable ;
        
        $scope.$parent.plotBollingerBands() ;
        $scope.config.enableFlag = enable ;
    }
    
    // ------------------- Server comm functions -------------------------------
    
} ) ;