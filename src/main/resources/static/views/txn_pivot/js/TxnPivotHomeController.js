capitalystNgApp.controller( 'TxnPivotHomeController', 
    function( $scope, $http, $location ) {
    
    // ---------------- Local variables --------------------------------------
    
    // ---------------- Scope variables --------------------------------------
    $scope.$parent.navBarTitle = "Pivot of Transactions" ;
    $scope.catSelectionPaneHidden = false ;
    $scope.pivotDuration = {
        startDate : moment().subtract(1, 'month').startOf( 'month' ),
        endDate : moment().toDate(),
    } ;

    // -----------------------------------------------------------------------
    // --- [START] Controller initialization ---------------------------------
    console.log( "Loading TxnPivotHomeController" ) ;
    initializeController() ;
    // --- [END] Controller initialization -----------------------------------
    
    
    // -----------------------------------------------------------------------
    // --- [START] Scope functions -------------------------------------------
    $scope.toggleCatSelectionPane = function() {
        var palette = document.getElementById( "category-selection-pane" ) ;
        var display = document.getElementById( "txn-pivot-panel" ) ;
        
        if( $scope.catSelectionPaneHidden ) {
            palette.style.display = "block" ;
            palette.style.width = "25%" ;
            display.style.width = "75%" ;
        }
        else {
            palette.style.display = "none" ;
            palette.style.width = "0%" ;
            display.style.width = "100%" ;
        }
        $scope.catSelectionPaneHidden = !$scope.catSelectionPaneHidden ;
    }
    
    // --- [END] Scope functions

    // -----------------------------------------------------------------------
    // --- [START] Local functions -------------------------------------------
    
    function initializeController() {
        initializeDateRange() ;
    }
    
    function initializeDateRange() {

        var startDt = $scope.pivotDuration.startDate ;
        var endDt = $scope.pivotDuration.endDate ;
        var text = moment( startDt ).format( 'MMM D, YYYY' ) + ' - ' +
                   moment( endDt ).format( 'MMM D, YYYY' ) ;
        
        $('#pivotDuration span').html( text ) ;            
     
        $('#pivotDuration').daterangepicker({
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
                'Last 2 Months' : [ 
                    moment().subtract(1, 'month').startOf( 'month' ), 
                    moment()
                ],
                'Last 3 Months' : [ 
                    moment().subtract(2, 'month').startOf( 'month' ), 
                    moment()
                ],
                'Last 6 Months' : [ 
                    moment().subtract(5, 'month').startOf( 'month' ), 
                    moment()
                ],
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
            $('#pivotDuration span').html( text ) ;
            $scope.pivotDuration.startDate = start.toDate() ;
            $scope.pivotDuration.endDate   = end.toDate() ;
        });
    }
    
    // ------------------- Server comm functions -----------------------------
} ) ;