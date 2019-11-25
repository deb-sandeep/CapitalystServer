capitalystNgApp.controller( 'LedgerHomeController', 
    function( $scope, $http, $rootScope, $location, $window ) {
    
    // ---------------- Local variables --------------------------------------
    
    // ---------------- Scope variables --------------------------------------
    $scope.$parent.navBarTitle = "Ledger View" ;
    $scope.searchCriteria = {
        accountId : null,
        startDate : moment().subtract(30, 'days').toDate(),
        endDate : moment().toDate()
    } ;
    
    $scope.ledgerEntries = [] ;

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
            fetchLedgerEntries() ;
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
                    $scope.ledgerEntries.push( response.data[i] ) ;
                }
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
} ) ;