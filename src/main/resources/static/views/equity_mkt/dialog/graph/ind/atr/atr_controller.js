capitalystNgApp.controller( 'ATRController', 
                            function( $scope, $http ) {
    
    // ---------------- Constants ----------------------------------------------
    const CHART_ID = "atr" ;
    const ATR_LINE_DS_ID = "atr-line" ;
    
    // ---------------- Local variables ----------------------------------------
    var chart = null ;
    var datasets = [] ;
    var chartOptions = null ;
    
    var atrLineData = null ;
    
    function resetState() {
        
        if( chart != null ) {
            chart.destroy() ;
        }

        datasets.length = 0 ;
        atrLineData = null ;
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
        
        if( $scope.$parent.isFooterChartVisible( CHART_ID ) ) {
            $scope.showATRChart() ;
        }
    } ) ;

    $scope.enableFlagUpdated = function() {
        if( $scope.config.enableFlag ) {
            $scope.showATRChart() ;
        }
        else {
            $scope.hideATRChart() ;
        }
    }
    
    $scope.hideATRChart = function() {
        
        $scope.$parent.hideFooterChart( CHART_ID ) ;
        $scope.config.enableFlag = false ;
        
        if( chart != null ) {
            chart.destroy() ;
            datasets.length = 0 ;
            atrLineData = null ;
        }
    }
    
    $scope.showATRChart = function() {
        
        const symbol = $scope.$parent.graphParams.symbolNse ;
        
        $http.get( '/Equity/GraphData/Indicator/ATR' + 
                   '?symbolNse=' + encodeURIComponent( symbol ) +
                   '&windowSize=' + $scope.config.windowSize )
        .then ( 
            function( response ){
                
                atrLineData = response.data[ ATR_LINE_DS_ID ] ;
                
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
        
        addDataset( getATRLineDataset() ) ;
        
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
        ctx.fillText( "ATR", 60, 10 ) ;
        
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

    function getATRLineDataset() {
        
        return {
            name             : ATR_LINE_DS_ID,
            type             : 'line',
            data             : atrLineData,
            borderColor      : '#FD8135',
            backgroundColor  : '#FD8135',
            borderWidth      : 1,
            tension          : 0,
            radius           : 0,
            yAxisID          : 'y',
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
                    yMin: 0,
                    yMax: 0,
                    borderColor: 'rgb(150, 150, 150)',
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