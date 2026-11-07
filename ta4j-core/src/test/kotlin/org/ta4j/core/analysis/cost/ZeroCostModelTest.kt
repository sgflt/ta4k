/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.analysis.cost

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.TestUtils.assertNumEquals
import org.ta4j.core.TradingRecordTestContext
import org.ta4j.core.backtest.analysis.cost.ZeroCostModel
import org.ta4j.core.num.NumFactory

class ZeroCostModelTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculatePerPosition(numFactory: NumFactory) {
        val context = TradingRecordTestContext()
            .withNumFactory(numFactory)
            .withTransactionCostModel(ZeroCostModel)
            .withHoldingCostModel(ZeroCostModel)

        context
            .enter(1.0).at(100.0)
            .exit(1.0).at(110.0)

        val position = context.tradingRecord.positions.first()
        val cost = position.positionCost

        assertNumEquals(numFactory.zero(), cost)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculatePerPrice(numFactory: NumFactory) {
        val model = ZeroCostModel
        val cost = model.calculate(numFactory.numOf(100), numFactory.numOf(1))
        assertNumEquals(numFactory.zero(), cost)
    }
}