/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.execution

import org.ta4j.core.TradeType
import org.ta4j.core.backtest.CurrentBarTrigger
import org.ta4j.core.backtest.ExecutionMode
import org.ta4j.core.backtest.ExecutionTrigger
import org.ta4j.core.backtest.NextBarOpenTrigger
import org.ta4j.core.events.MarketEvent
import org.ta4j.core.num.NumFactory

/**
 * Decides when and at what price a strategy signal is filled.
 */
fun interface OrderTriggerFactory {
    /**
     * @param side whether the order buys or sells
     * @param event the event on which the strategy signalled
     */
    fun create(side: TradeType, event: MarketEvent, numFactory: NumFactory): ExecutionTrigger

    companion object {
        fun of(executionMode: ExecutionMode): OrderTriggerFactory = when (executionMode) {
            ExecutionMode.CURRENT_CLOSE -> OrderTriggerFactory { _, _, _ -> CurrentBarTrigger() }
            ExecutionMode.NEXT_OPEN -> OrderTriggerFactory { _, event, _ -> NextBarOpenTrigger(event.beginTime) }
        }
    }
}
