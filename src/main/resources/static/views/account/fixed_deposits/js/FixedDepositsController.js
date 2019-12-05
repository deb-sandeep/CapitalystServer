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
                "description": null
            },
            "parentAccount": null,
            "openDate": null,
            "matureDate": null,
            "openAmt": 0,
            "matureAmt": 0,
            "autoRenew": false,
            "interestRate": 0,
            "recurringAmt": null,
            "recurringDom": null
        }) ;
        $( '#fixedDepositEditDialog' ).modal( 'show' ) ;
    }

    // -----------------------------------------------------------------------
    // --- [START] Local functions -------------------------------------------
    
    function initializeController() {
        fetchAccountsFromServer() ;
    }
    
    function broadcastEditScopeChanged( index, accountClone ) {
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