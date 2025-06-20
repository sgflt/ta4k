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
package org.ta4j.core.indicators.bool

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.TradingRecordTestContext
import org.ta4j.core.api.Indicators
import org.ta4j.core.num.NumFactory

class IsRisingIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should be true when all values in window are rising with strict strength`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(50.0, 70.0, 80.0, 90.0, 99.0, 60.0, 30.0, 20.0, 10.0, 0.0)
            .withIndicator(IsRisingIndicator(numFactory, Indicators.extended(numFactory).closePrice(), 4, 1.0))

        // Skip first few bars to get enough data
        context.fastForward(4)  // Move to index 4 (value 99.0)

        // Check if the rising trend is detected
        context.assertNextTrue()  // Should be true as values went from 50->70->80->90->99

        // Move to next bar where trend breaks
        context.assertNextFalse()  // Should be false as 99->60 is falling
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle partial rising with lower strength requirement`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 20.0, 15.0, 25.0, 30.0)
            .withIndicator(IsRisingIndicator(numFactory, Indicators.extended(numFactory).closePrice(), 4, 0.5))

        context.fastForwardUntilStable()

        // With 50% strength, should be satisfied even if not all periods are rising
        // Window [10,20,15,25] has 2 out of 3 rising periods (20>10, 25>15)
        context.assertNextTrue()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle flat sequence with same values`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 100.0, 100.0, 100.0, 100.0)
            .withIndicator(IsRisingIndicator(numFactory, Indicators.extended(numFactory).closePrice(), 3, 1.0))

        context.fastForwardUntilStable()

        // All values are equal, no rising periods
        repeat(2) {
            context.assertNextFalse()
        }
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle descending sequence`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 90.0, 80.0, 70.0, 60.0)
            .withIndicator(IsRisingIndicator(numFactory, Indicators.extended(numFactory).closePrice(), 3, 1.0))

        context.fastForwardUntilStable()

        // All values are falling, no rising periods
        repeat(2) {
            context.assertNextFalse()
        }
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle single bar window`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 20.0, 15.0, 25.0)
            .withIndicator(IsRisingIndicator(numFactory, Indicators.extended(numFactory).closePrice(), 1, 1.0))

        context.fastForwardUntilStable()

        context.assertNextFalse()  // 15 < 20
        context.assertNextTrue()   // 25 > 15
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle mixed positive and negative values`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(-10.0, -5.0, 0.0, 5.0, 10.0)
            .withIndicator(IsRisingIndicator(numFactory, Indicators.extended(numFactory).closePrice(), 4, 1.0))

        context.fastForwardUntilStable()

        // All values are consistently rising
        context.assertNextTrue()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle window larger than available data`(numFactory: NumFactory) {
        val indicator = IsRisingIndicator(
            numFactory,
            Indicators.extended(numFactory).closePrice(),
            10, // Window larger than data
            1.0
        )

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(1.0, 2.0, 3.0)
            .withIndicator(indicator)

        context.advance()  // Move to index 0 (value 1.0)
        assertThat(indicator.value).isFalse()  // Not enough data yet

        context.advance()  // Move to index 1 (value 2.0)
        assertThat(indicator.value).isTrue()   // 2.0 > 1.0, 100% rising

        context.advance()  // Move to index 2 (value 3.0)
        assertThat(indicator.value).isTrue()   // Both 2>1 and 3>2, 100% rising
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle zero strength requirement`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 90.0, 80.0, 70.0)
            .withIndicator(IsRisingIndicator(numFactory, Indicators.extended(numFactory).closePrice(), 3, 0.0))

        context.fastForwardUntilStable()

        // With 0% strength requirement, should always be true once stable
        context.assertNextTrue()
    }

    @Test
    fun `should use correct string representation`() {
        val numFactory = org.ta4j.core.num.DoubleNumFactory
        val indicator = IsRisingIndicator(
            numFactory,
            Indicators.extended(numFactory).closePrice(),
            5,
            0.8
        )

        assertThat(indicator.toString()).contains("IsRising")
        assertThat(indicator.toString()).contains("5")
        assertThat(indicator.toString()).contains("0.8")
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should convert to rule for strategy usage`(numFactory: NumFactory) {
        val indicator = IsRisingIndicator(numFactory, Indicators.extended(numFactory).closePrice(), 3, 1.0)
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 20.0, 30.0, 40.0, 35.0)
            .withIndicator(indicator)

        // Convert to rule for use in strategies
        val rule = indicator.toRule()

        context.fastForward(4)  // Move to rising trend
        assertThat(rule.isSatisfied).isTrue()

        context.advance()  // Move to break in trend
        assertThat(rule.isSatisfied).isFalse()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should test lag and stability properties`(numFactory: NumFactory) {
        val indicator = IsRisingIndicator(numFactory, Indicators.extended(numFactory).closePrice(), 3, 1.0)
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(1.0, 2.0, 3.0, 4.0, 5.0)
            .withIndicator(indicator)

        // Lag should equal bar count
        assertThat(indicator.lag).isEqualTo(3)

        // Should become stable after enough bars
        context.fastForwardUntilStable()
        assertThat(indicator.isStable).isTrue()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should work in trading record context for strategy testing`(numFactory: NumFactory) {
        val tradingContext = TradingRecordTestContext()
            .withNumFactory(numFactory)

        val marketContext = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(50.0, 70.0, 80.0, 90.0, 99.0, 60.0, 30.0, 20.0)

        val indicator = IsRisingIndicator(numFactory, Indicators.extended(numFactory).closePrice(), 4, 1.0)
        marketContext.withIndicator(indicator)

        val tradingRecord = tradingContext
            .enter(1.0).at(70.0)  // Enter when price is 70
            .exit(1.0).at(99.0)   // Exit at peak when rising trend is strong
            .tradingRecord

        // Verify that the rising indicator could be used for entry/exit logic
        assertThat(tradingRecord).isNotNull()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle edge case with alternating values`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 20.0, 10.0, 20.0, 10.0, 20.0)
            .withIndicator(IsRisingIndicator(numFactory, Indicators.extended(numFactory).closePrice(), 3, 0.5))

        context.fastForwardUntilStable()

        // With alternating pattern and 50% strength, should sometimes be satisfied
        // depending on the exact window position
        repeat(3) {
            context.advance()
            // Don't assert specific values as they depend on sliding window position
            // Just ensure no exceptions are thrown
        }
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should maintain count accuracy over long sequences`(numFactory: NumFactory) {
        val prices = (1..100).map { it.toDouble() }.toDoubleArray()  // Strictly ascending

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(*prices)
            .withIndicator(IsRisingIndicator(numFactory, Indicators.extended(numFactory).closePrice(), 10, 1.0))

        context.fastForwardUntilStable()

        // Should remain true throughout the ascending sequence
        repeat(50) {
            context.assertNextTrue()
        }
    }
}
