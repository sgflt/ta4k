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
package org.ta4j.core.indicators.bool

import java.time.Instant
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.TradeType
import org.ta4j.core.api.Indicators
import org.ta4j.core.indicators.bool.TrailingStopLossIndicator.PositionInfo
import org.ta4j.core.num.NumFactory
import org.ta4j.core.num.NumFactoryProvider
import org.ta4j.core.strategy.RuntimeContext

class TrailingStopLossIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should return false when no position is open`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 105.0, 110.0, 95.0)

        val priceIndicator = Indicators.extended(numFactory).closePrice()
        context.withIndicator(priceIndicator)

        val mockRuntimeContext = mockk<RuntimeContext>()
        every { mockRuntimeContext.getValue<PositionInfo?>(any()) } returns null

        val indicator = TrailingStopLossIndicator(
            priceIndicator = priceIndicator,
            lossPercentage = numFactory.numOf(10.0),
            runtimeContext = mockRuntimeContext,
            barCount = 4
        )
        context.withIndicator(indicator)

        // Advance through some price movements
        repeat(4) {
            context.advance()
            assertThat(indicator.value).isFalse()
        }
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should trigger stop loss for BUY position when price falls below threshold`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 105.0, 110.0, 95.0) // Price rises then falls

        val priceIndicator = Indicators.extended(numFactory).closePrice()
        context.withIndicator(priceIndicator)

        val mockRuntimeContext = mockk<RuntimeContext>()
        every { mockRuntimeContext.getValue<PositionInfo?>(any()) } returns PositionInfo(
            isOpen = true,
            tradeType = TradeType.BUY,
            entryTime = Instant.now(),
            entryPrice = numFactory.numOf(100.0),
            barsInPosition = 4
        )

        val indicator = TrailingStopLossIndicator(
            priceIndicator = priceIndicator,
            lossPercentage = numFactory.numOf(10.0), // 10% trailing stop
            runtimeContext = mockRuntimeContext,
            barCount = 4
        )
        context.withIndicator(indicator)

        // Move through price sequence
        context.advance() // 100.0
        assertThat(indicator.value).isFalse() // No loss yet

        context.advance() // 105.0
        assertThat(indicator.value).isFalse() // Price rising

        context.advance() // 110.0 - new high
        assertThat(indicator.value).isFalse() // Still rising

        context.advance() // 95.0 - significant drop
        // Highest was 110, 10% trailing stop = 110 * 0.9 = 99
        // Current price 95 < 99, so stop loss should trigger
        assertThat(indicator.value).isTrue()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should not trigger stop loss for BUY position when price stays above threshold`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 105.0, 110.0, 101.0) // Price rises then minor dip

        val priceIndicator = Indicators.extended(numFactory).closePrice()
        context.withIndicator(priceIndicator)

        val mockRuntimeContext = mockk<RuntimeContext>()
        val positionInfo = PositionInfo(
            isOpen = true,
            tradeType = TradeType.BUY,
            entryTime = Instant.now(),
            entryPrice = numFactory.numOf(100.0),
            barsInPosition = 4
        )
        every { mockRuntimeContext.getValue<PositionInfo?>(any()) } returns positionInfo

        val indicator = TrailingStopLossIndicator(
            priceIndicator = priceIndicator,
            lossPercentage = numFactory.numOf(10.0), // 10% trailing stop
            runtimeContext = mockRuntimeContext,
            barCount = 4
        )
        context.withIndicator(indicator)

        // Move through price sequence
        repeat(4) {
            context.advance()
        }

        // Highest is 110, 10% trailing stop = 110 * 0.9 = 99
        // Final price 101 > 99, so stop loss should not trigger
        assertThat(indicator.value).isFalse()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should trigger stop loss for SELL position when price rises above threshold`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 95.0, 90.0, 105.0) // Price falls then rises

        val priceIndicator = Indicators.extended(numFactory).closePrice()
        context.withIndicator(priceIndicator)

        val mockRuntimeContext = mockk<RuntimeContext>()
        val positionInfo = PositionInfo(
            isOpen = true,
            tradeType = TradeType.SELL,
            entryTime = Instant.now(),
            entryPrice = numFactory.numOf(100.0),
            barsInPosition = 4
        )
        every { mockRuntimeContext.getValue<PositionInfo?>(any()) } returns positionInfo

        val indicator = TrailingStopLossIndicator(
            priceIndicator = priceIndicator,
            lossPercentage = numFactory.numOf(10.0), // 10% trailing stop
            runtimeContext = mockRuntimeContext,
            barCount = 4
        )
        context.withIndicator(indicator)

        // Move through price sequence
        context.advance() // 100.0
        assertThat(indicator.value).isFalse()

        context.advance() // 95.0
        assertThat(indicator.value).isFalse() // Price falling (good for SELL)

        context.advance() // 90.0 - new low
        assertThat(indicator.value).isFalse() // Still falling

        context.advance() // 105.0 - significant rise
        // Lowest was 90, 10% trailing stop = 90 * 1.1 = 99
        // Current price 105 > 99, so stop loss should trigger
        assertThat(indicator.value).isTrue()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should respect barCount limitation`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 120.0, 110.0, 105.0, 95.0) // High early, then decline

        val priceIndicator = Indicators.extended(numFactory).closePrice()
        context.withIndicator(priceIndicator)

        val mockRuntimeContext = mockk<RuntimeContext>()
        val positionInfo = PositionInfo(
            isOpen = true,
            tradeType = TradeType.BUY,
            entryTime = Instant.now(),
            entryPrice = numFactory.numOf(100.0),
            barsInPosition = 5
        )
        every { mockRuntimeContext.getValue<PositionInfo?>(any()) } returns positionInfo

        // Indicator that only looks back 2 bars
        val indicator = TrailingStopLossIndicator(
            priceIndicator = priceIndicator,
            lossPercentage = numFactory.numOf(10.0),
            barCount = 2, // Only look back 2 bars
            runtimeContext = mockRuntimeContext
        )
        context.withIndicator(indicator)

        // Move to the end
        repeat(5) { context.advance() }

        // With barCount=2, highest in last 2 bars is max(105, 95) = 105
        // 10% trailing stop = 105 * 0.9 = 94.5
        // Current price 95 > 94.5, so should NOT trigger
        assertThat(indicator.value).isFalse()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle edge case with same entry and current price`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 100.0, 100.0)

        val priceIndicator = Indicators.extended(numFactory).closePrice()
        context.withIndicator(priceIndicator)

        val mockRuntimeContext = mockk<RuntimeContext>()
        val positionInfo = PositionInfo(
            isOpen = true,
            tradeType = TradeType.BUY,
            entryTime = Instant.now(),
            entryPrice = numFactory.numOf(100.0),
            barsInPosition = 3
        )
        every { mockRuntimeContext.getValue<PositionInfo?>(any()) } returns positionInfo

        val indicator = TrailingStopLossIndicator(
            priceIndicator = priceIndicator,
            lossPercentage = numFactory.numOf(10.0),
            runtimeContext = mockRuntimeContext,
            barCount = 4
        )
        context.withIndicator(indicator)

        repeat(3) {
            context.advance()
            // Price is flat at 100, trailing stop = 100 * 0.9 = 90
            // Current price 100 > 90, so should not trigger
            assertThat(indicator.value).isFalse()
        }
    }

    @Test
    fun `should have meaningful string representation`() {
        val numFactory = NumFactoryProvider.defaultNumFactory
        val priceIndicator = Indicators.extended(numFactory).closePrice()

        val indicator = TrailingStopLossIndicator(
            priceIndicator = priceIndicator,
            lossPercentage = numFactory.numOf(15.0),
            runtimeContext = mockk<RuntimeContext>(),
            barCount = 50,
        )

        val stringRep = indicator.toString()
        assertThat(stringRep).contains("TrailingStopLoss")
        assertThat(stringRep).contains("15")
        assertThat(stringRep).contains("barCount=50")
    }
}
