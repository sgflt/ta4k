/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest

import java.time.Instant
import org.ta4j.core.events.CandleReceived
import org.ta4j.core.events.MarketEvent
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * Execution trigger that executes orders at the next bar's open price.
 * Tracks the creation time to ensure execution only happens on the next bar.
 */
class NextBarOpenTrigger(private var creationBarTime: Instant) : ExecutionTrigger {

    override fun shouldExecute(event: MarketEvent): Boolean {
        return when (event) {
            is CandleReceived -> {
                // Execute only if this is a different bar than when order was created
                return event.beginTime > creationBarTime
            }

            else -> false
        }
    }

    override fun getExecutionPrice(event: MarketEvent, numFactory: NumFactory): Num? {
        return when (event) {
            is CandleReceived -> numFactory.numOf(event.openPrice)
            else -> null
        }
    }
}
