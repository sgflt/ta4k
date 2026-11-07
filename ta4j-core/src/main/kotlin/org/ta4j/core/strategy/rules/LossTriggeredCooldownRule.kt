/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.strategy.rules

import java.time.Duration
import org.ta4j.core.TradeType
import org.ta4j.core.backtest.Position
import org.ta4j.core.backtest.strategy.BackTestTradingRecord.ClosedPositionsResolver
import org.ta4j.core.backtest.strategy.BackTestTradingRecord.LastClosedPositionResolver
import org.ta4j.core.backtest.strategy.runtime.CurrentTimeResolver
import org.ta4j.core.indicators.TimeFrame
import org.ta4j.core.strategy.Rule
import org.ta4j.core.strategy.RuntimeContext
import org.ta4j.core.utils.TimeFrameMapping

/**
 * Blocks new entries for [cooldownBars] bars after a losing position.
 *
 * Satisfied when the last closed position (optionally only positions entered as [tradeType]) was not
 * a loss, when [resetRule] is satisfied, or when at least [cooldownBars] bars of [timeFrame] have
 * passed since its exit. Bars are counted from time, like [OpenedPositionMinimumBarCountRule].
 * Works in backtests and live trading alike, as both publish closed positions.
 */
class LossTriggeredCooldownRule(
    private val cooldownBars: Int,
    timeFrame: TimeFrame,
    private val runtimeContext: RuntimeContext,
    private val resetRule: Rule? = null,
    private val tradeType: TradeType? = null,
) : AbstractRule() {

    private val barDuration: Duration = TimeFrameMapping.getDuration(timeFrame)
    private val lastClosedPositionResolver = LastClosedPositionResolver()
    private val closedPositionsResolver = ClosedPositionsResolver()
    private val currentTimeResolver = CurrentTimeResolver()

    init {
        require(cooldownBars >= 0) { "Cooldown bars must not be negative, but was: $cooldownBars" }
    }

    override val isSatisfied: Boolean
        get() {
            val satisfied = evaluate()
            traceIsSatisfied(satisfied)
            return satisfied
        }

    private fun evaluate(): Boolean {
        val lastPosition = lastRelevantClosedPosition() ?: return true
        if (!lastPosition.hasLoss) {
            return true
        }
        if (resetRule?.isSatisfied == true) {
            return true
        }

        val exitTime = lastPosition.exit?.whenExecuted ?: return false
        val currentTime = runtimeContext.getValue(currentTimeResolver) ?: return false
        if (currentTime.isBefore(exitTime)) {
            return false
        }
        val elapsedBars = Duration.between(exitTime, currentTime).toNanos() / barDuration.toNanos()
        return elapsedBars >= cooldownBars
    }

    private fun lastRelevantClosedPosition(): Position? =
        if (tradeType == null) {
            runtimeContext.getValue(lastClosedPositionResolver)
        } else {
            runtimeContext.getValue(closedPositionsResolver)
                ?.lastOrNull { it.isClosed && it.entry?.type == tradeType }
        }

    override fun toString() = "LossTriggeredCooldownRule[cooldownBars=$cooldownBars] => $isSatisfied"
}
