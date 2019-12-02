capitalystNgApp.filter( "remark", function() {
    return function( remark ) {
        
        if( remark == null || remark.trim() == "" ) {
            return "" ;
        }
        else if( remark.length > 50 ) {
            return remark.substring( 0, 50 ) + " ..." ;
        }
        return remark ;
    }
}) ;
