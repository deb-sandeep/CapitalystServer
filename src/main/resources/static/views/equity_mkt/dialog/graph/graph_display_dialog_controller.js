capitalystNgApp.controller( 'GraphDisplayDialogController', 
                            function( $scope, $http ) {
    
    const BUY_COLOR            = '#256BEF' ;
    const SELL_COLOR           = '#E7871C' ;
    const EOD_LINE_COLOR       = '#B5B7B5' ;
    const EOD_LINE_COLOR_GREEN = '#93DA91' ;
    const EOD_LINE_COLOR_RED   = '#FDA4A4' ;
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
    $scope.durationKeys = [ '3y', '2y', '1y', '6m', '3m', '2m', '1m' ] ;
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
    
    $scope.getAmtClass = function( value ) {
        return ( value < 0 ) ? "neg_amt" : "pos_amt" ;
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
        if( $scope.chartData.avgData.length > 0 ) {
            cmpColor = ( $scope.chartData.avgData[0].y < $scope.chartData.cmpData[0].y ) ? 
                       '#00FF00' : '#FF0000' ; 
        }
        
        var eodLineColor = EOD_LINE_COLOR_RED ;
        if( $scope.chartData.eodPriceList[0] < $scope.chartData.eodPriceList.at(-1) ) {
            eodLineColor = EOD_LINE_COLOR_GREEN ;
        }
        
        const data = {
          labels: $scope.chartData.labels,
          datasets: [
              {
                type             : 'scatter',
                data             : $scope.chartData.buyData,
                backgroundColor  : BUY_COLOR,
                borderColor      : BUY_COLOR,
                pointRadius      : getPointRadiusArray( $scope.chartData.buyData ),
              },
              {
                type             : 'scatter',
                data             : $scope.chartData.sellData,
                backgroundColor  : SELL_COLOR,
                borderColor      : SELL_COLOR,
                radius           : getPointRadiusArray( $scope.chartData.sellData ),
              },
              {
                type             : 'scatter',
                data             : $scope.chartData.avgData,
                backgroundColor  : AVG_LINE_COLOR,
                borderColor      : AVG_LINE_COLOR,
                radius           : 5,
                borderWidth      : 1,
                showLine         : true,
              },
              {
                type             : 'line',
                data             : $scope.chartData.eodPriceList,
                borderColor      : eodLineColor,
                backgroundColor  : eodLineColor,
                borderWidth      : 1,
                tension          : 0,
                radius           : 0,
              },
              {
                type             : 'scatter',
                data             : $scope.chartData.cmpData,
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
    
    function caculateMovingAverage( data, window ) {
        
        var sum = 0;
        var result = [] ;
        
        if( data.length < window ){ return result; }
        
        for( var i=0; i<window; ++i ) {
            sum += data[i] ;
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
                drawChart() ;
            }
        )
        .finally(function() {
            $scope.$emit( 'interactingWithServer', { isStart : false } ) ;
            $scope.inbetweenServerCall = false ;
        }) ;
    }
} ) ;