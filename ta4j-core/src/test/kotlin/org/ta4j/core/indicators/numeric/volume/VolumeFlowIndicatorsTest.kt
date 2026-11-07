/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.volume

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

internal class VolumeFlowIndicatorsTest {

    // high, low, close, volume
    private val bars = listOf(
        listOf(12.0, 10.0, 11.0, 1000.0),
        listOf(13.0, 11.0, 12.5, 2000.0),
        listOf(12.5, 10.5, 11.0, 1500.0),
        listOf(14.0, 12.0, 13.5, 3000.0),
        listOf(15.0, 13.0, 14.0, 2500.0),
    )

    private val events = bars.mapIndexed { i, (high, low, close, volume) ->
        val begin = Instant.EPOCH.plus(Duration.ofDays(i.toLong()))
        CandleReceived(
            timeFrame = TimeFrame.DAY,
            beginTime = begin,
            endTime = begin.plus(Duration.ofDays(1)),
            openPrice = close,
            highPrice = high,
            lowPrice = low,
            closePrice = close,
            volume = volume,
        )
    }

    private fun context(numFactory: NumFactory) = MarketEventTestContext()
        .withNumFactory(numFactory)
        .withMarketEvents(events)

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun easeOfMovementAveragesRawValues(numFactory: NumFactory) {
        // raw values: none, 1, -2/3, 1, 0.8
        context(numFactory)
            .withIndicator(Indicators.extended(numFactory).easeOfMovement(2, 1000))
            .assertNextNaN()
            .assertNextNaN()
            .assertIsUnStable()
            .assertNext(0.1666667)
            .assertIsStable()
            .assertNext(0.1666667)
            .assertNext(0.9)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun klingerVolumeForceFollowsTrend(numFactory: NumFactory) {
        context(numFactory)
            .withIndicator(Indicators.extended(numFactory).klingerVolumeForce())
            .assertNextNaN()
            .assertIsUnStable()
            .assertNext(200000.0)
            .assertIsStable()
            .assertNext(-150000.0) // trend flips down
            .assertNext(300000.0) // and up again
            .assertNext(333333.3333) // trend holds: cm = 6
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun klingerOscillatorIsDifferenceOfEmas(numFactory: NumFactory) {
        val kvo = Indicators.extended(numFactory).klingerVolumeOscillator(2, 3)
        val context = context(numFactory).withIndicator(kvo)
        context.fastForward(3)
        assertNumEquals(-58333.3333, kvo.value)
        context.advance()
        assertNumEquals(26388.8889, kvo.value)
        context.advance()
        assertThat(kvo.isStable).isTrue()
    }
}
