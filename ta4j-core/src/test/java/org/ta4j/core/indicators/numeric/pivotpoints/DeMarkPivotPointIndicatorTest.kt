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
package org.ta4j.core.indicators.numeric.pivotpoints

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.TestUtils.assertNumEquals
import org.ta4j.core.TestUtils.assertStable
import org.ta4j.core.TestUtils.assertUnstable
import org.ta4j.core.mocks.MockMarketEventBuilder
import org.ta4j.core.num.NumFactory

class DeMarkPivotPointIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate pivot point with barbased time level`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)

        val indicator = DeMarkPivotPointIndicator(numFactory, TimeLevel.BARBASED)
        context.withIndicator(indicator)

        // First bar - no calculation yet
        context.withCandlePrices(100.0, 110.0, 105.0)
        context.assertNextNaN()

        // Second bar - calculate pivot from first bar
        // First bar: O=100, H=100, L=100, C=100
        // Close == Open, so X = H + L + 2*C = 100 + 100 + 2*100 = 400
        // Pivot = 400 / 4 = 100
        context.assertNext(100.0)

        // Third bar - calculate pivot from second bar
        // Second bar: O=110, H=110, L=110, C=110
        // Close == Open, so X = H + L + 2*C = 110 + 110 + 2*110 = 440
        // Pivot = 440 / 4 = 110
        context.assertNext(110.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate pivot point when close greater than open`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)

        val indicator = DeMarkPivotPointIndicator(numFactory, TimeLevel.BARBASED)
        context.withIndicator(indicator)

        // First bar with custom OHLC where Close > Open
        val events = MockMarketEventBuilder()
            .candle()
            .openPrice(100.0).highPrice(110.0).lowPrice(95.0).closePrice(105.0)
            .add()
            .candle()
            .openPrice(105.0).highPrice(115.0).lowPrice(100.0).closePrice(112.0)
            .add()
            .build()

        context.withMarketEvents(events)

        context.fastForward(1) // First bar
        assertUnstable(indicator)

        // Second bar - calculate pivot from first bar
        // First bar: O=100, H=110, L=95, C=105
        // Close > Open, so X = 2*H + L + C = 2*110 + 95 + 105 = 220 + 95 + 105 = 420
        // Pivot = 420 / 4 = 105
        context.fastForward(1)
        assertStable(indicator)
        assertNumEquals(105.0, indicator.value)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate pivot point when close less than open`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)

        val indicator = DeMarkPivotPointIndicator(numFactory, TimeLevel.BARBASED)
        context.withIndicator(indicator)

        // First bar with custom OHLC where Close < Open
        val events = MockMarketEventBuilder()
            .candle()
            .openPrice(110.0).highPrice(115.0).lowPrice(100.0).closePrice(105.0)
            .add()
            .candle()
            .openPrice(105.0).highPrice(112.0).lowPrice(98.0).closePrice(102.0)
            .add()
            .build()

        context.withMarketEvents(events)


        // Second bar - calculate pivot from first bar
        // First bar: O=110, H=115, L=100, C=105
        // Close < Open, so X = H + 2*L + C = 115 + 2*100 + 105 = 115 + 200 + 105 = 420
        // Pivot = 420 / 4 = 105
        context.fastForwardUntilStable()
        assertStable(indicator)
        context.assertCurrent(105.0)
    }


    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle precision correctly with small values`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)

        val indicator = DeMarkPivotPointIndicator(numFactory, TimeLevel.BARBASED)
        context.withIndicator(indicator)

        // Test with small decimal values
        val events = MockMarketEventBuilder()
            .candle()
            .openPrice(1.2345).highPrice(1.2567).lowPrice(1.2123).closePrice(1.2456)
            .add()
            .candle()
            .openPrice(1.2456).highPrice(1.2678).lowPrice(1.2234).closePrice(1.2567)
            .add()
            .build()

        context.withMarketEvents(events)

        context.fastForward(2)

        // First bar: O=1.2345, H=1.2567, L=1.2123, C=1.2456
        // Close > Open, so X = 2*1.2567 + 1.2123 + 1.2456 = 2.5134 + 1.2123 + 1.2456 = 4.9713
        // Pivot = 4.9713 / 4 = 1.242825
        assertStable(indicator)
        context.assertCurrent(1.242825)
    }
}
