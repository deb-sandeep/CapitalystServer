capitalystNgApp.config( function( $routeProvider ) {
    $routeProvider 
    .when( "/", {
        templateUrl : "/views/txn_pivot/template/TxnPivotHome.html",
        controller : "TxnPivotHomeController"
    })
});