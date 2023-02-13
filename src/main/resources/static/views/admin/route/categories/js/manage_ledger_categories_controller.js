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
    
    // Stores the display name of the CatName instance being edited currently 
    // or null if none are being edited.
    $scope.l1CatNameBeingEdited = null ;
    
    // Stores the l1 name of the category instance being edited currently 
    // or null if none are being edited.
    $scope.l2CatNameBeingEdited = null ;
    
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

    // Note - catData contains either ledgerCategories.credit or debit
    $scope.catEditCtx = {
        catData           : null,
        cat               : null,
        selectedL1CatName : null,
        selectedL2CatName : null,
        newL1CatName      : null,
        newL2CatName      : null,
        amountLoadingRule : null
    } ;
    
    $scope.loadingRuleValidationResult = {
        validationMsg : null,
        yearlyCap     : 0,
        monthlyCap    : []
    } ;
    
    $scope.ledgerEntriesForDisplay = [] ;
    
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
    
    // Starts the edit of a L2 category name.
    $scope.startEditingL2CategoryName = function( category ) {
        
        resetEditedStatus( $scope.ledgerCategories.credit ) ;
        resetEditedStatus( $scope.ledgerCategories.debit ) ;
        
        category.beingEdited = true ;
        $scope.l2CatNameBeingEdited = category.l2CatName ;
    }
    
    // Reverts/Escapes the editing mode of L2 category name. Since no
    // changes are made to the core data structures this is a simple rollback
    $scope.revertL1CategoryEditChanges = function( category ) {
        console.log( "Reverting edit changes" ) ;
        category.beingEdited = false ;
        $scope.l2CatNameBeingEdited = null ;
    }
    
    // Saves the edit changes to a L2 category name. This is simple since
    // only one category is changing.
    $scope.saveL2CategoryName = function( cat ) {
        
        var oldCatName = cat.l2CatName ;
        var newCatName = $scope.l2CatNameBeingEdited ;
        
        console.log( "Saving change in L2 category name" ) ;
        console.log( "   Old cat name = " + oldCatName ) ;
        console.log( "   New cat name - " + newCatName ) ;
        
        cat.beingEdited = false ;
        $scope.l2CatNameBeingEdited = null ;
        
        if( oldCatName != newCatName && newCatName != "" ) {
            
            console.log( "L2 cat name change detected." ) ;

            cat.l2CatName = newCatName ;
            
            var catList = [] ;
            catList.push( cat ) ;
                        
            saveCategoryEditChangesOnServer( catList,
                function() {
                },
                function() {
                    cat.l2CatName = oldCatName ;
                } 
            ) ;
        }
        else {
            console.log( "No change detected. Skipping update." ) ;
        }
    }
    
    $scope.showParentCategoryDialog = function( cat ) {
        
        $scope.catEditCtx.cat = cat ;
        if( cat.creditClassification ) {
            $scope.catEditCtx.catData = $scope.ledgerCategories.credit ;
        }
        else {
            $scope.catEditCtx.catData = $scope.ledgerCategories.debit ;
        }
        
        $( '#changeParentCategoryDialog' ).modal( 'show' ) ;
    }
    
    $scope.hideParentCategoryDialog = function() {
        
        clearCatEditCtx() ;
        $( '#changeParentCategoryDialog' ).modal( 'hide' ) ;
    }
    
    $scope.newL1CatNameEntered = function() {
        $scope.catEditCtx.selectedL1CatName = null ;
    }
    
    $scope.newL2CatNameEntered = function() {
        $scope.catEditCtx.selectedL2CatName = null ;
    }
    
    $scope.applyNewParentCategory = function() {
        console.log( "Applying new parent category." ) ;
        
        var newL1CatName = validateNewL1CatName() ;
        if( newL1CatName != null && 
            newL1CatName != $scope.catEditCtx.cat.l1CatName ) {
                
            console.log( "New L1 cat name = " + newL1CatName ) ;
            applyNewL1CatName( newL1CatName, $scope.catEditCtx ) ;
        }
        $scope.hideParentCategoryDialog() ;
    }
    
    $scope.showLedgerEntriesDialog = function( cat ) {
        fetchLedgerEntries( cat ) ;
    }
    
    $scope.hideLedgerEntriesDialog = function() {
        $scope.ledgerEntriesForDisplay.length = 0 ;
        $( '#viewLedgerEntriesDialog' ).modal( 'hide' ) ;
    }
    
    $scope.showMergeCategoryDialog = function( cat ) {
        
        clearCatEditCtx() ;
        $scope.catEditCtx.cat = cat ;
        if( cat.creditClassification ) {
            $scope.catEditCtx.catData = $scope.ledgerCategories.credit ;
        }
        else {
            $scope.catEditCtx.catData = $scope.ledgerCategories.debit ;
        }
        
        $( '#mergeLedgerCategoryDialog' ).modal( 'show' ) ;
    }

    $scope.hideMergeCategoryDialog = function() {
        clearCatEditCtx() ;
        $( '#mergeLedgerCategoryDialog' ).modal( 'hide' ) ;
    }
    
    $scope.mergeCategory = function() {
        
        var newL1CatName = validateNewL1CatName() ;
        if( newL1CatName != null ) {
            console.log( "New L1 cat name = " + newL1CatName ) ;
            
            var newL2CatName = validateNewL2CatName() ;
            if( newL2CatName != null ) {
                console.log( "New L2 cat name = " + newL2CatName ) ;
                
                var cat = $scope.catEditCtx.cat ;
                
                if( !( newL1CatName == cat.l1CatName && 
                       newL2CatName == cat.l2CatName ) ) {
                        
                    mergeCategoriesOnServer( cat.id, newL1CatName, newL2CatName ) ; 
                }
            }
        }
        $scope.hideMergeCategoryDialog() ;
    }
    
    $scope.showYearlyCapEditDialog = function( cat ) {
        
        clearCatEditCtx() ;
        $scope.catEditCtx.cat = cat ;
        $scope.catEditCtx.amountLoadingRule = cat.amountLoadingRule ;
        $scope.catEditCtx.yearlyCap         = cat.yearlyCap ;
        
        $scope.validateLoadingRule() ;
        
        $( '#yearlyCapEditDialog' ).modal( 'show' ) ;
    }

    $scope.hideYearlyCapEditDialog = function() {
        $( '#yearlyCapEditDialog' ).modal( 'hide' ) ;
    }

    $scope.validateLoadingRule = function() {
        
        clearLoadingRuleValidationResult() ;

        validateLoadingRuleOnServer( function( yearlyCap, monthlyCap ) {
            
            $scope.loadingRuleValidationResult.yearlyCap = yearlyCap ;
            for( var i=0; i<monthlyCap.length; i++ ) {
                $scope.loadingRuleValidationResult.monthlyCap.push( monthlyCap[i] ) ; 
            }
            
        }, function( errMsg ) {
            
            if( errMsg.startsWith( 'Required request body is missing' ) ) {
                errMsg = "Empty rule." ;
            }
            $scope.loadingRuleValidationResult.validationMsg = errMsg ;
        } ) ;
    }
    
    $scope.saveYearlyCapRule = function() {
        
        var editedCatList = [] ;
        var cat = $scope.catEditCtx.cat ;
        
        cat.amountLoadingRule = $scope.catEditCtx.amountLoadingRule ;
        cat.yearlyCap         = $scope.loadingRuleValidationResult.yearlyCap ;
        
        editedCatList.push( cat ) ;
        
        saveCategoryEditChangesOnServer( editedCatList,
            function() {
                $scope.hideYearlyCapEditDialog() ;
            },
            function() {
                $scope.hideYearlyCapEditDialog() ;
                fetchClassificationCategories() ;
            } 
        ) ;
    }
    
    $scope.deleteYearlyCapRule = function() {
        
        var editedCatList = [] ;
        var cat = $scope.catEditCtx.cat ;
        
        cat.amountLoadingRule = null ;
        cat.yearlyCap         = 0 ;
        
        editedCatList.push( cat ) ;
        
        saveCategoryEditChangesOnServer( editedCatList,
            function() {
                $scope.hideYearlyCapEditDialog() ;
            },
            function() {
                $scope.hideYearlyCapEditDialog() ;
                fetchClassificationCategories() ;
            } 
        ) ;
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
        $scope.l2CatNameBeingEdited = null ;

        $scope.ledgerCategories.credit.l1Categories.length = 0 ;
        $scope.ledgerCategories.credit.l2Categories.clear() ;
        $scope.ledgerCategories.debit.l1Categories.length = 0 ;  
        $scope.ledgerCategories.debit.l2Categories.clear() ;
        
        clearCatEditCtx() ;
        
        $scope.$emit( 'interactingWithServer', { isStart : true } ) ;
        $http.get( '/Ledger/Categories' )
        .then ( 
            function( response ){
                populateMasterCategories( response.data ) ;
                console.log( "Fetching ledger enties classification counter." ) ;
                fetchClassifiedLedgerEntriesCounter() ;
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
    
    function fetchClassifiedLedgerEntriesCounter() {
        
        $http.get( '/Ledger/Categories/ClassificationCount' )
        .then ( 
            function( response ){
                populateNumLedgerEntries( response.data ) ;
            }, 
            function( error ){
                $scope.$parent.addErrorAlert( "Could not fetch classification categories.\n" +
                                              error.data.message ) ;
            }
        ) ;
    }
    
    function fetchLedgerEntries( cat ) {
        
        $scope.ledgerEntriesForDisplay.length = 0 ;
        
        
        $scope.$emit( 'interactingWithServer', { isStart : true } ) ;
        $http.get( '/Ledger/Entries?' +
                   'selectCreditEntries=' + cat.creditClassification + 
                   '&l1CatName=' + cat.l1CatName + 
                   '&l2CatName=' + cat.l2CatName )
        .then ( 
            function( response ){
                $scope.ledgerEntriesForDisplay = response.data ;
                $( '#viewLedgerEntriesDialog' ).modal( 'show' ) ; 
            }, 
            function( error ){
                $scope.$parent.addErrorAlert( "Could not fetch ledger entries.\n" +
                                              error.data.message ) ;
            }
        )
        .finally(function() {
            $scope.$emit( 'interactingWithServer', { isStart : false } ) ;
        }) ;        
    }
    
    function saveCategoryEditChangesOnServer( categoryList, 
                                              successCallback, 
                                              errorCallback ) {
        
        $http.post( '/Ledger/Categories', categoryList )
        .then ( 
            function(){
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
    }
    
    function mergeCategoriesOnServer( oldCatId, newL1CatName, newL2CatName ) {
        
        $scope.$emit( 'interactingWithServer', { isStart : true } ) ;
        $http.post( '/Ledger/Categories/Merge', {
            oldCatId : oldCatId,
            newL1CatName : newL1CatName,
            newL2CatName : newL2CatName
        } )
        .then ( 
            function(){
                console.log( "Changes successfully saved on server" ) ;
                fetchClassificationCategories() ;
            }, 
            function( error ){
                $scope.$parent.addErrorAlert( "Error saving changes on server.\n" + 
                                              error.data.message ) ;
                console.log( error ) ;
            }
        )
        .finally(function() {
            $scope.$emit( 'interactingWithServer', { isStart : false } ) ;
        }) ;
    }
    
    function validateLoadingRuleOnServer( successCallback, errorCallback ) {
        
        $http.post( '/Ledger/Category/AmountLoading/Validate', 
                    $scope.catEditCtx.amountLoadingRule )
        .then ( 
            function( response ){
                successCallback( response.data.yearlyCap,
                                 response.data.monthlyCap ) ;
            }, 
            function( error ){
                errorCallback( error.data.message ) ;
            }
        )
    }
    
    // ------------------- Server response processors ------------------------
    
    function clearCatEditCtx() {
        
        $scope.catEditCtx.cat = null ;
        $scope.catEditCtx.catData = null ;
        $scope.catEditCtx.selectedL1CatName = null ;
        $scope.catEditCtx.selectedL2CatName = null ;
        $scope.catEditCtx.newL1CatName = null ;
        $scope.catEditCtx.newL2CatName = null ;
        $scope.catEditCtx.amountLoadingRule = null ;
    }

    function clearLoadingRuleValidationResult() {
        $scope.loadingRuleValidationResult.validationMsg = null ;
        $scope.loadingRuleValidationResult.yearlyCap = 0 ;
        $scope.loadingRuleValidationResult.monthlyCap.length = 0 ;
    }

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
    
    function populateNumLedgerEntries( counters ) {
        for( var i=0; i<counters.length; i++ ) {
            
            var counter = counters[i] ;
            var catData = counter.isCreditEntry ? 
                            $scope.ledgerCategories.credit : 
                            $scope.ledgerCategories.debit ;
                            
            if( catData.l2Categories.has( counter.l1CatName ) ) {
                var l2Array = catData.l2Categories.get( counter.l1CatName ) ;
                
                for( var j=0; j<l2Array.length; j++ ) {
                    var cat = l2Array[j] ;
                    if( cat.l2CatName == counter.l2CatName ) {
                        cat.numLedgerEntries = counter.numEntries ;
                        break ;
                    }
                }
            }
        }
    }
    
    function validateNewL1CatName() {
        
        
        if( $scope.catEditCtx.selectedL1CatName == null && 
            $scope.catEditCtx.newL1CatName == null ) {
            console.log( "No new parent category selected." ) ;
            return null ;
        }
        
        var newL1CatName = ( $scope.catEditCtx.selectedL1CatName == null ) ? 
                             $scope.catEditCtx.newL1CatName : 
                             $scope.catEditCtx.selectedL1CatName ;
                             
        return newL1CatName ;
    }
    
    function validateNewL2CatName() {
        
        if( $scope.catEditCtx.selectedL2CatName == null && 
            $scope.catEditCtx.newL2CatName == null ) {
            console.log( "No new L2 category selected." ) ;
            return null ;
        }
        
        var newL2CatName = ( $scope.catEditCtx.selectedL2CatName == null ) ? 
                             $scope.catEditCtx.newL2CatName : 
                             $scope.catEditCtx.selectedL2CatName ;
                             
        return newL2CatName ;
    }
    
    function applyNewL1CatName( newL1CatName, editCtx ) {
        
        var categoryList = [] ;
        var cat = editCtx.cat ;
        
        cat.l1CatName = newL1CatName ;
        categoryList.push( cat ) ;
        
        // Much easier to rearrange the tree by fetching new data rather
        // than trying to manipulate the client side data structure
        saveCategoryEditChangesOnServer( categoryList,
            function() { // Called on success
                fetchClassificationCategories() ;
            },
            function() { // Called on failure
                fetchClassificationCategories() ;
            } 
        ) ;        
    }
} ) ;