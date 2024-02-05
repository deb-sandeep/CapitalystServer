capitalystNgApp.controller( 'BuyController', 
    function( $scope, $http ) {
    
    // ---------------- Local variables --------------------------------------
    var sortDir = {
    } ;
    
    
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
    
    $scope.showGraphDialog = function( txn ) {
        $scope.$emit( 'graphDialogDisplayTrigger', {
            symbolNse   : txn.parentHolding.symbolNse,
            companyName : txn.parentHolding.companyName,
            ownerName   : txn.parentHolding.ownerName
        }) ;
    }
    
    $scope.sortRows = function( colId, property, type ) {
        
        sortDir[colId] = ( sortDir[colId] == "asc" ) ? "desc" : "asc" ;
        sortArrayByProperty( sortDir[colId], $scope.buyTxns, property, type ) ;
    }
    
    $scope.$parent.operatingFYChanged = function() {
        console.log( "Operating FY changed : " + $scope.$parent.operatingFY.value ) ;
        fetchBuyDataFromServer() ;
    }
    
    $scope.selectNextOperatingYear = function() {
        if( $scope.$parent.operatingFY.nextChoice != null ) {
            $scope.$parent.operatingFY = $scope.$parent.operatingFY.nextChoice ;
            $scope.$parent.operatingFYChanged() ;
        }
    }
    
    $scope.selectPrevOperatingYear = function() {
        if( $scope.$parent.operatingFY.prevChoice != null ) {
            $scope.$parent.operatingFY = $scope.$parent.operatingFY.prevChoice ;
            $scope.$parent.operatingFYChanged() ;
        }
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
        
        $http.get( '/Equity/Transactions/Buy?fy=' + $scope.$parent.operatingFY.value )
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