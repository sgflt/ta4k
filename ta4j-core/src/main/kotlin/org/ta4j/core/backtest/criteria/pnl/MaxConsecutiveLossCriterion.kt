/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.criteria.pnl

import org.ta4j.core.num.Num

/**
 * Largest total net loss of a streak of consecutive losing positions (a negative number).
 */
class MaxConsecutiveLossCriterion : AbstractConsecutivePnlCriterion() {
    override fun accepts(profit: Num) = profit.isNegative

    override fun preferForStreak(candidate: Num, best: Num) = candidate < best
}
