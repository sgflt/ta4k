/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2024 Ta4j Organization & respective
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
package org.ta4j.core.analysis

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.TestUtils.assertNumEquals
import org.ta4j.core.TradeType
import org.ta4j.core.TradingRecordTestContext
import org.ta4j.core.backtest.Position
import org.ta4j.core.backtest.analysis.RealizedCashFlow
import org.ta4j.core.backtest.strategy.BackTestTradingRecord
import org.ta4j.core.num.NumFactory
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

class RealizedCashFlowTest {

    private val clock = Clock.fixed(Instant.MIN, ZoneId.systemDefault())

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun cashFlowBuyWithOnlyOnePosition(numFactory: NumFactory) {
        val position = Position(TradeType.BUY, numFactory = numFactory)
        val now = Instant.now(clock)

        // Execute buy at price 1
        position.operate(now, numFactory.numOf(1), numFactory.numOf(1))

        // Execute sell at price 2
        position.operate(now.plus(Duration.ofMinutes(1)), numFactory.numOf(2), numFactory.numOf(1))

        val cashFlow = RealizedCashFlow(numFactory, position)

        assertNumEquals(numFactory.numOf(1), cashFlow.getValue(now))
        assertNumEquals(numFactory.numOf(2), cashFlow.getValue(now.plus(Duration.ofMinutes(1))))
    }

    @ParameterizedTest
    @Disabled("Ta4J currently does not support mixed trades")
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun cashFlowWithSellAndBuyTrades(numFactory: NumFactory) {
        val buyContext = TradingRecordTestContext()
            .withNumFactory(numFactory)
            .withTradeType(TradeType.BUY)

        // First trade: buy at 2, sell at 1 (loss: 50%)
        buyContext.enter(1.0).at(2.0)
        buyContext.exit(1.0).at(1.0)

        // Second trade: buy at 5, sell at 6 (profit: 20%)
        buyContext.enter(1.0).at(5.0)
        buyContext.exit(1.0).at(6.0)

        // Switch to sell context for the third trade
        val sellContext = TradingRecordTestContext()
            .withNumFactory(numFactory)
            .withTradeType(TradeType.SELL)

        // Third trade: sell at 3, buy at 20 (loss: large)
        sellContext.enter(1.0).at(3.0)
        sellContext.exit(1.0).at(20.0)

        // Merge the trading records
        val tradingRecord = buyContext.tradingRecord
        sellContext.tradingRecord.positions.forEach { position ->
            tradingRecord.enter(
                position.entry!!.whenExecuted,
                position.entry!!.netPrice,
                position.entry!!.amount
            )
            if (position.exit != null) {
                tradingRecord.exit(
                    position.exit!!.whenExecuted,
                    position.exit!!.netPrice,
                    position.exit!!.amount
                )
            }
        }

        val cashFlow = RealizedCashFlow(numFactory, tradingRecord)
        val now = Instant.now(clock)

        assertNumEquals(numFactory.numOf(1), cashFlow.getValue(now))
        assertNumEquals(numFactory.numOf(0.5), cashFlow.getValue(now.plus(Duration.ofMinutes(1))))
        assertNumEquals(numFactory.numOf(0.5), cashFlow.getValue(now.plus(Duration.ofMinutes(2))))
        assertNumEquals(numFactory.numOf(0.5), cashFlow.getValue(now.plus(Duration.ofMinutes(3))))
        assertNumEquals(numFactory.numOf(0.6), cashFlow.getValue(now.plus(Duration.ofMinutes(4))))
        assertNumEquals(numFactory.numOf(0.6), cashFlow.getValue(now.plus(Duration.ofMinutes(5))))
        assertNumEquals(numFactory.numOf(-2.8), cashFlow.getValue(now.plus(Duration.ofMinutes(6))))
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun cashFlowShortSellWith20PercentGain(numFactory: NumFactory) {
        val position = Position(TradeType.SELL, numFactory = numFactory)
        val now = Instant.now(clock)

        // Short sell at 100
        position.operate(now.plus(Duration.ofMinutes(1)), numFactory.numOf(100), numFactory.numOf(1))

        // Cover at 80 (20% gain)
        position.operate(now.plus(Duration.ofMinutes(3)), numFactory.numOf(80), numFactory.numOf(1))

        val cashFlow = RealizedCashFlow(numFactory, position)

        assertNumEquals(numFactory.numOf(1), cashFlow.getValue(now))
        assertNumEquals(numFactory.numOf(1), cashFlow.getValue(now.plus(Duration.ofMinutes(1))))
        assertNumEquals(numFactory.numOf(1.1), cashFlow.getValue(now.plus(Duration.ofMinutes(2))))
        assertNumEquals(numFactory.numOf(1.2), cashFlow.getValue(now.plus(Duration.ofMinutes(3))))
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun cashFlowShortSellWith100PercentLoss(numFactory: NumFactory) {
        val position = Position(TradeType.SELL, numFactory = numFactory)
        val now = Instant.now(clock)

        // Short sell at 100
        position.operate(now.plus(Duration.ofMinutes(1)), numFactory.numOf(100), numFactory.numOf(1))

        // Cover at 200 (100% loss)
        position.operate(now.plus(Duration.ofMinutes(11)), numFactory.numOf(200), numFactory.numOf(1))

        val cashFlow = RealizedCashFlow(numFactory, position)

        // Check values at each step (price increases by 10 each minute)
        for (i in 0..11) {
            val expectedValue = if (i <= 1) {
                numFactory.one()
            } else {
                val decline = numFactory.numOf(0.1) * numFactory.numOf(i - 1)
                val x = numFactory.one() - decline
                if (x > numFactory.zero()) x else numFactory.zero()
            }
            assertNumEquals(
                expectedValue,
                cashFlow.getValue(now.plus(Duration.ofMinutes(i.toLong())))
            )
        }
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun cashFlowWithNoTrades(numFactory: NumFactory) {
        val tradingRecord = BackTestTradingRecord(TradeType.BUY, numFactory = numFactory)
        val cashFlow = RealizedCashFlow(numFactory, tradingRecord)
        val now = Instant.now(clock)

        // Should return 1 for any timestamp when no trades exist
        assertNumEquals(numFactory.numOf(1), cashFlow.getValue(now))
        assertNumEquals(numFactory.numOf(1), cashFlow.getValue(now.plus(Duration.ofMinutes(5))))
        assertNumEquals(numFactory.numOf(1), cashFlow.getValue(now.plus(Duration.ofMinutes(10))))
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun cashFlowWithMultipleBuyPositions(numFactory: NumFactory) {
        val startTime = Instant.parse("1970-01-01T00:00:00.000Z")

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withStartTime(startTime)
            .withCandleDuration(ChronoUnit.MINUTES)
            .withCandlePrices(100.0, 120.0, 150.0, 135.0, 100.0, 100.0, 100.0, 200.0, 200.0, 160.0)
            .toTradingRecordContext()
            .withTradeType(TradeType.BUY)

        // Position 1: 100 -> 120 (profit: +20%)
        context.enter(1.0).asap()
        context.exit(1.0).asap()

        // Position 2: 150 -> 135 (loss: -10%)
        context.enter(1.0).asap()
        context.exit(1.0).asap()

        // Position 3: 100 -> 100 (neutral)
        context.enter(1.0).asap()
        context.exit(1.0).asap()

        // Position 4: 100 -> 200 (profit: +100%)
        context.enter(1.0).asap()
        context.exit(1.0).asap()

        // Position 5: 200 -> 160 (loss: -20%)
        context.enter(1.0).asap()
        context.exit(1.0).asap()

        val tradingRecord = context.tradingRecord
        val cashFlow = RealizedCashFlow(numFactory, tradingRecord)

        val endTime = startTime.plus(Duration.ofMinutes(1))

        // Initial value at the first trade time
        assertNumEquals(numFactory.one(), cashFlow.getValue(endTime.plus(Duration.ofMinutes(1))))

        // After Position 1 close: 1.0 * 1.20 = 1.20
        assertNumEquals(numFactory.numOf(1.20), cashFlow.getValue(endTime.plus(Duration.ofMinutes(2))))

        // After Position 2 close: 1.20 * 0.90 = 1.08
        assertNumEquals(numFactory.numOf(1.08), cashFlow.getValue(endTime.plus(Duration.ofMinutes(4))))

        // After Position 3 close: 1.08 * 1.00 = 1.08
        assertNumEquals(numFactory.numOf(1.08), cashFlow.getValue(endTime.plus(Duration.ofMinutes(6))))

        // After Position 4 close: 1.08 * 2.00 = 2.16
        assertNumEquals(numFactory.numOf(2.16), cashFlow.getValue(endTime.plus(Duration.ofMinutes(8))))

        // After Position 5 close: 2.16 * 0.80 = 1.728
        assertNumEquals(numFactory.numOf(1.728), cashFlow.getValue(endTime.plus(Duration.ofMinutes(10))))

        // Test interpolation points
        assertNumEquals(numFactory.numOf(1.10), cashFlow.getValue(endTime.plus(Duration.ofMinutes(1).plusSeconds(30))))
        assertNumEquals(numFactory.numOf(1.14), cashFlow.getValue(endTime.plus(Duration.ofMinutes(3))))
        assertNumEquals(numFactory.numOf(1.08), cashFlow.getValue(endTime.plus(Duration.ofMinutes(5))))
        assertNumEquals(numFactory.numOf(1.62), cashFlow.getValue(endTime.plus(Duration.ofMinutes(7))))
        assertNumEquals(numFactory.numOf(1.944), cashFlow.getValue(endTime.plus(Duration.ofMinutes(9))))
    }
}