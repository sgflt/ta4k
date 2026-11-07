/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.criteria

import org.junit.jupiter.api.Test
import org.ta4j.core.TradeType
import org.ta4j.core.TradingRecordTestContext
import org.ta4j.core.backtest.criteria.NumberOfBreakEvenPositionsCriterion

class NumberOfBreakEvenPositionsCriterionTest {

    @Test
    fun calculateWithNoPositions() {
        TradingRecordTestContext()
            .withCriterion(NumberOfBreakEvenPositionsCriterion())
            .assertResults(0.0)
    }

    @Test
    fun calculateWithTwoLongPositions() {
        TradingRecordTestContext()
            .withCriterion(NumberOfBreakEvenPositionsCriterion())
            .enter(1.0).at(100.0)
            .exit(1.0).at(100.0)
            .enter(1.0).at(105.0)
            .exit(1.0).at(105.0)
            .assertResults(2.0)
    }

    @Test
    fun calculateWithOneLongPosition() {
        TradingRecordTestContext()
            .withCriterion(NumberOfBreakEvenPositionsCriterion())
            .enter(1.0).at(100.0)
            .exit(1.0).at(100.0)
            .assertResults(1.0)
    }

    @Test
    fun calculateWithTwoShortPositions() {
        TradingRecordTestContext()
            .withTradeType(TradeType.SELL)
            .withCriterion(NumberOfBreakEvenPositionsCriterion())
            .enter(1.0).at(100.0)
            .exit(1.0).at(100.0)
            .enter(1.0).at(105.0)
            .exit(1.0).at(105.0)
            .assertResults(2.0)
    }

    @Test
    fun calculateOneOpenPositionShouldReturnZero() {
        TradingRecordTestContext()
            .withCriterion(NumberOfBreakEvenPositionsCriterion())
            .enter(1.0).at(100.0)
            .assertResults(0.0)
    }
}