/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.candles.price

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.api.Indicators

class OpenPriceIndicatorTest {

    private lateinit var context: MarketEventTestContext

    @BeforeEach
    fun setUp() {
        context = MarketEventTestContext().withDefaultMarketEvents()
        context.withIndicator(Indicators.openPrice())
    }

    @Test
    fun indicatorShouldRetrieveBarOpenPrice() {
        for (i in 0 until 10) {
            context.assertNext(i + 1.0)
        }
    }
}