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

} ) ;