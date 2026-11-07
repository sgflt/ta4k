/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.criteria.ratio

import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * Sortino ratio: mean(excess return) / downside deviation, where downside deviation is the
 * root mean square of negative excess returns over all N returns.
 *
 * Zero when there is no downside.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Sortino_ratio">Sortino ratio</a>
 */
class SortinoRatioCriterion(
    annualRiskFreeRate: Double = 0.0,
    private val annualization: Annualization = Annualization.ANNUALIZED,
) : AbstractReturnSeriesCriterion(annualRiskFreeRate) {

    override fun ratio(excessReturns: List<Num>, periodsPerYear: Double, numFactory: NumFactory): Num {
        val zero = numFactory.zero()
        val downsideSquares = excessReturns.fold(zero) { sum, r -> if (r.isNegative) sum + r * r else sum }
        val downsideDeviation = (downsideSquares / numFactory.numOf(excessReturns.size)).sqrt()
        if (downsideDeviation.isZero) {
            return zero
        }
        return scale(mean(excessReturns, numFactory) / downsideDeviation, annualization, periodsPerYear, numFactory)
    }
}
