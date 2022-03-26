capitalystNgApp.config( function( $routeProvider ) {
    $routeProvider 
    .when( "/", {
        templateUrl : "/views/admin/template/AdminHome.html",
        controller : "AdminHomeController"
    })
});