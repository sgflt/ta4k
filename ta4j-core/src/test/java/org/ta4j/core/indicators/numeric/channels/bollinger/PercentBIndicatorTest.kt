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
package org.ta4j.core.indicators.numeric.channels.bollinger

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.api.Indicators
import org.ta4j.core.num.NumFactory

internal class PercentBIndicatorTest {

    private lateinit var testContext: MarketEventTestContext

    @BeforeEach
    fun setUp() {
        testContext = MarketEventTestContext()
        testContext.withCandlePrices(10.0, 12.0, 15.0, 14.0, 17.0, 20.0, 21.0, 20.0, 20.0, 19.0, 20.0, 17.0, 12.0, 12.0, 9.0, 8.0, 9.0, 10.0, 9.0, 10.0)
    }

    @ParameterizedTest(name = "%BI [{index}] {0}")
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun percentBUsingSMAAndStandardDeviation(numFactory: NumFactory) {
        testContext.withNumFactory(numFactory)

        val pcb = PercentBIndicator(Indicators.closePrice(), 5, 2.0)
        testContext.withIndicator(pcb)
            .fastForwardUntilStable()
            .assertCurrent(0.8146)
            .assertNext(0.8607)
            .assertNext(0.7951)
            .assertNext(0.6388)
            .assertNext(0.5659)
            .assertNext(0.1464)
            .assertNext(0.5000)
            .assertNext(0.0782)
            .assertNext(0.0835)
            .assertNext(0.2374)
            .assertNext(0.2169)
            .assertNext(0.2434)
            .assertNext(0.3664)
            .assertNext(0.5659)
            .assertNext(0.5000)
            .assertNext(0.7391)
    }
}