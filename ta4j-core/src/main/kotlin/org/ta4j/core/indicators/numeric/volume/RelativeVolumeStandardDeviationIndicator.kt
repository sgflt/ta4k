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
 * Relative Volume Standard Deviation Indicator.
 *
 * This indicator calculates how many standard deviations the current volume is
 * from the average volume over a specified period. It's essentially a z-score
 * for volume, which helps identify periods of unusual trading activity.
 *
 * Formula: (Current Volume - Average Volume) / Volume Standard Deviation
 *
 * Relative Volume (often called RVOL) is an indicator that tells traders
 * how current trading volume compares to past trading volume over a given period.
 * The standard deviation normalization helps identify significant volume spikes or lulls.
 *
 * @param numFactory the number factory
 * @param barCount the time frame for calculating average and standard deviation
 *
 * @see <a href="https://www.tradingview.com/script/Eize4T9L-Relative-Volume-Standard-Deviation/">
 *      Relative Volume Standard Deviation</a>
 */
class RelativeVolumeStandardDeviationIndicator(
    numFactory: NumFactory,
    private val barCount: Int,
) : NumericIndicator(numFactory) {

    private val volume = VolumeIndicator(numFactory)
    private val averageVolume = volume.sma(barCount)
    private val volumeStandardDeviation = volume.stddev(barCount)

    init {
        require(barCount > 0) { "Bar count must be positive" }
    }

    private fun calculate(): Num {
        return if (volumeStandardDeviation.value.isZero) {
            // When standard deviation is zero (all volumes are the same),
            // return zero to indicate no deviation from norm
            numFactory.zero()
        } else {
            (volume.value - averageVolume.value) / volumeStandardDeviation.value
        }
    }

    override fun updateState(bar: Bar) {
        volume.onBar(bar)
        averageVolume.onBar(bar)
        volumeStandardDeviation.onBar(bar)
        value = calculate()
    }

    override val lag = barCount

    override val isStable: Boolean
        get() = averageVolume.isStable && volumeStandardDeviation.isStable

    override fun toString(): String {
        return "RelativeVolumeStandardDeviation($barCount) => $value"
    }
}
