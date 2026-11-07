/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core

import org.ta4j.core.backtest.TradingRecord
import org.ta4j.core.events.MarketEvent
import org.ta4j.core.num.Num

interface ExternalCriterionTest {

    fun getMarketEvents(): List<MarketEvent>

    fun getFinalCriterionValue(vararg params: Any): Num

    fun getTradingRecord(): TradingRecord
}