/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2025 Ta4j Organization & respective authors (see AUTHORS)
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
