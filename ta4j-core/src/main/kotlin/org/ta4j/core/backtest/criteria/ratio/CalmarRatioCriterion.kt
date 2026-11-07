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
 * Calmar ratio: compound annual growth rate / maximum drawdown, both from the equity curve.
 *
 * Returns the annual growth rate itself when there is no drawdown, zero for curves shorter
 * than two points or spanning no time.
 *
 * @see <a href="https://www.investopedia.com/terms/c/calmarratio.asp">Calmar ratio</a>
 */
class CalmarRatioCriterion : AbstractEquityCurveCriterion() {

    override fun calculate(curve: List<EquityPoint>, numFactory: NumFactory): Num {
        if (curve.size < 2) {
            return numFactory.zero()
        }
        val years = Duration.between(curve.first().time, curve.last().time).toMillis() / YEAR_MILLIS
        if (years <= 0.0) {
            return numFactory.zero()
        }
        val totalGrowth = (curve.last().equity / curve.first().equity).doubleValue()
        val annualGrowth = numFactory.numOf(totalGrowth.pow(1.0 / years) - 1.0)
        val drawdown = maximumDrawdown(curve, numFactory)
        return if (drawdown.isZero) annualGrowth else annualGrowth / drawdown
    }

    private companion object {
        private val YEAR_MILLIS = Duration.ofDays(365).plusHours(6).toMillis().toDouble()

        fun maximumDrawdown(curve: List<EquityPoint>, numFactory: NumFactory): Num {
            var peak = curve.first().equity
            var maximum = numFactory.zero()
            for (point in curve) {
                if (point.equity > peak) peak = point.equity
                val drawdown = (peak - point.equity) / peak
                if (drawdown > maximum) maximum = drawdown
            }
            return maximum
        }
    }
}
