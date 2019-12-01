capitalystNgApp.controller( 'UploadAccountStmtController', 
        function( $scope, $http, Upload ) {
    
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
    $scope.uploadFiles = function( files, errFiles ) {
        $scope.files = files;
        $scope.errFiles = errFiles;
        angular.forEach( files, function( file ) {
            file.upload = Upload.upload( {
                url: 'https://angular-file-upload-cors-srv.appspot.com/upload',
                data: {file: file}
            } ) ;

            file.upload.then( function ( response ) {
                $timeout( function () {
                    file.result = response.data;
                } ) ;
            }, 
            function( response ) {
                if( response.status > 0 )
                    $scope.errorMsg = response.status + ': ' + response.data ;
            },
            function( evt ) {
                file.progress = Math.min(100, parseInt(100.0 * 
                                         evt.loaded / evt.total) ) ;
            } ) ;
        } ) ;
    }
    
    // --- [END] Scope functions

    // -----------------------------------------------------------------------
    // --- [START] Local functions -------------------------------------------
    function initializeController() {
    }
    
    
    // ------------------- Server comm functions -----------------------------
} ) ;