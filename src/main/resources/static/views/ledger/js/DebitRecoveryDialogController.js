capitalystNgApp.controller( 'DebitRecoveryDialogController', 
    function( $scope, $http, $ngConfirm ) {
    
    // ---------------- Local variables --------------------------------------
    
    // ---------------- Scope variables --------------------------------------
    $scope.creditTxn = null ;
    $scope.selectedDebitTxns = [] ;
    $scope.debitTxnList = [] ;

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
    
    $scope.getNextBatchOfDebitTxns = function() {
        
        var refTxnId = $scope.creditTxn.id ;
        if( $scope.debitTxnList.length != 0 ) {
            var lastEntry = $scope.debitTxnList[ $scope.debitTxnList.length-1 ] ;
            refTxnId = lastEntry.id ;
        }
        getDebitTxns( refTxnId, true ) ;
    } 
    
    $scope.getPreviousBatchOfDebitTxns = function() {
        
        var refTxnId = $scope.creditTxn.id ;
        if( $scope.debitTxnList.length != 0 ) {
            var firstEntry = $scope.debitTxnList[ 0 ] ;
            refTxnId = firstEntry.id ;
        }
        getDebitTxns( refTxnId, false ) ;
    } 
    
    // --- [END] Scope functions

    // -----------------------------------------------------------------------
    // --- [START] Local functions -------------------------------------------
    function clearState() {
        
        $scope.selectedDebitTxns.length = 0 ;
        $scope.debitTxnList.length = 0 ;
    }
    
    function initializeController() {
        
        clearState() ;
        $scope.getNextBatchOfDebitTxns() ;
    }
    
    function getDebitTxns( refTxnId, prev ) {
        
        $scope.debitTxnList.length = 0 ;
        
        $http.get( '/Ledger/DebitEntries?refTxnId=' + refTxnId + 
                                       '&isNextBatch=' + prev + 
                                       '&numTxns=15' )
        .then ( 
            function( response ){
                console.log( response.data ) ;
                $scope.debitTxnList = response.data ;
            }, 
            function(){
                $scope.$parent.addErrorAlert( "Error getting debit txn batch." ) ;
            }
        )
    }
} ) ;