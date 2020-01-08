capitalystNgApp.controller( 'InfoEditController', 
            function( $scope, $http, $ngConfirm ) {
    
    // ---------------- Local variables --------------------------------------
    
    // ---------------- Scope variables --------------------------------------
    $scope.holdingIndex = -1 ;
    $scope.holding = null ;
    
    // -----------------------------------------------------------------------
    // --- [START] Controller initialization ---------------------------------
    console.log( "Loading MF InfoEditController" ) ;
    initializeController() ;
    // --- [END] Controller initialization -----------------------------------
    
    // -----------------------------------------------------------------------
    // --- [START] Scope event listeners -------------------------------------
    $scope.$on( 'mfHoldingEditScopeChanged', function( event, args ) {
        console.log( "MF edit scope changed." ) ;
        var editScope = $scope.$parent.$parent.editScope ;
        $scope.holdingIndex = editScope.index ;
        $scope.holding = editScope.holding ;
    } ) ;
    
    // --- [START] Scope functions -------------------------------------------
    $scope.updateHolding = function() {
        console.log( "Saving edit." ) ;
        if( isUserInputValid() ) {
            updateHolding( function() {
                var holdings = $scope.$parent.$parent.mfHoldings ; 
                var holding = holdings[ $scope.holdingIndex ] ;
                
                holding.isin = $scope.holding.isin ;
                holding.url = $scope.holding.url ;
                
                resetEditControllerState() ;
                
                $( '#mfEditInfoDialog' ).modal( 'hide' ) ;
            } ) ;
        }
        else {
            $ngConfirm( 'Invalid input. Cant be saved.' ) ;
        }
    }
    
    $scope.cancelEdit = function() {
        console.log( "Discarding edit." ) ;
        resetEditControllerState() ;
        $( '#mfEditInfoDialog' ).modal( 'hide' ) ;
    }
    // --- [END] Scope functions

    // -----------------------------------------------------------------------
    // --- [START] Local functions -------------------------------------------
    function initializeController() {
    }
    
    function resetEditControllerState() {
        $scope.holdingIndex = -1 ;
        $scope.holding = null ;
    }
    
    function isUserInputValid() {
        if( isNotEmptyOrNull( $scope.holding.isin ) ) {
            if( isNotEmptyOrNull( $scope.holding.url ) ) {
                return true ;
            }
        }
        return false ;
    }
    
    function isNotEmptyOrNull( input ) {
        return !( input == null || input.trim() == "" ) ;
    }
    
    // ------------------- Server comm functions -----------------------------
    function updateHolding( successCallback ) {
        $scope.$emit( 'interactingWithServer', { isStart : true } ) ;
        $http.post( '/MutualFund/InfoUpdate', {
           id : $scope.holding.assetId,
           isin : $scope.holding.isin,
           url : $scope.holding.url
        } )
        .then ( 
            function( response ){
                console.log( "MF successfully updated" ) ;
                successCallback() ;
            }, 
            function( error ){
                $scope.$parent.addErrorAlert( "Error fupdating MF info" ) ;
            }
        )
        .finally(function() {
            $scope.$emit( 'interactingWithServer', { isStart : false } ) ;
        }) ;
    }
} ) ;