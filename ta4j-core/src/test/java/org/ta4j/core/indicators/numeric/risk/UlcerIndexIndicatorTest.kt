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
package org.ta4j.core.indicators.numeric.risk

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.api.Indicators
import org.ta4j.core.num.NumFactory

class UlcerIndexIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate ulcer index using 14 period with IBM data`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(
                194.75, 195.00, 195.10, 194.46, 190.60, 188.86, 185.47, 184.46, 182.31, 185.22,
                184.00, 182.87, 187.45, 194.51, 191.63, 190.02, 189.53, 190.27, 193.13, 195.55,
                195.84, 195.15, 194.35, 193.62, 197.68, 197.91, 199.08, 199.03, 198.42, 199.29,
                199.01, 198.29, 198.40, 200.84, 201.22, 200.50, 198.65, 197.25, 195.70, 197.77,
                195.69, 194.87, 195.08
            )
            .withIndicator(
                Indicators.extended(numFactory).closePrice().ulcer(14)
            )

        context.assertNext(0.0)
            .fastForward(25)
            .assertNext(1.2340096463740846)
            .assertNext(0.560553282860879)
            .assertNext(0.39324888828140886)
            .assertNext(0.38716275079310825)
            .assertNext(0.3889794194862251)
            .assertNext(0.4114481689096125)
            .assertNext(0.42841008722557894)
            .assertNext(0.42841008722557894)
            .assertNext(0.3121617589229034)
            .assertNext(0.2464924497436544)
            .assertNext(0.4089008481549337)
            .assertNext(0.667264629592715)
            .assertNext(0.9913518177402276)
            .assertNext(1.0921325741850083)
            .assertNext(1.3156949266800984)
            .assertNext(1.5606676136361992)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should be stable after sufficient data points`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(
                100.0, 101.0, 102.0, 103.0, 104.0, 105.0, 106.0, 107.0, 108.0, 109.0,
                110.0, 111.0, 112.0, 113.0, 114.0, 115.0
            )
            .withIndicator(Indicators.extended(numFactory).closePrice().ulcer(14))
            .fastForwardUntilStable()

        context.assertIsStable()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle zero values gracefully`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0)
            .withIndicator(Indicators.extended(numFactory).closePrice().ulcer(5))

        // When prices don't change, ulcer index should be 0 (no drawdown)
        context.fastForward(7)
        context.assertCurrent(0.0)
    }
}
