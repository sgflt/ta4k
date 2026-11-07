/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.strategy.rules

import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num
import org.ta4j.core.strategy.RuntimeContext

/**
 * A trailing stop-loss rule with an absolute distance from the best price since entry.
 *
 * For BUY positions: triggers when price <= highest price since entry - loss amount
 * For SELL positions: triggers when price >= lowest price since entry + loss amount
 *
 * See [AbstractTrailingStopRule] for how the best price is tracked.
 *
 * @param indicator the price indicator
 * @param lossAmount the distance in absolute price units
 * @param runtimeContext the runtime context providing the open position
 */
class TrailingFixedAmountStopLossRule(
    indicator: NumericIndicator,
    val lossAmount: Num,
    runtimeContext: RuntimeContext,
) : AbstractTrailingStopRule(indicator, runtimeContext, StopDistance.fixedAmount(lossAmount)) {

    constructor(
        indicator: NumericIndicator,
        lossAmount: Double,
        runtimeContext: RuntimeContext,
    ) : this(indicator, indicator.numFactory.numOf(lossAmount), runtimeContext)

    override fun toString(): String {
        return "TrailingFixedAmountStopLossRule[${lossAmount}] => $isSatisfied"
    }
}
