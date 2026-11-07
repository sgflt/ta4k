/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.momentum.adx

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * DX indicator.
 *
 *
 *
 * Part of the Directional Movement System.
 */
class DXIndicator(numFactory: NumFactory, barCount: Int) : NumericIndicator(numFactory) {
    private val plusDIIndicator = PlusDIIndicator(numFactory, barCount)
    private val minusDIIndicator = MinusDIIndicator(numFactory, barCount)

    override val lag: Int
        get() = plusDIIndicator.lag

    private fun calculate(): Num {
        val pdiValue = plusDIIndicator.value
        val mdiValue = minusDIIndicator.value
        if (pdiValue.plus(mdiValue) == numFactory.zero()) {
            return numFactory.zero()
        }

        return ((pdiValue - mdiValue).abs() / (pdiValue + mdiValue)) * numFactory.hundred()
    }


    override fun updateState(bar: Bar) {
        plusDIIndicator.onBar(bar)
        minusDIIndicator.onBar(bar)
        value = calculate()
    }


    override val isStable
        get() = plusDIIndicator.isStable && minusDIIndicator.isStable


    override fun toString() = "${javaClass.simpleName} $plusDIIndicator $minusDIIndicator"
}
