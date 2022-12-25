capitalystNgApp.controller( 'ManageConfigController', 
    function( $scope, $http, $ngConfirm ) {
    
    // ---------------- Local variables --------------------------------------
    
    // ---------------- Scope variables --------------------------------------
    $scope.$parent.navBarTitle = "Manage system configuration" ;
    $scope.$parent.activeModuleId = "manage_config" ;
    
    $scope.allCfgs = {} ;
    $scope.cfgGroupNames = [] ;
    $scope.selectedCfgGroup = null ;
    $scope.groupedCfgs = null ;
    
    $scope.tempEditValues = {
        value       : "",
        description : ""
    } ;
    $scope.isEditing = false ;
    
    // -----------------------------------------------------------------------
    // --- [START] Controller initialization ---------------------------------
    console.log( "Loading ManageConfigController" ) ;
    fetchAllConfigs( $scope.selectedCfgGroup ) ;
    // --- [END] Controller initialization -----------------------------------
    
    // -----------------------------------------------------------------------
    // --- [START] Scope functions -------------------------------------------
    $scope.groupChanged = function() {
        $scope.groupedCfgs = $scope.allCfgs[ $scope.selectedCfgGroup ] ;
    }
    
    $scope.editConfig = function( cfg ) {
        
        if( $scope.isEditable( cfg ) ) {
            resetEditState() ;
            cfg.editing = true ;
            $scope.isEditing = true ;
            $scope.tempEditValues.value = cfg.value ;
            $scope.tempEditValues.description = cfg.description ;
        }
    }
    
    $scope.cancelEditing = function() {
        resetEditState() ;
    }
    
    $scope.saveConfig = function( cfg, descrFromTemp ) {
        if( cfg.boolFlag ) {
            cfg.value = cfg.boolValue ? "true" : "false" ;        
        }
        else {
            cfg.value = $scope.tempEditValues.value ;
        }
        
        if( descrFromTemp ) {
            cfg.description = $scope.tempEditValues.description ;
        }
        
        resetEditState() ;
        saveCfgOnServer( cfg ) ;
    }
    
    $scope.refresh = function() {
        fetchAllConfigs( $scope.selectedCfgGroup ) ;
    }
    
    $scope.isEditable = function( cfg ) {
        return !cfg.configName.includes( "(ro)" ) ;
    }
    
    // --- [END] Scope functions

    // -----------------------------------------------------------------------
    // --- [START] Local functions -------------------------------------------
    
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
    function fetchAllConfigs( selectedGroup ) {
        
        $http.get( '/AllConfig' )
        .then ( 
            function( response ){
                console.log( response.data ) ;
                
                $scope.cfgGroupNames.length = 0 ;
                if( selectedGroup != null ) {
                    $scope.selectedCfgGroup = selectedGroup ;
                }
                
                $scope.allCfgs = response.data ;
                for( const groupName in $scope.allCfgs ) {
                    $scope.cfgGroupNames.push( groupName ) ;
                    $scope.allCfgs[ groupName ].forEach( cfg => {
                        cfg.editing = false ;
                        cfg.boolValue = cfg.value == 'true' ;
                    }) ;
                    
                    if( selectedGroup == null ) {
                        $scope.selectedCfgGroup = groupName ;
                    }
                }
                
                $scope.groupChanged() ;
            }, 
            function( error ){
                $scope.$parent.addErrorAlert( "Could not fetch configs.\n" +
                                              error.data.message ) ;
            }
        )
    }
    
    function saveCfgOnServer( cfg ) {
        
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
    }
    // ------------------- Server response processors ------------------------
} ) ;