/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest

import org.ta4j.core.events.MarketEvent
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * Defines when a pending order should be executed based on market events.
 */
interface ExecutionTrigger {
    /**
     * Determines if the pending order should be executed based on the given market event.
     *
     * @param event the market event to check
     * @return true if the order should be executed
     */
    fun shouldExecute(event: MarketEvent): Boolean

    /**
     * Gets the execution price for the order based on the market event.
     *
     * @param event the market event that triggered execution
     * @param numFactory the factory to create Num instances
     * @return the execution price, or null if price cannot be determined
     */
    fun getExecutionPrice(event: MarketEvent, numFactory: NumFactory): Num?
}

