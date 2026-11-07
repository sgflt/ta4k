/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.reports

import java.time.temporal.ChronoUnit
import org.ta4j.core.backtest.TradingRecord
import org.ta4j.core.backtest.criteria.ExpectedShortfallCriterion
import org.ta4j.core.backtest.criteria.NumberOfPositionsCriterion
import org.ta4j.core.backtest.criteria.TimeInTradeCriterion
import org.ta4j.core.backtest.criteria.ValueAtRiskCriterion
import org.ta4j.core.backtest.criteria.pnl.AverageLossCriterion
import org.ta4j.core.backtest.criteria.pnl.AverageProfitCriterion
import org.ta4j.core.backtest.criteria.pnl.LossCriterion
import org.ta4j.core.backtest.criteria.pnl.ProfitCriterion
import org.ta4j.core.backtest.criteria.pnl.ProfitLossCriterion
import org.ta4j.core.backtest.criteria.pnl.ProfitLossPercentageCriterion

/**
 * Generates a [PerformanceReport] based on the provided trading record
 * and bar series.
 */
class PerformanceReportGenerator : ReportGenerator<PerformanceReport> {
    override fun generate(tradingRecord: TradingRecord): PerformanceReport {
        val pnl = ProfitLossCriterion().calculate(tradingRecord)
        val pnlPercentage = ProfitLossPercentageCriterion().calculate(tradingRecord)
        val netProfit = ProfitCriterion(false).calculate(tradingRecord)
        val averageProfit = AverageProfitCriterion().calculate(tradingRecord)
        val averageLoss = AverageLossCriterion().calculate(tradingRecord)
        val netLoss = LossCriterion(false).calculate(tradingRecord)
        val numberOfPositions = NumberOfPositionsCriterion().calculate(tradingRecord)
        val valueAtRisk =
            ValueAtRiskCriterion(tradingRecord.numFactory, 0.95).calculate(tradingRecord)
        val expectedShortfall =
            ExpectedShortfallCriterion(tradingRecord.numFactory, 0.95).calculate(tradingRecord)
        val minutesInMarket = TimeInTradeCriterion(ChronoUnit.MINUTES).calculate(tradingRecord)
        return PerformanceReport(
            pnl,
            pnlPercentage,
            netProfit,
            netLoss,
            averageProfit,
            averageLoss,
            numberOfPositions,
            valueAtRisk,
            tradingRecord.maximumDrawdown,
            expectedShortfall,
            minutesInMarket
        )
    }
}
