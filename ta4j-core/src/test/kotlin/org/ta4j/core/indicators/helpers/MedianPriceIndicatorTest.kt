/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.helpers

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.indicators.numeric.candles.price.MedianPriceIndicator
import org.ta4j.core.num.NumFactory

class MedianPriceIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun indicatorShouldRetrieveBarMedianPrice(numFactory: NumFactory) {
        // For simplicity, use withCandlePrices which sets OHLC to the same value
        // Since MedianPriceIndicator calculates (high + low) / 2, and high = low = price
        // The median price will be (price + price) / 2 = price
        val medianPriceIndicator = MedianPriceIndicator(numFactory)

        MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 20.0, 30.0, 15.0, 25.0)
            .withIndicator(medianPriceIndicator)
            .assertNext(10.0)  // (10 + 10) / 2 = 10
            .assertNext(20.0)  // (20 + 20) / 2 = 20
            .assertNext(30.0)  // (30 + 30) / 2 = 30
            .assertNext(15.0)  // (15 + 15) / 2 = 15
            .assertNext(25.0)  // (25 + 25) / 2 = 25
    }
}
