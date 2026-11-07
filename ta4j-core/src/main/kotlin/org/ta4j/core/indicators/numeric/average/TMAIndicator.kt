/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.average

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.NumFactory

/**
 * Triangular Moving Average (TMA) Indicator.
 *
 * TMA is a double-smoothing of the Simple Moving Average (SMA).
 *
 * Triangular Moving Average (TMA) is a type of moving average that places
 * greater emphasis on the central portion of the data series. It is calculated
 * as a second smoothing of a Simple Moving Average (SMA), making it smoother
 * than the SMA or Exponential Moving Average (EMA). Due to its double-smoothing
 * nature, the TMA is commonly used to identify long-term trends in financial
 * markets, reducing noise from short-term fluctuations.
 *
 * @property indicator an indicator
 * @property barCount the Simple Moving Average time frame
 */
class TMAIndicator(
    numFactory: NumFactory,
    private val indicator: NumericIndicator,
    private val barCount: Int
) : NumericIndicator(numFactory) {

    private var processedBars = 0
    private val firstSMA = indicator.sma(barCount)
    private val secondSMA = firstSMA.sma(barCount)

    override fun updateState(bar: Bar) {
        indicator.onBar(bar)
        firstSMA.onBar(bar)
        secondSMA.onBar(bar)
        processedBars++
        value = secondSMA.value
    }

    override val lag: Int
        get() = barCount * 2  // Double smoothing means double lag

    override val isStable: Boolean
        get() = processedBars >= (barCount * 2 - 1)

    override fun toString(): String {
        return "TMA($barCount) => $value"
    }
}
