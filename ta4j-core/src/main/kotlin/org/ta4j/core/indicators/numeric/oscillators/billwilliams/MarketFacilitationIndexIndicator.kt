/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.oscillators.billwilliams

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.NaN

/**
 * Bill Williams Market Facilitation Index: `(high - low) / volume`, the price movement per unit of volume.
 * Zero volume gives NaN. Not to be confused with the Money Flow Index.
 *
 * @see <a href="https://www.investopedia.com/terms/m/marketfacilitationindex.asp">Investopedia</a>
 */
class MarketFacilitationIndexIndicator(
    private val highPrice: NumericIndicator,
    private val lowPrice: NumericIndicator,
    private val volume: NumericIndicator,
) : NumericIndicator(highPrice.numFactory) {

    override fun updateState(bar: Bar) {
        highPrice.onBar(bar)
        lowPrice.onBar(bar)
        volume.onBar(bar)
        val barVolume = volume.value
        value = if (barVolume.isNaN || barVolume.isZero) NaN else (highPrice.value - lowPrice.value) / barVolume
    }

    override val lag = 1

    override val isStable: Boolean
        get() = highPrice.isStable && lowPrice.isStable && volume.isStable

    override fun toString() = "BW-MFI => $value"
}
