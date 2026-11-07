/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.volume

import org.ta4j.core.api.Indicators
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * Chaikin Money Flow (CMF) indicator.
 *
 * The Chaikin Money Flow indicator measures the amount of Money Flow Volume over a specific period.
 *
 * Formula: CMF = Sum of Money Flow Volume over n periods / Sum of Volume over n periods
 * Where Money Flow Volume = Close Location Value * Volume
 *
 * @param numFactory the number factory
 * @param barCount the time frame (number of periods)
 *
 * @see <a href="http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:chaikin_money_flow_cmf">
 *      Chaikin Money Flow</a>
 * @see <a href="http://www.fmlabs.com/reference/default.htm?url=ChaikinMoneyFlow.htm">
 *      FMLabs - Chaikin Money Flow</a>
 */
class ChaikinMoneyFlowIndicator(
    numFactory: NumFactory,
    private val barCount: Int,
) : NumericIndicator(numFactory) {

    private val closeLocationValue = Indicators.extended(numFactory).closeLocationValue()
    private val volume = Indicators.extended(numFactory).volume()

    // Money Flow Volume = Close Location Value * Volume
    private val moneyFlowVolume = closeLocationValue.multipliedBy(volume)

    // Running totals for the sliding window
    private val sumOfMoneyFlowVolume = moneyFlowVolume.runningTotal(barCount)
    private val sumOfVolume = volume.runningTotal(barCount)

    init {
        require(barCount > 0) { "Bar count must be positive" }
    }

    private fun calculate(): Num {
        return if (sumOfVolume.value.isZero) {
            numFactory.zero()
        } else {
            sumOfMoneyFlowVolume.value / sumOfVolume.value
        }
    }

    override fun updateState(bar: Bar) {
        closeLocationValue.onBar(bar)
        volume.onBar(bar)
        sumOfMoneyFlowVolume.onBar(bar)
        sumOfVolume.onBar(bar)

        value = calculate()
    }

    override val lag = barCount

    override val isStable: Boolean
        get() = sumOfMoneyFlowVolume.isStable && sumOfVolume.isStable

    override fun toString(): String {
        return "CMF($barCount) => $value"
    }
}
