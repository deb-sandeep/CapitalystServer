capitalystNgApp.controller( 'FixedDepositsController', 
    function( $scope, $http, $ngConfirm, $window ) {
    
    // ---------------- Local variables --------------------------------------
    
    // ---------------- Scope variables --------------------------------------
    $scope.$parent.navBarTitle = "Fixed Deposits" ;
    $scope.accounts = null ;
    $scope.totalBalance = 0 ;
    
    $scope.editScope = {
        index : -1,
        account : null
    } ;
    
    // -----------------------------------------------------------------------
    // --- [START] Controller initialization ---------------------------------
    console.log( "Loading FixedDepositsController" ) ;
    initializeController() ;
    // --- [END] Controller initialization -----------------------------------
    
    
    // -----------------------------------------------------------------------
    // --- [START] Scope functions -------------------------------------------
    // --- [END] Scope functions
    $scope.showNewFixedDepositDialog = function() {
        broadcastEditScopeChanged( -1, {
            "baseAccount": {
                "accountNumber": null,
                "accountOwner": null,
                "accountType": null,
                "shortName": null,
                "bankName": null,
                "bankBranch": null,
                "description": null,
                "balance" : null
            },
            "parentAccount": null,
            "openDate": null,
            "matureDate": null,
            "openAmt": null,
            "matureAmt": null,
            "autoRenew": false,
            "interestRate": null,
            "recurringAmt": null,
            "recurringDom": null
        }) ;
        $( '#fixedDepositEditDialog' ).modal( 'show' ) ;
    }

    $scope.editAccount = function( index ) {
        var clonedAccount = JSON.parse( JSON.stringify( $scope.accounts[index] ) ) ;
        broadcastEditScopeChanged( index, clonedAccount ) ;
        $( '#fixedDepositEditDialog' ).modal( 'show' ) ;
    }
    
    $scope.duplicateAccount = function( index ) {
        var clonedAccount = JSON.parse( JSON.stringify( $scope.accounts[index] ) ) ;
        broadcastEditScopeChanged( -1, clonedAccount ) ;
        $( '#fixedDepositEditDialog' ).modal( 'show' ) ;
    }
    
    $scope.recomputeTotalBalance = function() {
        $scope.totalBalance = 0 ;
        angular.forEach( $scope.accounts, function( account, key ){
            $scope.totalBalance += account.baseAccount.balance ;
        }) ;
    }
    
    $scope.deleteAccount = function( index ) {
        console.log( "Deleting fixed deposit at index = " + index ) ;
        var account = $scope.accounts[ index ] ;
        
        $ngConfirm({
            title: 'Confirm!',
            content: 'Delete fixed deposit ' + account.baseAccount.accountNumber + 
                     ' for ' + account.baseAccount.accountOwner,
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
                            $scope.totalBalance = 0 ;
                            angular.forEach( $scope.accounts, function( account, key ){
                                $scope.totalBalance += account.baseAccount.balance ;
                            }) ;
                        }) ;
                        return true ;
                    }
                }
            }
        });
    }
    
    // -----------------------------------------------------------------------
    // --- [START] Local functions -------------------------------------------
    
    function initializeController() {
        $scope.$parent.activeTabKey = "FD" ;
        fetchAccountsFromServer() ;
    }
    
    function broadcastEditScopeChanged( index, accountClone ) {
        
        accountClone.openDate = new Date( accountClone.openDate ) ;
        accountClone.matureDate = new Date( accountClone.matureDate ) ;
        
        $scope.editScope = {
            index : index,
            account : accountClone
        } ;
        $scope.$broadcast( 'fixedDepositEditScopeChanged', null ) ;
    }
    
    // ------------------- Server comm functions -----------------------------
    function fetchAccountsFromServer() {
        
        $scope.$emit( 'interactingWithServer', { isStart : true } ) ;
        $http.get( '/Account/FixedDeposit' )
        .then ( 
            function( response ){
                $scope.accounts = response.data ;
                angular.forEach( $scope.accounts, function( account, key ){
                    account.selected = false ;
                    account.openDate = new Date( account.openDate ) ;
                    account.matureDate = new Date( account.matureDate ) ;
                    $scope.totalBalance += account.baseAccount.balance ;
                }) ;
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
        $http.delete( '/Account/FixedDeposit/' + account.id )
        .then ( 
            function( response ){
                console.log( "Deleted fixed deposit data" ) ;
                successCallback() ;
            }, 
            function( error ){
                $scope.$parent.addErrorAlert( "Error deleting fixed deposit." ) ;
            }
        )
        .finally(function() {
            $scope.$emit( 'interactingWithServer', { isStart : false } ) ;
        }) ;
    }
} ) ;