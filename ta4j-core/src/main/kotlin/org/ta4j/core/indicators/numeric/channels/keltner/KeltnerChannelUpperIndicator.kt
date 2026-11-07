/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.channels.keltner

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.indicators.numeric.momentum.ATRIndicator
import org.ta4j.core.num.NumFactory

/**
 * Keltner Channel (upper line) indicator.
 *
 * @see [
 * http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:keltner_channels](http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:keltner_channels)
 */
class KeltnerChannelUpperIndicator(
    numFactory: NumFactory,
    private val keltnerMiddleIndicator: KeltnerChannelMiddleIndicator,
    private val averageTrueRangeIndicator: ATRIndicator,
    ratio: Double,
) : NumericIndicator(numFactory) {
    private val ratio = numFactory.numOf(ratio)


    /**
     * Constructor.
     *
     * @param middle the [.keltnerMiddleIndicator]
     * @param ratio the [.ratio]
     * @param barCountATR the bar count for the [ATRIndicator]
     */
    constructor(
        numFactory: NumFactory,
        middle: KeltnerChannelMiddleIndicator,
        ratio: Double,
        barCountATR: Int,
    ) : this(numFactory, middle, ATRIndicator(numFactory, barCount = barCountATR), ratio)


    private fun calculate() = keltnerMiddleIndicator.value + (ratio * averageTrueRangeIndicator.value)


    public override fun updateState(bar: Bar) {
        keltnerMiddleIndicator.onBar(bar)
        averageTrueRangeIndicator.onBar(bar)
        value = calculate()
    }


    override val isStable: Boolean
        get() = keltnerMiddleIndicator.isStable && averageTrueRangeIndicator.isStable

    override val lag: Int
        get() = maxOf(keltnerMiddleIndicator.lag, averageTrueRangeIndicator.lag)
}
