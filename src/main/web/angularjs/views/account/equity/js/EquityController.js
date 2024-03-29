capitalystNgApp.controller( 'EquityController', 
    function( $scope, $http ) {
    
    // ---------------- Local variables --------------------------------------
    
    // ---------------- Scope variables --------------------------------------
    $scope.$parent.navBarTitle = "Equity Holdings" ;

    $scope.equityHoldings = [] ;
    $scope.totalValueAtCost = 0 ;
    $scope.totalValueAtNav = 0 ;
    $scope.totalPAT = 0 ;
    $scope.totalPATPct = 0 ;
    
    $scope.valueAtCostOfSelectedHoldings = 0 ;
    $scope.redemptionValueOfSelectedHoldings = 0 ;
    $scope.profitValueOfSelectedHoldings = 0 ;
    
    $scope.holdingType = "Family" ;
    
    // -----------------------------------------------------------------------
    // --- [START] Controller initialization ---------------------------------
    console.log( "Loading EquityController" ) ;
    $scope.$parent.activeTabKey = "EQ" ;
    fetchEquityHoldingsFromServer() ;
    // --- [END] Controller initialization -----------------------------------
    
    // -----------------------------------------------------------------------
    // --- [START] Scope functions -------------------------------------------
    $scope.changeHoldingType = function( newType ) {
        
        $scope.holdingType = newType ;
        $scope.totalValueAtCost = 0 ;
        $scope.totalValueAtNav = 0 ;
        $scope.totalPAT = 0 ;
        $scope.totalPATPct = 0 ;
        
        $scope.valueAtCostOfSelectedHoldings = 0 ;
        $scope.redemptionValueOfSelectedHoldings = 0 ;
        $scope.profitValueOfSelectedHoldings = 0 ;

        fetchEquityHoldingsFromServer() ;
    }
    
    $scope.getAmtClass = function( value ) {
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
        triggerJob( 'NSEBhavcopyRefreshJob' ) ;
    }
    
    // --- [END] Scope functions

    // -----------------------------------------------------------------------
    // --- [START] Local functions -------------------------------------------
    
    function reCalculateSelectionTotals() {
        
        $scope.valueAtCostOfSelectedHoldings = 0 ;
        $scope.redemptionValueOfSelectedHoldings = 0 ;
        $scope.profitValueOfSelectedHoldings = 0 ;
        
        for( var i=0; i<$scope.equityHoldings.length; i++ ) {
            var holding = $scope.equityHoldings[i] ;
            if( holding.selected ) {
                $scope.valueAtCostOfSelectedHoldings += holding.valueAtCost ;
                $scope.redemptionValueOfSelectedHoldings += holding.valueAtMktPrice ;
                $scope.profitValueOfSelectedHoldings += holding.pat ;
            }
        }
    }
    
    // ------------------- Server comm functions -----------------------------
    function fetchEquityHoldingsFromServer() {
        
        $scope.$emit( 'interactingWithServer', { isStart : true } ) ;
        
        $http.get( '/Equity/' + $scope.holdingType + 'Holding' )
        .then ( 
            function( response ){
                
                console.log( response.data ) ;
                
                $scope.equityHoldings.length = 0 ;
                
                for( var i=0; i<response.data.length; i++ ) {
                    
                    var holding = response.data[i] ;
                    
                    $scope.totalValueAtCost += holding.valueAtCost ;
                    $scope.totalValueAtNav += holding.valueAtMktPrice ;
                    $scope.totalPAT += holding.pat ;
                    
                    // These are the extra attributes we are adding to the holding
                    holding.selected = false ;
                    holding.visible = holding.quantity > 0 ;
                    
                    $scope.equityHoldings.push( holding ) ;
                }
                
                $scope.totalPATPct = Math.ceil( ( $scope.totalPAT / 
                                                  $scope.totalValueAtCost ) * 100 ) ;
            }, 
            function(){
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