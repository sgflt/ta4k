/*
 * SPDX-License-Identifier: MIT
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
