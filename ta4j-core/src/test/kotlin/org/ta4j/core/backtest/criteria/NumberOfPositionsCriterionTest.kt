/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.criteria

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.num.NumFactory

class NumberOfPositionsCriterionTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateWithNoPositions(numFactory: NumFactory) {
        val marketContext = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 105.0, 110.0, 100.0, 95.0, 105.0)

        val context = marketContext.toTradingRecordContext()
            .withCriterion(NumberOfPositionsCriterion())

        context.assertResults(0.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateWithTwoPositions(numFactory: NumFactory) {
        val marketContext = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 105.0, 110.0, 100.0, 95.0, 105.0)

        val context = marketContext.toTradingRecordContext()
            .withCriterion(NumberOfPositionsCriterion())

        context
            .enter(1.0).asap()
            .exit(1.0).after(2)
            .enter(1.0).after(1)
            .exit(1.0).after(2)

        context.assertResults(2.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateWithOnePosition(numFactory: NumFactory) {
        val marketContext = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 105.0, 110.0, 100.0, 95.0, 105.0)

        val context = marketContext.toTradingRecordContext()
            .withCriterion(NumberOfPositionsCriterion())

        context
            .enter(1.0).asap()
            .exit(1.0).after(2)

        context.assertResults(1.0)
    }
}