/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.volatility

import java.time.Duration
import java.time.Instant
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.TestUtils.assertNumEquals
import org.ta4j.core.api.Indicators
import org.ta4j.core.events.CandleReceived
import org.ta4j.core.indicators.TimeFrame
import org.ta4j.core.indicators.numeric.volatility.SqueezeProIndicator.SqueezeLevel
import org.ta4j.core.num.NumFactory

internal class SqueezeCompressionKalmanTest {

    /** Candles with the given closes and a range of `halfRange` on each side. */
    private fun candles(closes: List<Double>, halfRanges: List<Double>) = closes.indices.map { i ->
        val begin = Instant.EPOCH.plus(Duration.ofDays(i.toLong()))
        CandleReceived(
            timeFrame = TimeFrame.DAY,
            beginTime = begin,
            endTime = begin.plus(Duration.ofDays(1)),
            openPrice = closes[i],
            highPrice = closes[i] + halfRanges[i],
            lowPrice = closes[i] - halfRanges[i],
            closePrice = closes[i],
            volume = 1.0,
        )
    }

    private fun squeezeAfter(numFactory: NumFactory, closes: List<Double>, halfRange: Double): SqueezeProIndicator {
        val squeeze = Indicators.extended(numFactory).squeezePro(5)
        MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(candles(closes, closes.map { halfRange }))
            .withIndicator(squeeze)
            .fastForward(closes.size)
        assertThat(squeeze.isStable).isTrue()
        return squeeze
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun flatClosesWithWideBarsAreInHighSqueeze(numFactory: NumFactory) {
        val squeeze = squeezeAfter(numFactory, List(12) { 10.0 }, 1.0)
        assertThat(squeeze.squeezeLevel).isEqualTo(SqueezeLevel.HIGH)
        assertNumEquals(0.0, squeeze.value)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun steepTrendIsNotInSqueezeAndHasPositiveMomentum(numFactory: NumFactory) {
        // steps of 3 with ranges of 1: bands are wider than 2 * average true range (3.5)
        val squeeze = squeezeAfter(numFactory, List(12) { 10.0 + 3 * it }, 0.5)
        assertThat(squeeze.squeezeLevel).isEqualTo(SqueezeLevel.NONE)
        assertThat(squeeze.value.isPositive).isTrue()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun inSqueezeIsFalseWhileUnstable(numFactory: NumFactory) {
        val inSqueeze = Indicators.extended(numFactory).squeezePro(5).inSqueeze()
        MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(candles(List(3) { 10.0 }, List(3) { 1.0 }))
            .withIndicator(inSqueeze)
            .fastForward(3)
        assertThat(inSqueeze.value).isFalse()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun compressionIsFullWhenEveryWidthIsAtItsLow(numFactory: NumFactory) {
        val closes = List(8) { if (it % 2 == 0) 10.0 else 14.0 } + List(6) { 12.0 }
        val halfRanges = List(8) { 2.0 } + List(6) { 0.1 }
        val compression = Indicators.extended(numFactory).compression(3, 5)
        MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(candles(closes, halfRanges))
            .withIndicator(compression)
            .fastForward(closes.size)
        assertThat(compression.isStable).isTrue()
        assertNumEquals(100.0, compression.value)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun kalmanFilterSteps(numFactory: NumFactory) {
        MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 12.0, 11.0)
            .withIndicator(Indicators.extended(numFactory).closePrice().kalman(1, 1))
            .assertNext(10.0)
            .assertIsStable()
            .assertNext(11.25) // gain 5/8
            .assertNext(11.0952381) // gain 13/21
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun kinematicKalmanFirstUpdate(numFactory: NumFactory) {
        val filter = Indicators.extended(numFactory).closePrice().kinematicKalman(1, 1)
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(0.0, 2.0)
            .withIndicator(filter)
        context.advance()
        assertThat(filter.isStable).isFalse()
        context.advance() // position gain 11/14, velocity gain 3/7, innovation 2
        assertNumEquals(1.5714286, filter.value)
        assertNumEquals(0.8571429, filter.velocity)
        assertThat(filter.isStable).isTrue()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun kinematicKalmanLearnsTrendVelocity(numFactory: NumFactory) {
        val closes = DoubleArray(30) { 2.0 * it }
        val filter = Indicators.extended(numFactory).closePrice().kinematicKalman()
        MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(*closes)
            .withIndicator(filter)
            .fastForward(closes.size)
        assertThat(filter.velocity.doubleValue()).isCloseTo(2.0, Offset.offset(0.05))
        assertThat(filter.value.doubleValue()).isCloseTo(closes.last(), Offset.offset(0.05))
    }
}
