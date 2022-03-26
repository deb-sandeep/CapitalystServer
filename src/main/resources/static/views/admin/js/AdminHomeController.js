capitalystNgApp.controller( 'AdminHomeController', function( $scope ) {
    
    // ---------------- Local variables --------------------------------------
    
    // ---------------- Scope variables --------------------------------------
    $scope.$parent.navBarTitle = "Administrative functions" ;
    $scope.menuPanelHidden = false ;

    // -----------------------------------------------------------------------
    // --- [START] Controller initialization ---------------------------------
    console.log( "Loading AdminHomeController" ) ;
    initializeController() ;
    // --- [END] Controller initialization -----------------------------------
    
    
    // -----------------------------------------------------------------------
    // --- [START] Scope functions -------------------------------------------
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
    
    $scope.toggleCategorySelection = function( node ) {
        node.toggleSelection() ;
    }
    // --- [END] Scope functions

    // -----------------------------------------------------------------------
    // --- [START] Local functions -------------------------------------------
    
    function initializeController() {
    }
    
    // ------------------- Server comm functions -----------------------------
    
    // ------------------- Server response processors ------------------------
} ) ;