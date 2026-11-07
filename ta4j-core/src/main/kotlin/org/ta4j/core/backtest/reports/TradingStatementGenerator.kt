/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.reports

import org.ta4j.core.backtest.strategy.BacktestStrategy

/**
 * Generates a [TradingStatement] based on the provided trading record and
 * bar series.
 */
class TradingStatementGenerator
/**
 * Constructor with new [PerformanceReportGenerator] and new
 * [PositionStatsReportGenerator].
 */ @JvmOverloads constructor(
    private val performanceReportGenerator: PerformanceReportGenerator = PerformanceReportGenerator(),
    private val positionStatsReportGenerator: PositionStatsReportGenerator = PositionStatsReportGenerator(),
) {
    /**
     * Constructor.
     *
     * @param performanceReportGenerator the [PerformanceReportGenerator]
     * @param positionStatsReportGenerator the [PositionStatsReportGenerator]
     */


    fun generate(strategy: BacktestStrategy): TradingStatement {
        val performanceReport = this.performanceReportGenerator.generate(strategy.tradeRecord!!)
        val positionStatsReport = this.positionStatsReportGenerator.generate(strategy.tradeRecord!!)
        return TradingStatement(strategy, positionStatsReport, performanceReport)
    }
}
