capitalystNgApp.controller( 'AccountHomeController', 
    function( $scope, $http, $rootScope, $location, $window ) {
    
    // ---------------- Local variables --------------------------------------
    
    // ---------------- Scope variables --------------------------------------
    $scope.$parent.navBarTitle = "Accounts" ;
    $scope.accounts = null ;
    $scope.editScope = {
        index : -1,
        account : null
    } ;
    
    // -----------------------------------------------------------------------
    // --- [START] Controller initialization ---------------------------------
    console.log( "Loading AccountHomeController" ) ;
    initializeController() ;
    // --- [END] Controller initialization -----------------------------------
    
    
    // -----------------------------------------------------------------------
    // --- [START] Scope functions -------------------------------------------
    $scope.showNewAccountDialog = function() {
        broadcastEditScopeChanged( -1, {
            accountNumber: "",
            accountOwner: "",
            accountType: "",
            bankBranch: "",
            bankName: "",
            shortName: "",            
            description: "",
        }) ;
        $( '#editAccountDialog' ).modal( 'show' ) ;
    }
    
    $scope.editAccount = function( index ) {
        var clonedAccount = JSON.parse( JSON.stringify( $scope.accounts[index] ) ) ;
        broadcastEditScopeChanged( index, clonedAccount ) ;
        $( '#editAccountDialog' ).modal( 'show' ) ;
    }
    
    $scope.deleteAccount = function( index ) {
        console.log( "Deleting account at index = " + index ) ;
    }
    
    // --- [END] Scope functions

    // -----------------------------------------------------------------------
    // --- [START] Local functions -------------------------------------------
    
    function initializeController() {
        $scope.editScope = null ;
        fetchAccountSummaryListFromServer() ;
    }
    
    function broadcastEditScopeChanged( index, accountClone ) {
        $scope.editScope = {
            index : index,
            account : accountClone
        } ;
        $scope.$broadcast( 'editScopeChanged', null ) ;
    }
    
    // ------------------- Server comm functions -----------------------------
    function fetchAccountSummaryListFromServer() {
        
        $scope.$parent.interactingWithServer = true ;
        $http.get( '/Account' )
        .then ( 
            function( response ){
                $scope.accounts = response.data ;
            }, 
            function( error ){
                $scope.$parent.addErrorAlert( "Error fetch accounts." ) ;
            }
        )
        .finally(function() {
            $scope.$parent.interactingWithServer = false ;
        }) ;
    }
} ) ;