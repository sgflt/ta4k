/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.bool.candles

import java.time.Duration
import java.time.Instant
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.api.Indicators
import org.ta4j.core.events.CandleReceived
import org.ta4j.core.indicators.TimeFrame
import org.ta4j.core.indicators.bool.BooleanIndicator
import org.ta4j.core.num.NumFactory

/**
 * All scenarios use an average period of 2 and start with quiet candles; the last two
 * (bodies 0.5, ranges 2 and 1) set the averages for the first pattern candle.
 */
internal class CandlePatternsTest {

    private val quiet = listOf(
        listOf(10.0, 10.5, 9.5, 10.0),
        listOf(10.0, 11.0, 9.0, 10.5),
        listOf(10.5, 11.0, 10.0, 10.0),
    )

    // open, high, low, close
    private fun candles(vararg ohlc: List<Double>) = (quiet + ohlc).mapIndexed { i, (open, high, low, close) ->
        val begin = Instant.EPOCH.plus(Duration.ofDays(i.toLong()))
        CandleReceived(
            timeFrame = TimeFrame.DAY,
            beginTime = begin,
            endTime = begin.plus(Duration.ofDays(1)),
            openPrice = open,
            highPrice = high,
            lowPrice = low,
            closePrice = close,
            volume = 1.0
        )
    }

    private fun lastValue(numFactory: NumFactory, indicator: BooleanIndicator, vararg ohlc: List<Double>): Boolean {
        val events = candles(*ohlc)
        MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(events)
            .withIndicator(indicator)
            .fastForward(events.size)
        assertThat(indicator.isStable).isTrue()
        return indicator.value
    }

    private val hammer = listOf(10.0, 10.1, 8.9, 10.1)

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun hammerAtPriorLow(numFactory: NumFactory) {
        assertThat(lastValue(numFactory, Indicators.candlePatterns.hammer(2), hammer)).isTrue()
        assertThat(lastValue(numFactory, CandlePatterns.hangingMan(2), hammer)).isFalse()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun shootingStarOpensAbovePriorClose(numFactory: NumFactory) {
        val star = listOf(10.2, 11.4, 10.2, 10.3)
        assertThat(lastValue(numFactory, CandlePatterns.shootingStar(2), star)).isTrue()
        assertThat(lastValue(numFactory, CandlePatterns.invertedHammer(2), star)).isFalse()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun marubozuHasLongBodyAndNoShadows(numFactory: NumFactory) {
        val bullish = listOf(10.0, 11.05, 9.98, 11.0)
        assertThat(lastValue(numFactory, CandlePatterns.bullishMarubozu(2), bullish)).isTrue()
        assertThat(lastValue(numFactory, CandlePatterns.bearishMarubozu(2), bullish)).isFalse()
        assertThat(lastValue(numFactory, CandlePatterns.bullishMarubozu(2), listOf(10.0, 11.5, 9.98, 11.0))).isFalse()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun morningStar(numFactory: NumFactory) {
        val first = listOf(12.0, 12.2, 9.8, 10.0)
        val star = listOf(9.5, 9.7, 9.2, 9.4)
        assertThat(lastValue(numFactory, CandlePatterns.morningStar(averagePeriod = 2), first, star, listOf(9.6, 11.6, 9.5, 11.5))).isTrue()
        // third candle closes below the middle of the first body
        assertThat(lastValue(numFactory, CandlePatterns.morningStar(averagePeriod = 2), first, star, listOf(9.6, 10.9, 9.5, 10.8))).isFalse()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun darkCloudCoverAndPiercingLine(numFactory: NumFactory) {
        val white = listOf(10.0, 12.1, 9.9, 12.0)
        val cover = listOf(12.5, 12.6, 10.7, 10.8)
        assertThat(lastValue(numFactory, CandlePatterns.darkCloudCover(averagePeriod = 2), white, cover)).isTrue()
        assertThat(lastValue(numFactory, CandlePatterns.piercingLine(averagePeriod = 2), white, cover)).isFalse()

        val black = listOf(12.0, 12.1, 9.9, 10.0)
        val piercing = listOf(9.5, 11.3, 9.4, 11.2)
        assertThat(lastValue(numFactory, CandlePatterns.piercingLine(averagePeriod = 2), black, piercing)).isTrue()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun threeInsideUp(numFactory: NumFactory) {
        val first = listOf(12.0, 12.1, 9.9, 10.0)
        val inside = listOf(10.5, 11.0, 10.4, 10.9)
        assertThat(lastValue(numFactory, CandlePatterns.threeInsideUp(2), first, inside, listOf(10.9, 12.6, 10.8, 12.5))).isTrue()
        assertThat(lastValue(numFactory, CandlePatterns.threeInsideDown(2), first, inside, listOf(10.9, 12.6, 10.8, 12.5))).isFalse()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun bullishKicker(numFactory: NumFactory) {
        val bearishMarubozu = listOf(11.0, 11.02, 9.97, 10.0)
        val bullishMarubozu = listOf(11.5, 12.6, 11.48, 12.55)
        assertThat(lastValue(numFactory, CandlePatterns.bullishKicker(2), bearishMarubozu, bullishMarubozu)).isTrue()
    }
}
