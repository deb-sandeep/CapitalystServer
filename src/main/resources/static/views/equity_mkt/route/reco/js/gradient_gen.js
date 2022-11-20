const G_GRADIENT = 
    [ 'green1', 'green2', 'green3', 'green4', 'green5', 
      'green6', 'green7', 'green8', 'green9', 'green10' ] ;
              
const RG_GRADIENT = 
    [ 'red10' , 'red8',   'red6',   'red4',   'red2', 
      'green2', 'green4', 'green6', 'green8', 'green10' ] ;
    
const GR_GRADIENT = 
    [ 'green10', 'green8', 'green6', 'green4', 'green2', 
      'red2',    'red4',   'red6',   'red8',   'red10' ] ;
    
function UniformGradient( gradientMap ) {
    
    this.generateGradient = function( values, colorValues ) {
        
        if( gradientMap == null ) {
            gradientMap = G_GRADIENT ;
        }
        
        var numGradientSteps = gradientMap.length ;
        var numValues = values.length ;
        var numValuesPerStep = numValues/numGradientSteps ;
        
        for( var i=0; i<numGradientSteps; i++ ) {
            for( j=0; j<numValuesPerStep; j++ ) {
                colorValues.push( gradientMap[i] ) ; 
            }
        }
    }
}

function ThresholdGradient( threshold ) {
    
    this.generateGradient = function( values, colorValues ) {
        
        if( threshold == null ){ threshold = 0 ; }
        
        var numValues = values.length ;
        var min       = values[0] ;
        var max       = values[numValues-1] ;
        var posBase   = max - threshold ;
        var negBase   = threshold - min ;
        
        for( var i=0; i<numValues; i++ ) {
            
            var prefix = null ;
            var base   = 0 ;
            var x      = 0 ;
            var value  = values[i] ;
            
            if( value < threshold ) {
                x      = threshold - values[i] ;
                base   = negBase ;
                prefix = "red" ;
            }
            else {
                x      = values[i] - threshold ;
                base   = posBase ;
                prefix = "green" ;
            }
            
            var y = Math.round( x * 10 / base ) ;
            colorValues[i] = prefix + y ;
        }
    }
}

function Gradient( gradientMapper ) {
    
    this.values = [] ;
    this.colorValues = [] ;
    
    this.addValue = function( val ) {
        this.values.push( val ) ;
    }
    
    this.clear = function() {
        this.values.length = 0 ;
    }
    
    this.initialize = function() {
        this.values.sort( function( a, b ){
            return a-b ;
        } ) ;
        
        if( gradientMapper == null ) {
            gradientMapper = new UniformGradient( G_GRADIENT ) ;
        }
        
        gradientMapper.generateGradient( this.values, 
                                         this.colorValues ) ;
    }
    
    this.getColor = function( val ) {
        return this.colorValues[ this.values.indexOf( val ) ] ;
    }
}

