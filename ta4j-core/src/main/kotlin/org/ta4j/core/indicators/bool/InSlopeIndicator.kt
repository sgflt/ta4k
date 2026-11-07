/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.bool

import org.ta4j.core.api.Indicator
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.helpers.previous.PreviousNumericValueIndicator
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * A rule that monitors when an [Indicator] shows a specified slope.
 *
 *
 *
 * Satisfied when the difference of the value of the [indicator][Indicator]
 * and its previous (n-th) value is between the values of `maxSlope`
 * or/and `minSlope`. It can test both, positive and negative slope.
 */
class InSlopeIndicator(
    numFactory: NumFactory,
    ref: NumericIndicator,
    private val nthPrevious: Int,
    /** The minimum slope between ref and prev.  */
    private val minSlope: Num,
    /** The maximum slope between ref and prev.  */
    private val maxSlope: Num,
) : BooleanIndicator() {
    private val diff = ref.minus(PreviousNumericValueIndicator(numFactory, ref, nthPrevious))

    private fun calculate(): Boolean {
        val difference = diff.value
        val minSlopeSatisfied = minSlope.isNaN || difference >= minSlope
        val maxSlopeSatisfied = maxSlope.isNaN || difference <= maxSlope
        val isNaN = minSlope.isNaN && maxSlope.isNaN

        return minSlopeSatisfied && maxSlopeSatisfied && !isNaN
    }

    override fun updateState(bar: Bar) {
        diff.onBar(bar)
        value = calculate()
    }

    override val lag = nthPrevious

    override val isStable get() = diff.isStable
}
