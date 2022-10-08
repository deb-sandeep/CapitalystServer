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
    $scope.smaGraphs = {
       d5 : {
          window : 5,
          enabled : true,
          color : '#008080',
          dash : [1,4] 
       },
       d10 : {
          window : 10,
          enabled : true,
          color : '#FF6347',
          dash : [1,4] 
       },
       d20 : {
          window : 20,
          enabled : false,
          color : '#008B8B',
          dash : [1,4] 
       },
       d50 : {
          window : 50,
          enabled : false,
          color : '#483D8B',
          dash : [1,4] 
       },
       d100 : {
          window : 100,
          enabled : false,
          color : '#DAA520',
          dash : [1,4] 
       },
       d200 : {
          window : 200,
          enabled : false,
          color : '#20B2AA',
          dash : [1,4] 
       },
    } ;
    
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
    
    $scope.smaGraphOptionsChanged = function() {
        console.log( $scope.smaGraphs ) ;
    }
    
    $scope.smaGraphOptionsChanged = function() {
        drawChart() ;
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
            borderWidth      : 1,
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
    
    function getSMADataset( datasets ) {
        
        var eodPriceList = $scope.chartData.eodPriceList ;
        
        for( const smaKey in $scope.smaGraphs ) {
            
            const smaCfg = $scope.smaGraphs[smaKey] ;
            
            if( !smaCfg.enabled ) { continue ; }
                
            var smaValues = calculateSMA( eodPriceList, smaCfg.window ) ;
            
            if( smaValues.length == 0 ) { continue ; }
            
            while( smaValues.length != eodPriceList.length ) {
                smaValues.unshift( null ) ;
            }
            
            datasets.push( {
                type             : 'line',
                data             : smaValues,
                borderColor      : smaCfg.color ,
                backgroundColor  : smaCfg.color,
                borderWidth      : 2,
                tension          : 0.25,
                radius           : 0,
                borderDash       : smaCfg.dash
            } ) ;
        }
    }
    
    function drawChart() {
        
        $( '#graphDisplayDialog' ).modal( 'show' ) ;
        
        if( chart != null ) { chart.destroy() ; }

        const ctx = document.getElementById( 'eodGraph' ) ;
        var datasets = [] ;
        
        datasets.push( getBuyTradesDataset()  ) ;
        datasets.push( getSellTradesDataset() ) ;
        datasets.push( getAvgPriceDataset()   ) ;
        datasets.push( getEodPriceDataset()   ) ;
        datasets.push( getCMPDataset()        ) ;
        
        getSMADataset( datasets ) ;
        
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