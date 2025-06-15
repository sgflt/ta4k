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
package org.ta4j.core.analysis.cost

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.TestUtils.assertNumEquals
import org.ta4j.core.TradeType
import org.ta4j.core.TradingRecordTestContext
import org.ta4j.core.backtest.Position
import org.ta4j.core.backtest.analysis.cost.CostModel
import org.ta4j.core.backtest.analysis.cost.LinearTransactionCostModel
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

class LinearTransactionCostModelTest {

    private lateinit var transactionModel: CostModel

    @BeforeEach
    fun setUp() {
        transactionModel = LinearTransactionCostModel(0.01)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateSingleTradeCost(numFactory: NumFactory) {
        val price = numFactory.numOf(100)
        val amount = numFactory.numOf(2)
        val cost = transactionModel.calculate(price, amount)

        assertNumEquals(numFactory.numOf(2), cost)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateBuyPosition(numFactory: NumFactory) {
        val context = TradingRecordTestContext()
            .withNumFactory(numFactory)
            .withTransactionCostModel(transactionModel)
            .withTradeType(TradeType.BUY)

        context.enter(1.0).at(100.0)
            .exit(1.0).at(110.0)

        val position = context.tradingRecord.positions.first()
        val costFromBuy = position.entry!!.cost
        val costFromSell = position.exit!!.cost
        val costsFromModel = transactionModel.calculate(position)

        assertNumEquals(costsFromModel, costFromBuy + costFromSell)
        assertNumEquals(costsFromModel, numFactory.numOf(2.1))
        assertNumEquals(costFromBuy, numFactory.numOf(1))
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateSellPosition(numFactory: NumFactory) {
        val context = TradingRecordTestContext()
            .withNumFactory(numFactory)
            .withTransactionCostModel(transactionModel)
            .withTradeType(TradeType.SELL)

        context.enter(1.0).at(100.0)
            .exit(1.0).at(110.0)

        val position = context.tradingRecord.positions.first()
        val costFromSell = position.entry!!.cost
        val costFromBuy = position.exit!!.cost
        val costsFromModel = transactionModel.calculate(position)

        assertNumEquals(costsFromModel, costFromSell + costFromBuy)
        assertNumEquals(costsFromModel, numFactory.numOf(2.1))
        assertNumEquals(costFromSell, numFactory.numOf(1))
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateOpenPosition(numFactory: NumFactory) {
        val context = TradingRecordTestContext()
            .withNumFactory(numFactory)
            .withTransactionCostModel(transactionModel)

        context.enter(1.0).at(100.0)

        val position = context.tradingRecord.currentPosition
        val costsFromModel = transactionModel.calculate(position)

        assertNumEquals(costsFromModel, numFactory.numOf(1))
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun testStrategyExecution(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(1.0, 2.0, 3.0, 4.0)
            .toTradingRecordContext()
            .withTransactionCostModel(LinearTransactionCostModel(0.0026))

        context
            .enter(25.0).after(1)
            .exit(25.0).after(1)
            .enter(25.0).after(1)
            .exit(25.0).after(1)

        val firstPositionBuy = numFactory.one()
            .plus(numFactory.one() * numFactory.numOf(0.0026))
        val firstPositionSell = numFactory.two()
            .minus(numFactory.two() * numFactory.numOf(0.0026))
        val firstPositionProfit = (firstPositionSell - firstPositionBuy) * numFactory.numOf(25)

        val secondPositionBuy = numFactory.three()
            .plus(numFactory.three() * numFactory.numOf(0.0026))
        val secondPositionSell = numFactory.numOf(4)
            .minus(numFactory.numOf(4) * numFactory.numOf(0.0026))
        val secondPositionProfit = (secondPositionSell - secondPositionBuy) * numFactory.numOf(25)

        val overallProfit = firstPositionProfit + secondPositionProfit

        assertNumEquals(
            overallProfit, context.tradingRecord.positions
                .map { it.profit }
                .fold(numFactory.zero()) { acc: Num, profit: Num -> acc + profit }
        )
    }
}