capitalystNgApp.controller( 'RecoController', 
    function( $scope, $http ) {
    
    // ---------------- Local variables --------------------------------------
    var sortDir = {
        health : 'asc',
    } ;
    
    // ---------------- Scope variables --------------------------------------
    $scope.$parent.navBarTitle = "Equity Recommendations" ;
    $scope.$parent.activeModuleId = "reco" ;
    
    $scope.recommendations = [] ;
    
    function resetState() {
        $scope.recommendations.length = 0 ;
    } ;
    
    // -----------------------------------------------------------------------
    // --- [START] Controller initialization ---------------------------------
    console.log( "Loading BuyController" ) ;
    fetchRecoDataFromServer() ;
    
    // --- [END] Controller initialization -----------------------------------
    
    // -----------------------------------------------------------------------
    // --- [START] Scope functions -------------------------------------------
    $scope.getAmtClass = function( value ) {
        return ( value < 0 ) ? "neg_amt" : "pos_amt" ;
    }
    
    $scope.getRowClass = function( row ) {
        return  "" ;
    }
    
    $scope.sort = function( field ) {
        sortTable( field ) ;
    }
    
    // --- [END] Scope functions

    // -----------------------------------------------------------------------
    // --- [START] Local functions -------------------------------------------
    
    // ------------------- Server comm functions -----------------------------
    function fetchRecoDataFromServer() {
        
        $scope.$emit( 'interactingWithServer', { isStart : true } ) ;
        
        $http.get( '/Equity/Recommendations' )
        .then ( 
            function( response ){
                console.log( response.data ) ;
                resetState() ;
                for( var i=0; i<response.data.length; i++ ) {
                    var reco = response.data[i] ;
                    $scope.recommendations.push( reco ) ;
                }
                
                sortTable( "health" ) ;
            }, 
            function(){
                $scope.$parent.addErrorAlert( "Error fetching eq recos." ) ;
            }
        )
        .finally(function() {
            $scope.$emit( 'interactingWithServer', { isStart : false } ) ;
        }) ;
    }
    
    function sortTable( field ) {
        
        var dir = sortDir[ field ] ;
        var newDir = ( dir == "asc" ) ? "desc" : "asc" ;
        sortDir[ field ] = newDir ; 
        
        if( field == "health" ) {
            $scope.recommendations.sort( healthSort ) ;
        }
        else if( field == "symbol" ) {
            $scope.recommendations.sort( symbolSort ) ;
        }
        else if( field == "mktcap" ) {
            $scope.recommendations.sort( mktcapSort ) ;
        }
        else if( field == "beta" ) {
            $scope.recommendations.sort( betaSort ) ;
        }
        else if( field == "cagr" ) {
            $scope.recommendations.sort( cagrSort ) ;
        }
        else if( field == "mc" ) {
            $scope.recommendations.sort( mcSort ) ;
        }
        else if( field == "pe" ) {
            $scope.recommendations.sort( peSort ) ;
        }
        else if( field == "pscore" ) {
            $scope.recommendations.sort( pscoreSort ) ;
        }    
    }
    
    function healthSort( r1, r2 ) {
        return sortDir[ "health"] == "asc" ?
            ( r1.goodnessScore - r2.goodnessScore ) :
            ( r2.goodnessScore - r1.goodnessScore ) ;
    }
    
    function symbolSort( r1, r2 ) {
        return sortDir[ "symbol"] == "asc" ?
            ( r1.symbolNse.localeCompare( r2.symbolNse ) ) :
            ( r2.symbolNse.localeCompare( r1.symbolNse ) ) ;
    }
    
    function mktcapSort( r1, r2 ) {
        return sortDir[ "mktcap"] == "asc" ?
            ( r1.indicators.marketCap - r2.indicators.marketCap ) :
            ( r2.indicators.marketCap - r1.indicators.marketCap ) ;
    }
    
    function betaSort( r1, r2 ) {
        return sortDir[ "beta"] == "asc" ?
            ( r1.indicators.beta - r2.indicators.beta ) :
            ( r2.indicators.beta - r1.indicators.beta ) ;
    }
    
    function cagrSort( r1, r2 ) {
        return sortDir[ "cagr"] == "asc" ?
            ( r1.indicators.cagrEbit - r2.indicators.cagrEbit ) :
            ( r2.indicators.cagrEbit - r1.indicators.cagrEbit ) ;
    }
    
    function mcSort( r1, r2 ) {
        return sortDir[ "mc"] == "asc" ?
            ( r1.indicators.mcEssentialScore - r2.indicators.mcEssentialScore ) :
            ( r2.indicators.mcEssentialScore - r1.indicators.mcEssentialScore ) ;
    }
    
    function peSort( r1, r2 ) {
        return sortDir[ "pe"] == "asc" ?
            ( r1.indicators.pe - r2.indicators.pe ) :
            ( r2.indicators.pe - r1.indicators.pe ) ;
    }
    
    function pscoreSort( r1, r2 ) {
        return sortDir[ "pscore"] == "asc" ?
            ( r1.indicators.piotroskiScore - r2.indicators.piotroskiScore ) :
            ( r2.indicators.piotroskiScore - r1.indicators.piotroskiScore ) ;
    }
        
} ) ;