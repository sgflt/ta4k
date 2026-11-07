/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.strategy.rules

import java.time.Instant
import java.time.temporal.ChronoUnit
import org.ta4j.core.TradeType
import org.ta4j.core.strategy.RuntimeContext
import org.ta4j.core.strategy.RuntimeValueResolver

/**
 * A rule that waits for a specified time period after a trade of a specified type.
 *
 * Satisfied after a fixed amount of time has passed since the last trade.
 *
 * This rule uses RuntimeContext to access trading state and current time
 * in a type-safe manner following the Ta4j framework patterns.
 */
class WaitForRule(
    /** The trade type to wait for. */
    private val tradeType: TradeType,
    /** The amount of time to wait. */
    private val waitPeriod: Long,
    /** The time unit for the wait period. */
    private val timeUnit: ChronoUnit,
    /** Runtime context to access trading state. */
    private val runtimeContext: RuntimeContext,
) : AbstractRule() {

    init {
        require(waitPeriod >= 0) { "Wait period must be non-negative, but was: $waitPeriod" }
    }

    override val isSatisfied: Boolean
        get() {
            // Get current time from runtime context
            val currentTime = runtimeContext.getValue(CURRENT_TIME_RESOLVER) ?: return false

            // Get last trade time for the specified trade type from runtime context
            val lastTradeTime = runtimeContext.getValue(createLastTradeTimeResolver(tradeType))
                ?: return false // No trade of this type has occurred yet

            val timeSinceLastTrade = timeUnit.between(lastTradeTime, currentTime)
            val satisfied = timeSinceLastTrade >= waitPeriod

            traceIsSatisfied(satisfied)
            return satisfied
        }

    override fun toString(): String {
        return "WaitForRule[$tradeType, $waitPeriod $timeUnit] => $isSatisfied"
    }

    companion object {
        /** Resolver for current time. */
        private val CURRENT_TIME_RESOLVER = RuntimeValueResolver<Instant?> { context ->
            context.getValue("currentTime") as? Instant
        }

        /** Creates a resolver for last trade time of a specific trade type. */
        private fun createLastTradeTimeResolver(tradeType: TradeType) =
            RuntimeValueResolver<Instant?> { context ->
                context.getValue("lastTrade${tradeType.name}Time") as? Instant
            }
    }
}
