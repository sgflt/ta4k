/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.swing

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

internal class SwingAndTrendTest {

    private val closes = doubleArrayOf(10.0, 11.0, 13.0, 12.0, 10.5, 9.0, 10.0, 12.0, 11.0, 14.0)

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun zigZagConfirmsReversalsOfAtLeastTheAmount(numFactory: NumFactory) {
        val zigZag = Indicators.extended(numFactory).zigZag(2)
        val pivotHigh = zigZag.pivotHigh()
        val pivotLow = zigZag.pivotLow()
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(*closes)
            .withIndicators(zigZag, pivotHigh, pivotLow)

        val highs = mutableListOf<Boolean>()
        val lows = mutableListOf<Boolean>()
        val trends = mutableListOf<ZigZagTrend>()
        repeat(closes.size) {
            context.advance()
            highs += pivotHigh.value
            lows += pivotLow.value
            trends += zigZag.trend
        }
        // low 10 confirmed by 13, high 13 confirmed by 10.5, low 9 confirmed by 12
        assertThat(lows).containsExactly(false, false, true, false, false, false, false, true, false, false)
        assertThat(highs).containsExactly(false, false, false, false, true, false, false, false, false, false)
        assertThat(trends.take(3)).containsExactly(ZigZagTrend.UNDEFINED, ZigZagTrend.UNDEFINED, ZigZagTrend.UP)
        assertThat(trends.last()).isEqualTo(ZigZagTrend.UP)
        assertNumEquals(9.0, zigZag.value)
        assertNumEquals(13.0, zigZag.lastHighPrice)
        assertNumEquals(14.0, zigZag.extremePrice)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun recentZigZagSwings(numFactory: NumFactory) {
        val zigZag = Indicators.extended(numFactory).zigZag(2)
        val recentHigh = zigZag.recentSwingHigh()
        val recentLow = zigZag.recentSwingLow()
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(*closes)
            .withIndicators(recentHigh, recentLow)

        context.fastForward(4)
        assertThat(recentHigh.value.isNaN).isTrue()
        assertThat(recentHigh.isStable).isFalse()
        assertNumEquals(10.0, recentLow.value)
        context.advance()
        assertNumEquals(13.0, recentHigh.value)
        context.fastForward(3)
        assertNumEquals(9.0, recentLow.value)
    }

    private fun trending(step: Double) = (0 until 30).map { i ->
        val close = 50.0 + step * i
        val begin = Instant.EPOCH.plus(Duration.ofDays(i.toLong()))
        CandleReceived(
            timeFrame = TimeFrame.DAY,
            beginTime = begin,
            endTime = begin.plus(Duration.ofDays(1)),
            openPrice = close,
            highPrice = close + 0.5,
            lowPrice = close - 0.5,
            closePrice = close,
            volume = 1.0,
        )
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun upTrendInRisingMarket(numFactory: NumFactory) {
        val factory = Indicators.extended(numFactory)
        val up = factory.upTrend()
        val down = factory.downTrend()
        MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(trending(1.0))
            .withIndicators(up, down)
            .fastForward(30)
        assertThat(up.isStable).isTrue()
        assertThat(up.value).isTrue()
        assertThat(down.value).isFalse()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun downTrendInFallingMarket(numFactory: NumFactory) {
        val factory = Indicators.extended(numFactory)
        val up = factory.upTrend()
        val down = factory.downTrend()
        MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(trending(-1.0))
            .withIndicators(up, down)
            .fastForward(30)
        assertThat(down.value).isTrue()
        assertThat(up.value).isFalse()
    }
}
