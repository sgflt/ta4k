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

import java.time.Duration
import java.time.Instant
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.api.Indicators.closePrice
import org.ta4j.core.events.CandleReceived
import org.ta4j.core.indicators.TimeFrame
import org.ta4j.core.indicators.numeric.candles.VolumeIndicator
import org.ta4j.core.num.NumFactory

class CovarianceIndicatorTest {

    private fun createCandleEvents(data: List<Pair<Double, Double>>): List<CandleReceived> {
        return data.mapIndexed { index, (close, volume) ->
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = Instant.EPOCH.plus(Duration.ofDays(index.toLong())),
                endTime = Instant.EPOCH.plus(Duration.ofDays(index + 1L)),
                closePrice = close,
                volume = volume
            )
        }
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun usingBarCount5UsingClosePriceAndVolume(numFactory: NumFactory) {
        val testData = listOf(
            6.0 to 100.0, 7.0 to 105.0, 9.0 to 130.0, 12.0 to 160.0, 11.0 to 150.0,
            10.0 to 130.0, 11.0 to 95.0, 13.0 to 120.0, 15.0 to 180.0, 12.0 to 160.0,
            8.0 to 150.0, 4.0 to 200.0, 3.0 to 150.0, 4.0 to 85.0, 3.0 to 70.0,
            5.0 to 90.0, 8.0 to 100.0, 9.0 to 95.0, 11.0 to 110.0, 10.0 to 95.0
        )

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(createCandleEvents(testData))

        val covar = CovarianceIndicator(closePrice(), VolumeIndicator(numFactory), 5)

        context.withIndicator(covar)
            .fastForwardUntilStable()
            .assertCurrent(54.0)
            .assertNext(32.0)
            .assertNext(7.2)
            .assertNext(1.6)
            .assertNext(31.0)
            .assertNext(33.6)
            .assertNext(21.2)
            .assertNext(-48.8)
            .assertNext(2.8)
            .assertNext(18.2)
            .assertNext(23.6)
            .assertNext(-2.2)
            .assertNext(-5.4)
            .assertNext(20.6)
            .assertNext(35.4)
            .assertNext(10.2)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun firstValueShouldBeNaN(numFactory: NumFactory) {
        val testData = listOf(6.0 to 100.0, 7.0 to 105.0, 9.0 to 130.0)

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(createCandleEvents(testData))

        val covar = CovarianceIndicator(closePrice(), VolumeIndicator(numFactory), 5)

        context.withIndicator(covar)
            .assertNext(0.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun shouldBeZeroWhenBarCountIs1(numFactory: NumFactory) {
        val testData = listOf(
            6.0 to 100.0, 7.0 to 105.0, 9.0 to 130.0, 12.0 to 160.0, 11.0 to 150.0,
            10.0 to 130.0, 11.0 to 95.0, 13.0 to 120.0, 15.0 to 180.0, 12.0 to 160.0
        )

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(createCandleEvents(testData))

        val covar = CovarianceIndicator(closePrice(), VolumeIndicator(numFactory), 1)

        context.withIndicator(covar)
            .fastForward(4)
            .assertCurrent(0.0)
            .fastForward(5)
            .assertCurrent(0.0)
    }
}
