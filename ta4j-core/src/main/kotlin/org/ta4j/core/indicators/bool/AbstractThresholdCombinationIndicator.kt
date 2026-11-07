/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.bool

import org.ta4j.core.api.series.Bar

/**
 * Combines two boolean indicators, each looked at over the last [threshold] bars.
 */
abstract class AbstractThresholdCombinationIndicator(
    first: BooleanIndicator,
    second: BooleanIndicator,
    protected val threshold: Int,
) : BooleanIndicator() {

    private val firstWithin = WasTrueWithinIndicator(first, threshold)
    private val secondWithin = WasTrueWithinIndicator(second, threshold)

    protected abstract fun combine(firstWasTrue: Boolean, secondWasTrue: Boolean): Boolean

    override fun updateState(bar: Bar) {
        firstWithin.onBar(bar)
        secondWithin.onBar(bar)
        value = combine(firstWithin.value, secondWithin.value)
    }

    override val lag = threshold

    override val isStable: Boolean
        get() = firstWithin.isStable && secondWithin.isStable

    override fun toString() = "${javaClass.simpleName}($threshold) => $value"
}

/** True when both indicators were true within the last [threshold] bars, not necessarily on the same bar. */
class AndWithThresholdIndicator(first: BooleanIndicator, second: BooleanIndicator, threshold: Int) :
    AbstractThresholdCombinationIndicator(first, second, threshold) {
    override fun combine(firstWasTrue: Boolean, secondWasTrue: Boolean) = firstWasTrue && secondWasTrue
}

/** True when either indicator was true within the last [threshold] bars. */
class OrWithThresholdIndicator(first: BooleanIndicator, second: BooleanIndicator, threshold: Int) :
    AbstractThresholdCombinationIndicator(first, second, threshold) {
    override fun combine(firstWasTrue: Boolean, secondWasTrue: Boolean) = firstWasTrue || secondWasTrue
}
