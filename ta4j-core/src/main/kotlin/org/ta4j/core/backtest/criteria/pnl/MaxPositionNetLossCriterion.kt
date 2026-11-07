/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.criteria.pnl

import org.ta4j.core.num.Num

/**
 * Largest net loss of a single losing position (a negative number), or zero when there is none.
 */
class MaxPositionNetLossCriterion : AbstractPositionExtremePnlCriterion() {
    override fun accepts(profit: Num) = profit.isNegative

    override fun isMoreExtreme(candidate: Num, best: Num) = candidate < best
}
