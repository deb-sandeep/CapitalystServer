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
    var datasets = [] ;
    var chartOptions = null 
    
    var minQty = 999999 ;
    var maxQty = 0 ;
    var qtyRange = 0 ;
    
    var seriesCache = new Map() ;
    
    // ---------------- Object templates --------------------------------------
    var baseMAOpts = {
        smaEnabled : false,
        emaEnabled : false,
        dash       : [2,4] 
    }

    // ---------------- Scope variables --------------------------------------
    $scope.chartData = null ;
    $scope.graphParams = null ;
    $scope.durationKeys = [ '5y', '3y', '2y', '1y', '6m', '3m', '2m', '1m' ] ;
    $scope.duration = '3m' ;
    $scope.maGraphs = {
       d5  :{ ...baseMAOpts, window:   5, color: '#0338FB', smaEnabled: true },
       d10 :{ ...baseMAOpts, window:  10, color: '#C30061', smaEnabled: true },
       d20 :{ ...baseMAOpts, window:  20, color: '#118788' },
       d50 :{ ...baseMAOpts, window:  50, color: '#FC5D08' },
       d100:{ ...baseMAOpts, window: 100, color: '#102C99' },
       d200:{ ...baseMAOpts, window: 200, color: '#C30061' },
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
            var opts = $scope.maGraphs ;
            
            for( key in $scope.maGraphs ) {
                opts[key].smaEnabled = false ;
                opts[key].emaEnabled = false ;
            }
            
            if( newDuration == '1m' || newDuration == '2m' ) {
                opts.d5.smaEnabled  = true ;
                opts.d10.smaEnabled = true ;
            }
            else if( newDuration == '3m' || newDuration == '6m' ) {
                opts.d10.smaEnabled = true ;
                opts.d20.smaEnabled = true ;
            }
            else if( newDuration == '1y' ) {
                opts.d20.smaEnabled = true ;
                opts.d50.smaEnabled = true ;
            }
            else if( newDuration == '2y' || newDuration == '3y' ) {
                opts.d50.smaEnabled  = true ;
                opts.d100.smaEnabled = true ;
            }
            else if( newDuration == '5y' ) {
                opts.d100.smaEnabled = true ;
                opts.d200.smaEnabled = true ;
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
    
    $scope.maGraphOptionsChanged = function( maType, window ) {
        
        const seriesName = maType + '-' + window ;
        const maCfg      = $scope.maGraphs[window] ;
        const visibility = maCfg[ maType + 'Enabled' ] ;
        
        if( visibility == true ) {
            var series = getSeries( seriesName, () => {
                            return maType == 'sma' ?
                                   calculateSMA( maCfg.window ) :
                                   calculateEMA( maCfg.window ) ;
                         } ) ;
                         
            addDataset( getMADataset( seriesName, maCfg, series ) ) ;
        }
        else {
            removeDataset( seriesName ) ;
        }
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
    
    function drawChart() {
        
        $( '#graphDisplayDialog' ).modal( 'show' ) ;
        if( chart != null ) { 
            chart.destroy() ; 
            chart = null ;
        }

        datasets.length = 0 ;
        chartOptions = getChartOptions() ;
        
        addDataset( getBuyTradesDataset()  ) ;
        addDataset( getSellTradesDataset() ) ;
        addDataset( getAvgPriceDataset()   ) ;
        addDataset( getEodPriceDataset()   ) ;
        addDataset( getCMPDataset()        ) ;
        
        addMADatasets() ;
        
        chart = new Chart( document.getElementById( 'eodGraph' ), {
            data: {
              labels: $scope.chartData.labels,
              datasets: datasets
            },
            options: chartOptions
        } ) ;
    }
    
    function removeDataset( seriesName ) {
        
        for( var i=0; i<datasets.length; i++ ) {
            var dataset = datasets[i] ;
            if( dataset.hasOwnProperty( 'name' ) ) {
                if( dataset.name == seriesName ) {
                    datasets.splice( i, 1 ) ;
                    chart.update() ;
                    break ;
                }                    
            }
        }
    }
    
    function addDataset( dataset ) {
        
        datasets.push( dataset ) ;
        if( chart != null ) {
            chart.update() ;
        }
    }
    
    function getSeries( key, genFn ) {
        
        var series = null ;
        if( seriesCache.has( key ) ) {
            series = seriesCache.get( key ) ;
        }
        else {
            series = genFn() ;
            seriesCache.set( key, series ) ;
        }
        return series ;
    }
    
    function getBuyTradesDataset() {
        
        const seriesKey = 'buy-trades' ;
        var buyData = getSeries( seriesKey, 
                                 () => $scope.chartData.buyData ) ;
        return {
            name             : seriesKey,
            type             : 'scatter',
            data             : buyData,
            backgroundColor  : BUY_COLOR,
            borderColor      : BUY_COLOR,
            pointRadius      : getPointRadiusArray( buyData ),
        } ;
    }
    
    function getSellTradesDataset() {
        
        const seriesKey = 'sell-trades' ;
        var sellData = getSeries( seriesKey, 
                                  () => $scope.chartData.sellData ) ;
        return {
            name             : seriesKey,
            type             : 'scatter',
            data             : sellData,
            backgroundColor  : SELL_COLOR,
            borderColor      : SELL_COLOR,
            radius           : getPointRadiusArray( sellData ),
        } ;
    }
    
    function getAvgPriceDataset() {
        
        const seriesKey = 'avg-price' ;
        var avgPriceData = getSeries( seriesKey,
                                      () => $scope.chartData.avgData ) ;
        return {
            name             : seriesKey,
            type             : 'scatter',
            data             : avgPriceData,
            backgroundColor  : AVG_LINE_COLOR,
            borderColor      : AVG_LINE_COLOR,
            radius           : 5,
            borderWidth      : 1,
            showLine         : true,
        } ;
    }
    
    function getEodPriceDataset() {
        
        const seriesKey = 'eod-price' ;
        var eodPriceList = getSeries( seriesKey, 
                                      () => $scope.chartData.eodPriceList ) ;
        
        var eodLineColor = EOD_LINE_COLOR_RED ;
        if( eodPriceList[0] < eodPriceList.at(-1) ) {
            eodLineColor = EOD_LINE_COLOR_GREEN ;
        }
        
        return {
            name             : seriesKey,
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
            cmpColor = ( cd.avgData[0].y < cd.cmpData[0].y ) ? 
                       '#00FF00' : '#FF0000' ; 
        }
        
        return {
            name             : 'cur-price',
            type             : 'scatter',
            data             : cd.cmpData,
            borderColor      : cmpColor,
            backgroundColor  : cmpColor,
            radius           : 4,
        } ;
    }
    
    function addMADatasets() {
        
        for( const maKey in $scope.maGraphs ) {
            
            const maCfg = $scope.maGraphs[maKey] ;
            
            if( maCfg.smaEnabled ) {
                var seriesName = "sma-" + maKey ;
                var dataset = getMADataset( seriesName, maCfg, 
                                     getSeries( seriesName, function() {
                                        return calculateSMA( maCfg.window ) ;
                                     } ) ) ;
                addDataset( dataset ) ;
            }
            
            if( maCfg.emaEnabled ) {
                var seriesName = "ema-" + maKey ;
                var dataset = getMADataset( seriesName, maCfg, 
                                     getSeries( seriesName, function() {
                                        return calculateEMA( maCfg.window ) ;
                                     } ) ) ;
                addDataset( dataset ) ;
            }
        }
    }
    
    function getMADataset( seriesName, maCfg, maValues ) {
        
        return {
            name             : seriesName,
            type             : 'line',
            data             : maValues,
            borderColor      : maCfg.color ,
            backgroundColor  : maCfg.color,
            borderWidth      : 1,
            tension          : 0.25,
            radius           : 0,
            borderDash       : maCfg.dash
        } ;
    }
    
    function getPointRadiusArray( data ) {
        
        var radiusArray = [] ;
        
        for( var i=0; i<data.length; i++ ) {

            var d = data[i].q - minQty ;
            var r = SCATTER_POINT_RADIUS ;
            
            if( qtyRange > 0 ) {
                r = ((d * RADIUS_RANGE)/qtyRange) + MIN_RADIUS ;    
            }
            radiusArray.push( r ) ;
        }
        
        return radiusArray ;
    }
    
    function calculateSMA( window ) {
        
        const data = $scope.chartData.eodPriceList ;
        
        var sum = 0;
        var result = [] ;
        
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
    
    function calculateEMA( window ) {
        
        const data = $scope.chartData.eodPriceList ;

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
                
                $scope.chartData = response.data ;
                
                datasets.length = 0 ;
                seriesCache.clear() ;
                extractQuantityRange() ;
                drawChart() ;
            }
        )
        .finally(function() {
            $scope.$emit( 'interactingWithServer', { isStart : false } ) ;
            $scope.inbetweenServerCall = false ;
        }) ;
    }

    // ------------------- Post server comm data processing --------------------
    function extractQuantityRange() {
        
        minQty = 99999 ;
        maxQty = 0 ;
        
        for( var i=0; i<$scope.chartData.buyData.length; i++ ) {
            minQty = Math.min( minQty, $scope.chartData.buyData[i].q ) ;
            maxQty = Math.max( maxQty, $scope.chartData.buyData[i].q ) ;
        }
        
        for( var i=0; i<$scope.chartData.sellData.length; i++ ) {
            minQty = Math.min( minQty, $scope.chartData.sellData[i].q ) ;
            maxQty = Math.max( maxQty, $scope.chartData.sellData[i].q ) ;
        }
        qtyRange = maxQty - minQty ;
    }
    
    // ------------------- Graph options ---------------------------------------
    
    function getChartOptions() {
        
        return {
            
            plugins : {
                legend: { display: false },
                tooltip: getTooltipOptions(),
                zoom : getZoomPanOptions()
            },
            scales : getScaleOptions(),
            animation : getAnimationOptions(),
            transitions : getTransitionsOptions()
        } ;
    }
    
    function getTooltipOptions() {
        return {
            callbacks: {
                title : renderTooltipTitle,
                label : renderTooltipLabel
            }
        } ;
    }
    
    function getZoomPanOptions() {
        return {
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
        } ;
    }
    
    function getScaleOptions() {
        return {
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
        } ;
    }
    
    function getAnimationOptions() {
        return {
            duration : 0,
            easing: 'linear'
        } ;
    }
    
    function getTransitionsOptions() {
        return {
            zoom : {
                animation : {
                    duration : 1000,
                    easing: 'easeOutCubic'
                }
            }
        } ;
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
    
} ) ;