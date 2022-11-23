capitalystNgApp.controller( 'MACDController', 
                            function( $scope, $http ) {
    
    // ---------------- Local variables ----------------------------------------
    var chart = null ;
    var datasets = [] ;
    var chartOptions = null ;
    
    var macdLineData = null ;
    var macdSigData  = null ;
    var macdHistData = null ;
    
    var fresh = true ;
    
    function resetState() {
        
        if( chart != null ) {
            chart.destroy() ;
        }

        datasets.length = 0 ;
        macdLineData = null ;
        macdSigData = null ;
        macdHistData = null ;
    }

    // ---------------- Scope variables ----------------------------------------
    $scope.config = {
        minWindowSize : 12,
        maxWindowSize : 26,
        sigWindowSize : 9,
        enableFlag    : false
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
        else if( $scope.$parent.isFooterChartVisible( 'macd') ) {
            $scope.showMACDChart() ;
        }
    } ) ;

    $scope.enableFlagUpdated = function() {
        if( $scope.config.enableFlag ) {
            $scope.showMACDChart() ;
        }
        else {
            $scope.hideMACDChart() ;
        }
    }
    
    $scope.hideMACDChart = function() {
        
        $scope.$parent.hideFooterChart( 'macd' ) ;
        $scope.config.enableFlag = false ;
        
        if( chart != null ) {
            chart.destroy() ;
            datasets.length = 0 ;
            macdLineData = null ;
            macdSigData = null ;
            macdHistData = null ;
        }
    }
    
    $scope.showMACDChart = function() {
        
        const symbol = $scope.$parent.graphParams.symbolNse ;
        
        $http.get( '/Equity/GraphData/Indicator/MACD' + 
                   '?symbolNse=' + symbol +
                   '&minWindowSize=' + $scope.config.minWindowSize +
                   '&maxWindowSize=' + $scope.config.maxWindowSize +
                   '&sigWindowSize=' + $scope.config.sigWindowSize )
        .then ( 
            function( response ){
                
                macdLineData = response.data[ 'macd-line'   ] ;
                macdSigData  = response.data[ 'macd-signal' ] ;
                macdHistData = response.data[ 'macd-hist'   ] ;
                
                $scope.$parent.showFooterChart( 'macd' ) ;
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
        
        addDataset( getMACDLineDataset() ) ;
        addDataset( getMACDSignalDataset() ) ;
        addDataset( getMACDHistDataset()   ) ;
        
        var canvas = document.getElementById( 'macdChartCanvas' ) ;
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
        ctx.fillText( "MACD", 60, 10 ) ;
        
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

    function getMACDLineDataset() {
        
        return {
            name             : 'macd-line',
            type             : 'line',
            data             : macdLineData,
            borderColor      : '#3aaee9',
            backgroundColor  : '#3aaee9',
            borderWidth      : 1,
            tension          : 0,
            radius           : 0,
            yAxisID          : 'y',
        } ;
    }
    
    function getMACDSignalDataset() {
        
        return {
            name             : 'macd-signal',
            type             : 'line',
            data             : macdSigData,
            borderColor      : '#d36b1b',
            backgroundColor  : '#d36b1b',
            borderWidth      : 1,
            tension          : 0,
            radius           : 0,
        } ;
    }
    
    function getMACDHistDataset() {
        
        return {
            name             : 'macd-hist',
            type             : 'line',
            data             : macdHistData,
            radius           : 0,
            borderWidth      : 0,
            fill : {
                target: 'origin',
                above: 'rgba(0, 255, 0, 0.3)',
                below: 'rgba(255, 0, 0, 0.3)'
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