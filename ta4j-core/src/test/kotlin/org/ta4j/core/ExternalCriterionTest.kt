/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core

import org.ta4j.core.backtest.TradingRecord
import org.ta4j.core.events.MarketEvent
import org.ta4j.core.num.Num

interface ExternalCriterionTest {

    val marketEvents: List<MarketEvent>

    fun getFinalCriterionValue(vararg params: Any): Num

    /** Reads a new trading record from the external source. */
    fun readTradingRecord(): TradingRecord
}