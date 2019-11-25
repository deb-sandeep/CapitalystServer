capitalystNgApp.controller( 'LedgerHomeController', 
    function( $scope, $http, $rootScope, $location, $window ) {
    
    // ---------------- Local variables --------------------------------------
    
    // ---------------- Scope variables --------------------------------------
    $scope.$parent.navBarTitle = "Ledger View" ;
    $scope.searchCriteria = {
        accountId : null,
        startDate : moment().subtract(6, 'days').toDate(),
        endDate : moment().toDate()
    } ;

    // -----------------------------------------------------------------------
    // --- [START] Controller initialization ---------------------------------
    console.log( "Loading LedgerHomeController" ) ;
    initializeController() ;
    // --- [END] Controller initialization -----------------------------------
    
    
    // -----------------------------------------------------------------------
    // --- [START] Scope functions -------------------------------------------
    // --- [END] Scope functions

    // -----------------------------------------------------------------------
    // --- [START] Local functions -------------------------------------------
    
    function initializeController() {
        var parameters = new URLSearchParams( window.location.search ) ;
        var accountId = parameters.get( 'accountId' ) ;
        $scope.searchCriteria.accountId = accountId ;
        initializeDateRange() ;
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
            $scope.$digest() ;
        });
    }
    
    // ------------------- Server comm functions -----------------------------
    /* Template server communication function
    function <serverComm>() {
        
        $scope.$emit( 'interactingWithServer', { isStart : true } ) ;
        $http.post( '/<API endpoint>', {
            'eventId'       : eventId,
        } )
        .then ( 
            function( response ){
                var data = response.data ;
                // TODO: Server data processing logic
            }, 
            function( error ){
                $scope.$parent.addErrorAlert( "<Error Message>" ) ;
            }
        )
        .finally(function() {
            $scope.$emit( 'interactingWithServer', { isStart : false } ) ;
        }) ;
    }
    */
} ) ;