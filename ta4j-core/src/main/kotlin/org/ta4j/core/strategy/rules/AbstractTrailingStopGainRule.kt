/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.strategy.rules

import org.ta4j.core.backtest.Trade
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num
import org.ta4j.core.strategy.RuntimeContext

/**
 * Base class for trailing stop-gains that lock in profit after a minimum gain.
 *
 * The rule arms once the best price since entry has moved [distance] in favour of
 * the position, measured from the net entry price. From then on it triggers when
 * price retraces [distance] from that best price.
 */
abstract class AbstractTrailingStopGainRule(
    priceIndicator: NumericIndicator,
    runtimeContext: RuntimeContext,
    distance: StopDistance,
) : AbstractTrailingStopRule(priceIndicator, runtimeContext, distance) {

    final override fun isArmed(entry: Trade, referencePrice: Num): Boolean {
        val activationDistance = distanceFrom(entry.netPrice)
        if (activationDistance.isNaN) {
            return false
        }
        val activationPrice = StopType.GAIN.stopPrice(entry.type, entry.netPrice, activationDistance)
        return StopType.GAIN.isTriggered(entry.type, activationPrice, referencePrice)
    }
}
