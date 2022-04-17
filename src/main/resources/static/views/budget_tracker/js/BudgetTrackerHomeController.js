capitalystNgApp.controller( 'BudgetTrackerHomeController', 
    function( $scope, $http ) {
    
    // ---------------- Local variables --------------------------------------
    
    var tableRenderer = new BudgetTableRenderer() ;
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

    $scope.showLedgerEntriesDialog = function( cat ) {
        $( '#viewLedgerEntriesDialog' ).modal( 'show' ) ;
    }
    
    $scope.hideLedgerEntriesDialog = function() {
        $scope.ledgerEntriesForDisplay.length = 0 ;
        $( '#viewLedgerEntriesDialog' ).modal( 'hide' ) ;
    }
    
    $scope.triggerLedgerEntryDisplay = function( id, startOfMonth ) {
        console.log( id + ", " + startOfMonth ) ;
    }

    $scope.$on( 'ledgerEntryDisplayTrigger', function( event, args ) {
        console.log( "In home controller. Id = " + args.categoryId + 
                     ", startDate = " + args.startOfMonth ) ;
    } ) ;

    // --- [END] Scope functions

    // -----------------------------------------------------------------------
    // --- [START] Local functions -------------------------------------------
    
    function initializeController() {
        fetchBudgetSpread() ;
    }
    
    // ------------------- Server comm functions -----------------------------
    
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