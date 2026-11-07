/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.strategy.rules

import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num
import org.ta4j.core.strategy.RuntimeContext

/**
 * A take-profit rule with an absolute distance from the net entry price.
 *
 * For BUY positions: triggers when price >= entry price + gain amount
 * For SELL positions: triggers when price <= entry price - gain amount
 *
 * @param indicator the price indicator
 * @param gainAmount the distance in absolute price units
 * @param runtimeContext the runtime context providing the open position
 */
class FixedAmountStopGainRule(
    indicator: NumericIndicator,
    val gainAmount: Num,
    runtimeContext: RuntimeContext,
) : AbstractStopRule(indicator, runtimeContext, StopType.GAIN, StopDistance.fixedAmount(gainAmount)) {

    constructor(
        indicator: NumericIndicator,
        gainAmount: Double,
        runtimeContext: RuntimeContext,
    ) : this(indicator, indicator.numFactory.numOf(gainAmount), runtimeContext)

    override fun toString(): String {
        return "FixedAmountStopGainRule[${gainAmount}] => $isSatisfied"
    }
}
