/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.statistics

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.api.Indicators
import org.ta4j.core.num.NumFactory

internal class VarianceIndicatorTest {
    private lateinit var testContext: MarketEventTestContext

    @BeforeEach
    fun setUp() {
        testContext = MarketEventTestContext()
        testContext.withCandlePrices(1.0, 2.0, 3.0, 4.0, 3.0, 4.0, 5.0, 4.0, 3.0, 0.0, 9.0)
    }

    @ParameterizedTest(name = "VAR(4) [{index}] {0}")
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun varianceUsingBarCount4UsingClosePrice(numFactory: NumFactory) {
        testContext.withNumFactory(numFactory)

        val varianceIndicator = Indicators.closePrice().variance(4)
        testContext.withIndicator(varianceIndicator)

        // unstable values may produce garbage, this is why they are called unstable
        testContext.assertNext(0.0)
        assertFalse(varianceIndicator.isStable)

        testContext.assertNext(0.5)
        assertFalse(varianceIndicator.isStable)

        testContext.assertNext(1.0)
        assertFalse(varianceIndicator.isStable)

        // stable date bellow
        testContext.assertNext(1.6667)
        assertTrue(varianceIndicator.isStable)

        testContext.assertNext(0.6667)
        assertTrue(varianceIndicator.isStable)

        testContext.assertNext(0.3333)
        testContext.assertNext(0.6667)
        testContext.assertNext(0.6667)
        testContext.assertNext(0.6667)
        testContext.assertNext(4.6667)
        testContext.assertNext(14.000)
    }

    @Test
    fun varianceShouldBeZeroWhenBarCountIs1() {
        assertThrows(IllegalArgumentException::class.java) { Indicators.closePrice().variance(1) }
    }

    @ParameterizedTest(name = "VAR(2) [{index}] {0}")
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun varianceUsingBarCount2UsingClosePrice(numFactory: NumFactory) {
        testContext.withNumFactory(numFactory)

        val varianceIndicator = Indicators.closePrice().variance(2)
        testContext.withIndicator(varianceIndicator)

        testContext.assertNext(0.0)
        assertFalse(varianceIndicator.isStable)

        testContext.assertNext(0.5)
        assertTrue(varianceIndicator.isStable)

        testContext.assertNext(0.5)
        assertTrue(varianceIndicator.isStable)

        testContext.assertNext(0.5)
        assertTrue(varianceIndicator.isStable)

        testContext.assertNext(0.5)
        assertTrue(varianceIndicator.isStable)

        testContext.assertNext(0.5)
        assertTrue(varianceIndicator.isStable)

        testContext.assertNext(0.5)
        testContext.assertNext(0.5)
        testContext.assertNext(0.5)
        testContext.assertNext(4.5)
        testContext.assertNext(40.5)
    }
}
