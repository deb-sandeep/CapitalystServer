function getProperty( obj, propString ) {
  
  if( !propString )
    return obj ;

  var prop, props = propString.split( '.' ) ;

  for( var i=0, iLen=props.length-1; i<iLen; i++) {
    
    prop = props[ i ] ;

    var candidate = obj[prop] ;
    if( candidate !== undefined ) {
      obj = candidate ;
    } 
    else {
      break ;
    }
  }
  
  return obj[ props[i] ] ;
}

function sortArrayByProperty( sortDir, array, property, type ) {
    
    array.sort( function( o1, o2 ){
        
        var p1 = getProperty( o1, property ) ;
        var p2 = getProperty( o2, property ) ;
        
        if( type == 'string' ) {
            if( p1 == null && p2 != null ) {
                return 1 ;
            }
            else if( p1 != null && p2 == null ) {
                return -1 ;
            }
            else if( p1 == null && p2 == null ) {
                return 0 ;
            }
            
            return sortDir == "asc" ?
                p1.localeCompare( p2 ) : p2.localeCompare( p1 ) ;
        }
        else if( type == 'num' ) {
            return sortDir == "asc" ?
                (p1 - p2) : (p2 - p1) ;
        }
        else if( type == 'date' ) {
            
            var d1 = Date.parse( p1 ) ;
            var d2 = Date.parse( p2 ) ;
            
            return sortDir == "asc" ? (d1-d2) : (d2-d1) ;
        }
    } ) ;
}

Date.prototype.toShortFormat = function() {

    const monthNames = ["Jan", "Feb", "Mar", "Apr",
                        "May", "Jun", "Jul", "Aug",
                        "Sep", "Oct", "Nov", "Dec"];
    
    const day = this.getDate();
    
    const monthIndex = this.getMonth();
    const monthName = monthNames[monthIndex];
    
    const year = this.getFullYear();
    
    return `${day}-${monthName}-${year}`;  
}