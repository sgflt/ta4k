/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.volume

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

internal class ForceIndexIndicatorTest {

    // bullish scenario from upstream ta4j's test: close, volume
    private val closes = listOf(102, 104, 107, 109, 112, 114, 117, 119, 121, 124, 126, 129)
    private val volumes = listOf(1000, 1100, 1200, 1300, 1400, 1500, 1600, 1700, 1800, 1900, 2000, 2100)

    private fun candles() = closes.indices.map { i ->
        val begin = Instant.EPOCH.plus(Duration.ofDays(i.toLong()))
        CandleReceived(
            timeFrame = TimeFrame.DAY,
            beginTime = begin,
            endTime = begin.plus(Duration.ofDays(1)),
            openPrice = closes[i].toDouble(),
            highPrice = closes[i].toDouble(),
            lowPrice = closes[i].toDouble(),
            closePrice = closes[i].toDouble(),
            volume = volumes[i].toDouble()
        )
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun smoothsPriceChangeTimesVolume(numFactory: NumFactory) {
        MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(candles())
            .withIndicator(Indicators.extended(numFactory).forceIndex(3))
            .assertNextNaN()
            .assertIsUnStable()
            .assertNext(2200.0)
            .assertNext(2900.0)
            .assertNext(2750.0)
            .assertIsStable()
            .assertNext(3475.0)
            .assertNext(3237.5)
            .assertNext(4018.75)
            .assertNext(3709.375)
            .assertNext(3654.6875)
            .assertNext(4677.34375)
            .assertNext(4338.671875)
            .assertNext(5319.3359375)
    }

    @Test
    fun rejectsNonPositiveBarCount() {
        assertThatThrownBy { Indicators.forceIndex(0) }.isInstanceOf(IllegalArgumentException::class.java)
    }
}
