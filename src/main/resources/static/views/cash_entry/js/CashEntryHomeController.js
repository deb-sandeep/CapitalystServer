capitalystNgApp.controller( 'CashEntryHomeController', 
    function( $scope, $http, $location, editIntent ) {
    
    // ---------------- Local variables --------------------------------------
    var toDate = new Date() ;
    var fromDate = moment().subtract( 6, 'days' ).toDate() ;
    
    // ---------------- Scope variables --------------------------------------
    $scope.entries = [] ;
    $scope.masterCategories = {
        credit : {
            l1Categories : [],
            l2Categories : new Map()
        },
        debit : {
            l1Categories : [],
            l2Categories : new Map()
        }
     } ;
     
     $scope.relevantCategoriesForSelectedEntries = null ;
    
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
                fetchClassificationCategories() ;
            }, 
            function( error ){
                $scope.$parent.addErrorAlert( "Error fetch accounts." ) ;
            }
        )
        .finally(function() {
            $scope.$emit( 'interactingWithServer', { isStart : false } ) ;
        }) ;
    }

    function fetchClassificationCategories() {
        
        $scope.$emit( 'interactingWithServer', { isStart : true } ) ;
        $http.get( '/Ledger/CashEntryCategories' )
        .then ( 
            function( response ){
                populateMasterCategories( response.data ) ;
            }, 
            function( error ){
                $scope.$parent.addErrorAlert( "Could not fetch classification categories." ) ;
            }
        )
        .finally(function() {
            $scope.$emit( 'interactingWithServer', { isStart : false } ) ;
        }) ;
    }
    
    function populateMasterCategories( categories ) {
        
        $scope.masterCategories.credit.l1Categories.length = 0 ;
        $scope.masterCategories.credit.l2Categories.clear() ;
        
        $scope.masterCategories.debit.l1Categories.length = 0 ;
        $scope.masterCategories.debit.l2Categories.clear() ;
        
        for( var i=0; i<categories.length; i++ ) {
            var category = categories[i] ;
            if( category.creditClassification ) {
                classifyCategoryInMasterList( 
                        $scope.masterCategories.credit.l1Categories, 
                        $scope.masterCategories.credit.l2Categories,
                        category ) ; 
            }
            else {
                classifyCategoryInMasterList( 
                        $scope.masterCategories.debit.l1Categories, 
                        $scope.masterCategories.debit.l2Categories,
                        category ) ; 
            }
        }
    }
    
    function classifyCategoryInMasterList( l1CatList, l2CatMap, category ) {
        
        var l1 = category.l1CatName ;
        var l2 = category.l2CatName ;
        
        if( l1CatList.indexOf( l1 ) == -1 ) {
            l1CatList.push( l1 ) ;
        }
        
        if( !l2CatMap.has( l1 ) ) {
            l2CatMap.set( l1, [] ) ;
        }
        
        var l2List = l2CatMap.get( l1 ) ;
        l2List.push( l2 ) ;
    }
} ) ;