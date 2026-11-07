/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.average

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num

/**
 * McGinley Dynamic: `MD = MD' + (P - MD') / (N * (P / MD')^4)`, a moving average that speeds up when
 * price runs away from it. Seeded with the first price.
 *
 * Upstream ta4j uses the exponent 2; this uses John McGinley's original 4.
 *
 * @see <a href="https://www.investopedia.com/terms/m/mcginley-dynamic.asp">Investopedia</a>
 */
class McGinleyMAIndicator(
    private val indicator: NumericIndicator,
    private val barCount: Int,
) : NumericIndicator(indicator.numFactory) {

    init {
        require(barCount > 0) { "Bar count must be positive, but was: $barCount" }
    }

    private val bars = numFactory.numOf(barCount)
    private var previous: Num? = null
    private var barsPassed = 0

    override fun updateState(bar: Bar) {
        indicator.onBar(bar)
        ++barsPassed
        val price = indicator.value
        val last = previous
        val next = when {
            price.isNaN -> last ?: price
            last == null || last.isNaN || last.isZero -> price
            else -> last + (price - last) / (bars * (price / last).pow(4))
        }
        previous = next
        value = next
    }

    override val lag = barCount

    override val isStable: Boolean
        get() = barsPassed >= barCount && indicator.isStable

    override fun toString() = "McGinleyMA($barCount) => $value"
}
