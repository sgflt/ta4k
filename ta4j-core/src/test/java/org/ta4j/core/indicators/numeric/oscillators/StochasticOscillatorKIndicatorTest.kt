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
package org.ta4j.core.indicators.numeric.oscillators

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.TestUtils.assertNumEquals
import org.ta4j.core.mocks.MockMarketEventBuilder
import org.ta4j.core.num.NumFactory

class StochasticOscillatorKIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun stochasticOscillatorKParam14(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(MockMarketEventBuilder()
                .candle()
                .openPrice(44.98)
                .closePrice(119.13)
                .highPrice(119.50)
                .lowPrice(116.00)
                .add()
                .candle()
                .openPrice(45.05)
                .closePrice(116.75)
                .highPrice(119.94)
                .lowPrice(116.00)
                .add()
                .candle()
                .openPrice(45.11)
                .closePrice(113.50)
                .highPrice(118.44)
                .lowPrice(111.63)
                .add()
                .candle()
                .openPrice(45.19)
                .closePrice(111.56)
                .highPrice(114.19)
                .lowPrice(110.06)
                .add()
                .candle()
                .openPrice(45.12)
                .closePrice(112.25)
                .highPrice(112.81)
                .lowPrice(109.63)
                .add()
                .candle()
                .openPrice(45.15)
                .closePrice(110.00)
                .highPrice(113.44)
                .lowPrice(109.13)
                .add()
                .candle()
                .openPrice(45.13)
                .closePrice(113.50)
                .highPrice(115.81)
                .lowPrice(110.38)
                .add()
                .candle()
                .openPrice(45.12)
                .closePrice(117.13)
                .highPrice(117.50)
                .lowPrice(114.06)
                .add()
                .candle()
                .openPrice(45.15)
                .closePrice(115.63)
                .highPrice(118.44)
                .lowPrice(114.81)
                .add()
                .candle()
                .openPrice(45.24)
                .closePrice(114.13)
                .highPrice(116.88)
                .lowPrice(113.13)
                .add()
                .candle()
                .openPrice(45.43)
                .closePrice(118.81)
                .highPrice(119.00)
                .lowPrice(116.19)
                .add()
                .candle()
                .openPrice(45.43)
                .closePrice(117.38)
                .highPrice(119.75)
                .lowPrice(117.00)
                .add()
                .candle()
                .openPrice(45.58)
                .closePrice(119.13)
                .highPrice(119.13)
                .lowPrice(116.88)
                .add()
                .candle()
                .openPrice(45.58)
                .closePrice(115.38)
                .highPrice(119.44)
                .lowPrice(114.56)
                .add()
                .build())

        val sof = StochasticOscillatorKIndicator(numFactory, 14)
        context.withIndicator(sof)

        context.fastForward(1)
        assertNumEquals(313.0 / 3.5, sof.value)

        context.fastForward(12)
        assertNumEquals(1000.0 / 10.81, sof.value)

        context.fastForward(1)
        assertNumEquals(57.8168, sof.value)
    }
}