capitalystNgApp.controller( 'PortfolioController', 
    function( $scope, $http ) {
    
    // ---------------- Local variables --------------------------------------
    var curSparklineView = "Discrete" ;
    
    // ---------------- Scope variables --------------------------------------
    $scope.$parent.navBarTitle = "Equity Portfolio" ;
    $scope.$parent.activeModuleId = "portfolio" ;
    
    $scope.equityHoldings = [] ;
    $scope.totalValueAtCost = 0 ;
    $scope.totalValueAtNav = 0 ;
    $scope.totalPAT = 0 ;
    $scope.totalDayGain = 0 ;
    $scope.totalPATPct = 0 ;
    
    $scope.valueAtCostOfSelectedHoldings = 0 ;
    $scope.redemptionValueOfSelectedHoldings = 0 ;
    $scope.profitValueOfSelectedHoldings = 0 ;
    $scope.dayGainOfSelectedHoldings = 0 ;
    
    $scope.holdingType = "Family" ;
    $scope.holdingForTxnsDisplay = null ;
    
    // -----------------------------------------------------------------------
    // --- [START] Controller initialization ---------------------------------
    console.log( "Loading EquityController" ) ;
    fetchEquityHoldingsFromServer() ;
    // --- [END] Controller initialization -----------------------------------
    
    // -----------------------------------------------------------------------
    // --- [START] Scope functions -------------------------------------------
    $scope.changeHoldingType = function( newType ) {
        
        $scope.holdingType = newType ;
        $scope.totalValueAtCost = 0 ;
        $scope.totalValueAtNav = 0 ;
        $scope.totalPAT = 0 ;
        $scope.totalDayGain = 0 ;
        $scope.totalPATPct = 0 ;
        
        $scope.valueAtCostOfSelectedHoldings = 0 ;
        $scope.redemptionValueOfSelectedHoldings = 0 ;
        $scope.profitValueOfSelectedHoldings = 0 ;
        $scope.dayGainOfSelectedHoldings = 0 ;

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
    
    $scope.showTransactionsDialog = function( holding ) {
        $scope.$broadcast( 'holdingTxnsDisplayTrigger', {
            holding : holding,
            holdingType : $scope.holdingType
        }) ;
        $( '#txnsDisplayDialog' ).modal( 'show' ) ;
    }
    
    $scope.toggleSparkline = function() {
        
        curSparklineView = ( curSparklineView == "Discrete" ) ? 
                           "Cumulative" : "Discrete" ;

        for( var i=0; i<$scope.equityHoldings.length; i++ ) {
            var eh = $scope.equityHoldings[i] ;
            
            if( curSparklineView == "Cumulative" ) {
                eh.visibleSparklineData = eh.cumulativeSparklineData ;
            }
            else {
                eh.visibleSparklineData = eh.sparklineData ;
            }
        }
        paintSparklines() ;
    }
    
    // --- [END] Scope functions

    // -----------------------------------------------------------------------
    // --- [START] Local functions -------------------------------------------
    
    function reCalculateSelectionTotals() {
        
        $scope.valueAtCostOfSelectedHoldings = 0 ;
        $scope.redemptionValueOfSelectedHoldings = 0 ;
        $scope.profitValueOfSelectedHoldings = 0 ;
        $scope.dayGainOfSelectedHoldings = 0 ;
        
        for( var i=0; i<$scope.equityHoldings.length; i++ ) {
            var holding = $scope.equityHoldings[i] ;
            if( holding.selected ) {
                $scope.valueAtCostOfSelectedHoldings += holding.valueAtCost ;
                $scope.redemptionValueOfSelectedHoldings += holding.valueAtMktPrice ;
                $scope.profitValueOfSelectedHoldings += holding.pat ;
                $scope.dayGainOfSelectedHoldings += holding.dayGain ;
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
                    $scope.totalDayGain += holding.dayGain ;
                    
                    // These are the extra attributes we are adding to the holding
                    holding.selected = false ;
                    holding.visible = holding.quantity > 0 ;
                    holding.cumulativeSparklineData = getCumulativeSparklineData( holding ) ;
                    holding.visibleSparklineData = holding.sparklineData ;
                    
                    $scope.equityHoldings.push( holding ) ;
                }
                
                $scope.totalPATPct = Math.ceil( ( $scope.totalPAT / 
                                                  $scope.totalValueAtCost ) * 100 ) ;
                                                  
                setTimeout( paintSparklines, 100 ) ;
            }, 
            function(){
                $scope.$parent.addErrorAlert( "Error fetching MF portfolio." ) ;
            }
        )
        .finally(function() {
            $scope.$emit( 'interactingWithServer', { isStart : false } ) ;
        }) ;
    }
    
    function getCumulativeSparklineData( holding ) {
        
        var cumData = [] ;
        var lastData = 0 ;
        var curData = 0 ;
        
        for( var i=holding.sparklineData.length-1; i>=0; i-- ) {
            curData = lastData + holding.sparklineData[i] ;
            cumData.push( curData ) ;
            lastData = curData ; 
        }
        
        cumData.reverse() ;
        
        return cumData ;
    }
    
    function paintSparklines() {
        
        for( var i=0; i<$scope.equityHoldings.length; i++ ) {
            var eh = $scope.equityHoldings[i] ;
            $( '#spark_' + i ).sparkline( eh.visibleSparklineData, {
                type: 'bar',
                barColor : 'green',
                negBarColor : 'red' 
            } ) ;
        }
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