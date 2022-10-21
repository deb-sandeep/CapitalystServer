package com.sandy.capitalyst.server.api.equity.graph.internal;

import org.ta4j.core.Indicator ;
import org.ta4j.core.indicators.numeric.NumericIndicator ;

public abstract class AbstractIndicatorController {

    protected Double[] getValues( NumericIndicator indicator ) {
        int numElements = indicator.getBarSeries().getBarCount() ;
        Double[] values = Indicator.toDouble( indicator, 0, numElements ) ;
        return values ;
    }
}
