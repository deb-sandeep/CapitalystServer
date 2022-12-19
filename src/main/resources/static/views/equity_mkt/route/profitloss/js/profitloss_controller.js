capitalystNgApp.controller( 'ProfitLossController', 
    function( $scope, $http ) {
    
    // ---------------- Local variables --------------------------------------
    var sortDir = {
        symbol : 'asc'
    } ;
    
    // ---------------- Scope variables --------------------------------------
    $scope.$parent.navBarTitle = "Profit Loss from Equity (this FY)" ;
    $scope.$parent.activeModuleId = "profitloss" ;
    
    $scope.sellTxns = [] ;
    
    $scope.allTotal = {
        costPrice : 0,
        sellPrice : 0,
        sellTxnCharges : 0,
        amountRecd : 0,
        taxAmount : 0,
        pat : 0,
        patPct : 0
    } ;
    
    $scope.selTotal = {
        costPrice : 0,
        sellPrice : 0,
        sellTxnCharges : 0,
        amountRecd : 0,
        taxAmount : 0,
        pat : 0,
        patPct : 0
    } ;
    
    function resetState() {
        $scope.sellTxns.length = 0 ;
        resetTotal( $scope.allTotal ) ;
        resetTotal( $scope.selTotal ) ;
    } ;
    
    function resetTotal( total ) {
        total.costPrice      = 0 ;
        total.sellPrice      = 0 ;
        total.sellTxnCharges = 0 ;
        total.amountRecd     = 0 ;
        total.taxAmount      = 0 ;
        total.pat            = 0 ;
        total.patPct         = 0 ;
    }
    
    // -----------------------------------------------------------------------
    // --- [START] Controller initialization ---------------------------------
    console.log( "Loading ProfitLossController" ) ;
    fetchProfitLossDataFromServer() ;
    
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
        
        for( var i=0; i<$scope.sellTxns.length; i++ ) {
            var txn = $scope.sellTxns[i] ;
            txn.selected = txn.parentHolding.ownerName == ownerName ;
        }    
        recomputeSelTotals() ;
    }
    
    $scope.selectAllHoldingsForSymbol = function( symbolIcici ) {

        for( var i=0; i<$scope.sellTxns.length; i++ ) {
            var txn = $scope.sellTxns[i] ;
            txn.selected = txn.parentHolding.symbolIcici == symbolIcici ;
        }    
        recomputeSelTotals() ;
    }
    
    $scope.clearSelections = function() {
        
        for( var i=0; i<$scope.sellTxns.length; i++ ) {
            $scope.sellTxns[i].selected = false ;
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
        sortArrayByProperty( sortDir[colId], $scope.sellTxns, property, type ) ;
    }
    
    // --- [END] Scope functions

    // -----------------------------------------------------------------------
    // --- [START] Local functions -------------------------------------------
    function recomputeSelTotals() {
        
        resetTotal( $scope.selTotal ) ;
        for( var i=0; i<$scope.sellTxns.length; i++ ) {
            var txn = $scope.sellTxns[i] ;
            if( txn.selected ) {
                
                $scope.selTotal.costPrice      += txn.valueAtCostPrice ;
                $scope.selTotal.sellPrice      += txn.valueAtMktPrice ;
                $scope.selTotal.sellTxnCharges += txn.sellTxnCharges ;
                $scope.selTotal.amountRecd     += txn.amountRecd ;
                $scope.selTotal.taxAmount      += txn.taxAmount ;
                $scope.selTotal.pat            += txn.pat ;
                
                if( $scope.selTotal.costPrice != 0 ) {
                    $scope.selTotal.patPct = ( $scope.selTotal.pat /
                                               $scope.selTotal.costPrice ) * 100 ;
                }
            }
        }    
    }
    
    // ------------------- Server comm functions -----------------------------
    function fetchProfitLossDataFromServer() {
        
        $scope.$emit( 'interactingWithServer', { isStart : true } ) ;
        
        $http.get( '/Equity/Transactions/Sell' )
        .then ( 
            function( response ){
                console.log( response.data ) ;
                resetState() ;
                for( var i=0; i<response.data.length; i++ ) {
                    var txn = response.data[i] ;
                    
                    // Injecting new fields into txn
                    txn.selected = false ;
                    txn.amountRecd = ( txn.valueAtMktPrice - txn.sellTxnCharges ) ;
                    
                    $scope.sellTxns.push( txn ) ;
                    
                    $scope.allTotal.costPrice      += txn.valueAtCostPrice ;
                    $scope.allTotal.sellPrice      += txn.valueAtMktPrice ;
                    $scope.allTotal.sellTxnCharges += txn.sellTxnCharges ;
                    $scope.allTotal.amountRecd     += txn.amountRecd ;
                    $scope.allTotal.taxAmount      += txn.taxAmount ;
                    $scope.allTotal.pat            += txn.pat ;
                    
                    if( $scope.allTotal.costPrice != 0 ) {
                        $scope.allTotal.patPct = ( $scope.allTotal.pat /
                                                   $scope.allTotal.costPrice ) * 100 ;
                    }
                }
            }, 
            function(){
                $scope.$parent.addErrorAlert( "Error fetching sell txns for current year." ) ;
            }
        )
        .finally(function() {
            $scope.$emit( 'interactingWithServer', { isStart : false } ) ;
        }) ;
    }
} ) ;