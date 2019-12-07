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

        prePopulateDatePicker( "opening_date", $scope.account.openDate ) ;
        prePopulateDatePicker( "maturity_date", $scope.account.matureDate ) ;
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
        
        var openDtControl = $( '#opening_date' ) ;
        var matureDtControl = $( '#maturity_date' ) ;
        
        openDtControl.datetimepicker({
            format: "MMM / DD / YYYY"
        }) ;
        matureDtControl.datetimepicker({
            format: "MMM / DD / YYYY"
        }) ;
        
        openDtControl.off( "dp.change" ) ;
        matureDtControl.off( "dp.change" ) ;
        
        openDtControl.on( "dp.change", function( e ){
            $scope.account.openDate = e.date.toDate() ;
        }) ;
        matureDtControl.on( "dp.change", function( e ){
            $scope.account.matureDate = e.date.toDate() ;
        }) ;
    }
    
    function prePopulateDatePicker( id, date ) {
        
        var dp = $( '#' + id ) ;
        dp.data( "DateTimePicker" ).date( date ) ;
    }
    
    // ------------------- Server comm functions -----------------------------
    function fetchSavingAccounts() {
        $http.get( '/Account/SavingAccount' )
        .then ( function( response ){
            $scope.savingAccounts = response.data ;
        })
    }
} ) ;