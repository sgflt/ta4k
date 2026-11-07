/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.bool.trend

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.bool.BooleanIndicator
import org.ta4j.core.indicators.numeric.momentum.adx.ADXIndicator
import org.ta4j.core.indicators.numeric.momentum.adx.MinusDIIndicator
import org.ta4j.core.indicators.numeric.momentum.adx.PlusDIIndicator
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * Trend filter from the directional movement system: the ADX shows a strong trend (above [strengthThreshold]) and
 * the directional indicators of the previous bar point the way subclasses ask for.
 */
abstract class AbstractDirectionalTrendIndicator(
    numFactory: NumFactory,
    barCount: Int,
    strengthThreshold: Number,
) : BooleanIndicator() {

    private val strength = ADXIndicator(numFactory, barCount, barCount)
    private val previousPlusDI = PlusDIIndicator(numFactory, barCount).previous()
    private val previousMinusDI = MinusDIIndicator(numFactory, barCount).previous()
    private val threshold = numFactory.numOf(strengthThreshold)

    protected abstract fun isDirection(plusDI: Num, minusDI: Num): Boolean

    override fun updateState(bar: Bar) {
        strength.onBar(bar)
        previousPlusDI.onBar(bar)
        previousMinusDI.onBar(bar)
        value = isStable && strength.value > threshold && isDirection(previousPlusDI.value, previousMinusDI.value)
    }

    override val lag: Int
        get() = strength.lag + 1

    override val isStable: Boolean
        get() = strength.isStable && previousPlusDI.isStable && previousMinusDI.isStable
}

/** Strong up trend: ADX above the threshold and +DI above -DI on the previous bar. */
class UpTrendIndicator(numFactory: NumFactory, barCount: Int = 5, strengthThreshold: Number = 25) :
    AbstractDirectionalTrendIndicator(numFactory, barCount, strengthThreshold) {
    override fun isDirection(plusDI: Num, minusDI: Num) = plusDI > minusDI
    override fun toString() = "UpTrend => $value"
}

/** Strong down trend: ADX above the threshold and -DI above +DI on the previous bar. */
class DownTrendIndicator(numFactory: NumFactory, barCount: Int = 5, strengthThreshold: Number = 25) :
    AbstractDirectionalTrendIndicator(numFactory, barCount, strengthThreshold) {
    override fun isDirection(plusDI: Num, minusDI: Num) = minusDI > plusDI
    override fun toString() = "DownTrend => $value"
}
