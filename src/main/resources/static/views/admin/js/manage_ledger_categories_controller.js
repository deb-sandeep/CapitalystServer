function CatName( name ) {
    
    this.displayName = name ;
    this.beingEdited = false ;
}

capitalystNgApp.controller( 'ManageLedgerCategoriesController', 
    function( $scope, $http ) {
    
    // ---------------- Local variables --------------------------------------
    
    // ---------------- Scope variables --------------------------------------
    $scope.$parent.navBarTitle = "Manage L1 and L2 ledger categories" ;
    $scope.$parent.activeModuleId = "cat_management" ;
    
    $scope.activeCategory = "Debit" ;
    $scope.l1CatNameBeingEdited = null ;
    
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
    
    $scope.saveCategoryAttributes = function( cat ) {
        console.log( "TODO: Updating category " + cat ) ;
    }
    
    $scope.editCategory = function( cat ) {
        console.log( "Editing category " + cat ) ;
    }
    
    $scope.editL1CategoryName = function( catName ) {
        
        resetEditedStatus( $scope.ledgerCategories.credit ) ;
        resetEditedStatus( $scope.ledgerCategories.debit ) ;
        
        catName.beingEdited = true ;
        $scope.l1CatNameBeingEdited = catName.displayName ;
    }
    
    $scope.revertEditChanges = function( catName ) {
        console.log( "Reverting edit changes" ) ;
        catName.beingEdited = false ;
        $scope.l1CatNameBeingEdited = null ;
    }
    
    $scope.saveDebitL1CategoryName = function( catName ) {
        
        var oldCatName = catName.displayName ;
        var newCatName = $scope.l1CatNameBeingEdited ;
        
        console.log( "Saving change in L1 category name" ) ;
        console.log( "   Old cat name = " + oldCatName ) ;
        console.log( "   New cat name - " + newCatName ) ;
        
        if( oldCatName == newCatName || newCatName == "" ) {
            catName.beingEdited = false ;
        }
        else {
            catName.beingEdited = false ;
            
            var l2CatMap = $scope.ledgerCategories.debit.l2Categories ;
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
                    $scope.l1CatNameBeingEdited = null ;
                } 
            ) ;
        }
    }
    
    // --- [END] Scope functions

    // -----------------------------------------------------------------------
    // --- [START] Local functions -------------------------------------------
    
    function initializeController() {
        fetchClassificationCategories() ;
    }
    
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
        
        $scope.$emit( 'interactingWithServer', { isStart : true } ) ;
        $http.get( '/Ledger/Categories' )
        .then ( 
            function( response ){
                populateMasterCategories( response.data ) ;
                console.log( $scope.ledgerCategories ) ;
            }, 
            function( error ){
                $scope.$parent.addErrorAlert( "Could not fetch classification categories." ) ;
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
                classifyCategoryInMasterList( 
                        $scope.ledgerCategories.credit.l1Categories, 
                        $scope.ledgerCategories.credit.l2Categories,
                        category ) ; 
            }
            else {
                classifyCategoryInMasterList( 
                        $scope.ledgerCategories.debit.l1Categories, 
                        $scope.ledgerCategories.debit.l2Categories,
                        category ) ; 
            }
        }
    }
    
    function classifyCategoryInMasterList( l1CatList, l2CatMap, category ) {
        
        var catName = new CatName( category.l1CatName ) ;
        
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