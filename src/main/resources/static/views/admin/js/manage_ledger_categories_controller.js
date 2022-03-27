capitalystNgApp.controller( 'ManageLedgerCategoriesController', 
    function( $scope, $http ) {
    
    // ---------------- Local variables --------------------------------------
    
    // ---------------- Scope variables --------------------------------------
    $scope.$parent.navBarTitle = "Manage L1 and L2 ledger categories" ;
    $scope.$parent.activeModuleId = "cat_management" ;
    
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

    // -----------------------------------------------------------------------
    // --- [START] Controller initialization ---------------------------------
    console.log( "Loading ManageLedgerCategoriesController" ) ;
    initializeController() ;
    // --- [END] Controller initialization -----------------------------------
    
    
    // -----------------------------------------------------------------------
    // --- [START] Scope functions -------------------------------------------

    // --- [END] Scope functions

    // -----------------------------------------------------------------------
    // --- [START] Local functions -------------------------------------------
    
    function initializeController() {
        fetchClassificationCategories() ;
    }
    
    // ------------------- Server comm functions -----------------------------
    function fetchClassificationCategories() {
        
        $scope.$emit( 'interactingWithServer', { isStart : true } ) ;
        $http.get( '/Ledger/Categories' )
        .then ( 
            function( response ){
                populateMasterCategories( response.data ) ;
                console.log( $scope.masterCategories ) ;
            }, 
            function( error ){
                $scope.$parent.addErrorAlert( "Could not fetch classification categories." ) ;
            }
        )
        .finally(function() {
            $scope.$emit( 'interactingWithServer', { isStart : false } ) ;
        }) ;
    }
    
    // ------------------- Server response processors ------------------------
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
        
        if( l1CatList.indexOf( l1 ) == -1 ) {
            l1CatList.push( l1 ) ;
        }
        
        if( !l2CatMap.has( l1 ) ) {
            l2CatMap.set( l1, [] ) ;
        }
        
        var l2List = l2CatMap.get( l1 ) ;
        l2List.push( category ) ;
    }
        
} ) ;