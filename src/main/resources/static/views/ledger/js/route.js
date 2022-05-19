capitalystNgApp.config( function( $routeProvider ) {
    $routeProvider 
    .when( "/", {
        templateUrl : "/views/ledger/template/LedgerHome.html",
        controller : "LedgerHomeController"
    })
});