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
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.ta4j.core.indicators.helpers

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.api.Indicators
import org.ta4j.core.indicators.helpers.ConvergenceDivergenceStrictType.NEGATIVE_CONVERGENT_STRICT
import org.ta4j.core.indicators.helpers.ConvergenceDivergenceStrictType.NEGATIVE_DIVERGENT_STRICT
import org.ta4j.core.indicators.helpers.ConvergenceDivergenceStrictType.POSITIVE_CONVERGENT_STRICT
import org.ta4j.core.indicators.helpers.ConvergenceDivergenceStrictType.POSITIVE_DIVERGENT_STRICT
import org.ta4j.core.indicators.helpers.ConvergenceDivergenceType.NEGATIVE_CONVERGENT
import org.ta4j.core.indicators.helpers.ConvergenceDivergenceType.NEGATIVE_DIVERGENT
import org.ta4j.core.indicators.helpers.ConvergenceDivergenceType.POSITIVE_CONVERGENT
import org.ta4j.core.indicators.helpers.ConvergenceDivergenceType.POSITIVE_DIVERGENT
import org.ta4j.core.mocks.MockMarketEventBuilder
import org.ta4j.core.num.NumFactory

class ConvergenceDivergenceIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should detect positive convergence when both indicators rise together`(numFactory: NumFactory) {
        val ref = Indicators.extended(numFactory).closePrice()
        val other = Indicators.extended(numFactory).openPrice()

        val indicator = ConvergenceDivergenceIndicator(ref, other, 3, POSITIVE_CONVERGENT)

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(indicator)
            .withMarketEvents(
                MockMarketEventBuilder()
                    .candle().openPrice(10.0).closePrice(11.0).highPrice(12.0).lowPrice(9.0).add()
                    .candle().openPrice(13.0).closePrice(15.0).highPrice(16.0).lowPrice(12.0).add()
                    .candle().openPrice(16.0).closePrice(19.0).highPrice(20.0).lowPrice(15.0).add()
                    .candle().openPrice(19.0).closePrice(23.0).highPrice(24.0).lowPrice(18.0).add()
                    .candle().openPrice(22.0).closePrice(27.0).highPrice(28.0).lowPrice(21.0).add()
                    .build()
            )

        context.assertNextFalse() // Bar 0 - not enough data
        context.assertNextFalse() // Bar 1 - not enough data  
        context.assertNextFalse() // Bar 2 - not enough data
        context.assertNextTrue()  // Bar 3 - should detect positive convergence
        context.assertNextTrue()  // Bar 4 - continues positive convergence
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should detect negative convergence when both indicators fall together`(numFactory: NumFactory) {
        val ref = Indicators.extended(numFactory).closePrice()
        val other = Indicators.extended(numFactory).openPrice()

        val indicator = ConvergenceDivergenceIndicator(ref, other, 3, NEGATIVE_CONVERGENT)

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(indicator)
            .withMarketEvents(
                MockMarketEventBuilder()
                    .candle().openPrice(25.0).closePrice(24.0).highPrice(26.0).lowPrice(23.0).add()
                    .candle().openPrice(20.0).closePrice(18.0).highPrice(22.0).lowPrice(17.0).add()
                    .candle().openPrice(15.0).closePrice(12.0).highPrice(17.0).lowPrice(11.0).add()
                    .candle().openPrice(10.0).closePrice(7.0).highPrice(12.0).lowPrice(6.0).add()
                    .candle().openPrice(5.0).closePrice(3.0).highPrice(7.0).lowPrice(2.0).add()
                    .build()
            )

        context.assertNextFalse() // Bar 0 - not enough data
        context.assertNextFalse() // Bar 1 - not enough data
        context.assertNextFalse() // Bar 2 - not enough data
        context.assertNextTrue()  // Bar 3 - should detect negative convergence
        context.assertNextTrue()  // Bar 4 - continues negative convergence
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should detect positive divergence when ref rises and other falls`(numFactory: NumFactory) {
        val ref = Indicators.extended(numFactory).closePrice()
        val other = Indicators.extended(numFactory).openPrice()

        val indicator = ConvergenceDivergenceIndicator(ref, other, 3, POSITIVE_DIVERGENT)

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(indicator)
            .withMarketEvents(
                MockMarketEventBuilder()
                    .candle().openPrice(25.0).closePrice(10.0).highPrice(26.0).lowPrice(9.0).add()
                    .candle().openPrice(20.0).closePrice(15.0).highPrice(22.0).lowPrice(14.0).add()
                    .candle().openPrice(15.0).closePrice(20.0).highPrice(22.0).lowPrice(14.0).add()
                    .candle().openPrice(10.0).closePrice(25.0).highPrice(27.0).lowPrice(9.0).add()
                    .candle().openPrice(5.0).closePrice(30.0).highPrice(32.0).lowPrice(4.0).add()
                    .build()
            )

        context.assertNextFalse() // Bar 0 - not enough data
        context.assertNextFalse() // Bar 1 - not enough data
        context.assertNextFalse() // Bar 2 - not enough data
        context.assertNextTrue()  // Bar 3 - should detect positive divergence
        context.assertNextTrue()  // Bar 4 - continues positive divergence
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should detect negative divergence when ref falls and other rises`(numFactory: NumFactory) {
        val ref = Indicators.extended(numFactory).closePrice()
        val other = Indicators.extended(numFactory).openPrice()

        val indicator = ConvergenceDivergenceIndicator(ref, other, 3, NEGATIVE_DIVERGENT)

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(indicator)
            .withMarketEvents(
                MockMarketEventBuilder()
                    .candle().openPrice(5.0).closePrice(25.0).highPrice(26.0).lowPrice(4.0).add()
                    .candle().openPrice(10.0).closePrice(20.0).highPrice(22.0).lowPrice(9.0).add()
                    .candle().openPrice(15.0).closePrice(15.0).highPrice(18.0).lowPrice(14.0).add()
                    .candle().openPrice(20.0).closePrice(10.0).highPrice(22.0).lowPrice(9.0).add()
                    .candle().openPrice(25.0).closePrice(5.0).highPrice(27.0).lowPrice(4.0).add()
                    .build()
            )

        context.assertNextFalse() // Bar 0 - not enough data
        context.assertNextFalse() // Bar 1 - not enough data
        context.assertNextFalse() // Bar 2 - not enough data
        context.assertNextTrue()  // Bar 3 - should detect negative divergence
        context.assertNextTrue()  // Bar 4 - continues negative divergence
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should detect strict positive convergence when both consistently rise`(numFactory: NumFactory) {
        val ref = Indicators.extended(numFactory).closePrice()
        val other = Indicators.extended(numFactory).openPrice()

        val indicator = ConvergenceDivergenceIndicator(ref, other, 3, strictType = POSITIVE_CONVERGENT_STRICT)

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(indicator)
            .withMarketEvents(
                MockMarketEventBuilder()
                    .candle().openPrice(10.0).closePrice(11.0).highPrice(12.0).lowPrice(9.0).add()
                    .candle().openPrice(11.0).closePrice(12.0).highPrice(13.0).lowPrice(10.0).add()
                    .candle().openPrice(12.0).closePrice(13.0).highPrice(14.0).lowPrice(11.0).add()
                    .candle().openPrice(13.0).closePrice(14.0).highPrice(15.0).lowPrice(12.0).add()
                    .candle().openPrice(14.0).closePrice(15.0).highPrice(16.0).lowPrice(13.0).add()
                    .build()
            )

        context.assertNextFalse() // Bar 0 - not enough data
        context.assertNextFalse() // Bar 1 - not enough data
        context.assertNextFalse() // Bar 2 - not enough data
        context.assertNextTrue()  // Bar 3 - should detect strict positive convergence
        context.assertNextTrue()  // Bar 4 - continues strict positive convergence
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should detect strict negative convergence when both consistently fall`(numFactory: NumFactory) {
        val ref = Indicators.extended(numFactory).closePrice()
        val other = Indicators.extended(numFactory).openPrice()

        val indicator = ConvergenceDivergenceIndicator(ref, other, 3, strictType = NEGATIVE_CONVERGENT_STRICT)

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(indicator)
            .withMarketEvents(
                MockMarketEventBuilder()
                    .candle().openPrice(15.0).closePrice(14.0).highPrice(16.0).lowPrice(14.0).add()
                    .candle().openPrice(14.0).closePrice(13.0).highPrice(15.0).lowPrice(13.0).add()
                    .candle().openPrice(13.0).closePrice(12.0).highPrice(14.0).lowPrice(12.0).add()
                    .candle().openPrice(12.0).closePrice(11.0).highPrice(13.0).lowPrice(11.0).add()
                    .candle().openPrice(11.0).closePrice(10.0).highPrice(12.0).lowPrice(10.0).add()
                    .build()
            )

        context.assertNextFalse() // Bar 0 - not enough data
        context.assertNextFalse() // Bar 1 - not enough data
        context.assertNextFalse() // Bar 2 - not enough data
        context.assertNextTrue()  // Bar 3 - should detect strict negative convergence
        context.assertNextTrue()  // Bar 4 - continues strict negative convergence
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should detect strict positive divergence when ref rises and other consistently falls`(numFactory: NumFactory) {
        val ref = Indicators.extended(numFactory).closePrice()
        val other = Indicators.extended(numFactory).openPrice()

        val indicator = ConvergenceDivergenceIndicator(ref, other, 3, strictType = POSITIVE_DIVERGENT_STRICT)

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(indicator)
            .withMarketEvents(
                MockMarketEventBuilder()
                    .candle().openPrice(15.0).closePrice(10.0).highPrice(16.0).lowPrice(9.0).add()
                    .candle().openPrice(14.0).closePrice(11.0).highPrice(17.0).lowPrice(10.0).add()
                    .candle().openPrice(13.0).closePrice(12.0).highPrice(18.0).lowPrice(11.0).add()
                    .candle().openPrice(12.0).closePrice(13.0).highPrice(19.0).lowPrice(12.0).add()
                    .candle().openPrice(11.0).closePrice(14.0).highPrice(20.0).lowPrice(13.0).add()
                    .build()
            )

        context.assertNextFalse() // Bar 0 - not enough data
        context.assertNextFalse() // Bar 1 - not enough data
        context.assertNextFalse() // Bar 2 - not enough data
        context.assertNextTrue()  // Bar 3 - should detect strict positive divergence
        context.assertNextTrue()  // Bar 4 - continues strict positive divergence
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should detect strict negative divergence when ref falls and other consistently rises`(numFactory: NumFactory) {
        val ref = Indicators.extended(numFactory).closePrice()
        val other = Indicators.extended(numFactory).openPrice()

        val indicator = ConvergenceDivergenceIndicator(ref, other, 3, strictType = NEGATIVE_DIVERGENT_STRICT)

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(indicator)
            .withMarketEvents(
                MockMarketEventBuilder()
                    .candle().openPrice(10.0).closePrice(15.0).highPrice(20.0).lowPrice(9.0).add()
                    .candle().openPrice(11.0).closePrice(14.0).highPrice(21.0).lowPrice(10.0).add()
                    .candle().openPrice(12.0).closePrice(13.0).highPrice(22.0).lowPrice(11.0).add()
                    .candle().openPrice(13.0).closePrice(12.0).highPrice(23.0).lowPrice(12.0).add()
                    .candle().openPrice(14.0).closePrice(11.0).highPrice(24.0).lowPrice(13.0).add()
                    .build()
            )

        context.assertNextFalse() // Bar 0 - not enough data
        context.assertNextFalse() // Bar 1 - not enough data
        context.assertNextFalse() // Bar 2 - not enough data
        context.assertNextTrue()  // Bar 3 - should detect strict negative divergence
        context.assertNextTrue()  // Bar 4 - continues strict negative divergence
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should return false when indicators move sideways`(numFactory: NumFactory) {
        val ref = Indicators.extended(numFactory).closePrice()
        val other = Indicators.extended(numFactory).openPrice()

        val indicator = ConvergenceDivergenceIndicator(ref, other, 3, POSITIVE_CONVERGENT)

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(indicator)
            .withMarketEvents(
                MockMarketEventBuilder()
                    .candle().openPrice(10.0).closePrice(11.0).highPrice(12.0).lowPrice(9.0).add()
                    .candle().openPrice(10.5).closePrice(10.5).highPrice(12.0).lowPrice(9.0).add()
                    .candle().openPrice(10.0).closePrice(11.0).highPrice(12.0).lowPrice(9.0).add()
                    .candle().openPrice(10.5).closePrice(10.5).highPrice(12.0).lowPrice(9.0).add()
                    .candle().openPrice(10.0).closePrice(11.0).highPrice(12.0).lowPrice(9.0).add()
                    .build()
            )

        context.assertNextFalse() // Bar 0 - not enough data
        context.assertNextFalse() // Bar 1 - not enough data
        context.assertNextFalse() // Bar 2 - not enough data
        context.assertNextFalse() // Bar 3 - no clear convergence/divergence
        context.assertNextFalse() // Bar 4 - no clear convergence/divergence
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle different bar counts correctly`(numFactory: NumFactory) {
        val ref = Indicators.extended(numFactory).closePrice()
        val other = Indicators.extended(numFactory).openPrice()

        // Test different bar counts
        val indicator3 = ConvergenceDivergenceIndicator(ref, other, 3, POSITIVE_CONVERGENT)
        val indicator5 = ConvergenceDivergenceIndicator(ref, other, 5, POSITIVE_CONVERGENT)
        val indicator10 = ConvergenceDivergenceIndicator(ref, other, 10, POSITIVE_CONVERGENT)

        assertThat(indicator3.lag).isEqualTo(3)
        assertThat(indicator5.lag).isEqualTo(5)
        assertThat(indicator10.lag).isEqualTo(10)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle custom strength and slope parameters`(numFactory: NumFactory) {
        val ref = Indicators.extended(numFactory).closePrice()
        val other = Indicators.extended(numFactory).openPrice()

        // Test with custom parameters
        val weakStrength =
            ConvergenceDivergenceIndicator(ref, other, 3, POSITIVE_CONVERGENT, minStrength = 0.3, minSlope = 0.1)
        val strongStrength =
            ConvergenceDivergenceIndicator(ref, other, 3, POSITIVE_CONVERGENT, minStrength = 0.9, minSlope = 0.5)

        assertThat(weakStrength).isNotNull()
        assertThat(strongStrength).isNotNull()
        assertThat(weakStrength.lag).isEqualTo(3)
        assertThat(strongStrength.lag).isEqualTo(3)
    }
}
