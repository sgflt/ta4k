/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.strategy.rules

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.indicators.helpers.FixedBooleanIndicator

internal class BooleanIndicatorRuleTest {

    private lateinit var context: MarketEventTestContext

    @BeforeEach
    fun setUp() {
        context = MarketEventTestContext()
            .withCandlePrices(1.0, 2.0, 3.0, 4.0, 5.0)
            .withIndicator(FixedBooleanIndicator(true, true, false, false, true))
    }

    @Test
    fun isSatisfied() {
        context
            .assertNextTrue()
            .assertNextTrue()
            .assertNextFalse()
            .assertNextFalse()
            .assertNextTrue()
    }
}