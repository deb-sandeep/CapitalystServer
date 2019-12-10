capitalystNgApp.config( function( $routeProvider ) {
    $routeProvider 
    .when( "/", {
        templateUrl : "/views/cash_entry/template/CashEntryHome.html",
        controller : "CashEntryHomeController"
    })
});