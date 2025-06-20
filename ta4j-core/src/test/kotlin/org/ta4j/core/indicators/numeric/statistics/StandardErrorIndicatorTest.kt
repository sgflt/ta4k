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
import org.ta4j.core.num.NumFactory

class StandardErrorIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun usingBarCount5UsingClosePrice(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 20.0, 30.0, 40.0, 50.0, 40.0, 40.0, 50.0, 40.0, 30.0, 20.0, 10.0)

        val se = StandardErrorIndicator(closePrice(), 5)

        context.withIndicator(se, "se")
            .fastForwardUntilStable()

        context.onIndicator("se")
            .assertNext(5.0990) // Index 5: Actual value: 5.0990195135927845
            .assertNext(3.1623) // Index 6: Actual value: 3.162277660168379
            .assertNext(2.4495) // Index 7: Actual value: 2.4494897427831783
            .assertNext(2.4495) // Index 8: Actual value: 2.4494897427831783
            .assertNext(3.1623) // Index 9: Actual value: 3.162277660168379
            .assertNext(5.0990) // Index 10: Actual value: 5.0990195135927845
            .assertNext(7.0711) // Index 11: Actual value: 7.071067811865475
    }
}
