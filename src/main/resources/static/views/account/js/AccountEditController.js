capitalystNgApp.controller( 'AccountEditController', function( $scope, $http ) {
    
    // ---------------- Local variables --------------------------------------
    
    // ---------------- Scope variables --------------------------------------
    $scope.accountIndex = -1 ;
    $scope.account = null ;
    
    // -----------------------------------------------------------------------
    // --- [START] Controller initialization ---------------------------------
    console.log( "Loading AccountEditController" ) ;
    // --- [END] Controller initialization -----------------------------------
    
    // -----------------------------------------------------------------------
    // --- [START] Scope event listeners -------------------------------------
    $scope.$on( 'editScopeChanged', function( event, args ) {
        var editScope = $scope.$parent.$parent.editScope ;
        $scope.accountIndex = editScope.index ;
        $scope.account = editScope.account ;
    } ) ;
    
    // --- [START] Scope functions -------------------------------------------
    $scope.saveAccount = function() {
        console.log( "Saving edit." ) ;
        $( '#editAccountDialog' ).modal( 'hide' ) ;
    }
    
    $scope.cancelEdit = function() {
        console.log( "Discarding edit." ) ;
        $( '#editAccountDialog' ).modal( 'hide' ) ;
    }
    // --- [END] Scope functions

    // -----------------------------------------------------------------------
    // --- [START] Local functions -------------------------------------------
    // ------------------- Server comm functions -----------------------------
} ) ;