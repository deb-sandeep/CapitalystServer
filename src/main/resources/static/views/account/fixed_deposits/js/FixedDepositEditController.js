capitalystNgApp.controller( 'FixedDepositEditController', function( $scope, $http ) {
    
    // ---------------- Local variables --------------------------------------
    
    // ---------------- Scope variables --------------------------------------
    $scope.accountIndex = -1 ;
    $scope.account = null ;
    $scope.savingAccounts = null ;
    
    // -----------------------------------------------------------------------
    // --- [START] Controller initialization ---------------------------------
    console.log( "Loading FixedDepositEditController" ) ;
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
        fetchSavingAccounts() ;
    }
    
    function resetEditControllerState() {
        $scope.accountIndex = -1 ;
        $scope.account = null ;
    }
    
    // ------------------- Server comm functions -----------------------------
    function fetchSavingAccounts() {
        $http.get( '/Account/SavingAccount' )
        .then ( function( response ){
            $scope.savingAccounts = response.data ;
        })
    }
} ) ;