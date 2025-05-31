/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2025 Ta4j Organization & respective
 * authors (see AUTHORS)
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
