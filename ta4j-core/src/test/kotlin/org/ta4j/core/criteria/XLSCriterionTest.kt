/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.criteria

import org.ta4j.core.ExternalCriterionTest
import org.ta4j.core.XlsTestsUtils
import org.ta4j.core.backtest.TradingRecord
import org.ta4j.core.events.MarketEvent
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

class XLSCriterionTest(
    private val clazz: Class<*>,
    private val fileName: String,
    private val criterionColumn: Int,
    private val statesColumn: Int,
    private val numFactory: NumFactory,
) : ExternalCriterionTest {

    private var cachedSeries: List<MarketEvent>? = null

    override fun getMarketEvents(): List<MarketEvent> {
        if (cachedSeries == null) {
            cachedSeries = XlsTestsUtils.getMarketEvents(clazz, fileName)
        }
        return cachedSeries!!
    }

    override fun getFinalCriterionValue(vararg params: Any): Num {
        return XlsTestsUtils.getFinalCriterionValue(
            clazz,
            fileName,
            criterionColumn,
            numFactory,
            *params
        )!!
    }

    override fun getTradingRecord(): TradingRecord {
        return XlsTestsUtils.getTradingRecord(clazz, fileName, statesColumn, numFactory)
    }
}
