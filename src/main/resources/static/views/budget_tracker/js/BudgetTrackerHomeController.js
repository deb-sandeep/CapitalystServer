capitalystNgApp.controller( 'BudgetTrackerHomeController', 
    function( $scope, $http ) {
    
    // ---------------- Local variables --------------------------------------
    
    var tableRenderer = new BudgetTableRenderer( $scope.$parent.onMobile ) ;
    var budgetSpread = null ;
    
    var l1Expanded = false ;
    var treeExpanded = false ;
    
    // ---------------- Scope variables --------------------------------------
    $scope.$parent.navBarTitle = "Budget Tracker" ;
    $scope.financialYearChoices = [ 2020, 2021, 2022 ] ;
    
    $scope.userChoices = {
        financialYear : $scope.financialYearChoices[2],
        showPlanned   : false,
        showAvailable : false,
        showConsumed  : true
    }
    
    $scope.ledgerEntriesForDisplay = [] ;
    $scope.l1CatForEntries = null ;
    $scope.l2CatForEntries = null ;
    
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
    
    $scope.refreshTree = function() {
        fetchBudgetSpread() ;
    }
    
    $scope.toggleExpandL1 = function() {
        l1Expanded = !l1Expanded ;
        tableRenderer.changeL1ExpansionState( l1Expanded ) ;
    }
    
    $scope.toggleExpandAll = function() {
        treeExpanded = !treeExpanded ;
        tableRenderer.changeTreeExpansionState( treeExpanded ) ;
    }
    
    $scope.handleRowDisplayOptionsChanges = function() {
        tableRenderer.render( budgetSpread, $scope.userChoices ) ;
    }

    $scope.hideLedgerEntriesDialog = function() {
        $scope.ledgerEntriesForDisplay.length = 0 ;
        $( '#viewLedgerEntriesDialog' ).modal( 'hide' ) ;
    }
    
    $scope.$on( 'ledgerEntryDisplayTrigger', function( _event, args ) {
        
        $scope.l1CatForEntries = args.l1CatName ;
        $scope.l2CatForEntries = args.l2CatName ;
        
        fetchLedgerEntriesForMonth( args.l1CatName, 
                                    args.l2CatName, 
                                    args.startOfMonth ) ;
    } ) ;

    // --- [END] Scope functions

    // -----------------------------------------------------------------------
    // --- [START] Local functions -------------------------------------------
    
    function initializeController() {
        if( $scope.$parent.onMobile ) {
            l1Expanded = true ;
        }
        fetchBudgetSpread() ;
    }
    
    // ------------------- Server comm functions -----------------------------
    
    function fetchLedgerEntriesForMonth( l1CatName, l2CatName, startOfMonth ) {
        
        $scope.ledgerEntriesForDisplay.length = 0 ;
        
        $scope.$emit( 'interactingWithServer', { isStart : true } ) ;
        $http.get( '/Ledger/DebitEntriesForMonth?' +
                   'l1CatName=' + l1CatName + '&' +
                   'l2CatName=' + l2CatName + '&' + 
                   'startOfMonth=' + startOfMonth )
        .then ( 
            function( response ){
                $scope.ledgerEntriesForDisplay = response.data ;
                $( '#viewLedgerEntriesDialog' ).modal( 'show' ) ; 
            }, 
            function( error ){
                $scope.$parent.addErrorAlert( "Could not fetch ledger entries.\n" +
                                              error.data.message ) ;
            }
        )
        .finally(function() {
            $scope.$emit( 'interactingWithServer', { isStart : false } ) ;
        }) ;        
    }
    
    function fetchBudgetSpread() {
        
        $scope.$emit( 'interactingWithServer', { isStart : true } ) ;
        $http.get( '/Budget/Spread/' + $scope.userChoices.financialYear )
        .then ( 
            function( response ){
                console.log( response.data ) ;
                budgetSpread = response.data ;
                $scope.$parent.navBarTitle = response.data.lineItemName ;
                tableRenderer.render( budgetSpread, $scope.userChoices ) ;
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