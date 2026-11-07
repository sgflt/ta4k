/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.oscillators

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.TestUtils.assertNumEquals
import org.ta4j.core.api.Indicators
import org.ta4j.core.indicators.numeric.statistics.PercentRankIndicator
import org.ta4j.core.num.NumFactory

internal class MomentumOscillatorsTest {

    private val wave = doubleArrayOf(
        10.0, 11.0, 12.5, 12.0, 13.5, 13.0, 11.5, 10.5, 11.0, 12.0,
        13.0, 14.5, 14.0, 12.5, 11.0, 10.0, 10.5, 12.0, 13.5, 15.0,
    )

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun percentRankOfPreviousWindow(numFactory: NumFactory) {
        MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(1.0, 3.0, 2.0, 5.0, 4.0)
            .withIndicator(Indicators.extended(numFactory).closePrice().percentRank(3))
            .assertNextNaN()
            .assertNext(100.0)
            .assertNext(50.0)
            .assertIsUnStable()
            .assertNext(100.0)
            .assertIsStable()
            .assertNext(66.66666666666667)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun stochasticOfSeriesKeepsValueWhenFlat(numFactory: NumFactory) {
        MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 12.0, 11.0, 11.0, 11.0, 11.0)
            .withIndicator(Indicators.extended(numFactory).closePrice().stochastic(3))
            .assertNext(0.0) // flat start
            .assertNext(100.0)
            .assertNext(50.0)
            .assertNext(0.0) // window 12, 11, 11
            .assertNext(0.0) // window 11, 11, 11 is flat: keeps 0
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun kriIsPercentDistanceFromSma(numFactory: NumFactory) {
        MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 12.0, 14.0)
            .withIndicator(Indicators.extended(numFactory).closePrice().kri(3))
            .fastForward(3)
            .assertCurrent(16.666666666666668) // (14 - 12) / 12 * 100
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun connorsRsiAveragesItsThreeParts(numFactory: NumFactory) {
        val close = Indicators.extended(numFactory).closePrice()
        val connors = close.connorsRsi(3, 2, 5)
        val priceRsi = close.rsi(3)
        val streakRsi = close.streak().rsi(2)
        val rank = PercentRankIndicator(close.difference(), 5)
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(*wave)
            .withIndicators(connors, priceRsi, streakRsi, rank)

        context.fastForward(wave.size)
        assertThat(connors.isStable).isTrue()
        val expected = (priceRsi.value + streakRsi.value + rank.value) / numFactory.numOf(3)
        assertNumEquals(expected, connors.value)
        assertThat(connors.value.doubleValue()).isBetween(0.0, 100.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun schaffTrendCycleStaysWithinRange(numFactory: NumFactory) {
        val stc = Indicators.extended(numFactory).closePrice().schaffTrendCycle(3, 6, 4, 2)
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(*wave)
            .withIndicator(stc)
        repeat(wave.size) {
            context.advance()
            assertThat(stc.value.doubleValue()).isBetween(0.0, 100.0)
        }
        assertThat(stc.isStable).isTrue()
        // closes rose for the last four bars: the cycle points up
        assertThat(stc.value.doubleValue()).isGreaterThan(50.0)
    }
}
