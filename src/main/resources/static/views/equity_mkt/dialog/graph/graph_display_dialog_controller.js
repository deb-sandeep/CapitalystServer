capitalystNgApp.controller( 'GraphDisplayDialogController', 
                            function( $scope, $http, $window ) {
    
    const BUY_COLOR            = '#00f0ff' ;
    const SELL_COLOR           = '#ffc300' ;
    const EOD_LINE_COLOR       = '#B5B7B5' ;
    const EOD_LINE_COLOR_GREEN = '#adc5b3' ;
    const EOD_LINE_COLOR_RED   = '#c4aba7' ;
    const AVG_LINE_COLOR       = '#ABABAB' ;
    const SCATTER_POINT_RADIUS = 5 ;
    const MIN_RADIUS           = 2 ;
    const MAX_RADIUS           = 7 ;
    const RADIUS_RANGE         = MAX_RADIUS - MIN_RADIUS ;
    
    // ---------------- Local variables --------------------------------------
    var chart = null ;
    var datasets = [] ;
    var chartOptions = null ;
    var annotations = {} ;
    
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
            height  : 125,
            bottom  : -1,     // Updated when chart is made visible
        },
        rsi : {
            visible : false,
            height  : 125,
            bottom  : -1,
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
    $scope.duration = '6m' ;
    
    $scope.maGraphs = {
        d5  :{ ...baseMAOpts, window:   5, color: '#00baff' },
        d10 :{ ...baseMAOpts, window:  10, color: '#00ea50', smaEnabled: true, dash:[2,2] },
        d20 :{ ...baseMAOpts, window:  20, color: '#ff00a1' },
        d50 :{ ...baseMAOpts, window:  50, color: '#fc8e04' },
        d100:{ ...baseMAOpts, window: 100, color: '#49bed0' },
    } ;
    
    $scope.bollingerOptions = {
        upper  : { enabled: false, color: '#139f9f', dash:[]    },
        middle : { enabled: false, color: '#ff00a1', dash:[2,2] },
        lower  : { enabled: false, color: '#139f9f', dash:[]    },
    } ;
    
    $scope.measureConfig = {
        enabled : true,
        currentMode : null,
        start : { x : 0, y : 0 },
        end   : { x : 0, y : 0 },
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
                
                if( opts[key].smaEnabled || opts[key].emaEnabled ) {
                    anyMAEnabled = true ;
                    opts[key].smaEnabled = false ;
                    opts[key].emaEnabled = false ;
                }
            }
            
            opts.d10.smaEnabled = true ;
            
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
    
    // Plot all the bollinger bands - upper, middle and lower.
    $scope.plotBollingerBands = function() {
        for( const key in $scope.bollingerOptions ) {
            plotBollingerBand( key ) ;
        }
    }
    
    $scope.resetZoom = function() {
        if( chart != null ) {
            chart.resetZoom() ;
            syncChartXAxisRange() ;
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
    
    $scope.isSeriesVisible = function( seriesName ) {
        return (getDatasetIndex( seriesName ) != -1) ;
    }
    
    $scope.isFooterChartVisible = function( chartId ) {
        
        var chartMeta = footerChartsMeta[ chartId ] ;
        return chartMeta.visible ;
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
        
        // Updating the meta information
        chartMeta.visible = false ;
        chartMeta.bottom = -1 ;
        
        // Updating the footer meta
        footerChartsMeta.footer.height = newFooterHeight ;
        
        // Changes to the DOM of EOD chart
        eodChartDiv.style.height = newEodChartHeight + "px" ;
        
        // Changes to the DOM of chart which is to be hidden
        chartDiv.style.display = "none" ;

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
    
    $scope.showMCChart = function() {
        $window.open( "https://www.moneycontrol.com/mc/stock/chart" + 
                      "?exchangeId=" + $scope.graphParams.symbolNse + 
                      "&ex=NSE" ) ;
    }
    
    $scope.toggleMeasureMode = function() {
        
        if( !$scope.measureConfig.enabled ) {
            $scope.measureConfig.enabled = true ;
        }
        else {
            $scope.measureConfig.enabled = false ;
            deleteMeasureAnnotation( 'all' ) ;
            chart.update() ;
        }
    }
    
    $scope.modeButtonEnabledClass = function( enabled ) {
        return enabled ? "mode-btn-enabled" : "mode-btn-disabled" ;
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
                
                $scope.$broadcast( 'eodGraphPreDestroy', null ) ;
        
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
    
    // ------------------- Chart drawing functions -----------------------------
    function drawChart() {
        
        if( chart != null ) { 
            chart.destroy() ; 
            chart = null ;
        }

        datasets.length = 0 ;
        annotations = {} ;
        chartOptions = getChartOptions() ;
        
        addDataset( getBuyTradesDataset()  ) ;
        addDataset( getSellTradesDataset() ) ;
        addDataset( getAvgPriceDataset()   ) ;
        addDataset( getEodPriceDataset()   ) ;
        addDataset( getCMPDataset()        ) ;
        
        addMADatasets() ;
        
        Chart.defaults.color = '#b0b0b0' ;
        Chart.defaults.borderColor = '#4d4d4d' ;
        
        chart = new Chart( document.getElementById( 'eodChartCanvas' ), {
            data: {
              labels: $scope.chartData.labels,
              datasets: datasets
            },
            options: chartOptions
        } ) ;
        
        $scope.$broadcast( 'eodGraphPostRender', null ) ;
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
            borderWidth      : 1,
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
        var index = getDatasetIndex( seriesName ) ;
        if( index != -1 ) {
            datasets.splice( index, 1 ) ;
            removed = true ;            
        }
        return removed ;
    }
    
    function getDatasetIndex( seriesName ) {
        
        for( var i=0; i<datasets.length; i++ ) {
            var dataset = datasets[i] ;
            if( dataset.hasOwnProperty( 'name' ) ) {
                if( dataset.name == seriesName ) {
                    return i ;
                }                    
            }
        }
        return -1 ;
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
    
    // ------------------- Mouse handling --------------------------------------
    function handleMouseClick( event ) {
        
        const p = getMouseCoordinates( event ) ;
        if( $scope.measureConfig.enabled ) {
            handleMeasureClickEvent( p ) ;
        }
    }
    
    function handleMouseHover( event ) {
        
        const p = getMouseCoordinates( event ) ;
        if( $scope.measureConfig.enabled ) {
            handleMeasureHoverEvent( p ) ;
        }
    }
    
    function handleMeasureClickEvent( p ) {
        
        const config  = $scope.measureConfig ;
        const curMode = config.currentMode ;
         
        if( curMode == null || curMode == 'seeking-start' ) {
            config.start = p ;
            config.currentMode = 'seeking-end' ;

            addMeasureStartAnnotation( p ) ;
            deleteMeasureAnnotation( 'measureEnd' ) ;
            chart.update() ;
        }
        else if( curMode == 'seeking-end' ) {
            config.end = p ;
            config.currentMode = 'seeking-start' ;

            addMeasureEndAnnotation( p ) ;
            chart.update() ;
        }
    }
    
    function handleMeasureHoverEvent( p ) {
        
        const config  = $scope.measureConfig ;
        const curMode = config.currentMode ;
         
        if( curMode == 'seeking-end' ) {
            config.end = p ;
            addMeasureRegionAnnotation( p ) ;
        }
        chart.update() ;
    }
    
    function addMeasureStartAnnotation( p ) {
        annotations[ 'measureStart' ] = {
           type   : 'point',
           xValue : p.x,
           yValue : p.y,
           radius : 3,
           
           borderColor     : 'cyan',
           backgroundColor : 'cyan',
        } ;
    }
    
    function addMeasureEndAnnotation( p ) {
        annotations[ 'measureEnd' ] = {
           type   : 'point',
           xValue : p.x,
           yValue : p.y,
           radius : 3,
           
           borderColor     : 'red',
           backgroundColor : 'red',
        } ;
    }
    
    function addMeasureRegionAnnotation() {
        
        const start = $scope.measureConfig.start ;
        const end   = $scope.measureConfig.end ;
        const minX  = Math.min( start.x, end.x ) ;
        const maxX  = Math.max( start.x, end.x ) ;
        const minY  = Math.min( start.y, end.y ) ;
        const maxY  = Math.max( start.y, end.y ) ;
        
        annotations[ 'measureRegion' ] = {
           type : 'box',
           xMin : minX,
           xMax : maxX,
           yMin : minY,
           yMax : maxY,
           
           borderColor     : 'rgba(90, 93, 107, 0.3)',
           backgroundColor : 'rgba(90, 93, 107, 0.3)',
        } ;
        
        addMeasureDetailsAnnotation( start, end ) ;
    }
    
    function addMeasureDetailsAnnotation( start, end ) {
        
        const minX  = Math.min( start.x, end.x ) ;
        const maxX  = Math.max( start.x, end.x ) ;
        const minY  = Math.min( start.y, end.y ) ;
        const maxY  = Math.max( start.y, end.y ) ;

        const startDate   = new Date( minX ) ;
        const endDate     = new Date( maxX ) ;
        const numDays     = Math.floor((maxX - minX)/(1000*60*60*24)) ; 
        const priceChg    = end.y - start.y ;
        const priceChgPct = (priceChg / start.y)*100 ; 
        
        //content.push( "Start date : " + startDate.toLocaleDateString() ) ;
        //content.push( "End date   : " + endDate.toLocaleDateString() ) ;
        const pctChgStr = Number( priceChgPct ).toFixed( 1 ) + "%        " ;
        const numDayStr = getDurationStr( numDays ) ;
        
        annotations[ 'measurePriceChgDetails' ] = {
            type      : 'label',
            xValue    : maxX,
            yValue    : maxY,
            content   : pctChgStr,
            textAlign : 'left',
            color     : 'white',
            font   : {
              size   : 12,
              family : 'courier'
            },
            backgroundColor: 'rgba(0,0,0,0.0)',
        } ;
        
        annotations[ 'measureNumDaysDetails' ] = {
            type      : 'label',
            xValue    : (maxX+minX)/2,
            yValue    : minY,
            content   : numDayStr,
            textAlign : 'center',
            color     : 'white',
            font   : {
              size   : 12,
              family : 'courier'
            },
            backgroundColor: 'rgba(0,0,0,0.0)',
        } ;
    }
    
    function getDurationStr( numDays ) {
        
        var str = "" ;
        
        if( numDays > 365 ) {
            var numYears = Math.floor( numDays/365 ) ;
            str = numYears + "y " ;
            numDays -= numYears * 365 ;
        }
        
        if( numDays > 30 ) {
            var numMonths = Math.floor( numDays/30 ) ;
            str += numMonths + "m ";
            numDays -= numMonths * 30 ;
        }
        
        if( numDays > 0 ) {
            str += numDays + "d" ;
        }
        
        return str ;
    }
    
    function deleteMeasureAnnotation( name ) {
        if( name == 'all' ) {
            for( name in annotations ) {
                if( name.startsWith( 'measure' ) ) {
                    deleteMeasureAnnotation( name  ) ;
                }
            }
        }
        else if( annotations.hasOwnProperty( name ) ) {
            delete annotations[ name ] ;
        }
    }
    
    function getMouseCoordinates( event ) {
        
        const relPos = Chart.helpers.getRelativePosition(event, chart) ;
        
        return { 
            x: chart.scales.x.getValueForPixel( relPos.x ) , 
            y: chart.scales.y.getValueForPixel( relPos.y ) , 
        } ;
    }
    
    // ------------------- Graph options ---------------------------------------
    function getChartOptions() {
        
        return {
            
            plugins : {
                legend: { display: false },
                tooltip: getTooltipOptions(),
                zoom : getZoomPanOptions(),
                autocolors : false,
                annotation : {
                    annotations : annotations,
                },
            },
            scales : getScaleOptions(),
            animation : getAnimationOptions(),
            transitions : getTransitionsOptions(),
            responsive : true,
            maintainAspectRatio: false,
            events: ['click', 'mousemove'],
            onClick: handleMouseClick,
            onHover: handleMouseHover,
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
                },
                onZoom: syncChartXAxisRange            
            },
            pan : {
                mode : 'xy',
                enabled : true,
                onPan: syncChartXAxisRange   
            }
        } ;
    }
    
    function syncChartXAxisRange() {
        
        var scaleRanges = chart.getDatasetMeta(0)._scaleRanges ;
        
        Chart.helpers.each( Chart.instances, function( instance ) {
            
            var instanceXScale = instance.config._config.options.scales.x ;
            
            instanceXScale.min = scaleRanges.xmin ;
            instanceXScale.max = scaleRanges.xmax ;
            
            instance.update();
        } ) ;
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
            },
            y : {
                ticks: {
                    font: {
                        family: 'courier'
                    },
                    callback: function( value, index, ticks ) {
                        return ('' + Math.round(value)).padStart( 6, ' ' ) ;
                    }
                }
            }
        } ;
    }
    
    function renderYTick( value, index, ticks ) {
        return ('' + value).padStart( 10, ' ' ) ;
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