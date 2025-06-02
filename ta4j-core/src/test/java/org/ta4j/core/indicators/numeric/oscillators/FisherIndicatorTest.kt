/**
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
package org.ta4j.core.indicators.numeric.oscillators

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.mocks.MockMarketEventBuilder
import org.ta4j.core.num.NumFactory

class FisherIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `fisher indicator calculation matches expected values`(numFactory: NumFactory) {
        val events = MockMarketEventBuilder()
            .candle().openPrice(44.98).closePrice(45.05).highPrice(45.17).lowPrice(44.96).add()
            .candle().openPrice(45.05).closePrice(45.10).highPrice(45.15).lowPrice(44.99).add()
            .candle().openPrice(45.11).closePrice(45.19).highPrice(45.32).lowPrice(45.11).add()
            .candle().openPrice(45.19).closePrice(45.14).highPrice(45.25).lowPrice(45.04).add()
            .candle().openPrice(45.12).closePrice(45.15).highPrice(45.20).lowPrice(45.10).add()
            .candle().openPrice(45.15).closePrice(45.14).highPrice(45.20).lowPrice(45.10).add()
            .candle().openPrice(45.13).closePrice(45.10).highPrice(45.16).lowPrice(45.07).add()
            .candle().openPrice(45.12).closePrice(45.15).highPrice(45.22).lowPrice(45.10).add()
            .candle().openPrice(45.15).closePrice(45.22).highPrice(45.27).lowPrice(45.14).add()
            .candle().openPrice(45.24).closePrice(45.43).highPrice(45.45).lowPrice(45.20).add()
            .candle().openPrice(45.43).closePrice(45.44).highPrice(45.50).lowPrice(45.39).add()
            .candle().openPrice(45.43).closePrice(45.55).highPrice(45.60).lowPrice(45.35).add()
            .candle().openPrice(45.58).closePrice(45.55).highPrice(45.61).lowPrice(45.39).add()
            .candle().openPrice(45.45).closePrice(45.01).highPrice(45.55).lowPrice(44.80).add()
            .candle().openPrice(45.03).closePrice(44.23).highPrice(45.04).lowPrice(44.17).add()
            .candle().openPrice(44.23).closePrice(43.95).highPrice(44.29).lowPrice(43.81).add()
            .candle().openPrice(43.91).closePrice(43.08).highPrice(43.99).lowPrice(43.08).add()
            .candle().openPrice(43.07).closePrice(43.55).highPrice(43.65).lowPrice(43.06).add()
            .candle().openPrice(43.56).closePrice(43.95).highPrice(43.99).lowPrice(43.53).add()
            .candle().openPrice(43.93).closePrice(44.47).highPrice(44.58).lowPrice(43.93).add()
            .build()

        val indicator = FisherIndicator(numFactory)
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(events)
            .withIndicator(indicator)

        context.fastForwardUntilStable()
        context.assertNext(0.6448642008177138)
        context.assertNext(0.8361770425706673)
        context.assertNext(0.9936697984965788)
        context.assertNext(0.8324807235379169)
        context.assertNext(0.5026313552592737)
        context.assertNext(0.06492516204615063)
    }
}
