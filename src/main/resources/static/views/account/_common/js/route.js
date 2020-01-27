capitalystNgApp.config( function( $routeProvider ) {
    $routeProvider 
    .when( "/", {
        templateUrl : "/views/account/savings/SavingAccounts.html",
        controller : "SavingAccountsController"
    })
    .when( "/savingAccounts", {
        templateUrl : "/views/account/savings/SavingAccounts.html",
        controller : "SavingAccountsController"
    })
    .when( "/fixedDeposits", {
        templateUrl : "/views/account/fixed_deposits/FixedDeposits.html",
        controller : "FixedDepositsController"
    })
    .when( "/mutualFunds", {
        templateUrl : "/views/account/mf/MutualFunds.html",
        controller : "MutualFundsController"
    })
    .when( "/equityHoldings", {
        templateUrl : "/views/account/equity/Equity.html",
        controller : "EquityController"
    })
});