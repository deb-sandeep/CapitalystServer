capitalystNgApp.controller( 'GraphDisplayDialogController', 
                            function( $scope, $http ) {
    
    const BUY_COLOR            = '#256BEF' ;
    const SELL_COLOR           = '#E7871C' ;
    const EOD_LINE_COLOR       = '#B5B7B5' ;
    const EOD_LINE_COLOR_GREEN = '#93DA91' ;
    const EOD_LINE_COLOR_RED   = '#FDA4A4' ;
    const AVG_LINE_COLOR       = '#ABABAB' ;
    const SCATTER_POINT_RADIUS = 4 ;
    
    // ---------------- Local variables --------------------------------------
    var chart = null ;
    var chartData = null ;
    
    // ---------------- Scope variables --------------------------------------
    $scope.graphParams = null ;
    $scope.durationKeys = [ '1m', '2m', '3m', '6m', '1y', '2y', '3y' ] ;
    $scope.duration = '3m' ;
    
    // -----------------------------------------------------------------------
    // --- [START] Scope functions -------------------------------------------
    
    $scope.$on( 'graphDialogDisplay', function( _event, args ) {
        $scope.graphParams = args ;
        fetchChartData() ;
    } ) ;
    
    $scope.setDuration = function( newDuration ) {
        if( newDuration != $scope.duration ) {
            $scope.duration = newDuration ;
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
    // --- [END] Scope functions

    // -----------------------------------------------------------------------
    // --- [START] Local functions -------------------------------------------
    
    function drawChart() {
        
        if( chart != null ) {
            chart.destroy() ;
        }
        
        $( '#graphDisplayDialog' ).modal( 'show' ) ;

        const ctx = document.getElementById( 'eodGraph' ) ;
        
        var cmpColor = '#000000' ;
        if( chartData.avgData.length > 0 ) {
            cmpColor = ( chartData.avgData[0].y < chartData.cmpData[0].y ) ? 
                       '#00FF00' : '#FF0000' ; 
        }
        
        var eodLineColor = EOD_LINE_COLOR_RED ;
        if( chartData.eodPriceList[0] < chartData.eodPriceList.at(-1) ) {
            eodLineColor = EOD_LINE_COLOR_GREEN ;
        }
        
        const data = {
          labels: chartData.labels,
          datasets: [
              {
                type             : 'scatter',
                data             : chartData.buyData,
                backgroundColor  : BUY_COLOR,
                borderColor      : BUY_COLOR,
                radius           : SCATTER_POINT_RADIUS,
              },
              {
                type             : 'scatter',
                data             : chartData.sellData,
                backgroundColor  : SELL_COLOR,
                borderColor      : SELL_COLOR,
                radius           : SCATTER_POINT_RADIUS,
              },
              {
                type             : 'scatter',
                data             : chartData.avgData,
                backgroundColor  : AVG_LINE_COLOR,
                borderColor      : AVG_LINE_COLOR,
                radius           : 5,
                borderWidth      : 1,
                showLine         : true,
              },
              {
                type             : 'line',
                data             : chartData.eodPriceList,
                borderColor      : eodLineColor,
                backgroundColor  : eodLineColor,
                borderWidth      : 1,
                tension          : 0,
                radius           : 0,
              },
              {
                type             : 'scatter',
                data             : chartData.cmpData,
                borderColor      : cmpColor,
                backgroundColor  : cmpColor,
                radius           : 4,
              },
            ]
        } ;
        
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
            }
        } ;
        
        chart = new Chart( ctx, {
            data: data,
            options: options
        } ) ;
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
            return "Avg price = " + context.formattedValue ;
        }
        return "Price = " + context.formattedValue ;
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
                chartData = response.data ;
                drawChart() ;
            }
        )
        .finally(function() {
            $scope.$emit( 'interactingWithServer', { isStart : false } ) ;
            $scope.inbetweenServerCall = false ;
        }) ;
    }
} ) ;