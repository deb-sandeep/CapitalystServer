capitalystNgApp.controller( 'EquityMktLandingController', 
    function( $scope ) {
    
    // ---------------- Local variables --------------------------------------
    
    // ---------------- Scope variables --------------------------------------

    // Common framework variables. 
    $scope.alerts = [] ;
    $scope.navBarTitle = "Equity Market" ;
    $scope.activeModuleId = "portfolio" ;
    
    $scope.operatingFY = null ;
    $scope.fyChoices = getFYChoices() ;
    
    // -----------------------------------------------------------------------
    // --- [START] Controller initialization ---------------------------------
    console.log( "Loading EquityMktLandingController" ) ;
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

    $scope.$on( 'graphDialogDisplayTrigger', function( _event, args ) {
        $scope.$broadcast( 'graphDialogDisplay', args ) ;
    } ) ;
    
    // ----------- UI related scope functions --------------------------------
    $scope.addErrorAlert = function( msgString ) {
        console.log( msgString ) ;
        $scope.alerts.push( { type: 'danger', msg: msgString } ) ;
    } ;
    
    $scope.dismissAlert = function( index ) {
        $scope.alerts.splice( index, 1 ) ;
    }
    
    $scope.getActiveClass = function( moduleId ) {
        if( $scope.activeModuleId == moduleId ) {
            return "active" ;
        }
        return "" ;
    }
    
    // --- [END] Scope functions

    // -----------------------------------------------------------------------
    // --- [START] Local functions -------------------------------------------
    
    function initializeController() {
    }
    
    function getFYChoices() {
        
        var choices = [] ;
        var curFY = getCurrentFY() ;
        
        for( var i=curFY-5; i<=curFY; i++ ) {
            var prevChoice = choices.length > 0 ? choices[choices.length-1] : null ;
            choices.push( {
                label : 'FY ' + i,
                value : i,
                prevChoice : prevChoice,
                nextChoice : null
            } ) ;
            
            if( prevChoice != null ) {
                prevChoice.nextChoice = choices[choices.length-1] ;
            }
            
            if( i == curFY ) {
                $scope.operatingFY = choices[ choices.length - 1 ] ;
            }
        }
        return choices ;
    }
    
    function getCurrentFY() {
      const date = new Date() ;
      
      var year = date.getFullYear() ;
      var month = date.getMonth() ;
      
      return ( month >=0 && month <=2 ) ? year-1 : year ;
    }
    
    // ------------------- Server comm functions -----------------------------
} ) ;