capitalystNgApp.controller( 'ManageConfigController', 
    function( $scope, $http, $ngConfirm ) {
    
    // ---------------- Local variables --------------------------------------
    const NO_GROUP = "-- NO GROUP --" ;
    
    // ---------------- Scope variables --------------------------------------
    $scope.$parent.navBarTitle = "Manage system configuration" ;
    $scope.$parent.activeModuleId = "manage_config" ;
    
    $scope.allCfgs = {} ;
    $scope.cfgGroupNames = [] ;
    $scope.selectedCfgGroup = NO_GROUP ;
    $scope.groupedCfgs = null ;
    
    $scope.tempEditValues = {
        value : "",
        description : ""
    } ;
    $scope.isEditing = false ;
    
    // -----------------------------------------------------------------------
    // --- [START] Controller initialization ---------------------------------
    console.log( "Loading ManageConfigController" ) ;
    initializeController() ;
    // --- [END] Controller initialization -----------------------------------
    
    // -----------------------------------------------------------------------
    // --- [START] Scope functions -------------------------------------------
    $scope.groupChanged = function() {
        $scope.groupedCfgs = $scope.allCfgs[ $scope.selectedCfgGroup ] ;
    }
    
    $scope.showNewGroupDialog = function() {
        $( '#newGroupDialog' ).modal( 'show' ) ;
    }
    
    $scope.saveNewGroup = function() {
        $scope.hideNewGroupDialog() ;
    }
    
    $scope.hideNewGroupDialog = function() {
        $( '#newGroupDialog' ).modal( 'hide' ) ;
    }
    
    $scope.editConfig = function( cfg ) {
        resetEditState() ;
        cfg.editing = true ;
        $scope.isEditing = true ;
        $scope.tempEditValues.value = cfg.value ;
        $scope.tempEditValues.description = cfg.description ;
    }
    
    $scope.cancelEditing = function() {
        resetEditState() ;
    }
    
    $scope.saveConfig = function( cfg ) {
        cfg.value = $scope.tempEditValues.value ;
        cfg.description = $scope.tempEditValues.description ;
        resetEditState() ;
        saveCfgOnServer( cfg ) ;
    }
    
    // --- [END] Scope functions

    // -----------------------------------------------------------------------
    // --- [START] Local functions -------------------------------------------
    
    function initializeController() {
        fetchClassificationCategories() ;
    }
    
    function resetEditState() {
        
        $scope.isEditing = false ;
        $scope.tempEditValues.value = null ;
        $scope.tempEditValues.description = null ;
        
        for( const groupName in $scope.allCfgs ) {
            $scope.allCfgs[ groupName ].forEach( cfg => {
                cfg.editing = false ;
            }) ;
        }
    }
    
    // ------------------- Server comm functions -----------------------------
    function fetchClassificationCategories() {
        
        $scope.$emit( 'interactingWithServer', { isStart : true } ) ;
        $http.get( '/AllConfig' )
        .then ( 
            function( response ){
                console.log( response.data ) ;
                
                $scope.cfgGroupNames.length = 0 ;
                $scope.selectedCfgGroup = NO_GROUP ;
                
                $scope.allCfgs = response.data ;
                for( const groupName in $scope.allCfgs ) {
                    $scope.cfgGroupNames.push( groupName ) ;
                    $scope.allCfgs[ groupName ].forEach( cfg => {
                        cfg.editing = false ;
                    }) ;
                }
                
                $scope.groupChanged() ;
            }, 
            function( error ){
                $scope.$parent.addErrorAlert( "Could not fetch configs.\n" +
                                              error.data.message ) ;
            }
        )
        .finally(function() {
            $scope.$emit( 'interactingWithServer', { isStart : false } ) ;
        }) ;
    }
    
    function saveCfgOnServer( cfg ) {
        
        $scope.$emit( 'interactingWithServer', { isStart : true } ) ;
        $http.post( '/Config', cfg )
        .then ( 
            function( response ){
                console.log( response.data ) ;
            }, 
            function( error ){
                $scope.$parent.addErrorAlert( "Could not save config.\n" +
                                              error.data.message ) ;
            }
        )
        .finally(function() {
            $scope.$emit( 'interactingWithServer', { isStart : false } ) ;
        }) ;
    }
    // ------------------- Server response processors ------------------------
} ) ;