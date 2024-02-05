capitalystNgApp.controller( 'CashEntryHomeController', 
    function( $scope, $http, $location, editIntent ) {
    
    // ---------------- Local variables --------------------------------------
    var toDate = new Date() ;
    var fromDate = moment().subtract( 15, 'days' ).toDate() ;
    
    // ---------------- Scope variables --------------------------------------
    $scope.entries = [] ;
     
    // -----------------------------------------------------------------------
    // --- [START] Controller initialization ---------------------------------
    console.log( "Loading CashEntryHomeController" ) ;
    initializeController() ;
    // --- [END] Controller initialization -----------------------------------
    
    
    // -----------------------------------------------------------------------
    // --- [START] Scope functions -------------------------------------------
    $scope.editEntry = function( index ) {
        var entry = $scope.entries[index] ;
        var clone = JSON.parse( JSON.stringify( entry ) ) ;
        clone.valueDate = new Date( clone.valueDate ) ;
        editIntent.setEditIntent( clone, index ) ;
        $location.path( "/editEntry" ) ;
    }
    
    $scope.duplicateEntry = function( index ) {
        var entry = $scope.entries[index] ;
        var clone = JSON.parse( JSON.stringify( entry ) ) ;
        clone.valueDate = new Date() ;
        clone.id = null ;
        editIntent.setEditIntent( clone, -1 ) ;
        $location.path( "/editEntry" ) ;
    }
    
    $scope.newCashEntry = function() {
        var entry =     {
            "id"        : -1,
            "account"   : null,
            "valueDate" : new Date(),
            "remarks"   : null,
            "amount"    : null,
            "l1Cat"     : null,
            "l2Cat"     : null,
            "notes"     : null
        } ;
        editIntent.setEditIntent( entry, -1 ) ;
        $location.path( "/editEntry" ) ;
    }
    
    $scope.loadMoreEntries = function() {
        toDate = moment( fromDate ).toDate() ;
        fromDate = moment( toDate ).subtract( 15, 'days' ).toDate() ;
        fetchLedgerEntries() ;
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
                if( data.length > 0 ) {
                    $scope.$parent.cashBalance = data[0].account.balance ;
                    angular.forEach( data, function( entry, key ){
                        entry.amount *= -1 ;
                        $scope.entries.push( entry ) ;
                    }) ;
                }
            }, 
            function( error ){
                $scope.$parent.addErrorAlert( "Error fetching ledger entries." ) ;
            }
        )
        .finally(function() {
            $scope.$emit( 'interactingWithServer', { isStart : false } ) ;
        }) ;
    }
} ) ;