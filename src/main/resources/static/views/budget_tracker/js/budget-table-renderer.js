function triggerLedgerEntryDisplay( entryId, startOfMonth ) {
    console.log( entryId ) ;
    
    var controllerElement = document.getElementById( 'bodyId' ) ;
    var angularElement    = angular.element( controllerElement ) ;
    var scope             = angularElement.scope() ;
    
    scope.triggerLedgerEntryDisplay( entryId, startOfMonth ) ;
}

function BudgetTableRenderer() {

    var spread = null ;
    var showPlanned = false ;
    var showAvailable = false ;
    var showConsumed = false ;
    
    this.render = function( spreadData, userChoices ) {

        spread = spreadData ;
        showPlanned = userChoices.showPlanned ;
        showAvailable = userChoices.showAvailable ;
        showConsumed = userChoices.showConsumed ;
        
        var tableDOM = getTableDOM() ;
        var container = document.getElementById( "budget-table-div" ) ;
        while( container.firstChild ) {
            container.removeChild( container.firstChild ) ;
        }
        container.appendChild( tableDOM ) ;
        
        $( "#budget-spread-table" ).treetable( { expandable: true } ) ;
    }
    
    this.changeL1ExpansionState = function( expand ) {
        
        var table = $( "#budget-spread-table" ) ;
        var cmd   = expand ? "expandNode" : "collapseNode" ; 
        
        for( var i=0; i<spread.l1LineItems.length; i++ ) {
            table.treetable( cmd, "L1-" + i ) ;
        }
    }

    this.changeTreeExpansionState = function( expand ) {
        
        var table = $( "#budget-spread-table" ) ; 
        var cmd   = expand ? "expandNode" : "collapseNode" ; 
        
        for( var i=0; i<spread.l1LineItems.length; i++ ) {
            
            table.treetable( cmd, "L1-" + i ) ;

            var l1LineItem = spread.l1LineItems[i] ;
            for( var j=0; j<l1LineItem.l2LineItems.length; j++ ) {
                table.treetable( cmd, "L1-" + i + "-" + j ) ;
            }
        }
    }

    function getTableDOM() {
        
        return TABLE( 
            {
                border : 1,
                id : "budget-spread-table"
            }, 
            buildTableHeader(),
            buildTableBody() 
        ) ;
    }
    
    function buildTableHeader() {
        
        var rows = [] ;
        
        rows.push( buildHdrMonthNameRow() ) ;
        if( showPlanned ) {
            rows.push( buildHdrPlannedRow() ) ;
        }
        if( showAvailable ) {
            rows.push( buildHdrAvailableRow() ) ;
        }
        if( showConsumed ) {
            rows.push( buildHdrConsumedRow() ) ;
        }
        rows.push( buildHdrRemainingRow() ) ;
        
        return THEAD( rows ) ;
    }
    
    function buildHdrMonthNameRow() {
        
        return TR(
           TD(),
           TD.map( spread.budgetCells, function( cell, attributes ) {
                attributes[ "width" ] = 90 ;
                return cell.monthName ; 
           } ),
           TD( 'Total' ) 
        ) ;
    }
    
    function buildHdrPlannedRow() {
        
        return TR(
           TD( "Planned" ),
           TD.map( spread.budgetCells, function( cell ) {
                return fmtAmt( cell.planned ) ; 
           } ),
           TD( fmtAmt( spread.totalPlanned ) ) 
        ) ;
    }
    
    function buildHdrConsumedRow() {
        
        return TR(
           TD( "Consumed" ),
           TD.map( spread.budgetCells, function( cell ) {
                return fmtAmt( cell.consumed ) ; 
           } ),
           TD( fmtAmt( spread.totalConsumed ) ) 
        ) ;
    }
    
    function buildHdrAvailableRow() {
        
        return TR(
           TD( "Available" ),
           TD.map( spread.budgetCells, function( cell ) {
                return fmtAmt( cell.available ) ; 
           } ),
           TD( "" ) 
        ) ;
    }
    
    function buildHdrRemainingRow() {
        
        var totalDev = spread.totalPlanned - spread.totalConsumed ;
        return TR(
           TD( "Savings" ),
           TD.map( spread.budgetCells, function( cell, attributes ) {
            
                attributes[ "class" ] = getFGClass( cell.remaining ) ; 
                return fmtAmt( cell.remaining ) ; 
           } ),
           TD( { "class" : getFGClass( totalDev ) }, fmtAmt( totalDev ) )  
        ) ;
    }
    
    function buildTableBody() {
        
        var trList = [] ;
        var l1LineItems = spread.l1LineItems ;
        
        for( var i = 0; i<l1LineItems.length; i++ ) {
            
            var l1Item = l1LineItems[i] ;
            
            trList.push( buildL1RemainingTR( l1Item, i ) ) ;
            if( showPlanned ) {
                trList.push( buildL1PlannedTR( l1Item, i ) ) ;
            }
            if( showAvailable ) {
                trList.push( buildL1AvailableTR( l1Item, i ) ) ;
            }
            if( showConsumed ) {
                trList.push( buildL1ConsumedTR( l1Item, i ) ) ;
            }
            
            for( var j=0; j<l1Item.l2LineItems.length; j++ ) {
                
                var l2Item = l1Item.l2LineItems[j] ;
                
                trList.push( buildL2RemainingTR( l2Item, i, j ) ) ;
                if( showPlanned ) {
                    trList.push( buildL2PlannedTR  ( l2Item, i, j ) ) ;
                }
                if( showAvailable ) {
                    trList.push( buildL2AvailableTR( l2Item, i, j ) ) ;
                }
                if( showConsumed ) {
                    trList.push( buildL2ConsumedTR ( l2Item, i, j ) ) ;
                }
            }
        }
        
        return TBODY( trList ) ;
    }
    
    function buildL1RemainingTR( l1LineItem, index ) {
        
        var totalDev = l1LineItem.totalPlanned - l1LineItem.totalConsumed ;
        
        return TR( {
                "data-tt-id" : "L1-" + index
            }, 
            TD( l1LineItem.lineItemName ),
            TD.map( l1LineItem.budgetCells, function( cell, attributes ){
                
                attributes[ "class" ] = getFGClass( cell.remaining ) ; 
                return fmtAmt( cell.remaining ) ; 
            } ),
            TD( { "class" : getFGClass( totalDev ) }, fmtAmt( totalDev ) )  
        ) ;
    }
    
    function buildL1PlannedTR( l1LineItem, index ) {
        
        return TR( {
                "data-tt-id" : "L1-" + index + "-planned",
                "data-tt-parent-id" : "L1-" + index
            },
            TD( "Planned" ),
            TD.map( l1LineItem.budgetCells, function( cell ){
                return fmtAmt( cell.planned ) ; 
            } ),
            TD( fmtAmt( l1LineItem.totalPlanned ) )  
        ) ;
    }
    
    function buildL1AvailableTR( l1LineItem, index ) {
        
        return TR( {
                "data-tt-id" : "L1-" + index + "-available",
                "data-tt-parent-id" : "L1-" + index
            },
            TD( "Available" ),
            TD.map( l1LineItem.budgetCells, function( cell ){
                return fmtAmt( cell.available ) ; 
            } ),
            TD( "" )  
        ) ;
    }
    
    function buildL1ConsumedTR( l1LineItem, index ) {
        
        return TR( {
                "data-tt-id" : "L1-" + index + "-consumed",
                "data-tt-parent-id" : "L1-" + index
            },
            TD( "Consumed" ),
            TD.map( l1LineItem.budgetCells, function( cell, attributes ){
                attributes[ "class" ] = getFGClass(cell.available - cell.consumed) ;
                return fmtAmt( cell.consumed ) ; 
            } ),
            TD( fmtAmt( l1LineItem.totalConsumed ) )  
        ) ;
    }
    
    function buildL2RemainingTR( l2LineItem, l1Index, l2Index ) {
        
        var totalDev = l2LineItem.totalPlanned - l2LineItem.totalConsumed ;
        
        return TR( {
                "data-tt-id" : "L1-" + l1Index + "-" + l2Index,
                "data-tt-parent-id" : "L1-" + l1Index,
                "l2-branch" : "true"
            },
            TD( l2LineItem.lineItemName ),
            TD.map( l2LineItem.budgetCells, function( cell, attributes ){
                
                attributes[ "class" ] = getFGClass( cell.remaining ) ; 
                return fmtAmt( cell.remaining ) ; 
            } ),
            TD( { "class" : getFGClass( totalDev ) }, fmtAmt( totalDev ) )  
        ) ;
    }
    
    function buildL2PlannedTR( l2LineItem, l1Index, l2Index ) {
        
        return TR( {
                "data-tt-id" : "L1-" + l1Index + "-" + l2Index + "-planned",
                "data-tt-parent-id" : "L1-" + l1Index + "-" + l2Index
            },
            TD( "Planned" ),
            TD.map( l2LineItem.budgetCells, function( cell ){
                return fmtAmt( cell.planned ) ; 
            } ),
            TD( fmtAmt( l2LineItem.totalPlanned ) )  
        ) ;
    }
    
    function buildL2AvailableTR( l2LineItem, l1Index, l2Index ) {
        
        return TR( {
                "data-tt-id" : "L1-" + l1Index + "-" + l2Index + "-available",
                "data-tt-parent-id" : "L1-" + l1Index + "-" + l2Index
            },
            TD( "Available" ),
            TD.map( l2LineItem.budgetCells, function( cell ){
                return fmtAmt( cell.available ) ; 
            } ),
            TD( "" )  
        ) ;
    }
    
    function buildL2ConsumedTR( l2LineItem, l1Index, l2Index ) {
        
        return TR( {
                "data-tt-id" : "L1-" + l1Index + "-" + l2Index + "-consumed",
                "data-tt-parent-id" : "L1-" + l1Index + "-" + l2Index
            },
            TD( "Consumed" ),
            TD.map( l2LineItem.budgetCells, function( cell, attributes ){
                
                attributes[ "class" ]   = getFGClass(cell.available - cell.consumed) ;
                attributes[ "onclick" ] = "triggerLedgerEntryDisplay( " + 
                                                l2LineItem.category.id + "," + 
                                                "'" + cell.startOfMonth +"' ) ;" ;
                return fmtAmt( cell.consumed ) ; 
            } ),
            TD( fmtAmt( l2LineItem.totalConsumed ) )  
        ) ;
    }
    
    // -------------------------------------------------------------------------
    //                       Utility functions
    
    function fmtAmt( amt ) {
        
        var fmt = "0.00" ;
        
        if( amt != null ) {
            fmt = amt.toLocaleString('en-IN', {
                maximumFractionDigits: 2,
                style: 'currency',
                currency: 'INR'
            } ) ;
        }
        
        if( fmt.indexOf( '.' ) != -1 ) {
            fmt = fmt.substring( 0, fmt.indexOf( '.' ) ) ; 
        }
        
        fmt = fmt.replace( "\u20B9", "" ) ;
        fmt = fmt.replace( /\s/g, '' ) ;
        
        return fmt ;
    }
    
    function getFGClass( amt ) {
        return amt >= 0 ? "green-font" : "red-font" ;
    }
}