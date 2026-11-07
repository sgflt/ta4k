/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.oscillators

import java.time.Duration
import java.time.Instant
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.api.Indicators
import org.ta4j.core.events.CandleReceived
import org.ta4j.core.indicators.TimeFrame
import org.ta4j.core.num.NumFactory

internal class UltimateOscillatorAndVortexTest {

    // open, high, low, close
    private val ohlc = listOf(
        listOf(10.0, 12.0, 9.0, 11.0),
        listOf(11.0, 13.0, 10.0, 12.0),
        listOf(12.0, 14.0, 11.0, 13.0),
        listOf(13.0, 13.0, 10.0, 11.0),
        listOf(11.0, 12.0, 8.0, 9.0),
        listOf(9.0, 11.0, 8.0, 10.0),
        listOf(10.0, 13.0, 9.0, 12.0),
        listOf(12.0, 15.0, 11.0, 14.0),
    )

    private fun candles() = ohlc.mapIndexed { i, (open, high, low, close) ->
        val begin = Instant.EPOCH.plus(Duration.ofDays(i.toLong()))
        CandleReceived(
            timeFrame = TimeFrame.DAY,
            beginTime = begin,
            endTime = begin.plus(Duration.ofDays(1)),
            openPrice = open,
            highPrice = high,
            lowPrice = low,
            closePrice = close,
            volume = 100.0
        )
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun ultimateOscillatorWeighsThreeAverages(numFactory: NumFactory) {
        MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(candles())
            .withIndicator(Indicators.extended(numFactory).ultimateOscillator(2, 3, 4))
            .assertNext(66.66666666666667)
            .assertNext(66.66666666666667)
            .assertNext(66.66666666666667)
            .assertIsUnStable()
            .assertNext(52.77777777777778)
            .assertIsStable()
            .assertNext(34.34850863422292)
            .assertNext(42.51177394034537)
            .assertNext(63.54359925788497)
            .assertNext(72.20779220779221)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun vortexLinesAndOscillator(numFactory: NumFactory) {
        val vortex = Indicators.extended(numFactory).vortex(3)
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(candles())
            .withIndicator(vortex.positive, "plus")
            .withIndicator(vortex.negative, "minus")
            .withIndicator(vortex.oscillator, "osc")

        context.fastForward(3)
        context.onIndicator("plus").assertCurrentNaN()
        context.fastForward(1)
        context.onIndicator("plus").assertCurrent(1.1111111111111112)
        context.onIndicator("minus").assertCurrent(0.8888888888888888)
        context.onIndicator("osc").assertCurrent(0.2222222222222222)
        context.fastForward(1)
        context.onIndicator("plus").assertCurrent(0.8)
        context.onIndicator("minus").assertCurrent(1.1)
        context.onIndicator("osc").assertCurrent(-0.3)
        context.fastForward(3)
        context.onIndicator("plus").assertCurrent(1.2727272727272727)
        context.onIndicator("minus").assertCurrent(0.7272727272727273)
        context.onIndicator("osc").assertCurrent(0.5454545454545454)
    }

    @Test
    fun rejectsInvalidPeriods() {
        assertThatThrownBy { Indicators.ultimateOscillator(7, 7, 28) }.isInstanceOf(IllegalArgumentException::class.java)
        assertThatThrownBy { Indicators.vortex(1) }.isInstanceOf(IllegalArgumentException::class.java)
    }
}
