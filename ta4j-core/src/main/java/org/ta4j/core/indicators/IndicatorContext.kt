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

import java.util.*
import kotlin.collections.Map.Entry
import org.ta4j.core.api.Indicator
import org.ta4j.core.api.callback.BarListener
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.IndicatorContext.IndicatorIdentification
import org.ta4j.core.indicators.bool.BooleanIndicator
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num

class IndicatorContext private constructor(
    internal val timeFrame: TimeFrame,
    vararg indicators: Indicator<*>,
) : BarListener, Iterable<Entry<IndicatorIdentification, Indicator<*>>> {

    private var history: IndicatorHistory? = null
    var isStable: Boolean = false
        get() {
            if (field) {
                return field
            }

            field = indicators.values.all { it.isStable }
            return field
        }
        private set

    private val _indicators = indicators.associateBy { generatePlaceholderName(it) }.toMutableMap()
    val indicators: Map<IndicatorIdentification, Indicator<*>>
        get() = _indicators

    private val changeListeners = mutableSetOf<IndicatorChangeListener>()
    private val updateListeners = mutableSetOf<IndicatorContextUpdateListener>()

    fun register(changeListener: IndicatorChangeListener) {
        changeListeners.add(changeListener)
    }

    fun register(updateListener: IndicatorContextUpdateListener) {
        updateListeners.add(updateListener)
    }


    fun add(indicator: Indicator<*>) {
        _indicators[generatePlaceholderName(indicator)] = indicator
    }

    fun add(indicator: Indicator<*>, name: IndicatorIdentification) {
        _indicators[name] = indicator
    }


    val first: Indicator<*>?
        get() = indicators.values.firstOrNull()

    override fun iterator(): Iterator<Entry<IndicatorIdentification, Indicator<*>>> = indicators.iterator()

    private operator fun get(name: IndicatorIdentification): Indicator<*>? = indicators[name]

    fun getNumericIndicator(indicatorId: IndicatorIdentification): NumericIndicator? =
        this[indicatorId] as? NumericIndicator

    fun getBooleanIndicator(indicatorId: IndicatorIdentification): BooleanIndicator? =
        this[indicatorId] as? BooleanIndicator

    fun addAll(vararg indicators: Indicator<*>) {
        indicators.forEach { _indicators[generatePlaceholderName(it)] = it }
    }

    fun previousValue(indicatorId: IndicatorIdentification, bars: Int = 1): Num? {
        return history?.previous(indicatorId, bars)
            ?: throw IllegalStateException("History is not enabled for this context.")
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

    fun timeFrame(): TimeFrame = timeFrame

    fun enableHistory(historyWindow: Int) {
        history = IndicatorHistory(historyWindow)
        register(history!!)
    }

    @JvmRecord
    data class IndicatorIdentification(val name: String, val lag: Int = 0)

    @JvmRecord
    data class HistoryValue(
        val indicatorId: IndicatorIdentification,
        val value: Num,
    )

    companion object {
        @JvmStatic
        private fun generatePlaceholderName(indicator: Indicator<*>) =
            IndicatorIdentification(UUID.randomUUID().toString(), indicator.lag)

        fun of(timeFrame: TimeFrame, vararg indicators: Indicator<*>) =
            IndicatorContext(timeFrame, *indicators)

        @JvmStatic
        fun empty(timeFrame: TimeFrame = TimeFrame.UNDEFINED, historyWindow: Int? = null): IndicatorContext {
            val context = IndicatorContext(timeFrame)
            historyWindow?.let { context.enableHistory(it) }
            return context
        }
    }
}
