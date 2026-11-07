/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.average

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num

/**
 * Hull moving average (HMA) indicator.
 *
 * @see [
 * http://alanhull.com/hull-moving-average](http://alanhull.com/hull-moving-average)
 */
class HMAIndicator(indicator: NumericIndicator, private val barCount: Int) : NumericIndicator(indicator.numFactory) {
    private val sqrtWma: WMAIndicator


    /**
     * Constructor.
     *
     * @param indicator the [Indicator]
     * @param barCount the time frame
     */
    init {
        val halfWma = indicator.wma(barCount / 2)
        val origWma = indicator.wma(barCount)

        val indicatorForSqrtWma = halfWma.multipliedBy(2).minus(origWma)
        sqrtWma = indicatorForSqrtWma.wma(numFactory.numOf(barCount).sqrt().intValue())
    }

    private fun calculate(): Num {
        return sqrtWma.value
    }

    public override fun updateState(bar: Bar) {
        sqrtWma.onBar(bar)
        value = calculate()
    }

    override val lag: Int
        get() = barCount

    override val isStable: Boolean
        get() = sqrtWma.isStable

    override fun toString() = "HMA($barCount) => $value"
}
