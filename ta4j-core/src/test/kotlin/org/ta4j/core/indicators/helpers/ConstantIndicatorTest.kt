/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.helpers

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.indicators.numeric.ConstantNumericIndicator
import org.ta4j.core.num.NumFactory

class ConstantIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun constantIndicator(numFactory: NumFactory) {
        val constantValue = numFactory.numOf(30.33)
        val constantIndicator = ConstantNumericIndicator(constantValue)

        MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(1.0, 2.0, 3.0, 4.0, 5.0)
            .withIndicator(constantIndicator)
            .assertNext(30.33)
            .assertNext(30.33)
            .assertNext(30.33)
            .assertNext(30.33)
    }
}
