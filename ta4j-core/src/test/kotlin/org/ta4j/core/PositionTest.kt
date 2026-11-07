/*
 * SPDX-License-Identifier: MIT
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
    @DisplayName("Exit trade of a long position is a sell with costs subtracted")
    fun exitTradeOfLongPositionIsSell(numFactory: NumFactory) {
        val context = TradingRecordTestContext()
            .withNumFactory(numFactory)
            .withTradeType(TradeType.BUY)
            .withTransactionCostModel(LinearTransactionCostModel(0.01))

        context.enter(1.0).at(100.0)
            .exit(1.0).at(110.0)

        val position = context.tradingRecord.lastPosition!!
        assertThat(position.entry!!.type).isEqualTo(TradeType.BUY)
        assertThat(position.exit!!.type).isEqualTo(TradeType.SELL)
        assertNumEquals(numFactory.numOf(101), position.entry!!.netPrice)
        assertNumEquals(numFactory.numOf(108.9), position.exit!!.netPrice)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    @DisplayName("Exit trade of a short position is a buy with costs added")
    fun exitTradeOfShortPositionIsBuy(numFactory: NumFactory) {
        val context = TradingRecordTestContext()
            .withNumFactory(numFactory)
            .withTradeType(TradeType.SELL)
            .withTransactionCostModel(LinearTransactionCostModel(0.01))

        context.enter(1.0).at(100.0)
            .exit(1.0).at(110.0)

        val position = context.tradingRecord.lastPosition!!
        assertThat(position.entry!!.type).isEqualTo(TradeType.SELL)
        assertThat(position.exit!!.type).isEqualTo(TradeType.BUY)
        assertNumEquals(numFactory.numOf(99), position.entry!!.netPrice)
        assertNumEquals(numFactory.numOf(111.1), position.exit!!.netPrice)
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
