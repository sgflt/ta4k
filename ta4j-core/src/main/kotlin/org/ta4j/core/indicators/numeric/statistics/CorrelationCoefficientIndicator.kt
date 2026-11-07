/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.statistics

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num

/**
 * Correlation coefficient indicator.
 *
 * @see [
 * https://chartschool.stockcharts.com/table-of-contents/technical-indicators-and-overlays/technical-indicators/correlation-coefficient](https://chartschool.stockcharts.com/table-of-contents/technical-indicators-and-overlays/technical-indicators/correlation-coefficient)
 */
class CorrelationCoefficientIndicator(
    indicator1: NumericIndicator,
    indicator2: NumericIndicator,
    barCount: Int,
) : NumericIndicator(indicator1.numFactory) {
    private val variance1 = indicator1.variance(barCount)
    private val variance2 = indicator2.variance(barCount)
    private val covariance = indicator1.covariance(indicator2, barCount)


    private fun calculate(): Num {
        val multipliedSqrt = (variance1.value * variance2.value).sqrt()
        return covariance.value / multipliedSqrt
    }


    public override fun updateState(bar: Bar) {
        variance1.onBar(bar)
        variance2.onBar(bar)
        covariance.onBar(bar)
        value = calculate()
    }


    override val isStable
        get() = variance1.isStable && variance2.isStable && covariance.isStable

    override val lag: Int
        get() = variance1.lag
}
