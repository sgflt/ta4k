/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2024 Ta4j Organization & respective authors (see AUTHORS)
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
package org.ta4j.core.indicators.numeric.helpers

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.api.Indicators
import org.ta4j.core.num.NumFactory

class GainIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate gain using close price correctly`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(1.0, 2.0, 3.0, 4.0, 3.0, 4.0, 7.0, 4.0, 3.0, 3.0, 5.0, 3.0, 2.0)

        val gainIndicator = Indicators.extended(numFactory).closePrice().gain()

        context
            .withIndicator(gainIndicator)
            .assertNext(0.0)  // First value - no previous, so gain = 0
            .assertNext(1.0)  // 2 - 1 = 1 (gain)
            .assertNext(1.0)  // 3 - 2 = 1 (gain)
            .assertNext(1.0)  // 4 - 3 = 1 (gain)
            .assertNext(0.0)  // 3 - 4 = -1 (loss, so gain = 0)
            .assertNext(1.0)  // 4 - 3 = 1 (gain)
            .assertNext(3.0)  // 7 - 4 = 3 (gain)
            .assertNext(0.0)  // 4 - 7 = -3 (loss, so gain = 0)
            .assertNext(0.0)  // 3 - 4 = -1 (loss, so gain = 0)
            .assertNext(0.0)  // 3 - 3 = 0 (no change, so gain = 0)
            .assertNext(2.0)  // 5 - 3 = 2 (gain)
            .assertNext(0.0)  // 3 - 5 = -2 (loss, so gain = 0)
            .assertNext(0.0)  // 2 - 3 = -1 (loss, so gain = 0)
    }
}
