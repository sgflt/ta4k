/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2024 Ta4j Organization & respective authors (see AUTHORS)
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
