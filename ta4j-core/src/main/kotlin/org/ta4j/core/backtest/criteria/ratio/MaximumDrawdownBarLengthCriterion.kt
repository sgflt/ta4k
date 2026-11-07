/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.criteria.ratio

import org.ta4j.core.backtest.analysis.EquityPoint
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * Longest drawdown measured in bars: from an equity peak to the lowest point before equity
 * makes a new peak. Zero without drawdown.
 */
class MaximumDrawdownBarLengthCriterion : AbstractEquityCurveCriterion() {

    override fun calculate(curve: List<EquityPoint>, numFactory: NumFactory): Num {
        if (curve.isEmpty()) {
            return numFactory.zero()
        }
        var peakIndex = 0
        var troughIndex = 0
        var longest = 0
        curve.forEachIndexed { index, point ->
            when {
                point.equity > curve[peakIndex].equity -> {
                    peakIndex = index
                    troughIndex = index
                }

                point.equity < curve[troughIndex].equity -> {
                    troughIndex = index
                    longest = maxOf(longest, troughIndex - peakIndex)
                }
            }
        }
        return numFactory.numOf(longest)
    }
}
