function SelectedDebitTxn( debitTxn, associationId, recoveredAmt, note ) {
    
    this.associationId   = associationId ;
    this.debitTxn        = debitTxn ;
    this.recoveredAmount = recoveredAmt ;
    this.note            = note ;
}

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
        getPaginatedDebitTxns( $scope.creditTxn.id, 1 ) ;
    } 
    
    $scope.getPreviousBatchOfDebitTxns = function() {
        getPaginatedDebitTxns( $scope.creditTxn.id, -1 ) ;
    } 
    
    $scope.alreadySelected = function( entry ) {
        
        for( var i=0; i<$scope.selectedDebitTxns.length; i++ ) {
            var selEntry = $scope.selectedDebitTxns[i] ;
            if( entry.id == selEntry.debitTxn.id ) {
                return true ;
            }
        }
        return false ;
    }
    
    $scope.selectDebitTxn = function( debitTxn ) {
        
        var entry = new SelectedDebitTxn( debitTxn, 
                                          -1,
                                          getRemainingAmount( debitTxn ), 
                                          "" ) ;
                                          
        $scope.selectedDebitTxns.push( entry ) ;
    }
    
    $scope.removeDebitTxn = function( index ) {
        $scope.selectedDebitTxns.splice( index, 1 ) ;
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
        
        // Fetch any already associated debit transactions
        getAssociatedDebitTxns( $scope.creditTxn.id, function(){
            // Fetch the first batch of debit transactions
            $scope.getNextBatchOfDebitTxns() ;
        }) ;
        
    }
    
    function getAssociatedDebitTxns( creditTxnId, callback ) {
        
        $scope.selectedDebitTxns.length = 0 ;
        
        $http.get( '/DebitAssociation/'  + creditTxnId )
        .then ( 
            function( response ){
                for( var i=0; i<response.data.length; i++ ) {
                    
                    var tupule   = response.data[i] ;
                    var dca      = tupule[0] ;
                    var debitTxn = tupule[1] ;
                    
                    var entry = new SelectedDebitTxn( debitTxn,
                                                      dca.id,
                                                      dca.amount,
                                                      dca.note ) ;
                                                      
                    $scope.selectedDebitTxns.push( entry ) ;
                }
                callback() ;
            }, 
            function(){
                $scope.$parent.addErrorAlert( "Error getting associated debit txns." ) ;
            }
        )
    }
    
    function getPaginatedDebitTxns( refTxnId, pageStep ) {
        
        if( $scope.currentResultPage <=0 && pageStep == -1 ) {
            return ;
        }
        
        $scope.debitTxnList.length = 0 ;
        $scope.currentResultPage += pageStep ;
        
        var numTxnsPerPage = 10 ;
        var offset = $scope.currentResultPage * numTxnsPerPage ;
        
        $http.get( '/Ledger/PaginatedDebitEntries' + 
                                       '?refTxnId=' + refTxnId + 
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
    
    function getRemainingAmount( debitTxn ) {
        
        var remainingAmt = $scope.creditTxn.amount ;
        for( var i=0; i<$scope.selectedDebitTxns.length; i++ ) {
            var debit = $scope.selectedDebitTxns[i] ;
            remainingAmt -= debit.recoveredAmount ;    
        }
        
        remainingAmt = remainingAmt > 0 ? remainingAmt : 0 ;
        if( remainingAmt > -1*debitTxn.amount ) {
            remainingAmt = ( -1 * debitTxn.amount ) ;
        }
        return remainingAmt ;
    }
} ) ;