/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.reports

import org.ta4j.core.backtest.TradingRecord

/**
 * Generic interface for generating trading reports.
 *
 * @param <T> type of report to be generated
</T> */
fun interface ReportGenerator<T> {
    /**
     * Generates a report based on the `tradingRecord`.
     *
     * @param tradingRecord the trading record (not null)
     *
     * @return generated report
     */
    fun generate(tradingRecord: TradingRecord): T
}
