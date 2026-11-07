/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.strategy.rules

import java.time.ZonedDateTime
import org.ta4j.core.indicators.helpers.DateTimeIndicator

/**
 * Base class for rules satisfied when one calendar field of the current bar time
 * (hour, minute, day of week, ...) is in an allowed set.
 *
 * The field is read in the time zone of [timeIndicator].
 *
 * @param timeIndicator the bar time
 * @param allowedValues the accepted field values
 * @param label short name used in [toString]
 */
abstract class AbstractTimeFieldRule<T>(
    private val timeIndicator: DateTimeIndicator,
    allowedValues: Collection<T>,
    private val label: String,
) : AbstractRule() {

    private val allowedValues: Set<T> = allowedValues.toSet()

    init {
        require(this.allowedValues.isNotEmpty()) { "At least one value is required" }
    }

    /**
     * @return the field of [dateTime] this rule checks
     */
    protected abstract fun field(dateTime: ZonedDateTime): T

    final override val isSatisfied: Boolean
        get() {
            val satisfied = field(timeIndicator.value) in allowedValues
            traceIsSatisfied(satisfied)
            return satisfied
        }

    override fun toString() = "$label$allowedValues => $isSatisfied"
}
