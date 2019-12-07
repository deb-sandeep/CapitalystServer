capitalystNgApp.controller( 'FixedDepositEditController', function( $scope, $http ) {
    
    // ---------------- Local variables --------------------------------------
    
    // ---------------- Scope variables --------------------------------------
    $scope.accountIndex = -1 ;
    $scope.account = null ;
    $scope.savingAccounts = null ;
    
    // -----------------------------------------------------------------------
    // --- [START] Controller initialization ---------------------------------
    console.log( "Loading FixedDepositEditController" ) ;
    initializeController() ;
    // --- [END] Controller initialization -----------------------------------
    
    // -----------------------------------------------------------------------
    // --- [START] Scope event listeners -------------------------------------
    $scope.$on( 'fixedDepositEditScopeChanged', function( event, args ) {
        console.log( "fixedDepositEditScopeChanged event received." ) ;
        var editScope = $scope.$parent.$parent.editScope ;
        $scope.accountIndex = editScope.index ;
        $scope.account = editScope.account ;
    } ) ;
    
    // --- [START] Scope functions -------------------------------------------
    $scope.saveAccount = function() {
        console.log( "Saving edit." ) ;
        $( '#fixedDepositEditDialog' ).modal( 'hide' ) ;
    }
    
    $scope.cancelEdit = function() {
        console.log( "Discarding edit." ) ;
        resetEditControllerState() ;
        $( '#fixedDepositEditDialog' ).modal( 'hide' ) ;
    }
    // --- [END] Scope functions

    // -----------------------------------------------------------------------
    // --- [START] Local functions -------------------------------------------
    function initializeController() {
        fetchSavingAccounts() ;
        initDatePickers() ;
    }
    
    function resetEditControllerState() {
        $scope.accountIndex = -1 ;
        $scope.account = null ;
    }
    
    function initDatePickers() {
        
        console.log( "Initializing date pickers." ) ;
        
        var dpStart = $( '#opening_date' ) ;
        var dpEnd   = $( '#maturity_date' ) ;
        
        dpStart.datetimepicker({
            format: "MMM / DD / YYYY"
        }) ;
        dpEnd.datetimepicker({
            format: "MMM / DD / YYYY"
        }) ;
        
        dpStart.off( "dp.change" ) ;
        dpEnd.off( "dp.change" ) ;
        
        dpStart.on( "dp.change", function( e ){
            console.log( "Start date = " + e.date ) ;
        }) ;
        dpEnd.on( "dp.change", function( e ){
            console.log( "End date = " + e.date ) ;
        }) ;
    }
    
    // ------------------- Server comm functions -----------------------------
    function fetchSavingAccounts() {
        $http.get( '/Account/SavingAccount' )
        .then ( function( response ){
            $scope.savingAccounts = response.data ;
        })
    }
} ) ;