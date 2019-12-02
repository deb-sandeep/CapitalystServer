capitalystNgApp.filter( "amt", function() {
    return function( amt ) {
        
        var fmt = amt.toLocaleString('en-IN', {
            maximumFractionDigits: 2,
            style: 'currency',
            currency: 'INR'
        } ) ;
        
        if( fmt.indexOf( '.' ) != -1 ) {
            fmt = fmt.substring( 0, fmt.indexOf( '.' ) ) ; 
        }
        
        fmt = fmt.replace( "\u20B9", "" ) ;
        fmt = fmt.replace( /\s/g, '' ) ;
        
        return fmt ;
    }
}) ;
