capitalystNgApp.controller( 'RecoController', 
    function( $scope, $http ) {
    
    // ---------------- Local variables --------------------------------------
    
    // ---------------- Scope variables --------------------------------------
    $scope.$parent.navBarTitle = "Equity Recommendations" ;
    $scope.$parent.activeModuleId = "reco" ;
    
    function resetState() {
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
                    var txn = response.data[i] ;
                    
                    // Injecting new fields into txn
                }
            }, 
            function(){
                $scope.$parent.addErrorAlert( "Error fetching eq recos." ) ;
            }
        )
        .finally(function() {
            $scope.$emit( 'interactingWithServer', { isStart : false } ) ;
        }) ;
    }
} ) ;