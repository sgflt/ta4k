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
package org.ta4j.core

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.TestUtils.assertNumEquals
import org.ta4j.core.backtest.Position
import org.ta4j.core.backtest.analysis.cost.LinearTransactionCostModel
import org.ta4j.core.num.NumFactory

internal class PositionTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    @DisplayName("New position should start with correct state")
    fun newPositionShouldHaveCorrectState(numFactory: NumFactory) {
        val position = Position(TradeType.BUY, numFactory = numFactory)
        assertThat(position.isNew).isTrue()
        assertThat(position.isOpened).isFalse()
        assertThat(position.isClosed).isFalse()
        assertThat(position.entry).isNull()
        assertThat(position.exit).isNull()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    @DisplayName("Position should transition states correctly")
    fun shouldTransitionStatesCorrectly(numFactory: NumFactory) {
        val context = TradingRecordTestContext()
            .withNumFactory(numFactory)

        context.enter(1.0).at(100.0)
        val modifiedPosition = context.tradingRecord.currentPosition
        assertThat(modifiedPosition.isOpened).isTrue()

        context.exit(1.0).at(110.0)
        assertThat(modifiedPosition.isClosed).isTrue()
        assertThat(context.tradingRecord.currentPosition.isNew).isTrue()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    @DisplayName("Should calculate profit correctly for long position")
    fun shouldCalculateLongPositionProfit(numFactory: NumFactory) {
        val context = TradingRecordTestContext()
            .withNumFactory(numFactory)
            .withTradeType(TradeType.BUY)

        context.enter(1.0).at(100.0)
            .exit(1.0).at(110.0)

        val position = context.tradingRecord.lastPosition!!
        assertNumEquals(numFactory.numOf(10), position.profit)
        assertThat(position.hasProfit()).isTrue()
        assertThat(position.hasLoss()).isFalse()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    @DisplayName("Should calculate profit correctly for short position")
    fun shouldCalculateShortPositionProfit(numFactory: NumFactory) {
        val context = TradingRecordTestContext()
            .withNumFactory(numFactory)
            .withTradeType(TradeType.SELL)

        context.enter(1.0).at(100.0)
            .exit(1.0).at(90.0)

        val position = context.tradingRecord.lastPosition!!
        assertNumEquals(numFactory.numOf(10), position.profit)
        assertThat(position.hasProfit()).isTrue()
        assertThat(position.hasLoss()).isFalse()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    @DisplayName("Should calculate costs with transaction cost model")
    fun shouldCalculateTransactionCosts(numFactory: NumFactory) {
        val context = TradingRecordTestContext()
            .withNumFactory(numFactory)
            .withTransactionCostModel(LinearTransactionCostModel(0.01))

        context.enter(1.0).at(100.0)
            .exit(1.0).at(110.0)

        val position = context.tradingRecord.lastPosition!!
        assertNumEquals(numFactory.numOf(2.1), position.positionCost)
        assertNumEquals(numFactory.numOf(7.9), position.profit)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    @DisplayName("Should handle zero prices")
    fun shouldHandleZeroPrices(numFactory: NumFactory) {
        val context = TradingRecordTestContext()
            .withNumFactory(numFactory)

        context.enter(1.0).at(0.0)
            .exit(1.0).at(0.0)

        val position = context.tradingRecord.lastPosition!!
        assertNumEquals(numFactory.zero(), position.profit)
        assertThat(position.hasProfit()).isFalse()
        assertThat(position.hasLoss()).isFalse()
    }

    @Test
    @DisplayName("Should throw exception when numFactory is null")
    fun shouldThrowExceptionOnNullNumFactory() {
        assertThrows(
            NullPointerException::class.java
        ) { Position(TradeType.BUY, numFactory = null as NumFactory) }
    }
}
