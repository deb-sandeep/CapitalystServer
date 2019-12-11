capitalystNgApp.controller( 'CashEntryHomeController', 
    function( $scope, $http, $location, editIntent ) {
    
    // ---------------- Local variables --------------------------------------
    var toDate = new Date() ;
    var fromDate = moment().subtract( 6, 'days' ).toDate() ;
    
    // ---------------- Scope variables --------------------------------------
    $scope.entries = [] ;
    
    // -----------------------------------------------------------------------
    // --- [START] Controller initialization ---------------------------------
    console.log( "Loading CashEntryHomeController" ) ;
    initializeController() ;
    // --- [END] Controller initialization -----------------------------------
    
    
    // -----------------------------------------------------------------------
    // --- [START] Scope functions -------------------------------------------
    $scope.editEntry = function( entry ) {
        editIntent.setEditIntent( entry ) ;
        $location.path( "/editEntry" ) ;
    }
    // --- [END] Scope functions

    // -----------------------------------------------------------------------
    // --- [START] Local functions -------------------------------------------
    
    function initializeController() {
        fetchLedgerEntries() ;
    }
    
    // ------------------- Server comm functions -----------------------------
    function fetchLedgerEntries() {
        
        $scope.$emit( 'interactingWithServer', { isStart : true } ) ;
        $http.get( '/Ledger/CashAtHome?' + 
                   'fromDate=' + fromDate.toISOString() + 
                   "&toDate=" + toDate.toISOString() )
        .then ( 
            function( response ){
                var data = response.data ;
                angular.forEach( data, function( entry, key ){
                    entry.amount *= -1 ;
                    $scope.entries.push( entry ) ;
                }) ;
            }, 
            function( error ){
                $scope.$parent.addErrorAlert( "Error fetch accounts." ) ;
            }
        )
        .finally(function() {
            $scope.$emit( 'interactingWithServer', { isStart : false } ) ;
        }) ;
    }

} ) ;