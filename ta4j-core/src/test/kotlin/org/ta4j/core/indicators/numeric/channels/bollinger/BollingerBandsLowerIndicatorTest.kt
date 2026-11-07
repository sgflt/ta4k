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

internal class BollingerBandsLowerIndicatorTest {

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

    @ParameterizedTest(name = "Lower bollinger band derived from STDEV of SMA [{index}] {0}")
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun bollingerBandsLowerUsingSMAAndStandardDeviation(numFactory: NumFactory) {
        testContext.withNumFactory(numFactory)

        val closePrice = Indicators.closePrice()
        val sma = closePrice.sma(barCount)
        val bbmSMA = BollingerBandsMiddleIndicator(sma)
        val standardDeviation = closePrice.stddev(barCount)
        val bblSMA = BollingerBandsLowerIndicator(bbmSMA, standardDeviation)

        testContext.withIndicator(bblSMA, "bblSMA")
            .fastForwardUntilStable()
            .onIndicator("bblSMA")
            .assertCurrent(0.0000)
            .assertNext(1.0000)
            .assertNext(2.1786)
            .assertNext(2.5119)
            .assertNext(2.0000)
            .assertNext(3.1786)
            .assertNext(2.0000)
            .assertNext(2.1786)
            .assertNext(2.1786)
            .assertNext(2.1786)
            .assertNext(1.0000)
    }

    @ParameterizedTest(name = "Lower bollinger band derived from STDEV of SMA with smaller K = 1.5 [{index}] {0}")
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun bollingerBandsLowerUsingSMAAndStandardDeviationWithK(numFactory: NumFactory) {
        testContext.withNumFactory(numFactory)

        val closePrice = Indicators.closePrice()
        val sma = closePrice.sma(barCount)
        val bbmSMA = BollingerBandsMiddleIndicator(sma)
        val standardDeviation = closePrice.stddev(barCount)
        val bblSMA = BollingerBandsLowerIndicator(bbmSMA, standardDeviation, numFactory.numOf(1.5))

        testContext.withIndicator(bblSMA, "bblSMA")
            .fastForwardUntilStable()
            .onIndicator("bblSMA")
            .assertCurrent(0.5000)
            .assertNext(1.5000)
            .assertNext(2.4673)
            .assertNext(2.8006)
            .assertNext(2.5000)
            .assertNext(3.4673)
            .assertNext(2.5000)
            .assertNext(2.4673)
            .assertNext(2.4673)
            .assertNext(2.4673)
            .assertNext(1.5000)
    }
}