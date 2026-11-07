/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.criteria

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.TradeType
import org.ta4j.core.backtest.criteria.AnalysisCriterion.PositionFilter
import org.ta4j.core.backtest.criteria.PositionsRatioCriterion
import org.ta4j.core.num.NumFactory

class PositionsRatioCriterionTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateWinningRatio(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 95.0, 102.0, 105.0, 97.0, 113.0)

        context.toTradingRecordContext()
            .withCriterion(PositionsRatioCriterion(PositionFilter.PROFIT))
            .enter(1.0).asap()
            .exit(1.0).asap()
            .enter(1.0).asap()
            .exit(1.0).asap()
            .enter(1.0).asap()
            .exit(1.0).asap()
            .assertResults(2.0 / 3)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateLosingRatio(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 95.0, 102.0, 105.0, 97.0, 113.0)

        context.toTradingRecordContext()
            .withCriterion(PositionsRatioCriterion(PositionFilter.LOSS))
            .enter(1.0).asap()
            .exit(1.0).asap()
            .enter(1.0).asap()
            .exit(1.0).asap()
            .enter(1.0).asap()
            .exit(1.0).asap()
            .assertResults(1.0 / 3)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateShortPositions(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 95.0, 102.0, 105.0, 97.0, 113.0)

        context.toTradingRecordContext()
            .withTradeType(TradeType.SELL)
            .withCriterion(PositionsRatioCriterion(PositionFilter.PROFIT))
            .enter(1.0).asap()
            .exit(1.0).asap()
            .enter(1.0).asap()
            .exit(1.0).asap().assertResults(0.5)
    }
}