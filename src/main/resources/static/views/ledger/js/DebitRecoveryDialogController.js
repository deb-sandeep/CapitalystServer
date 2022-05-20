capitalystNgApp.controller( 'DebitRecoveryDialogController', 
    function( $scope, $http, $ngConfirm ) {
    
    // ---------------- Local variables --------------------------------------
    
    // ---------------- Scope variables --------------------------------------
    $scope.creditTxn = null ;

    // -----------------------------------------------------------------------
    // --- [START] Controller initialization ---------------------------------
    console.log( "Loading DebitRecoveryDialogController." ) ;
    // --- [END] Controller initialization -----------------------------------
    
    
    // -----------------------------------------------------------------------
    // --- [START] Scope functions -------------------------------------------
    $scope.hideDebitRecoveryDialog = function() {
        $( '#debitRecoveryDialog' ).modal( 'hide' ) ;
    }

    $scope.$on( 'creditTxnSetForDebitRecoveryDialog', function( event, args ) {
        console.log( "Credit txn set for debit recovery mapping." ) ;
        $scope.creditTxn = args ;
        initializeController() ;
    } ) ;

    // --- [END] Scope functions

    // -----------------------------------------------------------------------
    // --- [START] Local functions -------------------------------------------
    function initializeController() {
        
        // Clear the state
        // Get next 15 credit txns from the txn id.
    } 
    
    function clearState() {
        
    }
    
} ) ;