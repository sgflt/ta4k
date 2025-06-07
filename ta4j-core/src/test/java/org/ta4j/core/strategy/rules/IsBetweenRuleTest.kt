/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2025 Ta4j Organization & respective
 * authors (see AUTHORS)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.ta4j.core.strategy.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.api.Indicators
import org.ta4j.core.indicators.helpers.FixedDecimalIndicator
import org.ta4j.core.indicators.numeric.ConstantNumericIndicator
import org.ta4j.core.num.NumFactory

class IsBetweenRuleTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should be satisfied when value is within bounds`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(5.0, 10.0, 15.0, 8.0, 12.0)

        val closePrice = Indicators.extended(numFactory).closePrice()
        context.withIndicator(closePrice)

        val rule = IsBetweenRule(closePrice, 20.0, 1.0)

        // All prices (5, 10, 15, 8, 12) should be within bounds [1, 20]
        context.fastForward(1)
        assertThat(rule.isSatisfied).isTrue()

        context.fastForward(1)
        assertThat(rule.isSatisfied).isTrue()

        context.fastForward(1)
        assertThat(rule.isSatisfied).isTrue()

        context.fastForward(1)
        assertThat(rule.isSatisfied).isTrue()

        context.fastForward(1)
        assertThat(rule.isSatisfied).isTrue()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should not be satisfied when value is above upper bound`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(5.0, 25.0, 15.0) // 25.0 is above upper bound of 20

        val closePrice = Indicators.extended(numFactory).closePrice()
        context.withIndicator(closePrice)

        val rule = IsBetweenRule(closePrice, 20.0, 1.0)

        context.fastForward(1)
        assertThat(rule.isSatisfied).isTrue() // 5.0 is within bounds

        context.fastForward(1)
        assertThat(rule.isSatisfied).isFalse() // 25.0 is above upper bound

        context.fastForward(1)
        assertThat(rule.isSatisfied).isTrue() // 15.0 is within bounds
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should not be satisfied when value is below lower bound`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(5.0, 0.5, 15.0) // 0.5 is below lower bound of 1

        val closePrice = Indicators.extended(numFactory).closePrice()
        context.withIndicator(closePrice)

        val rule = IsBetweenRule(closePrice, 20.0, 1.0)

        context.fastForward(1)
        assertThat(rule.isSatisfied).isTrue() // 5.0 is within bounds

        context.fastForward(1)
        assertThat(rule.isSatisfied).isFalse() // 0.5 is below lower bound

        context.fastForward(1)
        assertThat(rule.isSatisfied).isTrue() // 15.0 is within bounds
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should be satisfied when value equals bounds`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(1.0, 20.0, 10.0) // Test boundary values

        val closePrice = Indicators.extended(numFactory).closePrice()
        context.withIndicator(closePrice)

        val rule = IsBetweenRule(closePrice, 20.0, 1.0)

        context.fastForward(1)
        assertThat(rule.isSatisfied).isTrue() // 1.0 equals lower bound

        context.fastForward(1)
        assertThat(rule.isSatisfied).isTrue() // 20.0 equals upper bound

        context.fastForward(1)
        assertThat(rule.isSatisfied).isTrue() // 10.0 is within bounds
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should work with Num parameters`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(5.0, 25.0, 0.5)

        val closePrice = Indicators.extended(numFactory).closePrice()
        context.withIndicator(closePrice)

        val upperBound = numFactory.numOf(20)
        val lowerBound = numFactory.numOf(1)
        val rule = IsBetweenRule(closePrice, upperBound, lowerBound)

        context.fastForward(1)
        assertThat(rule.isSatisfied).isTrue() // 5.0 is within bounds

        context.fastForward(1)
        assertThat(rule.isSatisfied).isFalse() // 25.0 is above upper bound

        context.fastForward(1)
        assertThat(rule.isSatisfied).isFalse() // 0.5 is below lower bound
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should work with indicator parameters`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(5.0, 15.0, 25.0, 8.0)

        val closePrice = Indicators.extended(numFactory).closePrice()

        // Create dynamic bounds using SMA
        val upperBound = closePrice.sma(3).plus(5) // SMA + 5
        val lowerBound = closePrice.sma(3).minus(5) // SMA - 5

        context.withIndicator(closePrice)
        context.withIndicator(upperBound, "upper")
        context.withIndicator(lowerBound, "lower")

        val rule = IsBetweenRule(closePrice, upperBound, lowerBound)

        // Need at least 3 bars for SMA to be stable
        context.fastForward(3)

        // At this point we have prices [5, 15, 25]
        // SMA(3) = (5 + 15 + 25) / 3 = 15
        // Upper bound = 15 + 5 = 20
        // Lower bound = 15 - 5 = 10
        // Current price = 25, which is > 20, so should be false
        assertThat(rule.isSatisfied).isFalse()

        context.fastForward(1)
        // At this point we have prices [15, 25, 8]
        // SMA(3) = (15 + 25 + 8) / 3 = 16
        // Upper bound = 16 + 5 = 21
        // Lower bound = 16 - 5 = 11
        // Current price = 8, which is < 11, so should be false
        assertThat(rule.isSatisfied).isFalse()
    }

    @Test
    fun `should work with FixedDecimalIndicator for testing`() {
        val context = MarketEventTestContext()
            .withCandlePrices(10.0, 20.0, 30.0)

        val numFactory = context.barSeries.numFactory

        // Create a fixed indicator with known values
        val refIndicator = FixedDecimalIndicator(context.barSeries, 5.0, 15.0, 25.0)
        val upperIndicator = ConstantNumericIndicator(numFactory.numOf(20))
        val lowerIndicator = ConstantNumericIndicator(numFactory.numOf(10))

        context.withIndicator(refIndicator)

        val rule = IsBetweenRule(refIndicator, upperIndicator, lowerIndicator)

        context.fastForward(1)
        assertThat(rule.isSatisfied).isFalse() // 5.0 < 10 (lower bound)

        context.fastForward(1)
        assertThat(rule.isSatisfied).isTrue() // 15.0 is within [10, 20]

        context.fastForward(1)
        assertThat(rule.isSatisfied).isFalse() // 25.0 > 20 (upper bound)
    }

    @Test
    fun `toString should provide meaningful representation`() {
        val context = MarketEventTestContext()
            .withCandlePrices(10.0)

        val closePrice = Indicators.closePrice()
        context.withIndicator(closePrice)

        val rule = IsBetweenRule(closePrice, 20.0, 5.0)

        context.fastForward(1)
        val stringRepresentation = rule.toString()

        assertThat(stringRepresentation)
            .contains("IsBetweenRule")
            .contains("=>")
            .contains("true") // 10.0 is within [5.0, 20.0]
    }
}
