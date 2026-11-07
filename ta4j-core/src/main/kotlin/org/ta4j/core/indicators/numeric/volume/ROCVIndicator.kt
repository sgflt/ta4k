/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.volume

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.indicators.numeric.candles.VolumeIndicator
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * Rate of change of volume (ROCV) indicator (also called "Momentum of Volume").
 *
 * The ROCV calculation compares the current volume with the volume "n" periods ago.
 *
 * Formula: ((Current Volume - Previous Volume) / Previous Volume) * 100
 *
 * @param numFactory the number factory
 * @param barCount the time frame (number of periods to look back)
 *
 * @see <a href="https://www.investopedia.com/terms/r/rateofchange.asp">Rate of Change</a>
 */
class ROCVIndicator(
    numFactory: NumFactory,
    private val barCount: Int,
) : NumericIndicator(numFactory) {

    private val volume = VolumeIndicator(numFactory)
    private val nPeriodsAgo = volume.previous(barCount)
    private var processedBars = 0

    init {
        require(barCount > 0) { "Bar count must be positive" }
    }

    override val lag: Int
        get() = barCount

    override val isStable: Boolean
        get() = processedBars > barCount && nPeriodsAgo.isStable

    private fun calculate(): Num {
        val currentValue = volume.value
        val nPeriodsAgoValue = nPeriodsAgo.value

        return if (nPeriodsAgoValue.isZero || nPeriodsAgoValue.isNaN) {
            numFactory.zero()
        } else {
            (currentValue - nPeriodsAgoValue) / nPeriodsAgoValue * numFactory.hundred()
        }
    }

    override fun updateState(bar: Bar) {
        volume.onBar(bar)
        nPeriodsAgo.onBar(bar)
        value = calculate()
        processedBars++
    }

    override fun toString(): String {
        return "ROCV($barCount) => $value"
    }
}
