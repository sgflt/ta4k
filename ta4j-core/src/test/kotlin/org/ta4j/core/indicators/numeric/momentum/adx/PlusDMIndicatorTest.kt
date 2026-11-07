/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.momentum.adx

import java.time.Duration
import java.time.Instant
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.events.CandleReceived
import org.ta4j.core.indicators.TimeFrame
import org.ta4j.core.num.NumFactory

class PlusDMIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate zero directional movement`(numFactory: NumFactory) {
        val events = listOf(
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = Instant.EPOCH,
                endTime = Instant.EPOCH.plus(Duration.ofDays(1)),
                openPrice = 0.0,
                highPrice = 10.0,
                lowPrice = 2.0,
                closePrice = 0.0,
                volume = 0.0
            ),
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = Instant.EPOCH.plus(Duration.ofDays(1)),
                endTime = Instant.EPOCH.plus(Duration.ofDays(2)),
                openPrice = 0.0,
                highPrice = 6.0,
                lowPrice = 6.0,
                closePrice = 0.0,
                volume = 0.0
            )
        )

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(events)
            .withIndicator(PlusDMIndicator(numFactory))

        context.fastForward(2)

        context.assertCurrent(0.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate zero directional movement when minus DM is greater`(numFactory: NumFactory) {
        val events = listOf(
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = Instant.EPOCH,
                endTime = Instant.EPOCH.plus(Duration.ofDays(1)),
                openPrice = 0.0,
                highPrice = 6.0,
                lowPrice = 12.0,
                closePrice = 0.0,
                volume = 0.0
            ),
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = Instant.EPOCH.plus(Duration.ofDays(1)),
                endTime = Instant.EPOCH.plus(Duration.ofDays(2)),
                openPrice = 0.0,
                highPrice = 12.0,
                lowPrice = 6.0,
                closePrice = 0.0,
                volume = 0.0
            )
        )

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(events)
            .withIndicator(PlusDMIndicator(numFactory))

        context.fastForward(2)

        context.assertCurrent(0.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate zero directional movement when both movements are equal`(numFactory: NumFactory) {
        val events = listOf(
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = Instant.EPOCH,
                endTime = Instant.EPOCH.plus(Duration.ofDays(1)),
                openPrice = 0.0,
                highPrice = 6.0,
                lowPrice = 20.0,
                closePrice = 0.0,
                volume = 0.0
            ),
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = Instant.EPOCH.plus(Duration.ofDays(1)),
                endTime = Instant.EPOCH.plus(Duration.ofDays(2)),
                openPrice = 0.0,
                highPrice = 12.0,
                lowPrice = 4.0,
                closePrice = 0.0,
                volume = 0.0
            )
        )

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(events)
            .withIndicator(PlusDMIndicator(numFactory))

        context.fastForward(2)

        context.assertCurrent(0.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate positive directional movement`(numFactory: NumFactory) {
        val events = listOf(
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = Instant.EPOCH,
                endTime = Instant.EPOCH.plus(Duration.ofDays(1)),
                openPrice = 0.0,
                highPrice = 6.0,
                lowPrice = 6.0,
                closePrice = 0.0,
                volume = 0.0
            ),
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = Instant.EPOCH.plus(Duration.ofDays(1)),
                endTime = Instant.EPOCH.plus(Duration.ofDays(2)),
                openPrice = 0.0,
                highPrice = 12.0,
                lowPrice = 4.0,
                closePrice = 0.0,
                volume = 0.0
            )
        )

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(events)
            .withIndicator(PlusDMIndicator(numFactory))

        context.fastForward(2)

        context.assertCurrent(6.0)
    }
}
