/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.criteria.ratio

import java.time.Duration
import kotlin.math.pow
import org.ta4j.core.backtest.analysis.EquityPoint
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * Base for ratios of per-bar returns (Sharpe, Sortino, Omega).
 *
 * Builds the per-bar returns of the equity curve, subtracts the per-bar risk-free rate derived from
 * [annualRiskFreeRate] and hands the excess returns to [ratio]. Bars per year are estimated from the
 * median time between equity points, so gaps (weekends, sessions) do not distort it.
 *
 * @param annualRiskFreeRate annual rate, e.g. 0.05 for 5 %
 */
abstract class AbstractReturnSeriesCriterion(
    private val annualRiskFreeRate: Double = 0.0,
) : AbstractEquityCurveCriterion() {

    /**
     * @param excessReturns per-bar returns minus the per-bar risk-free rate, at least two
     * @param periodsPerYear estimated number of bars per year
     */
    protected abstract fun ratio(excessReturns: List<Num>, periodsPerYear: Double, numFactory: NumFactory): Num

    final override fun calculate(curve: List<EquityPoint>, numFactory: NumFactory): Num {
        if (curve.size < 3) {
            return numFactory.zero()
        }
        val periodsPerYear = periodsPerYear(curve)
        val riskFreePerPeriod = numFactory.numOf((1.0 + annualRiskFreeRate).pow(1.0 / periodsPerYear) - 1.0)
        val one = numFactory.one()
        val excessReturns = curve.zipWithNext { previous, current ->
            current.equity / previous.equity - one - riskFreePerPeriod
        }
        return ratio(excessReturns, periodsPerYear, numFactory)
    }

    private fun periodsPerYear(curve: List<EquityPoint>): Double {
        val deltas = curve.zipWithNext { a, b -> Duration.between(a.time, b.time).toMillis() }
            .filter { it > 0 }
            .sorted()
        if (deltas.isEmpty()) {
            return 1.0
        }
        val median = deltas[deltas.size / 2].toDouble()
        return YEAR_MILLIS / median
    }

    protected companion object {
        private val YEAR_MILLIS = Duration.ofDays(365).plusHours(6).toMillis().toDouble()

        fun mean(values: List<Num>, numFactory: NumFactory): Num =
            values.fold(numFactory.zero()) { sum, value -> sum + value } / numFactory.numOf(values.size)

        fun scale(ratio: Num, annualization: Annualization, periodsPerYear: Double, numFactory: NumFactory): Num =
            when (annualization) {
                Annualization.PERIOD -> ratio
                Annualization.ANNUALIZED -> ratio * numFactory.numOf(periodsPerYear).sqrt()
            }
    }
}
