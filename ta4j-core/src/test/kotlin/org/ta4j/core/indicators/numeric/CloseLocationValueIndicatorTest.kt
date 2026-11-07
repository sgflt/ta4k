/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric

import java.time.temporal.ChronoUnit
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.mocks.MockMarketEventBuilder
import org.ta4j.core.num.NumFactory

internal class CloseLocationValueIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun getValue(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandleDuration(ChronoUnit.DAYS)
            .withMarketEvents(
                MockMarketEventBuilder()
                    .candle()
                    .closePrice(18.0).highPrice(20.0).lowPrice(10.0)
                    .add()
                    .candle()
                    .closePrice(20.0).highPrice(21.0).lowPrice(17.0)
                    .add()
                    .candle()
                    .closePrice(15.0).highPrice(16.0).lowPrice(14.0)
                    .add()
                    .candle()
                    .closePrice(11.0).highPrice(15.0).lowPrice(8.0)
                    .add()
                    .candle()
                    .closePrice(12.0).highPrice(12.0).lowPrice(10.0)
                    .add()
                    .build()
            )

        val indicator = CloseLocationValueIndicator(numFactory)
        context.withIndicator(indicator)

        context
            .assertNext(0.6)
            .assertNext(0.5)
            .assertNext(0.0)
            .assertNext(-1.0 / 7)
            .assertNext(1.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun returnZeroIfHighEqualsLow(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandleDuration(ChronoUnit.DAYS)
            .withMarketEvents(
                MockMarketEventBuilder()
                    .candle()
                    .closePrice(10.0).highPrice(10.0).lowPrice(10.0)
                    .add()
                    .candle()
                    .closePrice(12.0).highPrice(12.0).lowPrice(10.0)
                    .add()
                    .candle()
                    .closePrice(120.0).highPrice(140.0).lowPrice(100.0)
                    .add()
                    .build()
            )

        val indicator = CloseLocationValueIndicator(numFactory)
        context.withIndicator(indicator)

        context
            .assertNext(0.0)
            .assertNext(1.0)
            .assertNext(0.0)
    }
}
