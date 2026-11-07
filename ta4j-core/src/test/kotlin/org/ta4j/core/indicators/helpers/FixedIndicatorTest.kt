/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.helpers

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.num.NumFactory

class FixedIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun getValueOnFixedDecimalIndicatorDouble(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(1.0, 2.0, 3.0, 4.0, 5.0)

        val fixedDecimalIndicator = FixedDecimalIndicator(
            context.barSeries,
            13.37, 42.0, -17.0
        )

        context
            .withIndicator(fixedDecimalIndicator)
            .assertNext(13.37)
            .assertNext(42.0)
            .assertNext(-17.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun getValueOnFixedDecimalIndicatorString(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(1.0, 2.0, 3.0, 4.0, 5.0)

        val fixedDecimalIndicator = FixedDecimalIndicator(
            context.barSeries,
            "3.0", "-123.456", "0.0"
        )

        context
            .withIndicator(fixedDecimalIndicator)
            .assertNext(3.0)
            .assertNext(-123.456)
            .assertNext(0.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun getValueOnFixedBooleanIndicator(numFactory: NumFactory) {
        val fixedBooleanIndicator = FixedBooleanIndicator(
            false, false, true, false, true
        )

        MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(1.0, 2.0, 3.0, 4.0, 5.0)
            .withIndicator(fixedBooleanIndicator)
            .assertNextFalse()
            .assertNextFalse()
            .assertNextTrue()
            .assertNextFalse()
            .assertNextTrue()
    }
}
