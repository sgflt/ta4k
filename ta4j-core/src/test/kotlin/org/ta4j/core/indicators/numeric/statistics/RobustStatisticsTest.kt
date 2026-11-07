/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.statistics

import java.time.Duration
import java.time.Instant
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.api.Indicators
import org.ta4j.core.events.CandleReceived
import org.ta4j.core.indicators.TimeFrame
import org.ta4j.core.num.NumFactory

internal class RobustStatisticsTest {

    private fun candles(closes: List<Double>, volumes: List<Double>) = closes.indices.map { i ->
        val begin = Instant.EPOCH.plus(Duration.ofDays(i.toLong()))
        CandleReceived(
            timeFrame = TimeFrame.DAY,
            beginTime = begin,
            endTime = begin.plus(Duration.ofDays(1)),
            openPrice = closes[i],
            highPrice = closes[i],
            lowPrice = closes[i],
            closePrice = closes[i],
            volume = volumes[i],
        )
    }

    private val pairs = candles(listOf(1.0, 3.0, 2.0, 5.0, 4.0, 4.0), listOf(10.0, 20.0, 40.0, 30.0, 30.0, 10.0))

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun spearmanUsesAverageRanksForTies(numFactory: NumFactory) {
        val factory = Indicators.extended(numFactory)
        MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(pairs)
            .withIndicator(factory.closePrice().spearman(factory.volume(), 4))
            .assertNextNaN()
            .fastForward(2)
            .assertIsUnStable()
            .assertNext(0.4)
            .assertIsStable()
            .assertNext(-0.3162278)
            .assertNext(-0.5)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun kendallTauBCorrectsForTies(numFactory: NumFactory) {
        val factory = Indicators.extended(numFactory)
        MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(pairs)
            .withIndicator(factory.closePrice().kendallTau(factory.volume(), 4))
            .fastForward(3)
            .assertNext(0.3333333)
            .assertNext(-0.1825742)
            .assertNext(-0.4)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun hurstOfLinearTrendIsOne(numFactory: NumFactory) {
        MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(1.0, 2.0, 3.0, 4.0, 5.0, 6.0)
            .withIndicator(Indicators.extended(numFactory).closePrice().hurst(5))
            .fastForward(4)
            .assertIsUnStable()
            .assertNext(1.0)
            .assertIsStable()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun hurstOfAlternatingSeriesIsZero(numFactory: NumFactory) {
        MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(1.0, 2.0, 1.0, 2.0, 1.0)
            .withIndicator(Indicators.extended(numFactory).closePrice().hurst(5, 2))
            .fastForward(5)
            .assertCurrent(0.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun ewmaVarianceSeedsWithPopulationVariance(numFactory: NumFactory) {
        MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(1.0, 2.0, 3.0, 4.0, 5.0)
            .withIndicator(Indicators.extended(numFactory).closePrice().ewmaVariance(3, 0.5))
            .assertNextNaN()
            .assertNextNaN()
            .assertIsUnStable()
            .assertNext(0.6666667) // seed [1, 2, 3]
            .assertIsStable()
            .assertNext(2.3333333) // 0.5 * 2/3 + 0.5 * (4 - 2)²; mean becomes 3
            .assertNext(3.1666667)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun cusumClipsOutliers(numFactory: NumFactory) {
        MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 9.0, 9.0, 0.0, 20.0)
            .withIndicator(Indicators.extended(numFactory).closePrice().cusum(10, 0.5, 3, 0.5))
            .assertNext(0.0) // d = -0.5, sigma = 0.5
            .assertIsStable()
            .assertNext(0.5)
            .assertNext(1.0)
            .assertNext(2.5) // d = 9.5 clipped to 3 * 0.5
            .assertNext(0.0) // d = -10.5 within 3 * 5
    }
}
