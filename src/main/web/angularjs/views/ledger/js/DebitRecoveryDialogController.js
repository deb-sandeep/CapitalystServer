function SelectedDebitTxn( debitTxn, associationId, note ) {
    
    this.associationId   = associationId ;
    this.debitTxn        = debitTxn ;
    this.recoveredAmount = 0 ;
    this.note            = note ;
    
    this.maxRecoverableAmount = 0 ;
    this.otherCreditTxns      = [] ;
}

capitalystNgApp.controller( 'DebitRecoveryDialogController', 
    function( $scope, $http, $ngConfirm ) {
    
    // ---------------- Local variables --------------------------------------
    
    // ---------------- Scope variables --------------------------------------
    $scope.creditTxn = null ;
    $scope.selectedDebitTxns = [] ;
    $scope.debitTxnList = [] ;
    $scope.currentResultPage = -1 ;
    
    $scope.creditAmtRemaining = 0 ;
    $scope.errorMessages = [] ;
    
    $scope.saveSuccessFlag = false ;
    
    // -------------------------------------------------------------------------
    // --- [START] Scope functions ---------------------------------------------
    
    $scope.$on( 'creditTxnSetForDebitRecoveryDialog', 
                function( event, creditTxn ) {
                    
        clearState() ;
        
        $scope.creditTxn = creditTxn ;
        $scope.creditAmtRemaining = $scope.creditTxn.amount ;
        
        // Fetch any already associated debit transactions
        getAssociatedDebitTxns( $scope.creditTxn.id, function(){
            
            // Deduct the recovered amounts from the credit amount that remains
            for( var i=0; i<$scope.selectedDebitTxns.length; i++ ) {
                var txn = $scope.selectedDebitTxns[i] ;
                $scope.creditAmtRemaining -= txn.recoveredAmount ;
            }
            
            // Fetch the first page of debit transactions
            $scope.getNextBatchOfDebitTxns() ;
        }) ;
    } ) ;
    
    $scope.getNextBatchOfDebitTxns = function() {
        getPaginatedDebitTxns( $scope.creditTxn.id, 1 ) ;
    } 
    
    $scope.getPreviousBatchOfDebitTxns = function() {
        getPaginatedDebitTxns( $scope.creditTxn.id, -1 ) ;
    } 
    
    // If a debit ledger entry is already selected, hide the selection link
    // from it, preventing it from being selected again and visually denoting
    // that this entry is alredy selected for association. Note that this marker
    // does not imply that association with this entry is saved in the database.
    $scope.isLedgerEntryAlreadySelected = function( entry ) {
        
        for( var i=0; i<$scope.selectedDebitTxns.length; i++ ) {
            var selEntry = $scope.selectedDebitTxns[i] ;
            if( entry.id == selEntry.debitTxn.id ) {
                return true ;
            }
        }
        return false ;
    }
    
    // Called when a debit ledger entry is selected for association with the 
    // credit entry.
    $scope.selectDebitTxn = function( debitLedgerEntry ) {
        
        var entry = new SelectedDebitTxn( debitLedgerEntry, -1, "" ) ;
                                          
        $scope.selectedDebitTxns.push( entry ) ;
        
        // Fetch any associated recovery credit transactions (other than the
        // current credit transaction) and adjust the remaining credit 
        // accordingly. Note that a debit entry can be recovered by more than
        // one credit entry. Doing this ensures that we don't over recover
        // a debit entry.
        associateOtherCredits( entry ) ;
    }
    
    // Computes how much of the credit transaction amount is still available
    // for recovering debit expenses.
    $scope.recomputeRemainingCreditAmt = function() {
        
        $scope.creditAmtRemaining = $scope.creditTxn.amount ;
        
        for( var i=0; i<$scope.selectedDebitTxns.length; i++ ) {
            var debit = $scope.selectedDebitTxns[i] ;
            $scope.creditAmtRemaining -= debit.recoveredAmount ;    
        }
    }
    
    $scope.isValidAssociation = function() {
        
        $scope.errorMessages.length = 0 ;
        
        // At least one valid debit association should exist
        validateZeroAssociations() ;
        
        // None of the associated debit recoveries should be negative
        validateNegativeAssociationAmounts() ;
        
        // None of the recovered amount should be more than the credit amount
        validateRecoveredAmountLessThanCreditAmount() ;
        
        // None of the recovered amount should be more than the max allowed
        // recovery amount for that debit transaction. Remember that a debit
        // transaction can be recovered through many credit transactions.
        validateRecoveryMoreThanAllowed() ;
        
        return $scope.errorMessages.length == 0 ;
    }
    
    $scope.showErrorMessages = function() {
        
        var content = "<ul>"
        for( var i=0; i<$scope.errorMessages.length; i++ ) {
            content += "<li>" + $scope.errorMessages[i] + "</li>" ;
        }
        content += "</ul>" ;
        
        $ngConfirm({
            title: 'Association errors',
            content: content,
            scope: $scope,
            buttons: {
                ok: {
                    text: 'Ok',
                    btnClass: 'btn-default',
                    action: function(){
                        return true ;
                    }
                },
            }
        });
    }
    
    // This function can be called when all the validations have passed.
    $scope.saveAssociations = function() {
        
        var associations = [] ;
        
        for( var i=0; i<$scope.selectedDebitTxns.length; i++ ) {
            var debit = $scope.selectedDebitTxns[i] ;
            associations.push( {
                "id" : debit.associationId,
                "creditTxnId" : $scope.creditTxn.id,
                "debitTxnId"  : debit.debitTxn.id,
                "amount"      : debit.recoveredAmount,
                "note"        : debit.note
            }) ;
        }
        
        $http.post( '/DebitCreditAssociation', associations )
        .then ( 
            function( response ){
                
                var savedAssociations = response.data ;

                // Saved associations have their id updated to a non negative
                // integer. We need to populate the new id with the approprirate
                // selected debit entry, so that any future updates will 
                // happen appropriately.
                for( var j=0; j<savedAssociations.length; j++ ) {
                    var savedAssoc = savedAssociations[j] ;
                    
                    for( var i=0; i<$scope.selectedDebitTxns.length; i++ ) {
                        var debit = $scope.selectedDebitTxns[i] ;
                        
                        if( debit.associationId == -1 &&
                            debit.debitTxn.id  == savedAssoc.debitTxnId &&
                            $scope.creditTxn.id == savedAssoc.creditTxnId ) {
                            
                            debit.associationId = savedAssoc.id ;
                        }
                    }
                }
                
                // A temporary visual marker to show that the save was successful
                // The marker disappears after a second.
                $scope.saveSuccessFlag = true ;
                setTimeout( function(){
                    $scope.saveSuccessFlag = false ;
                    $scope.$apply() ;
                    if( $scope.isValidAssociation() ) {
                        $scope.$parent.hideDebitRecoveryDialog() ;
                    }
                }, 500 ) ;
            },
            function(){
                $scope.$parent.addErrorAlert( "Error saving associations." ) ;
            }
        )
    }
    
    $scope.removeDebitTxn = function( index ) {
        
        var removedAssoc = $scope.selectedDebitTxns.splice( index, 1 )[0] ;
        $scope.recomputeRemainingCreditAmt() ;
        
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
        
        $scope.creditTxn = null ;
        $scope.creditAmtRemaining = 0 ;
        $scope.currentResultPage = -1 ;
        $scope.selectedDebitTxns.length = 0 ;
        $scope.debitTxnList.length = 0 ;
    }
    
    // During initialization, for the given credit transaction fetch any 
    // existing debit transactions.
    function getAssociatedDebitTxns( creditTxnId, callback ) {
        
        $scope.selectedDebitTxns.length = 0 ;
        
        $http.get( '/DebitAssociation/'  + creditTxnId )
        .then ( 
            function( response ){
                
                // Thre response is a list of tupules where each tupule consists
                // of the following:
                //
                //  1. The debit credit association. This contains the association
                //     attributes like description and the recovery amount
                //  2. Debit ledger entry. Note that all debit entries share
                //     the same credit with which this dialog is instantiated
                //  3. A list of possible credit associations which might be
                //     trying to recover the debit transaction in this tupule
                for( var i=0; i<response.data.length; i++ ) {
                    var tupule = response.data[i] ;
                    
                    var dca              = tupule[0] ;
                    var debitLedgerEntry = tupule[1] ;
                    var creditDCAs       = tupule[2] ;
                    
                    var entry = new SelectedDebitTxn( debitLedgerEntry,
                                                      dca.id,
                                                      dca.note ) ;
                                                      
                    entry.recoveredAmount = dca.amount ;
                                                      
                    var alreadyRecoveredAmount = 0 ;
                    for( var j=0; j<creditDCAs.length; j++ ) {
                        var creditAssociation = creditDCAs[j] ;
                        
                        if( creditAssociation.creditTxnId != $scope.creditTxn.id ) {
                            entry.otherCreditTxns.push( creditAssociation ) ;
                            alreadyRecoveredAmount += creditAssociation.amount ;
                        }
                    }
                    
                    entry.maxRecoverableAmount = entry.debitTxn.amount +
                                                       alreadyRecoveredAmount ;
                                                       
                    $scope.selectedDebitTxns.push( entry ) ;
                }
                callback() ;
            }, 
            function(){
                $scope.$parent.addErrorAlert( "Error getting associated debit txns." ) ;
            }
        )
    }
    
    // When a debit ledger entry is selected from the paginated list we fetch
    // any associated credit transactions (other than the current credit txn)
    // This will help us determine the max recoverable amount of this debit
    // entry.
    function associateOtherCredits( selDebitTxn, callback ) {
        
        $http.get( '/CreditAssociation/'  + selDebitTxn.debitTxn.id )
        .then ( 
            function( response ){
                
                var alreadyRecoveredAmount = 0 ;
                for( var i=0; i<response.data.length; i++ ) {
                    
                    var tupule  = response.data[i] ;
                    var dca     = tupule[0] ;
                    
                    // We only consider credit transactions apart from the one
                    // we are dealing with                                  
                    if( $scope.creditTxn.id != dca.creditTxnId ) {
                        selDebitTxn.otherCreditTxns.push( dca ) ;
                        alreadyRecoveredAmount += dca.amount ;
                    }
                }
                
                selDebitTxn.maxRecoverableAmount = selDebitTxn.debitTxn.amount +
                                                   alreadyRecoveredAmount ;
                
                selDebitTxn.recoveredAmount = getDefaultRecoveryAmount( selDebitTxn ) ;
                
                // Since we have populated the default recovery amount, we 
                // also need to recompute the remaining credit amount for the 
                // main credit transaction.
                $scope.recomputeRemainingCreditAmt() ;
            }, 
            function(){
                $scope.$parent.addErrorAlert( "Error getting associated credit txns." ) ;
            }
        )
    }
    
    // For a selected debit ledger entry, compute the default recovery amount.
    function getDefaultRecoveryAmount( selDebitTxn ) {
        
        // To start with default recovery amount is the entire credit amount 
        var defaultRecoveryAmt = $scope.creditTxn.amount ;
        
        // Subtract the recovery amounts from all the associated debit txns
        for( var i=0; i<$scope.selectedDebitTxns.length; i++ ) {
            var debit = $scope.selectedDebitTxns[i] ;
            defaultRecoveryAmt -= debit.recoveredAmount ;    
        }
        
        // Negative is not logical, so set minimum to zero
        defaultRecoveryAmt = defaultRecoveryAmt > 0 ? defaultRecoveryAmt : 0 ;
        
        // Cap it at the max recoverable amount for the selected debit ledger entry
        if( defaultRecoveryAmt > -1*selDebitTxn.maxRecoverableAmount ) {
            defaultRecoveryAmt = ( -1 * selDebitTxn.maxRecoverableAmount ) ;
        }
        
        return defaultRecoveryAmt ;
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
    
    function validateZeroAssociations() {
        
        if( $scope.selectedDebitTxns.length == 0 ) {
            var msg = "No associated debit transactions." ;
            $scope.errorMessages.push( msg ) ;
        }
    }
    
    function validateNegativeAssociationAmounts() {
        
        for( var i=0; i<$scope.selectedDebitTxns.length; i++ ) {
            var debit = $scope.selectedDebitTxns[i] ;
            if( debit.recoveredAmount <= 0 ) {
                var msg = "Txn " + (i+1) + " no recovered amount." ;
                $scope.errorMessages.push( msg ) ;
            }   
        }
    }
    
    function validateRecoveredAmountLessThanCreditAmount() {
        
        if( $scope.creditAmtRemaining < 0 ) {
            var msg = "Total associated amt is more than credit amount." ;
            $scope.errorMessages.push( msg ) ;
        }
    }
    
    function validateRecoveryMoreThanAllowed() {
        
        for( var i=0; i<$scope.selectedDebitTxns.length; i++ ) {
            var debit = $scope.selectedDebitTxns[i] ;
            if( debit.recoveredAmount > Math.abs( debit.maxRecoverableAmount ) ) {
                var msg = "Txn " + (i+1) + " recovery is more than permissible. " +
                          "Max = " + (-1*debit.maxRecoverableAmount)  ;
                $scope.errorMessages.push( msg ) ;
            }   
        }
    }
} ) ;
