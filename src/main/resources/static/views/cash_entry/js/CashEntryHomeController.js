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
    $scope.editEntry = function( index ) {
        var entry = $scope.entries[index] ;
        var clone = JSON.parse( JSON.stringify( entry ) ) ;
        clone.valueDate = new Date( clone.valueDate ) ;
        editIntent.setEditIntent( clone, index ) ;
        $location.path( "/editEntry" ) ;
    }
    
    $scope.newCashEntry = function() {
        var entry =     {
            "id"        : -1,
            "valueDate" : new Date(),
            "remarks"   : null,
            "amount"    : null,
            "l1Cat"     : null,
            "l2Cat"     : null
        } ;
        editIntent.setEditIntent( entry, -1 ) ;
        $location.path( "/editEntry" ) ;
    }
    // --- [END] Scope functions

    // -----------------------------------------------------------------------
    // --- [START] Local functions -------------------------------------------
    
    function initializeController() {
        // Check the editIntent editEntryIndex
        // If null => either this is being loaded fresh or the edit screen  
        // was cancelled. We proceed normally.
        //
        // If the editEntryIndex is -1, it implies that a new entry was added
        // A normal refresh will take care of loading the new entry
        //
        // If the editEntryIndex is >= 0, implies that an existing entry was
        // edited. We replace the existing entry and the screen auto refreshes
        // itself.
        if( editIntent.editEntryIndex == null || 
            editIntent.editEntryIndex == -1 ) {
            fetchLedgerEntries() ;
        }
        else {
            $scope.entries[ editIntent.editEntryIndex ] = editIntent.editEntry ;
        }
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