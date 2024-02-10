capitalystNgApp.filter( "remark", function() {
    return function( remark ) {
        
        if( remark == null || remark.trim() === "" ) {
            return "" ;
        }
        else if( remark.length > 70 ) {
            return remark.substring( 0, 70 ) + " ..." ;
        }
        return remark ;
    }
}) ;
