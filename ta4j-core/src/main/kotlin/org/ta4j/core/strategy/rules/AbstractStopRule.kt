/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.strategy.rules

import org.ta4j.core.backtest.Trade
import org.ta4j.core.backtest.strategy.BackTestTradingRecord.CurrentPositionResolver
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num
import org.ta4j.core.strategy.RuntimeContext

/**
 * Base class for stop rules (stop-loss, take-profit, trailing stops, ...).
 *
 * Reads the open position from [runtimeContext], measures [distance] from
 * [referencePrice] and fires when [priceIndicator] reaches the stop price.
 * The rule is never satisfied when there is no open position or when a price is NaN.
 *
 * Subclasses customise behaviour by choosing a [StopType], a [StopDistance]
 * and, for trailing stops, by overriding [referencePrice] and [isArmed].
 */
abstract class AbstractStopRule(
    protected val priceIndicator: NumericIndicator,
    protected val runtimeContext: RuntimeContext,
    private val stopType: StopType,
    private val distance: StopDistance,
) : AbstractRule() {

    private val currentPositionResolver = CurrentPositionResolver()

    /**
     * Stop price for the currently open position, or null when no position is open
     * or the price is not available.
     */
    val stopPrice: Num?
        get() = openEntry()?.let { stopPrice(it) }

    final override val isSatisfied: Boolean
        get() {
            val satisfied = isTriggered
            traceIsSatisfied(satisfied)
            return satisfied
        }

    /**
     * Price the stop distance is measured from. Fixed stops use the net entry price,
     * trailing stops override this with the best price since entry.
     *
     * @param entry entry trade of the open position
     */
    protected open fun referencePrice(entry: Trade): Num = entry.netPrice

    /**
     * Whether the stop is active. Inactive stops have no [stopPrice] and never fire.
     *
     * @param entry entry trade of the open position
     * @param referencePrice current [referencePrice]
     */
    protected open fun isArmed(entry: Trade, referencePrice: Num): Boolean = true

    /**
     * @return distance of the stop from [referencePrice], as configured for this rule
     */
    protected fun distanceFrom(referencePrice: Num): Num = distance.distanceFrom(referencePrice)

    private val isTriggered: Boolean
        get() {
            val entry = openEntry() ?: return false
            val stopPrice = stopPrice(entry) ?: return false
            val currentPrice = priceIndicator.value
            if (currentPrice.isNaN) {
                return false
            }
            return stopType.isTriggered(entry.type, stopPrice, currentPrice)
        }

    private fun openEntry(): Trade? =
        runtimeContext.getValue(currentPositionResolver)
            ?.takeIf { it.isOpened }
            ?.entry

    private fun stopPrice(entry: Trade): Num? {
        val referencePrice = referencePrice(entry)
        if (referencePrice.isNaN || !isArmed(entry, referencePrice)) {
            return null
        }
        val distance = distanceFrom(referencePrice)
        if (distance.isNaN) {
            return null
        }
        return stopType.stopPrice(entry.type, referencePrice, distance)
    }
}
