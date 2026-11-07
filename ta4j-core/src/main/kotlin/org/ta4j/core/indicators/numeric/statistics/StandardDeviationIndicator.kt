/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.statistics

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator

/**
 * Standard deviation indicator.
 *
 * @see [
 * http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:standard_deviation_volatility](http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:standard_deviation_volatility)
 */
class StandardDeviationIndicator(indicator: NumericIndicator, barCount: Int) : NumericIndicator(indicator.numFactory) {
    private val variance = VarianceIndicator(indicator, barCount)


    private fun calculate() = variance.value.sqrt()


    public override fun updateState(bar: Bar) {
        variance.onBar(bar)
        value = calculate()
    }


    override val isStable
        get() = variance.isStable


    override val lag: Int
        get() = variance.lag


    override fun toString() = "STDDEV($variance) => $value"
}
