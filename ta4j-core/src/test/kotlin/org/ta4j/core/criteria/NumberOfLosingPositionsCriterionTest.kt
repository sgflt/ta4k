/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.criteria

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.TradeType
import org.ta4j.core.backtest.criteria.NumberOfLosingPositionsCriterion
import org.ta4j.core.num.NumFactory

class NumberOfLosingPositionsCriterionTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateWithNoPositions(factory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(factory)
            .withCandlePrices(100.0, 105.0, 110.0, 100.0, 95.0, 105.0)
            .toTradingRecordContext()
            .withCriterion(NumberOfLosingPositionsCriterion())

        context.assertResults(0.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateWithTwoLongPositions(factory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(factory)
            .withCandlePrices(100.0, 105.0, 110.0, 99.0, 95.0, 91.0)
            .toTradingRecordContext()
            .withTradeType(TradeType.BUY)
            .withCriterion(NumberOfLosingPositionsCriterion())

        context.enter(1.0).asap()
            .exit(1.0).after(3)
            .enter(1.0).asap()
            .exit(1.0).asap()

        context.assertResults(2.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateWithOneLongPosition(factory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(factory)
            .withCandlePrices(100.0, 105.0, 110.0, 100.0, 95.0, 105.0)
            .toTradingRecordContext()
            .withTradeType(TradeType.BUY)
            .withCriterion(NumberOfLosingPositionsCriterion())

        context.enter(1.0).asap()
            .exit(1.0).after(4)

        context.assertResults(1.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateWithTwoShortPositions(factory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(factory)
            .withCandlePrices(100.0, 105.0, 110.0, 100.0, 95.0, 105.0)
            .toTradingRecordContext()
            .withTradeType(TradeType.SELL)
            .withCriterion(NumberOfLosingPositionsCriterion())

        context.enter(1.0).asap()
            .exit(1.0).asap()
            .enter(1.0).after(3)
            .exit(1.0).asap()

        context.assertResults(2.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateOneOpenPositionShouldReturnZero(factory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(factory)
            .withCandlePrices(100.0, 105.0, 110.0)
            .toTradingRecordContext()
            .withCriterion(NumberOfLosingPositionsCriterion())

        context.enter(1.0).asap()
        context.assertResults(0.0)
    }
}