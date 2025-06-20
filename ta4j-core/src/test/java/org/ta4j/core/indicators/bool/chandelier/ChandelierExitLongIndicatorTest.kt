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
package org.ta4j.core.indicators.bool.chandelier

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.mocks.MockMarketEventBuilder
import org.ta4j.core.num.NumFactory

class ChandelierExitLongIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun massIndexUsing3And8BarCounts(numFactory: NumFactory) {
        val testData = MockMarketEventBuilder()
            .candle().lowPrice(99.0).highPrice(101.0).closePrice(100.0).add()
            .candle().lowPrice(104.0).highPrice(106.0).closePrice(105.0).add()
            .candle().lowPrice(109.0).highPrice(111.0).closePrice(110.0).add()
            .candle().lowPrice(107.0).highPrice(109.0).closePrice(108.0).add()
            .candle().lowPrice(92.0).highPrice(96.0).closePrice(90.0).add() // this is out of 3 * atr
            .candle().lowPrice(103.0).highPrice(105.0).closePrice(104.0).add()
            .candle().lowPrice(105.0).highPrice(107.0).closePrice(106.0).add()
            .build()

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(testData)

        val cel = ChandelierExitLongIndicator(numFactory, 5, 2.0)

        context.withIndicator(cel)
            .assertNextFalse()
            .assertNextFalse()
            .assertNextFalse()
            .assertNextFalse()
            .assertNextTrue()
            .assertNextFalse()
            .assertNextFalse()
    }
}