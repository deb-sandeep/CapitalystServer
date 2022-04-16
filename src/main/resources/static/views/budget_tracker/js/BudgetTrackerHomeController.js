capitalystNgApp.controller( 'BudgetTrackerHomeController', 
    function( $scope, $http ) {
    
    // ---------------- Local variables --------------------------------------
    
    // ---------------- Scope variables --------------------------------------
    $scope.$parent.navBarTitle = "Budget Tracker" ;
    $scope.financialYearChoices = [ 2020, 2021, 2022 ] ;
    
    $scope.financialYear = $scope.financialYearChoices[2] ;
    
    $scope.budgetSpread = null ;

    // -----------------------------------------------------------------------
    // --- [START] Controller initialization ---------------------------------
    console.log( "Loading TxnPivotHomeController" ) ;
    initializeController() ;
    
    // --- [END] Controller initialization -----------------------------------
    
    
    // -----------------------------------------------------------------------
    // --- [START] Scope functions -------------------------------------------
    
    $scope.handleFYChange = function() {
        fetchBudgetSpread() ;
    }

    // --- [END] Scope functions

    // -----------------------------------------------------------------------
    // --- [START] Local functions -------------------------------------------
    
    function initializeController() {
        fetchBudgetSpread() ;
    }
    
    // ------------------- Server comm functions -----------------------------
    
    function fetchBudgetSpread() {
        
        $scope.$emit( 'interactingWithServer', { isStart : true } ) ;
        $http.get( '/Budget/Spread/' + $scope.financialYear )
        .then ( 
            function( response ){
                console.log( response.data ) ;
                $scope.budgetSpread = response.data ;
            }, 
            function(){
                $scope.$parent.addErrorAlert( "Could not fetch budget spread." ) ;
            }
        )
        .finally(function() {
            $scope.$emit( 'interactingWithServer', { isStart : false } ) ;
        }) ;
    }
    
    // ------------------- Server response processors ------------------------
} ) ;