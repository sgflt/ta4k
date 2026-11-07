/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.reports

import org.ta4j.core.num.Num

/**
 * Represents a report with statistics for positions.
 *
 * @param profitCount The number of positions making a profit.
 * @param lossCount The number of positions making a loss.
 * @param breakEvenCount The number of positions with a break even.
 * @param winningPositionsRatio
 * @param losingPositionsRatio
 */
@JvmRecord
data class PositionStatsReport(
    val totalPositions: Num?,
    val profitCount: Num?,
    val lossCount: Num?,
    val winningPositionsRatio: Num?,
    val losingPositionsRatio: Num?,
    val breakEvenCount: Num?,
) {
    override fun toString(): String = """
           total positions: $totalPositions
           profits: $profitCount
           losses: $lossCount
           profitsRatio: $winningPositionsRatio
           lossesRatio: $losingPositionsRatio
           breakEvens: $breakEvenCount
       """.trimIndent()
}
