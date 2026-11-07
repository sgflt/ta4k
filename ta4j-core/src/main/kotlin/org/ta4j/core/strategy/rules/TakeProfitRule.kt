/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.strategy.rules

import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num
import org.ta4j.core.strategy.RuntimeContext

/**
 * A take-profit rule (also known as stop-gain rule).
 *
 * Satisfied when the indicator reaches the gain threshold relative to the entry price.
 * For BUY positions: triggers when current price >= entry price * (1 + gain%)
 * For SELL positions: triggers when current price <= entry price * (1 - gain%)
 *
 * @param indicator the price indicator
 * @param gainPercentage the gain percentage (e.g., 10 for 10% gain)
 * @param runtimeContext the runtime context providing the open position
 */
class TakeProfitRule(
    indicator: NumericIndicator,
    val gainPercentage: Num,
    runtimeContext: RuntimeContext,
) : AbstractStopRule(indicator, runtimeContext, StopType.GAIN, StopDistance.percentage(gainPercentage)) {

    constructor(
        indicator: NumericIndicator,
        gainPercentage: Double,
        runtimeContext: RuntimeContext,
    ) : this(indicator, indicator.numFactory.numOf(gainPercentage), runtimeContext)

    override fun toString(): String {
        return "TakeProfitRule[${gainPercentage.doubleValue()}%] => $isSatisfied"
    }
}
