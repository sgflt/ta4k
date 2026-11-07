/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.criteria.helpers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.TradingRecordTestContext
import org.ta4j.core.backtest.criteria.helpers.AverageCriterion
import org.ta4j.core.backtest.criteria.pnl.ProfitLossCriterion
import org.ta4j.core.num.NumFactory

internal class AverageCriterionTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateStandardErrorPnL(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 105.0, 110.0, 100.0, 95.0, 105.0)
            .toTradingRecordContext()

        context.enter(1.0).asap()
            .exit(1.0).after(2) // +10
            .enter(1.0).asap()
            .exit(1.0).after(2) // +5

        val criterion = AverageCriterion(ProfitLossCriterion())
        assertEquals(7.5, criterion.calculate(context.tradingRecord).doubleValue())
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun testCalculateOneOpenPositionShouldReturnZero(numFactory: NumFactory) {
        val context = TradingRecordTestContext(
            MarketEventTestContext().withNumFactory(numFactory).withDefaultMarketEvents()
        )

        context.enter(1.0).at(2.0)

        val criterion = AverageCriterion(ProfitLossCriterion())
        assertEquals(0.0, criterion.calculate(context.tradingRecord).doubleValue())
    }
}