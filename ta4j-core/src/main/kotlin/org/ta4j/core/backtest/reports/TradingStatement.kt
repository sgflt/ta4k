/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.reports

import org.ta4j.core.backtest.strategy.BacktestStrategy

/**
 * Represents a trading statement report containing position and performance
 * statistics.
 */
@JvmRecord
data class TradingStatement(
    val strategy: BacktestStrategy,
    val positionStatsReport: PositionStatsReport,
    @JvmField val performanceReport: PerformanceReport,
) {
    override fun toString(): String = """
            
            Strategy: ${strategy.name}
            =======================
            Positions:
            $positionStatsReport
            =======================
            Performance:
            $performanceReport
            
        """.trimIndent()
}
