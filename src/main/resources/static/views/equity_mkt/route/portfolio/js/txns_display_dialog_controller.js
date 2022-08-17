capitalystNgApp.controller( 'TxnsDisplayDialogController', function( $scope ) {
    
    // ---------------- Local variables --------------------------------------
    
    // ---------------- Scope variables --------------------------------------
    $scope.holding = null ;
    $scope.symbol = null ;
    $scope.companyName = null ;
    $scope.owner = null ;
    $scope.holdingType = null ;
    $scope.txns = [] ;
    $scope.totalQuantity = 0 ;
    $scope.totalCost = 0 ;
    $scope.avgCostPrice = 0 ;
    $scope.totalValuePostTax = 0 ;
    $scope.totalPAT = 0 ;
    
    // Aggregate value of selectedt transactions
    $scope.selectedTxnsTotalQty = 0 ;
    $scope.selectedTxnsAvgCost = 0 ;
    $scope.selectedTxnsBuyCost = 0 ;
    $scope.selectedTxnsMktValue = 0 ;
    $scope.selectedTxnsPAT = 0 ;
    $scope.selectedTxnsPATPct = 0 ;
    
    function clearState() {
        
        $scope.holding = null ;
        $scope.symbol = null ;
        $scope.companyName = null ;
        $scope.owner = null ;
        $scope.holdingType = null ;
        $scope.txns.length = 0 ;
        $scope.totalQuantity = 0 ;
        $scope.totalCost = 0 ;
        $scope.avgCostPrice = 0 ;
        $scope.totalValuePostTax = 0 ;
        $scope.totalPAT = 0 ;
        
        clearAggregateValueState() ;
    }
    
    function clearAggregateValueState() {
        
        $scope.selectedTxnsTotalQty = 0 ;
        $scope.selectedTxnsAvgCost = 0 ;
        $scope.selectedTxnsBuyCost = 0 ;
        $scope.selectedTxnsMktValue = 0 ;
        $scope.selectedTxnsPAT = 0 ;
        $scope.selectedTxnsPATPct = 0 ;
    }
        
    // -----------------------------------------------------------------------
    // --- [START] Controller initialization ---------------------------------
    console.log( "Loading TxnsDisplayDialogController" ) ;
    // --- [END] Controller initialization -----------------------------------
    
    // -----------------------------------------------------------------------
    // --- [START] Scope functions -------------------------------------------
    
    $scope.$on( 'holdingTxnsDisplayTrigger', function( _event, args ) {
        initialize( args.holding, args.holdingType ) ;
    } ) ;

    $scope.hideTransactionsDialog = function() {
        $scope.$parent.holdingForSelectedTxns = null ;
        $( '#txnsDisplayDialog' ).modal( 'hide' ) ;
    }
    
    $scope.ltcgRowClass = function( txn ) {
        if( txn.ltcgQuailifed ) {
            return "ltcg_row" ;
        }
        return null ;
    }
    
    $scope.txnSelectionChanged = function( txn ) {
        reCalculateSelectionTotals() ;
    }
    
    // --- [END] Scope functions

    // -----------------------------------------------------------------------
    // --- [START] Local functions -------------------------------------------
    function initialize( holding, holdingType ) {
        clearState() ;
        
        $scope.holding = holding ;
        $scope.holdingType = holdingType ;
        
        // If holdingType is Family, the holding is of type FamilyEquityHoldingVO
        // which will contain multiple individual holding, each with its own
        // list of transactions (EquityTxnVO).
        //
        // In case  the holdingType is not Family (Individual), the holding
        // will be of type IndividualEquityHoldingVO and will contain a list 
        // of transactions.
        if( holdingType == 'Family' ) {
            for( var i=0; i<holding.holdings.length; i++ ) {
                var indHolding = holding.holdings[i] ;
                extractTxns( indHolding.ownerName, indHolding.txns ) ;
            }
            $scope.owner = "Family" ;
        }
        else {
            extractTxns( holding.ownerName, holding.txns ) ;
            $scope.owner = holding.ownerName ;
        }
        
        $scope.symbol = holding.symbolIcici ;
        $scope.companyName = holding.companyName ;
        
        $scope.txns.sort( function( t1, t2 ){
            var n1 = Date.parse( t1.txnDate ) ;
            var n2 = Date.parse( t2.txnDate ) ;
            return n1-n2 ;
        }) ;
    } 
    
    function extractTxns( ownerName, txns ) {
        
        for( var i=0; i<txns.length; i++ ) {
            
            var txn = txns[i] ;
            
            // Injecting new attribute
            txn.ownerName = ownerName ;
            txn.selected = txn.ltcgQuailifed ;
            
            $scope.txns.push( txn ) ;
            
            $scope.totalQuantity += txn.quantityLeft ;
            $scope.totalCost += txn.quantityLeft * txn.txnPrice ;
            $scope.avgCostPrice = $scope.totalCost / $scope.totalQuantity ;
            $scope.totalValuePostTax += txn.valuePostTax ;
            $scope.totalPAT += txn.pat ;
        }
        
        reCalculateSelectionTotals() ;
    }
    
    function reCalculateSelectionTotals() {
        
        clearAggregateValueState() ;
        
        for( var i=0; i<$scope.txns.length; i++ ) {
            var txn = $scope.txns[i] ;
            
            if( txn.selected ) {
                
                $scope.selectedTxnsTotalQty += txn.quantityLeft ;
                $scope.selectedTxnsBuyCost  += txn.valueAtCost ;
                $scope.selectedTxnsMktValue += txn.valuePostTax ;

                $scope.selectedTxnsAvgCost = $scope.selectedTxnsBuyCost /
                                             $scope.selectedTxnsTotalQty ;
                                             
                $scope.selectedTxnsPAT = $scope.selectedTxnsMktValue - 
                                         $scope.selectedTxnsBuyCost ;
                                         
                $scope.selectedTxnsPATPct = ( $scope.selectedTxnsPAT / 
                                              $scope.selectedTxnsBuyCost ) * 100 ;
            }
        }
    }
    
    // ------------------- Server comm functions -----------------------------
} ) ;