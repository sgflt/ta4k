/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.reports

import org.ta4j.core.num.Num

/**
 * Represents a report that contains performance statistics.
 *
 * @param totalProfitLoss The total PnL.
 * @param totalProfitLossPercentage The total PnL in percent.
 * @param totalProfit The total profit.
 * @param totalLoss The total loss.
 * @param expectedShortfall
 * @param minutesInMarket
 */
@JvmRecord
data class PerformanceReport(
    @JvmField val totalProfitLoss: Num?,
    @JvmField val totalProfitLossPercentage: Num?,
    @JvmField val totalProfit: Num?,
    @JvmField val totalLoss: Num?,
    val averageProfit: Num?,
    val averageLoss: Num?,
    val totalPositions: Num?,
    val valueAtRisk: Num?,
    val maximumDrawdown: Num?,
    val expectedShortfall: Num?,
    val minutesInMarket: Num?,
) {
    override fun toString(): String = """
            totalProfitLoss:            ${totalProfitLoss!!.doubleValue()}
            totalProfitLossPercentage:  ${totalProfitLossPercentage!!.doubleValue()}
            totalProfit:                ${totalProfit!!.doubleValue()}
            totalLoss:                  ${totalLoss!!.doubleValue()}
            averageProfit:              ${averageProfit!!.doubleValue()}
            averageLoss:                ${averageLoss!!.doubleValue()}
            totalPositions:             ${totalPositions!!.intValue()}
            valueAtRisk:                ${valueAtRisk!!.doubleValue()}
            maximumDrawdown:            ${maximumDrawdown!!.doubleValue()}
            expectedShortfall:          ${expectedShortfall!!.doubleValue()}
            minutesInMarket:            ${minutesInMarket!!.intValue()}
        """.trimIndent()
}
