/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.helpers.previous

import org.ta4j.core.api.Indicator
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.bool.BooleanIndicator

/**
 * @author Lukáš Kvídera
 */
class PreviousBooleanValueIndicator(indicator: BooleanIndicator, n: Int) : BooleanIndicator() {
    private val previousValueHelper = PreviousValueHelper(indicator as Indicator<Boolean?>, n)


    private fun calculate() = previousValueHelper.value == true

    public override fun updateState(bar: Bar) {
        previousValueHelper.onBar(bar)
        value = calculate()
    }

    override val lag
        get() = previousValueHelper.lag

    override val isStable
        get() = previousValueHelper.isStable
}
