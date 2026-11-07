/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.strategy.rules

import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num
import org.ta4j.core.strategy.RuntimeContext

/**
 * A stop-loss rule whose distance from the net entry price is volatility * coefficient.
 *
 * For BUY positions: triggers when price <= entry price - volatility * coefficient
 * For SELL positions: triggers when price >= entry price + volatility * coefficient
 *
 * The distance follows the current value of [volatility], which has to be registered
 * in the indicator context.
 *
 * @param indicator the price indicator
 * @param volatility the volatility in price units (e.g. ATR or standard deviation)
 * @param coefficient multiple of [volatility] used as distance
 * @param runtimeContext the runtime context providing the open position
 */
open class VolatilityStopLossRule(
    indicator: NumericIndicator,
    volatility: NumericIndicator,
    val coefficient: Num,
    runtimeContext: RuntimeContext,
) : AbstractStopRule(indicator, runtimeContext, StopType.LOSS, StopDistance.volatility(volatility, coefficient)) {

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
