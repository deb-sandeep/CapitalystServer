capitalystNgApp.controller( 'FixedDepositEditController', 
        function( $scope, $http, $ngConfirm ) {
    
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
        if( isUserInputValid() ) {
            saveAccount( function( savedAccount ) {
                var accounts = $scope.$parent.$parent.accounts ; 
                if( $scope.accountIndex == -1 ) {
                    accounts.push( savedAccount ) ;
                }
                else {
                    accounts[ $scope.accountIndex ] = savedAccount ;
                }
                resetEditControllerState() ;
                $( '#fixedDepositEditDialog' ).modal( 'hide' ) ;
            } ) ;
        }
        else {
            $ngConfirm( 'Invalid input. Cant be saved.' ) ;
        }
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
    
    function isUserInputValid() {
        if( isBaseAccountDataValid() ) {
            if( isFDAccountDataValid() ) {
                return true ;
            }
        }
        return false ;
    }
    
    function isBaseAccountDataValid() {
        var account = $scope.account ;
        if( isNotEmptyOrNull( account.baseAccount.accountOwner ) ) {
            if( isNotEmptyOrNull( account.baseAccount.accountType ) ) {
                if( isNotEmptyOrNull( account.baseAccount.bankName ) ) {
                    if( isNotEmptyOrNull( account.baseAccount.bankBranch ) ) {
                        if( isNotEmptyOrNull( account.baseAccount.accountNumber ) ) {
                            if( isNotEmptyOrNull( account.baseAccount.shortName ) ) {
                                return true ;
                            }
                        }
                    }
                }
            }
        }
        return false ;
    }
    
    function isFDAccountDataValid() {
        var account = $scope.account ;
        if( account.baseAccount.balance != null && account.baseAccount.balance >= 0 ) {
            if( account.openAmt != null && account.openAmt >=0 ) {
                if( account.matureAmt != null && account.matureAmt >=0 ) {
                    if( account.interestRate != null && account.matureAmt >=0 ) {
                        return true ;
                    }
                }
            }
        }
        return false ;
    }
    
    function isNotEmptyOrNull( input ) {
        return !( input == null || input.trim() == "" ) ;
    }
    
    // ------------------- Server comm functions -----------------------------
    function fetchSavingAccounts() {
        $http.get( '/Account/SavingAccount' )
        .then ( function( response ){
            $scope.savingAccounts = response.data ;
        })
    }

    function saveAccount( successCallback ) {
        $scope.$emit( 'interactingWithServer', { isStart : true } ) ;
        $http.post( '/Account/FixedDeposit', $scope.account )
        .then ( 
            function( response ){
                console.log( "Saved account data" ) ;
                successCallback( response.data ) ;
            }, 
            function( error ){
                $scope.$parent.addErrorAlert( "Error fetch RefData." ) ;
            }
        )
        .finally(function() {
            $scope.$emit( 'interactingWithServer', { isStart : false } ) ;
        }) ;
    }

} ) ;