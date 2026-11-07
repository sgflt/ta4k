/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.bool

import java.time.Instant
import org.ta4j.core.api.Indicator
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.bool.chandelier.ChandelierExitLongIndicator
import org.ta4j.core.indicators.bool.chandelier.ChandelierExitShortIndicator
import org.ta4j.core.indicators.helpers.previous.PreviousBooleanValueIndicator
import org.ta4j.core.num.NumFactoryProvider
import org.ta4j.core.strategy.rules.BooleanIndicatorRule

abstract class BooleanIndicator : Indicator<Boolean> {
    private var currentBeginTime: Instant = Instant.MIN
    override var value = false
        protected set

    fun toRule(): BooleanIndicatorRule = BooleanIndicatorRule(this)


    fun previous(barCount: Int) = PreviousBooleanValueIndicator(this, barCount)

    /** True when this indicator was true on any of the last [barCount] bars. */
    fun wasTrueWithin(barCount: Int) = WasTrueWithinIndicator(this, barCount)

    /** True when this and [other] were both true within the last [threshold] bars. */
    fun andWithin(other: BooleanIndicator, threshold: Int) = AndWithThresholdIndicator(this, other, threshold)

    /** True when this or [other] was true within the last [threshold] bars. */
    fun orWithin(other: BooleanIndicator, threshold: Int) = OrWithThresholdIndicator(this, other, threshold)

    override fun onBar(bar: Bar) {
        if (bar.beginTime.isAfter(currentBeginTime)) {
            updateState(bar)
            currentBeginTime = bar.beginTime
        }
    }


    /**
     * Updates internal state of indicator.
     *
     * If indicator depends on other indicators, it is required to call [.onBar] on them to refresh their state
     * before calculation
     *
     * @param bar that comes from exchange's stream
     */
    protected abstract fun updateState(bar: Bar)

    companion object {
        @JvmStatic
        fun chandelierExitLong(
            barCount: Int,
            coefficient: Double,
        ): ChandelierExitLongIndicator {
            return ChandelierExitLongIndicator(NumFactoryProvider.defaultNumFactory, barCount, coefficient)
        }

        @JvmStatic
        fun chandelierExitShort(
            barCount: Int,
            coefficient: Double,
        ): ChandelierExitShortIndicator {
            return ChandelierExitShortIndicator(NumFactoryProvider.defaultNumFactory, barCount, coefficient)
        }
    }
}
