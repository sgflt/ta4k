/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.bool.chandelier

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.mocks.MockMarketEventBuilder
import org.ta4j.core.num.NumFactory

class ChandelierExitLongIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun massIndexUsing3And8BarCounts(numFactory: NumFactory) {
        val testData = MockMarketEventBuilder()
            .candle().lowPrice(99.0).highPrice(101.0).closePrice(100.0).add()
            .candle().lowPrice(104.0).highPrice(106.0).closePrice(105.0).add()
            .candle().lowPrice(109.0).highPrice(111.0).closePrice(110.0).add()
            .candle().lowPrice(107.0).highPrice(109.0).closePrice(108.0).add()
            .candle().lowPrice(92.0).highPrice(96.0).closePrice(90.0).add() // this is out of 3 * atr
            .candle().lowPrice(103.0).highPrice(105.0).closePrice(104.0).add()
            .candle().lowPrice(105.0).highPrice(107.0).closePrice(106.0).add()
            .build()

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(testData)

        val cel = ChandelierExitLongIndicator(numFactory, 5, 2.0)

        context.withIndicator(cel)
            .assertNextFalse()
            .assertNextFalse()
            .assertNextFalse()
            .assertNextFalse()
            .assertNextTrue()
            .assertNextFalse()
            .assertNextFalse()
    }
}