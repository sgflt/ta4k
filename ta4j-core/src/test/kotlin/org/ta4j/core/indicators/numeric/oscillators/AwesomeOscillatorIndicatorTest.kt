/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.oscillators

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.api.Indicators.medianPrice
import org.ta4j.core.mocks.MockMarketEventBuilder
import org.ta4j.core.num.NumFactory

class AwesomeOscillatorIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateWithSma2AndSma3(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(
                MockMarketEventBuilder()
                    .candle().openPrice(0.0).closePrice(0.0).highPrice(16.0).lowPrice(8.0).add()
                    .candle().openPrice(0.0).closePrice(0.0).highPrice(12.0).lowPrice(6.0).add()
                    .candle().openPrice(0.0).closePrice(0.0).highPrice(18.0).lowPrice(14.0).add()
                    .candle().openPrice(0.0).closePrice(0.0).highPrice(10.0).lowPrice(6.0).add()
                    .candle().openPrice(0.0).closePrice(0.0).highPrice(8.0).lowPrice(4.0).add()
                    .build()
            )

        val awesome = AwesomeOscillatorIndicator(numFactory, medianPrice(), 2, 3)

        context.withIndicator(awesome)
            .fastForwardUntilStable()
            .assertCurrent(1.0 / 6)
            .assertNext(1.0)
            .assertNext(-3.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun withSma1AndSma2(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(
                MockMarketEventBuilder()
                    .candle().openPrice(0.0).closePrice(0.0).highPrice(16.0).lowPrice(8.0).add()
                    .candle().openPrice(0.0).closePrice(0.0).highPrice(12.0).lowPrice(6.0).add()
                    .candle().openPrice(0.0).closePrice(0.0).highPrice(18.0).lowPrice(14.0).add()
                    .candle().openPrice(0.0).closePrice(0.0).highPrice(10.0).lowPrice(6.0).add()
                    .candle().openPrice(0.0).closePrice(0.0).highPrice(8.0).lowPrice(4.0).add()
                    .build()
            )

        val awesome = AwesomeOscillatorIndicator(numFactory, medianPrice(), 1, 2)

        context.withIndicator(awesome)
            .fastForwardUntilStable()
            .assertCurrent(-1.5)
            .assertNext(3.5)
            .assertNext(-4.0)
            .assertNext(-1.0)
    }
}
