/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.supportresistance

import java.time.Duration
import java.time.Instant
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.TestUtils.assertNumEquals
import org.ta4j.core.api.Indicators
import org.ta4j.core.events.CandleReceived
import org.ta4j.core.indicators.TimeFrame
import org.ta4j.core.num.NumFactory

internal class SupportResistanceTest {

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

    // peaks 12, 13, 15; troughs 10.2, 9.9
    private val turns = listOf(10.0, 12.0, 10.2, 13.0, 9.9, 12.0, 15.0, 14.0)

    private fun context(numFactory: NumFactory, closes: List<Double>, volumes: List<Double> = closes.map { 1.0 }) =
        MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(candles(closes, volumes))

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun bounceCountGroupsTurnsIntoBuckets(numFactory: NumFactory) {
        val factory = Indicators.extended(numFactory)
        val support = factory.bounceCountSupport(8, 0.5)
        val resistance = factory.bounceCountResistance(8, 0.5)
        context(numFactory, turns).withIndicators(support, resistance).fastForward(turns.size)

        assertThat(support.isStable).isTrue()
        assertNumEquals(10.05, support.value) // 10.2 and 9.9 share a bucket
        assertNumEquals(15.0, resistance.value) // one bounce each: ties go to the higher price
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun bounceCountOnlyCountsTurnsInsideTheWindow(numFactory: NumFactory) {
        val factory = Indicators.extended(numFactory)
        val support = factory.bounceCountSupport(3, 0.5)
        val resistance = factory.bounceCountResistance(3, 0.5)
        context(numFactory, turns).withIndicators(support, resistance).fastForward(turns.size)

        // window 12, 15, 14: one peak, no trough
        assertNumEquals(15.0, resistance.value)
        assertThat(support.value.isNaN).isTrue()
    }

    private val clusterCloses = listOf(10.0, 10.2, 12.0, 12.1, 10.1)
    private val clusterVolumes = listOf(100.0, 50.0, 120.0, 60.0, 30.0)

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun priceClusterPicksHeaviestVolume(numFactory: NumFactory) {
        val factory = Indicators.extended(numFactory)
        val support = factory.priceClusterSupport(5, 0.3)
        val resistance = factory.priceClusterResistance(4, 0.3)
        context(numFactory, clusterCloses, clusterVolumes).withIndicators(support, resistance)
            .fastForward(clusterCloses.size)

        // both clusters weigh 180; the one near 10 has three bars
        assertNumEquals(10.0722222, support.value)
        // without the first bar the cluster near 12 is heavier
        assertNumEquals(12.0333333, resistance.value)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun volumeProfileDensityAndMode(numFactory: NumFactory) {
        val factory = Indicators.extended(numFactory)
        val exact = factory.volumeProfile(3, 0)
        val smooth = factory.volumeProfile(3, 1)
        context(numFactory, listOf(10.0, 10.0, 12.0), listOf(100.0, 50.0, 30.0))
            .withIndicators(exact, smooth)
            .fastForward(3)

        assertNumEquals(30.0, exact.value)
        assertNumEquals(20.0669134, smooth.value)
        assertNumEquals(61.4610711, smooth.densityAt(numFactory.numOf(10)))
        assertNumEquals(10.0, smooth.modePrice)
        assertThat(smooth.isStable).isTrue()
    }
}
