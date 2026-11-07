/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest

import org.ta4j.core.events.CandleReceived
import org.ta4j.core.events.MarketEvent
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * Execution trigger that executes orders at the current bar's close price.
 * This trigger executes immediately when any market event occurs.
 */
class CurrentBarTrigger : ExecutionTrigger {
    override fun shouldExecute(event: MarketEvent): Boolean {
        // Execute immediately on any market event
        return true
    }

    override fun getExecutionPrice(event: MarketEvent, numFactory: NumFactory): Num? {
        return when (event) {
            is CandleReceived -> numFactory.numOf(event.closePrice)
            else -> null
        }
    }
}
