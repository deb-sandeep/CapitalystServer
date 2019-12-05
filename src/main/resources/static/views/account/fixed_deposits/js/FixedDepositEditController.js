capitalystNgApp.controller( 'FixedDepositEditController', function( $scope, $http ) {
    
    // ---------------- Local variables --------------------------------------
    
    // ---------------- Scope variables --------------------------------------
    $scope.refData = null ;
    $scope.accountIndex = -1 ;
    $scope.account = null ;
    
    // -----------------------------------------------------------------------
    // --- [START] Controller initialization ---------------------------------
    console.log( "Loading AccountEditController" ) ;
    initializeController() ;
    // --- [END] Controller initialization -----------------------------------
    
    // -----------------------------------------------------------------------
    // --- [START] Scope event listeners -------------------------------------
    $scope.$on( 'fixedDepositEditScopeChanged', function( event, args ) {
        console.log( "fixedDepositEditScopeChanged event received." ) ;
        var editScope = $scope.$parent.$parent.editScope ;
        $scope.accountIndex = editScope.index ;
        $scope.account = editScope.account ;
    } ) ;
    
    // --- [START] Scope functions -------------------------------------------
    $scope.saveAccount = function() {
        console.log( "Saving edit." ) ;
        $( '#fixedDepositEditDialog' ).modal( 'hide' ) ;
    }
    
    $scope.cancelEdit = function() {
        console.log( "Discarding edit." ) ;
        resetEditControllerState() ;
        $( '#fixedDepositEditDialog' ).modal( 'hide' ) ;
    }
    // --- [END] Scope functions

    // -----------------------------------------------------------------------
    // --- [START] Local functions -------------------------------------------
    function initializeController() {
        loadRefData() ;
    }
    
    function resetEditControllerState() {
        $scope.accountIndex = -1 ;
        $scope.account = null ;
    }
    
    // ------------------- Server comm functions -----------------------------
    function loadRefData() {
        $scope.$emit( 'interactingWithServer', { isStart : true } ) ;
        $http.get( '/RefData' )
        .then ( 
            function( response ){
                $scope.refData = response.data ;
            }, 
            function( error ){
                $scope.$parent.addErrorAlert( "Error fetch RefData." ) ;
            }
        )
        .finally(function() {
            $scope.$emit( 'interactingWithServer', { isStart : false } ) ;
        }) ;
    }
} ) ;