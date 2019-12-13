capitalystNgApp.controller( 'CashEntryController', 
    function( $scope, $http, $rootScope, $location, $window, $ngConfirm ) {
    
    // ---------------- Local variables --------------------------------------
    
    // ---------------- Scope variables --------------------------------------

    // Common framework variables. 
    $scope.alerts = [] ;
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
    console.log( "Loading CashEntryController" ) ;
    initializeController() ;
    // --- [END] Controller initialization -----------------------------------
    
    
    // -----------------------------------------------------------------------
    // --- [START] Scope functions -------------------------------------------
    $scope.$on( 'interactingWithServer', function( event, args ) {
        if( args.isStart ) {
            $( '#serverInteractionLoader' ).modal( 'show' ) ;
        }
        else {
            $( '#serverInteractionLoader' ).modal( 'hide' ) ;
        }
    } ) ;

    // ----------- UI related scope functions --------------------------------
    $scope.addErrorAlert = function( msgString ) {
        console.log( msgString ) ;
        $scope.alerts.push( { type: 'danger', msg: msgString } ) ;
    } ;
    
    $scope.dismissAlert = function( index ) {
        $scope.alerts.splice( index, 1 ) ;
    }
    // --- [END] Scope functions

    // -----------------------------------------------------------------------
    // --- [START] Local functions -------------------------------------------
    
    function initializeController() {
        fetchClassificationCategories() ;
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
    // ------------------- Server comm functions -----------------------------
} ) ;