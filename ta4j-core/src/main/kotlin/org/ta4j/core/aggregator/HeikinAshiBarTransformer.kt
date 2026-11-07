/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.aggregator

import org.ta4j.core.api.series.Bar
import org.ta4j.core.num.Num

/**
 * Emits a Heikin-Ashi bar for every source bar:
 * close = (O + H + L + C) / 4, open = (previous HA open + previous HA close) / 2 (first: (O + C) / 2),
 * high = max(H, HA open, HA close), low = min(L, HA open, HA close).
 */
class HeikinAshiBarTransformer : AbstractBarTransformer() {
    private var previous: AggregatedBar? = null

    override fun onBar(bar: Bar) {
        val numFactory = bar.closePrice.numFactory
        val two = numFactory.two()
        val close = (bar.openPrice + bar.highPrice + bar.lowPrice + bar.closePrice) / numFactory.numOf(4)
        val open = previous?.let { (it.openPrice + it.closePrice) / two } ?: ((bar.openPrice + bar.closePrice) / two)
        val heikinAshi = AggregatedBar(
            timeFrame = bar.timeFrame,
            timePeriod = bar.timePeriod,
            beginTime = bar.beginTime,
            endTime = bar.endTime,
            openPrice = open,
            highPrice = maxOf3(bar.highPrice, open, close),
            lowPrice = minOf3(bar.lowPrice, open, close),
            closePrice = close,
            volume = bar.volume,
        )
        previous = heikinAshi
        emit(heikinAshi)
    }

    private fun maxOf3(a: Num, b: Num, c: Num): Num = listOf(a, b, c).reduce { x, y -> if (y > x) y else x }

    private fun minOf3(a: Num, b: Num, c: Num): Num = listOf(a, b, c).reduce { x, y -> if (y < x) y else x }
}
