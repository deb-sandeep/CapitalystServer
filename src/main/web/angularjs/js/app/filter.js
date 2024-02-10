capitalystNgApp.filter( "amt", function() {
    return function( amt ) {

        let fmt = "0.00";

        if( amt != null ) {
            fmt = amt.toLocaleString('en-IN', {
                maximumFractionDigits: 2,
                style: 'currency',
                currency: 'INR'
            } ) ;
        }
        
        if( fmt.indexOf( '.' ) !== -1 ) {
            fmt = fmt.substring( 0, fmt.indexOf( '.' ) ) ; 
        }
        
        fmt = fmt.replace( "\u20B9", "" ) ;
        fmt = fmt.replace( /\s/g, '' ) ;
        
        return fmt ;
    }
}) ;

capitalystNgApp.filter('titlecase', function() {
    
    return function( input ) {
      if( !input || typeof input !== 'string' ) {
        return '' ;
      }

      return input.toLowerCase().split(' ').map( value => {
        return value.charAt(0).toUpperCase() + value.substring(1) ;
      }).join(' ') ;
    }
}) ;
