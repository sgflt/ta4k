/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.criteria.ratio

import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * Sharpe ratio: mean(excess return) / sample standard deviation of excess returns (N - 1).
 *
 * Zero when there are fewer than two returns or no volatility.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Sharpe_ratio">Sharpe ratio</a>
 */
class SharpeRatioCriterion(
    annualRiskFreeRate: Double = 0.0,
    private val annualization: Annualization = Annualization.ANNUALIZED,
) : AbstractReturnSeriesCriterion(annualRiskFreeRate) {

    override fun ratio(excessReturns: List<Num>, periodsPerYear: Double, numFactory: NumFactory): Num {
        val mean = mean(excessReturns, numFactory)
        val squaredDeviations = excessReturns.fold(numFactory.zero()) { sum, r -> sum + (r - mean) * (r - mean) }
        val deviation = (squaredDeviations / numFactory.numOf(excessReturns.size - 1)).sqrt()
        if (deviation.isZero) {
            return numFactory.zero()
        }
        return scale(mean / deviation, annualization, periodsPerYear, numFactory)
    }
}
