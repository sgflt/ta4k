/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.criteria

import java.time.temporal.ChronoUnit
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.TradeType
import org.ta4j.core.num.NumFactory

class TimeInTradeCriterionTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    @DisplayName("Should calculate zero for no positions")
    fun calculateWithNoPositions(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .toTradingRecordContext()
            .withCriterion(TimeInTradeCriterion.minutes())

        context.assertResults(0.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    @DisplayName("Should calculate correct time for two positions")
    fun calculateWithTwoPositions(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandleDuration(ChronoUnit.MINUTES)
            .withCandlePrices(100.0, 0.0, 110.0, 100.0, 0.0, 0.0, 105.0)
            .toTradingRecordContext()
            .withCriterion(TimeInTradeCriterion.minutes())
            .withTradeType(TradeType.BUY)

        context
            .enter(100.0).after(1)
            .exit(100.0).after(2)
            .enter(100.0).after(1)
            .exit(100.0).after(3)

        context.assertResults(5.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    @DisplayName("Should calculate correct time for one position")
    fun calculateWithOnePosition(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withCandleDuration(ChronoUnit.MINUTES)
            .withCandlePrices(100.0, 0.0, 0.0, 70.0)
            .withNumFactory(numFactory)
            .toTradingRecordContext()
            .withCriterion(TimeInTradeCriterion.minutes())
            .withTradeType(TradeType.BUY)

        context
            .enter(100.0).after(1)
            .exit(100.0).after(3)

        context.assertResults(3.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    @DisplayName("Should return zero for open position")
    fun calculateOneOpenPositionShouldReturnZero(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withCandleDuration(ChronoUnit.MINUTES)
            .withCandlePrices(100.0, 0.0, 0.0, 70.0)
            .withNumFactory(numFactory)
            .toTradingRecordContext()
            .withCriterion(TimeInTradeCriterion.minutes())
            .withTradeType(TradeType.BUY)

        context
            .enter(100.0).after(1)
            .forwardTime(3)

        context.assertResults(0.0)
    }
}
