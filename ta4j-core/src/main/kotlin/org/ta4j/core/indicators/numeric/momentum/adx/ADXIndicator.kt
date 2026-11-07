/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.momentum.adx

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.indicators.numeric.average.MMAIndicator
import org.ta4j.core.num.NumFactory

/**
 * ADX indicator.
 *
 *
 *
 * Part of the Directional Movement System.
 *
 * @see [https://www.investopedia.com/terms/a/adx.asp](https://www.investopedia.com/terms/a/adx.asp)
 */
class ADXIndicator(numFactory: NumFactory, private val diBarCount: Int, private val adxBarCount: Int) :
    NumericIndicator(numFactory) {
    private val averageDXIndicator = MMAIndicator(DXIndicator(numFactory, diBarCount), adxBarCount)


    /**
     * Constructor.
     *
     * @param numFactory the bar numFactory
     * @param barCount the bar count for [DXIndicator] and
     * [.averageDXIndicator]
     */
    constructor(numFactory: NumFactory, barCount: Int) : this(numFactory, barCount, barCount)


    override fun toString(): String {
        return javaClass.simpleName + " diBarCount: " + this.diBarCount + " adxBarCount: " + this.adxBarCount
    }


    private fun calculate() = averageDXIndicator.value


    override fun updateState(bar: Bar) {
        averageDXIndicator.onBar(bar)
        value = calculate()
    }


    override val isStable
        get() = averageDXIndicator.isStable

    override val lag: Int
        get() = averageDXIndicator.lag
}
