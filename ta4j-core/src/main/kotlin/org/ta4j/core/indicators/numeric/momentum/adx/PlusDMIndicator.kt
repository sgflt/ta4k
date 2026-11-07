/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.momentum.adx

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.NumFactory

/**
 * +DM indicator.
 *
 * Part of the Directional Movement System.
 */
class PlusDMIndicator(numFactory: NumFactory) : NumericIndicator(numFactory) {
    private var previousBar: Bar? = null
    override var isStable = false
        private set

    override val lag: Int = 2

    private fun calculate(bar: Bar) = when {
        previousBar == null -> {
            previousBar = bar
            numFactory.zero()
        }

        else -> {
            isStable = true
            val prevBar = previousBar!!
            val upMove = bar.highPrice.minus(prevBar.highPrice)
            val downMove = prevBar.lowPrice.minus(bar.lowPrice)
            previousBar = bar

            if (upMove > downMove && upMove > numFactory.zero()) {
                upMove
            } else {
                numFactory.zero()
            }
        }
    }

    override fun updateState(bar: Bar) {
        value = calculate(bar)
    }
}
