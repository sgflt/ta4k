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
package org.ta4j.core.indicators.numeric.oscilators

import java.util.*
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.indicators.numeric.helpers.GainIndicator
import org.ta4j.core.indicators.numeric.helpers.LossIndicator
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * Chande Momentum Oscillator (CMO) indicator.
 *
 * The CMO is a momentum oscillator that measures the momentum of a security by comparing
 * the sum of gains to the sum of losses over a specified period.
 *
 * Formula: CMO = ((Sum of Gains - Sum of Losses) / (Sum of Gains + Sum of Losses)) * 100
 *
 * @param indicator a price indicator
 * @param barCount the time frame (number of periods)
 *
 * @see <a href="http://tradingsim.com/blog/chande-momentum-oscillator-cmo-technical-indicator/">
 *      Chande Momentum Oscillator Technical Indicator</a>
 * @see <a href="http://www.investopedia.com/terms/c/chandemomentumoscillator.asp">
 *      Investopedia - Chande Momentum Oscillator</a>
 */
class CMOIndicator(
    numFactory: NumFactory,
    private val indicator: NumericIndicator,
    private val barCount: Int,
) : NumericIndicator(numFactory) {

    private val gainIndicator = GainIndicator(indicator)
    private val lossIndicator = LossIndicator(indicator)

    private val gainsWindow = ArrayDeque<Num>(barCount)
    private val lossesWindow = ArrayDeque<Num>(barCount)

    private var sumOfGains = numFactory.zero()
    private var sumOfLosses = numFactory.zero()

    init {
        require(barCount > 0) { "Bar count must be positive" }
    }

    private fun calculate(): Num {
        val currentGain = gainIndicator.value
        val currentLoss = lossIndicator.value

        // Remove old values if window is full
        if (gainsWindow.size == barCount) {
            sumOfGains -= gainsWindow.removeFirst()
            sumOfLosses -= lossesWindow.removeFirst()
        }

        // Add new values
        gainsWindow.addLast(currentGain)
        lossesWindow.addLast(currentLoss)
        sumOfGains += currentGain
        sumOfLosses += currentLoss

        // Calculate CMO
        val totalSum = sumOfGains + sumOfLosses
        return if (totalSum.isZero) {
            numFactory.zero()
        } else {
            (sumOfGains - sumOfLosses) / totalSum * numFactory.hundred()
        }
    }

    override fun updateState(bar: Bar) {
        gainIndicator.onBar(bar)
        lossIndicator.onBar(bar)

        value = calculate()
    }

    override val lag = barCount

    override val isStable: Boolean
        get() = gainsWindow.size >= barCount

    override fun toString(): String {
        return "CMO($barCount) => $value"
    }
}
