/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2025 Ta4j Organization & respective authors (see AUTHORS)
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
package org.ta4j.core.backtest.reports

import org.ta4j.core.backtest.TradingRecord
import org.ta4j.core.backtest.criteria.NumberOfBreakEvenPositionsCriterion
import org.ta4j.core.backtest.criteria.NumberOfLosingPositionsCriterion
import org.ta4j.core.backtest.criteria.NumberOfPositionsCriterion
import org.ta4j.core.backtest.criteria.NumberOfWinningPositionsCriterion
import org.ta4j.core.backtest.criteria.PositionsRatioCriterion.Companion.losingPositionsRatioCriterion
import org.ta4j.core.backtest.criteria.PositionsRatioCriterion.Companion.winningPositionsRatioCriterion

/**
 * Generates a [PositionStatsReport] based on provided trading record and
 * bar series.
 */
class PositionStatsReportGenerator : ReportGenerator<PositionStatsReport?> {
    override fun generate(tradingRecord: TradingRecord): PositionStatsReport {
        val totalPositions = NumberOfPositionsCriterion().calculate(tradingRecord)
        val winningPositions = NumberOfWinningPositionsCriterion().calculate(tradingRecord)
        val losingPositions = NumberOfLosingPositionsCriterion().calculate(tradingRecord)
        val breakEvenPositions = NumberOfBreakEvenPositionsCriterion().calculate(tradingRecord)
        val losingPositionsRatio = losingPositionsRatioCriterion().calculate(tradingRecord)
        val winningPositionsRatio = winningPositionsRatioCriterion().calculate(tradingRecord)
        return PositionStatsReport(
            totalPositions,
            winningPositions,
            losingPositions,
            winningPositionsRatio,
            losingPositionsRatio,
            breakEvenPositions
        )
    }
}
