capitalystNgApp.controller( 'DebitRecoveryDialogController', 
    function( $scope, $http, $ngConfirm ) {
    
    // ---------------- Local variables --------------------------------------
    
    // ---------------- Scope variables --------------------------------------
    $scope.creditTxn = null ;
    $scope.selectedDebitTxns = [] ;
    $scope.debitTxnList = [] ;
    $scope.currentResultPage = -1 ;

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
        
        $scope.creditTxn = args ;
        initializeController() ;
    } ) ;
    
    $scope.getNextBatchOfDebitTxns = function() {
        getDebitTxns( $scope.creditTxn.id, 1 ) ;
    } 
    
    $scope.getPreviousBatchOfDebitTxns = function() {
        getDebitTxns( $scope.creditTxn.id, -1 ) ;
    } 
    
    $scope.alreadySelected = function( entry ) {
        
        for( var i=0; i<$scope.selectedDebitTxns.length; i++ ) {
            var selEntry = $scope.selectedDebitTxns[i] ;
            if( entry.id == selEntry.id ) {
                return true ;
            }
        }
        return false ;
    }
    
    $scope.selectDebitTxn = function( entry ) {
        $scope.selectedDebitTxns.push( entry ) ;
    }
    
    // --- [END] Scope functions

    // -----------------------------------------------------------------------
    // --- [START] Local functions -------------------------------------------
    function clearState() {
        
        $scope.currentResultPage = -1 ;
        $scope.selectedDebitTxns.length = 0 ;
        $scope.debitTxnList.length = 0 ;
    }
    
    function initializeController() {
        
        clearState() ;
        $scope.getNextBatchOfDebitTxns() ;
    }
    
    function getDebitTxns( refTxnId, pageStep ) {
        
        if( $scope.currentResultPage <=0 && pageStep == -1 ) {
            return ;
        }
        
        $scope.debitTxnList.length = 0 ;
        $scope.currentResultPage += pageStep ;
        
        var numTxnsPerPage = 10 ;
        var offset = $scope.currentResultPage * numTxnsPerPage ;
        
        $http.get( '/Ledger/DebitEntries?refTxnId=' + refTxnId + 
                                       '&offset='   + offset + 
                                       '&numTxns='  + numTxnsPerPage )
        .then ( 
            function( response ){
                $scope.debitTxnList = response.data ;
            }, 
            function(){
                $scope.$parent.addErrorAlert( "Error getting debit txn batch." ) ;
            }
        )
    }
} ) ;