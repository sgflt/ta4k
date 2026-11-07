/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.average

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num

/**
 * Triple exponential moving average indicator (also called "TRIX").
 *
 *
 *
 * TEMA needs "3 * period - 2" of data to start producing values in contrast to
 * the period samples needed by a regular EMA.
 *
 * @see [https://en.wikipedia.org/wiki/Triple_exponential_moving_average](https://en.wikipedia.org/wiki/Triple_exponential_moving_average)
 *
 * @see [https://www.investopedia.com/terms/t/triple-exponential-moving-average.asp](https://www.investopedia.com/terms/t/triple-exponential-moving-average.asp)
 */
class TripleEMAIndicator(indicator: NumericIndicator, private val barCount: Int) :
    NumericIndicator(indicator.numFactory) {
    private val ema = EMAIndicator(indicator, barCount)
    private val emaEma = EMAIndicator(ema, barCount)
    private val emaEmaEma = EMAIndicator(emaEma, barCount)


    private fun calculate(): Num {
        val three = numFactory.numOf(3)
        return three * (ema.value - emaEma.value) + emaEmaEma.value
    }


    public override fun updateState(bar: Bar) {
        ema.onBar(bar)
        emaEma.onBar(bar)
        emaEmaEma.onBar(bar)
        value = calculate()
    }

    override val lag: Int
        get() = barCount


    override val isStable
        get() = ema.isStable && emaEma.isStable && emaEmaEma.isStable


    override fun toString() = "TrEMA($barCount) => $value"
}
