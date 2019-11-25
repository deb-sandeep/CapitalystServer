capitalystNgApp.config( function( $routeProvider ) {
    $routeProvider 
    .when( "/", {
        templateUrl : "/views/ledger/template/home/LedgerHome.html",
        controller : "LedgerHomeController"
    })
});