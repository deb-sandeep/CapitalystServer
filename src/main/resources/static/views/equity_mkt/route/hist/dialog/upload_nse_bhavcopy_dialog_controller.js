capitalystNgApp.controller( 'UploadNSEBhavcopyController', 
        function( $scope, $http, Upload, $timeout ) {
    
    // ---------------- Local variables --------------------------------------
    
    // ---------------- Scope variables --------------------------------------
    $scope.files = null ;
    $scope.results = null ;
    $scope.errorMsg = null ;
    $scope.progress = 0 ;
    
    // -----------------------------------------------------------------------
    // --- [START] Controller initialization ---------------------------------
    console.log( "Loading UploadNSEBhavcopyController" ) ;
    // --- [END] Controller initialization -----------------------------------
    
    // -----------------------------------------------------------------------
    // --- [START] Scope event listeners -------------------------------------
    
    // --- [START] Scope functions -------------------------------------------
    $scope.uploadFiles = function( files ) {
        $scope.files = files ;
        if( files && files.length ) {
            Upload.upload( {
                url: '/Equity/NSEBhavcopy/FileUpload',
                arrayKey:'',
                data: {
                    files: files
                }
            })
            .then( function( response ) {
                $timeout( function () {
                    $scope.results = response.data ;
                }) ;
            }, 
            function( response ) {
                if( response.status > 0 ) {
                    $scope.errorMsg = response.status + ': ' + response.data ;
                }
            }, 
            function( evt ) {
                var pct = parseInt(100.0 * evt.loaded / evt.total) ;
                $scope.progress = Math.min( 100, pct ) ;
            } ) ;
        } 
    }
    
    $scope.hideDialog = function() {
        resetState() ;
        $( '#uploadNSEBhavcopyDialog' ).modal( 'hide' ) ;
        $scope.$parent.refreshTable() ;
    }
    
    // --- [END] Scope functions

    // -----------------------------------------------------------------------
    // --- [START] Local functions -------------------------------------------
    function resetState() {
        $scope.files = null ;
        $scope.results = null ;
        $scope.errorMsg = null ;
        $scope.progress = 0 ;
    }
    
    // ------------------- Server comm functions -----------------------------
} ) ;