/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2024 Ta4j Organization & respective
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
package org.ta4j.core.analysis

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.TestUtils.assertNumEquals
import org.ta4j.core.TradeType
import org.ta4j.core.TradingRecordTestContext
import org.ta4j.core.backtest.analysis.Returns
import org.ta4j.core.num.NumFactory
import java.time.Instant
import java.time.temporal.ChronoUnit

class ReturnsTest {

    private lateinit var context: MarketEventTestContext
    private lateinit var tradingContext: TradingRecordTestContext
    private lateinit var startTime: Instant

    @BeforeEach
    fun setUp() {
        startTime = Instant.parse("2024-01-01T10:00:00Z")
        context = MarketEventTestContext()
            .withCandleDuration(ChronoUnit.MINUTES)
            .withStartTime(startTime)
            .withCandlePrices(100.0, 110.0, 105.0, 115.0, 120.0, 118.0, 125.0, 122.0, 130.0, 135.0)

        tradingContext = context.toTradingRecordContext()
    }

    @Nested
    @DisplayName("ARITHMETIC Returns Tests")
    inner class ArithmeticReturnsTest {

        @Test
        @DisplayName("Should calculate correct arithmetic returns for long position")
        fun shouldCalculateArithmeticReturnsForLongPosition() {
            // Given
            tradingContext
                .enter(1.0).asap()
                .exit(1.0).after(4)

            // When
            val returns = Returns(
                context.barSeries.numFactory,
                tradingContext.tradingRecord,
                Returns.ReturnType.ARITHMETIC
            )

            // Then
            assertNumEquals(0.00, returns.getValue(startTime.plus(2, ChronoUnit.MINUTES)))
            assertNumEquals(0.10, returns.getValue(startTime.plus(3, ChronoUnit.MINUTES)))
            assertNumEquals(-0.0455, returns.getValue(startTime.plus(4, ChronoUnit.MINUTES)))
            assertNumEquals(0.0952, returns.getValue(startTime.plus(5, ChronoUnit.MINUTES)))
            assertNumEquals(0.0435, returns.getValue(startTime.plus(6, ChronoUnit.MINUTES)))
        }

        @Test
        @DisplayName("Should calculate correct arithmetic returns for short position")
        fun shouldCalculateArithmeticReturnsForShortPosition() {
            // Given
            tradingContext
                .withTradeType(TradeType.SELL)
                .enter(1.0).asap()
                .exit(1.0).after(4)

            // When
            val returns = Returns(
                context.barSeries.numFactory,
                tradingContext.tradingRecord,
                Returns.ReturnType.ARITHMETIC
            )

            // Then
            assertNumEquals(0.00, returns.getValue(startTime.plus(2, ChronoUnit.MINUTES)))
            assertNumEquals(-0.10, returns.getValue(startTime.plus(3, ChronoUnit.MINUTES)))
            assertNumEquals(0.0455, returns.getValue(startTime.plus(4, ChronoUnit.MINUTES)))
            assertNumEquals(-0.0952, returns.getValue(startTime.plus(5, ChronoUnit.MINUTES)))
            assertNumEquals(-0.0435, returns.getValue(startTime.plus(6, ChronoUnit.MINUTES)))
        }
    }

    @Nested
    @DisplayName("LOG Returns Tests")
    inner class LogReturnsTest {

        @Test
        @DisplayName("Should calculate correct log returns for long position")
        fun shouldCalculateLogReturnsForLongPosition() {
            // Given
            tradingContext
                .enter(1.0).asap()
                .exit(1.0).after(4)

            // When
            val returns = Returns(
                context.barSeries.numFactory,
                tradingContext.tradingRecord,
                Returns.ReturnType.LOG
            )

            // Then
            assertNumEquals(0.00, returns.getValue(startTime.plus(2, ChronoUnit.MINUTES)))
            assertNumEquals(0.0953, returns.getValue(startTime.plus(3, ChronoUnit.MINUTES)))
            assertNumEquals(-0.0465, returns.getValue(startTime.plus(4, ChronoUnit.MINUTES)))
            assertNumEquals(0.0909, returns.getValue(startTime.plus(5, ChronoUnit.MINUTES)))
            assertNumEquals(0.0426, returns.getValue(startTime.plus(6, ChronoUnit.MINUTES)))
        }

        @Test
        @DisplayName("Should calculate correct log returns for short position")
        fun shouldCalculateLogReturnsForShortPosition() {
            // Given
            tradingContext
                .withTradeType(TradeType.SELL)
                .enter(1.0).asap()
                .exit(1.0).after(4)

            // When
            val returns = Returns(
                context.barSeries.numFactory,
                tradingContext.tradingRecord,
                Returns.ReturnType.LOG
            )

            // Then
            assertNumEquals(0.00, returns.getValue(startTime.plus(2, ChronoUnit.MINUTES)))
            assertNumEquals(-0.0953, returns.getValue(startTime.plus(3, ChronoUnit.MINUTES)))
            assertNumEquals(0.0465, returns.getValue(startTime.plus(4, ChronoUnit.MINUTES)))
            assertNumEquals(-0.0909, returns.getValue(startTime.plus(5, ChronoUnit.MINUTES)))
            assertNumEquals(-0.0426, returns.getValue(startTime.plus(6, ChronoUnit.MINUTES)))
        }
    }

    @Nested
    @DisplayName("Multiple Positions Tests")
    inner class MultiplePositionsTest {

        @ParameterizedTest
        @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
        @DisplayName("Should handle gaps between trades correctly")
        fun shouldHandleGapsBetweenTrades(numFactory: NumFactory) {
            // Given
            val testContext = MarketEventTestContext()
                .withCandleDuration(ChronoUnit.MINUTES)
                .withNumFactory(numFactory)
                .withStartTime(startTime)
                .withCandlePrices(100.0, 110.0, 105.0, 115.0, 120.0, 118.0, 125.0, 122.0, 130.0, 135.0)
                .withNumFactory(numFactory)
                .toTradingRecordContext()
                // First position
                .enter(1.0).asap()
                .exit(1.0).after(2)
                // gap 3 bars without opened position
                .enter(1.0).after(3)
                .exit(1.0).after(2)

            // When
            val returns = Returns(
                testContext.barSeries.numFactory,
                testContext.tradingRecord,
                Returns.ReturnType.ARITHMETIC
            )

            // Then
            // First position returns
            assertNumEquals(0.10, returns.getValue(startTime.plus(3, ChronoUnit.MINUTES)))
            assertNumEquals(-0.0455, returns.getValue(startTime.plus(4, ChronoUnit.MINUTES)))

            // Gap period
            assertNumEquals(0.0, returns.getValue(startTime.plus(5, ChronoUnit.MINUTES)))
            assertNumEquals(0.0, returns.getValue(startTime.plus(6, ChronoUnit.MINUTES)))
            assertNumEquals(0.0, returns.getValue(startTime.plus(7, ChronoUnit.MINUTES)))

            // Second position returns
            assertNumEquals(0.0593, returns.getValue(startTime.plus(8, ChronoUnit.MINUTES)))
            assertNumEquals(-0.024, returns.getValue(startTime.plus(9, ChronoUnit.MINUTES)))

            // After last trade
            assertNumEquals(0.0, returns.getValue(startTime.plus(10, ChronoUnit.MINUTES)))
            assertNumEquals(0.0, returns.getValue(startTime.plus(11, ChronoUnit.MINUTES)))
        }

        @ParameterizedTest
        @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
        @DisplayName("Should return zero for times before first trade")
        fun shouldReturnZeroBeforeFirstTrade(numFactory: NumFactory) {
            // Given
            tradingContext
                .withNumFactory(numFactory)
                .enter(1.0).at(118.0)
                .exit(1.0).at(122.0)

            // When
            val returns = Returns(
                context.barSeries.numFactory,
                tradingContext.tradingRecord,
                Returns.ReturnType.ARITHMETIC
            )

            // Then
            assertNumEquals(0.0, returns.getValue(startTime))
            assertNumEquals(0.0, returns.getValue(startTime.plus(1, ChronoUnit.MINUTES)))
            assertNumEquals(0.0, returns.getValue(startTime.plus(2, ChronoUnit.MINUTES)))
            assertNumEquals(0.0, returns.getValue(startTime.plus(3, ChronoUnit.MINUTES)))
            assertNumEquals(0.0, returns.getValue(startTime.plus(4, ChronoUnit.MINUTES)))
        }
    }
}