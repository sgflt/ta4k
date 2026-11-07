/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.strategy.rules

import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num
import org.ta4j.core.strategy.RuntimeContext

/**
 * A trailing stop-loss rule.
 *
 * For BUY positions: triggers when price <= highest price since entry * (1 - loss%)
 * For SELL positions: triggers when price >= lowest price since entry * (1 + loss%)
 *
 * See [AbstractTrailingStopRule] for how the best price is tracked.
 *
 * @param indicator the price indicator
 * @param lossPercentage the trailing distance in percent (e.g., 5 for 5%)
 * @param runtimeContext the runtime context providing the open position
 */
class TrailingStopLossRule(
    indicator: NumericIndicator,
    val lossPercentage: Num,
    runtimeContext: RuntimeContext,
) : AbstractTrailingStopRule(indicator, runtimeContext, StopDistance.percentage(lossPercentage)) {

    constructor(
        indicator: NumericIndicator,
        lossPercentage: Double,
        runtimeContext: RuntimeContext,
    ) : this(indicator, indicator.numFactory.numOf(lossPercentage), runtimeContext)

    override fun toString(): String {
        return "TrailingStopLossRule[${lossPercentage.doubleValue()}%] => $isSatisfied"
    }
}
