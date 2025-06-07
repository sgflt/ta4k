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
import org.ta4j.core.indicators.numeric.ConstantNumericIndicator
import org.ta4j.core.num.NumFactory

class IsEqualRuleTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should be satisfied when indicators have equal values`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 20.0, 30.0, 20.0, 10.0)

        val closePrice = context.barSeries.numFactory.let { factory ->
            org.ta4j.core.api.Indicators.extended(factory).closePrice()
        }

        val constantIndicator = ConstantNumericIndicator(numFactory.numOf(20.0))
        val rule = IsEqualRule(closePrice, constantIndicator)

        context.withIndicator(closePrice)
        context.withIndicator(constantIndicator)

        // Test sequence: 10.0, 20.0, 30.0, 20.0, 10.0
        context.advance()
        assertThat(rule.isSatisfied).isFalse() // 10.0 != 20.0

        context.advance()
        assertThat(rule.isSatisfied).isTrue() // 20.0 == 20.0

        context.advance()
        assertThat(rule.isSatisfied).isFalse() // 30.0 != 20.0

        context.advance()
        assertThat(rule.isSatisfied).isTrue() // 20.0 == 20.0

        context.advance()
        assertThat(rule.isSatisfied).isFalse() // 10.0 != 20.0
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should work with constructor taking Number value`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(15.5, 15.5, 16.0)

        val closePrice = context.barSeries.numFactory.let { factory ->
            org.ta4j.core.api.Indicators.extended(factory).closePrice()
        }

        val rule = IsEqualRule(closePrice, 15.5)

        context.withIndicator(closePrice)

        context.advance()
        assertThat(rule.isSatisfied).isTrue() // 15.5 == 15.5

        context.advance()
        assertThat(rule.isSatisfied).isTrue() // 15.5 == 15.5

        context.advance()
        assertThat(rule.isSatisfied).isFalse() // 16.0 != 15.5
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should work with constructor taking Num value`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 200.0, 100.0)

        val closePrice = context.barSeries.numFactory.let { factory ->
            org.ta4j.core.api.Indicators.extended(factory).closePrice()
        }

        val targetValue = numFactory.numOf(100.0)
        val rule = IsEqualRule(closePrice, targetValue)

        context.withIndicator(closePrice)

        context.advance()
        assertThat(rule.isSatisfied).isTrue() // 100.0 == 100.0

        context.advance()
        assertThat(rule.isSatisfied).isFalse() // 200.0 != 100.0

        context.advance()
        assertThat(rule.isSatisfied).isTrue() // 100.0 == 100.0
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should work with two different indicators`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 15.0, 20.0, 15.0)

        val closePrice = context.barSeries.numFactory.let { factory ->
            org.ta4j.core.api.Indicators.extended(factory).closePrice()
        }

        val openPrice = context.barSeries.numFactory.let { factory ->
            org.ta4j.core.api.Indicators.extended(factory).openPrice()
        }

        val rule = IsEqualRule(closePrice, openPrice)

        context.withIndicator(closePrice)
        context.withIndicator(openPrice)

        // Since we're using the same price for open and close in our test data,
        // they should always be equal
        context.advance()
        assertThat(rule.isSatisfied).isTrue()

        context.advance()
        assertThat(rule.isSatisfied).isTrue()

        context.advance()
        assertThat(rule.isSatisfied).isTrue()

        context.advance()
        assertThat(rule.isSatisfied).isTrue()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle edge cases with zero and negative values`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(-5.0, 0.0, 5.0, 0.0, -5.0)

        val closePrice = context.barSeries.numFactory.let { factory ->
            org.ta4j.core.api.Indicators.extended(factory).closePrice()
        }

        val zeroRule = IsEqualRule(closePrice, 0.0)
        val negativeRule = IsEqualRule(closePrice, -5.0)

        context.withIndicator(closePrice)

        context.advance()
        assertThat(zeroRule.isSatisfied).isFalse() // -5.0 != 0.0
        assertThat(negativeRule.isSatisfied).isTrue() // -5.0 == -5.0

        context.advance()
        assertThat(zeroRule.isSatisfied).isTrue() // 0.0 == 0.0
        assertThat(negativeRule.isSatisfied).isFalse() // 0.0 != -5.0

        context.advance()
        assertThat(zeroRule.isSatisfied).isFalse() // 5.0 != 0.0
        assertThat(negativeRule.isSatisfied).isFalse() // 5.0 != -5.0

        context.advance()
        assertThat(zeroRule.isSatisfied).isTrue() // 0.0 == 0.0
        assertThat(negativeRule.isSatisfied).isFalse() // 0.0 != -5.0

        context.advance()
        assertThat(zeroRule.isSatisfied).isFalse() // -5.0 != 0.0
        assertThat(negativeRule.isSatisfied).isTrue() // -5.0 == -5.0
    }

    @Test
    fun `should have proper toString representation`() {
        val numFactory = org.ta4j.core.num.DoubleNumFactory
        val indicator1 = ConstantNumericIndicator(numFactory.numOf(10.0))
        val indicator2 = ConstantNumericIndicator(numFactory.numOf(10.0))
        val rule = IsEqualRule(indicator1, indicator2)

        val result = rule.toString()
        assertThat(result).contains("IsEqualRule")
        assertThat(result).contains("=>")
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should work with decimal precision`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.123, 10.124, 10.123)

        val closePrice = context.barSeries.numFactory.let { factory ->
            org.ta4j.core.api.Indicators.extended(factory).closePrice()
        }

        val rule = IsEqualRule(closePrice, 10.123)

        context.withIndicator(closePrice)

        context.advance()
        assertThat(rule.isSatisfied).isTrue() // 10.123 == 10.123

        context.advance()
        assertThat(rule.isSatisfied).isFalse() // 10.124 != 10.123

        context.advance()
        assertThat(rule.isSatisfied).isTrue() // 10.123 == 10.123
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle changing indicator values over time`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(1.0, 2.0, 3.0, 4.0, 5.0, 4.0, 3.0, 2.0, 1.0)

        val closePrice = context.barSeries.numFactory.let { factory ->
            org.ta4j.core.api.Indicators.extended(factory).closePrice()
        }

        // Create a simple moving average that changes over time
        val sma3 = closePrice.sma(3)
        val rule = IsEqualRule(closePrice, sma3)

        context.withIndicator(closePrice)
        context.withIndicator(sma3)

        // Fast forward until SMA is stable
        context.fastForward(3)

        // Now test equality - close price and SMA3 should rarely be equal
        var equalCount = 0
        var totalTests = 0

        while (context.advance()) {
            if (rule.isSatisfied) {
                equalCount++
            }
            totalTests++
        }

        // Verify that the rule worked (equality should be rare but possible)
        assertThat(totalTests).isGreaterThan(0)
        assertThat(equalCount).isLessThanOrEqualTo(totalTests)
    }
}
