/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.criteria

import java.time.Instant
import org.ta4j.core.TestUtils.assertNumEquals
import org.ta4j.core.TradeType
import org.ta4j.core.backtest.BacktestBarSeriesBuilder
import org.ta4j.core.backtest.Position
import org.ta4j.core.backtest.criteria.AnalysisCriterion
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

class OpenedPositionUtils {

    fun testCalculateOneOpenPositionShouldReturnExpectedValue(
        numFactory: NumFactory,
        criterion: AnalysisCriterion,
        expectedValue: Num,
    ) {
        val series = BacktestBarSeriesBuilder()
            .withNumFactory(numFactory)
            .build()

        // Add test bars with the specified data
        val testPrices = doubleArrayOf(100.0, 105.0, 110.0, 100.0, 95.0, 105.0)
        for (price in testPrices) {
            series.barBuilder()
                .closePrice(price)
                .openPrice(price)
                .highPrice(price)
                .lowPrice(price)
                .volume(1000.0)
                .add()
        }

        val trade = Position(TradeType.BUY, numFactory = numFactory)
        trade.operate(Instant.now(), numFactory.numOf(2.5), numFactory.one())

        val value = criterion.calculate(trade)

        assertNumEquals(expectedValue, value)
    }

    fun testCalculateOneOpenPositionShouldReturnExpectedValue(
        numFactory: NumFactory,
        criterion: AnalysisCriterion,
        expectedValue: Int,
    ) {
        testCalculateOneOpenPositionShouldReturnExpectedValue(
            numFactory,
            criterion,
            numFactory.numOf(expectedValue)
        )
    }
}
