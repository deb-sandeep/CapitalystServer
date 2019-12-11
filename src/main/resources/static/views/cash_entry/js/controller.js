capitalystNgApp.controller( 'CashEntryController', 
    function( $scope, $http, $rootScope, $location, $window, $ngConfirm ) {
    
    // ---------------- Local variables --------------------------------------
    
    // ---------------- Scope variables --------------------------------------

    // Common framework variables. 
    $scope.alerts = [] ;
    
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
    }
    
    // ------------------- Server comm functions -----------------------------
} ) ;