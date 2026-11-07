/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators

import java.time.Instant
import org.ta4j.core.api.Indicator
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num

/**
 * Stores the history of indicator values for a given time frame.
 */
internal class IndicatorHistory(val windowSize: Int = 1) : IndicatorChangeListener {
    private val history = mutableMapOf<IndicatorContext.IndicatorIdentification, ArrayDeque<Num>>()

    override fun accept(
        tick: Instant,
        indicatorId: IndicatorContext.IndicatorIdentification,
        indicator: Indicator<*>,
    ) {
        val value = when (indicator) {
            is NumericIndicator -> indicator.value
            else -> return
        }

        history.getOrPut(indicatorId) { ArrayDeque(windowSize) }.apply {
            addLast(value)
            if (size > windowSize) {
                removeFirst()
            }
        }
    }

    fun previous(indicatorId: IndicatorContext.IndicatorIdentification, bars: Int): Num? {
        require(bars in 1..windowSize) { "Bars must be between 1 and $windowSize" }
        val values = history[indicatorId] ?: throw IllegalArgumentException("No history for indicator $indicatorId")

        require(values.size >= bars) {
            "Not enough history for indicator $indicatorId. Required: $bars, Available: ${values.size}"
        }

        return values.getOrNull(values.size - bars)
    }
}
