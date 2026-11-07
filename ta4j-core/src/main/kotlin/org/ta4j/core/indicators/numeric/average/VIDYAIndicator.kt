/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.average

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.indicators.numeric.oscillators.CMOIndicator
import org.ta4j.core.num.Num

/**
 * Chande's Variable Index Dynamic Average: an EMA whose smoothing constant is |CMO| / 100, so it
 * follows price closely in trends and flattens in ranges. Seeded with the first price.
 *
 * @param cmoBarCount bar count of the Chande Momentum Oscillator driving the smoothing
 */
class VIDYAIndicator(
    private val indicator: NumericIndicator,
    private val cmoBarCount: Int,
) : NumericIndicator(indicator.numFactory) {

    private val cmo = CMOIndicator(numFactory, indicator, cmoBarCount)
    private val hundred = numFactory.hundred()
    private var previous: Num? = null

    override fun updateState(bar: Bar) {
        indicator.onBar(bar)
        cmo.onBar(bar)
        val price = indicator.value
        val last = previous
        val next = if (last == null || last.isNaN || cmo.value.isNaN) {
            price
        } else {
            last + cmo.value.abs() / hundred * (price - last)
        }
        previous = next
        value = next
    }

    override val lag = cmoBarCount

    override val isStable: Boolean
        get() = cmo.isStable

    override fun toString() = "VIDYA($cmoBarCount) => $value"
}
