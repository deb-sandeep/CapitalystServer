capitalystNgApp.service( 'editIntent', function() {
    
    this.editEntryIndex = null ;
    this.editEntry = null ;
    
    this.setEditIntent = function( entry, index ) {
        console.log( "Edit intent generated for ledger entry. " + entry.l1Cat ) ;
        this.editEntryIndex = index ;
        this.editEntry = entry ;
    }
    
}) ;
