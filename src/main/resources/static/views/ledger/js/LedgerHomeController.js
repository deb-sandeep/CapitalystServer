capitalystNgApp.controller( 'LedgerHomeController', 
    function( $scope, $http, $rootScope, $location, $window, $ngConfirm ) {
    
    // ---------------- Local variables --------------------------------------
    var selectedEntries = [] ;
    var pivotSrcColNames = [ "Type", "L1", "L2", "Amount" ] ;
    var pivotSrcData = [] ;
    var l1FilterSelections = [] ;
    var l2FilterSelections = [] ;
    
    // ---------------- Scope variables --------------------------------------
    $scope.$parent.navBarTitle = "Ledger View" ;
    $scope.account = null ;
    
    $scope.searchQuery = {
        accountIds : [],
        startDate : moment().subtract(1, 'month').toDate(),
        endDate : moment().toDate(),
        minAmt : null,
        maxAmt : null,
        customRule : null,
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
    
    $scope.entryFilterText = null ;
    
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
        $scope.entryFilterText = null ;
        
        fetchLedgerEntries() ;
    }
    
    $scope.toggleSelectionForAllEntries = function() {
        for( var i=0; i<$scope.ledgerEntries.length; i++ ) {
            var entry = $scope.ledgerEntries[i] ;
            entry.selected = false ;
            
            if( $scope.bulkSelState.value == true && 
                entry.visible ) {
                entry.selected = true ;
            }
        }
    }
    
    $scope.selectAndCategorize = function( entry ) {
        entry.selected = true ;
        $scope.showCategorizationDialog() ;
    }
    
    $scope.showCategorizationDialog = function() {
        var numEntriesSelected = 0 ;
        var entryTypeCount = 0 ;
        
        resetClassificationState() ;
        
        for( var i=0; i<$scope.ledgerEntries.length; i++ ) {
            var entry = $scope.ledgerEntries[i] ;
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
            if( Math.abs( entryTypeCount ) != numEntriesSelected ) {
                $ngConfirm( 'Selected entries contain both credit and debit entries.' ) ;
            }
            else {
                $scope.relevantCategoriesForSelectedEntries = 
                                        ( selectedEntries[0].amount < 0 ) ?
                                        $scope.masterCategories.debit :
                                        $scope.masterCategories.credit ;
                $( '#entryCategorizationDialog' ).modal( 'show' ) ;
            }
        }
        else {
            $ngConfirm( 'Please select some ledger entries to categorize.' ) ;
        }
    }
    
    $scope.cancelClassification = function() {
        resetClassificationState() ;
        $( '#entryCategorizationDialog' ).modal( 'hide' ) ;
    }
    
    $scope.applyClassification = function() {
        
        var input = $scope.userSel ;
        if( ( input.l1Cat == null && input.l1CatNew == null ) ||
            ( input.l2Cat == null && input.l2CatNew == null ) ) {
            $ngConfirm( "Please add valid L1 and L2 categorization." ) ;
            return ;
        }
        
        var newCategory = false ;
        var l1Cat = input.l1Cat ;
        var l2Cat = input.l2Cat ;

        if( l1Cat == null || l2Cat == null ) {
            newCategory = true ;
            l1Cat = ( l1Cat == null ) ? input.l1CatNew : l1Cat ;
            l2Cat = ( l2Cat == null ) ? input.l2CatNew : l2Cat ;
        }
        
        if( input.saveRule ) {
            if( $scope.searchQuery.customRule == null ) {
                $ngConfigm( "There is no custom rule to save." ) ;
                return ;
            }
            
            if( input.ruleName == null ) {
                $ngConfigm( "Please enter an unique rule name." ) ;
                return ;
            }
        }
            
        for( var i=0; i<selectedEntries.length; i++ ) {
            var entry = selectedEntries[i] ;
            entry.l1Cat = l1Cat ;
            entry.l2Cat = l2Cat ;
            entry.notes = ( input.saveRule ) ? input.ruleName : input.notes ;
        }
        
        applyClassificationOnServer( l1Cat, l2Cat, newCategory ) ;
        resetClassificationState() ;
        
        $( '#entryCategorizationDialog' ).modal( 'hide' ) ;
    }
    
    $scope.newL1CategoryEntered = function() {
        $scope.userSel.l1Cat = null ;
        $scope.userSel.l2Cat = null ;
    }
    
    $scope.newL2CategoryEntered = function() {
        $scope.userSel.l2Cat = null ;
    }
    
    $scope.entryFilterTextChanged = function() {
        $scope.bulkSelState.value = false ;
        filterEntries( true ) ;
    }
    
    $scope.selectPrevMonth = function() {
        selectPrevNextMonth( false ) ;
    }

    $scope.selectNextMonth = function() {
        selectPrevNextMonth( true ) ;
    }
    
    $scope.showOnlyUnclassifiedCriteriaChanged = function() {
        filterEntries( true ) ;
    }

    // --- [END] Scope functions

    // -----------------------------------------------------------------------
    // --- [START] Local functions -------------------------------------------
    
    function initializeController() {
        var parameters = new URLSearchParams( window.location.search ) ;
        var accountIds = parameters.get( 'accountIds' ) ;
        
        if( accountIds == null || accountIds.length == 0 ) {
            $scope.searchQuery.accountIds = null ;
        }
        else {
            $scope.searchQuery.accountIds = accountIds.split( "," ) ;
        }
        initializeDateRange() ;
        fetchLedgerEntries() ;
    }
    
    function initializeDateRange() {

        var startDt = $scope.searchQuery.startDate ;
        var endDt = $scope.searchQuery.endDate ;
        var text = moment( startDt ).format( 'MMM D, YYYY' ) + ' - ' +
                   moment( endDt ).format( 'MMM D, YYYY' ) ;
        
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
            var text = start.format( 'MMM D, YYYY' ) + ' - ' + end.format('MMM D, YYYY') ;
            $('#ledgerDuration span').html( text ) ;
            $scope.searchQuery.startDate = start.toDate() ;
            $scope.searchQuery.endDate   = end.toDate() ;
        });
    }
    
    // ------------------- Server comm functions -----------------------------
    function fetchLedgerEntries() {
        
        if( $scope.searchQuery.accountIds == null || 
            $scope.searchQuery.accountIds.length == 0 ) return ;
        
        $scope.$emit( 'interactingWithServer', { isStart : true } ) ;
        $http.post( '/Ledger/Search', $scope.searchQuery )
        .then ( 
            function( response ){
                $scope.ledgerEntries.length = 0 ;
                for( var i=0; i<response.data.length; i++ ) {
                    var entry = response.data[i] ;
                    if( i == 0 ) {
                        $scope.$parent.navBarTitle = 
                            entry.account.accountOwner + " - " + 
                            entry.account.accountNumber + " - " + 
                            entry.account.shortName ;
                    }
                    // Additional attribute to track user selection in view
                    entry.selected = false ;
                    entry.visible = true ;
                    $scope.ledgerEntries.push( entry ) ;
                }
                resetPivotCatSelection() ;
                filterEntries( true ) ;
                fetchClassificationCategories() ;
            }, 
            function( error ){
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
        
        for( var i=0; i<categories.length; i++ ) {
            var category = categories[i] ;
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
    }
    
    function classifyCategoryInMasterList( l1CatList, l2CatMap, category ) {
        
        var l1 = category.l1CatName ;
        var l2 = category.l2CatName ;
        
        if( l1CatList.indexOf( l1 ) == -1 ) {
            l1CatList.push( l1 ) ;
        }
        
        if( !l2CatMap.has( l1 ) ) {
            l2CatMap.set( l1, [] ) ;
        }
        
        var l2List = l2CatMap.get( l1 ) ;
        l2List.push( l2 ) ;
    }
    
    function resetClassificationState() {
        
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
    
    function applyClassificationOnServer( l1Cat, l2Cat, newCategory )  {

        var postData = {
            entryIdList      : [], 
            l1Cat            : l1Cat,
            l2Cat            : l2Cat,
            newClassifier    : newCategory,
            rule             : $scope.searchQuery.customRule,
            saveRule         : $scope.userSel.saveRule,
            creditClassifier : false,
            ruleName         : $scope.userSel.ruleName,
            notes            : $scope.userSel.notes
        } ;

        for( var i=0; i<selectedEntries.length; i++ ) {
            var entry = selectedEntries[i] ;
            if( i == 0 ) {
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
            }, 
            function( error ){
                $scope.$parent.addErrorAlert( "Could not apply classification." ) ;
            }
        )
        .finally(function() {
            $scope.$emit( 'interactingWithServer', { isStart : false } ) ;
        }) ;
    }
    
    function filterEntries( refreshPivot ) {
        
        pivotSrcData = [] ;
        
        for( var i=0; i<$scope.ledgerEntries.length; i++ ) {
            var entry = $scope.ledgerEntries[i] ;
            
            entry.selected = false ;
            entry.visible = true ;
            
            if( !( $scope.entryFilterText == null || 
                   $scope.entryFilterText.trim() == "" ) ) {
                
                if( !entry.remarks
                          .toLowerCase()
                          .includes( $scope.entryFilterText
                                           .toLowerCase() ) ) {
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
                var type = entry.amount > 0 ? "Income" : "Expense" ;
                pivotSrcData.push( [ 
                    type, 
                    entry.l1Cat, 
                    entry.l2Cat, 
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
            l1FilterSelections.indexOf( entry.l1Cat ) != -1 ) {
            entry.visible = true ;
        }
        
        if( !entry.visible && 
            l2FilterSelections.length > 0 && 
            l2FilterSelections.indexOf( entry.l2Cat ) != -1 ) {
            entry.visible = true ;
        }
    }
    
    function refreshPivotTable() {
        var pivotTable = new PivotTable() ;
        pivotSrcData.sort( function( tupule1, tupule2 ) {
            return tupule1[0].localeCompare( tupule2[0] ) ;
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
    
    function ledgerPivotRenderHelperCallback( rowIndex, colIndex, cellData ) {
        var fmt = "" ;
        if( cellData != null ) {
            if( isNaN( cellData ) ) {
                fmt = cellData ;
            }
            else {
                var amt = parseFloat( cellData ) ;
                var fmt = amt.toLocaleString( 'en-IN', {
                    maximumFractionDigits: 2,
                    style: 'currency',
                    currency: 'INR'
                } ) ;
                
                if( fmt.indexOf( '.' ) != -1 ) {
                    fmt = fmt.substring( 0, fmt.indexOf( '.' ) ) ; 
                }
                
                fmt = fmt.replace( "\u20B9", "" ) ;
                fmt = fmt.replace( /\s/g, '' ) ;
            }
        }
        return fmt ;
    }
    
    function ledgerPivotRowSelectionCallback( depth, selected, rowCellValues ) {
        var cat = rowCellValues[0] ;
        var catArray = null ;
        
        if( depth == 2 ){ catArray = l1FilterSelections ; }
        else if( depth == 3 ){ catArray = l2FilterSelections ; }
        
        if( catArray != null ) {
            if( selected ) {
                catArray.push( cat ) ;
            }
            else {
                var index = catArray.indexOf( cat ) ;
                if ( index != -1 )catArray.splice( index, 1 ) ;
            }
        }
        
        filterEntries( false ) ;
        $scope.$apply() ;
    }

    function selectPrevNextMonth( isNext ) {
        
        var crit = $scope.searchQuery ;
        var m1, m2 ;
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
        var crit = $scope.searchQuery ;
        var text = crit.startDate.format('MMM D, YYYY') + ' - ' + 
                   crit.endDate.format('MMM D, YYYY') ; 
        $('#ledgerDuration span').html( text ) ;
    }
    
    function resetPivotCatSelection() {
        l1FilterSelections.length = 0 ;
        l2FilterSelections.length = 0 ;
    }
} ) ;