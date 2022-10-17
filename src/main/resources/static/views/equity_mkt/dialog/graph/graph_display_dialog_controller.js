capitalystNgApp.controller( 'GraphDisplayDialogController', 
                            function( $scope, $http ) {
    
    const BUY_COLOR            = '#256BEF' ;
    const SELL_COLOR           = '#E7871C' ;
    const EOD_LINE_COLOR       = '#B5B7B5' ;
    const EOD_LINE_COLOR_GREEN = '#CAE5CD' ;
    const EOD_LINE_COLOR_RED   = '#FFDDD4' ;
    const AVG_LINE_COLOR       = '#ABABAB' ;
    const SCATTER_POINT_RADIUS = 5 ;
    const MIN_RADIUS           = 2 ;
    const MAX_RADIUS           = 7 ;
    const RADIUS_RANGE         = MAX_RADIUS - MIN_RADIUS ;
    
    // ---------------- Local variables --------------------------------------
    var chart = null ;
    var minQuantity = 999999 ;
    var maxQuantity = 0 ;
    var quantityRange = 0 ;
    
    // ---------------- Scope variables --------------------------------------
    $scope.chartData = null ;
    $scope.graphParams = null ;
    $scope.durationKeys = [ '5y', '3y', '2y', '1y', '6m', '3m', '2m', '1m' ] ;
    $scope.duration = '3m' ;
    $scope.maGraphs = {
       d5 : {
          window : 5,
          smaEnabled : true,
          emaEnabled : false,
          color : '#0338FB',
          dash : [2,4] 
       },
       d10 : {
          window : 10,
          smaEnabled : true,
          emaEnabled : false,
          color : '#C30061',
          dash : [2,4] 
       },
       d20 : {
          window : 20,
          smaEnabled : false,
          emaEnabled : false,
          color : '#118788',
          dash : [2,4] 
       },
       d50 : {
          window : 50,
          smaEnabled : false,
          emaEnabled : false,
          color : '#FC5D08',
          dash : [2,4] 
       },
       d100 : {
          window : 100,
          smaEnabled : false,
          emaEnabled : false,
          color : '#102C99',
          dash : [2,4] 
       },
       d200 : {
          window : 200,
          smaEnabled : false,
          emaEnabled : false,
          color : '#C30061',
          dash : [2,4] 
       },
    } ;
    
    // -----------------------------------------------------------------------
    // --- [START] Scope functions -------------------------------------------
    console.log( "Loading GraphDisplayDialogController" ) ;
    
    $scope.$on( 'graphDialogDisplay', function( _event, args ) {
        $scope.graphParams = args ;
        fetchChartData() ;
    } ) ;
    
    $scope.setDuration = function( newDuration ) {
        if( newDuration != $scope.duration ) {
            $scope.duration = newDuration ;
            
            for( key in $scope.maGraphs ) {
                $scope.maGraphs[key].smaEnabled = false ;
                $scope.maGraphs[key].emaEnabled = false ;
            }
            
            if( newDuration == '1m' || 
                newDuration == '2m' ) {
                $scope.maGraphs.d5.smaEnabled   = true ;
                $scope.maGraphs.d10.smaEnabled  = true ;
            }
            else if( newDuration == '3m' |
                     newDuration == '6m' ) {
                $scope.maGraphs.d10.smaEnabled = true ;
                $scope.maGraphs.d20.smaEnabled = true ;
            }
            else if( newDuration == '1y' ) {
                $scope.maGraphs.d20.smaEnabled = true ;
                $scope.maGraphs.d50.smaEnabled = true ;
            }
            else if( newDuration == '2y' || 
                     newDuration == '3y' ) {
                $scope.maGraphs.d50.smaEnabled  = true ;
                $scope.maGraphs.d100.smaEnabled = true ;
            }
            else if( newDuration == '5y' ) {
                $scope.maGraphs.d100.smaEnabled = true ;
                $scope.maGraphs.d200.smaEnabled = true ;
            }
            fetchChartData() ;
        }
    }
    
    $scope.getDurationBtnClass = function( duration ) {
        if( $scope.duration == duration ) {
            return "sel-duration-btn" ;
        }
        return null ;
    }
    
    $scope.hideGraphDialog = function( holding ) {
        $( '#graphDisplayDialog' ).modal( 'hide' ) ;
    }
    
    $scope.getAmtClass = function( value ) {
        return ( value < 0 ) ? "neg_amt" : "pos_amt" ;
    }
    
    $scope.maGraphOptionsChanged = function( smaType ) {
        drawChart( false ) ;
    }
    
    $scope.resetZoom = function() {
        if( chart != null ) {
            chart.resetZoom() ;
        }
    }
    
    $scope.getCurrentMktPrice = function() {
        if( $scope.chartData == null ) {
            return 0 ;
        }
        else if( $scope.chartData.holding != null ) {
            return $scope.chartData.holding.currentMktPrice ;
        }
        return $scope.chartData.equityMaster.close ;
    }
    
    $scope.bollinger = function() {
        console.log( "Bollinger" ) ;
    }
    
    // --- [END] Scope functions

    // -----------------------------------------------------------------------
    // --- [START] Local functions -------------------------------------------
    
    function getBuyTradesDataset() {
        var buyData = $scope.chartData.buyData ;
        return {
            type             : 'scatter',
            data             : buyData,
            backgroundColor  : BUY_COLOR,
            borderColor      : BUY_COLOR,
            pointRadius      : getPointRadiusArray( buyData ),
        } ;
    }
    
    function getSellTradesDataset() {
        var sellData = $scope.chartData.sellData ;
        return {
            type             : 'scatter',
            data             : sellData,
            backgroundColor  : SELL_COLOR,
            borderColor      : SELL_COLOR,
            radius           : getPointRadiusArray( sellData ),
        } ;
    }
    
    function getAvgPriceDataset() {
        return {
            type             : 'scatter',
            data             : $scope.chartData.avgData,
            backgroundColor  : AVG_LINE_COLOR,
            borderColor      : AVG_LINE_COLOR,
            radius           : 5,
            borderWidth      : 1,
            showLine         : true,
        } ;
    }
    
    function getEodPriceDataset() {
        
        var eodPriceList = $scope.chartData.eodPriceList ;
        var eodLineColor = EOD_LINE_COLOR_RED ;
        if( eodPriceList[0] < eodPriceList.at(-1) ) {
            eodLineColor = EOD_LINE_COLOR_GREEN ;
        }
        
        return {
            type             : 'line',
            data             : eodPriceList,
            borderColor      : eodLineColor,
            backgroundColor  : eodLineColor,
            borderWidth      : 2,
            tension          : 0,
            radius           : 0,
        } ;
    }
    
    function getCMPDataset() {
        
        var cd = $scope.chartData ;
        var cmpColor = '#000000' ;
        
        if( cd.avgData.length > 0 ) {
            cmpColor = ( cd.avgData[0].y < cd.cmpData[0].y ) ? '#00FF00' : 
                                                               '#FF0000' ; 
        }
        
        return {
            type             : 'scatter',
            data             : cd.cmpData,
            borderColor      : cmpColor,
            backgroundColor  : cmpColor,
            radius           : 4,
        } ;
    }
    
    function getMADataset( datasets ) {
        
        var eodPriceList = $scope.chartData.eodPriceList ;
        
        for( const maKey in $scope.maGraphs ) {
            
            const maCfg = $scope.maGraphs[maKey] ;
            
            if( !( maCfg.smaEnabled || maCfg.emaEnabled ) ) { 
                continue ; 
            }
            
            var maValues = null ;
            
            if( maCfg.smaEnabled ) {
                maValues = calculateSMA( eodPriceList, maCfg.window ) ;
                if( maValues.length == 0 ) { continue ; }
                datasets.push( {
                    type             : 'line',
                    data             : maValues,
                    borderColor      : maCfg.color ,
                    backgroundColor  : maCfg.color,
                    borderWidth      : 1,
                    tension          : 0.25,
                    radius           : 0,
                    borderDash       : maCfg.dash
                } ) ;
            }
            
            if( maCfg.emaEnabled ) {
                maValues = calculateEMA( eodPriceList, maCfg.window ) ;
                if( maValues.length == 0 ) { continue ; }
                datasets.push( {
                    type             : 'line',
                    data             : maValues,
                    borderColor      : maCfg.color ,
                    backgroundColor  : maCfg.color,
                    borderWidth      : 1,
                    tension          : 0.25,
                    radius           : 0,
                    borderDash       : maCfg.dash
                } ) ;
            }
        }
    }
    
    function drawChart( animate ) {
        
        $( '#graphDisplayDialog' ).modal( 'show' ) ;
        
        if( chart != null ) { chart.destroy() ; }

        const ctx = document.getElementById( 'eodGraph' ) ;
        const animationDuration = animate ? 1000 : 0 ;

        var datasets = [] ;
        
        datasets.push( getBuyTradesDataset()  ) ;
        datasets.push( getSellTradesDataset() ) ;
        datasets.push( getAvgPriceDataset()   ) ;
        datasets.push( getEodPriceDataset()   ) ;
        datasets.push( getCMPDataset()        ) ;
        
        getMADataset( datasets ) ;
        
        const options = {
            plugins : {
                legend: {
                    display: false,
                },
                tooltip: {
                    callbacks: {
                        title : renderTooltipTitle,
                        label : renderTooltipLabel
                    }
                },
                zoom : {
                    zoom: {
                        mode : 'xy',
                        wheel : {
                            enabled : true,
                            speed : 0.05,
                            modifierKey : 'ctrl',
                        },
                        drag : {
                            enabled : true,
                            modifierKey : 'meta',
                        }
                    },
                    pan : {
                        mode : 'xy',
                        enabled : true,
                    }
                }
            },
            scales : {
                x : {
                    type:'time',
                    time: { 
                        unit: 'day',
                        displayFormats: {
                           'day': 'DD-MM-YY'
                        } 
                    },
                    ticks: {
                        font: {
                            size: 10,
                        }
                    }
                }  
            },
            animation : {
                duration : animationDuration 
            },
            transitions : {
                zoom : {
                    animation : {
                        duration : 1000,
                        easing: 'easeOutCubic'
                    }
                }
            }
        } ;
        
        chart = new Chart( ctx, {
            data: {
              labels: $scope.chartData.labels,
              datasets: datasets
            },
            options: options
        } ) ;
    }
    
    function getPointRadiusArray( data ) {
        
        var radiusArray = [] ;
        
        for( var i=0; i<data.length; i++ ) {

            var d = data[i].q - minQuantity ;
            var r = SCATTER_POINT_RADIUS ;
            
            if( quantityRange > 0 ) {
                r = ((d * RADIUS_RANGE)/quantityRange) + MIN_RADIUS ;    
            }
            radiusArray.push( r ) ;
        }
        
        return radiusArray ;
    }
    
    function renderTooltipTitle( context ) {
        var title = context[0].label ;
        title = title.substring( 0, title.lastIndexOf( ',' ) ) ;
        return title ;
    }
    
    function renderTooltipLabel( context ) {
        if( context.dataset.type == 'scatter' ) {
            if( context.raw.hasOwnProperty( 'q' ) ) {
                return "Units = " + context.dataset.data[ context.dataIndex ].q ;
            }
        }
        return "Price = " + context.formattedValue ;
    }
    
    function extractQuantityRange() {
        
        minQuantity = 99999 ;
        maxQuantity = 0 ;
        
        for( var i=0; i<$scope.chartData.buyData.length; i++ ) {
            minQuantity = Math.min( minQuantity, $scope.chartData.buyData[i].q ) ;
            maxQuantity = Math.max( maxQuantity, $scope.chartData.buyData[i].q ) ;
        }
        
        for( var i=0; i<$scope.chartData.sellData.length; i++ ) {
            minQuantity = Math.min( minQuantity, $scope.chartData.sellData[i].q ) ;
            maxQuantity = Math.max( maxQuantity, $scope.chartData.sellData[i].q ) ;
        }
        quantityRange = maxQuantity - minQuantity ;
    }
    
    function calculateSMA( data, window ) {
        
        var sum = 0;
        var result = [] ;
        
        if( data.length < window ){ return result; }
        
        for( var i=0; i<window; ++i ) {
            sum += data[i] ;
            result.push( sum/(i+1) ) ;
        }
        
        result.push( sum / window ) ;
        
        var steps = data.length - window - 1 ;
        for( var i=0; i<steps; ++i) {
            sum -= data[i] ;
            sum += data[i + window] ;
            result.push( sum / window ) ;
        }
        return result ;
    }
    
    function calculateEMA( data, window ) {
        
        var k = 2/( window + 1 ) ;
        var emaArray = [data[0]] ;
        
        for( var i=1; i<data.length; i++ ) {
            emaArray.push( k*data[i] + emaArray[i - 1]*(1 - k) ) ;
        }
        return emaArray;
    }
    
    // ------------------- Server comm functions -------------------------------
    
    function fetchChartData() {
        
        console.log( "Fetching chart data for " + $scope.graphParams.symbolNse + 
                     " and owner "              + $scope.graphParams.ownerName ) ;
        
        $scope.inbetweenServerCall = true ;
        $http.get( '/Equity/GraphData' + 
                   '?duration='  + $scope.duration + 
                   '&symbolNse=' + $scope.graphParams.symbolNse + 
                   '&owner='     + $scope.graphParams.ownerName )
        .then ( 
            function( response ){
                console.log( response.data ) ;
                $scope.chartData = response.data ;
                extractQuantityRange() ;
                drawChart( true ) ;
            }
        )
        .finally(function() {
            $scope.$emit( 'interactingWithServer', { isStart : false } ) ;
            $scope.inbetweenServerCall = false ;
        }) ;
    }
} ) ;