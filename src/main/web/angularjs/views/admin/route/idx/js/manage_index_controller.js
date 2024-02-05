capitalystNgApp.controller( 'ManageIndexController', 
    function( $scope, $http ) {
    
    // ---------------- Local variables --------------------------------------
    
    // ---------------- Scope variables --------------------------------------
    $scope.$parent.navBarTitle = "Manage indexes" ;
    $scope.$parent.activeModuleId = "idx_management" ;
    
    $scope.indexTypes = [
       "Broad Market",
       "Sectoral",
       "Thematic",
       "Strategy"
    ] ;
    
    $scope.idxList = [] ;
    $scope.idxUnderEdit = {
        id : -1,
        name : null,
        type : null,
        includedStocksUrl : null,
        description : null
    } ;
    
    $scope.messages = [] ;
    
    // -----------------------------------------------------------------------
    // --- [START] Controller initialization ---------------------------------
    console.log( "Loading ManageIndexController" ) ;
    initializeController() ;
    // --- [END] Controller initialization -----------------------------------
    
    // -----------------------------------------------------------------------
    // --- [START] Scope functions -------------------------------------------
    
    // --- [START] Scope functions dealilng with non UI logic ----------------
    $scope.addMessage = function( msgString ) {
        $scope.messages.push( msgString ) ;
    } ;
    
    $scope.dismissMessage = function( index ) {
        $scope.messages.splice( index, 1 ) ;
    }
    
    $scope.showIndexMetaEditDialog = function( meta ) {
        populateIdxUnderEdit( meta ) ;
        $( '#idxMetaEditorDialog' ).modal( 'show' ) ;
    }
    
    $scope.saveEditedIndexMeta = function() {
        saveIndexMaster( $scope.idxUnderEdit, function( updatedMeta ){
            
            var existing = false ;
            for( var i=0; i<$scope.idxList.length; i++ ) {
                var meta = $scope.idxList[i] ;
                if( updatedMeta.id == meta.id ) {
                    meta.type = updatedMeta.type ;
                    meta.name = updatedMeta.name ;
                    meta.includedStocksUrl = updatedMeta.includedStocksUrl ;
                    meta.description = updatedMeta.description ;
                    
                    existing = true ;
                    break ;
                }
            }
            
            if( !existing ) {
                $scope.idxList.push( updatedMeta ) ;
            }
            
            $scope.addMessage( "Index " + updatedMeta.name + " updated" ) ;
            $scope.hideIndexMetaEditDialog() ;
        } ) ;
    }
    
    $scope.hideIndexMetaEditDialog = function() {
        clearState() ;
        $( '#idxMetaEditorDialog' ).modal( 'hide' ) ;
    }
    
    $scope.refreshIndex = function( idx ) {
        refreshIndexMaster( idx ) ;
    }
    
    // --- [END] Scope functions

    // -----------------------------------------------------------------------
    // --- [START] Local functions -------------------------------------------
    
    function initializeController() {
        fetchIndexMasters() ;
    }
    
    function clearState() {
        populateIdxUnderEdit( null ) ;
    }
    
    function populateIdxUnderEdit( ref ) {
        if( ref != null ) {
            $scope.idxUnderEdit.id   = ref.id ;
            $scope.idxUnderEdit.name = ref.name ;
            $scope.idxUnderEdit.type = ref.type ;
            $scope.idxUnderEdit.includedStocksUrl = ref.includedStocksUrl ;
            $scope.idxUnderEdit.description = ref.description ;
        }
        else {
            $scope.idxUnderEdit.id   = null ;
            $scope.idxUnderEdit.name = null ;
            $scope.idxUnderEdit.type = null ;
            $scope.idxUnderEdit.includedStocksUrl = null ;
            $scope.idxUnderEdit.description = null ;
        }
    }
    
    // ------------------- Server comm functions -----------------------------
    function fetchIndexMasters() {
        
        clearState() ;
        
        $scope.$emit( 'interactingWithServer', { isStart : true } ) ;
        $http.get( '/IndexMaster' )
        .then ( 
            function( response ){
                $scope.idxList.length = 0 ;
                for( var i=0; i<response.data.length; i++ ) {
                    $scope.idxList.push( response.data[i] ) ;
                }
            }, 
            function( error ){
                $scope.$parent.addErrorAlert( "Could not fetch index masters.\n" +
                                              error.data.message ) ;
            }
        )
        .finally(function() {
            $scope.$emit( 'interactingWithServer', { isStart : false } ) ;
        }) ;
    }
    
    function saveIndexMaster( master, callback ) {
        
        $scope.$emit( 'interactingWithServer', { isStart : true } ) ;
        $http.post( '/IndexMaster', master )
        .then ( 
            function( response ){
                callback( response.data ) ;
            }, 
            function( error ){
                $scope.$parent.addErrorAlert( "Could not dave index masters.\n" +
                                              error.data.message ) ;
            }
        )
        .finally(function() {
            $scope.$emit( 'interactingWithServer', { isStart : false } ) ;
        }) ;
    }
    
    function refreshIndexMaster( idx ) {
        
        $scope.$emit( 'interactingWithServer', { isStart : true } ) ;
        $http.post( '/IndexMaster/Refresh/' + idx.id )
        .then ( 
            function( response ){
                $scope.addMessage( response.data.message + " for index " + idx.name ) ;
            }, 
            function( error ){
                $scope.$parent.addErrorAlert( "Could not save index masters.\n" +
                                              error.data.message ) ;
            }
        )
        .finally(function() {
            $scope.$emit( 'interactingWithServer', { isStart : false } ) ;
        }) ;
    }    
} ) ;