capitalystNgApp.controller( 'LedgerHomeController', 
    function( $scope, $http, $rootScope, $location, $window, $ngConfirm ) {
    
    // ---------------- Local variables --------------------------------------
    var selectedEntries = [] ;
    
    // ---------------- Scope variables --------------------------------------
    $scope.$parent.navBarTitle = "Ledger View" ;
    $scope.account = null ;
    $scope.searchCriteria = {
        accountId : null,
        startDate : moment().subtract(1, 'month').toDate(),
        endDate : moment().toDate(),
        lowerAmtThreshold : null,
        upperAmtThreshold : null,
        customRule : null
    } ;
    
    $scope.ledgerEntries = [] ;
    $scope.entriesBulkSelectionState = {
       value : false
    } ;
    
    $scope.ledgerCategories = {
       credit : {
           l1Categories : [],
           l2Categories : new Map()
       },
       debit : {
           l1Categories : [],
           l2Categories : new Map()
       }
    } ;
    
    $scope.classificationCategories = null ;
    $scope.userSel = {
        l1Cat : null,
        l1CatNew : null,
        l2Cat : null,
        l2CatNew : null,
        saveRule : false,
        ruleName : null
    } ;

    // -----------------------------------------------------------------------
    // --- [START] Controller initialization ---------------------------------
    console.log( "Loading LedgerHomeController" ) ;
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
        $scope.searchCriteria.startDate = moment().subtract(30, 'month').toDate() ;
        $scope.searchCriteria.endDate = moment().toDate() ;
        $scope.searchCriteria.lowerAmtThreshold = null ;
        $scope.searchCriteria.upperAmtThreshold = null ;
        $scope.searchCriteria.customRule = null ;
        
        fetchLedgerEntries() ;
    }
    
    $scope.toggleSelectionForAllEntries = function() {
        for( var i=0; i<$scope.ledgerEntries.length; i++ ) {
            var entry = $scope.ledgerEntries[i] ;
            entry.selected = $scope.entriesBulkSelectionState.value ;
        }
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
                $scope.classificationCategories = 
                                        ( selectedEntries[0].amount < 0 ) ?
                                        $scope.ledgerCategories.debit :
                                        $scope.ledgerCategories.credit ;
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
        
        var userSel = $scope.userSel ;
        if( userSel.l1Cat == null &&
            userSel.l1CatNew == null ) {
            $ngConfirm( "Please add valid L1 categorization." ) ;
            return ;
        }
        
        if( userSel.l2Cat == null &&
            userSel.l2CatNew == null ) {
            $ngConfirm( "Please add valid L2 categorization." ) ;
            return ;
        }
        
        var newCategory = false ;
        var l1Cat = userSel.l1Cat ;
        if( l1Cat == null ) {
            l1Cat = userSel.l1CatNew ;
            newCategory = true ;
        }
        
        var l2Cat = userSel.l2Cat ;
        if( l2Cat == null ) {
            l2Cat = userSel.l2CatNew ;
            newCategory = true ;
        }
        
        if( userSel.saveRule ) {
            if( $scope.searchCriteria.customRule == null ) {
                $ngConfigm( "There is no custom rule to save." ) ;
            }
            
            if( userSel.ruleName == null ) {
                $ngConfigm( "Please enter an unique rule name." ) ;
            }
        }
            
        for( var i=0; i<selectedEntries.length; i++ ) {
            var entry = selectedEntries[i] ;
            entry.l1Cat = l1Cat ;
            entry.l2Cat = l2Cat ;
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

    // --- [END] Scope functions

    // -----------------------------------------------------------------------
    // --- [START] Local functions -------------------------------------------
    
    function initializeController() {
        var parameters = new URLSearchParams( window.location.search ) ;
        var accountId = parameters.get( 'accountId' ) ;
        $scope.searchCriteria.accountId = accountId ;
        initializeDateRange() ;
        fetchLedgerEntries() ;
    }
    
    function initializeDateRange() {

        $('#ledgerDuration span').html( 
            moment( $scope.searchCriteria.startDate ).format('MMM D, YYYY')
            + ' - ' +
            moment( $scope.searchCriteria.endDate ).format('MMM D, YYYY')
        );
     
        $('#ledgerDuration').daterangepicker({
            format: 'MM/DD/YYYY',
            startDate: $scope.searchCriteria.startDate,
            endDate: $scope.searchCriteria.endDate,
            showDropdowns: true,
            showWeekNumbers: true,
            ranges: {
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
            opens         : 'right',
            drops         : 'down',
            buttonClasses : ['btn', 'btn-sm'],
            applyClass    : 'btn-primary',
            cancelClass   : 'btn-default',
            separator     : ' to ',
            locale        : {
                applyLabel       : 'Submit',
                cancelLabel      : 'Cancel',
                fromLabel        : 'From',
                toLabel          : 'To',
                customRangeLabel : 'Custom',
                daysOfWeek       : ['Su', 'Mo', 'Tu', 'We', 'Th', 'Fr','Sa'],
                monthNames       : [
                    'January', 'February', 'March', 'April', 'May', 
                    'June', 'July', 'August', 'September', 
                    'October', 'November', 'December'],
                firstDay         : 1
            }
        }, function( start, end, label ) {
            $('#ledgerDuration span').html( 
                start.format('MMM D, YYYY') 
                + ' - ' + 
                end.format('MMM D, YYYY')
            ) ;
            $scope.searchCriteria.startDate = start.toDate() ;
            $scope.searchCriteria.endDate   = end.toDate() ;
        });
    }
    
    // ------------------- Server comm functions -----------------------------
    function fetchLedgerEntries() {
        
        if( $scope.searchCriteria.accountId == null ) return ;
        
        $scope.$emit( 'interactingWithServer', { isStart : true } ) ;
        $http.post( '/Ledger/Search', $scope.searchCriteria )
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
                    $scope.ledgerEntries.push( entry ) ;
                }
                fetchClassificationCategories() ;
                setTimeout( function(){
                    sortTable.init() ;
                }, 500 ) ;
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
                populateLedgerEntryClassificationCategories( response.data ) ;
            }, 
            function( error ){
                $scope.$parent.addErrorAlert( "Could not fetch classification categories." ) ;
            }
        )
        .finally(function() {
            $scope.$emit( 'interactingWithServer', { isStart : false } ) ;
        }) ;
    }
    
    function populateLedgerEntryClassificationCategories( categories ) {
        
        $scope.ledgerCategories.credit.l1Categories.length = 0 ;
        $scope.ledgerCategories.credit.l2Categories.clear() ;
        $scope.ledgerCategories.debit.l1Categories.length = 0 ;
        $scope.ledgerCategories.debit.l2Categories.clear() ;
        $scope.selectedL1Category = null ;
        $scope.selectedL2Category = null ;
        
        for( var i=0; i<categories.length; i++ ) {
            var category = categories[i] ;
            if( category.creditClassification ) {
                classifyCategory( 
                        $scope.ledgerCategories.credit.l1Categories, 
                        $scope.ledgerCategories.credit.l2Categories,
                        category ) ; 
            }
            else {
                classifyCategory( 
                        $scope.ledgerCategories.debit.l1Categories, 
                        $scope.ledgerCategories.debit.l2Categories,
                        category ) ; 
            }
        }
    }
    
    function classifyCategory( l1CatList, l2CatMap, category ) {
        
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
        $scope.classificationCategories = null ;
        $scope.userSel.l1Cat = null ;
        $scope.userSel.l1CatNew = null ;
        $scope.userSel.l2Cat = null ;
        $scope.userSel.l2CatNew = null ;
        $scope.userSel.saveRule = false ;
        $scope.userSel.ruleName = null ;
    }
    
    function applyClassificationOnServer( l1Cat, l2Cat, newCategory )  {

        var postData = {
            entryIdList : [], 
            l1Cat : l1Cat,
            l2Cat : l2Cat,
            newClassifier : newCategory,
            rule : $scope.searchCriteria.customRule,
            saveRule : $scope.userSel.saveRule,
            creditClassifier : false,
            ruleName : $scope.userSel.ruleName
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
            }, 
            function( error ){
                $scope.$parent.addErrorAlert( "Could not apply classification." ) ;
            }
        )
        .finally(function() {
            $scope.$emit( 'interactingWithServer', { isStart : false } ) ;
        }) ;
    }
    
} ) ;