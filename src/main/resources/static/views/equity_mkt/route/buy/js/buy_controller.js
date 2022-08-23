capitalystNgApp.controller( 'BuyController', 
    function( $scope, $http ) {
    
    // ---------------- Local variables --------------------------------------
    
    // ---------------- Scope variables --------------------------------------
    $scope.$parent.navBarTitle = "Equity Buy (this FY)" ;
    $scope.$parent.activeModuleId = "buy" ;
    
    $scope.buyTxns = [] ;
    
    $scope.allTotal = {
        costPrice : 0
    } ;
    
    $scope.selTotal = {
        costPrice : 0
    } ;
    
    function resetState() {
        $scope.buyTxns.length = 0 ;
        resetTotal( $scope.allTotal ) ;
        resetTotal( $scope.selTotal ) ;
    } ;
    
    function resetTotal( total ) {
        total.costPrice = 0 ;
    }
    
    // -----------------------------------------------------------------------
    // --- [START] Controller initialization ---------------------------------
    console.log( "Loading BuyController" ) ;
    fetchBuyDataFromServer() ;
    
    // --- [END] Controller initialization -----------------------------------
    
    // -----------------------------------------------------------------------
    // --- [START] Scope functions -------------------------------------------
    $scope.getAmtClass = function( value ) {
        return ( value < 0 ) ? "neg_amt" : "pos_amt" ;
    }
    
    $scope.getRowClass = function( txn ) {
        if( txn.selected ) {
            return "selected_row" ;
        }
        return  "" ;
    }
    
    $scope.txnSelectionChanged = function() {
        recomputeSelTotals() ;
    }
    
    $scope.selectAllHoldingsForOwner = function( ownerName ) {
        
        for( var i=0; i<$scope.buyTxns.length; i++ ) {
            var txn = $scope.buyTxns[i] ;
            txn.selected = txn.parentHolding.ownerName == ownerName ;
        }    
        recomputeSelTotals() ;
    }
    
    $scope.selectAllHoldingsForSymbol = function( symbolIcici ) {

        for( var i=0; i<$scope.buyTxns.length; i++ ) {
            var txn = $scope.buyTxns[i] ;
            txn.selected = txn.parentHolding.symbolIcici == symbolIcici ;
        }    
        recomputeSelTotals() ;
    }
    
    // --- [END] Scope functions

    // -----------------------------------------------------------------------
    // --- [START] Local functions -------------------------------------------
    function recomputeSelTotals() {
        
        resetTotal( $scope.selTotal ) ;
        for( var i=0; i<$scope.buyTxns.length; i++ ) {
            var txn = $scope.buyTxns[i] ;
            if( txn.selected ) {
                $scope.selTotal.costPrice += txn.valueAtCost ;
            }
        }    
    }
    
    // ------------------- Server comm functions -----------------------------
    function fetchBuyDataFromServer() {
        
        $scope.$emit( 'interactingWithServer', { isStart : true } ) ;
        
        $http.get( '/Equity/Transactions/Buy' )
        .then ( 
            function( response ){
                console.log( response.data ) ;
                resetState() ;
                for( var i=0; i<response.data.length; i++ ) {
                    var txn = response.data[i] ;
                    
                    // Injecting new fields into txn
                    txn.selected = false ;
                    
                    $scope.buyTxns.push( txn ) ;
                    
                    $scope.allTotal.costPrice += txn.valueAtCost ;
                }
            }, 
            function(){
                $scope.$parent.addErrorAlert( "Error fetching MF portfolio." ) ;
            }
        )
        .finally(function() {
            $scope.$emit( 'interactingWithServer', { isStart : false } ) ;
        }) ;
    }
} ) ;