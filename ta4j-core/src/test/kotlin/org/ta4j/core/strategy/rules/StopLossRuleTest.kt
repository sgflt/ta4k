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
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.TradeType
import org.ta4j.core.api.Indicators
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory
import org.ta4j.core.strategy.RuntimeContext
import org.ta4j.core.strategy.RuntimeValueResolver

class StopLossRuleTest {

    companion object {
        /**
         * Creates a mocked RuntimeContext with specified entry price and trade type.
         */
        fun createRuntimeContextMock(
            entryPrice: Num? = null,
            tradeType: TradeType = TradeType.BUY,
        ): RuntimeContext = mockk<RuntimeContext>().apply {
            every { getValue("currentEntryPrice") } returns entryPrice
            every { getValue("currentTradeType") } returns tradeType

            every { getValue(any<RuntimeValueResolver<Any?>>()) } answers {
                val resolver = firstArg<RuntimeValueResolver<*>>()
                resolver.resolve(this@apply)
            }
        }

        /**
         * Creates a mocked RuntimeContext with entry price as Double.
         */
        fun createRuntimeContextMock(
            numFactory: NumFactory,
            entryPrice: Double? = null,
            tradeType: TradeType = TradeType.BUY,
        ): RuntimeContext = createRuntimeContextMock(
            entryPrice = entryPrice?.let { numFactory.numOf(it) },
            tradeType = tradeType
        )
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should trigger stop loss for BUY position when price drops below threshold`(numFactory: NumFactory) {
        val runtimeContext = createRuntimeContextMock(numFactory, 100.0, TradeType.BUY)

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 95.0, 90.0, 85.0)  // Price drops from 100 to 85

        val closePrice = Indicators.extended(numFactory).closePrice()
        context.withIndicator(closePrice)

        val stopLossRule = StopLossRule(closePrice, 5.0, runtimeContext) // 5% stop loss

        context.advance() // Price at 100 (entry)
        assertThat(stopLossRule.isSatisfied).isFalse() // At entry price, no stop

        context.advance() // Price at 95 (exactly at threshold: 100 * 0.95 = 95)
        assertThat(stopLossRule.isSatisfied).isTrue() // Should trigger at 5% loss

        context.advance() // Price at 90 (below threshold)
        assertThat(stopLossRule.isSatisfied).isTrue() // Should still be triggered

        context.advance() // Price at 85 (further below threshold)
        assertThat(stopLossRule.isSatisfied).isTrue() // Should still be triggered
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should not trigger stop loss for BUY position when price stays above threshold`(numFactory: NumFactory) {
        val runtimeContext = createRuntimeContextMock(numFactory, 100.0, TradeType.BUY)

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 98.0, 96.0, 95.1) // Price stays above 5% threshold

        val closePrice = Indicators.extended(numFactory).closePrice()
        context.withIndicator(closePrice)

        val stopLossRule = StopLossRule(closePrice, 5.0, runtimeContext)

        while (context.advance()) {
            assertThat(stopLossRule.isSatisfied).isFalse() // Should never trigger
        }
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should trigger stop loss for SELL position when price rises above threshold`(numFactory: NumFactory) {
        val runtimeContext = createRuntimeContextMock(numFactory, 100.0, TradeType.SELL)

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 103.0, 105.0, 110.0) // Price rises from 100 to 110

        val closePrice = Indicators.extended(numFactory).closePrice()
        context.withIndicator(closePrice)

        val stopLossRule = StopLossRule(closePrice, 5.0, runtimeContext) // 5% stop loss

        context.advance() // Price at 100 (entry)
        assertThat(stopLossRule.isSatisfied).isFalse() // At entry price, no stop

        context.advance() // Price at 103 (below threshold: 100 * 1.05 = 105)
        assertThat(stopLossRule.isSatisfied).isFalse() // Should not trigger yet

        context.advance() // Price at 105 (exactly at threshold)
        assertThat(stopLossRule.isSatisfied).isTrue() // Should trigger at 5% loss

        context.advance() // Price at 110 (above threshold)
        assertThat(stopLossRule.isSatisfied).isTrue() // Should still be triggered
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should not trigger stop loss for SELL position when price stays below threshold`(numFactory: NumFactory) {
        val runtimeContext = createRuntimeContextMock(numFactory, 100.0, TradeType.SELL)

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 102.0, 104.0, 104.9) // Price stays below 5% threshold

        val closePrice = Indicators.extended(numFactory).closePrice()
        context.withIndicator(closePrice)

        val stopLossRule = StopLossRule(closePrice, 5.0, runtimeContext)

        while (context.advance()) {
            assertThat(stopLossRule.isSatisfied).isFalse() // Should never trigger
        }
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should not be satisfied when entry price is not available`(numFactory: NumFactory) {
        val runtimeContext = createRuntimeContextMock(numFactory, null, TradeType.BUY)

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 50.0) // Huge price drop

        val closePrice = Indicators.extended(numFactory).closePrice()
        context.withIndicator(closePrice)

        val stopLossRule = StopLossRule(closePrice, 5.0, runtimeContext)

        while (context.advance()) {
            assertThat(stopLossRule.isSatisfied).isFalse() // Should never trigger without entry price
        }
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle high loss percentage`(numFactory: NumFactory) {
        val runtimeContext = createRuntimeContextMock(numFactory, 100.0)

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 50.0, 30.0, 10.0) // Huge drops

        val closePrice = Indicators.extended(numFactory).closePrice()
        context.withIndicator(closePrice)

        val stopLossRule = StopLossRule(closePrice, 50.0, runtimeContext) // 50% stop loss

        context.advance() // Price at 100
        assertThat(stopLossRule.isSatisfied).isFalse()

        context.advance() // Price at 50 (exactly at threshold: 100 * 0.5 = 50)
        assertThat(stopLossRule.isSatisfied).isTrue() // Should trigger at 50% loss

        context.advance() // Price at 30
        assertThat(stopLossRule.isSatisfied).isTrue()

        context.advance() // Price at 10
        assertThat(stopLossRule.isSatisfied).isTrue()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should work with Num constructor`(numFactory: NumFactory) {
        val runtimeContext = createRuntimeContextMock(numFactory, 100.0)

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 95.0)

        val closePrice = Indicators.extended(numFactory).closePrice()
        context.withIndicator(closePrice)

        val lossPercentage = numFactory.numOf(5.0)
        val stopLossRule = StopLossRule(closePrice, lossPercentage, runtimeContext)

        context.advance() // Price at 100
        assertThat(stopLossRule.isSatisfied).isFalse()

        context.advance() // Price at 95 (5% drop)
        assertThat(stopLossRule.isSatisfied).isTrue()
    }

    @Test
    fun `should throw exception for negative loss percentage`() {
        val runtimeContext = mockk<RuntimeContext>()
        val context = MarketEventTestContext()
            .withCandlePrices(100.0)

        val closePrice = Indicators.closePrice()
        context.withIndicator(closePrice)

        assertThatThrownBy {
            StopLossRule(closePrice, -5.0, runtimeContext)
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("lossPercentage must be non-negative")
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should have meaningful string representation`(numFactory: NumFactory) {
        val runtimeContext = createRuntimeContextMock(numFactory, 100.0, TradeType.BUY)
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0)

        val closePrice = Indicators.closePrice()
        context.withIndicator(closePrice)

        val stopLossRule = StopLossRule(closePrice, 10.5, runtimeContext)

        assertThat(stopLossRule.toString()).contains("StopLossRule")
        assertThat(stopLossRule.toString()).contains("10.5%")
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle price exactly at threshold for BUY position`(numFactory: NumFactory) {
        val runtimeContext = createRuntimeContextMock(numFactory, 100.0)

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 95.0) // Exactly 5% drop

        val closePrice = Indicators.extended(numFactory).closePrice()
        context.withIndicator(closePrice)

        val stopLossRule = StopLossRule(closePrice, 5.0, runtimeContext)

        context.advance() // Price at 100
        assertThat(stopLossRule.isSatisfied).isFalse()

        context.advance() // Price at 95 (exactly at threshold)
        assertThat(stopLossRule.isSatisfied).isTrue() // Should trigger at exact threshold
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle price exactly at threshold for SELL position`(numFactory: NumFactory) {
        val runtimeContext = createRuntimeContextMock(numFactory, 100.0, TradeType.SELL)

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 105.0) // Exactly 5% rise

        val closePrice = Indicators.extended(numFactory).closePrice()
        context.withIndicator(closePrice)

        val stopLossRule = StopLossRule(closePrice, 5.0, runtimeContext)

        context.advance() // Price at 100
        assertThat(stopLossRule.isSatisfied).isFalse()

        context.advance() // Price at 105 (exactly at threshold)
        assertThat(stopLossRule.isSatisfied).isTrue() // Should trigger at exact threshold
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle fractional loss percentages`(numFactory: NumFactory) {
        val runtimeContext = createRuntimeContextMock(numFactory, 100.0)

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 97.5, 97.49) // Test 2.5% loss threshold

        val closePrice = Indicators.extended(numFactory).closePrice()
        context.withIndicator(closePrice)

        val stopLossRule = StopLossRule(closePrice, 2.5, runtimeContext) // 2.5% stop loss

        context.advance() // Price at 100
        assertThat(stopLossRule.isSatisfied).isFalse()

        context.advance() // Price at 97.5 (exactly at threshold: 100 * 0.975 = 97.5)
        assertThat(stopLossRule.isSatisfied).isTrue()

        context.advance() // Price at 97.49 (below threshold)
        assertThat(stopLossRule.isSatisfied).isTrue()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should work with different indicator types`(numFactory: NumFactory) {
        val runtimeContext = createRuntimeContextMock(numFactory, 100.0)

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 95.0)

        // Test with SMA instead of close price
        val sma = Indicators.extended(numFactory).closePrice().sma(1) // 1-period SMA = close price
        context.withIndicator(sma)

        val stopLossRule = StopLossRule(sma, 5.0, runtimeContext)

        context.advance() // Price at 100
        assertThat(stopLossRule.isSatisfied).isFalse()

        context.advance() // Price at 95
        assertThat(stopLossRule.isSatisfied).isTrue()
    }
}
