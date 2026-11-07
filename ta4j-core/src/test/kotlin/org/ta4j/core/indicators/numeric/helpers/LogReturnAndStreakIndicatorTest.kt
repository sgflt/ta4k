/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.helpers

import kotlin.math.ln
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.TestUtils.assertNumEquals
import org.ta4j.core.api.Indicators
import org.ta4j.core.num.NumFactory

class LogReturnAndStreakIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `log return compares value with value barCount bars ago`(numFactory: NumFactory) {
        val closePrice = Indicators.extended(numFactory).closePrice()
        val oneBar = closePrice.logReturn()
        val twoBars = closePrice.logReturn(2)
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 110.0, 99.0)
            .withIndicators(oneBar, twoBars)

        context.advance()
        assertThat(oneBar.value.isNaN).isTrue()
        assertThat(oneBar.isStable).isFalse()

        context.advance()
        assertNumEquals(ln(110.0 / 100.0), oneBar.value)
        assertThat(oneBar.isStable).isTrue()
        assertThat(twoBars.value.isNaN).isTrue()

        context.advance()
        assertNumEquals(ln(99.0 / 110.0), oneBar.value)
        assertNumEquals(ln(99.0 / 100.0), twoBars.value)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `log return is NaN for non positive values`(numFactory: NumFactory) {
        val logReturn = Indicators.extended(numFactory).closePrice().logReturn()
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(0.0, 10.0)
            .withIndicator(logReturn)

        context.advance()
        context.advance()
        assertThat(logReturn.value.isNaN).isTrue()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `streak counts consecutive rises and falls`(numFactory: NumFactory) {
        val streak = Indicators.extended(numFactory).closePrice().streak()
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 11.0, 12.0, 11.0, 10.0, 10.0, 12.0)
            .withIndicator(streak)

        context.advance()
        assertNumEquals(0.0, streak.value)
        assertThat(streak.isStable).isFalse()

        val expected = listOf(1.0, 2.0, -1.0, -2.0, 0.0, 1.0)
        for (value in expected) {
            context.advance()
            assertNumEquals(value, streak.value)
        }
        assertThat(streak.isStable).isTrue()
    }
}
