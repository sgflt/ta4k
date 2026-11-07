/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.criteria

import java.time.temporal.ChronoUnit
import kotlin.math.pow
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.TradeType
import org.ta4j.core.backtest.criteria.AverageReturnPerBarCriterion
import org.ta4j.core.num.NumFactory

class AverageReturnPerBarCriterionTest {

    private val context = MarketEventTestContext()

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateOnlyWithGainPositions(numFactory: NumFactory) {
        val tradingContext = context
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 105.0, 110.0, 100.0, 95.0, 105.0)
            .toTradingRecordContext()
            .withCriterion(AverageReturnPerBarCriterion(numFactory, ChronoUnit.DAYS))
            .enter(1.0).asap()
            .exit(1.0).after(2)
            .enter(1.0).asap()
            .exit(1.0).after(2)

        tradingContext.assertResults((110.0 / 100.0 * 105.0 / 100.0).pow(1.0 / 4.0))
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateWithASimplePosition(numFactory: NumFactory) {
        val tradingContext = context
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 105.0, 110.0)
            .toTradingRecordContext()
            .withCriterion(AverageReturnPerBarCriterion(numFactory, ChronoUnit.DAYS))
            .enter(1.0).asap()
            .exit(1.0).after(2)

        val expectedReturn = numFactory.numOf(110.0).div(numFactory.numOf(100.0))
            .pow(numFactory.numOf(1.0 / 2.0))
        tradingContext.assertResults(expectedReturn.doubleValue())
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateOnlyWithLossPositions(numFactory: NumFactory) {
        val tradingContext = context
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 95.0, 100.0, 80.0, 85.0, 70.0)
            .toTradingRecordContext()
            .withCriterion(AverageReturnPerBarCriterion(numFactory, ChronoUnit.DAYS))
            .enter(1.0).asap()
            .exit(1.0).asap()
            .enter(1.0).asap()
            .exit(1.0).after(3)

        val expectedReturn = numFactory.numOf(95.0 / 100.0 * 70.0 / 100.0).pow(numFactory.numOf(1.0 / 4.0))
        tradingContext.assertResults(expectedReturn.doubleValue())
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateWithLosingShortPositions(numFactory: NumFactory) {
        val tradingContext = context
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 105.0, 90.0)
            .toTradingRecordContext()
            .withTradeType(TradeType.SELL)
            .withCriterion(AverageReturnPerBarCriterion(numFactory, ChronoUnit.DAYS))
            .enter(1.0).asap()
            .exit(1.0).after(2)

        val expectedReturn = numFactory.numOf((100.0 - 90.0) / 100.0 + 1.0).pow(numFactory.numOf(1.0 / 2.0))
        tradingContext.assertResults(expectedReturn.doubleValue())
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateWithNoBarsShouldReturnOne(numFactory: NumFactory) {
        val tradingContext = context
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 95.0, 100.0, 80.0, 85.0, 70.0)
            .toTradingRecordContext()
            .withCriterion(AverageReturnPerBarCriterion(numFactory, ChronoUnit.DAYS))

        tradingContext.assertResults(1.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateWithOnePosition(numFactory: NumFactory) {
        val tradingContext = context
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 105.0)
            .toTradingRecordContext()
            .withCriterion(AverageReturnPerBarCriterion(numFactory, ChronoUnit.DAYS))
            .enter(1.0).asap()
            .exit(1.0).asap()

        val expectedReturn = numFactory.numOf(105.0 / 100.0).pow(numFactory.numOf(1.0))
        tradingContext.assertResults(expectedReturn.doubleValue())
    }
}
