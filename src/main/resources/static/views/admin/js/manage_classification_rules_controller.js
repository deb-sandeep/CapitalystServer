capitalystNgApp.controller( 'ManageClassificationRulesController', 
    function( $scope, $http ) {
    
    // ---------------- Local variables --------------------------------------
    
    // ---------------- Scope variables --------------------------------------
    $scope.$parent.navBarTitle = "Manage classification rules" ;
    $scope.$parent.activeModuleId = "classification_rules" ;
    
    // This is used to highlight the Credit or Debit button on the page
    // This is updated when either of the buttons are clicked.
    $scope.activeCategory = "Debit" ;
    
    // Classification of the raw categories data into an usable data structure
    // Note that l1Categories store instances of CatName while l2Category
    // key is the display name of CatName and the value is the category
    // instance returned by the server.
    $scope.ledgerCategories = {
       credit : {
           l1Categories : [],
           l2Categories : new Map()
       },
       debit : {
           l1Categories : [],
           l2Categories : new Map()
       }
    } ;    

    // -----------------------------------------------------------------------
    // --- [START] Controller initialization ---------------------------------
    console.log( "Loading ManageClassificationRulesController" ) ;
    initializeController() ;
    // --- [END] Controller initialization -----------------------------------
    
    // -----------------------------------------------------------------------
    // --- [START] Scope functions -------------------------------------------
    
    $scope.getTabActivationClass = function( id ) {
        return (id == $scope.activeCategory)? "btn-primary" : "btn-secondary" ;
    }
    
    $scope.showCreditEntries = function() {
        $scope.activeCategory = "Credit" ;
    }
    
    $scope.showDebitEntries = function() {
        $scope.activeCategory = "Debit" ;
    }
    
    // --- [START] Scope functions dealilng with non UI logic ----------------
    
    // --- [END] Scope functions

    // -----------------------------------------------------------------------
    // --- [START] Local functions -------------------------------------------
    
    function initializeController() {
        fetchClassificationCategories() ;
    }
    
    // ------------------- Server comm functions -----------------------------
    function fetchClassificationCategories() {
        
        // Reset the internal data structures since this can be called both
        // during initialization and runtime.
        $scope.ledgerCategories.credit.l1Categories.length = 0 ;
        $scope.ledgerCategories.credit.l2Categories.clear() ;
        $scope.ledgerCategories.debit.l1Categories.length = 0 ;  
        $scope.ledgerCategories.debit.l2Categories.clear() ;
        
        $scope.$emit( 'interactingWithServer', { isStart : true } ) ;
        $http.get( '/Ledger/Categories' )
        .then ( 
            function( response ){
                populateMasterCategories( response.data ) ;
                console.log( $scope.ledgerCategories ) ;
            }, 
            function( error ){
                $scope.$parent.addErrorAlert( "Could not fetch classification categories.\n" +
                                              error.data.message ) ;
            }
        )
        .finally(function() {
            $scope.$emit( 'interactingWithServer', { isStart : false } ) ;
        }) ;
    }
    
    // ------------------- Server response processors ------------------------
    
    function populateMasterCategories( categories ) {
        
        $scope.ledgerCategories.credit.l1Categories.length = 0 ;
        $scope.ledgerCategories.credit.l2Categories.clear() ;
        
        $scope.ledgerCategories.debit.l1Categories.length = 0 ;
        $scope.ledgerCategories.debit.l2Categories.clear() ;
        
        for( var i=0; i<categories.length; i++ ) {
            
            var category = categories[i] ;
            
            // Extention attributes added to the server returned obj
            category.beingEdited = false ;
            category.numLedgerEntries = 0 ;
            
            if( category.creditClassification ) {
                classifyCategoryInMasterList( $scope.ledgerCategories.credit,
                                              category ) ; 
            }
            else {
                classifyCategoryInMasterList( $scope.ledgerCategories.debit,
                                              category ) ; 
            }
        }
    }
    
    function classifyCategoryInMasterList( categoryData, category ) {
        
        var l1CatList = categoryData.l1Categories ;
        var l2CatMap  = categoryData.l2Categories ;
        var l1CatName = category.l1CatName ;
        var l2CatName = category.l2CatName ;
        
        if( l1CatList.filter( e => e == l1CatName ).length <= 0 ) {
            l1CatList.push( l1CatName ) ;
        }
        
        if( !l2CatMap.has( l1CatName ) ) {
            l2CatMap.set( l1CatName, [] ) ;
        }
        
        var l2List = l2CatMap.get( l1CatName ) ;
        l2List.push( l2CatName ) ;
    }
    
} ) ;