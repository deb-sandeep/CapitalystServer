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
            $scope.txns.push( txn ) ;
            
            $scope.totalQuantity += txn.quantityLeft ;
            $scope.totalCost += txn.quantityLeft * txn.txnPrice ;
            $scope.avgCostPrice = $scope.totalCost / $scope.totalQuantity ;
            $scope.totalValuePostTax += txn.valuePostTax ;
            $scope.totalPAT += txn.pat ;
        }
    }
    // ------------------- Server comm functions -----------------------------
} ) ;