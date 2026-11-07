/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.average

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num
import org.ta4j.core.utils.CircularNumArray

/**
 * Linearly Weighted Moving Average (LWMA) indicator.
 *
 * @see [
 * https://www.investopedia.com/terms/l/linearlyweightedmovingaverage.asp](https://www.investopedia.com/terms/l/linearlyweightedmovingaverage.asp)
 */
class LWMAIndicator(private val indicator: NumericIndicator, private val barCount: Int) :
    NumericIndicator(indicator.numFactory) {
    private val values = CircularNumArray(barCount)
    private val weights = ArrayList<Num>(barCount)
    private val denominator: Num


    /**
     * Constructor.
     *
     * @param indicator the [Indicator]
     * @param barCount the time frame
     */
    init {
        for (i in 1..<barCount + 1) {
            weights.add(numFactory.numOf(i))
        }
        denominator = numFactory.numOf(weights.sumOf { it.intValue() })
    }


    private fun calculate(): Num {
        var sum = numFactory.zero()

        values.addLast(indicator.value)

        if (values.isNotFull) {
            return numFactory.zero()
        }

        var count = 0
        for (value in values) {
            val weight = weights[count++]
            sum += value * weight
        }

        return sum / denominator
    }


    public override fun updateState(bar: Bar) {
        indicator.onBar(bar)
        value = calculate()
    }

    override val lag: Int
        get() = barCount


    override val isStable: Boolean
        get() = !values.isNotFull && indicator.isStable


    override fun toString() = "LWMA($barCount) => $value"
}
