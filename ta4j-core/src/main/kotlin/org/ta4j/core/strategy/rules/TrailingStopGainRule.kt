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
 * A trailing stop-gain rule that locks in profit after a minimum gain.
 *
 * The rule arms once the best price since entry has gained [gainPercentage] over
 * the net entry price. From then on it triggers when price retraces [gainPercentage]
 * from that best price:
 * - BUY: armed when highest >= entry * (1 + gain%), triggers when price <= highest * (1 - gain%)
 * - SELL: armed when lowest <= entry * (1 - gain%), triggers when price >= lowest * (1 + gain%)
 *
 * See [AbstractTrailingStopRule] for how the best price is tracked.
 *
 * @param indicator the price indicator
 * @param gainPercentage the activation gain and retracement in percent (e.g., 10 for 10%)
 * @param runtimeContext the runtime context providing the open position
 */
class TrailingStopGainRule(
    indicator: NumericIndicator,
    val gainPercentage: Num,
    runtimeContext: RuntimeContext,
) : AbstractTrailingStopRule(indicator, runtimeContext, StopDistance.percentage(gainPercentage)) {

    constructor(
        indicator: NumericIndicator,
        gainPercentage: Double,
        runtimeContext: RuntimeContext,
    ) : this(indicator, indicator.numFactory.numOf(gainPercentage), runtimeContext)

    override fun isArmed(entry: Trade, referencePrice: Num): Boolean {
        val activationPrice = StopType.GAIN.stopPrice(entry.type, entry.netPrice, distanceFrom(entry.netPrice))
        return StopType.GAIN.isTriggered(entry.type, activationPrice, referencePrice)
    }

    override fun toString(): String {
        return "TrailingStopGainRule[${gainPercentage.doubleValue()}%] => $isSatisfied"
    }
}
