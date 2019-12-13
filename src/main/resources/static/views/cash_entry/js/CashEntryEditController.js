capitalystNgApp.controller( 'CashEntryEditController', 
    function( $scope, $http, $location, editIntent, $ngConfirm ) {
    
    // ---------------- Local variables --------------------------------------
    
    // ---------------- Scope variables --------------------------------------
    $scope.editEntry = null ;
    $scope.relevantCategoriesForSelectedEntries = null ;
    
    // -----------------------------------------------------------------------
    // --- [START] Controller initialization ---------------------------------
    console.log( "Loading CashEntryEditController" ) ;
    initializeController() ;
    // --- [END] Controller initialization -----------------------------------
    
    
    // -----------------------------------------------------------------------
    // --- [START] Scope functions -------------------------------------------
    $scope.cancelCashEntry = function() {
        editIntent.editEntryIndex = null ;
        editIntent.editEntry = null ;
        $location.path( "/" ) ;
    }
    
    $scope.$watch( 'editEntry.amount', function(){
        if( $scope.editEntry != null ) {
            $scope.relevantCategoriesForSelectedEntries = 
                                    ( $scope.editEntry.amount < 0 ) ?
                                    $scope.$parent.masterCategories.debit :
                                    $scope.$parent.masterCategories.credit ;
        }
    }) ;
    
    $scope.saveCashEntry = function() {
        var validationMsg = validateInputs() ;
        if( validationMsg != null ) {
            $ngConfirm( validationMsg ) ;
        }
        else {
            console.log( "Input is valid. Saving cash entry." ) ;
        }
    }
    // --- [END] Scope functions

    // -----------------------------------------------------------------------
    // --- [START] Local functions -------------------------------------------
    
    function validateInputs() {
        
        var entry = $scope.editEntry ;
        
        if( entry.valueDate == null ) {
            return "Transaction date must be specified." ;
        }
        
        var tomorrow = moment().add( 1, 'days' ).startOf( 'day' ).toDate() ;
        if( entry.valueDate >= tomorrow ) {
            return "Transaction date can't be in the future." ;
        }
        
        if( entry.amount == null || entry.amount == 0 ) {
            return "Please enter a valid amount." ;
        }
        
        if( entry.l1Cat == null ) {
            return "Please provide valid categorization" ;
        }
        
        if( entry.l2Cat == null ) {
            return "Please provide valid subcategorization" ;
        }
        
        return null ;
    }
    
    function initializeController() {
        if( editIntent.editEntry == null ) {
            $location.path( "/" ) ;
        }
        else {
            $scope.editEntry = editIntent.editEntry ;
            initDatePicker() ;
        }
    }
    
    function initDatePicker() {
        
        var ceDateCtl = $( '#ceDate' ) ;
        
        ceDateCtl.off( "dp.change" ) ;
        ceDateCtl.datetimepicker({
            format: "MMM / DD / YYYY",
            focusOnShow:false
        }) ;
        
        ceDateCtl.on( "dp.change", function( e ){
            $scope.editEntry.valueDate = e.date.toDate() ;
            $( '#ceAmt' ).focus() ;
        }) ;
        
        ceDateCtl.data( "DateTimePicker" )
                 .date( $scope.editEntry.valueDate ) ;
    }
    
    // ------------------- Server comm functions -----------------------------
} ) ;