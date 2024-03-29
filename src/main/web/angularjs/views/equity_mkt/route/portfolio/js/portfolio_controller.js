capitalystNgApp.controller( 'PortfolioController', 
    function( $scope, $http ) {
    
    // ---------------- Local variables --------------------------------------
    var sortDir = {
        symbol : 'asc'
    } ;
    
    var curSparklineView = "Discrete" ;
    
    var trendScore = {
        'VERY BULLISH' : 5,
        'BULLISH'      : 4,
        'NEUTRAL'      : 3,
        'BEARISH'      : 2,
        'VERY BEARISH' : 1        
    } ;
    
    // ---------------- Scope variables --------------------------------------
    $scope.$parent.navBarTitle = "Equity Portfolio" ;
    $scope.$parent.activeModuleId = "portfolio" ;
    
    $scope.equityHoldings = [] ;
    
    $scope.allTotal = {
        valueAtCost : 0,
        mktValue : 0,
        pat : 0,
        sellValue : 0,
        patPct : 0,
        dayGain : 0,
        discreteSLData : [],
        cumulativeSLData : [],
        visibleSLData : null
    } ;
    
    $scope.selTotal = {
        valueAtCost : 0,
        mktValue : 0,
        pat : 0,
        sellValue : 0,
        patPct : 0,
        dayGain : 0,
        discreteSLData : [],
        cumulativeSLData : [],
        visibleSLData : null
    } ;
    
    $scope.holdingType = "Family" ;
    $scope.holdingForTxnsDisplay = null ;
    $scope.inbetweenServerCall = false ;
    $scope.marketOpen = true ;
    $scope.onMobile = window.mobileCheck() ;
    
    // -----------------------------------------------------------------------
    // --- [START] Controller initialization ---------------------------------
    console.log( "Loading EquityController" ) ;
    getMarketOpenStatus() ;
    fetchEquityHoldingsFromServer( true ) ;
    
    // --- [END] Controller initialization -----------------------------------
    
    // -----------------------------------------------------------------------
    // --- [START] Scope functions -------------------------------------------
    $scope.changeHoldingType = function( newType ) {
        
        $scope.holdingType = newType ;
        
        resetSelTotals() ;
        resetAllTotals() ;

        fetchEquityHoldingsFromServer( false ) ;
    }
    
    $scope.getAmtClass = function( value ) {
        return ( value < 0 ) ? "neg_amt" : "pos_amt" ;
    }

    $scope.getRowClass = function( holding ) {
        if( holding.selected ) {
            return "selected_row" ;
        }
        else if( holding.ltcgQualPct > 99 ) {
            return "full_ltcg" ;
        }
        return  "" ;
    }
    
    $scope.holdingSelectionChanged = function( holding ) {
        calculateTotals( true ) ;
        redrawSelSparkline() ;
    }
    
    $scope.selectAllHoldingsForOwner = function( ownerName ) {
        for( var i=0; i<$scope.equityHoldings.length; i++ ) {
            var holding = $scope.equityHoldings[i] ;
            holding.selected = ( holding.ownerName == ownerName ) ;
        }
        calculateTotals( true ) ;
        redrawSelSparkline() ;
    }
    
    $scope.selectAllHoldingsForSymbol = function( symbolIcici ) {
        for( var i=0; i<$scope.equityHoldings.length; i++ ) {
            var holding = $scope.equityHoldings[i] ;
            holding.selected = ( holding.symbolIcici == symbolIcici ) ;
        }
        calculateTotals( true ) ;
        redrawSelSparkline() ;
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
    
    $scope.showGraphDialog = function( holding ) {
        $scope.$emit( 'graphDialogDisplayTrigger', {
            symbolNse   : holding.symbolNse,
            companyName : holding.companyName,
            ownerName   : holding.ownerName 
        }) ;
    }
    
    $scope.toggleSparkline = function() {
        
        curSparklineView = ( curSparklineView == "Discrete" ) ? 
                           "Cumulative" : "Discrete" ;
        setVisibleSLData() ;
        paintSparklines() ;
    }
    
    $scope.sort = function( field ) {
        var dir = sortDir[ field ] ;
        var newDir = ( dir == "asc" ) ? "desc" : "asc" ;
        sortDir[ field ] = newDir ; 
        
        if( field == "ltcgPct" ) {
            $scope.equityHoldings.sort( ltcgPctSort ) ;
        }
        else if( field == "sellValue" ) {
            $scope.equityHoldings.sort( sellValueSort ) ;
        }
        else if( field == "trend" ) {
            $scope.equityHoldings.sort( trendSort ) ;
        }
        else if( field == "prevTrend" ) {
            $scope.equityHoldings.sort( prevTrendSort ) ;
        }
    }
    
    $scope.sortRows = function( colId, property, type ) {
        
        sortDir[colId] = ( sortDir[colId] == "asc" ) ? "desc" : "asc" ;
        sortArrayByProperty( sortDir[colId], $scope.equityHoldings, property, type ) ;
    }
    
    // --- [END] Scope functions

    // -----------------------------------------------------------------------
    // --- [START] Local functions -------------------------------------------
    
    function redrawSelSparkline() {
        $( '#spark_sel' ).sparkline( $scope.selTotal.visibleSLData, {
            type: 'bar',
            barColor : 'green',
            negBarColor : 'red' 
        } ) ;
    }
    
    function setVisibleSLData() {
        
        for( var i=0; i<$scope.equityHoldings.length; i++ ) {
            var eh = $scope.equityHoldings[i] ;
            
            if( curSparklineView == "Cumulative" ) {
                eh.visibleSparklineData = eh.cumulativeSparklineData ;
            }
            else {
                eh.visibleSparklineData = eh.sparklineData ;
            }
        }
        
        $scope.allTotal.visibleSLData = ( curSparklineView == "Discrete" ) ?
                                        $scope.allTotal.discreteSLData :
                                        $scope.allTotal.cumulativeSLData ;
        
        $scope.selTotal.visibleSLData = ( curSparklineView == "Discrete" ) ?
                                        $scope.selTotal.discreteSLData :
                                        $scope.selTotal.cumulativeSLData ;
    }
    
    function resetState() {
        
        $scope.holdingForTxnsDisplay = null ;
        
        resetAllTotals() ;
        resetSelTotals() ;
    }
    
    function resetSelTotals() {
        
        $scope.selTotal.valueAtCost             = 0 ;
        $scope.selTotal.mktValue                = 0 ;
        $scope.selTotal.pat                     = 0 ;
        $scope.selTotal.sellValue               = 0 ;
        $scope.selTotal.patPct                  = 0 ;
        $scope.selTotal.dayGain                 = 0 ;
        $scope.selTotal.discreteSLData.length   = 0 ;
        $scope.selTotal.cumulativeSLData.length = 0 ;
    }
    
    function resetAllTotals() {
        
        $scope.allTotal.valueAtCost             = 0 ;
        $scope.allTotal.mktValue                = 0 ;
        $scope.allTotal.pat                     = 0 ;
        $scope.allTotal.sellValue               = 0 ;
        $scope.allTotal.patPct                  = 0 ;
        $scope.allTotal.dayGain                 = 0 ;
        $scope.allTotal.discreteSLData.length   = 0 ;
        $scope.allTotal.cumulativeSLData.length = 0 ;
    }
    
    function calculateTotals( selected ) {
        
        if( selected ) {
            resetSelTotals() ;
        }
        
        var total = selected ? $scope.selTotal : $scope.allTotal ;
        
        for( var i=0; i<$scope.equityHoldings.length; i++ ) {
            
            var holding = $scope.equityHoldings[i] ;
            
            if( selected && !holding.selected ) {
                continue ;
            }
            
            total.valueAtCost += holding.valueAtCost ;
            total.mktValue    += holding.valueAtMktPrice ;
            total.pat         += holding.pat ;
            total.dayGain     += holding.dayGain ;
            
            for( var j=0; j<holding.sparklineData.length; j++ ) {
                var slData = holding.sparklineData[j] ;
                
                if( total.discreteSLData.length > j ) {
                    total.discreteSLData[j] += slData ;
                }
                else {
                    total.discreteSLData.push( slData ) ;
                }
            }
        }
        
        total.sellValue = total.mktValue + total.pat ;
        total.cumulativeSLData = getCumulativeSLData( total.discreteSLData ) ;
        
        if( total.valueAtCost == 0 ) {
            total.patPct = 0 ;
        }
        else {
            total.patPct = ( total.pat / total.valueAtCost ) * 100 ;
        }
        
        if( selected ) {
            $scope.selTotal.visibleSLData = ( curSparklineView == "Discrete" ) ?
                                            $scope.selTotal.discreteSLData :
                                            $scope.selTotal.cumulativeSLData ;
        }
    }
    
    // ------------------- Server comm functions -----------------------------
    function getMarketOpenStatus() {
        
        $http.get( '/Breeze/MarketStatus' )
        .then ( 
            function( response ){
                console.log( response.data ) ;
                $scope.marketOpen = response.data.mktOpen ;
            }
        )
    }
    
    function fetchEquityHoldingsFromServer( scheduleNextFetch ) {
        
        $http.get( '/Equity/' + $scope.holdingType + 'Holding' )
        .then ( 
            function( response ){
                
                console.log( response.data ) ;
                
                // Save any old state that we want to resurrect post response
                var oldSparklineView = curSparklineView ; 
                
                resetState() ;
                
                // Reinstate any old state
                curSparklineView = oldSparklineView ; 
                
                if( isFullRefreshRequired( response ) ) {
                    doFullRefresh( response ) ;
                    setTimeout( paintSparklines, 100 ) ;
                }
                else {
                    doDeltaRefresh( response ) ;
                }
                
                calculateTotals( true  ) ;
                calculateTotals( false ) ;
                
                setVisibleSLData() ;
                
                if( scheduleNextFetch ) {
                    var delay = $scope.marketOpen ? 5 : 3000 ; 
                    setTimeout( function(){
                        fetchEquityHoldingsFromServer( scheduleNextFetch )
                    }, delay*1000 ) ;
                    getMarketOpenStatus() ;
                }
            }
        )
    }
    
    function isFullRefreshRequired( response ) {
        
        var fullRefreshRequired = true ;
        
        if( response.data.length != 0 ) {
            if( $scope.equityHoldings.length == response.data.length ) {
                var sampleLocalHolding  = $scope.equityHoldings[0] ;
                var sampleServerHolding = response.data[0] ;
                
                if( sampleLocalHolding.holdingType == 
                    sampleServerHolding.holdingType ) {
                
                    fullRefreshRequired = false ;                    
                }                       
            }
        }
        return fullRefreshRequired ;  
    }
    
    function doFullRefresh( response ) {
        
        $scope.equityHoldings.length = 0 ;
        for( var i=0; i<response.data.length; i++ ) {
            
            var holding = response.data[i] ;
            
            // These are the extra attributes we are adding to the holding
            holding.selected = false ;
            holding.visible = holding.quantity > 0 ;
            holding.cumulativeSparklineData = getCumulativeSLData( holding.sparklineData ) ;
            holding.visibleSparklineData = holding.sparklineData ;
            holding.dayPctChange = calculateDayPctChange( holding ) ;
            
            $scope.equityHoldings.push( holding ) ;
        }
    }
    
    function doDeltaRefresh( response ) {
        
        for( var i=0; i<response.data.length; i++ ) {
            
            var serverHolding = response.data[i] ;
            var localHolding  = getHoldingWithUniqueId( serverHolding.uniqueId ) ;

            localHolding.currentMktPrice = serverHolding.currentMktPrice ;
            localHolding.valueAtMktPrice = serverHolding.valueAtMktPrice ;
            localHolding.pat             = serverHolding.pat ;            
            localHolding.patPct          = serverHolding.patPct ;            
            localHolding.dayGain         = serverHolding.dayGain ;            
            localHolding.lastUpdate      = serverHolding.lastUpdate ;
            localHolding.dayPctChange    = calculateDayPctChange( serverHolding ) ;
        }
    }
    
    function calculateDayPctChange( holding ) {
        
        var changePerUnit = holding.dayGain / holding.quantity ;
        var lastClosing   = holding.currentMktPrice - changePerUnit ;
        
        return ( changePerUnit / lastClosing )*100 ;
    }
    
    function getHoldingWithUniqueId( uid ) {
        
        for( var i=0; i<$scope.equityHoldings.length; i++ ) {
            
            var holding = $scope.equityHoldings[i] ;
            if( holding.uniqueId == uid ) {
                return holding ;
            }
        }
        return null ;
    }
    
    function getCumulativeSLData( discreteSLdata ) {
        
        var cumData = [] ;
        var lastData = 0 ;
        var curData = 0 ;
        
        for( var i=discreteSLdata.length-1; i>=0; i-- ) {
            curData = lastData + discreteSLdata[i] ;
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
        
        $( '#spark_tot' ).sparkline( $scope.allTotal.visibleSLData, {
            type: 'bar',
            barColor : 'green',
            negBarColor : 'red' 
        } ) ;
        
        $( '#spark_sel' ).sparkline( $scope.selTotal.visibleSLData, {
            type: 'bar',
            barColor : 'green',
            negBarColor : 'red' 
        } ) ;
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
    
    // ---------------------- Sorting functions --------------------------------

    function ltcgPctSort( h1, h2 ) {
        
        let h1Pct = (h1.ltcgQty/h1.quantity)*100 ;
        let h2Pct = (h2.ltcgQty/h2.quantity)*100 ;
        
        return sortDir[ "ltcgPct" ] == "asc" ? 
               (h1Pct - h2Pct) : (h2Pct - h1Pct) ;
    }
    
    function sellValueSort( h1, h2 ) {
        
        let h1SellVal = h1.valueAtCost + h1.pat ;
        let h2SellVal = h2.valueAtCost + h2.pat ;
        
        return sortDir[ "sellValue" ] == "asc" ?
               (h1SellVal - h2SellVal) : (h2SellVal - h1SellVal) ;
    }
    
    function trendSort( h1, h2 ) {
        return sortDir[ "trend"] == "asc" ?
            ( trendScore[h1.indicators.trend] - trendScore[h2.indicators.trend] ) :
            ( trendScore[h2.indicators.trend] - trendScore[h1.indicators.trend] ) ;
    }
        
    function prevTrendSort( h1, h2 ) {
        return sortDir[ "prevTrend"] == "asc" ?
            ( trendScore[h1.indicators.prevTrend] - trendScore[h2.indicators.prevTrend] ) :
            ( trendScore[h2.indicators.prevTrend] - trendScore[h1.indicators.prevTrend] ) ;
    }
        
} ) ;