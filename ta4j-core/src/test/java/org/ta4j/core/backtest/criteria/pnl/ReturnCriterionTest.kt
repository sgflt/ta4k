/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2024 Ta4j Organization & respective authors (see AUTHORS)
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
package org.ta4j.core.backtest.criteria.pnl

import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.TestUtils.assertNumEquals
import org.ta4j.core.TradeType
import org.ta4j.core.TradingRecordTestContext
import org.ta4j.core.backtest.Position
import org.ta4j.core.backtest.strategy.BackTestTradingRecord
import org.ta4j.core.num.NumFactory

internal class ReturnCriterionTest {
    private val clock: Clock = Clock.fixed(Instant.now(), ZoneId.systemDefault())


    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateWithWinningLongPositions(numFactory: NumFactory?) {
        val context = TradingRecordTestContext()
            .withNumFactory(numFactory)
            .withTradeType(TradeType.BUY)

        // First trade: buy at 100, sell at 110 (return: 1.10)
        context.enter(1.0).at(100.0)
            .exit(1.0).at(110.0)

        // Second trade: buy at 100, sell at 105 (return: 1.05)
        context.enter(1.0).at(100.0)
            .exit(1.0).at(105.0)

        // Total return with base percentage: 1.10 * 1.05
        context.withCriterion(ReturnCriterion())
            .assertResults(1.10 * 1.05)

        // Total return without base percentage: (1.10 * 1.05) - 1
        context.withCriterion(ReturnCriterion(false))
            .assertResults(1.10 * 1.05 - 1)
    }


    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateWithLosingLongPositions(numFactory: NumFactory?) {
        val context = TradingRecordTestContext()
            .withNumFactory(numFactory)
            .withTradeType(TradeType.BUY)

        // First trade: buy at 100, sell at 95 (return: 0.95)
        context.enter(1.0).at(100.0)
            .exit(1.0).at(95.0)

        // Second trade: buy at 100, sell at 70 (return: 0.70)
        context.enter(1.0).at(100.0)
            .exit(1.0).at(70.0)

        // Total return with base percentage: 0.95 * 0.70
        context.withCriterion(ReturnCriterion())
            .assertResults(0.95 * 0.70)

        // Total return without base percentage: (0.95 * 0.70) - 1
        context.withCriterion(ReturnCriterion(false))
            .assertResults(0.95 * 0.70 - 1)
    }


    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateReturnWithWinningShortPositions(numFactory: NumFactory?) {
        val context = TradingRecordTestContext()
            .withNumFactory(numFactory)
            .withTradeType(TradeType.SELL)

        // First trade: sell at 100, buy at 95 (return: 1.05)
        context.enter(1.0).at(100.0)
            .exit(1.0).at(95.0)

        // Second trade: sell at 100, buy at 70 (return: 1.30)
        context.enter(1.0).at(100.0)
            .exit(1.0).at(70.0)

        // Total return with base percentage: 1.05 * 1.30
        context.withCriterion(ReturnCriterion())
            .assertResults(1.05 * 1.30)

        // Total return without base percentage: (1.05 * 1.30) - 1
        context.withCriterion(ReturnCriterion(false))
            .assertResults(1.05 * 1.30 - 1)
    }


    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateReturnWithLosingShortPositions(numFactory: NumFactory?) {
        val context = TradingRecordTestContext()
            .withNumFactory(numFactory)
            .withTradeType(TradeType.SELL)

        // First trade: sell at 100, buy at 105 (return: 0.95)
        context.enter(1.0).at(100.0)
            .exit(1.0).at(105.0)

        // Second trade: sell at 100, buy at 130 (return: 0.70)
        context.enter(1.0).at(100.0)
            .exit(1.0).at(130.0)

        // Total return with base percentage: 0.95 * 0.70
        context.withCriterion(ReturnCriterion())
            .assertResults(0.95 * 0.70)

        // Total return without base percentage: (0.95 * 0.70) - 1
        context.withCriterion(ReturnCriterion(false))
            .assertResults(0.95 * 0.70 - 1)
    }


    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateWithNoPositions(numFactory: NumFactory) {
        val tradingRecord = BackTestTradingRecord(startingType = TradeType.BUY, numFactory = numFactory)

        val withBase = ReturnCriterion()
        assertNumEquals(1.0, withBase.calculate(tradingRecord))

        val withoutBase = ReturnCriterion(false)
        assertNumEquals(0.0, withoutBase.calculate(tradingRecord))
    }


    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateWithOpenedPosition(numFactory: NumFactory) {
        var position = Position(TradeType.BUY, numFactory = numFactory)

        // Test with base percentage
        val withBase = ReturnCriterion()
        assertNumEquals(1.0, withBase.calculate(position))

        // Add entry operation
        val now = Instant.now(this.clock)
        position.operate(now, numFactory.numOf(100), numFactory.numOf(1))
        assertNumEquals(1.0, withBase.calculate(position))

        // Test without base percentage
        position = Position(TradeType.BUY, numFactory = numFactory)
        val withoutBase = ReturnCriterion(false)
        assertNumEquals(0.0, withoutBase.calculate(position))

        // Add entry operation
        position.operate(now, numFactory.numOf(100), numFactory.numOf(1))
        assertNumEquals(0.0, withoutBase.calculate(position))
    }
}
