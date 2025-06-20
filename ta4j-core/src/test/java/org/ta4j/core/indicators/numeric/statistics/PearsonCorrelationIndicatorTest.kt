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
package org.ta4j.core.indicators.numeric.statistics

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.api.Indicators.closePrice
import org.ta4j.core.api.Indicators.volume
import org.ta4j.core.mocks.MockMarketEventBuilder
import org.ta4j.core.num.NumFactory

class PearsonCorrelationIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun test(numFactory: NumFactory) {
        // Create market events with exact close prices and volumes from original test
        val marketEvents = MockMarketEventBuilder()
            .candle().closePrice(6.0).volume(100.0).add()
            .candle().closePrice(7.0).volume(105.0).add()
            .candle().closePrice(9.0).volume(130.0).add()
            .candle().closePrice(12.0).volume(160.0).add()
            .candle().closePrice(11.0).volume(150.0).add()
            .candle().closePrice(10.0).volume(130.0).add()
            .candle().closePrice(11.0).volume(95.0).add()
            .candle().closePrice(13.0).volume(120.0).add()
            .candle().closePrice(15.0).volume(180.0).add()
            .candle().closePrice(12.0).volume(160.0).add()
            .candle().closePrice(8.0).volume(150.0).add()
            .candle().closePrice(4.0).volume(200.0).add()
            .candle().closePrice(3.0).volume(150.0).add()
            .candle().closePrice(4.0).volume(85.0).add()
            .candle().closePrice(3.0).volume(70.0).add()
            .candle().closePrice(5.0).volume(90.0).add()
            .candle().closePrice(8.0).volume(100.0).add()
            .candle().closePrice(9.0).volume(95.0).add()
            .candle().closePrice(11.0).volume(110.0).add()
            .candle().closePrice(10.0).volume(95.0).add()
            .build()

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(marketEvents)

        val close = closePrice()
        val volumeRunningTotal = volume().runningTotal(2)
        val coef = PearsonCorrelationIndicator(close, volumeRunningTotal, 5)

        context.withIndicator(coef)
            .fastForward(2)
            .assertNext(0.9640797490298872)
            .assertNext(0.9666189661412724)
            .assertNext(0.9219)
            .assertNext(0.9205)
            .assertNext(0.4565)
            .assertNext(-0.4622)
            .assertNext(0.05747)
            .assertNext(0.1442)
            .assertNext(-0.1263)
            .assertNext(-0.5345)
            .assertNext(-0.7275)
            .assertNext(0.1676)
            .assertNext(0.2506)
            .assertNext(-0.2938)
            .assertNext(-0.3586)
            .assertNext(0.1713)
            .assertNext(0.9841)
            .assertNext(0.9799)
    }
}