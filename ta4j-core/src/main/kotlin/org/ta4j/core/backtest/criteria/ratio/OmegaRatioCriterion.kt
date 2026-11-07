/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.criteria.ratio

import org.ta4j.core.num.NaN
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * Omega ratio: sum(max(r - threshold, 0)) / sum(max(threshold - r, 0)) over per-bar returns.
 *
 * NaN when there are gains but no shortfall (unbounded), zero when there are neither.
 *
 * @param threshold per-bar return threshold, e.g. 0.0
 * @see <a href="https://en.wikipedia.org/wiki/Omega_ratio">Omega ratio</a>
 */
class OmegaRatioCriterion(private val threshold: Double = 0.0) : AbstractReturnSeriesCriterion() {

    override fun ratio(excessReturns: List<Num>, periodsPerYear: Double, numFactory: NumFactory): Num {
        val tau = numFactory.numOf(threshold)
        val zero = numFactory.zero()
        var upside = zero
        var downside = zero
        for (r in excessReturns) {
            if (r > tau) upside += r - tau else downside += tau - r
        }
        return when {
            downside.isZero && upside.isZero -> zero
            downside.isZero -> NaN
            else -> upside / downside
        }
    }
}
