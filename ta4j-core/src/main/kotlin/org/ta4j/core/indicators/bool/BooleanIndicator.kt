/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2025 Ta4j Organization & respective authors (see AUTHORS)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
