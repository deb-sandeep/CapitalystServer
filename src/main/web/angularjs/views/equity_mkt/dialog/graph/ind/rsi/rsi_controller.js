capitalystNgApp.controller( 'RSIController', 
                            function( $scope, $http ) {
    
    // ---------------- Constants ----------------------------------------------
    const CHART_ID = "rsi" ;
    const RSI_LINE_DS_ID = "rsi-line" ;
    
    // ---------------- Local variables ----------------------------------------
    var chart = null ;
    var datasets = [] ;
    var chartOptions = null ;
    var fresh = true ;
    
    var rsiLineData = null ;
    
    function resetState() {
        
        if( chart != null ) {
            chart.destroy() ;
        }

        datasets.length = 0 ;
        rsiLineData = null ;
    }

    // ---------------- Scope variables ----------------------------------------
    $scope.config = {
        windowSize : 14,
        enableFlag : false,
    } ;
    
    // -------------------------------------------------------------------------
    // --- [START] Scope functions ---------------------------------------------
    $scope.$on( "eodGraphPreDestroy", function( _event, args ) {
    } ) ;

    $scope.$on( "eodGraphPostRender", function( _event, args ) {
        
        if( fresh ) {
            fresh = false ;
            $scope.config.enableFlag = true ;
            $scope.enableFlagUpdated() ;
        }
        else if( $scope.$parent.isFooterChartVisible( CHART_ID ) ) {
            $scope.showRSIChart() ;
        }
    } ) ;

    $scope.enableFlagUpdated = function() {
        if( $scope.config.enableFlag ) {
            $scope.showRSIChart() ;
        }
        else {
            $scope.hideRSIChart() ;
        }
    }
    
    $scope.hideRSIChart = function() {
        
        $scope.$parent.hideFooterChart( CHART_ID ) ;
        $scope.config.enableFlag = false ;
        
        if( chart != null ) {
            chart.destroy() ;
            datasets.length = 0 ;
            rsiLineData = null ;
        }
    }
    
    $scope.showRSIChart = function() {
        
        const symbol = $scope.$parent.graphParams.symbolNse ;
        
        $http.get( '/Equity/GraphData/Indicator/RSI' + 
                   '?symbolNse=' + encodeURIComponent( symbol ) +
                   '&windowSize=' + $scope.config.windowSize )
        .then ( 
            function( response ){
                
                rsiLineData = response.data[ RSI_LINE_DS_ID ] ;
                
                $scope.$parent.showFooterChart( CHART_ID ) ;
                $scope.config.enableFlag = true ;
                
                drawChart() ;
            }
        ) ;
    }
    // --- [END] Scope functions

    // -------------------------------------------------------------------------
    // --- [START] Local functions ---------------------------------------------
    function drawChart() {
        
        if( chart != null ) { 
            chart.destroy() ; 
            chart = null ;
        }

        datasets.length = 0 ;
        chartOptions = getChartOptions() ;
        
        addDataset( getRSISellTriggerLineDataset() ) ;
        addDataset( getRSIBuyTriggerLineDataset()  ) ;
        
        var canvas = document.getElementById( CHART_ID + 'ChartCanvas' ) ;
        chart = new Chart( canvas, {
            data: {
              labels: $scope.$parent.chartData.labels,
              datasets: datasets
            },
            options: chartOptions
        } ) ;
        
        paintChartName( canvas ) ;
    }
        
    function paintChartName( canvas ) {
        
        var ctx = canvas.getContext( "2d" ) ;
        var oldColor = ctx.fillStyle ;
        
        ctx.font = "12px Courier" ;
        ctx.fillStyle = "white" ;
        ctx.fillText( "RSI", 0, 10 ) ;
        
        ctx.fillStyle = oldColor ;
    }
        
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
    
    // ------------------- Dataset creation functions --------------------------

    function getRSISellTriggerLineDataset() {
        
        return {
            name             : RSI_LINE_DS_ID ,
            type             : 'line',
            data             : rsiLineData,
            borderColor      : '#70D2FE',
            backgroundColor  : '#70D2FE',
            borderWidth      : 1,
            tension          : 0,
            radius           : 0,
            yAxisID          : 'y',
            fill : {
                target: {value:70},
                above: 'rgba(255, 0, 0, 0.2)',
                below: 'rgba(255, 255, 255, 0.0)'
            }
        } ;
    }
    
    function getRSIBuyTriggerLineDataset() {
        
        return {
            name             : RSI_LINE_DS_ID + '1' ,
            type             : 'line',
            data             : rsiLineData,
            borderColor      : '#00eab0',
            backgroundColor  : '#00eab0',
            borderWidth      : 0,
            tension          : 0,
            radius           : 0,
            yAxisID          : 'y',
            fill : {
                target: {value:30},
                above: 'rgba(255, 255, 255, 0.0)',
                below: 'rgba(0, 255, 0, 0.2)',
            }
        } ;
    }
    
    // ------------------- Graph options ---------------------------------------
    function getChartOptions() {
        
        return {
            
            plugins : {
                autocolors:false,
                legend: { display: false },
                annotation: getAnnotationOptions()
            },
            scales : getScaleOptions(),
            animation : getAnimationOptions(),
            transitions : getTransitionsOptions(),
            responsive : true,
            maintainAspectRatio: false
        } ;
    }
    
    function getAnnotationOptions() {
        
        return {
            annotations: {
                line1: {
                    type: 'line',
                    yMin: 70,
                    yMax: 70,
                    borderColor: '#FF7A7A',
                    borderWidth: 1,
                },
                line2: {
                    type: 'line',
                    yMin: 30,
                    yMax: 30,
                    borderColor: '#57A045',
                    borderWidth: 1,
                }
            }
        } ;
    }
    
    function getScaleOptions() {
        return {
            x : {
                display: true,
                type: 'time',
                position: 'left',
                time: { 
                    unit: 'day',
                    displayFormats: {
                       'day': 'DD-MM-YY'
                    } 
                },
                ticks: {
                    display: false,
                    font: {
                        size: 10,
                    }
                },
                grid: {
                    display: false,
                    color: 'rgba(255,255,255,1.0)',
                }
            },
            y : {
                position : 'right',
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
    
} ) ;