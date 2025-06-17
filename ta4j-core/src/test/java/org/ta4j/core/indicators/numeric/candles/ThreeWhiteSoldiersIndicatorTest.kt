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
package org.ta4j.core.indicators.numeric.candles

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.mocks.MockMarketEventBuilder
import org.ta4j.core.num.NumFactory

class ThreeWhiteSoldiersIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun testValue(numFactory: NumFactory) {
        // Test data from original Java test - build with proper OHLC
        val marketEvents = MockMarketEventBuilder()
            .candle().openPrice(19.0).highPrice(22.0).lowPrice(15.0).closePrice(19.0).add()  // neutral
            .candle().openPrice(10.0).highPrice(20.0).lowPrice(8.0).closePrice(18.0).add()   // bullish
            .candle().openPrice(17.0).highPrice(21.0).lowPrice(15.0).closePrice(16.0).add()  // bearish (black candle)
            .candle().openPrice(15.6).highPrice(18.1).lowPrice(14.0).closePrice(18.0).add()  // bullish (soldier 1)
            .candle().openPrice(16.0).highPrice(20.0).lowPrice(15.0).closePrice(19.9).add()  // bullish (soldier 2)
            .candle().openPrice(16.8).highPrice(23.0).lowPrice(16.7).closePrice(23.0).add()  // bullish (soldier 3)
            .candle().openPrice(17.0).highPrice(25.0).lowPrice(17.0).closePrice(25.0).add()  // bullish
            .candle().openPrice(23.0).highPrice(24.0).lowPrice(15.0).closePrice(16.8).add()  // bearish
            .build()

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(marketEvents)
            .withIndicator(ThreeWhiteSoldiersIndicator(numFactory, 3, 0.1))

        // First few bars should return false (not enough data or no pattern)
        repeat(6) {
            context.assertNextFalse()
        }

        // At index 6, should detect the three white soldiers pattern
        context.assertNextTrue()

        // Remaining bar should be false
        context.assertNextFalse()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun testInitialBars(numFactory: NumFactory) {
        val marketEvents = MockMarketEventBuilder()
            .candle().openPrice(10.0).highPrice(13.0).lowPrice(9.0).closePrice(12.0).add()  // bullish
            .candle().openPrice(10.0).highPrice(13.0).lowPrice(9.0).closePrice(12.0).add()  // bullish
            .candle().openPrice(10.0).highPrice(13.0).lowPrice(9.0).closePrice(12.0).add()  // bullish
            .candle().openPrice(10.0).highPrice(13.0).lowPrice(9.0).closePrice(12.0).add()  // bullish
            .build()

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(marketEvents)
            .withIndicator(ThreeWhiteSoldiersIndicator(numFactory, 3, 0.1))

        // Should return false for first few bars when not enough data
        repeat(4) {
            context.assertNextFalse()
        }
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun testNoPattern(numFactory: NumFactory) {
        val marketEvents = MockMarketEventBuilder()
            .candle().openPrice(20.0).highPrice(21.0).lowPrice(17.0).closePrice(18.0).add()  // bearish
            .candle().openPrice(18.0).highPrice(20.0).lowPrice(17.0).closePrice(19.0).add()  // bullish
            .candle().openPrice(19.0).highPrice(20.0).lowPrice(17.0).closePrice(18.0).add()  // bearish - mixed pattern
            .candle().openPrice(18.0).highPrice(21.0).lowPrice(17.0).closePrice(20.0).add()  // bullish
            .candle().openPrice(20.0).highPrice(21.0).lowPrice(18.0).closePrice(19.0).add()  // bearish
            .build()

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(marketEvents)
            .withIndicator(ThreeWhiteSoldiersIndicator(numFactory, 3, 0.1))

        // Should not detect pattern in any of these mixed candles
        repeat(5) {
            context.assertNextFalse()
        }
    }
}
