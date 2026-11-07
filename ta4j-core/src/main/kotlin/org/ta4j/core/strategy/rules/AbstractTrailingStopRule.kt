/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.strategy.rules

import org.ta4j.core.backtest.Trade
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num
import org.ta4j.core.strategy.RuntimeContext

/**
 * Base class for trailing stops.
 *
 * The stop is measured from the best price reached since entry (highest for long,
 * lowest for short positions) and only ever moves in favour of the position.
 * Tracking starts at the entry trade price.
 *
 * The best price is updated from [priceIndicator] whenever the rule is evaluated,
 * so no extra indicator has to be registered. Prices from bars on which the rule
 * is not evaluated (e.g. when it sits inside an [AndRule] whose other side is
 * false) are not taken into account.
 */
abstract class AbstractTrailingStopRule(
    priceIndicator: NumericIndicator,
    runtimeContext: RuntimeContext,
    distance: StopDistance,
) : AbstractStopRule(priceIndicator, runtimeContext, StopType.LOSS, distance) {

    private var trackedEntry: Trade? = null
    private lateinit var bestPrice: Num

    final override fun referencePrice(entry: Trade): Num {
        if (entry !== trackedEntry) {
            trackedEntry = entry
            bestPrice = entry.pricePerAsset
        }

        val currentPrice = priceIndicator.value
        if (!currentPrice.isNaN && isBetter(entry, currentPrice, bestPrice)) {
            bestPrice = currentPrice
        }
        return bestPrice
    }

    private fun isBetter(entry: Trade, price: Num, than: Num) =
        if (entry.isBuy) price > than else price < than
}
