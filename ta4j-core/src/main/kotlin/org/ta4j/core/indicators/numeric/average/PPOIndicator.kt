/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.average

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * Percentage price oscillator (PPO) indicator (also called "MACD Percentage
 * Price Oscillator (MACD-PPO)").
 *
 * @see [https://www.investopedia.com/terms/p/ppo.asp](https://www.investopedia.com/terms/p/ppo.asp)
 */
open class PPOIndicator(
    numFactory: NumFactory,
    indicator: NumericIndicator,
    shortBarCount: Int = 12,
    longBarCount: Int = 26,
) : NumericIndicator(
    numFactory = numFactory
) {
    private val shortTermEma = indicator.ema(shortBarCount)
    private val longTermEma = indicator.ema(longBarCount)

    init {
        require(shortBarCount <= longBarCount) { "Long term barCount must be greater than short term barCount" }
    }

    private fun calculate(): Num {
        return (shortTermEma.value - longTermEma.value) / longTermEma.value * numFactory.hundred()
    }

    override fun updateState(bar: Bar) {
        shortTermEma.onBar(bar)
        longTermEma.onBar(bar)
        value = calculate()
    }

    override val lag: Int
        get() = shortTermEma.lag

    override val isStable: Boolean
        get() = longTermEma.isStable
}
