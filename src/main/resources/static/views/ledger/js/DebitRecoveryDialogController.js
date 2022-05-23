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
    
    $scope.creditAmtRemaining = 0 ;
    $scope.errorMessages = [] ;
    
    $scope.saveSuccessFlag = false ;
    
    // -----------------------------------------------------------------------
    // --- [START] Controller initialization ---------------------------------
    console.log( "Loading DebitRecoveryDialogController." ) ;
    // --- [END] Controller initialization -----------------------------------
    
    
    // -----------------------------------------------------------------------
    // --- [START] Scope functions -------------------------------------------
    $scope.hideDebitRecoveryDialog = function() {
        $( '#debitRecoveryDialog' ).modal( 'hide' ) ;
    }

    $scope.$on( 'creditTxnSetForDebitRecoveryDialog', function( event, creditTxn ) {
        initializeController( creditTxn ) ;
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
                                          getMaxRecoverableAmount( debitTxn ), 
                                          "" ) ;
                                          
        $scope.selectedDebitTxns.push( entry ) ;
        $scope.recomputeRemainingCreditAmt() ;
    }
    
    $scope.recomputeRemainingCreditAmt = function() {
        
        $scope.creditAmtRemaining = $scope.creditTxn.amount ;
        
        for( var i=0; i<$scope.selectedDebitTxns.length; i++ ) {
            var debit = $scope.selectedDebitTxns[i] ;
            $scope.creditAmtRemaining -= debit.recoveredAmount ;    
        }
    }
    
    $scope.isValidAssociation = function() {
        
        $scope.errorMessages.length = 0 ;
        
        validateZeroAssociations() ;
        validateNegativeAssociationAmounts() ;
        validateRecoveredAmountLessThanCreditAmount() ;
        
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
                $scope.saveSuccessFlag = true ;
                setTimeout( function(){
                    $scope.saveSuccessFlag = false ;
                    $scope.$apply() ;
                }, 1000 ) ;
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

    // -----------------------------------------------------------------------
    // --- [START] Local functions -------------------------------------------
    function clearState() {
        
        $scope.creditTxn = null ;
        $scope.creditAmtRemaining = 0 ;
        $scope.currentResultPage = -1 ;
        $scope.selectedDebitTxns.length = 0 ;
        $scope.debitTxnList.length = 0 ;
    }
    
    function initializeController( creditTxn ) {
        
        clearState() ;
        
        $scope.creditTxn = creditTxn ;
        $scope.creditAmtRemaining = $scope.creditTxn.amount ;
        
        // Fetch any already associated debit transactions
        getAssociatedDebitTxns( $scope.creditTxn.id, function(){
            
            for( var i=0; i<$scope.selectedDebitTxns.length; i++ ) {
                var txn = $scope.selectedDebitTxns[i] ;
                $scope.creditAmtRemaining -= txn.recoveredAmount ;
            }
            
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
    
    function getMaxRecoverableAmount( debitTxn ) {
        
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
} ) ;