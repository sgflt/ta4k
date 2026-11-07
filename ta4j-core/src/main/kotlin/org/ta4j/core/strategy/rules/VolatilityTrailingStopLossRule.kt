/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.strategy.rules

import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num
import org.ta4j.core.strategy.RuntimeContext

/**
 * A trailing stop-loss rule whose distance from the best price since entry is volatility * coefficient.
 *
 * See [AbstractTrailingStopRule] for how the best price is tracked.
 *
 * The distance follows the current value of [volatility], which has to be registered
 * in the indicator context.
 *
 * @param indicator the price indicator
 * @param volatility the volatility in price units (e.g. ATR or standard deviation)
 * @param coefficient multiple of [volatility] used as distance
 * @param runtimeContext the runtime context providing the open position
 */
open class VolatilityTrailingStopLossRule(
    indicator: NumericIndicator,
    volatility: NumericIndicator,
    val coefficient: Num,
    runtimeContext: RuntimeContext,
) : AbstractTrailingStopRule(indicator, runtimeContext, StopDistance.volatility(volatility, coefficient)) {

    constructor(
        indicator: NumericIndicator,
        volatility: NumericIndicator,
        coefficient: Number,
        runtimeContext: RuntimeContext,
    ) : this(indicator, volatility, indicator.numFactory.numOf(coefficient), runtimeContext)

    override fun toString(): String {
        return "${javaClass.simpleName}[${coefficient}] => $isSatisfied"
    }
}
