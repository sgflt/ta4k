/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.criteria.pnl

import org.ta4j.core.num.Num

/**
 * Highest total net profit of a streak of consecutive winning positions.
 */
class MaxConsecutiveProfitCriterion : AbstractConsecutivePnlCriterion() {
    override fun accepts(profit: Num) = profit.isPositive

    override fun preferForStreak(candidate: Num, best: Num) = candidate > best
}
