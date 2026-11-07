/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.strategy.rules

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
) : AbstractTrailingStopGainRule(indicator, runtimeContext, StopDistance.percentage(gainPercentage)) {

    constructor(
        indicator: NumericIndicator,
        gainPercentage: Double,
        runtimeContext: RuntimeContext,
    ) : this(indicator, indicator.numFactory.numOf(gainPercentage), runtimeContext)

    override fun toString(): String {
        return "TrailingStopGainRule[${gainPercentage.doubleValue()}%] => $isSatisfied"
    }
}
