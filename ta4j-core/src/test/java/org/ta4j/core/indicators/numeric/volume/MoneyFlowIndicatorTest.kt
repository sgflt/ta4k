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
package org.ta4j.core.indicators.numeric.volume

import java.time.Instant
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.TestUtils.assertNumEquals
import org.ta4j.core.TestUtils.assertStable
import org.ta4j.core.TestUtils.assertUnstable
import org.ta4j.core.events.CandleReceived
import org.ta4j.core.indicators.TimeFrame
import org.ta4j.core.num.DoubleNumFactory
import org.ta4j.core.num.NaN
import org.ta4j.core.num.NumFactory

class MoneyFlowIndexIndicatorTest {

    @Test
    fun constructorWithInvalidBarCount() {
        assertThrows<IllegalArgumentException> {
            MoneyFlowIndexIndicator(DoubleNumFactory, 0)
        }

        assertThrows<IllegalArgumentException> {
            MoneyFlowIndexIndicator(DoubleNumFactory, -1)
        }
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun shouldReturnNaNForUnstableBars(numFactory: NumFactory) {
        val mfi = MoneyFlowIndexIndicator(numFactory, 14)
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(mfi)
            .withCandlePrices(
                50.0, 51.0, 49.0, 52.0, 48.0, 53.0, 47.0, 54.0,
                46.0, 55.0, 45.0, 56.0, 44.0, 57.0, 43.0, 58.0
            )

        // Should be unstable for first barCount-1 bars
        repeat(13) {
            assertUnstable(mfi)
            assertNumEquals(NaN, mfi.value)
            context.advance()
        }

        // After barCount bars, should become stable
        context.advance()
        assertStable(mfi)
        assertThat(mfi.value).isNotEqualTo(NaN)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun shouldCalculateMFIWithRisingPrices(numFactory: NumFactory) {
        val mfi = MoneyFlowIndexIndicator(numFactory, 3)
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(mfi)
            .withCandlePrices(10.0, 11.0, 12.0, 13.0)

        // First 3 bars to establish baseline
        context.fastForward(3)
        assertStable(mfi)

        // With consistently rising prices, MFI should be high (close to 100)
        val mfiValue = mfi.value.doubleValue()
        assertThat(mfiValue).isGreaterThan(80.0) // Should indicate overbought condition
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun shouldCalculateMFIWithFallingPrices(numFactory: NumFactory) {
        val mfi = MoneyFlowIndexIndicator(numFactory, 3)
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(mfi)
            .withCandlePrices(13.0, 12.0, 11.0, 10.0)

        // First 3 bars to establish baseline
        context.fastForward(3)
        assertStable(mfi)

        // With consistently falling prices, MFI should be low (close to 0)
        val mfiValue = mfi.value.doubleValue()
        assertThat(mfiValue).isLessThan(20.0) // Should indicate oversold condition
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun shouldCalculateMFIWithMixedPrices(numFactory: NumFactory) {
        val mfi = MoneyFlowIndexIndicator(numFactory, 4)
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(mfi)
            .withCandlePrices(50.0, 52.0, 48.0, 51.0, 49.0)

        // Fast forward to get stable indicator
        context.fastForward(4)
        assertStable(mfi)

        // With mixed price movement, MFI should be in the middle range
        val mfiValue = mfi.value.doubleValue()
        assertThat(mfiValue).isBetween(20.0, 80.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun shouldHandleEqualPrices(numFactory: NumFactory) {
        val mfi = MoneyFlowIndexIndicator(numFactory, 3)
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(mfi)
            .withCandlePrices(50.0, 50.0, 50.0, 50.0)

        // Fast forward to get stable indicator
        context.fastForward(3)
        assertStable(mfi)

        // With equal prices (no price movement), MFI should be neutral (50)
        assertNumEquals(50.0, mfi.value)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun shouldHandleAllPositiveFlow(numFactory: NumFactory) {
        val mfi = MoneyFlowIndexIndicator(numFactory, 3)
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(mfi)
            // Start with equal price, then all rising
            .withCandlePrices(50.0, 51.0, 52.0, 53.0)

        // Fast forward to get stable indicator
        context.fastForward(3)
        assertStable(mfi)

        // With only positive money flow, MFI should be 100
        context.assertCurrent(99.98115341123257)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun shouldCalculateCorrectLag(numFactory: NumFactory) {
        val barCount = 14
        val mfi = MoneyFlowIndexIndicator(numFactory, barCount)

        assertThat(mfi.lag).isEqualTo(barCount)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun shouldHaveCorrectToString(numFactory: NumFactory) {
        val barCount = 14
        val mfi = MoneyFlowIndexIndicator(numFactory, barCount)

        assertThat(mfi.toString()).startsWith("MFI(14) =>")
    }

    @Test
    fun shouldIntegrateWithTradingRecordContext() {
        val context = MarketEventTestContext()
            .withCandlePrices(
                100.0, 102.0, 101.0, 103.0, 99.0,  // Mixed movement
                104.0, 105.0, 106.0, 107.0, 108.0, // Rising trend
                106.0, 104.0, 102.0, 100.0, 98.0   // Falling trend
            )

        val mfi = MoneyFlowIndexIndicator(context.barSeries.numFactory, 5)
        context.withIndicator(mfi, "MFI")

        // Fast forward until the MFI is stable
        context.fastForwardUntilStable()

        // Verify MFI is working and producing values in valid range
        assertStable(mfi)
        val mfiValue = mfi.value.doubleValue()
        assertThat(mfiValue).isBetween(0.0, 100.0)

        // Test with TradingRecordTestContext
        val tradingContext = context.toTradingRecordContext()

        // Enter and exit positions based on MFI signals
        tradingContext
            .enter(1.0).asap()
            .forwardTime(3)
            .exit(1.0).asap()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun shouldCalculateRealisticMFIValues(numFactory: NumFactory) {
        // Test with realistic price and volume data
        val mfi = MoneyFlowIndexIndicator(numFactory, 14)
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(mfi)
            // Simulate realistic price movements
            .withCandlePrices(
                100.0, 101.5, 99.8, 102.3, 98.7, 103.1, 97.9, 104.2,
                96.5, 105.0, 95.8, 106.1, 94.9, 107.3, 93.8, 108.5, 92.1
            )

        // Let the indicator stabilize
        context.fastForward(14)
        assertStable(mfi)

        // MFI should be within valid range [0, 100]
        val mfiValue = mfi.value.doubleValue()
        assertThat(mfiValue).isBetween(0.0, 100.0)

        // Continue with one more bar and verify it's still in range
        context.advance() // Process next bar
        assertThat(mfi.value.doubleValue()).isBetween(0.0, 100.0)

        // Process final bar and verify range is still maintained
        context.advance()
        assertThat(mfi.value.doubleValue()).isBetween(0.0, 100.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun givenBarCount_whenGetValueForIndexWithinBarCount_thenReturnNaN(numFactory: NumFactory) {
        val mfi = MoneyFlowIndexIndicator(numFactory, 5)
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(mfi)
            .withCandlePrices(10.0, 10.0, 10.0, 10.0, 10.0, 9.0, 11.0)

        // First 5 values should be NaN (unstable period)
        repeat(5) {
            assertUnstable(mfi)
            assertNumEquals(NaN, mfi.value)
            context.advance()
        }

        // 6th value should not be NaN (stable)
        context.advance()
        assertStable(mfi)
        assertThat(mfi.value).isNotEqualTo(NaN)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun givenBarCountOf1_whenGetValue_thenReturnEdgeCaseCorrectedValue(numFactory: NumFactory) {
        val mfi = MoneyFlowIndexIndicator(numFactory, 1)
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(mfi)
            .withCandlePrices(10.0, 9.0, 10.0, 11.0, 12.0, 11.0, 11.0)

        // First value should be NaN
        assertUnstable(mfi)
        assertNumEquals(NaN, mfi.value)
        context.advance()

        // Subsequent values should match expected calculations
        context.assertNext(1.21951219)
        context.assertNext(99.00990099009901)
        context.assertNext(99.18032786885246)
        context.assertNext(99.3103448275862)
        context.assertNext(0.8196721311475414)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun givenBarCountOf3_whenGetValue_thenReturnCorrectValue(numFactory: NumFactory) {
        val mfi = MoneyFlowIndexIndicator(numFactory, 3)
        val marketEvents = listOf(
            createCandleEvent(0, 10.0, 10.0, 10.0, 10.0, 0.0),
            createCandleEvent(1, 9.0, 9.0, 9.0, 9.0, 10.0),
            createCandleEvent(2, 10.0, 10.0, 10.0, 10.0, 10.0),
            createCandleEvent(3, 11.0, 11.0, 11.0, 11.0, 10.0),
            createCandleEvent(4, 12.0, 12.0, 12.0, 12.0, 10.0),
            createCandleEvent(5, 11.0, 11.0, 11.0, 11.0, 10.0),
            createCandleEvent(6, 12.0, 12.0, 12.0, 12.0, 10.0),
            createCandleEvent(7, 9.0, 9.0, 9.0, 9.0, 10.0)
        )

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(mfi)
            .withMarketEvents(marketEvents)

        // First 3 values should be NaN (unstable period)
        repeat(3) {
            assertUnstable(mfi)
            assertNumEquals(NaN, mfi.value)
            context.advance()
        }

        // Test specific expected values
        context.assertNext(70.0)
        context.assertNext(99.69788519637463)
        context.assertNext(67.64705882352942)
        context.assertNext(68.57142857142857)
        context.assertNext(37.5)
    }

    private fun createCandleEvent(
        index: Long,
        open: Double,
        high: Double,
        low: Double,
        close: Double,
        volume: Double,
    ) = CandleReceived(
        timeFrame = TimeFrame.DAY,
        beginTime = Instant.EPOCH.plusSeconds(index * 86400),
        endTime = Instant.EPOCH.plusSeconds((index + 1) * 86400),
        openPrice = open,
        highPrice = high,
        lowPrice = low,
        closePrice = close,
        volume = volume
    )
}
