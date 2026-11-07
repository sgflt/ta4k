/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.oscillators

import java.time.Instant
import java.time.temporal.ChronoUnit
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.events.CandleReceived
import org.ta4j.core.indicators.TimeFrame
import org.ta4j.core.num.NumFactory

internal class StochasticOscillatorDIndicatorTest {
    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun noMovement(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(
                1.0,
                2.0,
                3.0,
                4.0,
                5.0,
                6.0,
                7.0,
                8.0,
                9.0,
                10.0,
                8.0,
                7.0,
                6.0,
                5.0,
                4.0,
                3.0,
                2.0,
                1.0,
                2.0,
                3.0,
                4.0,
                5.0
            )

        val k = StochasticOscillatorKIndicator(numFactory, 2)
        val d = StochasticOscillatorDIndicator(numFactory, k)

        context.withIndicator(d)

        context.fastForwardUntilStable() // (C - L) / (H - L) => 0/0 => 100.0
            .assertNext(100.0)
            .assertNext(100.0)
            .assertNext(100.0)
    }


    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun movement(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandleDuration(ChronoUnit.MINUTES)
            .withMarketEvents(
                listOf(
                    CandleReceived(
                        timeFrame = TimeFrame.DAY,
                        beginTime = Instant.EPOCH,
                        endTime = Instant.EPOCH.plusSeconds(60),
                        closePrice = 1.0,
                        highPrice = 2.0,
                        lowPrice = 0.0,
                        volume = 0.0,
                        openPrice = 0.0
                    ),
                    CandleReceived(
                        timeFrame = TimeFrame.DAY,
                        beginTime = Instant.EPOCH.plusSeconds(60),
                        endTime = Instant.EPOCH.plusSeconds(120),
                        closePrice = 2.0,
                        highPrice = 2.0,
                        lowPrice = 0.0,
                        volume = 0.0,
                        openPrice = 0.0
                    ),
                    CandleReceived(
                        timeFrame = TimeFrame.DAY,
                        beginTime = Instant.EPOCH.plusSeconds(120),
                        endTime = Instant.EPOCH.plusSeconds(180),
                        closePrice = 3.0,
                        highPrice = 3.0,
                        lowPrice = 0.0,
                        volume = 0.0,
                        openPrice = 0.0
                    ),
                    CandleReceived(
                        timeFrame = TimeFrame.DAY,
                        beginTime = Instant.EPOCH.plusSeconds(180),
                        endTime = Instant.EPOCH.plusSeconds(240),
                        closePrice = 1.0,
                        highPrice = 2.0,
                        lowPrice = 0.0,
                        volume = 0.0,
                        openPrice = 0.0
                    ),
                    CandleReceived(
                        timeFrame = TimeFrame.DAY,
                        beginTime = Instant.EPOCH.plusSeconds(240),
                        endTime = Instant.EPOCH.plusSeconds(300),
                        closePrice = 2.0,
                        highPrice = 12.0,
                        lowPrice = 0.0,
                        volume = 0.0,
                        openPrice = 0.0
                    ),
                    CandleReceived(
                        timeFrame = TimeFrame.DAY,
                        beginTime = Instant.EPOCH.plusSeconds(300),
                        endTime = Instant.EPOCH.plusSeconds(360),
                        closePrice = 2.0,
                        highPrice = 25.0,
                        lowPrice = 1.0,
                        volume = 0.0,
                        openPrice = 0.0
                    )
                )
            )

        val k = StochasticOscillatorKIndicator(numFactory, 2)
        val d = StochasticOscillatorDIndicator(numFactory, k)

        context.withIndicator(d)

        context.fastForwardUntilStable()
            .assertNext(77.7777)
            .assertNext(49.9999)
            .assertNext(19.3333)
    }
}
