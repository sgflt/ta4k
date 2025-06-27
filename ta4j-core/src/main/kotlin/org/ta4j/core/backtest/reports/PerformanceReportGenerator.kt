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

import java.time.temporal.ChronoUnit
import org.ta4j.core.backtest.TradingRecord
import org.ta4j.core.backtest.criteria.ExpectedShortfallCriterion
import org.ta4j.core.backtest.criteria.NumberOfPositionsCriterion
import org.ta4j.core.backtest.criteria.TimeInTradeCriterion
import org.ta4j.core.backtest.criteria.ValueAtRiskCriterion
import org.ta4j.core.backtest.criteria.pnl.AverageLossCriterion
import org.ta4j.core.backtest.criteria.pnl.AverageProfitCriterion
import org.ta4j.core.backtest.criteria.pnl.NetLossCriterion
import org.ta4j.core.backtest.criteria.pnl.NetProfitCriterion
import org.ta4j.core.backtest.criteria.pnl.NetProfitLossRatioCriterion
import org.ta4j.core.backtest.criteria.pnl.ProfitLossCriterion
import org.ta4j.core.num.NumFactoryProvider.defaultNumFactory

/**
 * Generates a [PerformanceReport] based on the provided trading record
 * and bar series.
 */
class PerformanceReportGenerator : ReportGenerator<PerformanceReport> {
    override fun generate(tradingRecord: TradingRecord): PerformanceReport {
        val pnl = ProfitLossCriterion().calculate(tradingRecord)
        val pnlPercentage = NetProfitLossRatioCriterion().calculate(tradingRecord)
        val netProfit = NetProfitCriterion().calculate(tradingRecord)
        val averageProfit = AverageProfitCriterion().calculate(tradingRecord)
        val averageLoss = AverageLossCriterion().calculate(tradingRecord)
        val netLoss = NetLossCriterion().calculate(tradingRecord)
        val numberOfPositions = NumberOfPositionsCriterion().calculate(tradingRecord)
        val valueAtRisk =
            ValueAtRiskCriterion(defaultNumFactory, 0.95).calculate(tradingRecord)
        val expectedShortfall =
            ExpectedShortfallCriterion(defaultNumFactory, 0.95).calculate(tradingRecord)
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
