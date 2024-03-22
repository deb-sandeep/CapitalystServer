function CatNode( name, parentNode ) {
    
    this.displayName = name ;
    this.parent = parentNode ;
    this.childNodes = [] ;
    this.depth = 0 ;
    this.selected = true ;
    this.childLookupMap = new Map() ;
    
    this.addChild = function( cat ) {
        this.childNodes.push( cat ) ;
        this.childLookupMap.set( cat.displayName, cat ) ;
    }
    
    this.getChild = function( displayName ) {
        return this.childLookupMap.get( displayName ) ;
    }
    
    this.initialize = function() {
        var mother = parentNode ;
        while( mother != null ) {
            this.depth++ ;
            mother = mother.parent ;
        }
    }
    
    this.toggleSelection = function() {
        this.setSelection( this.selected ) ;
    }
    
    this.setSelection = function( state ) {
        this.selected = state ;
        for( var i = 0; i<this.childNodes.length; i++ ) {
            this.childNodes[i].setSelection( state ) ;
        }
    }
    
    this.initialize() ;
}

capitalystNgApp.controller( 'TxnPivotHomeController', 
    function( $scope, $http, $location ) {
    
    // ---------------- Local variables --------------------------------------
    var pivotSrcColNames = [ "Type", "L1", "L2", "Month", "Amount", "Remarks" ] ;
    var pivotSrcData = [] ;
    
    // ---------------- Scope variables --------------------------------------
    $scope.$parent.navBarTitle = "Pivot of Transactions" ;
    $scope.catSelectionPaneHidden = true ;
    $scope.pivotDuration = {
        startDate : moment().subtract(11, 'month').startOf( 'month' ),
        endDate : moment().endOf('day').toDate(),
    } ;

    $scope.masterCategories = {
        credit : {
            l1Categories : [],
            l2Categories : new Map()
        },
        debit : {
            l1Categories : [],
            l2Categories : new Map()
        }
    } ;
    
    $scope.categoryTreeForDisplay = [
        new CatNode( 'Income', null ),
        new CatNode( 'Expense', null ),
    ] ;
    
    $scope.catLinearTree = [] ;

    // -----------------------------------------------------------------------
    // --- [START] Controller initialization ---------------------------------
    console.log( "Loading TxnPivotHomeController" ) ;
    initializeController() ;
    // --- [END] Controller initialization -----------------------------------
    
    
    // -----------------------------------------------------------------------
    // --- [START] Scope functions -------------------------------------------
    $scope.toggleCatSelectionPane = function() {
        var palette = document.getElementById( "category-selection-pane" ) ;
        var display = document.getElementById( "txn-pivot-panel" ) ;
        
        if( $scope.catSelectionPaneHidden ) {
            palette.style.display = "block" ;
            palette.style.width = "15%" ;
            display.style.width = "85%" ;
        }
        else {
            palette.style.display = "none" ;
            palette.style.width = "0%" ;
            display.style.width = "100%" ;
        }
        $scope.catSelectionPaneHidden = !$scope.catSelectionPaneHidden ;
    }
    
    $scope.toggleCategorySelection = function( node ) {
        node.toggleSelection() ;
        renderPivotTable() ;
    }
    
    // --- [END] Scope functions

    // -----------------------------------------------------------------------
    // --- [START] Local functions -------------------------------------------
    
    function initializeController() {
        initializeDateRange() ;
        fetchClassificationCategories() ;
    }
    
    function initializeDateRange() {

        var startDt = $scope.pivotDuration.startDate ;
        var endDt = $scope.pivotDuration.endDate ;
        var text = moment( startDt ).format( 'MMM D, YYYY' ) + ' - ' +
                   moment( endDt ).format( 'MMM D, YYYY' ) ;
        
        $('#pivotDuration span').html( text ) ;            
     
        $('#pivotDuration').daterangepicker({
            format          : 'MM/DD/YYYY',
            startDate       : startDt,
            endDate         : endDt,
            showDropdowns   : true,
            showWeekNumbers : false,
            opens           : 'right',
            drops           : 'down',
            buttonClasses   : ['btn', 'btn-sm'],
            applyClass      : 'btn-primary',
            cancelClass     : 'btn-default',
            separator       : ' to ',
            
            ranges : {
                'Last 2 Months' : [ 
                    moment().subtract(1, 'month').startOf( 'month' ), 
                    moment()
                ],
                'Last 3 Months' : [ 
                    moment().subtract(2, 'month').startOf( 'month' ), 
                    moment()
                ],
                'Last 6 Months' : [ 
                    moment().subtract(5, 'month').startOf( 'month' ), 
                    moment()
                ],
                'Last 12 Months' : [ 
                    moment().subtract(11, 'month').startOf( 'month' ), 
                    moment()
                ],
                'YTD' : [ 
                    moment().startOf( 'year' ), 
                    moment()
                ],
            },
            locale : {
                applyLabel       : 'Submit',
                cancelLabel      : 'Cancel',
                fromLabel        : 'From',
                toLabel          : 'To',
                customRangeLabel : 'Custom',
                daysOfWeek       : ['Su', 'Mo', 'Tu', 'We', 'Th', 'Fr','Sa'],
                firstDay         : 1,
                monthNames       : ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 
                                    'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'],
            }
        }, 
        function( start, end, label ) {
            var text = start.format( 'MMM D, YYYY' ) + ' - ' + end.format('MMM D, YYYY') ;
            $('#pivotDuration span').html( text ) ;
            $scope.pivotDuration.startDate = start.startOf('day').toDate() ;
            $scope.pivotDuration.endDate   = end.endOf('day').toDate() ;
            fetchAndRenderPivotEntries() ;
        });
    }
    
    function printCatNode( catNode, indent ) {
        
        console.log( indent + '[' + catNode.depth + '] ' + catNode.displayName ) ;
        for( var i=0; i<catNode.childNodes.length; i++ ) {
            var node = catNode.childNodes[i] ;
            printCatNode( node, indent + "    " ) ;
        }
    }
    
    function printLinearTree() {
        
        for( var i=0; i<$scope.catLinearTree.length; i++ ) {
            var node = $scope.catLinearTree[i] ;
            var indent = "" ;
            for( var j=0; j<node.depth; j++ ) {
                indent += "   " ;
            }
            console.log( indent + node.linearIndex + "  " + node.displayName ) ;
        }
    }

    function renderPivotTable() {
        console.log( "Rendering pivot table" ) ;
        var pivotTable = new PivotTable() ;
        pivotTable.setPivotData( pivotSrcColNames, 
                                 getFilteredPivotData() ) ;
        pivotTable.initializePivotTable( 
                [ "Type", "L1", "L2" ], 
                "Month", 
                "Amount" 
        ) ;
        pivotTable.renderPivotTable( 
                "pivot_table_div",   // The id of the div 
                null,                // Caption - no caption
                pivotRenderCallback, // Formatting each cell
                null,                // Selection call back
                false,               // Expand all
                true                 // Show columns 
        ) ; 
        pivotTable.expandFirstLevel() ;
    }
    
    function getFilteredPivotData() {
        var filteredPivotSrcData = [] ;
        for( var i=0; i<pivotSrcData.length; i++ ) {
            var tupule = pivotSrcData[i] ;
            var catRoot = ( tupule[0] == 'Income' ) ?
                                            $scope.categoryTreeForDisplay[0] :
                                            $scope.categoryTreeForDisplay[1] ;
                                            
            var l1Node = catRoot.getChild( tupule[1] ) ;
            if( typeof l1Node === 'undefined' ) {
                filteredPivotSrcData.push( tupule ) ;
            }
            else {
                var l2Node = l1Node.getChild( tupule[2] ) ;
                if( l2Node === undefined ) {
                    debugger
                }
                if( l2Node.selected ) {
                    filteredPivotSrcData.push( tupule ) ;
                }
            }
        }
        
        filteredPivotSrcData.sort( function( tupule1, tupule2 ) {
            var typeCompare = tupule1[0].localeCompare( tupule2[0] ) ;
            if( typeCompare === 0 ) {
                var l1Compare = tupule1[1].localeCompare( tupule2[1] ) ;
                if( l1Compare === 0 ) {
                    const l2Compare = tupule1[2].localeCompare(tupule2[2]);
                    return l2Compare ;
                }
                return l1Compare
            }
            return -typeCompare ;
        } ) ;
        
        return filteredPivotSrcData ;
    }
    
    function pivotRenderCallback( rowIndex, colIndex, renderData ) {

        let fmt = "";
        const cellData = renderData.content;
        if( cellData != null ) {
            if( isNaN( cellData ) ) {
                fmt = cellData ;
            }
            else {
                var amt = parseFloat( cellData ) ;
                fmt = amt.toLocaleString('en-IN', {
                    maximumFractionDigits: 2,
                    style: 'currency',
                    currency: 'INR'
                });

                if( fmt.indexOf( '.' ) !== -1 ) {
                    fmt = fmt.substring( 0, fmt.indexOf( '.' ) ) ; 
                }
                
                fmt = fmt.replace( "\u20B9", "" ) ;
                fmt = fmt.replace( /\s/g, '' ) ;
                
                renderData.classes.push( amt > 0 ? 'credit' : 'debit' ) ;
            }
        }
        
        renderData.content = fmt ;
        return renderData ;
    }
    
    // ------------------- Server comm functions -----------------------------
    function fetchClassificationCategories() {
        
        $scope.$emit( 'interactingWithServer', { isStart : true } ) ;
        console.log( 'Fetching classification categories.' ) ;

        $http.get( '/Ledger/Categories' )
        .then ( 
            function( response ){
                populateMasterCategories( response.data ) ;
                createCategoryTree( $scope.masterCategories.credit, 
                                           $scope.categoryTreeForDisplay[0] ) ;
                createCategoryTree( $scope.masterCategories.debit, 
                                           $scope.categoryTreeForDisplay[1] ) ;
                fetchAndRenderPivotEntries() ;
                setTimeout( function(){
                    $( "#catTreeTable" ).treetable({ expandable: true }) ;
                    $( "#catTreeTable" ).treetable( 'expandNode', $scope.categoryTreeForDisplay[0].linearIndex ) ;
                    $( "#catTreeTable" ).treetable( 'expandNode', $scope.categoryTreeForDisplay[1].linearIndex ) ;
                }, 0 ) ;
            }, 
            function( error ){
                $scope.$parent.addErrorAlert( "Could not fetch classification categories." ) ;
            }
        )
        .finally(function() {
            $scope.$emit( 'interactingWithServer', { isStart : false } ) ;
        }) ;
    }
    
    function fetchAndRenderPivotEntries() {
        
        $scope.$emit( 'interactingWithServer', { isStart : true } ) ;
        
        $http.get( '/Ledger/PivotData?' + 
                   'startDate=' + $scope.pivotDuration.startDate.toISOString() + '&' +
                   'endDate=' + $scope.pivotDuration.endDate.toISOString() ) 
        .then ( 
            function( response ){
                pivotSrcData = response.data ;
                for( var i=0; i<pivotSrcData.length; i++ ) {
                    var tupule = pivotSrcData[i] ;
                    tupule[4] = parseFloat( tupule[4] ) ;
                }
                renderPivotTable() ;
            }, 
            function( error ){
                $scope.$parent.addErrorAlert( "Could not fetch pivot entries." ) ;
            }
        )
        .finally(function() {
            $scope.$emit( 'interactingWithServer', { isStart : false } ) ;
        }) ;
    }
    
    // ------------------- Server response processors ------------------------
    function populateMasterCategories( categories ) {
        
        $scope.masterCategories.credit.l1Categories.length = 0 ;
        $scope.masterCategories.credit.l2Categories.clear() ;

        $scope.masterCategories.debit.l1Categories.length = 0 ;
        $scope.masterCategories.debit.l2Categories.clear() ;

        $scope.selectedL1Category = null ;
        $scope.selectedL2Category = null ;
        
        for( let i=0; i<categories.length; i++ ) {
            const category = categories[i];
            if( category.creditClassification ) {
                classifyCategoryInMasterList( 
                        $scope.masterCategories.credit.l1Categories, 
                        $scope.masterCategories.credit.l2Categories,
                        category ) ; 
            }
            else {
                classifyCategoryInMasterList( 
                        $scope.masterCategories.debit.l1Categories, 
                        $scope.masterCategories.debit.l2Categories,
                        category ) ; 
            }
        }
    }
    
    function classifyCategoryInMasterList( l1CatList, l2CatMap, category ) {

        const l1 = category.l1CatName;
        const l2 = category.l2CatName;

        console.log( l1 + "::" + l2 ) ;

        if( l1CatList.indexOf( l1 ) === -1 ) {
            l1CatList.push( l1 ) ;
        }
        
        if( !l2CatMap.has( l1 ) ) {
            l2CatMap.set( l1, [] ) ;
        }

        const l2List = l2CatMap.get(l1);
        l2List.push( [ l2, category.selectedForTxnPivot ] ) ;
    }
    
    function createCategoryTree( masterCategoryCluster, rootDisplayCatNode ) {
        
        rootDisplayCatNode.linearIndex = $scope.catLinearTree.length + 1 ;
        $scope.catLinearTree.push( rootDisplayCatNode ) ;

        for( let i=0; i<masterCategoryCluster.l1Categories.length; i++ ) {
            const l1CatName = masterCategoryCluster.l1Categories[i];
            const l1Node = new CatNode(l1CatName, rootDisplayCatNode);

            l1Node.linearIndex = $scope.catLinearTree.length + 1 ;
            $scope.catLinearTree.push( l1Node ) ;
            
            rootDisplayCatNode.addChild( l1Node ) ;
            const l2Nodes = masterCategoryCluster.l2Categories.get(l1CatName);
            let anySelected = false;

            for( let j=0; j<l2Nodes.length; j++ ) {
                const l2NodeMeta = l2Nodes[j];
                const l2Node = new CatNode(l2NodeMeta[0], l1Node);

                l2Node.selected = l2NodeMeta[1] ;
                l2Node.linearIndex = $scope.catLinearTree.length + 1 ;
                
                l1Node.addChild( l2Node ) ;
                $scope.catLinearTree.push( l2Node ) ;
                
                anySelected |= l2Node.selected ;
            }
            l1Node.selected = anySelected ;
        }
    }
    
} ) ;
