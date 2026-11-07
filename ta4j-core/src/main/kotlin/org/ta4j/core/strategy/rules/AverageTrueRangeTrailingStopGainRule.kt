/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.strategy.rules

import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.indicators.numeric.momentum.ATRIndicator
import org.ta4j.core.num.Num
import org.ta4j.core.strategy.RuntimeContext

/**
 * A trailing stop-gain rule with a distance of ATR * coefficient. See [VolatilityTrailingStopGainRule].
 *
 * @param indicator the price indicator
 * @param atr the average true range, registered in the indicator context
 * @param coefficient multiple of [atr] used as distance
 * @param runtimeContext the runtime context providing the open position
 */
class AverageTrueRangeTrailingStopGainRule(
    indicator: NumericIndicator,
    atr: ATRIndicator,
    coefficient: Num,
    runtimeContext: RuntimeContext,
) : VolatilityTrailingStopGainRule(indicator, atr, coefficient, runtimeContext) {

    constructor(
        indicator: NumericIndicator,
        atr: ATRIndicator,
        coefficient: Number,
        runtimeContext: RuntimeContext,
    ) : this(indicator, atr, indicator.numFactory.numOf(coefficient), runtimeContext)
}
