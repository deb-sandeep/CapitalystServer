capitalystNgApp.config( function( $routeProvider ) {
    $routeProvider 
    .when( "/", {
        templateUrl : "/views/account/template/AccountHome.html",
        controller : "AccountHomeController"
    })
});