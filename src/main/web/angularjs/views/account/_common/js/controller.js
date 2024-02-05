capitalystNgApp.controller( 'AccountController', 
    function( $scope, $http, $rootScope, $location, $window ) {
    
    // ---------------- Local variables --------------------------------------
    
    // ---------------- Scope variables --------------------------------------

    // Common framework variables. 
    $scope.alerts = [] ;
    $scope.navBarTitle = "<Fill navbar title>" ;
    $scope.activeTabKey = "SB" ;
    
    $scope.refData = null ;
    
    // -----------------------------------------------------------------------
    // --- [START] Controller initialization ---------------------------------
    console.log( "Loading AccountController" ) ;
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
    
    $scope.showRoute = function( routePath ) {
        console.log( "Route path = " + routePath ) ;
        $location.path( routePath ) ;
    }
    
    $scope.getTabActiveClass = function( tabKey ) {
        if( tabKey == $scope.activeTabKey ) {
            return "active" ;
        }
        return "" ;
    }
    
    // --- [END] Scope functions

    // -----------------------------------------------------------------------
    // --- [START] Local functions -------------------------------------------
    
    function initializeController() {
        loadRefData() ;
    }
    
    // ------------------- Server comm functions -----------------------------
    function loadRefData() {
        $scope.$emit( 'interactingWithServer', { isStart : true } ) ;
        $http.get( '/RefData' )
        .then ( 
            function( response ){
                $scope.refData = response.data ;
            }, 
            function( error ){
                $scope.$parent.addErrorAlert( "Error fetch RefData." ) ;
            }
        )
        .finally(function() {
            $scope.$emit( 'interactingWithServer', { isStart : false } ) ;
        }) ;
    }

} ) ;