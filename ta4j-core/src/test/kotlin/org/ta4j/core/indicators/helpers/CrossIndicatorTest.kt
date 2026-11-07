/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.helpers

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.api.Indicators
import org.ta4j.core.num.NumFactory

class CrossIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun testCrossedConstant(numFactory: NumFactory) {
        // Use known price data similar to the original test
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(1.0, 2.0, 3.0, 4.0, 4.5, 6.0, 5.0, 4.0, 3.0, 2.0, 1.0)

        val closePrice = Indicators.extended(numFactory).closePrice()
        val crossIndicator = closePrice.crossedOver(5)
        context.withIndicator(crossIndicator)

        // Test the cross over behavior based on the original test pattern
        // Values: 1.0, 2.0, 3.0, 4.0, 4.5 - all below 5, should be false
        repeat(5) {
            context.assertNextFalse()
        }
        // Value: 6.0 - crosses over 5, should be true
        context.assertNextTrue()
        // Values: 5.0, 4.0, 3.0, 2.0, 1.0 - all at or below 5, should be false
        repeat(5) {
            context.assertNextFalse()
        }
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun testCrossedIndicator(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(8.0, 7.0, 5.0, 2.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0)

        val closePrice = Indicators.extended(numFactory).closePrice()
        val sma = closePrice.sma(2)
        val crossIndicator = closePrice.crossedOver(sma)
        context.withIndicator(crossIndicator)

        // Skip initial bars where SMA is warming up
        context.fastForward(2)

        // At index 2: close=5.0, sma=(7.0+5.0)/2=6.0, close < sma, no cross
        context.assertNextFalse()
        // At index 3: close=2.0, sma=(5.0+2.0)/2=3.5, close < sma, no cross
        context.assertNextFalse()
        // At index 4: close=5.0, sma=(2.0+5.0)/2=3.5, close crosses over sma
        context.assertNextTrue()
        // Remaining values should not cross
        repeat(5) {
            context.assertNextFalse()
        }
    }
}
