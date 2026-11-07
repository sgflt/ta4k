/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.criteria

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.num.NumFactory

class EnterAndHoldReturnCriterionTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateWithEmpty(numFactory: NumFactory) {
        val marketContext = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices()

        val buyContext = marketContext.toTradingRecordContext()
            .withCriterion(EnterAndHoldReturnCriterion.buy(marketContext.marketEvents.toList()))

        val sellContext = marketContext.toTradingRecordContext()
            .withCriterion(EnterAndHoldReturnCriterion.sell(marketContext.marketEvents.toList()))

        buyContext.assertResults(1.0)
        sellContext.assertResults(1.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateOnlyWithGainPositions(numFactory: NumFactory) {
        val marketContext = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 105.0, 110.0, 100.0, 95.0, 105.0)

        marketContext.toTradingRecordContext()
            .withCriterion(EnterAndHoldReturnCriterion.buy(marketContext.marketEvents.toList()))
            .enter(1.0).at(100.0)
            .exit(1.0).at(110.0)
            .enter(1.0).at(100.0)
            .exit(1.0).at(105.0)
            .assertResults(1.05)

        marketContext.toTradingRecordContext()
            .withCriterion(EnterAndHoldReturnCriterion.sell(marketContext.marketEvents.toList()))
            .enter(1.0).at(100.0)
            .exit(1.0).at(110.0)
            .enter(1.0).at(100.0)
            .exit(1.0).at(105.0)
            .assertResults(0.95)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateOnlyWithLossPositions(numFactory: NumFactory) {
        val marketContext = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 95.0, 100.0, 80.0, 85.0, 70.0)

        marketContext.toTradingRecordContext()
            .withCriterion(EnterAndHoldReturnCriterion.buy(marketContext.marketEvents.toList()))
            .enter(1.0).at(100.0)
            .exit(1.0).at(95.0)
            .enter(1.0).at(100.0)
            .exit(1.0).at(70.0)
            .assertResults(0.7)

        marketContext.toTradingRecordContext()
            .withCriterion(EnterAndHoldReturnCriterion.sell(marketContext.marketEvents.toList()))
            .enter(1.0).at(100.0)
            .exit(1.0).at(95.0)
            .enter(1.0).at(100.0)
            .exit(1.0).at(70.0)
            .assertResults(1.3)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateWithNoPositions(numFactory: NumFactory) {
        val marketContext = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 95.0, 100.0, 80.0, 85.0, 70.0)

        val buyContext = marketContext.toTradingRecordContext()
            .withCriterion(EnterAndHoldReturnCriterion.buy(marketContext.marketEvents.toList()))

        buyContext.assertResults(0.7)

        val sellContext = marketContext.toTradingRecordContext()
            .withCriterion(EnterAndHoldReturnCriterion.sell(marketContext.marketEvents.toList()))

        sellContext.assertResults(1.3)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateWithOnePosition(numFactory: NumFactory) {
        val marketContext = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 105.0, 95.0)

        marketContext.toTradingRecordContext()
            .withCriterion(EnterAndHoldReturnCriterion.buy(marketContext.marketEvents.toList()))
            .enter(1.0).at(105.0)
            .exit(1.0).at(95.0)
            .assertResults(0.95)

        marketContext.toTradingRecordContext()
            .withCriterion(EnterAndHoldReturnCriterion.sell(marketContext.marketEvents.toList()))
            .enter(1.0).at(105.0)
            .exit(1.0).at(95.0)
            .assertResults(1.05)
    }
}
