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
    
    var footerChartsMeta = {
        footer : {
            // Total height of footer. Sum of heights of all visible footer charts.
            height : 0,
        },
        macd : {
            visible : false,
            height  : 100,
            bottom  : -1,     // Updated when chart is made visible
        },
    } ;
    
    // ---------------- Object templates --------------------------------------
    var baseMAOpts = {
        smaEnabled : false,
        emaEnabled : false,
        dash       : [2,4] 
    }

    // ---------------- Scope variables --------------------------------------
    $scope.graphParams = null ; // Input to fetch the chart data
    $scope.chartData = null ;   // Obtained from server
    
    $scope.seriesCache = new Map() ;
    
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
    
    $scope.bollingerOptions = {
        upper  : { enabled: true, color: '#cfcccc', dash:[]    },
        middle : { enabled: true, color: '#3d54ff', dash:[2,4] },
        lower  : { enabled: true, color: '#cfcccc', dash:[]    },
    } ;
    
    // -----------------------------------------------------------------------
    // --- [START] Scope functions -------------------------------------------
    
    // Catching the event when the graph icon is pressed on any of the pages
    $scope.$on( 'graphDialogDisplay', function( _event, args ) {
        $scope.graphParams = args ;
        $( '#graphDisplayDialog' ).modal( 'show' ) ;
        fetchChartData() ;
    } ) ;
    
    // The time duration has been changed by the user. Fetch the graph
    // data afresh.
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
    
    // Selected duration button is decorated differently via dynamic CSS
    $scope.getDurationBtnClass = function( duration ) {
        if( $scope.duration == duration ) {
            return "sel-duration-btn" ;
        }
        return null ;
    }

    // The graph dialog is being hidden by user action    
    $scope.hideGraphDialog = function( holding ) {
        $( '#graphDisplayDialog' ).modal( 'hide' ) ;
    }
    
    // Negative and positive amounts are rendered differently.
    $scope.getAmtClass = function( value ) {
        return ( value < 0 ) ? "neg_amt" : "pos_amt" ;
    }
    
    // Moving average graph options have changed. Add or remove based on
    // selected visibility.
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
                         
            plotSeries( getMADataset( seriesName, maCfg, series ) ) ;
        }
        else {
            eraseSeries( seriesName ) ;
        }
    }
    
    // Bollinger curve visibility options changed
    $scope.bollingerGraphOptionsChanged = function( curveName ) {
        plotBollingerBand( curveName ) ;
    }
    
    // Plot all the bollinger bands - upper, middle and lower.
    $scope.plotBollingerBands = function() {
        
        eraseMASerieses() ;
        for( const key in $scope.bollingerOptions ) {
            plotBollingerBand( key ) ;
        }
    }
    
    $scope.resetZoom = function() {
        if( chart != null ) {
            chart.resetZoom() ;
        }
    }
    
    // Gets the current market price. If we have a holding the CMP might be
    // refreshed intraday, hence the special handling.
    $scope.getCurrentMktPrice = function() {
        if( $scope.chartData == null ) {
            return 0 ;
        }
        else if( $scope.chartData.holding != null ) {
            return $scope.chartData.holding.currentMktPrice ;
        }
        return $scope.chartData.equityMaster.close ;
    }
    
    $scope.repaintChart = function() {
        if( chart != null ) {
            chart.render() ;
        }
    }
    
    $scope.showFooterChart = function( chartId ) {
        
        var chartMeta = footerChartsMeta[ chartId ] ;
        if( chartMeta.visible ) { return ; }
        
        var eodChartDiv = document.getElementById( "eodChartDiv"  ) ;
        var chartDiv    = document.getElementById( chartId + "ChartDiv" ) ;
        
        var curFooterHeight   = footerChartsMeta.footer.height ;
        var newFooterHeight   = curFooterHeight + chartMeta.height ;
        var newEodChartHeight = eodChartDiv.clientHeight - chartMeta.height ;
        
        // Changes to the DOM of EOD chart
        eodChartDiv.style.height = newEodChartHeight + "px" ;
        
        // Changes to the DOM of chart which is to be made visible
        chartDiv.style.display = "block" ;
        chartDiv.style.bottom = curFooterHeight + "px" ;
        
        // Updating the meta information
        chartMeta.visible = true ;
        chartMeta.bottom = curFooterHeight ;
        
        // Updating the footer meta
        footerChartsMeta.footer.height = newFooterHeight ;
    }
    
    $scope.hideFooterChart = function( chartId ) {
        
        var chartMeta = footerChartsMeta[ chartId ] ;
        if( !chartMeta.visible ) { return ; }
        
        var eodChartDiv = document.getElementById( "eodChartDiv"  ) ;
        var chartDiv    = document.getElementById( chartId + "ChartDiv" ) ;
        
        var curFooterHeight   = footerChartsMeta.footer.height ;
        var newFooterHeight   = curFooterHeight - chartMeta.height ;
        var newEodChartHeight = eodChartDiv.clientHeight + chartMeta.height ;
        var chartBottom       = chartMeta.bottom ;
        
        // Changes to the DOM of EOD chart
        eodChartDiv.style.height = newEodChartHeight + "px" ;
        
        // Changes to the DOM of chart which is to be hidden
        chartDiv.style.display = "none" ;
        
        // Updating the meta information
        chartMeta.visible = false ;
        chartMeta.bottom = -1 ;
        
        // Updating the footer meta
        footerChartsMeta.footer.height = newFooterHeight ;
        
        // If there are other footer charts which are visible, we need to
        // shift down the ones whose yIndex is greater than the one we removed
        for( id in footerChartsMeta ) {
            
            if( id == 'footer' ) continue ;
            
            var meta = footerChartsMeta[ id ] ;
            if( meta.visible && meta.bottom > chartBottom ) {
                var chartDiv = document.getElementById( id + "ChartDiv" ) ;
                meta.bottom -= chartMeta.height ;
                chartDiv.style.bottom = meta.bottom + "px" ;
            }
        }
    }
    
    // -----------------------------------------------------------------------
    // --- [START] Local functions -------------------------------------------
    
    function drawChart() {
        
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
        
        chart = new Chart( document.getElementById( 'eodChartCanvas' ), {
            data: {
              labels: $scope.chartData.labels,
              datasets: datasets
            },
            options: chartOptions
        } ) ;
    }
    
    function plotBollingerBand( bandName ) {
        
        const cfg = $scope.bollingerOptions[ bandName ] ;
        const seriesName = "bollinger-" + bandName ;
        
        if( cfg.enabled ) {
            plotSeries( getBollingerDataset( seriesName, cfg ) ) ;
        }
        else {
            eraseSeries( seriesName ) ;
        }
    }
    
    function eraseMASerieses() {
        
        for( key in $scope.maGraphs ) {
            $scope.maGraphs[key].smaEnabled = false ;
            $scope.maGraphs[key].emaEnabled = false ;
            
            eraseSeries( 'sma-' + key ) ;
            eraseSeries( 'ema-' + key ) ;
        }
    }
    
    function plotSeries( dataset ) {
        addDataset( dataset ) ;
        if( chart != null ) {
            chart.update() ;
        }
    }
    
    function eraseSeries( seriesName ) {
        if( removeDataset( seriesName ) ) {
            chart.update() ;
        }
    }
    
    // ------------------- Dataset creation functions --------------------------
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
    
    function getBollingerDataset( seriesName, cfg ) {
        
        var values = $scope.seriesCache.get( seriesName ) ;
        return {
            name             : seriesName,
            type             : 'line',
            data             : values,
            borderColor      : cfg.color ,
            backgroundColor  : cfg.color,
            borderWidth      : 1,
            tension          : 0.25,
            radius           : 0,
            borderDash       : cfg.dash
        } ;
    }
    
    // ------------------- Series management utility functions -----------------
    function addDataset( dataset ) {
        removeDataset( dataset.name ) ;
        datasets.push( dataset ) ;
    }
    
    function removeDataset( seriesName ) {
        
        var removed = false ;
        for( var i=0; i<datasets.length; i++ ) {
            var dataset = datasets[i] ;
            if( dataset.hasOwnProperty( 'name' ) ) {
                if( dataset.name == seriesName ) {
                    datasets.splice( i, 1 ) ;
                    removed = true ;
                    break ;
                }                    
            }
        }
        return removed ;
    }
    
    function getSeries( key, genFn ) {
        
        var series = null ;
        if( $scope.seriesCache.has( key ) ) {
            series = $scope.seriesCache.get( key ) ;
        }
        else {
            series = genFn() ;
            $scope.seriesCache.set( key, series ) ;
        }
        return series ;
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
    
    // ------------------- Insitu series calculation functions -----------------
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
                $scope.seriesCache.clear() ;
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
            transitions : getTransitionsOptions(),
            responsive : true,
            maintainAspectRatio: false
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