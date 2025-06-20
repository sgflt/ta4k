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
package org.ta4j.core.indicators.numeric.volume

import org.ta4j.core.api.Indicators
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.NumFactory


/**
 * Time Segmented Volume (TSV) indicator.
 *
 * This class calculates the Time Segmented Volume (TSV). TSV is a volume-based
 * indicator that gauges the supply and demand for a security based on the price
 * changes and volume of each period (or "segment") in the given time frame.
 *
 * The calculation involves multiplying the volume of each period by the
 * difference between consecutive close prices, and summing up these
 * values over the given period.
 *
 * Formula: TSV = Sum over barCount of: (Close[i] - Close[i-1]) * Volume[i]
 *
 * @param numFactory the number factory for calculations
 * @param barCount the time frame for the calculation
 *
 * @see <a href="https://www.investopedia.com/terms/t/tsv.asp">Time Segmented
 *      Volume (TSV)</a>
 */
class TimeSegmentedVolumeIndicator(
    numFactory: NumFactory,
    private val barCount: Int,
) : NumericIndicator(numFactory) {

    private val closePrice = Indicators.extended(numFactory).closePrice()
    private val volume = Indicators.extended(numFactory).volume()

    // TSV Component = (Close[i] - Close[i-1]) * Volume[i]
    private val tsvComponent = closePrice.difference().multipliedBy(volume)

    // Sum of TSV components over the specified period
    private val runningTotal = tsvComponent.runningTotal(barCount)

    init {
        require(barCount > 0) { "Bar count must be positive" }
    }

    override fun updateState(bar: Bar) {
        runningTotal.onBar(bar)
        value = runningTotal.value
    }

    override val lag = barCount

    override val isStable: Boolean
        get() = runningTotal.isStable

    override fun toString(): String {
        return "TSV($barCount) => $value"
    }
}
