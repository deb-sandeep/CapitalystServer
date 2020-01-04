capitalystNgApp.controller( 'MutualFundsController', 
    function( $scope, $http, $ngConfirm, $window ) {
    
    // ---------------- Local variables --------------------------------------
    
    // ---------------- Scope variables --------------------------------------
    $scope.$parent.navBarTitle = "Mutual Funds" ;
    $scope.mfHoldings = [] ;
    $scope.totalValueAtCost = 0 ;
    $scope.totalValueAtNav = 0 ;
    $scope.totalProfitLossAfterTax = 0 ;
    
    $scope.valueAtCostOfSelectedHoldings = 0 ;
    $scope.redemptionValueOfSelectedHoldings = 0 ;
    
    // -----------------------------------------------------------------------
    // --- [START] Controller initialization ---------------------------------
    console.log( "Loading MutualFundsController" ) ;
    initializeController() ;
    // --- [END] Controller initialization -----------------------------------
    
    
    // -----------------------------------------------------------------------
    // --- [START] Scope functions -------------------------------------------
    $scope.getAmtClass = function(value) {
        return ( value < 0 ) ? "neg_amt" : "pos_amt" ;
    }
    
    $scope.getRowClass = function( holding ) {
        if( holding.selected ) {
            return "selected_holding" ;
        }
        else if( holding.ltcgQualPct > 99 ) {
            return "full_ltcg" ;
        }
        return  "" ;
    }
    
    $scope.mfSelectionChanged = function( holding ) {
        $scope.valueAtCostOfSelectedHoldings = 0 ;
        $scope.redemptionValueOfSelectedHoldings = 0 ;
        for( var i=0; i<$scope.mfHoldings.length; i++ ) {
            var holding = $scope.mfHoldings[i] ;
            if( holding.selected ) {
                $scope.valueAtCostOfSelectedHoldings += holding.valueAtCost ;
                $scope.redemptionValueOfSelectedHoldings += holding.valueAtNavAfterTax ;
            }
        }
    }
    
    // --- [END] Scope functions

    // -----------------------------------------------------------------------
    // --- [START] Local functions -------------------------------------------
    
    function initializeController() {
        $scope.$parent.activeTabKey = "MF" ;
        fetchMutualFundsPortfolioFromServer() ;
    }
    
    // ------------------- Server comm functions -----------------------------
    function fetchMutualFundsPortfolioFromServer() {
        
        $scope.$emit( 'interactingWithServer', { isStart : true } ) ;
        $http.get( '/MutualFund/Portfolio' )
        .then ( 
            function( response ){
                $scope.mfHoldings = response.data ;
                angular.forEach( $scope.mfHoldings, function( holding, key ){
                    $scope.totalValueAtCost += holding.valueAtCost ;
                    $scope.totalValueAtNav += holding.valueAtNav ;
                    $scope.totalProfitLossAfterTax += holding.profitLossAmtAfterTax ;
                    
                    holding.selected = false ;
                    holding.ltcgQualPct = Math.round( holding.numUnitsQualifiedForLTCG*100 / 
                                                      holding.unitsHeld ) ;
                    holding.avgHoldingMonths = Math.round( holding.averageHoldingDays / 30 ) ;
                    holding.yearlyGrowthPct = Math.round( getYearlyGrowthPct( holding ) ) ;
                }) ;
            }, 
            function( error ){
                $scope.$parent.addErrorAlert( "Error fetching MF portfolio." ) ;
            }
        )
        .finally(function() {
            $scope.$emit( 'interactingWithServer', { isStart : false } ) ;
        }) ;
    }
    
    function getYearlyGrowthPct( holding ) {
        
        var currentToCostRatio = holding.valueAtNavAfterTax / holding.valueAtCost ;
        var nthRoot = Math.pow( currentToCostRatio, 12/holding.avgHoldingMonths ) ;
        return ( nthRoot - 1 ) * 100 ;
    }
} ) ;