capitalystNgApp.config( function( $routeProvider ) {
    $routeProvider 
    .when( "/", {
        templateUrl : "/views/cash_entry/template/CashEntryHome.html",
        controller : "CashEntryHomeController"
    })
    .when( "/editEntry", {
        templateUrl : "/views/cash_entry/template/CashEntryEdit.html",
        controller : "CashEntryEditController"
    })
});