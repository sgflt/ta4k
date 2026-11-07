/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.strategy.rules

import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num
import org.ta4j.core.strategy.RuntimeContext

/**
 * A trailing stop-gain rule using volatility * coefficient as activation gain and retracement.
 *
 * See [AbstractTrailingStopGainRule] for the activation logic.
 *
 * The distance follows the current value of [volatility], which has to be registered
 * in the indicator context.
 *
 * @param indicator the price indicator
 * @param volatility the volatility in price units (e.g. ATR or standard deviation)
 * @param coefficient multiple of [volatility] used as distance
 * @param runtimeContext the runtime context providing the open position
 */
open class VolatilityTrailingStopGainRule(
    indicator: NumericIndicator,
    volatility: NumericIndicator,
    val coefficient: Num,
    runtimeContext: RuntimeContext,
) : AbstractTrailingStopGainRule(indicator, runtimeContext, StopDistance.volatility(volatility, coefficient)) {

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
