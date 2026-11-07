/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.candles.price

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.api.Indicators

class LowPriceIndicatorTest {
    private lateinit var context: MarketEventTestContext

    @BeforeEach
    fun setUp() {
        context = MarketEventTestContext().withDefaultMarketEvents()
        context.withIndicator(Indicators.lowPrice())
    }

    @Test
    fun indicatorShouldRetrieveBarHighPrice() {
        for (i in 0 until 10) {
            context.assertNext(i + 3.0)
        }
    }
}