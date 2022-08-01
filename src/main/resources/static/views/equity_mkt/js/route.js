capitalystNgApp.config( function( $routeProvider ) {
    $routeProvider 
    .when( "/", {
        templateUrl : "/views/equity_mkt/route/portfolio/portfolio.html",
        controller : "PortfolioController"
    })
    .when( "/portfolio", {
        templateUrl : "/views/equity_mkt/route/portfolio/portfolio.html",
        controller : "PortfolioController"
    })
});