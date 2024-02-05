capitalystNgApp.config( function( $routeProvider ) {
    $routeProvider 
    .when( "/", {
        templateUrl : "/views/budget_tracker/template/BudgetTrackerHome.html",
        controller : "BudgetTrackerHomeController"
    })
});