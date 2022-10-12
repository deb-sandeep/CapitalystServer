capitalystNgApp.controller( 'HistEODController', function( $scope, $http ) {
    
    // ---------------- Local variables --------------------------------------
    
    // ---------------- Scope variables --------------------------------------
    $scope.$parent.navBarTitle = "Historic Equity EOD data" ;
    $scope.$parent.activeModuleId = "hist" ;
    
    $scope.metadata = [] ;
    
    // -----------------------------------------------------------------------
    // --- [START] Controller initialization ---------------------------------
    console.log( "Loading HistEODController" ) ;
    fetchAllHistoricMeta() ;
    // --- [END] Controller initialization -----------------------------------
    
    // -----------------------------------------------------------------------
    // --- [START] Scope functions -------------------------------------------
    $scope.showGraphDialog = function( meta ) {
        $scope.$emit( 'graphDialogDisplayTrigger', {
            symbolNse   : meta.symbolNse,
            companyName : meta.symbolNse,
            ownerName   : 'Family' 
        }) ;
    }
    
    $scope.refreshTable = function() {
        fetchAllHistoricMeta() ;
    }
    
    $scope.showUploadDialog = function() {
        $( '#uploadEquityHistoryDialog' ).modal( 'show' ) ;
    }
    
    // --- [END] Scope functions

    // -----------------------------------------------------------------------
    // --- [START] Local functions -------------------------------------------
    
    // ------------------- Server comm functions -----------------------------
    function fetchAllHistoricMeta() {
        
        $http.get( '/Equity/HistoricData/Meta/All' )
        .then ( 
            function( response ){
                console.log( response.data ) ;
                $scope.metadata = response.data ;
            }, 
            function( error ){
                $scope.$parent.addErrorAlert( "Error getting historic metadata." ) ;
            }
        ) ;
    }
} ) ;