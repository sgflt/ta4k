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
import org.ta4j.core.api.Indicators
import org.ta4j.core.num.NumFactory

class CMOIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate CMO indicator correctly with 9 bar period`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(
                21.27, 22.19, 22.08, 22.47, 22.48, 22.53, 22.23, 21.43, 21.24, 21.29,
                22.15, 22.39, 22.38, 22.61, 23.36, 24.05, 24.75, 24.83, 23.95, 23.63,
                23.82, 23.87, 23.15, 23.19, 23.10, 22.65, 22.48, 22.87, 22.93, 22.91
            )
            .withIndicator(Indicators.extended(numFactory).closePrice().cmo(9))

        // Fast forward to when CMO becomes stable (after 9 bars)
        context.fastForward(5)
            .assertNext(85.1351)
            .assertNext(53.9326)
            .assertNext(6.2016)
            .assertNext(-1.083)
            .assertNext(0.7092)
            .assertNext(-1.4493)
            .assertNext(10.7266)
            .assertNext(-3.5857)
            .assertNext(4.7619)
            .assertNext(24.1983)
            .assertNext(47.644)
    }
}
