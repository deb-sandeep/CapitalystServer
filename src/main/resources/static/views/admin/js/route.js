capitalystNgApp.config( function( $routeProvider ) {
    $routeProvider 
    .when( "/", {
        templateUrl : "/views/admin/template/manage_ledger_categories.html",
        controller : "ManageLedgerCategoriesController"
    })
    .when( "/manage_ledger_categories", {
        templateUrl : "/views/admin/template/manage_ledger_categories.html",
        controller : "ManageLedgerCategoriesController"
    })
});