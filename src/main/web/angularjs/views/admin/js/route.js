capitalystNgApp.config( function( $routeProvider ) {
    $routeProvider 
    .when( "/", {
        templateUrl : "/views/admin/route/categories/manage_ledger_categories.html",
        controller : "ManageLedgerCategoriesController"
    })
    .when( "/manage_ledger_categories", {
        templateUrl : "/views/admin/route/categories/manage_ledger_categories.html",
        controller : "ManageLedgerCategoriesController"
    })
    .when( "/manage_classification_rules", {
        templateUrl : "/views/admin/route/rules/manage_classification_rules.html",
        controller : "ManageClassificationRulesController"
    })
    .when( "/onetime_actions", {
        templateUrl : "/views/admin/route/actions/onetime_actions.html",
        controller : "OneTimeActionsController"
    })
    .when( "/manage_idx", {
        templateUrl : "/views/admin/route/idx/manage_index.html",
        controller : "ManageIndexController"
    })
    .when( "/manage_config", {
        templateUrl : "/views/admin/route/config/manage_config.html",
        controller : "ManageConfigController"
    })
    .when( "/breeze_auth", {
        templateUrl : "/views/admin/route/breeze/breeze_auth.html",
        controller : "BreezeAuthController"
    })
});