capitalystNgApp.controller( 'RecoController', 
    function( $scope, $http, $ngConfirm ) {
    
    // ---------------- Local variables --------------------------------------
    var sortDir = {
    } ;
    
    var gradientMgr = {
       health  : new Gradient( new UniformGradient() ),
       mc      : new Gradient( new UniformGradient() ),
       
       rsi     : new Gradient( new UniformGradient( GR_GRADIENT ) ),
       
       perfLTP : new Gradient( new ThresholdGradient() ),
       perf1D  : new Gradient( new ThresholdGradient() ), 
       perf1W  : new Gradient( new ThresholdGradient() ), 
       perf2W  : new Gradient( new ThresholdGradient() ), 
       perf1M  : new Gradient( new ThresholdGradient() ), 
       perf2M  : new Gradient( new ThresholdGradient() ), 
       perf3M  : new Gradient( new ThresholdGradient() ), 
       perf6M  : new Gradient( new ThresholdGradient() ), 
       perf9M  : new Gradient( new ThresholdGradient() ), 
       perf1Y  : new Gradient( new ThresholdGradient() ), 
       perf3Y  : new Gradient( new ThresholdGradient() ),
       
       cagr    : new Gradient( new ThresholdGradient( 5 ) ),
    } ;
    
    var trendScore = {
        'VERY BULLISH' : 5,
        'BULLISH'      : 4,
        'NEUTRAL'      : 3,
        'BEARISH'      : 2,
        'VERY BEARISH' : 1        
    } ;
    
    // ---------------- Scope variables --------------------------------------
    $scope.$parent.navBarTitle = "Equity Recommendations" ;
    $scope.$parent.activeModuleId = "reco" ;
    
    $scope.recommendations = [] ;
    $scope.selectedStock = null ;
    
    $scope.ttmRefreshTriggered = false ;
    
    $scope.filter = {
        symbolFilter : "",
        sectorFilter : ""
    } ;
    
    function resetState() {
        $scope.recommendations.length = 0 ;
    } ;
    
    // -----------------------------------------------------------------------
    // --- [START] Controller initialization ---------------------------------
    console.log( "Loading BuyController" ) ;
    fetchRecoDataFromServer() ;
    
    // --- [END] Controller initialization -----------------------------------
    
    // -----------------------------------------------------------------------
    // --- [START] Scope functions -------------------------------------------
    $scope.getAmtClass = function( value ) {
        return ( value < 0 ) ? "neg_amt" : "pos_amt" ;
    }
    
    $scope.getCellClass = function( attrName, val ) {
        return gradientMgr[ attrName ].getColor( val ) ;
    }
    
    $scope.sort = function( field ) {
        var dir = sortDir[ field ] ;
        var newDir = ( dir == "asc" ) ? "desc" : "asc" ;
        sortDir[ field ] = newDir ; 
        
        if( field == "holding" ) {
            $scope.recommendations.sort( holdingSort ) ;
        }
        else if( field == "trend" ) {
            $scope.recommendations.sort( trendSort ) ;
        }    
        else if( field == "rsi" ) {
            $scope.recommendations.sort( rsiSort ) ;
        }    
    }
    
    $scope.sortRows = function( colId, property, type ) {
        
        sortDir[colId] = ( sortDir[colId] == "asc" ) ? "desc" : "asc" ;
        sortArrayByProperty( sortDir[colId], $scope.recommendations, property, type ) ;
    }
    
    $scope.showStockDetails = function( reco ) {
        $scope.selectedStock = reco.equityMaster ;
        $( '#stockDetailDialog' ).modal( 'show' ) ;
    }
    
    $scope.hideStockDetails = function() {
        $scope.selectedStock = null ;
        $( '#stockDetailDialog' ).modal( 'hide' ) ;
    }

    $scope.showGraphDialog = function( reco ) {
        $scope.$emit( 'graphDialogDisplayTrigger', {
            symbolNse   : reco.equityMaster.symbol,
            companyName : reco.equityMaster.name,
            ownerName   : 'Family' 
        }) ;
    }
    
    $scope.refreshLTP = function() {
        
        $scope.ttmRefreshTriggered = true ;
        $http.get( '/Equity/LTP' )
        .then( function( response ) {
            console.log( "Received latest LTPs" ) ;
            console.log( response.data ) ;
            
            gradientMgr['perfLTP'].clear() ;
            $scope.recommendations.forEach( reco => {
                var ltp = response.data[ reco.equityMaster.symbol ] ;
                if( ltp != null ) {
                    reco.ltp = ltp ;
                    reco.indicators.currentPrice = ltp.price ;
                    gradientMgr['perfLTP'].addValue( reco.ltp.pchange ) ;
                }    
            } ) ;
            console.log( "Reinitializing gradieng manager." ) ;
            gradientMgr['perfLTP'].initialize() ;
            
            sortDir[ "perfLTP" ] = "asc" ; 
            sortTable( "perfLTP" ) ;
        } )
        .finally(function() {
            $scope.ttmRefreshTriggered = false ;
        }) ;
    }
    
    $scope.toggleMonitor = function( reco ) {
        
        var symbol  = reco.equityMaster.symbol ;
        var content = reco.monitored ? 'Remove monitor from ' + symbol :
                                       'Add monitor to ' + symbol ; 
        
        $ngConfirm({
            title: 'Confirm ' + (reco.monitored ? 'remove' : 'add'),
            content: content ,
            scope: $scope,
            buttons: {
                close: function(scope, button){},
                yes: {
                    text: 'Yes',
                    btnClass: 'btn-blue',
                    action: function(scope, button){
                        if( reco.monitored ) {
                            $http.delete( '/Equity/Monitor/' + reco.equityMaster.isin )
                            .finally(function() { reco.monitored = false ; }) ;
                        }
                        else {
                            $http.post( '/Equity/Monitor/' + reco.equityMaster.isin )
                            .finally(function() { reco.monitored = true ; }) ;
                        }
                        return true ;
                    }
                }
            }
        });
    }
    
    $scope.downloadHistoricData = function( symbolNse ) {
        
        $http.get( '/Equity/HistoricData/' + symbolNse )
        .then(
            function( response ) {
                const filename = symbolNse + "-EOD.csv" ;
                const a = document.createElement( "a" ) ;
                
                a.href = "data:text/csv," + response.data;
                a.setAttribute( "download", filename ) ;
                
                document.body.appendChild(a);
                a.click();
                document.body.removeChild(a);
            }
        )
    }
    
    $scope.filterChanged = function() {
        
        var symFilter = $scope.filter.symbolFilter.trim().toLowerCase() ;
        var secFilter = $scope.filter.sectorFilter.trim().toLowerCase() ;
        
        for( var i=0; i<$scope.recommendations.length; i++ ) {
            
            var reco = $scope.recommendations[i] ;
            
            if( symFilter == "" && secFilter == "" ) {
                reco.visible = true ;
            }
            else {
                if( symFilter != "" ) {
                    
                    if( symFilter.startsWith('*') ) {
                        var filter = symFilter.substring( 1 ) ;
                        reco.visible = reco.equityMaster
                                           .symbol
                                           .toLowerCase()
                                           .includes( filter ) ;
                    }
                    else {
                        reco.visible = reco.equityMaster
                                           .symbol
                                           .toLowerCase()
                                           .startsWith( symFilter ) ;
                    } 
                }                
    
                if( secFilter != "" ) {
                    if( secFilter.startsWith('*') ) {
                        var filter = secFilter.substring( 1 ) ;
                        reco.visible = reco.equityMaster
                                           .sector
                                           .toLowerCase()
                                           .includes( filter ) ;
                    }
                    else {
                        reco.visible = reco.equityMaster
                                           .sector
                                           .toLowerCase()
                                           .startsWith( secFilter ) ;
                    } 
                }                
            }
        }
    }
    
    $scope.filterBySector = function( sector ) {
        
        $scope.filter.sectorFilter = sector ;
        $scope.filterChanged() ;
    }
    
    $scope.clearSymbolFilter = function() {
        
        $scope.filter.symbolFilter = "" ;
        $scope.filterChanged() ;
    }
    
    $scope.clearSectorFilter = function() {
        
        $scope.filter.sectorFilter = "" ;
        $scope.filterChanged() ;
    }
    
    // --- [END] Scope functions

    // -----------------------------------------------------------------------
    // --- [START] Local functions -------------------------------------------
    
    // ------------------- Server comm functions -----------------------------
    function fetchRecoDataFromServer() {
        
        $scope.$emit( 'interactingWithServer', { isStart : true } ) ;
        
        $http.get( '/Equity/Recommendations' )
        .then ( 
            function( response ){
                console.log( response.data ) ;
                resetState() ;
                for( var i=0; i<response.data.length; i++ ) {
                    
                    var reco = response.data[i] ;
                    
                    // Custom properties injection
                    reco.visible = true ;

                    $scope.recommendations.push( reco ) ;
                    
                    if( reco.ltp == null ) {
                        reco.ltp = {
                            pchange : reco.ttmPerf.perf1d,
                            price : reco.indicators.currentPrice
                        }
                    }
                    
                    gradientMgr['health'].addValue( reco.goodnessScore ) ;
                    gradientMgr['mc'    ].addValue( reco.indicators.mcEssentialScore ) ;
                    gradientMgr['cagr'  ].addValue( reco.indicators.cagrEbit ) ;
                    
                    gradientMgr['perfLTP'].addValue( reco.ltp.pchange ) ;
                    gradientMgr['perf1D' ].addValue( reco.ttmPerf.perf1d  ) ;
                    gradientMgr['perf1W' ].addValue( reco.ttmPerf.perf1w  ) ;
                    gradientMgr['perf2W' ].addValue( reco.ttmPerf.perf2w  ) ;
                    gradientMgr['perf1M' ].addValue( reco.ttmPerf.perf1m  ) ;
                    gradientMgr['perf2M' ].addValue( reco.ttmPerf.perf2m  ) ;
                    gradientMgr['perf3M' ].addValue( reco.ttmPerf.perf3m  ) ;
                    gradientMgr['perf6M' ].addValue( reco.ttmPerf.perf6m  ) ;
                    gradientMgr['perf9M' ].addValue( reco.ttmPerf.perf9m  ) ;
                    gradientMgr['perf1Y' ].addValue( reco.ttmPerf.perf12m  ) ;
                    gradientMgr['perf3Y' ].addValue( reco.indicators.pricePerf3Y  ) ;

                    if( reco.techIndicators.length > 1 ) {
                        gradientMgr['rsi'].addValue( reco.techIndicators[0].level ) ;
                    }                    
                }
                
                initializeGradientMgrs() ;
                
                sortDir[ "perfLTP" ] = "asc" ; 
                $scope.sortRows( "perfLTP", "ltp.pchange", "num" ) ;
            }, 
            function(){
                $scope.$parent.addErrorAlert( "Error fetching eq recos." ) ;
            }
        )
        .finally(function() {
            $scope.$emit( 'interactingWithServer', { isStart : false } ) ;
        }) ;
    }
    
    function makeAllRowsVisible() {
        
        for( var i=0; i<$scope.recommendations.length; i++ ) {
            reco.visible = true ;
        }
    }
    
    function initializeGradientMgrs() {
        for( var key in gradientMgr ) {
            if( gradientMgr.hasOwnProperty( key ) ) {
                gradientMgr[ key ].initialize() ;
            }
        }
    }
    
    function holdingSort( r1, r2 ) {
        var r1Holding = r1.inPortfolio ? 2 : 0 ;
        var r2Holding = r2.inPortfolio ? 2 : 0 ;
        
        var r1Monitored = r1.monitored ? 1 : 0 ;
        var r2Monitored = r2.monitored ? 1 : 0 ;
        
        var r1Score = r1Holding + r1Monitored ;
        var r2Score = r2Holding + r2Monitored ;
        
        return sortDir[ "holding"] == "asc" ?
            ( r2Score - r1Score ) :
            ( r1Score - r2Score ) ;
    }
    
    function trendSort( r1, r2 ) {
        
        return sortDir[ "trend"] == "asc" ?
            ( trendScore[r1.indicators.trend] - trendScore[r2.indicators.trend] ) :
            ( trendScore[r2.indicators.trend] - trendScore[r1.indicators.trend] ) ;
    }
        
    function rsiSort( r1, r2 ) {
        return sortDir["rsi"] == "asc" ?
            ( r1.techIndicators[0].level - r2.techIndicators[0].level ) :
            ( r2.techIndicators[0].level - r1.techIndicators[0].level ) ;
    }
} ) ;