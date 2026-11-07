/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.analysis.cost

import org.ta4j.core.backtest.analysis.cost.ZeroCostModel.ZERO_FEE_PER_TRADE

/**
 * With this cost model there are no trading costs.
 */
object ZeroCostModel : FixedTransactionCostModel(ZERO_FEE_PER_TRADE) {
    private const val ZERO_FEE_PER_TRADE = 0.0
}
