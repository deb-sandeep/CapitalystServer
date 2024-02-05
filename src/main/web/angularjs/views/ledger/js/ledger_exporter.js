function LedgerExporter( entries ) {
    
    var accounts = [] ;
    var headers = [] ;
    var headerWidths = [] ;
    var rows = [] ;

    preProcessEntries() ;
    
    this.export = function() {
        console.log( "Exporting ledger entries." ) ;

        const worksheet = XLSX.utils.json_to_sheet( rows ) ;
        const workbook = XLSX.utils.book_new() ;
        
        worksheet["!cols"] = headerWidths ;

        XLSX.utils.book_append_sheet( workbook, worksheet, "Entries" ) ;
        XLSX.utils.sheet_add_aoa( worksheet, [headers], { origin: "A1" } ) ;
        XLSX.writeFile( workbook, "LedgerEntries.xlsx", { compression: true } ) ;
    }
    
    function preProcessEntries() {

        console.log( "Preprocessing entries." ) ;      
        
        var selectedEntriesPresent = ledgerHasSelectedEntries() ;
          
        for( var i=0; i<entries.length; i++ ) {
            var entry = entries[i] ;
            if( entry.visible ) {
                if( selectedEntriesPresent ) {
                    if( entry.selected ) {
                        addAccount( entry.account ) ;            
                    }
                }
                else {
                    addAccount( entry.account ) ;            
                } 
            }
        }
        
        createHeaders() ;

        var singleAccountLedger = isLedgerForSingleAccount() ;
        for( var i=0; i<entries.length; i++ ) {
            var entry = entries[i] ;
            
            if( !entry.visible ) {
                continue ;
            }
            
            if( selectedEntriesPresent && !entry.selected ) {
                continue ;
            }
            
            var row = {} ;
            if( !singleAccountLedger ) {
                row.account = entry.account.accountNumber ;
                row.owner = entry.account.accountOwner ;
            }
            
            row.valueDate   = new Date( entry.valueDate ).toShortFormat() ;
            row.amount      = entry.amount ;
            row.category    = entry.l1Cat ;
            row.subCategory = entry.l2Cat ;
            row.remarks     = entry.remarks ;
            
            rows.push( row ) ;
        }
    }
    
    function createHeaders() {
        
        if( !isLedgerForSingleAccount() ) {
            headers.push( "Account", "Owner" ) ;
            headerWidths.push( {wch:16}, {wch:10} ) ;
        }
        
        headers.push( "Value Date", "Amount", "Category", "Sub-category", "Notes" ) ;
        headerWidths.push( {wch:12}, {wch:10}, {wch:20}, {wch:30}, {wch:100} ) ;
    }
    
    function addAccount( account ) {
        
        for( var i=0; i<accounts.length; i++ ) {
            if( accounts[i].id == account.id ) {
                return ;
            }
        }
        accounts.push( account ) ;
    }
    
    function isLedgerForSingleAccount() {
        return accounts.length == 1 ;
    }
    
    function ledgerHasSelectedEntries() {
        
        for( var i=0; i<entries.length; i++ ) {
            if( entries[i].selected ) {
                return true ;
            }
        }
        return false ;
    }
}