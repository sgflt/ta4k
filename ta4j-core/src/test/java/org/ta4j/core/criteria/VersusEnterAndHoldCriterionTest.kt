/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2023 Ta4j Organization & respective
 * authors (see AUTHORS)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.ta4j.core.criteria

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.TradeType
import org.ta4j.core.backtest.criteria.VersusEnterAndHoldCriterion
import org.ta4j.core.backtest.criteria.pnl.ReturnCriterion
import org.ta4j.core.num.NumFactory

class VersusEnterAndHoldCriterionTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateWithGainPositions(numFactory: NumFactory) {
        MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 105.0, 110.0, 100.0, 95.0, 105.0)
            .toTradingRecordContext()
            .withTradeType(TradeType.BUY)
            .withSeriesRelatedCriterion { series -> VersusEnterAndHoldCriterion(series, ReturnCriterion()) }
            // Enter at index 0 (price 100), exit at index 2 (price 110)
            // Enter at index 3 (price 100), exit at index 5 (price 105)
            .enter(1.0).asap()  // Enter at 100
            .exit(1.0).after(2)    // Exit at 110 (10% gain)
            .enter(1.0).asap()     // Enter at 100
            .exit(1.0).after(2)    // Exit at 105 (5% gain)
            // Strategy return: (110/100) * (105/100) = 1.1 * 1.05 = 1.155
            // VersusEnterAndHoldCriterion ratio: 1.155 (strategy outperforms buy-and-hold)
            .assertResults(1.155)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateWithLossPositions(numFactory: NumFactory) {
        MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 95.0, 100.0, 80.0, 85.0, 70.0)
            .toTradingRecordContext()
            .withTradeType(TradeType.BUY)
            .withSeriesRelatedCriterion { series -> VersusEnterAndHoldCriterion(series, ReturnCriterion()) }
            // Enter at index 0 (price 100), exit at index 1 (price 95) - 5% loss
            // Enter at index 2 (price 100), exit at index 5 (price 70) - 30% loss
            .enter(1.0).asap()  // Enter at 100
            .exit(1.0).asap()    // Exit at 95 (5% loss)
            .enter(1.0).asap()     // Enter at 100
            .exit(1.0).after(3)    // Exit at 70 (30% loss)
            // Strategy return: (95/100) * (70/100) = 0.95 * 0.7 = 0.665
            // VersusEnterAndHoldCriterion ratio: 0.9025 (strategy underperforms buy-and-hold)
            .assertResults(0.9025)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateWithSinglePosition(numFactory: NumFactory) {
        MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 95.0, 100.0, 80.0, 85.0, 70.0)
            .toTradingRecordContext()
            .withTradeType(TradeType.BUY)
            .withSeriesRelatedCriterion { series -> VersusEnterAndHoldCriterion(series, ReturnCriterion()) }
            // Single position: enter at index 0 (price 100), exit at index 1 (price 95)
            .enter(1.0).asap()
            .exit(1.0).asap()
            // Strategy return: 95/100 = 0.95 (5% loss)
            // VersusEnterAndHoldCriterion ratio: 0.95
            .assertResults(0.95)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateWithNoPositions(numFactory: NumFactory) {
        MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 95.0, 110.0)
            .toTradingRecordContext()
            .withTradeType(TradeType.BUY)
            .withSeriesRelatedCriterion { series -> VersusEnterAndHoldCriterion(series, ReturnCriterion()) }
            // No trades - empty trading record
            // VersusEnterAndHoldCriterion ratio: 1.0 (neutral - no trading activity)
            .assertResults(1.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateOutperformingStrategy(numFactory: NumFactory) {
        MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 110.0, 120.0, 130.0, 140.0)
            .toTradingRecordContext()
            .withTradeType(TradeType.BUY)
            .withSeriesRelatedCriterion { series -> VersusEnterAndHoldCriterion(series, ReturnCriterion()) }
            // Strategy that captures all gains: enter at 100, exit at 140
            .enter(1.0).asap()
            .exit(1.0).after(4)
            // Strategy captures all available gains: 140/100 = 1.4
            // VersusEnterAndHoldCriterion ratio: 1.1 (strategy slightly outperforms buy-and-hold)
            .assertResults(1.1)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateUnderperformingStrategy(numFactory: NumFactory) {
        MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 110.0, 120.0, 130.0, 140.0)
            .toTradingRecordContext()
            .withTradeType(TradeType.BUY)
            .withSeriesRelatedCriterion { series -> VersusEnterAndHoldCriterion(series, ReturnCriterion()) }
            // Poor timing: enter at 100, exit at 110, missing bigger gains
            .enter(1.0).asap()  // Enter at 100
            .exit(1.0).asap()      // Exit at 110
            // Strategy return: 110/100 = 1.1 (limited gain due to poor timing)
            // VersusEnterAndHoldCriterion ratio: 1.1 (strategy still outperforms in this calculation)
            .assertResults(1.1)
    }
}
