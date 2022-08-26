function Gradient( gradientMap ) {
    
    this.gradientMap = gradientMap ;
    this.values = [] ;
    this.colorValues = [] ;
    
    this.addValue = function( val ) {
        this.values.push( val ) ;
    }
    
    this.initialize = function() {
        this.values.sort( function( a, b ){
            return a-b;
        } ) ;
        
        var numGradientSteps = this.gradientMap.length ;
        var numValues = this.values.length ;
        var numValuesPerStep = numValues/numGradientSteps ;
        
        for( var i=0; i<numGradientSteps; i++ ) {
            for( j=0; j<numValuesPerStep; j++ ) {
                this.colorValues.push( this.gradientMap[i] ) ; 
            }
        }
    }
    
    this.getColor = function( val ) {
        return this.colorValues[ this.values.indexOf( val ) ] ;
    }
}