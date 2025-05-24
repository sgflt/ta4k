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
package org.ta4j.core.indicators.numeric.momentum

import java.time.Instant
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.TestUtils.assertStable
import org.ta4j.core.TestUtils.assertUnstable
import org.ta4j.core.events.CandleReceived
import org.ta4j.core.events.MarketEvent
import org.ta4j.core.indicators.TimeFrame
import org.ta4j.core.num.NumFactory

class MassIndexIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate mass index with specific test data using 3 and 8 bar counts`(numFactory: NumFactory) {
        // This test replicates the original Java test with specific OHLC data
        // Uses emaBarCount=3 (both single and double EMA) and barCount=8 (Mass Index window)
        val testData = createSpecificTestData()

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(testData)
            .withIndicator(MassIndexIndicator(numFactory, emaBarCount = 3, barCount = 8))

        // Advance to first bar and check initial value
        context.advance()
        context.assertCurrent(1.0) // Index 0: expected 1

        // Fast forward to index 14 and verify expected values (precision to 4 decimal places)
        context.fastForward(14) // Now at index 14
        context.assertCurrent(9.1158) // Index 14: expected 9.1158

        context.assertNext(9.2462) // Index 15: expected 9.2462
        context.assertNext(9.4026) // Index 16: expected 9.4026
        context.assertNext(9.2129) // Index 17: expected 9.2129
        context.assertNext(9.1576) // Index 18: expected 9.1576
        context.assertNext(9.0184) // Index 19: expected 9.0184
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate mass index correctly with proper OHLC data`(numFactory: NumFactory) {
        val candles = createCandlesWithVaryingSpread()

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(candles)
            .withIndicator(MassIndexIndicator(numFactory))

        val massIndex = context.firstNumericIndicator!!

        // Should be unstable initially
        assertUnstable(massIndex)

        // Fast forward to stability
        context.fastForwardUntilStable()
        assertStable(massIndex)

        // Verify that mass index is positive and reasonable
        assert(massIndex.value.isPositive) { "Mass Index should be positive" }
        assert(massIndex.value < numFactory.numOf(100)) { "Mass Index should be reasonable" }
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle custom parameters correctly`(numFactory: NumFactory) {
        val candles = createCandlesWithVaryingSpread()

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(candles)
            .withIndicator(MassIndexIndicator(numFactory, emaBarCount = 5, barCount = 10))

        val massIndex = context.firstNumericIndicator!!

        // Should stabilize faster with smaller parameters
        context.fastForwardUntilStable()
        assertStable(massIndex)

        // Verify reasonable values
        assert(massIndex.value.isPositive) { "Mass Index should be positive" }
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should use EMA of EMA not DoubleEMAIndicator`(numFactory: NumFactory) {
        // This test demonstrates why we can't use DoubleEMAIndicator (DEMA)
        val candles = createCandlesWithVaryingSpread()

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(candles)

        // Create indicators to compare
        val highPrice = org.ta4j.core.api.Indicators.extended(numFactory).highPrice()
        val lowPrice = org.ta4j.core.api.Indicators.extended(numFactory).lowPrice()
        val spread = highPrice.minus(lowPrice)

        val singleEma = spread.ema(9)
        val emaOfEma = singleEma.ema(9)  // What Mass Index uses: EMA(EMA(data))
        val doubleEma = spread.doubleEMA(9) // DEMA formula: 2×EMA - EMA(EMA)

        context.withIndicators(singleEma, emaOfEma, doubleEma)

        // Fast forward to stability
        context.fastForwardUntilStable()

        // These should be different values!
        val emaOfEmaValue = emaOfEma.value
        val doubleEmaValue = doubleEma.value

        // DEMA typically produces different (often higher) values than EMA(EMA)
        assert(emaOfEmaValue != doubleEmaValue) {
            "EMA(EMA) should differ from DoubleEMAIndicator: " +
                    "EMA(EMA)=${emaOfEmaValue}, DEMA=${doubleEmaValue}"
        }

        // For Mass Index, we specifically need the EMA(EMA) approach
        println("EMA(EMA): $emaOfEmaValue")
        println("DEMA: $doubleEmaValue")
        println("Difference: ${(emaOfEmaValue - doubleEmaValue).abs()}")
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle high volatility data correctly`(numFactory: NumFactory) {
        val candles = createHighVolatilityCandles()

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(candles)
            .withIndicator(MassIndexIndicator(numFactory))

        val massIndex = context.firstNumericIndicator!!

        context.fastForwardUntilStable()
        assertStable(massIndex)

        // High volatility should result in higher mass index values
        assert(massIndex.value.isPositive) { "Mass Index should be positive" }
        assert(massIndex.value > numFactory.numOf(15)) { "High volatility should produce higher Mass Index" }
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle low volatility data correctly`(numFactory: NumFactory) {
        val candles = createLowVolatilityCandles()

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(candles)
            .withIndicator(MassIndexIndicator(numFactory))

        val massIndex = context.firstNumericIndicator!!

        context.fastForwardUntilStable()
        assertStable(massIndex)

        // Low volatility should result in lower mass index values
        assert(massIndex.value.isPositive) { "Mass Index should be positive" }
        assert(massIndex.value < numFactory.numOf(30)) { "Low volatility should produce lower Mass Index" }
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should have correct lag calculation`(numFactory: NumFactory) {
        val emaBarCount = 9
        val barCount = 25
        val massIndex = MassIndexIndicator(numFactory, emaBarCount, barCount)

        val expectedLag = emaBarCount * 2 + barCount - 1
        assert(massIndex.lag == expectedLag) {
            "Expected lag: $expectedLag, but was: ${massIndex.lag}"
        }
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle edge case with zero spread`(numFactory: NumFactory) {
        val candles = createZeroSpreadCandles()

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(candles)
            .withIndicator(MassIndexIndicator(numFactory))

        val massIndex = context.firstNumericIndicator!!

        context.fastForwardUntilStable()
        assertStable(massIndex)

        // With zero spread, the mass index should be zero or very close to zero
        assert(massIndex.value.isZero || massIndex.value.abs() < numFactory.numOf(0.0001)) {
            "Mass Index should be zero with zero spread, but was: ${massIndex.value}"
        }
    }

    @Test
    fun `should validate constructor parameters`() {
        val numFactory = org.ta4j.core.num.DoubleNumFactory

        // Test invalid emaBarCount
        org.junit.jupiter.api.assertThrows<IllegalArgumentException> {
            MassIndexIndicator(numFactory, emaBarCount = 0, barCount = 25)
        }

        org.junit.jupiter.api.assertThrows<IllegalArgumentException> {
            MassIndexIndicator(numFactory, emaBarCount = -1, barCount = 25)
        }

        // Test invalid barCount
        org.junit.jupiter.api.assertThrows<IllegalArgumentException> {
            MassIndexIndicator(numFactory, emaBarCount = 9, barCount = 0)
        }

        org.junit.jupiter.api.assertThrows<IllegalArgumentException> {
            MassIndexIndicator(numFactory, emaBarCount = 9, barCount = -1)
        }

        // Test valid parameters
        val validIndicator = MassIndexIndicator(numFactory, emaBarCount = 5, barCount = 10)
        assert(validIndicator.lag == 19) // 5 * 2 + 10 - 1
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should maintain running sum correctly during window sliding`(numFactory: NumFactory) {
        val candles = mutableListOf<MarketEvent>()
        var time = Instant.ofEpochSecond(0)

        // Create exactly enough candles to test window sliding
        for (i in 0 until 50) {
            val basePrice = 100.0
            val spread = 2.0 // Constant spread for predictable ratios
            candles.add(
                CandleReceived(
                    TimeFrame.DAY,
                    time,
                    time.plusSeconds(86400),
                    basePrice, // open
                    basePrice + spread, // high
                    basePrice - spread, // low
                    basePrice, // close
                    1000.0 // volume
                )
            )
            time = time.plusSeconds(86400)
        }

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(candles)
            .withIndicator(MassIndexIndicator(numFactory, emaBarCount = 3, barCount = 5))

        val massIndex = context.firstNumericIndicator!!

        context.fastForwardUntilStable()
        val stableValue1 = massIndex.value

        // Advance a few more bars and check that values are still reasonable
        context.fastForward(5)
        val stableValue2 = massIndex.value

        // Values should be positive and relatively stable for constant spread data
        assert(stableValue1.isPositive) { "First stable value should be positive" }
        assert(stableValue2.isPositive) { "Second stable value should be positive" }

        // Values should be close to each other for constant volatility
        val difference = (stableValue2 - stableValue1).abs()
        assert(difference < numFactory.numOf(2.0)) {
            "Values should be relatively stable for constant spread data"
        }
    }

    private fun createSpecificTestData(): List<MarketEvent> {
        // This replicates the exact OHLC data from the original Java test
        val ohlcData = listOf(
            listOf(44.98, 45.17, 44.96, 45.05),  // Bar 0
            listOf(45.05, 45.15, 44.99, 45.10),  // Bar 1
            listOf(45.11, 45.32, 45.11, 45.19),  // Bar 2
            listOf(45.19, 45.25, 45.04, 45.14),  // Bar 3
            listOf(45.12, 45.20, 45.10, 45.15),  // Bar 4
            listOf(45.15, 45.20, 45.10, 45.14),  // Bar 5
            listOf(45.13, 45.16, 45.07, 45.10),  // Bar 6
            listOf(45.12, 45.22, 45.10, 45.15),  // Bar 7
            listOf(45.15, 45.27, 45.14, 45.22),  // Bar 8
            listOf(45.24, 45.45, 45.20, 45.43),  // Bar 9
            listOf(45.43, 45.50, 45.39, 45.44),  // Bar 10
            listOf(45.43, 45.60, 45.35, 45.55),  // Bar 11
            listOf(45.58, 45.61, 45.39, 45.55),  // Bar 12
            listOf(45.45, 45.55, 44.80, 45.01),  // Bar 13
            listOf(45.03, 45.04, 44.17, 44.23),  // Bar 14
            listOf(44.23, 44.29, 43.81, 43.95),  // Bar 15
            listOf(43.91, 43.99, 43.08, 43.08),  // Bar 16
            listOf(43.07, 43.65, 43.06, 43.55),  // Bar 17
            listOf(43.56, 43.99, 43.53, 43.95),  // Bar 18
            listOf(43.93, 44.58, 43.93, 44.47)   // Bar 19
        )

        val candles = mutableListOf<MarketEvent>()
        var time = Instant.ofEpochSecond(0)

        ohlcData.forEach { (open, high, low, close) ->
            candles.add(
                CandleReceived(
                    TimeFrame.DAY,
                    time,
                    time.plusSeconds(86400),
                    open,  // open
                    high,  // high
                    low,   // low
                    close, // close
                    1000.0 // volume (not used in Mass Index)
                )
            )
            time = time.plusSeconds(86400)
        }

        return candles
    }

    private fun createCandlesWithVaryingSpread(): List<MarketEvent> {
        val candles = mutableListOf<MarketEvent>()
        var time = Instant.ofEpochSecond(0)

        // Create 50 candles with varying high-low spreads
        for (i in 0 until 50) {
            val basePrice = 100.0 + i * 0.5
            val spread = 1.0 + (i % 7) * 0.3 // Varying spread from 1.0 to 2.8
            candles.add(
                CandleReceived(
                    TimeFrame.DAY,
                    time,
                    time.plusSeconds(86400),
                    basePrice, // open
                    basePrice + spread, // high
                    basePrice - spread, // low
                    basePrice + 0.2, // close
                    1000.0 + i * 10 // volume
                )
            )
            time = time.plusSeconds(86400)
        }
        return candles
    }

    private fun createHighVolatilityCandles(): List<MarketEvent> {
        val candles = mutableListOf<MarketEvent>()
        var time = Instant.ofEpochSecond(0)

        // Create 50 candles with high volatility (large spreads)
        for (i in 0 until 50) {
            val basePrice = 100.0 + i * 0.1
            val spread = 5.0 + (i % 5) * 2.0 // Large spreads from 5.0 to 13.0
            candles.add(
                CandleReceived(
                    TimeFrame.DAY,
                    time,
                    time.plusSeconds(86400),
                    basePrice, // open
                    basePrice + spread, // high
                    basePrice - spread, // low
                    basePrice + 0.5, // close
                    1000.0 + i * 10 // volume
                )
            )
            time = time.plusSeconds(86400)
        }
        return candles
    }

    private fun createLowVolatilityCandles(): List<MarketEvent> {
        val candles = mutableListOf<MarketEvent>()
        var time = Instant.ofEpochSecond(0)

        // Create 50 candles with low volatility (small spreads)
        for (i in 0 until 50) {
            val basePrice = 100.0 + i * 0.05
            val spread = 0.1 + (i % 3) * 0.05 // Small spreads from 0.1 to 0.2
            candles.add(
                CandleReceived(
                    TimeFrame.DAY,
                    time,
                    time.plusSeconds(86400),
                    basePrice, // open
                    basePrice + spread, // high
                    basePrice - spread, // low
                    basePrice + 0.02, // close
                    1000.0 + i * 5 // volume
                )
            )
            time = time.plusSeconds(86400)
        }
        return candles
    }

    private fun createZeroSpreadCandles(): List<MarketEvent> {
        val candles = mutableListOf<MarketEvent>()
        var time = Instant.ofEpochSecond(0)

        // Create 50 candles with zero spread (all OHLC values the same)
        for (i in 0 until 50) {
            val price = 100.0 + i * 0.1
            candles.add(
                CandleReceived(
                    TimeFrame.DAY,
                    time,
                    time.plusSeconds(86400),
                    price, // open
                    price, // high (same as open)
                    price, // low (same as open)
                    price, // close (same as open)
                    1000.0 + i * 10 // volume
                )
            )
            time = time.plusSeconds(86400)
        }
        return candles
    }
}
