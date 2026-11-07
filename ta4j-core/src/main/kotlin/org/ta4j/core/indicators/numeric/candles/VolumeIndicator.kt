/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.candles

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.SeriesRelatedNumericIndicator
import org.ta4j.core.num.NumFactory

/**
 * Volume indicator.
 *
 * Returns the open price of a bar.
 */
class VolumeIndicator(numFactory: NumFactory) : SeriesRelatedNumericIndicator(numFactory) {
    override fun updateState(bar: Bar) {
        value = bar.volume
    }

    override val lag: Int = 0
}
