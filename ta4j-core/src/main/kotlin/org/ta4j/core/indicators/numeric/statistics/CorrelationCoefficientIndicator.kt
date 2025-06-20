/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2025 Ta4j Organization & respective authors (see AUTHORS)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
