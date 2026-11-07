/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.strategy.rules

import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num
import org.ta4j.core.strategy.RuntimeContext

/**
 * A stop-loss rule.
 *
 * Satisfied when the indicator reaches the loss threshold relative to the entry price.
 * For BUY positions: triggers when current price <= entry price * (1 - loss%)
 * For SELL positions: triggers when current price >= entry price * (1 + loss%)
 *
 * @param indicator the price indicator
 * @param lossPercentage the loss percentage (e.g., 5 for 5% loss)
 * @param runtimeContext the runtime context providing the open position
 */
class StopLossRule(
    indicator: NumericIndicator,
    val lossPercentage: Num,
    runtimeContext: RuntimeContext,
) : AbstractStopRule(indicator, runtimeContext, StopType.LOSS, StopDistance.percentage(lossPercentage)) {

    constructor(
        indicator: NumericIndicator,
        lossPercentage: Double,
        runtimeContext: RuntimeContext,
    ) : this(indicator, indicator.numFactory.numOf(lossPercentage), runtimeContext)

    override fun toString(): String {
        return "StopLossRule[${lossPercentage.doubleValue()}%] => $isSatisfied"
    }
}
