capitalystNgApp.controller( 'BudgetTrackerRootController', 
    function( $scope, $http, $rootScope, $location, $window, $ngConfirm ) {
    
    // ---------------- Local variables --------------------------------------
    
    // ---------------- Scope variables --------------------------------------

    // Common framework variables. 
    $scope.alerts = [] ;
    $scope.navBarTitle = "<Fill navbar title>" ;
    $scope.onMobile = window.mobileCheck() ;
    
    // -----------------------------------------------------------------------
    // --- [START] Controller initialization ---------------------------------
    console.log( "Loading TxnPivotController" ) ;
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

    $scope.triggerLedgerEntryDisplay = function( l1CatName, l2CatName, startOfMonth ) {
        $scope.$broadcast( 'ledgerEntryDisplayTrigger', {
            l1CatName : l1CatName,
            l2CatName : l2CatName,
            startOfMonth : startOfMonth
        }) ;
    }
    
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
    }
    
    // ------------------- Server comm functions -----------------------------
} ) ;