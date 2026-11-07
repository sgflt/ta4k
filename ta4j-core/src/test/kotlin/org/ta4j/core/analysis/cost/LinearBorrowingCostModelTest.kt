/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.analysis.cost

import java.time.temporal.ChronoUnit
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.TestUtils.assertNumEquals
import org.ta4j.core.TradeType
import org.ta4j.core.TradingRecordTestContext
import org.ta4j.core.backtest.analysis.cost.CostModel
import org.ta4j.core.backtest.analysis.cost.LinearBorrowingCostModel
import org.ta4j.core.num.NumFactory

class LinearBorrowingCostModelTest {

    private val borrowingModel: CostModel = LinearBorrowingCostModel(0.01)
    private lateinit var context: TradingRecordTestContext

    @BeforeEach
    fun setUp() {
        context = TradingRecordTestContext()
            .withHoldingCostModel(borrowingModel)
            .withResolution(ChronoUnit.DAYS)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateZeroTest(numFactory: NumFactory) {
        context.withNumFactory(numFactory)

        val price = numFactory.numOf(100)
        val amount = numFactory.numOf(2)
        val cost = borrowingModel.calculate(price, amount)

        assertNumEquals(numFactory.zero(), cost)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateBuyPosition(numFactory: NumFactory) {
        context.withNumFactory(numFactory)

        context
            .withTradeType(TradeType.BUY)
            .enter(1.0).at(100.0)
            .exit(1.0).at(110.0)

        val position = context.tradingRecord.positions.first()

        val costsFromPosition = position.holdingCost
        val costsFromModel = borrowingModel.calculate(position)

        assertNumEquals(costsFromModel, costsFromPosition)
        assertNumEquals(numFactory.one(), costsFromModel)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateSellPosition(numFactory: NumFactory) {
        context.withNumFactory(numFactory)

        context
            .withTradeType(TradeType.SELL)
            .enter(1.0).at(100.0)
            .forwardTime(2)
            .exit(1.0).at(110.0)

        val position = context.tradingRecord.positions.first()

        val costsFromPosition = position.holdingCost
        val costsFromModel = borrowingModel.calculate(position)

        assertNumEquals(costsFromModel, costsFromPosition)
        assertNumEquals(numFactory.numOf(3), costsFromModel)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateOpenSellPosition(numFactory: NumFactory) {
        context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandleDuration(ChronoUnit.DAYS)
            .withCandlePrices(100.0, 100.0, 100.0, 100.0, 100.0)
            .toTradingRecordContext()
            .withHoldingCostModel(borrowingModel)
            .withResolution(ChronoUnit.DAYS)

        context
            .withTradeType(TradeType.SELL)
            .enter(1.0).asap()
            .forwardTime(4)

        val position = context.tradingRecord.currentPosition

        val costsFromPosition = position.holdingCost
        val costsFromModel = borrowingModel.calculate(position)

        assertNumEquals(costsFromModel, costsFromPosition)
        assertNumEquals(numFactory.numOf(4.0), costsFromModel)
    }
}
