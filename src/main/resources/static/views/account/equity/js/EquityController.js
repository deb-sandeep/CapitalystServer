capitalystNgApp.controller( 'EquityController', 
    function( $scope, $http, $ngConfirm, $window ) {
    
    // ---------------- Local variables --------------------------------------
    
    // ---------------- Scope variables --------------------------------------
    $scope.$parent.navBarTitle = "Equity Holdings" ;
    $scope.equityHoldings = [] ;
    $scope.totalValueAtCost = 0 ;
    $scope.totalValueAtNav = 0 ;
    $scope.totalProfit = 0 ;
    $scope.totalProfitPct = 0 ;
    
    $scope.valueAtCostOfSelectedHoldings = 0 ;
    $scope.redemptionValueOfSelectedHoldings = 0 ;
    $scope.profitValueOfSelectedHoldings = 0 ;
    
    // -----------------------------------------------------------------------
    // --- [START] Controller initialization ---------------------------------
    console.log( "Loading EquityController" ) ;
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
    
    $scope.holdingSelectionChanged = function( holding ) {
        reCalculateSelectionTotals() ;
    }
    
    $scope.selectAllHoldingsForOwner = function( ownerName ) {
        for( var i=0; i<$scope.equityHoldings.length; i++ ) {
            var holding = $scope.equityHoldings[i] ;
            holding.selected = ( holding.ownerName == ownerName ) ;
        }
        reCalculateSelectionTotals() ;
    }
    
    $scope.selectAllHoldingsForSymbol = function( symbolIcici ) {
        for( var i=0; i<$scope.equityHoldings.length; i++ ) {
            var holding = $scope.equityHoldings[i] ;
            holding.selected = ( holding.symbolIcici == symbolIcici ) ;
        }
        reCalculateSelectionTotals() ;
    }
    
    $scope.triggerJob = function() {
        console.log( "Triggering job" ) ; 
        triggerJob( 'NSEBhavcopyRefreshJob' ) ;
    }
    
    // --- [END] Scope functions

    // -----------------------------------------------------------------------
    // --- [START] Local functions -------------------------------------------
    function initializeController() {
        $scope.$parent.activeTabKey = "EQ" ;
        fetchEquityHoldingsFromServer() ;
    }
    
    function reCalculateSelectionTotals() {
        $scope.valueAtCostOfSelectedHoldings = 0 ;
        $scope.redemptionValueOfSelectedHoldings = 0 ;
        $scope.profitValueOfSelectedHoldings = 0 ;
        
        for( var i=0; i<$scope.equityHoldings.length; i++ ) {
            var holding = $scope.equityHoldings[i] ;
            if( holding.selected ) {
                $scope.valueAtCostOfSelectedHoldings += holding.valueAtCost ;
                $scope.redemptionValueOfSelectedHoldings += holding.valueAtMktPrice ;
                $scope.profitValueOfSelectedHoldings += holding.profitPostTax ;
            }
        }
    }
    // ------------------- Server comm functions -----------------------------
    function fetchEquityHoldingsFromServer() {
        
        $scope.$emit( 'interactingWithServer', { isStart : true } ) ;
        $http.get( '/Equity/Holding' )
        .then ( 
            function( response ){
                console.log( response.data ) ;
                $scope.equityHoldings = response.data ;
                angular.forEach( $scope.equityHoldings, function( holding, key ){
                    $scope.totalValueAtCost += holding.valueAtCost ;
                    $scope.totalValueAtNav += holding.valueAtMktPrice ;
                    $scope.totalProfit += holding.profitPostTax ;
                    
                    // These are the extra attributes we are adding to the holding
                    holding.selected = false ;
                    holding.visible = holding.quantity > 0 ;
                }) ;
                $scope.totalProfitPct = Math.ceil( ( $scope.totalProfit / $scope.totalValueAtCost ) * 100 ) ;
            }, 
            function( error ){
                $scope.$parent.addErrorAlert( "Error fetching MF portfolio." ) ;
            }
        )
        .finally(function() {
            $scope.$emit( 'interactingWithServer', { isStart : false } ) ;
        }) ;
    }
    
    function triggerJob( jobName ) {
        
        $scope.$emit( 'interactingWithServer', { isStart : true } ) ;
        $http.post( '/Job/TriggerNow/' + jobName )
        .then ( 
            function( response ){
                alert( "Job triggered. Refresh after some time." ) ;
            }, 
            function( error ){
                $scope.$parent.addErrorAlert( "Error triggering job." ) ;
            }
        )
        .finally(function() {
            $scope.$emit( 'interactingWithServer', { isStart : false } ) ;
        }) ;
    }
    
} ) ;