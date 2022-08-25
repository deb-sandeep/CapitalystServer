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
    .when( "/profitloss", {
        templateUrl : "/views/equity_mkt/route/profitloss/profitloss.html",
        controller : "ProfitLossController"
    })
    .when( "/buy", {
        templateUrl : "/views/equity_mkt/route/buy/buy.html",
        controller : "BuyController"
    })
    .when( "/reco", {
        templateUrl : "/views/equity_mkt/route/reco/reco.html",
        controller : "RecoController"
    })
});