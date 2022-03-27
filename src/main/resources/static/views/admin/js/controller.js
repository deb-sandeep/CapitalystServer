capitalystNgApp.controller( 'AdminLandingController', 
    function( $scope ) {
    
    // ---------------- Local variables --------------------------------------
    
    // ---------------- Scope variables --------------------------------------

    // Common framework variables. 
    $scope.alerts = [] ;
    $scope.navBarTitle = "Administrative functions" ;
    $scope.menuPanelHidden = false ;
    
    // -----------------------------------------------------------------------
    // --- [START] Controller initialization ---------------------------------
    console.log( "Loading AdminLandingController" ) ;
    initializeController() ;
    // --- [END] Controller initialization -----------------------------------
    
    
    // -----------------------------------------------------------------------
    // --- [START] Scope functions -------------------------------------------
    $scope.$on( 'interactingWithServer', function( event, args ) {
        if( args.isStart ) {
            $( '#serverInteractionLoader' ).modal( 'show' ) ;
        }
        else {
            $( '#serverInteractionLoader' ).modal( 'hide' ) ;
        }
    } ) ;

    // ----------- UI related scope functions --------------------------------
    $scope.addErrorAlert = function( msgString ) {
        console.log( msgString ) ;
        $scope.alerts.push( { type: 'danger', msg: msgString } ) ;
    } ;
    
    $scope.dismissAlert = function( index ) {
        $scope.alerts.splice( index, 1 ) ;
    }
    
    $scope.toggleMenuPane = function() {
        var menu = document.getElementById( "menu-pane" ) ;
        var fnPanel = document.getElementById( "admin-fn-panel" ) ;
        
        if( $scope.menuPanelHidden ) {
            menu.style.display = "block" ;
            menu.style.width = "15%" ;
            fnPanel.style.width = "85%" ;
        }
        else {
            menu.style.display = "none" ;
            menu.style.width = "0%" ;
            fnPanel.style.width = "100%" ;
        }
        $scope.menuPanelHidden = !$scope.menuPanelHidden ;
    }
    
    // --- [END] Scope functions

    // -----------------------------------------------------------------------
    // --- [START] Local functions -------------------------------------------
    
    function initializeController() {
    }
    
    // ------------------- Server comm functions -----------------------------
} ) ;