/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.analysis.cost

import java.time.Instant
import kotlin.random.Random
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.TestUtils.assertNumEquals
import org.ta4j.core.TradeType
import org.ta4j.core.backtest.Position
import org.ta4j.core.backtest.analysis.cost.FixedTransactionCostModel
import org.ta4j.core.backtest.analysis.cost.ZeroCostModel
import org.ta4j.core.num.DoubleNum
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

class FixedTransactionCostModelTest {

    companion object {
        private val PRICE: Num = DoubleNum.valueOf(100)
        private val AMOUNT: Num = DoubleNum.valueOf(5)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculatePerPositionWhenPositionIsOpen(numFactory: NumFactory) {
        val positionTrades = 1
        val feePerTrade = Random.nextDouble()
        val model = FixedTransactionCostModel(feePerTrade)

        val position = Position(TradeType.BUY, ZeroCostModel, model, numFactory)
        position.operate(Instant.now(), PRICE, AMOUNT)
        val cost = model.calculate(position)

        assertNumEquals(DoubleNum.valueOf(feePerTrade * positionTrades), cost)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculatePerPositionWhenPositionIsClosed(numFactory: NumFactory) {
        val positionTrades = 2
        val feePerTrade = Random.nextDouble()
        val model = FixedTransactionCostModel(feePerTrade)

        val position = Position(TradeType.BUY, model, model, numFactory)
        position.operate(Instant.now(), PRICE, AMOUNT)
        position.operate(Instant.now(), PRICE, AMOUNT)
        val cost = model.calculate(position, Random.nextInt())

        assertNumEquals(DoubleNum.valueOf(feePerTrade * positionTrades), cost)
    }

    @Test
    fun calculatePerPrice() {
        val feePerTrade = Random.nextDouble()
        val model = FixedTransactionCostModel(feePerTrade)
        val cost = model.calculate(PRICE, AMOUNT)

        assertNumEquals(cost, DoubleNum.valueOf(feePerTrade))
    }
}
