function AssociatedCreditTxn( creditLedgerEntry, associationId, amount, note ) {
    
    this.associationId   = associationId ;
    this.creditTxn       = creditLedgerEntry ;
    this.recoveredAmount = amount ;
    this.note            = note ;
}

capitalystNgApp.controller( 'AssociatedCreditsController', 
    function( $scope, $http ) {
    
    // ---------------- Local variables --------------------------------------
    
    // ---------------- Scope variables --------------------------------------
    $scope.debitTxn = null ;
    $scope.associatedCreditTxns = [] ;
    $scope.debitAmtRemaining = 0 ;
    
    // -------------------------------------------------------------------------
    // --- [START] Scope functions ---------------------------------------------
    
    $scope.$on( 'debitTxnSetForAssociatedCreditsDialog', 
                function( _event, debitTxn ) {
                    
        clearState() ;
        $scope.debitTxn = debitTxn ;
        
        getAssociatedCreditTxns( $scope.debitTxn.id, function(){
            recomputeRemainingDebitAmt() ;
        } ) ;
    } ) ;
    
    $scope.removeCreditAssociation = function( index ) {
        
        var removedAssoc = $scope.associatedCreditTxns.splice( index, 1 )[0] ;
        recomputeRemainingDebitAmt() ;
        
        if( removedAssoc.associationId != -1 ) {
            
            $http.delete( '/DebitCreditAssociation/' + removedAssoc.associationId )
            .then ( 
                function(){
                    console.log( "Successfully deleted association on server." ) ;
                }, 
                function(){
                    $scope.$parent.addErrorAlert( "Error saving associations." ) ;
                }
            )
        }
    }
    
    // --- [END] Scope functions

    // -------------------------------------------------------------------------
    // --- [START] Local functions ---------------------------------------------
    function clearState() {
        
        $scope.debitTxn = null ;
        $scope.associatedCreditTxns.length = 0 ;
    }
    
    // During initialization, for the given debit transaction fetch any 
    // existing credit transactions.
    function getAssociatedCreditTxns( debitTxnId, callback ) {
        
        $http.get( '/CreditAssociation/'  + debitTxnId )
        .then ( 
            function( response ){
                
                // Thre response is a list of tupules where each tupule consists
                // of the following:
                //
                //  1. The debit credit association. This contains the association
                //     attributes like description and the recovery amount
                //  2. Credit ledger entry. Note that all credit entries share
                //     the same debit with which this dialog is instantiated
                //  3. A list of possible debit associations which might be
                //     trying to feed from the credit transaction in this tupule
                for( var i=0; i<response.data.length; i++ ) {
                    var tupule = response.data[i] ;
                    
                    var dca               = tupule[0] ;
                    var creditLedgerEntry = tupule[1] ;
                    
                    var entry = new AssociatedCreditTxn( creditLedgerEntry,
                                                         dca.id,
                                                         dca.amount,
                                                         dca.note ) ;
                                                      
                    $scope.associatedCreditTxns.push( entry ) ;
                }
                
                callback() ;
            }, 
            function(){
                $scope.$parent.addErrorAlert( "Error getting associated credit txns." ) ;
            }
        )
    }
    
    function recomputeRemainingDebitAmt() {
        
        $scope.debitAmtRemaining = $scope.debitTxn.amount ;
        for( var i=0; i<$scope.associatedCreditTxns.length; i++ ) {
            var assocCredit = $scope.associatedCreditTxns[i] ;
            $scope.debitAmtRemaining += assocCredit.recoveredAmount ;
        }
    }
} ) ;