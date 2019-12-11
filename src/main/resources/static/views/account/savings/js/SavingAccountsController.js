capitalystNgApp.controller( 'SavingAccountsController', 
    function( $scope, $http, $ngConfirm, $window ) {
    
    // ---------------- Local variables --------------------------------------
    
    // ---------------- Scope variables --------------------------------------
    $scope.$parent.navBarTitle = "Saving, Credit & Current Accounts" ;
    $scope.accounts = null ;
    $scope.totalBalance = 0 ;
    $scope.totalDepositBalance = 0 ;
    
    $scope.editScope = {
        index : -1,
        account : null
    } ;
    
    $scope.stmtUploadAccount = null ;
    
    // -----------------------------------------------------------------------
    // --- [START] Controller initialization ---------------------------------
    console.log( "Loading SavingAccountsController" ) ;
    initializeController() ;
    // --- [END] Controller initialization -----------------------------------
    
    
    // -----------------------------------------------------------------------
    // --- [START] Scope functions -------------------------------------------
    $scope.$on( 'refreshTotals', function( event, args ) {
        fetchAccountsFromServer() ;
    } ) ;
    
    $scope.showNewAccountDialog = function() {
        broadcastEditScopeChanged( -1, {
            accountNumber: null,
            accountOwner: null,
            accountType: null,
            bankBranch: null,
            bankName: null,
            shortName: "",            
            description: "",
        }) ;
        $( '#savingAccountEditDialog' ).modal( 'show' ) ;
    }
    
    $scope.editAccount = function( index ) {
        var clonedAccount = JSON.parse( JSON.stringify( $scope.accounts[index] ) ) ;
        broadcastEditScopeChanged( index, clonedAccount ) ;
        $( '#savingAccountEditDialog' ).modal( 'show' ) ;
    }
    
    $scope.deleteAccount = function( index ) {
        console.log( "Deleting account at index = " + index ) ;
        var account = $scope.accounts[ index ] ;
        
        $ngConfirm({
            title: 'Confirm!',
            content: 'Delete account ' + account.shortName ,
            scope: $scope,
            buttons: {
                close: function(scope, button){
                    console.log( "User cancelled." ) ;
                },
                yes: {
                    text: 'Yes',
                    btnClass: 'btn-blue',
                    action: function(scope, button){
                        console.log( "Ok to delete account." ) ;
                        deleteAccount( account, function() {
                            $scope.accounts.splice( index, 1 ) ;
                        }) ;
                        return true ;
                    }
                }
            }
        });
    }
    
    $scope.viewLedger = function( account ) {
        
        account.selected = true ;
        
        var selAccIds = [] ;
        angular.forEach( $scope.accounts, function( account, key ){
            if( account.selected ) {
                selAccIds.push( account.id ) ;
            }
        }) ;
        
        $window.location.href = "/views/ledger/ledger.html?accountIds=" + 
                                selAccIds.join() ;
    }
    
    $scope.showAccountStmtUploadDialog = function( account ) {
        $scope.stmtUploadAccount = account ;
        $( '#uploadAccountStmtDialog' ).modal( 'show' ) ;
    }
    
    $scope.showConsolidatedLedger = function() {
        
        var selAccIds = [] ;
        angular.forEach( $scope.accounts, function( account, key ){
            selAccIds.push( account.id ) ;
        }) ;
        
        $window.location.href = "/views/ledger/ledger.html?accountIds=" + 
                                selAccIds.join() ;
    }
    
    // --- [END] Scope functions

    // -----------------------------------------------------------------------
    // --- [START] Local functions -------------------------------------------
    
    function initializeController() {
        $scope.$parent.activeTabKey = "SB" ;
        $scope.editScope = null ;
        fetchAccountsFromServer() ;
    }
    
    function broadcastEditScopeChanged( index, accountClone ) {
        $scope.editScope = {
            index : index,
            account : accountClone
        } ;
        $scope.$broadcast( 'savingAccountEditScopeChanged', null ) ;
    }
    
    // ------------------- Server comm functions -----------------------------
    function fetchAccountsFromServer() {
        
        $scope.$emit( 'interactingWithServer', { isStart : true } ) ;
        $http.get( '/Account/SavingAccount' )
        .then ( 
            function( response ){
                $scope.accounts = response.data ;
                angular.forEach( $scope.accounts, function( account, key ){
                    account.selected = false ;
                }) ;
                refreshTotals() ;
            }, 
            function( error ){
                $scope.$parent.addErrorAlert( "Error fetch accounts." ) ;
            }
        )
        .finally(function() {
            $scope.$emit( 'interactingWithServer', { isStart : false } ) ;
        }) ;
    }

    function deleteAccount( account, successCallback ) {
        $scope.$emit( 'interactingWithServer', { isStart : true } ) ;
        $http.delete( '/Account/SavingAccount/' + account.id )
        .then ( 
            function( response ){
                console.log( "Deleted account data" ) ;
                successCallback() ;
            }, 
            function( error ){
                $scope.$parent.addErrorAlert( "Error deleting account." ) ;
            }
        )
        .finally(function() {
            $scope.$emit( 'interactingWithServer', { isStart : false } ) ;
        }) ;
    }
    
    function refreshTotals() {
        $scope.totalBalance = 0 ;
        $scope.totalDepositBalance = 0 ;
        angular.forEach( $scope.accounts, function( account, key ){
            $scope.totalBalance += account.balance ;
            $scope.totalDepositBalance += account.depositBalance ;
        }) ;
    }
} ) ;