/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.strategy.rules

import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num
import org.ta4j.core.strategy.RuntimeContext

/**
 * A trailing stop-gain rule with an absolute distance.
 *
 * Arms once the best price since entry has gained [gainAmount] over the net entry price,
 * then triggers when price retraces [gainAmount] from that best price.
 *
 * See [AbstractTrailingStopRule] for how the best price is tracked.
 *
 * @param indicator the price indicator
 * @param gainAmount the distance in absolute price units
 * @param runtimeContext the runtime context providing the open position
 */
class TrailingFixedAmountStopGainRule(
    indicator: NumericIndicator,
    val gainAmount: Num,
    runtimeContext: RuntimeContext,
) : AbstractTrailingStopGainRule(indicator, runtimeContext, StopDistance.fixedAmount(gainAmount)) {

    constructor(
        indicator: NumericIndicator,
        gainAmount: Double,
        runtimeContext: RuntimeContext,
    ) : this(indicator, indicator.numFactory.numOf(gainAmount), runtimeContext)

    override fun toString(): String {
        return "TrailingFixedAmountStopGainRule[${gainAmount}] => $isSatisfied"
    }
}
