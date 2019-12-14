function LocalState() {
    
    this.user = "Sandeep" ;
    
    this.serialize = function() {
        $.cookie.json = true ;
        $.cookie( 'ceLocalState', this, { expires: 30 } ) ;
    }

    this.deserialize = function() {
        $.cookie.json = true ;
        var state = $.cookie( 'ceLocalState' ) ;
        if( typeof state != 'undefined' ) {
            this.user = state.user ;
        } ;
    }
}

capitalystNgApp.controller( 'CashEntryEditController', 
    function( $scope, $http, $location, editIntent, $ngConfirm ) {
    
    // ---------------- Local variables --------------------------------------
    var localState = new LocalState() ;
    
    // ---------------- Scope variables --------------------------------------
    $scope.individuals = [ "Sandeep", "Sreerekha" ] ;
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
                                    $scope.$parent.masterCategories.credit :
                                    $scope.$parent.masterCategories.debit ;
        }
    }) ;
    
    $scope.saveCashEntry = function() {
        var validationMsg = validateInputs() ;
        if( validationMsg != null ) {
            $ngConfirm( validationMsg ) ;
        }
        else {
            console.log( "Input is valid. Saving cash entry." ) ;
            saveEntryOnServer( function(){
                $location.path( "/" ) ;
            }) ;
        }
    }
    // --- [END] Scope functions

    // -----------------------------------------------------------------------
    // --- [START] Local functions -------------------------------------------
    
    function initializeController() {
        if( editIntent.editEntry == null ) {
            $location.path( "/" ) ;
        }
        else {
            localState.deserialize() ;
            $scope.editEntry = editIntent.editEntry ;
            
            if( $scope.editEntry.notes == null ) {
                $scope.editEntry.notes = localState.user ;
            }
            
            if( $scope.editEntry.l1Cat == null ) {
                $scope.editEntry.l1Cat = localState.l1Cat ;
            }
            
            if( $scope.editEntry.l2Cat == null ) {
                $scope.editEntry.l2Cat = localState.l2Cat ;
            }
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
    
    function validateInputs() {
        
        var entry = $scope.editEntry ;
        
        if( entry.notes == null || entry.notes == "" ) {
            return "Please specify the transaction initiator." ;
        }
        
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
    
    // ------------------- Server comm functions -----------------------------
    function saveEntryOnServer( callback )  {
        
        localState.user = $scope.editEntry.notes ;
        localState.serialize() ;

        $scope.$emit( 'interactingWithServer', { isStart : true } ) ;
        $http.post( '/Ledger/CashAtHome', $scope.editEntry )
        .then ( 
            function( response ){
                console.log( "Entry saved successfully." ) ;
                response.data.amount *= -1 ;
                $scope.editEntry = response.data ;
                editIntent.editEntry = response.data ;
                
                callback() ;
            }, 
            function( error ){
                if( error.status == 409 ) {
                    $ngConfirm( "Similar entry exists. Edit existing entry." ) ;
                }
            }
        )
        .finally(function() {
            $scope.$emit( 'interactingWithServer', { isStart : false } ) ;
        }) ;
    }    
} ) ;