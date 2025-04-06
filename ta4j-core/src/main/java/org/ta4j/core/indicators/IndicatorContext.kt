/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2024 Lukáš Kvídera
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

import org.ta4j.core.api.Indicator
import org.ta4j.core.api.callback.BarListener
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.bool.BooleanIndicator
import org.ta4j.core.indicators.numeric.NumericIndicator
import java.util.*

class IndicatorContext private constructor(
    internal val timeFrame: TimeFrame?,
    vararg indicators: Indicator<*>,
) : BarListener {

    var isStable: Boolean = false
        get() {
            if (field) {
                return field
            }

            field = indicators.values.all { it.isStable }
            return field
        }
        private set

    private val indicators = indicators.associateBy { generatePlaceholderName() }.toMutableMap()
    private val changeListeners = mutableSetOf<IndicatorChangeListener>()
    private val updateListeners = mutableSetOf<IndicatorContextUpdateListener>()

    fun register(changeListener: IndicatorChangeListener) {
        changeListeners.add(changeListener)
    }

    fun register(updateListener: IndicatorContextUpdateListener) {
        updateListeners.add(updateListener)
    }


    fun add(indicator: Indicator<*>) {
        indicators[generatePlaceholderName()] = indicator
    }

    fun add(indicator: Indicator<*>, name: IndicatorIdentification) {
        indicators[name] = indicator
    }


    val first: Indicator<*>?
        get() = indicators.values.firstOrNull()

    private operator fun get(name: IndicatorIdentification): Indicator<*>? = indicators[name]

    fun getNumericIndicator(indicatorId: IndicatorIdentification): NumericIndicator? =
        this[indicatorId] as? NumericIndicator

    fun getBooleanIndicator(indicatorId: IndicatorIdentification): BooleanIndicator? =
        this[indicatorId] as? BooleanIndicator

    fun addAll(vararg indicators: Indicator<*>) {
        indicators.forEach { this.indicators[generatePlaceholderName()] = it }
    }

    override fun onBar(bar: Bar) {
        indicators.forEach { (key, indicator) ->
            indicator.onBar(bar)
            changeListeners.forEach { it.accept(bar.beginTime, key, indicator) }
        }

        updateListeners.forEach { it.onContextUpdate(bar.endTime) }
    }

    val isNotEmpty: Boolean
        get() = indicators.isNotEmpty()

    operator fun contains(indicatorId: IndicatorIdentification): Boolean =
        indicators.containsKey(indicatorId)

    fun timeFrame(): TimeFrame? = timeFrame

    data class IndicatorIdentification(val name: String?)
    companion object {
        @JvmStatic
        private fun generatePlaceholderName() = IndicatorIdentification(UUID.randomUUID().toString())

        fun of(timeFrame: TimeFrame?, vararg indicators: Indicator<*>): IndicatorContext =
            IndicatorContext(timeFrame, *indicators)

        @JvmStatic
        fun empty(timeFrame: TimeFrame?): IndicatorContext =
            IndicatorContext(timeFrame)
    }
}
