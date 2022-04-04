capitalystNgApp.config( function( $routeProvider ) {
    $routeProvider 
    .when( "/", {
        templateUrl : "/views/admin/template/categories/manage_ledger_categories.html",
        controller : "ManageLedgerCategoriesController"
    })
    .when( "/manage_ledger_categories", {
        templateUrl : "/views/admin/template/categories/manage_ledger_categories.html",
        controller : "ManageLedgerCategoriesController"
    })
    .when( "/manage_classification_rules", {
        templateUrl : "/views/admin/template/rules/manage_classification_rules.html",
        controller : "ManageClassificationRulesController"
    })
});