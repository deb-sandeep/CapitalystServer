capitalystNgApp.controller( 'UploadAccountStmtController', 
        function( $scope, $http, Upload, $timeout ) {
    
    // ---------------- Local variables --------------------------------------
    
    // ---------------- Scope variables --------------------------------------
    
    // -----------------------------------------------------------------------
    // --- [START] Controller initialization ---------------------------------
    console.log( "Loading UploadAccountStmtController" ) ;
    initializeController() ;
    // --- [END] Controller initialization -----------------------------------
    
    // -----------------------------------------------------------------------
    // --- [START] Scope event listeners -------------------------------------
    
    // --- [START] Scope functions -------------------------------------------
    $scope.uploadFiles = function( files ) {
        $scope.files = files ;
        if( files && files.length ) {
            Upload.upload( {
                url: '/Account/Statement/Upload',
                data: {
                    files: files
                }
            } )
            .then( function( response ) {
                $timeout( function () {
                    $scope.result = response.data ;
                } ) ;
            }, 
            function( response ) {
                if( response.status > 0 ) {
                    $scope.errorMsg = response.status + ': ' + response.data;
                }
            }, 
            function( evt ) {
                var pct = parseInt( 100.0 * evt.loaded / evt.total ) ;
                $scope.progress = Math.min( 100, pct ) ;
            });
        }
    }
    
    $scope.cancelDialog = function() {
        $( '#uploadAccountStmtDialog' ).modal( 'hide' ) ;
    }
    
    // --- [END] Scope functions

    // -----------------------------------------------------------------------
    // --- [START] Local functions -------------------------------------------
    function initializeController() {
    }
    
    
    // ------------------- Server comm functions -----------------------------
} ) ;