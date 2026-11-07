/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.volume

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.CloseLocationValueIndicator
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * Accumulation-distribution indicator.
 *
 * The Accumulation/Distribution Line is a volume-based indicator designed to measure
 * the cumulative flow of money into and out of a security. It uses the Close Location
 * Value (CLV) to determine whether money is flowing into or out of a security.
 *
 * Formula:
 * 1. Money Flow Multiplier = Close Location Value (CLV)
 * 2. Money Flow Volume = Money Flow Multiplier × Volume
 * 3. AD = Previous AD + Money Flow Volume
 */
class AccumulationDistributionIndicator(numFactory: NumFactory) : NumericIndicator(numFactory) {

    private val clvIndicator = CloseLocationValueIndicator(numFactory)
    private var accumulationDistribution = numFactory.zero()

    private fun calculate(bar: Bar): Num {
        val moneyFlowMultiplier = clvIndicator.value
        val moneyFlowVolume = moneyFlowMultiplier * bar.volume
        accumulationDistribution += moneyFlowVolume
        return accumulationDistribution
    }

    override fun updateState(bar: Bar) {
        clvIndicator.onBar(bar)

        value = calculate(bar)
    }

    override val lag = 0

    override val isStable = true
}
