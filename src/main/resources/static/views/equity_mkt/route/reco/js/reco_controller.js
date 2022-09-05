capitalystNgApp.controller( 'RecoController', 
    function( $scope, $http ) {
    
    // ---------------- Local variables --------------------------------------
    var sortDir = {
        health : 'asc'
    } ;
    
    var gradientMgr = {
       health  : new Gradient( new UniformGradient() ),
       mc      : new Gradient( new UniformGradient() ),
       
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
        sortTable( field ) ;
    }
    
    $scope.showStockDetails = function( reco ) {
        $scope.selectedStock = reco.equityMaster ;
        $( '#stockDetailDialog' ).modal( 'show' ) ;
    }
    
    $scope.hideStockDetails = function() {
        $scope.selectedStock = null ;
        $( '#stockDetailDialog' ).modal( 'hide' ) ;
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
                    $scope.recommendations.push( reco ) ;
                    
                    gradientMgr['health' ].addValue( reco.goodnessScore ) ;
                    gradientMgr['mc'     ].addValue( reco.indicators.mcEssentialScore ) ;
                    gradientMgr['cagr'   ].addValue( reco.indicators.cagrEbit ) ;
                    
                    gradientMgr['perf1W' ].addValue( reco.ttmPerf.perf1w  ) ;
                    gradientMgr['perf2W' ].addValue( reco.ttmPerf.perf2w  ) ;
                    gradientMgr['perf1M' ].addValue( reco.ttmPerf.perf1m  ) ;
                    gradientMgr['perf2M' ].addValue( reco.ttmPerf.perf2m  ) ;
                    gradientMgr['perf3M' ].addValue( reco.ttmPerf.perf3m  ) ;
                    gradientMgr['perf6M' ].addValue( reco.ttmPerf.perf6m  ) ;
                    gradientMgr['perf9M' ].addValue( reco.ttmPerf.perf9m  ) ;
                    gradientMgr['perf1Y' ].addValue( reco.ttmPerf.perf12m  ) ;
                    gradientMgr['perf3Y' ].addValue( reco.indicators.pricePerf3Y  ) ;
                }
                
                initializeGradientMgrs() ;
                
                sortTable( "health" ) ;
            }, 
            function(){
                $scope.$parent.addErrorAlert( "Error fetching eq recos." ) ;
            }
        )
        .finally(function() {
            $scope.$emit( 'interactingWithServer', { isStart : false } ) ;
        }) ;
    }
    
    function initializeGradientMgrs() {
        for( var key in gradientMgr ) {
            if( gradientMgr.hasOwnProperty( key ) ) {
                gradientMgr[ key ].initialize() ;
            }
        }
    }
    
    function sortTable( field ) {
        
        var dir = sortDir[ field ] ;
        var newDir = ( dir == "asc" ) ? "desc" : "asc" ;
        sortDir[ field ] = newDir ; 
        
        if( field == "health" ) {
            $scope.recommendations.sort( healthSort ) ;
        }
        else if( field == "symbol" ) {
            $scope.recommendations.sort( symbolSort ) ;
        }
        else if( field == "price" ) {
            $scope.recommendations.sort( priceSort ) ;
        }
        else if( field == "mktcap" ) {
            $scope.recommendations.sort( mktcapSort ) ;
        }
        else if( field == "beta" ) {
            $scope.recommendations.sort( betaSort ) ;
        }
        else if( field == "cagr" ) {
            $scope.recommendations.sort( cagrSort ) ;
        }
        else if( field == "mc" ) {
            $scope.recommendations.sort( mcSort ) ;
        }
        else if( field == "pe" ) {
            $scope.recommendations.sort( peSort ) ;
        }
        else if( field == "pscore" ) {
            $scope.recommendations.sort( pscoreSort ) ;
        }    
        else if( field == "trend" ) {
            $scope.recommendations.sort( trendSort ) ;
        }    
        else if( field == "perf1W" ) {
            $scope.recommendations.sort( perf1WSort ) ;
        }
        else if( field == "perf2W" ) {
            $scope.recommendations.sort( perf2WSort ) ;
        }
        else if( field == "perf1M" ) {
            $scope.recommendations.sort( perf1MSort ) ;
        }
        else if( field == "perf2M" ) {
            $scope.recommendations.sort( perf2MSort ) ;
        }
        else if( field == "perf3M" ) {
            $scope.recommendations.sort( perf3MSort ) ;
        }
        else if( field == "perf6M" ) {
            $scope.recommendations.sort( perf6MSort ) ;
        }
        else if( field == "perf9M" ) {
            $scope.recommendations.sort( perf9MSort ) ;
        }
        else if( field == "perf1Y" ) {
            $scope.recommendations.sort( perf1YSort ) ;
        }
        else if( field == "perf3Y" ) {
            $scope.recommendations.sort( perf3YSort ) ;
        }
    }
    
    function healthSort( r1, r2 ) {
        return sortDir[ "health"] == "asc" ?
            ( r1.goodnessScore - r2.goodnessScore ) :
            ( r2.goodnessScore - r1.goodnessScore ) ;
    }
    
    function symbolSort( r1, r2 ) {
        return sortDir[ "symbol"] == "asc" ?
            ( r1.symbolNse.localeCompare( r2.symbolNse ) ) :
            ( r2.symbolNse.localeCompare( r1.symbolNse ) ) ;
    }
    
    function priceSort( r1, r2 ) {
        return sortDir[ "price"] == "asc" ?
            ( r1.indicators.currentPrice - r2.indicators.currentPrice ) :
            ( r2.indicators.currentPrice - r1.indicators.currentPrice ) ;
    }
    
    function mktcapSort( r1, r2 ) {
        return sortDir[ "mktcap"] == "asc" ?
            ( r1.indicators.marketCap - r2.indicators.marketCap ) :
            ( r2.indicators.marketCap - r1.indicators.marketCap ) ;
    }
    
    function betaSort( r1, r2 ) {
        return sortDir[ "beta"] == "asc" ?
            ( r1.indicators.beta - r2.indicators.beta ) :
            ( r2.indicators.beta - r1.indicators.beta ) ;
    }
    
    function cagrSort( r1, r2 ) {
        return sortDir[ "cagr"] == "asc" ?
            ( r1.indicators.cagrEbit - r2.indicators.cagrEbit ) :
            ( r2.indicators.cagrEbit - r1.indicators.cagrEbit ) ;
    }
    
    function mcSort( r1, r2 ) {
        return sortDir[ "mc"] == "asc" ?
            ( r1.indicators.mcEssentialScore - r2.indicators.mcEssentialScore ) :
            ( r2.indicators.mcEssentialScore - r1.indicators.mcEssentialScore ) ;
    }
    
    function peSort( r1, r2 ) {
        return sortDir[ "pe"] == "asc" ?
            ( r1.indicators.pe - r2.indicators.pe ) :
            ( r2.indicators.pe - r1.indicators.pe ) ;
    }
    
    function pscoreSort( r1, r2 ) {
        return sortDir[ "pscore"] == "asc" ?
            ( r1.indicators.piotroskiScore - r2.indicators.piotroskiScore ) :
            ( r2.indicators.piotroskiScore - r1.indicators.piotroskiScore ) ;
    }
        
    function trendSort( r1, r2 ) {
        
        return sortDir[ "trend"] == "asc" ?
            ( trendScore[r1.indicators.trend] - trendScore[r2.indicators.trend] ) :
            ( trendScore[r2.indicators.trend] - trendScore[r1.indicators.trend] ) ;
    }
        
    function perf1WSort( r1, r2 ) {
        return sortDir["perf1W"] == "asc" ?
            ( r1.ttmPerf.perf1w - r2.ttmPerf.perf1w ) :
            ( r2.ttmPerf.perf1w - r1.ttmPerf.perf1w ) ;
    }
    
    function perf2WSort( r1, r2 ) {
        return sortDir["perf2W"] == "asc" ?
            ( r1.ttmPerf.perf2w - r2.ttmPerf.perf2w ) :
            ( r2.ttmPerf.perf2w - r1.ttmPerf.perf2w ) ;
    }
    
    function perf1MSort( r1, r2 ) {
        return sortDir["perf1M"] == "asc" ?
            ( r1.ttmPerf.perf1m - r2.ttmPerf.perf1m ) :
            ( r2.ttmPerf.perf1m - r1.ttmPerf.perf1m ) ;
    }
    
    function perf2MSort( r1, r2 ) {
        return sortDir["perf2M"] == "asc" ?
            ( r1.ttmPerf.perf2m - r2.ttmPerf.perf2m ) :
            ( r2.ttmPerf.perf2m - r1.ttmPerf.perf2m ) ;
    }
    
    function perf3MSort( r1, r2 ) {
        return sortDir["perf3M"] == "asc" ?
            ( r1.ttmPerf.perf3m - r2.ttmPerf.perf3m ) :
            ( r2.ttmPerf.perf3m - r1.ttmPerf.perf3m ) ;
    }
    
    function perf6MSort( r1, r2 ) {
        return sortDir["perf6M"] == "asc" ?
            ( r1.ttmPerf.perf6m - r2.ttmPerf.perf6m ) :
            ( r2.ttmPerf.perf6m - r1.ttmPerf.perf6m ) ;
    }
    
    function perf9MSort( r1, r2 ) {
        return sortDir["perf9M"] == "asc" ?
            ( r1.ttmPerf.perf9m - r2.ttmPerf.perf9m ) :
            ( r2.ttmPerf.perf9m - r1.ttmPerf.perf9m ) ;
    }
    
    function perf1YSort( r1, r2 ) {
        return sortDir["perf1Y"] == "asc" ?
            ( r1.ttmPerf.perf12m - r2.ttmPerf.perf12m ) :
            ( r2.ttmPerf.perf12m - r1.ttmPerf.perf12m ) ;
    }
    
    function perf3YSort( r1, r2 ) {
        return sortDir["perf3Y"] == "asc" ?
            ( r1.indicators.pricePerf3Y - r2.indicators.pricePerf3Y ) :
            ( r2.indicators.pricePerf3Y - r1.indicators.pricePerf3Y ) ;
    }
} ) ;