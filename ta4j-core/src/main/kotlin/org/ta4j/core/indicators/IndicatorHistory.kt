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
