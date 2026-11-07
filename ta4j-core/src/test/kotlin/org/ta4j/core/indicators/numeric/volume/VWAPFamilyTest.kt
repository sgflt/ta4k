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
import org.ta4j.core.api.series.Bar
import org.ta4j.core.events.CandleReceived
import org.ta4j.core.indicators.TimeFrame
import org.ta4j.core.indicators.bool.BooleanIndicator
import org.ta4j.core.num.NumFactory

/**
 * Flat candles (typical price = close) with prices 10, 12, 11, 13 and volumes 1, 3, 2, 2.
 */
internal class VWAPFamilyTest {

    private val prices = listOf(10.0, 12.0, 11.0, 13.0)
    private val volumes = listOf(1.0, 3.0, 2.0, 2.0)

    private fun day(i: Int) = Instant.EPOCH.plus(Duration.ofDays(i.toLong()))

    private val events = prices.indices.map { i ->
        CandleReceived(
            timeFrame = TimeFrame.DAY,
            beginTime = day(i),
            endTime = day(i + 1),
            openPrice = prices[i],
            highPrice = prices[i],
            lowPrice = prices[i],
            closePrice = prices[i],
            volume = volumes[i],
        )
    }

    private fun context(numFactory: NumFactory) = MarketEventTestContext()
        .withNumFactory(numFactory)
        .withMarketEvents(events)

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun rollingVwapAndItsDispersion(numFactory: NumFactory) {
        val vwap = Indicators.extended(numFactory).vwap(3)
        val std = vwap.standardDeviation()
        val context = context(numFactory).withIndicators(vwap, std)

        context.advance()
        assertNumEquals(10.0, vwap.value)
        assertNumEquals(0.0, std.value)
        context.advance()
        assertNumEquals(11.5, vwap.value)
        assertNumEquals(0.8660254, std.value)
        context.advance()
        assertNumEquals(11.333333, vwap.value)
        assertNumEquals(0.7453560, std.value)
        assertThat(vwap.isStable).isTrue()
        context.advance() // window 12, 11, 13 with volumes 3, 2, 2
        assertNumEquals(12.0, vwap.value)
        assertNumEquals(0.7559289, std.value)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun bandsDeviationAndZScore(numFactory: NumFactory) {
        val vwap = Indicators.extended(numFactory).vwap(3)
        val upper = vwap.upperBand(2)
        val lower = vwap.lowerBand(2)
        val deviation = vwap.deviation()
        val zScore = vwap.zScore()
        context(numFactory).withIndicators(upper, lower, deviation, zScore).fastForward(4)

        assertNumEquals(13.5118579, upper.value)
        assertNumEquals(10.4881421, lower.value)
        assertNumEquals(1.0, deviation.value)
        assertNumEquals(1.3228757, zScore.value)
        assertThat(zScore.isStable).isTrue()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun anchoredAtTimeIsNaNUntilAnchor(numFactory: NumFactory) {
        context(numFactory)
            .withIndicator(Indicators.extended(numFactory).anchoredVwap(day(1)))
            .assertNextNaN()
            .assertIsUnStable()
            .assertNext(12.0)
            .assertIsStable()
            .assertNext(11.6)
            .assertNext(12.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun anchorSignalRestartsAccumulation(numFactory: NumFactory) {
        val sessionStart = object : BooleanIndicator() {
            override fun updateState(bar: Bar) {
                value = bar.beginTime == day(0) || bar.beginTime == day(2)
            }

            override val lag = 0
            override val isStable = true
        }
        context(numFactory)
            .withIndicator(Indicators.extended(numFactory).anchoredVwap(sessionStart))
            .assertNext(10.0)
            .assertNext(11.5)
            .assertNext(11.0) // restarted
            .assertNext(12.0)
    }
}
