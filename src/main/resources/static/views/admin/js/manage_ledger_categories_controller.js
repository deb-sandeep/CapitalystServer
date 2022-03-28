function CatName( name, categoryData ) {
    
    this.displayName = name ;
    this.categoryData = categoryData ;
    this.beingEdited = false ;
}

capitalystNgApp.controller( 'ManageLedgerCategoriesController', 
    function( $scope, $http ) {
    
    // ---------------- Local variables --------------------------------------
    
    // ---------------- Scope variables --------------------------------------
    $scope.$parent.navBarTitle = "Manage ledger categories" ;
    $scope.$parent.activeModuleId = "cat_management" ;
    
    // This is used to highlight the Credit or Debit button on the page
    // This is updated when either of the buttons are clicked.
    $scope.activeCategory = "Debit" ;
    
    // Stores the CatName instance being edited currently or null if none
    // are being edited.
    $scope.l1CatNameBeingEdited = null ;
    
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
    console.log( "Loading ManageLedgerCategoriesController" ) ;
    initializeController() ;
    // --- [END] Controller initialization -----------------------------------
    
    
    // -----------------------------------------------------------------------
    // --- [START] Scope functions -------------------------------------------
    
    $scope.getTabActivationClass = function( id ) {
        if( id == $scope.activeCategory ) {
            return "btn-primary" ;
        }
        return "btn-secondary" ;
    }
    
    $scope.showCreditEntries = function() {
        $scope.activeCategory = "Credit" ;
    }
    
    $scope.showDebitEntries = function() {
        $scope.activeCategory = "Debit" ;
    }
    
    // --- [START] Scope functions dealilng with non UI logic ----------------
    
    // Invoked when any of the checkbox attributes of a category is updated
    $scope.saveCategoryAttributes = function( cat ) {
        console.log( "Updating category " + cat.id ) ;
        
        var categoryList = [] ;
        categoryList.push( cat ) ;
        
        saveCategoryEditChangesOnServer( categoryList,
            function() { // Called on success
            },
            function() { // Called on failure
            } 
        ) ;        
    }
    
    // Starts the edit of a L1 category name.
    $scope.startEditingL1CategoryName = function( catName ) {
        
        resetEditedStatus( $scope.ledgerCategories.credit ) ;
        resetEditedStatus( $scope.ledgerCategories.debit ) ;
        
        catName.beingEdited = true ;
        $scope.l1CatNameBeingEdited = catName.displayName ;
    }
    
    // Reverts/Escapes the editing mode of L1 category name. Since no
    // changes are made to the core data structures this is a simple rollback
    $scope.revertL1CategoryEditChanges = function( catName ) {
        console.log( "Reverting edit changes" ) ;
        catName.beingEdited = false ;
        $scope.l1CatNameBeingEdited = null ;
    }
    
    // Saves the edit changes to a L1 category name. Note that when a L1
    // category name changes, the changes are cascaded to all the child 
    // category entries.
    $scope.saveL1CategoryName = function( catName ) {
        
        var oldCatName = catName.displayName ;
        var newCatName = $scope.l1CatNameBeingEdited ;
        
        console.log( "Saving change in L1 category name" ) ;
        console.log( "   Old cat name = " + oldCatName ) ;
        console.log( "   New cat name - " + newCatName ) ;
        
        catName.beingEdited = false ;
        
        if( oldCatName != newCatName && newCatName != "" ) {
            
            console.log( "L1 cat name change detected." ) ;
            
            var catData  = catName.categoryData ;
            var l2CatMap = catData.l2Categories ;
            var l2List   = l2CatMap.get( oldCatName ) ;
            
            for( var i=0; i<l2List.length; i++ ) {
                var cat = l2List[i] ;
                cat.l1CatName = newCatName ;
            }
            
            saveCategoryEditChangesOnServer( l2List,
                function() {
                    l2CatMap.delete( oldCatName ) ;
                    l2CatMap.set( newCatName, l2List ) ;
                    
                    catName.displayName = $scope.l1CatNameBeingEdited ;
                    $scope.l1CatNameBeingEdited = null ;
                },
                function() {
                    // We have polluted the internal data structure by now
                    // Easy is to reload the data.
                    fetchClassificationCategories() ;
                } 
            ) ;
        }
        else {
            console.log( "No change detected. Skipping update." ) ;
        }
    }
    
    $scope.editCategory = function( cat ) {
        console.log( "TODO: Editing category " + cat ) ;
    }
    
    // --- [END] Scope functions

    // -----------------------------------------------------------------------
    // --- [START] Local functions -------------------------------------------
    
    function initializeController() {
        fetchClassificationCategories() ;
    }
    
    // Goes through the internal ledger category classification data structure
    // and resets the beingEdited flag for all objects.
    function resetEditedStatus( catObj ) {
        for( var i=0; i<catObj.l1Categories.length; i++ ) {
            var catName = catObj.l1Categories[i] ;
            catName.beingEdited = false ;
            
            var categories = catObj.l2Categories.get( catName.displayName ) ;
            for( var j=0; j<categories.length; j++ ) {
                var cat = categories[j] ;
                cat.beingEdited = false ;
            }
        }
    }
    
    // ------------------- Server comm functions -----------------------------
    function fetchClassificationCategories() {
        
        // Reset the internal data structures since this can be called both
        // during initialization and runtime.
        $scope.l1CatNameBeingEdited = null ;
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
    
    function saveCategoryEditChangesOnServer( l2List, 
                                              successCallback, 
                                              errorCallback ) {
        
        $scope.$emit( 'interactingWithServer', { isStart : true } ) ;
        $http.post( '/Ledger/Categories', l2List )
        .then ( 
            function( response ){
                console.log( "Changes successfully saved on server" ) ;
                successCallback() ;
            }, 
            function( error ){
                $scope.$parent.addErrorAlert( "Error saving changes on server.\n" + 
                                              error.data.message ) ;
                console.log( error ) ;
                errorCallback() ;
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
            category.beingEdited = false ;
            
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
        var catName = new CatName( category.l1CatName, categoryData ) ;
        
        if( l1CatList.filter( e => e.displayName == catName.displayName ).length <= 0 ) {
            l1CatList.push( catName ) ;
        }
        
        if( !l2CatMap.has( catName.displayName ) ) {
            l2CatMap.set( catName.displayName, [] ) ;
        }
        
        var l2List = l2CatMap.get( catName.displayName ) ;
        l2List.push( category ) ;
    }
} ) ;