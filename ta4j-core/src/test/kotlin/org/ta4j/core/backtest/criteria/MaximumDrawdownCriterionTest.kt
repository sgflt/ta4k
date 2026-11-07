/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.criteria

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.TradeType
import org.ta4j.core.num.NumFactory

class MaximumDrawdownCriterionTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateWithNoTrades(numFactory: NumFactory) {
        val marketContext = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 105.0)
        marketContext.toTradingRecordContext()
            .withCriterion(MaximumDrawdownCriterion(numFactory))
            .assertResults(0.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateWithOnlyGains(numFactory: NumFactory) {
        MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(2.0, 3.0, 3.0, 5.0, 10.0, 20.0)
            .toTradingRecordContext()
            .withTradeType(TradeType.BUY)
            .withCriterion(MaximumDrawdownCriterion(numFactory))
            .enter(1.0).after(1)
            .exit(1.0).after(1)
            .enter(1.0).after(2)  // skip 3.0
            .exit(1.0).after(2)    // skip 10.0
            .assertResults(0.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateWithGainsAndLosses(numFactory: NumFactory) {
        MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(1.0, 2.0, 20.0, 1.0, 8.0, 3.0)
            .toTradingRecordContext()
            .withTradeType(TradeType.BUY)
            .withCriterion(MaximumDrawdownCriterion(numFactory))
            .enter(1.0).after(1)
            .exit(1.0).after(5)
            .assertResults((20.0 - 1.0) / 20.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateWithSimpleTrades(numFactory: NumFactory) {
        MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(1.0, 10.0, 10.0, 5.0, 5.0, 6.0, 6.0, 1.0)
            .toTradingRecordContext()
            .withTradeType(TradeType.BUY)
            .withCriterion(MaximumDrawdownCriterion(numFactory))
            .enter(1.0).after(1)
            .exit(1.0).after(1)
            .enter(1.0).after(1)
            .exit(1.0).after(1)
            .enter(1.0).after(1)
            .exit(1.0).after(1)
            .enter(1.0).after(1)
            .exit(1.0).after(1)
            .assertResults((6.0 - 1.0) / 6.0) // maximum drawdown within last trade
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateHODL(numFactory: NumFactory) {
        MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(1.0, 10.0, 10.0, 5.0, 5.0, 6.0, 6.0, 1.0)
            .toTradingRecordContext()
            .withTradeType(TradeType.BUY)
            .withCriterion(MaximumDrawdownCriterion(numFactory))
            .enter(1.0).after(1)
            .exit(1.0).after(7)
            .assertResults(0.9)  // 10.0 -> 1.0 which is -90 %
    }
}
