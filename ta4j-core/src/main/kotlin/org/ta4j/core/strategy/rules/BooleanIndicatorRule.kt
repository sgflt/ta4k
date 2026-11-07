/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.strategy.rules

import org.ta4j.core.api.Indicator
import org.ta4j.core.indicators.bool.BooleanIndicator

/**
 * Satisfied when the value of the boolean [indicator][Indicator] is
 * `true`.
 */
class BooleanIndicatorRule
/**
 * Constructor.
 *
 * @param indicator the boolean indicator
 */(private val indicator: BooleanIndicator) : AbstractRule() {
    override val isSatisfied: Boolean
        get() {
            val satisfied = indicator.value
            traceIsSatisfied(satisfied)
            return satisfied
        }


    override fun toString(): String {
        return String.format("BoolIndRule[%s] => %s", this.indicator, this.indicator.value)
    }
}
