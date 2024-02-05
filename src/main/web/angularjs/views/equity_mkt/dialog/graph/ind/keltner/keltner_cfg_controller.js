capitalystNgApp.controller( 'KeltnerCfgController', 
                            function( $scope, $http ) {
    
    // ---------------- Local variables ----------------------------------------

    // ---------------- Scope variables ----------------------------------------
    $scope.config = {
        emaCount : 20,
        atrCount : 10,
        atrScale : 3,
        enableFlag : false 
    } ;
    
    // -------------------------------------------------------------------------
    // --- [START] Scope functions ---------------------------------------------
    $scope.$on( "eodGraphPreDestroy", function( _event, args ) {
    } ) ;
    
    $scope.$on( "eodGraphPostRender", function( _event, args ) {
    } ) ;
    
    $scope.enableFlagUpdated = function() {
        if( $scope.config.enableFlag ) {
            $scope.fetchKeltnerChannel() ;
        }
        else {
            $scope.hideKeltnerChannel() ;
        }
    }
    
    $scope.hideKeltnerChannel = function() {
        enableKeltnerChannel( false ) ;         
    }
    
    $scope.fetchKeltnerChannel = function() {
        
        const symbol = $scope.$parent.graphParams.symbolNse ;
        
        $http.get( '/Equity/GraphData/Indicator/KeltnerChannel' + 
                   '?symbolNse='+ encodeURIComponent( symbol ) +
                   '&emaCount=' + $scope.config.emaCount +
                   '&atrCount=' + $scope.config.atrCount +
                   '&atrScale=' + $scope.config.atrScale )
        .then ( 
            function( response ){
                setSeries( "keltner-upper",  response ) ;         
                setSeries( "keltner-middle", response ) ;         
                setSeries( "keltner-lower",  response ) ;
                
                enableKeltnerChannel( true ) ;         
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
        return ( $scope.$parent.isSeriesVisible( "keltner-upper" ) || 
                 $scope.$parent.isSeriesVisible( "keltner-middle" ) || 
                 $scope.$parent.isSeriesVisible( "keltner-lower" ) ) ;
    }
    
    function enableKeltnerChannel( enable ) {
        
        $scope.$parent.keltnerOptions.upper.enabled  = enable ;
        $scope.$parent.keltnerOptions.middle.enabled = enable ;
        $scope.$parent.keltnerOptions.lower.enabled  = enable ;
        
        $scope.$parent.plotKeltnerChannel() ;
        $scope.config.enableFlag = enable ;
    }
    
    // ------------------- Server comm functions -------------------------------
    
} ) ;