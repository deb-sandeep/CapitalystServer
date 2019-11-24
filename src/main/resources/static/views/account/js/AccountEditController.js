capitalystNgApp.controller( 'AccountEditController', function( $scope, $http ) {
    
    // ---------------- Local variables --------------------------------------
    
    // ---------------- Scope variables --------------------------------------
    $scope.refData = null ;
    $scope.accountIndex = -1 ;
    $scope.account = null ;
    
    // -----------------------------------------------------------------------
    // --- [START] Controller initialization ---------------------------------
    console.log( "Loading AccountEditController" ) ;
    initializeController() ;
    // --- [END] Controller initialization -----------------------------------
    
    // -----------------------------------------------------------------------
    // --- [START] Scope event listeners -------------------------------------
    $scope.$on( 'editScopeChanged', function( event, args ) {
        var editScope = $scope.$parent.$parent.editScope ;
        $scope.accountIndex = editScope.index ;
        $scope.account = editScope.account ;
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
            } ) ;
        }
        $( '#editAccountDialog' ).modal( 'hide' ) ;
    }
    
    $scope.cancelEdit = function() {
        console.log( "Discarding edit." ) ;
        resetEditControllerState() ;
        $( '#editAccountDialog' ).modal( 'hide' ) ;
    }
    // --- [END] Scope functions

    // -----------------------------------------------------------------------
    // --- [START] Local functions -------------------------------------------
    function initializeController() {
        loadRefData() ;
    }
    
    function resetEditControllerState() {
        $scope.accountIndex = -1 ;
        $scope.account = null ;
    }
    
    function isUserInputValid() {
        var account = $scope.account ;
        if( isNotEmptyOrNull( account.accountOwner ) ) {
            if( isNotEmptyOrNull( account.accountType ) ) {
                if( isNotEmptyOrNull( account.bankName ) ) {
                    if( isNotEmptyOrNull( account.bankBranch ) ) {
                        if( isNotEmptyOrNull( account.accountNumber ) ) {
                            if( isNotEmptyOrNull( account.shortName ) ) {
                                return true ;
                            }
                        }
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
    function loadRefData() {
        $scope.$emit( 'interactingWithServer', { isStart : true } ) ;
        $http.get( '/RefData' )
        .then ( 
            function( response ){
                $scope.refData = response.data ;
            }, 
            function( error ){
                $scope.$parent.addErrorAlert( "Error fetch RefData." ) ;
            }
        )
        .finally(function() {
            $scope.$emit( 'interactingWithServer', { isStart : false } ) ;
        }) ;
    }

    function saveAccount( successCallback ) {
        $scope.$emit( 'interactingWithServer', { isStart : true } ) ;
        $http.post( '/Account', $scope.account )
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