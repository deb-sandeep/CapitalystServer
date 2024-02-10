capitalystNgApp.controller( 'LedgerHomeController', 
    function( $scope, $http, $rootScope, $location, $window, $ngConfirm ) {
    
    // ---------------- Local variables --------------------------------------
    const selectedEntries = [];
    const pivotSrcColNames = ["Type", "L1", "L2", "Amount"];
    const l1FilterSelections = [];
    const l2FilterSelections = [];
    let pivotSrcData = [];

        // ---------------- Scope variables --------------------------------------
    $scope.$parent.navBarTitle = "Ledger View" ;
    $scope.account = null ;
    
    $scope.searchQuery = {
        accountIds : [],
        startDate : moment().startOf('month'),
        endDate : moment().toDate(),
        minAmt : null,
        maxAmt : null,
        creditClassifier : false,
        l1CatName : null,
        l2CatName : null,
        showOnlyUnclassified : false
    } ;
    $scope.bulkSelState = {
            value : false
    } ;
    
    $scope.ledgerEntries = [] ;
    $scope.masterCategories = {
       credit : {
           l1Categories : [],
           l2Categories : new Map()
       },
       debit : {
           l1Categories : [],
           l2Categories : new Map()
       }
    } ;
    
    $scope.filterCategories = null ;
    
    $scope.relevantCategoriesForSelectedEntries = null ;
    $scope.userSel = {
        l1Cat : null,
        l1CatNew : null,
        l2Cat : null,
        l2CatNew : null,
        saveRule : false,
        ruleName : null,
        notes : null
    } ;
    
    $scope.remarksFilterText = null ;
    $scope.notesFilterText = null ;
    $scope.l1FilterText = null ;
    $scope.l2FilterText = null ;
    $scope.amountFilterText = null ;
    $scope.entryBeingSplit = null ;
    $scope.splitEntryDetails = {
       amount : 0,
       l1Cat : null,
       l1CatNew : null,
       l2Cat : null,
       l2CatNew : null,
       notes : null,
       errMsgs : [],
    } ;
    
    $scope.controlPanelVisible = true ;

    // -----------------------------------------------------------------------
    // --- [START] Controller initialization ---------------------------------
    initializeController() ;
    // --- [END] Controller initialization -----------------------------------
    
    
    // -----------------------------------------------------------------------
    // --- [START] Scope functions -------------------------------------------
    $scope.amtClass = function( amt ) {
        return ( amt < 0 ) ? "debit" : "credit" ; 
    }
    
    $scope.searchLedgerEntries = function() {
        fetchLedgerEntries() ;
    }
    
    $scope.resetSearchCriteria = function() {
        $scope.searchQuery.startDate = moment().subtract(1, 'month').toDate() ;
        $scope.searchQuery.endDate = moment().toDate() ;
        $scope.searchQuery.minAmt = null ;
        $scope.searchQuery.maxAmt = null ;
        $scope.searchQuery.customRule = null ;
        $scope.searchQuery.showOnlyUnclassified = false ;
        $scope.searchQuery.creditClassifier = false ;
        $scope.searchQuery.l1CatName = null ;
        $scope.searchQuery.l2CatName = null ;
        $scope.remarksFilterText = null ;
        
        fetchLedgerEntries() ;
    }
    
    $scope.toggleSelectionForAllEntries = function() {
        for( let i=0; i<$scope.ledgerEntries.length; i++ ) {
            const entry = $scope.ledgerEntries[i] ;
            entry.selected = ( $scope.bulkSelState.value === true ) && entry.visible ;
        }
    }
    
    $scope.selectAndCategorize = function( entry ) {
        
        entry.selected = true ;
        $scope.showCategorizationDialog() ;
    }
    
    $scope.selectEntry = function( entry ) {
        
        entry.selected = !entry.selected ;
        for( let i=0; i<$scope.ledgerEntries.length; i++ ) {
            $scope.ledgerEntries[i].editing = false ;
        }
    }
    
    $scope.showCategorizationDialog = function() {

        const errMsg = validateSelectedEntriesForClassification();

        if( errMsg == null ) {
            $scope.relevantCategoriesForSelectedEntries = 
                                    ( selectedEntries[0].amount < 0 ) ?
                                    $scope.masterCategories.debit :
                                    $scope.masterCategories.credit ;
            $( '#entryCategorizationDialog' ).modal( 'show' ) ;
        }
        else {
            $ngConfirm( errMsg ) ;
        }
    }
    
    $scope.cancelClassification = function() {
        resetClassificationState() ;
        $( '#entryCategorizationDialog' ).modal( 'hide' ) ;
    }
    
    $scope.applyClassification = function() {

        const input = $scope.userSel;
        if( ( input.l1Cat == null && input.l1CatNew == null ) ||
            ( input.l2Cat == null && input.l2CatNew == null ) ) {
            $ngConfirm( "Please add valid L1 and L2 categorization." ) ;
            return ;
        }

        let newCategory = false;
        let l1Cat = input.l1Cat;
        let l2Cat = input.l2Cat;

        if( l1Cat == null || l2Cat == null ) {
            newCategory = true ;
            l1Cat = ( l1Cat == null ) ? input.l1CatNew : l1Cat ;
            l2Cat = ( l2Cat == null ) ? input.l2CatNew : l2Cat ;
        }
        
        if( input.saveRule ) {
            if( $scope.searchQuery.customRule == null ) {
                $ngConfirm( "There is no custom rule to save." ) ;
                return ;
            }
            
            if( input.ruleName == null ) {
                $ngConfirm( "Please enter an unique rule name." ) ;
                return ;
            }
        }
            
        for( let i=0; i<selectedEntries.length; i++ ) {

            const entry = selectedEntries[i];

            entry.l1Cat = l1Cat ;
            entry.l2Cat = l2Cat ;
            
            // Note that if a note is entered in the classification dialog
            // it overrides the existing notes of all the impacted entries.
            // If the note is kept blank, the entries retain their existing
            // notes.
            if( input.notes == null || input.notes === "" ) {
                input.notes = "" ;
            }
            else {
                entry.notes = input.notes ;
            }
        }
        
        applyClassificationOnServer( l1Cat, l2Cat, newCategory ) ;
        
        $( '#entryCategorizationDialog' ).modal( 'hide' ) ;
    }
    
    $scope.newL1CategoryEntered = function() {
        
        if( $scope.userSel != null ) {
            $scope.userSel.l1Cat = null ;
            $scope.userSel.l2Cat = null ;
        }
        
        if( $scope.entryBeingSplit != null ) {
            $scope.splitEntryDetails.l1Cat = null ;
            $scope.splitEntryDetails.l2Cat = null ;
            $scope.validateSplitEntry() ;
        }
    }
    
    $scope.newL2CategoryEntered = function() {
        
        if( $scope.userSel != null ) {
            $scope.userSel.l2Cat = null ;
        }
        
        if( $scope.entryBeingSplit != null ) {
            $scope.splitEntryDetails.l2Cat = null ;
            $scope.validateSplitEntry() ;
        }
    }

    $scope.applyAllFilters = function() {
        $scope.remarksFilterTextChanged() ;
        $scope.l1FilterTextChanged() ;
        $scope.l2FilterTextChanged() ;
        $scope.notesFilterTextChanged() ;
        $scope.amountFilterTextChanged() ;
    }
    
    $scope.remarksFilterTextChanged = function() {
        if( $scope.remarksFilterText != null ) {
            filterEntries( true, "remarks", $scope.remarksFilterText.toLowerCase() ) ;
        }
    }
    
    $scope.l1FilterTextChanged = function() {
        if( $scope.l1FilterText != null ) {
            filterEntries( true, "l1Cat", $scope.l1FilterText.toLowerCase() ) ;
        }
    }

    $scope.l2FilterTextChanged = function() {
        if( $scope.l2FilterText != null ) {
            filterEntries( true, "l2Cat", $scope.l2FilterText.toLowerCase() ) ;
        }
    }

    $scope.notesFilterTextChanged = function() {
        if( $scope.notesFilterText != null ) {
            filterEntries( true, "notes", $scope.notesFilterText.toLowerCase() ) ;
        }
    }

    $scope.amountFilterTextChanged = function() {
        if( $scope.amountFilterText != null ) {
            filterEntries( true, "amount", $scope.amountFilterText, true ) ;
        }
    }

    $scope.selectPrevMonth = function() {
        selectPrevNextMonth( false ) ;
    }

    $scope.selectNextMonth = function() {
        selectPrevNextMonth( true ) ;
    }
    
    $scope.showOnlyUnclassifiedCriteriaChanged = function() {
        $scope.applyAllFilters() ;
    }

    $scope.deleteLedgerEntry = function( index ) {
        console.log( "Deleting entry at index = " + index ) ;
        const entry = $scope.ledgerEntries[index];

        $ngConfirm({
            title: 'Confirm!',
            content: 'Delete ledger entry ' + entry.remarks ,
            scope: $scope,
            buttons: {
                close: function(scope, button){
                    console.log( "User cancelled." ) ;
                },
                yes: {
                    text: 'Yes',
                    btnClass: 'btn-blue',
                    action: function(scope, button){
                        console.log( "Ok to delete account." ) ;
                        deleteLedgerEntryOnServer( entry, function() {
                            $scope.ledgerEntries.splice( index, 1 ) ;
                        }) ;
                        return true ;
                    }
                }
            }
        });
    }
    
    $scope.isCreditCardEntry = function( entry ) {
        if( entry != null ) {
            return entry.account.accountType === "CREDIT" ;
        }
        return false ;
    }
    
    $scope.isCashEntry = function( entry ) {
        if( entry != null ) {
            return entry.account.accountNumber === "CASH@HOME" ;
        }
        return false ;
    }

    $scope.showSplitLedgerEntryDialog = function( index ) {
        console.log( "Splitting ledger entry at index = " + index ) ;
        
        resetSplitEntryState() ;
        
        $scope.entryBeingSplit = $scope.ledgerEntries[ index ] ;
        $scope.relevantCategoriesForSelectedEntries = 
                            ( $scope.entryBeingSplit.amount < 0 ) ?
                                    $scope.masterCategories.debit :
                                    $scope.masterCategories.credit ;
        
        $scope.validateSplitEntry() ;
        
        $( '#entrySplitDialog' ).modal( 'show' ) ;
    }
    
    $scope.cancelSplit = function() {
        resetSplitEntryState() ;
        $( '#entrySplitDialog' ).modal( 'hide' ) ;
    }
    
    $scope.saveSplitLedgerEntry = function() {
        
        console.log( "Applying entry split." ) ;

        const input = $scope.splitEntryDetails;

        $scope.validateSplitEntry() ;
        if( input.errMsgs.length > 0 ) {
            $ngConfirm( "Please fix the errors first." ) ;
            return ;
        }

        let newCategory = false;
        let l1Cat = input.l1Cat;
        let l2Cat = input.l2Cat;

        if( l1Cat == null || l2Cat == null ) {
            newCategory = true ;
            l1Cat = ( l1Cat == null ) ? input.l1CatNew : l1Cat ;
            l2Cat = ( l2Cat == null ) ? input.l2CatNew : l2Cat ;
        }

        const postData = {
            entryId: $scope.entryBeingSplit.id,
            amount: input.amount,
            l1Cat: l1Cat,
            l2Cat: l2Cat,
            newClassifier: newCategory,
            notes: input.notes
        };

        console.log( postData ) ;
        applySplitOnServer( postData ) ;
    }
    
    $scope.validateSplitEntry = function() {

        const splitDetails = $scope.splitEntryDetails;
        const parentEntry = $scope.entryBeingSplit;

        splitDetails.errMsgs.length = 0 ;
        
        if( splitDetails.amount <= 0 ) {
            splitDetails.errMsgs.push( "Amount can't be less than or equal to zero" ) ;
        }
        else if( splitDetails.amount >= -1*parentEntry.amount ) {
            splitDetails.errMsgs.push( "Amount can't be greater than or equal to max value." ) ;
        }
        
        if( splitDetails.l1Cat == null && splitDetails.l1CatNew == null ) {
            splitDetails.errMsgs.push( "L1 category needs to be specified." ) ;
        }

        if( splitDetails.l2Cat == null && splitDetails.l2CatNew == null ) {
            splitDetails.errMsgs.push( "L2 category needs to be specified." ) ;
        }
        
        if( splitDetails.notes == null ) {
            splitDetails.errMsgs.push( "Notes needs to be specified." ) ;
        }
    }
    
    $scope.editEntry = function( entry ) {
        // Disable editing of all other entries except this one
        for( let i=0; i<$scope.ledgerEntries.length; i++ ) {
            const anEntry = $scope.ledgerEntries[i];
            anEntry.editing = false ;
        }
                
        entry.editing = true ;
        entry.selected = true ;
        
        // Copy the editable attributes to the clipboard
        $scope.userSel.l1Cat = entry.l1Cat ;
        $scope.userSel.l2Cat = entry.l2Cat ;
        $scope.userSel.notes = entry.notes ;
        
        $scope.relevantCategoriesForSelectedEntries = ( entry.amount < 0 ) ?
                                $scope.masterCategories.debit :
                                $scope.masterCategories.credit ;
    }
    
    $scope.saveEditedEntry = function( entryBeingEdited ) {
        
        console.log( "Saving edited entry" ) ;
        
        for( let i=0; i<$scope.ledgerEntries.length; i++ ) {
            const entry = $scope.ledgerEntries[i];
            if( entry.selected ) {
                entry.l1Cat = $scope.userSel.l1Cat ;
                entry.l2Cat = $scope.userSel.l2Cat ;
                entry.notes = $scope.userSel.notes ;
            }
        }

        const errMsg = validateSelectedEntriesForClassification() ;
        if( errMsg == null ) {
            applyClassificationOnServer( $scope.userSel.l1Cat, 
                                         $scope.userSel.l2Cat,
                                         null ) ;
        }
        else {
            $ngConfirm( errMsg ) ;
        }
    }
    
    $scope.toggleControlPanel = function() {
        $scope.controlPanelVisible = !$scope.controlPanelVisible ;
    }
    
    $scope.creditClassifierChanged = function() {
        if( $scope.searchQuery.creditClassifier ) {
            $scope.filterCategories = $scope.masterCategories.credit ;
        }
        else {
            $scope.filterCategories = $scope.masterCategories.debit ;
        }
    }
    
    $scope.showDebitRecoveryDialog = function( index ) {

        const creditTxn = $scope.ledgerEntries[index];
        $scope.$broadcast( 'creditTxnSetForDebitRecoveryDialog', creditTxn ) ;
        
        $( '#debitRecoveryDialog' ).modal( 'show' ) ;
    }
    
    $scope.hideDebitRecoveryDialog = function() {
        $( '#debitRecoveryDialog' ).modal( 'hide' ) ;
        $scope.searchLedgerEntries() ;
    }
    
    $scope.showAssociatedCreditsDialog = function( index ) {

        const debitTxn = $scope.ledgerEntries[index];
        $scope.$broadcast( 'debitTxnSetForAssociatedCreditsDialog', debitTxn ) ;
        
        $( '#associatedCreditsDialog' ).modal( 'show' ) ;
    }
    
    $scope.hideAssociatedCreditsDialog = function() {
        
        $( '#associatedCreditsDialog' ).modal( 'hide' ) ;
        $scope.searchLedgerEntries() ;
    }
    
    $scope.exportLedgerAsExcel = function() {

        const exporter = new LedgerExporter($scope.ledgerEntries);
        exporter.export() ;
    }

    $scope.deleteSelectedEntries = function() {
        let selectedEntryIds = [] ;
        for( let i=0; i<$scope.ledgerEntries.length; i++ ) {
            const entry = $scope.ledgerEntries[i];
            if( entry.selected ) {
                selectedEntryIds.push(entry.id) ;
            }
        }

        if( selectedEntryIds.length === 0 ) {
            $ngConfirm( 'No entries are selected.' ) ;
        }
        else {
            $ngConfirm({
                title: 'Confirm!',
                content: 'Delete ' + selectedEntryIds.length + ' selected entries' ,
                scope: $scope,
                buttons: {
                    close: function(scope, button){
                        console.log( "User cancelled." ) ;
                    },
                    yes: {
                        text: 'Yes',
                        btnClass: 'btn-blue',
                        action: function(scope, button){
                            console.log( "Ok to delete account." ) ;
                            deleteLedgerEntriesOnServer( selectedEntryIds, function() {
                                fetchLedgerEntries() ;
                            }) ;
                        }
                    }
                }
            });
        }
    }

    // --- [END] Scope functions

    // -----------------------------------------------------------------------
    // --- [START] Local functions -------------------------------------------
    
    function validateSelectedEntriesForClassification() {
        let numEntriesSelected = 0;
        let entryTypeCount = 0;

        for( let i=0; i<$scope.ledgerEntries.length; i++ ) {
            const entry = $scope.ledgerEntries[i];
            if( entry.selected ) {
                selectedEntries.push( entry ) ;
                numEntriesSelected++ ;
                if( entry.amount < 0 ) {
                    entryTypeCount-- ;
                }
                else {
                    entryTypeCount++ ;
                }
            }
        }
        
        if( numEntriesSelected > 0 ) {
            if( Math.abs( entryTypeCount ) !== numEntriesSelected ) {
                return "Selected entries contain both credit and debit entries." ;
            }
        }
        else {
            return "Please select some ledger entries to categorize." ;
        }
        return null ;
    }
    
    function resetSplitEntryState() {
        
        $scope.entryBeingSplit = null ;
        $scope.splitEntryDetails.amount = 0 ;
        $scope.splitEntryDetails.l1Cat = null ;
        $scope.splitEntryDetails.l1CatNew = null ;
        $scope.splitEntryDetails.l2Cat = null ;
        $scope.splitEntryDetails.l2CatNew = null ;
        $scope.splitEntryDetails.notes = null ;
        $scope.splitEntryDetails.errMsgs.length = 0 ;
    }
    
    function initializeController() {
        const parameters = new URLSearchParams(window.location.search);
        const accountIds = parameters.get('accountIds');

        if( accountIds == null || accountIds.length === 0 ) {
            $scope.searchQuery.accountIds = null ;
        }
        else {
            $scope.searchQuery.accountIds = accountIds.split( "," ) ;
            if( $scope.searchQuery.accountIds.length > 0 ) {
                $scope.$parent.navBarTitle = "Consolidated ledger" ; 
            }
        }
        initializeDateRange() ;
        fetchLedgerEntries() ;
    }
    
    function initializeDateRange() {

        const startDt = $scope.searchQuery.startDate;
        const endDt = $scope.searchQuery.endDate;
        const text = moment(startDt).format('MMM D, YYYY') + ' - ' +
                            moment(endDt).format('MMM D, YYYY');

        $('#ledgerDuration span').html( text ) ;            
     
        $('#ledgerDuration').daterangepicker({
            format          : 'MM/DD/YYYY',
            startDate       : startDt,
            endDate         : endDt,
            showDropdowns   : true,
            showWeekNumbers : false,
            opens           : 'right',
            drops           : 'down',
            buttonClasses   : ['btn', 'btn-sm'],
            applyClass      : 'btn-primary',
            cancelClass     : 'btn-default',
            separator       : ' to ',
            
            ranges : {
               'Last 7 Days' : [ 
                  moment().subtract(6, 'days'), 
                  moment()
               ],
               'Last 30 Days' : [ 
                  moment().subtract(29, 'days'), 
                  moment()
               ],
               'This Month' : [ 
                  moment().startOf('month'), 
                  moment().endOf('month')
               ],
               'Last Month' : [ 
                  moment().subtract(1, 'month').startOf('month'), 
                  moment().subtract(1, 'month').endOf('month')
               ]
            },
            locale : {
                applyLabel       : 'Submit',
                cancelLabel      : 'Cancel',
                fromLabel        : 'From',
                toLabel          : 'To',
                customRangeLabel : 'Custom',
                daysOfWeek       : ['Su', 'Mo', 'Tu', 'We', 'Th', 'Fr','Sa'],
                firstDay         : 1,
                monthNames       : ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 
                                    'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'],
            }
        }, 
        function( start, end, label ) {
            const text = start.format('MMM D, YYYY') + ' - ' + end.format('MMM D, YYYY');
            $('#ledgerDuration span').html( text ) ;
            $scope.searchQuery.startDate = start.toDate() ;
            $scope.searchQuery.endDate   = end.toDate() ;
        });
    }
    
    // ------------------- Server comm functions -----------------------------
    function fetchLedgerEntries() {
        
        if( $scope.searchQuery.accountIds == null || 
            $scope.searchQuery.accountIds.length === 0 ) return ;
        
        $scope.$emit( 'interactingWithServer', { isStart : true } ) ;
        $http.post( '/Ledger/Search', $scope.searchQuery )
        .then ( 
            function( response ){

                const ledgerEntries= response.data["ledgerEntries"];
                const associatedTxnIds = response.data["associatedTxnIds"];

                $scope.ledgerEntries.length = 0 ;
                for( let i=0; i<ledgerEntries.length; i++ ) {

                    const entry = ledgerEntries[i];
                    if( i === 0 && $scope.searchQuery.accountIds.length === 1 ) {
                        $scope.$parent.navBarTitle = 
                            entry.account.accountOwner + " - " + 
                            entry.account.accountNumber + " - " + 
                            entry.account.shortName ;
                    }
                    
                    // Additional attribute to track user selection in view
                    entry.selected = false ;
                    entry.visible = true ;
                    entry.editing = false ;
                    entry.hasAssociatedTxn = associatedTxnIds.includes( entry.id ) ;
                    
                    $scope.ledgerEntries.push( entry ) ;
                }
                
                resetClassificationState() ;
                resetPivotCatSelection() ;
                $scope.applyAllFilters() ;
                fetchClassificationCategories() ;
            }, 
            function(){
                $scope.$parent.addErrorAlert( "Could not fetch search results." ) ;
            }
        )
        .finally(function() {
            $scope.$emit( 'interactingWithServer', { isStart : false } ) ;
        }) ;
    }
    
    function fetchClassificationCategories() {
        
        $scope.$emit( 'interactingWithServer', { isStart : true } ) ;
        $http.get( '/Ledger/Categories' )
        .then ( 
            function( response ){
                populateMasterCategories( response.data ) ;
            }, 
            function( error ){
                $scope.$parent.addErrorAlert( "Could not fetch classification categories." ) ;
            }
        )
        .finally(function() {
            $scope.$emit( 'interactingWithServer', { isStart : false } ) ;
        }) ;
    }
    
    function populateMasterCategories( categories ) {
        
        $scope.masterCategories.credit.l1Categories.length = 0 ;
        $scope.masterCategories.credit.l2Categories.clear() ;
        $scope.masterCategories.debit.l1Categories.length = 0 ;
        $scope.masterCategories.debit.l2Categories.clear() ;
        $scope.selectedL1Category = null ;
        $scope.selectedL2Category = null ;
        
        for( let i=0; i<categories.length; i++ ) {
            const category = categories[i];
            if( category.creditClassification ) {
                classifyCategoryInMasterList( 
                        $scope.masterCategories.credit.l1Categories, 
                        $scope.masterCategories.credit.l2Categories,
                        category ) ; 
            }
            else {
                classifyCategoryInMasterList( 
                        $scope.masterCategories.debit.l1Categories, 
                        $scope.masterCategories.debit.l2Categories,
                        category ) ; 
            }
        }
        
        $scope.creditClassifierChanged() ;
    }
    
    function classifyCategoryInMasterList( l1CatList, l2CatMap, category ) {

        const l1 = category.l1CatName;
        const l2 = category.l2CatName;

        if( l1CatList.indexOf( l1 ) === -1 ) {
            l1CatList.push( l1 ) ;
        }
        
        if( !l2CatMap.has( l1 ) ) {
            l2CatMap.set( l1, [] ) ;
        }

        const l2List = l2CatMap.get(l1);
        l2List.push( l2 ) ;
    }
    
    function applyClassificationOnServer( l1Cat, l2Cat, newCategory )  {

        const postData = {
            entryIdList: [],
            l1Cat: l1Cat,
            l2Cat: l2Cat,
            newClassifier: newCategory,
            rule: $scope.searchQuery.customRule,
            saveRule: $scope.userSel.saveRule,
            creditClassifier: false,
            ruleName: $scope.userSel.ruleName,
            notes: $scope.userSel.notes
        };

        for( let i=0; i<selectedEntries.length; i++ ) {
            const entry = selectedEntries[i];
            if( i === 0 ) {
                postData.creditClassifier = ( entry.amount > 0 ) ;
            }
            postData.entryIdList.push( entry.id ) ;
        }

        $scope.$emit( 'interactingWithServer', { isStart : true } ) ;
        $http.post( '/Ledger/Classification', postData )
        .then ( 
            function( response ){
                console.log( "Classification applied successfully." ) ;
                if( postData.saveRule ) {
                    fetchLedgerEntries() ;
                }
                else {
                    resetClassificationState() ;                
                }
            }, 
            function( error ){
                $scope.$parent.addErrorAlert( "Could not apply classification." ) ;
            }
        )
        .finally(function() {
            $scope.$emit( 'interactingWithServer', { isStart : false } ) ;
        }) ;
    }
    
    function resetClassificationState() {
        
        for( let i=0; i<selectedEntries.length; i++ ) {
            const entry = selectedEntries[i];
            entry.selected = false ;
            entry.editing = false ;
        }
        
        selectedEntries.length = 0 ;
        $scope.relevantCategoriesForSelectedEntries = null ;
        
        $scope.userSel.l1Cat    = null ;
        $scope.userSel.l1CatNew = null ;
        $scope.userSel.l2Cat    = null ;
        $scope.userSel.l2CatNew = null ;
        $scope.userSel.saveRule = false ;
        $scope.userSel.ruleName = null ;
        $scope.userSel.notes    = null ;
    }
    
    function filterEntries( refreshPivot, entryAttribName, filterText, numeric=false ) {

        $scope.bulkSelState.value = false ;
        pivotSrcData = [] ;
        let compOp = '=' ;
        let compValue = 0 ;

        if( numeric ) {
            let str = filterText.replace( ' ', '' ) ;
            let op = str.charAt(0)

            if( op === '>' || op === '<' ) {
                compOp = op ;
                compValue = parseInt( str.substring(1) ) ;
            }
            else {
                compOp = '=' ;
                compValue = parseInt( str ) ;
            }
        }
        
        for( let i=0; i<$scope.ledgerEntries.length; i++ ) {
            const entry = $scope.ledgerEntries[i];

            entry.selected = false ;
            entry.visible = true ;
            
            if( !( filterText == null || filterText.trim() === "" ) ) {

                const attribVal = entry[entryAttribName] ;
                if( numeric ) {
                    entry.visible = (compOp === '=' && Math.floor(attribVal) === compValue) ||
                                    (compOp === '>' && attribVal >= compValue) ||
                                    (compOp === '<' && attribVal <= compValue);
                }
                else if( attribVal == null || !attribVal.toLowerCase().includes( filterText ) ) {
                    entry.visible = false ;
                }
            }
            
            if( $scope.searchQuery.showOnlyUnclassified && entry.visible ) {
                if( entry.l1Cat != null ) {
                    entry.visible = false ;
                }
            }
            
            if( entry.visible ) {
                determineVisibilityBasedOnPivotTableSelections( entry ) ;
            }
            
            if( entry.visible ) {
                const type = entry.amount >= 0 ? "Income" : "Expense";
                pivotSrcData.push( [ 
                    type, 
                    entry.l1Cat == null ? "": entry.l1Cat, 
                            entry.l2Cat == null ? "": entry.l2Cat, 
                                    entry.amount 
                                    ] ) ;
            }
        }
        
        if( refreshPivot ) refreshPivotTable() ;
    }
    
    function determineVisibilityBasedOnPivotTableSelections( entry ) {
        
        entry.visible = !( l1FilterSelections.length > 0 || 
                           l2FilterSelections.length > 0 ) ;
        
        if( l1FilterSelections.length > 0 && 
            l1FilterSelections.indexOf( entry.l1Cat ) !== -1 ) {
            entry.visible = true ;
        }
        
        if( !entry.visible && 
            l2FilterSelections.length > 0 && 
            l2FilterSelections.indexOf( entry.l2Cat ) !== -1 ) {
            entry.visible = true ;
        }
    }
    
    function refreshPivotTable() {
        const pivotTable = new PivotTable();
        pivotSrcData.sort( function( tupule1, tupule2 ) {
            const typeCompare = tupule1[0].localeCompare(tupule2[0]);
            if( typeCompare === 0 ) {
                const l1Compare = tupule1[1].localeCompare(tupule2[1]);
                if( l1Compare === 0 ) {
                    return tupule1[2].localeCompare(tupule2[2]) ;
                }
                return l1Compare
            }
            return typeCompare ;
        } ) ;
        
        pivotTable.setPivotData( pivotSrcColNames, pivotSrcData ) ;
        pivotTable.initializePivotTable( [ "Type", "L1", "L2" ], "Type", "Amount" ) ;
        pivotTable.renderPivotTable( "pivot_table_div", "Ledger Pivot", 
                                     ledgerPivotRenderHelperCallback, 
                                     ledgerPivotRowSelectionCallback,
                                     false, false ) ;
        pivotTable.expandFirstLevel() ;
        resetPivotCatSelection() ;
    }
    
    function ledgerPivotRenderHelperCallback( rowIndex, colIndex, renderData ) {
        let fmt = "";
        const cellData = renderData.content;
        if( cellData != null ) {
            if( isNaN( cellData ) ) {
                fmt = cellData ;
            }
            else {
                const amt = parseFloat(cellData);
                fmt = amt.toLocaleString('en-IN', {
                    maximumFractionDigits: 2,
                    style: 'currency',
                    currency: 'INR'
                });

                if( fmt.indexOf( '.' ) !== -1 ) {
                    fmt = fmt.substring( 0, fmt.indexOf( '.' ) ) ; 
                }
                
                fmt = fmt.replace( "\u20B9", "" ) ;
                fmt = fmt.replace( /\s/g, '' ) ;
            }
        }
        renderData.content = fmt ;
        return renderData ;
    }
    
    function ledgerPivotRowSelectionCallback( depth, selected, rowCellValues ) {
        const cat = rowCellValues[0];
        let catArray = null;

        if( depth === 2 ){ catArray = l1FilterSelections ; }
        else if( depth === 3 ){ catArray = l2FilterSelections ; }
        
        if( catArray != null ) {
            if( selected ) {
                catArray.push( cat ) ;
            }
            else {
                const index = catArray.indexOf(cat);
                if ( index !== -1 )catArray.splice( index, 1 ) ;
            }
        }
        
        console.log( catArray ) ;
        
        $scope.applyAllFilters() ;
        $scope.$apply() ;
    }

    function selectPrevNextMonth( isNext ) {

        const crit = $scope.searchQuery;
        let m1, m2;
        if( isNext ) {
            m1 = moment( crit.endDate ).add( 1, 'month' ) ;
            m2 = moment( crit.endDate ).add( 1, 'month' ) ;
        }
        else {
            m1 = moment( crit.endDate ).subtract( 1, 'month' ) ;
            m2 = moment( crit.endDate ).subtract( 1, 'month' ) ;
        }
        
        crit.startDate = m1.startOf( 'month' ) ; 
        crit.endDate = m2.endOf( 'month' ) ;
        
        refreshDatePickerLabel() ;
        fetchLedgerEntries() ;
    }
    
    function refreshDatePickerLabel() {
        const crit = $scope.searchQuery;
        const text = crit.startDate.format('MMM D, YYYY') + ' - ' +
                            crit.endDate.format('MMM D, YYYY');
        $('#ledgerDuration span').html( text ) ;
    }
    
    function resetPivotCatSelection() {
        l1FilterSelections.length = 0 ;
        l2FilterSelections.length = 0 ;
    }

    function deleteLedgerEntryOnServer( entry, successCallback ) {
        $scope.$emit( 'interactingWithServer', { isStart : true } ) ;
        $http.delete( '/Ledger/' + entry.id )
        .then ( 
            function( response ){
                console.log( "Deleted ledger entry" ) ;
                successCallback() ;
            }, 
            function( error ){
                $scope.$parent.addErrorAlert( "Error deleting ledger entry." ) ;
            }
        )
        .finally(function() {
            $scope.$emit( 'interactingWithServer', { isStart : false } ) ;
        }) ;
    }
    
    function deleteLedgerEntriesOnServer( entryIds, successCallback ) {
        $scope.$emit( 'interactingWithServer', { isStart : true } ) ;
        $http.post( '/Ledger/BulkDelete', entryIds )
        .then (
            function( response ){
                console.log( "Deleted ledger entries" ) ;
                successCallback() ;
            },
            function( error ){
                $scope.$parent.addErrorAlert( "Error deleting ledger entries." ) ;
            }
        )
        .finally(function() {
            $scope.$emit( 'interactingWithServer', { isStart : false } ) ;
        }) ;
    }

    function applySplitOnServer( postData ) {
        
        $scope.$emit( 'interactingWithServer', { isStart : true } ) ;
        $http.post( '/LedgerEntry/Split', postData )
        .then ( 
            function( response ){
                console.log( "Split ledger entry ledger entry" ) ;
                $( '#entrySplitDialog' ).modal( 'hide' ) ;
                fetchLedgerEntries() ;
                resetSplitEntryState() ;
            }, 
            function( error ){
                $scope.$parent.addErrorAlert( "Error splitting ledger entry." ) ;
            }
        )
        .finally(function() {
            $scope.$emit( 'interactingWithServer', { isStart : false } ) ;
        }) ;
    }
} ) ;
