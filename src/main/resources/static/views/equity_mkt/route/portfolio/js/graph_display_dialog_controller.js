capitalystNgApp.controller( 'GraphDisplayDialogController', function( $scope ) {
    
    // ---------------- Local variables --------------------------------------
    
    // ---------------- Scope variables --------------------------------------
    $scope.holding = null ;
    $scope.eodData = [] ;
    
    // -----------------------------------------------------------------------
    // --- [START] Controller initialization ---------------------------------
    console.log( "Loading GraphDisplayDialogController" ) ;
    // --- [END] Controller initialization -----------------------------------
    
    // -----------------------------------------------------------------------
    // --- [START] Scope functions -------------------------------------------
    
    $scope.$on( 'graphDialogDisplayTrigger', function( _event, args ) {
        console.log( "Trigger obtained " ) ;
        $scope.holding = args.holding ;
        fetchChartData() ;
        drawChart() ;
    } ) ;
    
    //TODO

    $scope.hideGraphDialog = function( holding ) {
        $( '#graphDisplayDialog' ).modal( 'hide' ) ;
    }
    
    // --- [END] Scope functions

    // -----------------------------------------------------------------------
    // --- [START] Local functions -------------------------------------------
    
    // ------------------- Server comm functions -----------------------------
    function drawChart() {
    }
    
    function fetchChartData() {
    }
} ) ;