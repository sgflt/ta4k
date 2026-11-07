/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.channels.bollinger

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.api.Indicators
import org.ta4j.core.num.NumFactory

internal class BollingerBandsUpperIndicatorTest {

    private var barCount: Int = 0
    private lateinit var testContext: MarketEventTestContext

    @BeforeEach
    fun setUp() {
        testContext = MarketEventTestContext()
        testContext.withCandlePrices(
            1.0, 2.0, 3.0, 4.0, 3.0, 4.0, 5.0, 4.0, 3.0, 3.0, 4.0, 3.0, 2.0
        )
        barCount = 3
    }

    @ParameterizedTest(name = "Upper bollinger band derived from STDEV of SMA [{index}] {0}")
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun bollingerBandsUpperUsingSMAAndStandardDeviation(numFactory: NumFactory) {
        testContext.withNumFactory(numFactory)

        val closePrice = Indicators.closePrice()
        val sma = closePrice.sma(barCount)
        val bbmSMA = BollingerBandsMiddleIndicator(sma)
        val standardDeviation = closePrice.stddev(barCount)
        val bbu = BollingerBandsUpperIndicator(bbmSMA, standardDeviation)

        testContext.withIndicator(bbu, "bbu")
            .fastForwardUntilStable()
            .onIndicator("bbu")
            .assertCurrent(4.0000)
            .assertNext(5.0000)
            .assertNext(4.4880)
            .assertNext(4.8213)
            .assertNext(6.0000)
            .assertNext(5.4880)
            .assertNext(6.0000)
            .assertNext(4.4880)
            .assertNext(4.4880)
            .assertNext(4.4880)
            .assertNext(5.0000)
    }

    @ParameterizedTest(name = "Upper bollinger band derived from STDEV of SMA with smaller K = 1.5 [{index}] {0}")
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun bollingerBandsUpperUsingSMAAndStandardDeviationWithK(numFactory: NumFactory) {
        testContext.withNumFactory(numFactory)

        val closePrice = Indicators.closePrice()
        val sma = closePrice.sma(barCount)
        val bbmSMA = BollingerBandsMiddleIndicator(sma)
        val standardDeviation = closePrice.stddev(barCount)
        val bbu = BollingerBandsUpperIndicator(bbmSMA, standardDeviation, numFactory.numOf(1.5))

        testContext.withIndicator(bbu, "bbu")
            .fastForwardUntilStable()
            .onIndicator("bbu")
            .assertCurrent(3.5000)
            .assertNext(4.5000)
            .assertNext(4.1993)
            .assertNext(4.5327)
            .assertNext(5.5000)
            .assertNext(5.1994)
            .assertNext(5.5000)
            .assertNext(4.1994)
            .assertNext(4.1994)
            .assertNext(4.1994)
            .assertNext(4.5000)
    }
}