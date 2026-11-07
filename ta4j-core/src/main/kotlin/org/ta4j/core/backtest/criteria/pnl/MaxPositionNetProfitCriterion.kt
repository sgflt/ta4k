/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.criteria.pnl

import org.ta4j.core.num.Num

/**
 * Highest net profit of a single winning position, or zero when there is none.
 */
class MaxPositionNetProfitCriterion : AbstractPositionExtremePnlCriterion() {
    override fun accepts(profit: Num) = profit.isPositive

    override fun isMoreExtreme(candidate: Num, best: Num) = candidate > best
}
