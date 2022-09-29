capitalystNgApp.controller( 'GraphDisplayDialogController', function( $scope ) {
    
    // ---------------- Local variables --------------------------------------
    
    // ---------------- Scope variables --------------------------------------
    $scope.holding = null ;
    $scope.eodData = [] ;
    
    // -----------------------------------------------------------------------
    // --- [START] Controller initialization ---------------------------------
    console.log( "Loading GraphDisplayDialogController" ) ;
    // --- [END] Controller initialization -----------------------------------
    
    // -----------------------------------------------------------------------
    // --- [START] Scope functions -------------------------------------------
    
    $scope.$on( 'graphDialogDisplayTrigger', function( _event, args ) {
        console.log( "Trigger obtained " ) ;
        $scope.holding = args.holding ;
        fetchChartData() ;
        drawChart() ;
    } ) ;
    
    //TODO

    $scope.hideGraphDialog = function( holding ) {
        $( '#graphDisplayDialog' ).modal( 'hide' ) ;
    }
    
    // --- [END] Scope functions

    // -----------------------------------------------------------------------
    // --- [START] Local functions -------------------------------------------
    
    // ------------------- Server comm functions -----------------------------
    function drawChart() {
        const ctx = document.getElementById('eodGraph').getContext('2d');
        
        const startDate = new Date(2020, 0, 1);
        const labels = [];
        for (let i = 0; i < 6; i++) {
          const date = moment(startDate).add(i, 'days').format('YYYY-MM-DD');
          labels.push(date.toString());
        }
        
        labels.push( new Date( 2022, 09, 30 ) ) ;
        
        const chart = new Chart(ctx, {
          type: 'line',
          data: {
            labels,
            datasets: [{
              label: '# of Votes',
              data: [12, 19, 3, 5, 2, 3],
              borderWidth: 1
            }]
          },
          options: {
            
            }
        });
    }
    
    function fetchChartData() {
    }
} ) ;