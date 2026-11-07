/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.strategy.rules

import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num
import org.ta4j.core.strategy.RuntimeContext

/**
 * A stop-loss rule with an absolute distance from the net entry price.
 *
 * For BUY positions: triggers when price <= entry price - loss amount
 * For SELL positions: triggers when price >= entry price + loss amount
 *
 * @param indicator the price indicator
 * @param lossAmount the distance in absolute price units
 * @param runtimeContext the runtime context providing the open position
 */
class FixedAmountStopLossRule(
    indicator: NumericIndicator,
    val lossAmount: Num,
    runtimeContext: RuntimeContext,
) : AbstractStopRule(indicator, runtimeContext, StopType.LOSS, StopDistance.fixedAmount(lossAmount)) {

    constructor(
        indicator: NumericIndicator,
        lossAmount: Double,
        runtimeContext: RuntimeContext,
    ) : this(indicator, indicator.numFactory.numOf(lossAmount), runtimeContext)

    override fun toString(): String {
        return "FixedAmountStopLossRule[${lossAmount}] => $isSatisfied"
    }
}
