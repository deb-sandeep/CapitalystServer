capitalystNgApp.controller( 'ManageClassificationRulesController', 
    function( $scope, $http, $ngConfirm ) {
    
    // ---------------- Local variables --------------------------------------
    
    // ---------------- Scope variables --------------------------------------
    $scope.$parent.navBarTitle = "Manage classification rules" ;
    $scope.$parent.activeModuleId = "classification_rules" ;
    
    // This is used to highlight the Credit or Debit button on the page
    // This is updated when either of the buttons are clicked.
    $scope.activeCategory = "Debit" ;
    
    // Classification of the raw categories data into an usable data structure
    // Note that l1Categories store instances of CatName while l2Category
    // key is the display name of CatName and the value is the category
    // instance returned by the server.
    $scope.ledgerCategories = {
       credit : {
           l1Categories : [],
           l2Categories : new Map()
       },
       debit : {
           l1Categories : [],
           l2Categories : new Map()
       }
    } ;    
    
    $scope.catData = null ;
    
    $scope.rules = [] ;
    
    $scope.ruleUnderEdit = {
        ruleId : null,
        creditClassifier : false,
        ruleName : null,
        l1CatName : null,
        l2CatName : null,
        ruleText : null,
        validationStatus : null,
        validationMsg : null
    } ;
    
    $scope.ledgerEntriesForDisplay = [] ;

    // -----------------------------------------------------------------------
    // --- [START] Controller initialization ---------------------------------
    console.log( "Loading ManageClassificationRulesController" ) ;
    initializeController() ;
    // --- [END] Controller initialization -----------------------------------
    
    // -----------------------------------------------------------------------
    // --- [START] Scope functions -------------------------------------------
    
    $scope.getTabActivationClass = function( id ) {
        return (id == $scope.activeCategory)? "btn-primary" : "btn-secondary" ;
    }
    
    $scope.showCreditEntries = function() {
        $scope.activeCategory = "Credit" ;
        $scope.catData = $scope.ledgerCategories.credit ;
    }
    
    $scope.showDebitEntries = function() {
        $scope.activeCategory = "Debit" ;
        $scope.catData = $scope.ledgerCategories.debit ;
    }
    
    $scope.isRuleVisible = function( rule ) {
        if( rule.creditClassifier ) {
            return $scope.activeCategory == 'Credit' ;
        }
        else {
            return $scope.activeCategory == 'Debit' ;
        }
    }
    
    $scope.showRuleEditor = function( rule ) {
        setRuleUnderEdit( rule ) ;
        $( '#ruleEditorDialog' ).modal( 'show' ) ;
    }
    
    $scope.hideRuleEditor = function() {
        clearRuleUnderEdit() ;
        $( '#ruleEditorDialog' ).modal( 'hide' ) ;
    }
    
    $scope.getRuleEditBoxClass = function() {
        var status = $scope.ruleUnderEdit.validationStatus ; 
        if( status == null ) {
            return "" ;
        }
        else if( status == "OK" ) {
            return "valid-rule" ;
        }
        return "invalid-rule" ;
    }
    
    $scope.validateEditedRule = function() {
        validateRuleOnServer( function() {
            $scope.ruleUnderEdit.validationStatus = "OK" ;
            $scope.ruleUnderEdit.validationMsg = null ;
        }, function( errMsg ) {
            $scope.ruleUnderEdit.validationStatus = "Error" ;
            $scope.ruleUnderEdit.validationMsg = errMsg ;
        } ) ;
    }
    
    $scope.saveEditedRule = function() {
        saveRuleOnServer() ;
    }
    
    $scope.addNewRule = function() {
        clearRuleUnderEdit() ;
        $scope.showRuleEditor() ;
    }
    
    $scope.deleteRule = function( rule ) {
        deleteRuleOnServer( rule ) ;
    }
    
    $scope.showMatchedEntries = function( rule ) {
        fetchMatchingEntries( rule, function(){
            $( "#viewLedgerEntriesDialog" ).modal( 'show' ) ;
        }) ;
    }
    
    $scope.hideLedgerEntriesDialog = function() {
        $scope.ledgerEntriesForDisplay.length = 0 ;
        $( "#viewLedgerEntriesDialog" ).modal( 'hide' ) ;
    }
    
    $scope.executeRule = function( rule ) {
        executeRuleOnServer( rule ) ;
    }

    $scope.executeAllRules = function() {
        executeAllRulesOnServer() ;
    }

    // --- [END] Scope functions

    // -----------------------------------------------------------------------
    // --- [START] Local functions -------------------------------------------
    
    function initializeController() {
        fetchClassificationCategories() ;
    }
    
    function clearRuleUnderEdit() {
        $scope.ruleUnderEdit.ruleId           = null ;
        $scope.ruleUnderEdit.creditClassifier = ( $scope.activeCategory == 'Credit' ) ;
        $scope.ruleUnderEdit.ruleName         = null ;
        $scope.ruleUnderEdit.l1CatName        = null ;
        $scope.ruleUnderEdit.l2CatName        = null ;
        $scope.ruleUnderEdit.ruleText         = null ;
        $scope.ruleUnderEdit.validationStatus = null ;
        $scope.ruleUnderEdit.validationMsg    = null ;
        
    }
    
    function setRuleUnderEdit( rule ) {
        clearRuleUnderEdit() ;
        if( rule != null ) {
            $scope.ruleUnderEdit.ruleId           = rule.id ;
            $scope.ruleUnderEdit.creditClassifier = rule.creditClassifier ;
            $scope.ruleUnderEdit.ruleName         = rule.ruleName ;
            $scope.ruleUnderEdit.l1CatName        = rule.l1Category ;
            $scope.ruleUnderEdit.l2CatName        = rule.l2Category ;
            $scope.ruleUnderEdit.ruleText         = rule.ruleText ;
            $scope.ruleUnderEdit.validationStatus = null ;
            $scope.ruleUnderEdit.validationMsg    = null ;
        }
    }
    
    // ------------------- Server comm functions -----------------------------
    function fetchClassificationCategories() {
        
        // Reset the internal data structures since this can be called both
        // during initialization and runtime.
        $scope.ledgerCategories.credit.l1Categories.length = 0 ;
        $scope.ledgerCategories.credit.l2Categories.clear() ;
        $scope.ledgerCategories.debit.l1Categories.length = 0 ;  
        $scope.ledgerCategories.debit.l2Categories.clear() ;
        
        $scope.$emit( 'interactingWithServer', { isStart : true } ) ;
        $http.get( '/Ledger/Categories' )
        .then ( 
            function( response ){
                populateMasterCategories( response.data ) ;
                fetchClassificationRules() ;
            }, 
            function( error ){
                $scope.$parent.addErrorAlert( "Could not fetch classification categories.\n" +
                                              error.data.message ) ;
            }
        )
        .finally(function() {
            $scope.$emit( 'interactingWithServer', { isStart : false } ) ;
        }) ;
    }
    
    function fetchClassificationRules() {
        
        $scope.rules.length = 0 ;
        
        $scope.$emit( 'interactingWithServer', { isStart : true } ) ;
        $http.get( '/Ledger/ClassificationRule' )
        .then ( 
            function( response ){
                
                for( var i=0; i<response.data.length; i++ ) {
                    var rule = response.data[i] ;
                    // Additional injected attribute
                    rule.matchCount = 0 ;
                    $scope.rules.push( rule ) ;
                }
                
                fetchRuleMatchCounter() ;
            }, 
            function( error ){
                $scope.$parent.addErrorAlert( "Could not fetch classification categories.\n" +
                                              error.data.message ) ;
            }
        )
        .finally(function() {
            $scope.$emit( 'interactingWithServer', { isStart : false } ) ;
        }) ;
    }
    
    function fetchRuleMatchCounter() {
        
        $scope.$emit( 'interactingWithServer', { isStart : true } ) ;
        $http.get( '/Ledger/ClassificationRule/MatchingCounter' )
        .then ( 
            function( response ){
                
                for( var i=0; i<response.data.length; i++ ) {
                    
                    var counter = response.data[i] ;
                    
                    for( var j=0; j<$scope.rules.length; j++ ) {
                        
                        var rule = $scope.rules[j] ;
                        if( rule.id == counter.ruleId ) {
                            rule.matchCount = counter.numMatches ;
                        }
                    }
                }
            }, 
            function( error ){
                $scope.$parent.addErrorAlert( "Could not fetch rule match count.\n" +
                                              error.data.message ) ;
            }
        )
        .finally(function() {
            $scope.$emit( 'interactingWithServer', { isStart : false } ) ;
        }) ;
    }
    
    function fetchMatchingEntries( rule, callback ) {
        
        $scope.ledgerEntriesForDisplay.length = 0 ;
        
        $scope.$emit( 'interactingWithServer', { isStart : true } ) ;
        $http.get( '/Ledger/ClassificationRule/MatchingEntries/' + rule.id )
        .then ( 
            function( response ){
                console.log( response.data ) ;
                for( var i=0; i<response.data.length; i++ ) {
                    var entry = response.data[i] ;
                    $scope.ledgerEntriesForDisplay.push( entry ) ;
                }
                callback() ;
            }, 
            function( error ){
                $scope.$parent.addErrorAlert( "Could not fetch classification categories.\n" +
                                              error.data.message ) ;
            }
        )
        .finally(function() {
            $scope.$emit( 'interactingWithServer', { isStart : false } ) ;
        }) ;
    }
    
    function validateRuleOnServer( successCallback, errorCallback ) {
        
        $http.post( '/Ledger/ClassificationRule/Validate', 
                   $scope.ruleUnderEdit.ruleText )
        .then ( 
            function(){
                successCallback() ;
            }, 
            function( error ){
                errorCallback( error.data.message ) ;
            }
        )
    }
    
    function saveRuleOnServer() {

        var editedAttribs = $scope.ruleUnderEdit ;
        
        $scope.$emit( 'interactingWithServer', { isStart : true } ) ;
        $http.post( '/Ledger/ClassificationRule', {
            id               : editedAttribs.ruleId,
            creditClassifier : editedAttribs.creditClassifier,
            l1Category       : editedAttribs.l1CatName,
            l2Category       : editedAttribs.l2CatName,
            ruleName         : editedAttribs.ruleName,
            ruleText         : editedAttribs.ruleText
        } )
        .then ( 
            function(){
                $scope.hideRuleEditor() ;
                fetchClassificationRules() ;
            }, 
            function( error ){
                $scope.$parent.addErrorAlert( "Could not fetch classification categories.\n" +
                                              error.data.message ) ;
            }
        )
        .finally(function() {
            $scope.$emit( 'interactingWithServer', { isStart : false } ) ;
        }) ;
    }
    
    function executeRuleOnServer( rule ) {

        $scope.$emit( 'interactingWithServer', { isStart : true } ) ;
        $http.post( '/Ledger/ClassificationRule/Execute/' + rule.id )
        .then ( 
            function( response ){
                $ngConfirm( response.data.message ) ;
            }, 
            function( error ){
                $scope.$parent.addErrorAlert( "Could not execute rule.\n" +
                                              error.data.message ) ;
            }
        )
        .finally(function() {
            $scope.$emit( 'interactingWithServer', { isStart : false } ) ;
        }) ;
    }
    
    function executeAllRulesOnServer() {

        $scope.$emit( 'interactingWithServer', { isStart : true } ) ;
        $http.post( '/Ledger/ClassificationRule/ExecuteAll' )
        .then ( 
            function( response ){
                $ngConfirm( response.data.message ) ;
            }, 
            function( error ){
                $scope.$parent.addErrorAlert( "Could not execute rule.\n" +
                                              error.data.message ) ;
            }
        )
        .finally(function() {
            $scope.$emit( 'interactingWithServer', { isStart : false } ) ;
        }) ;
    }
    
    function deleteRuleOnServer( rule ) {

        $scope.$emit( 'interactingWithServer', { isStart : true } ) ;
        $http.delete( '/Ledger/ClassificationRule/' + rule.id )
        .then ( 
            function(){
                fetchClassificationRules() ;
            }, 
            function( error ){
                $scope.$parent.addErrorAlert( "Could not delete rule.\n" +
                                              error.data.message ) ;
            }
        )
        .finally(function() {
            $scope.$emit( 'interactingWithServer', { isStart : false } ) ;
        }) ;
    }
    
    // ------------------- Server response processors ------------------------
    
    function populateMasterCategories( categories ) {
        
        for( var i=0; i<categories.length; i++ ) {
            
            var category = categories[i] ;
            
            // Extention attributes added to the server returned obj
            category.beingEdited = false ;
            category.numLedgerEntries = 0 ;
            
            if( category.creditClassification ) {
                classifyCategoryInMasterList( $scope.ledgerCategories.credit,
                                              category ) ; 
            }
            else {
                classifyCategoryInMasterList( $scope.ledgerCategories.debit,
                                              category ) ; 
            }
        }
        $scope.catData = $scope.ledgerCategories.debit ;
    }
    
    function classifyCategoryInMasterList( categoryData, category ) {
        
        var l1CatList = categoryData.l1Categories ;
        var l2CatMap  = categoryData.l2Categories ;
        var l1CatName = category.l1CatName ;
        var l2CatName = category.l2CatName ;
        
        if( l1CatList.filter( e => e == l1CatName ).length <= 0 ) {
            l1CatList.push( l1CatName ) ;
        }
        
        if( !l2CatMap.has( l1CatName ) ) {
            l2CatMap.set( l1CatName, [] ) ;
        }
        
        var l2List = l2CatMap.get( l1CatName ) ;
        l2List.push( l2CatName ) ;
    }
    
} ) ;