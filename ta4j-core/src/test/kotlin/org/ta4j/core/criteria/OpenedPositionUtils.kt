/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2023 Ta4j Organization & respective
 * authors (see AUTHORS)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
