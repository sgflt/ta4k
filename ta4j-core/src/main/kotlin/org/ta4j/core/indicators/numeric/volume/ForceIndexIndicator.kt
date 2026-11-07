/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.volume

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator

/**
 * Force Index (Alexander Elder): EMA of (price change * volume).
 *
 * @see <a href="https://school.stockcharts.com/doku.php?id=technical_indicators:force_index">StockCharts</a>
 */
class ForceIndexIndicator(
    closePrice: NumericIndicator,
    volume: NumericIndicator,
    private val barCount: Int = 13,
) : NumericIndicator(closePrice.numFactory) {

    init {
        require(barCount > 0) { "Bar count must be positive, but was: $barCount" }
    }

    private val smoothedForce = closePrice.difference().multipliedBy(volume).ema(barCount)

    override fun updateState(bar: Bar) {
        smoothedForce.onBar(bar)
        value = smoothedForce.value
    }

    override val lag = barCount + 1

    override val isStable: Boolean
        get() = smoothedForce.isStable

    override fun toString() = "ForceIndex($barCount) => $value"
}
