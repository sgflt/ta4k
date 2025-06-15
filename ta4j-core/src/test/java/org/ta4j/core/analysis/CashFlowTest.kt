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

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.slf4j.LoggerFactory
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.TestUtils.assertNumEquals
import org.ta4j.core.TradeType
import org.ta4j.core.backtest.analysis.CashFlow
import org.ta4j.core.num.NumFactory
import java.time.Instant

class CashFlowTest {

    private val log = LoggerFactory.getLogger(CashFlowTest::class.java)

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun cashFlowBuyWithOnlyOnePosition(numFactory: NumFactory) {
        val tradingContext = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(1.0, 2.0)
            .toTradingRecordContext()
            .enter(1.0).after(1)
            .exit(1.0).after(1)

        val cashFlow = CashFlow(tradingContext.tradingRecord)

        assertNumEquals(numFactory.one(), cashFlow.getValue(tradingContext.barSeries.getBar(0).endTime))
        assertNumEquals(numFactory.numOf(2), cashFlow.getValue(tradingContext.barSeries.getBar(1).endTime))
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun cashFlowShortSellWith20PercentGain(numFactory: NumFactory) {
        val tradingContext = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 90.0, 80.0)
            .toTradingRecordContext()
            .withTradeType(TradeType.SELL)
            .enter(1.0).after(1)
            .exit(1.0).after(2)

        val cashFlow = CashFlow(tradingContext.tradingRecord)
        val bars = tradingContext.barSeries

        assertNumEquals(numFactory.one(), cashFlow.getValue(bars.getBar(0).endTime))
        assertNumEquals(numFactory.numOf(1.1), cashFlow.getValue(bars.getBar(1).endTime))
        assertNumEquals(numFactory.numOf(1.2), cashFlow.getValue(bars.getBar(2).endTime))
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun cashFlowLongWith50PercentLoss(numFactory: NumFactory) {
        val tradingContext = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(200.0, 190.0, 180.0, 170.0, 160.0, 150.0, 140.0, 130.0, 120.0, 110.0, 100.0)
            .toTradingRecordContext()
            .withTradeType(TradeType.BUY)

        tradingContext.enter(1.0).after(1)
        tradingContext.exit(1.0).after(10)

        val cashFlow = CashFlow(tradingContext.tradingRecord)
        val bars = tradingContext.barSeries

        // Check values at each step (price decreases by 10 each bar)
        for (i in 0..10) {
            log.debug("{}: {}@{}", i, bars.getBar(i).endTime, bars.getBar(i).closePrice)
            val expectedValue = if (i < 1) {
                numFactory.one()
            } else {
                numFactory.one() - (numFactory.numOf(0.05) * numFactory.numOf(i))
            }
            log.debug("expected {}", expectedValue)
            assertNumEquals(expectedValue, cashFlow.getValue(bars.getBar(i).endTime))
        }
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun cashFlowShortSellWith100PercentLoss(numFactory: NumFactory) {
        val tradingContext = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 110.0, 120.0, 130.0, 140.0, 150.0, 160.0, 170.0, 180.0, 190.0, 200.0)
            .toTradingRecordContext()
            .withTradeType(TradeType.SELL)
            .enter(1.0).after(1)
            .exit(1.0).after(10)

        val cashFlow = CashFlow(tradingContext.tradingRecord)
        val bars = tradingContext.barSeries

        // Check values at each step (price increases by 10 each bar)
        for (i in 0..10) {
            log.debug("{}: {}@{}", i, bars.getBar(i).endTime, bars.getBar(i).closePrice)
            val expectedValue = if (i < 1) {
                numFactory.one()
            } else {
                numFactory.one() - (numFactory.numOf(0.1) * numFactory.numOf(i))
            }
            log.debug("expected {}", expectedValue)
            assertNumEquals(expectedValue, cashFlow.getValue(bars.getBar(i).endTime))
        }
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun cashFlowWithNoTrades(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
        val tradingRecord = context.toTradingRecordContext().tradingRecord
        val cashFlow = CashFlow(tradingRecord)

        assertNumEquals(numFactory.one(), cashFlow.getValue(Instant.EPOCH))
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun cashFlowHODL(numFactory: NumFactory) {
        val tradingContext = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 120.0, 150.0, 135.0, 100.0, 100.0, 100.0, 200.0, 200.0, 160.0)
            .toTradingRecordContext()
            .withTradeType(TradeType.BUY)

        // Position 1: 100 -> 160 (profit: +60%)
        tradingContext.enter(1.0).after(1)
        tradingContext.exit(1.0).after(9)

        val cashFlow = CashFlow(tradingContext.tradingRecord)
        val bars = tradingContext.barSeries

        // Initial value
        assertNumEquals(numFactory.one(), cashFlow.getValue(bars.getBar(0).endTime))

        // After Position 1 close: 120/100 = 1.20
        assertNumEquals(numFactory.numOf(1.20), cashFlow.getValue(bars.getBar(1).endTime))

        // After Position 2 close: 135/100 = 0.9
        assertNumEquals(numFactory.numOf(1.35), cashFlow.getValue(bars.getBar(3).endTime))

        // After Position 3 close: 100/100
        assertNumEquals(numFactory.numOf(1.0), cashFlow.getValue(bars.getBar(5).endTime))

        // After Position 4 close: 200/100 = 2.0
        assertNumEquals(numFactory.numOf(2.0), cashFlow.getValue(bars.getBar(7).endTime))

        // After Position 5 close: 160 / 100 = 1.6
        assertNumEquals(numFactory.numOf(1.6), cashFlow.getValue(bars.getBar(9).endTime))
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun cashFlowWithMultipleBuyPositions(numFactory: NumFactory) {
        val tradingContext = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 120.0, 150.0, 135.0, 100.0, 100.0, 100.0, 200.0, 200.0, 160.0)
            .toTradingRecordContext()
            .withTradeType(TradeType.BUY)

        // Position 1: 100 -> 120 (profit: +20%)
        tradingContext.enter(1.0).after(1)
        tradingContext.exit(1.0).after(1)

        // Position 2: 150 -> 135 (loss: -10%)
        tradingContext.enter(1.0).after(1)
        tradingContext.exit(1.0).after(1)

        // Position 3: 100 -> 100 (neutral)
        tradingContext.enter(1.0).after(1)
        tradingContext.exit(1.0).after(1)

        // Position 4: 100 -> 200 (profit: +100%)
        tradingContext.enter(1.0).after(1)
        tradingContext.exit(1.0).after(1)

        // Position 5: 200 -> 160 (loss: -20%)
        tradingContext.enter(1.0).after(1)
        tradingContext.exit(1.0).after(1)

        val cashFlow = CashFlow(tradingContext.tradingRecord)
        val bars = tradingContext.barSeries

        // Initial value
        assertNumEquals(numFactory.one(), cashFlow.getValue(bars.getBar(0).endTime))

        // After Position 1 close: 120/100 = 1.20
        assertNumEquals(numFactory.numOf(1.20), cashFlow.getValue(bars.getBar(1).endTime))

        // After Position 2 close: 135/150 = 0.9
        assertNumEquals(numFactory.numOf(0.9), cashFlow.getValue(bars.getBar(3).endTime))

        // After Position 3 close: 100/100
        assertNumEquals(numFactory.numOf(1.0), cashFlow.getValue(bars.getBar(5).endTime))

        // After Position 4 close: 200/100 = 2.0
        assertNumEquals(numFactory.numOf(2.0), cashFlow.getValue(bars.getBar(7).endTime))

        // After Position 5 close: 160 / 200 = 0.8
        assertNumEquals(numFactory.numOf(0.8), cashFlow.getValue(bars.getBar(9).endTime))
    }
}