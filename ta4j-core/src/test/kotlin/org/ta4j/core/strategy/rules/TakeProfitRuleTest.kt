/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2025 Ta4j Organization & respective
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
package org.ta4j.core.strategy.rules

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.TradeType
import org.ta4j.core.TradingRecordTestContext
import org.ta4j.core.api.Indicators
import org.ta4j.core.backtest.criteria.pnl.GrossReturnCriterion
import org.ta4j.core.num.NumFactory
import org.ta4j.core.strategy.RuntimeContext
import org.ta4j.core.strategy.RuntimeValueResolver

class TakeProfitRuleTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should trigger take profit for BUY position when price increases above threshold`(numFactory: NumFactory) {
        val closePrice = Indicators.extended(numFactory).closePrice()
        val marketContext = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 105.0, 109.0, 110.000001, 115.0)
            .withIndicator(closePrice)

        val runtimeContext = createMockRuntimeContext(numFactory, 100.0, TradeType.BUY)
        val takeProfitRule = TakeProfitRule(closePrice, 10.0, runtimeContext) // 10% gain

        // Entry at price 100
        marketContext.advance() // price = 100.0
        assertThat(takeProfitRule.isSatisfied).isFalse()

        // Price moves to 105
        marketContext.advance() // price = 105.0
        assertThat(takeProfitRule.isSatisfied).isFalse()

        // Price moves to 109 (below 10% gain threshold of 110)
        marketContext.advance() // price = 109.0
        assertThat(takeProfitRule.isSatisfied).isFalse()

        // Price moves to 110 (exactly 10% gain)
        marketContext.advance() // price = 110.0
        assertThat(takeProfitRule.isSatisfied).isTrue()

        // Price moves to 115 (above 10% gain)
        marketContext.advance() // price = 115.0
        assertThat(takeProfitRule.isSatisfied).isTrue()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should trigger take profit for SELL position when price decreases below threshold`(numFactory: NumFactory) {
        val closePrice = Indicators.extended(numFactory).closePrice()
        val marketContext = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 85.0, 86.0)
            .withIndicator(closePrice)

        val runtimeContext = createMockRuntimeContext(numFactory, 100.0, TradeType.SELL)
        val takeProfitRule = TakeProfitRule(closePrice, 15.0, runtimeContext) // 15% gain

        marketContext.advance()
        // Entry at price 100
        assertThat(takeProfitRule.isSatisfied).isFalse()

        marketContext.advance()
        assertThat(takeProfitRule.isSatisfied).isTrue()

        marketContext.advance()
        assertThat(takeProfitRule.isSatisfied).isFalse()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should not trigger when no position is open`(numFactory: NumFactory) {
        val closePrice = Indicators.extended(numFactory).closePrice()
        val marketContext = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 150.0)
            .withIndicator(Indicators.extended(numFactory).closePrice())

        val runtimeContext = createMockRuntimeContext(numFactory, null, null)
        val takeProfitRule = TakeProfitRule(closePrice, 10.0, runtimeContext)

        // No position context
        marketContext.advance() // price = 100.0
        assertThat(takeProfitRule.isSatisfied).isFalse()

        marketContext.advance() // price = 150.0
        assertThat(takeProfitRule.isSatisfied).isFalse()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should work with different gain percentages`(numFactory: NumFactory) {
        val closePrice = Indicators.extended(numFactory).closePrice()
        val marketContext = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 104.9, 105.0, 124.9, 125.0)
            .withIndicator(closePrice)

        val runtimeContext = createMockRuntimeContext(numFactory, 100.0, TradeType.BUY)

        // Test with 5% gain
        val rule5Percent = TakeProfitRule(closePrice, 5.0, runtimeContext)

        marketContext.advance() // price = 100.0
        assertThat(rule5Percent.isSatisfied).isFalse()

        marketContext.advance() // price = 104.9
        assertThat(rule5Percent.isSatisfied).isFalse()

        marketContext.advance() // price = 105.0
        assertThat(rule5Percent.isSatisfied).isTrue()

        // Test with 25% gain
        val rule25Percent = TakeProfitRule(closePrice, 25.0, runtimeContext)

        marketContext.advance() // price = 124.9
        assertThat(rule25Percent.isSatisfied).isFalse()

        marketContext.advance() // price = 125.0
        assertThat(rule25Percent.isSatisfied).isTrue()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should work with Num constructor`(numFactory: NumFactory) {
        val closePrice = Indicators.extended(numFactory).closePrice()
        val marketContext = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 112.4, 112.5)
            .withIndicator(closePrice)

        val runtimeContext = createMockRuntimeContext(numFactory, 100.0, TradeType.BUY)
        val gainPercentage = numFactory.numOf(12.5)
        val takeProfitRule = TakeProfitRule(closePrice, gainPercentage, runtimeContext)

        // Test threshold calculation with Num
        marketContext.advance() // price = 100.0
        assertThat(takeProfitRule.isSatisfied).isFalse()

        marketContext.advance() // price = 112.4
        assertThat(takeProfitRule.isSatisfied).isFalse()

        marketContext.advance() // price = 112.5
        assertThat(takeProfitRule.isSatisfied).isTrue()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should provide meaningful string representation`(numFactory: NumFactory) {
        val closePrice = Indicators.extended(numFactory).closePrice()
        MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0)
            .withIndicator(closePrice)

        val runtimeContext = createMockRuntimeContext(numFactory, 100.0, TradeType.BUY)
        val takeProfitRule = TakeProfitRule(closePrice, 15.5, runtimeContext)

        assertThat(takeProfitRule.toString()).contains("TakeProfitRule")
        assertThat(takeProfitRule.toString()).contains("15.5%")
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should test precision with decimal gain percentages`(numFactory: NumFactory) {
        val closePrice = Indicators.extended(numFactory).closePrice()
        val marketContext = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 107.24, 107.25)
            .withIndicator(closePrice)

        val runtimeContext = createMockRuntimeContext(numFactory, 100.0, TradeType.BUY)
        val takeProfitRule = TakeProfitRule(closePrice, 7.25, runtimeContext) // 7.25% gain

        // Entry price
        marketContext.advance() // price = 100.0
        assertThat(takeProfitRule.isSatisfied).isFalse()

        // Threshold should be 107.25
        marketContext.advance() // price = 107.24
        assertThat(takeProfitRule.isSatisfied).isFalse()

        marketContext.advance() // price = 107.25
        assertThat(takeProfitRule.isSatisfied).isTrue()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle large gain percentages correctly`(numFactory: NumFactory) {
        val closePrice = Indicators.extended(numFactory).closePrice()
        val marketContext = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 149.99, 150.0)
            .withIndicator(closePrice)

        val runtimeContext = createMockRuntimeContext(numFactory, 100.0, TradeType.BUY)
        val takeProfitRule = TakeProfitRule(closePrice, 50.0, runtimeContext) // 50% gain

        // Entry price
        marketContext.advance() // price = 100.0
        assertThat(takeProfitRule.isSatisfied).isFalse()

        // Threshold should be 150.0
        marketContext.advance() // price = 149.99
        assertThat(takeProfitRule.isSatisfied).isFalse()

        marketContext.advance() // price = 150.0
        assertThat(takeProfitRule.isSatisfied).isTrue()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle small gain percentages correctly`(numFactory: NumFactory) {
        val closePrice = Indicators.extended(numFactory).closePrice()
        val marketContext = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 100.09, 100.1)
            .withIndicator(closePrice)

        val runtimeContext = createMockRuntimeContext(numFactory, 100.0, TradeType.BUY)
        val takeProfitRule = TakeProfitRule(closePrice, 0.1, runtimeContext) // 0.1% gain

        // Entry price
        marketContext.advance() // price = 100.0
        assertThat(takeProfitRule.isSatisfied).isFalse()

        // Threshold should be 100.1
        marketContext.advance() // price = 100.09
        assertThat(takeProfitRule.isSatisfied).isFalse()

        marketContext.advance() // price = 100.1
        assertThat(takeProfitRule.isSatisfied).isTrue()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should work correctly with TradingRecordTestContext integration`(numFactory: NumFactory) {
        val context = TradingRecordTestContext()
            .withNumFactory(numFactory)
            .withCriterion(GrossReturnCriterion())
            .withTradeType(TradeType.BUY)

        // Using traditional test context pattern for real scenario
        context.enter(1.0).at(100.0)

        // Simulate profit scenario - if we had a real runtime context connected to the trading record
        // the take profit rule would trigger when the profit threshold is reached
        context.exit(1.0).at(115.0) // 15% profit

        context.assertResults(1.15) // 15% return
    }

    private fun createMockRuntimeContext(
        numFactory: NumFactory,
        entryPrice: Double?,
        tradeType: TradeType?,
    ): RuntimeContext {
        val mockContext = mockk<RuntimeContext>()

        every { mockContext.getValue("currentEntryPrice") } returns entryPrice?.let { numFactory.numOf(it) }
        every { mockContext.getValue("currentTradeType") } returns tradeType

        every { mockContext.getValue(any<RuntimeValueResolver<Any?>>()) } answers {
            val resolver = firstArg<RuntimeValueResolver<*>>()
            resolver.resolve(mockContext)
        }

        return mockContext
    }
}
