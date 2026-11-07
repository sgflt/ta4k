/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators

import kotlin.collections.Map.Entry
import org.ta4j.core.api.series.Symbol

/**
 * Aggregation class that stores indicator contexts related to defined timeframes.
 */
class IndicatorContexts private constructor(val symbol: Symbol) : Iterable<Entry<TimeFrame, IndicatorContext>> {
    private val timeFramedContexts = HashMap<TimeFrame, IndicatorContext>()

    override fun iterator(): Iterator<Entry<TimeFrame, IndicatorContext>> = timeFramedContexts.entries.iterator()

    fun add(context: IndicatorContext) {
        timeFramedContexts.put(context.timeFrame(), context)
    }


    operator fun get(timeFrame: TimeFrame): IndicatorContext {
        return timeFramedContexts.computeIfAbsent(timeFrame) { IndicatorContext.empty(symbol, it) }
    }

    fun register(changeListener: IndicatorChangeListener) {
        timeFramedContexts.values.forEach { indicatorContext -> indicatorContext.register(changeListener) }
    }

    fun register(contextUpdateListener: IndicatorContextUpdateListener) {
        timeFramedContexts.values.forEach { indicatorContext -> indicatorContext.register(contextUpdateListener) }
    }

    fun enableHistory(windowSize: Int) {
        timeFramedContexts.values.forEach { it.enableHistory(windowSize) }
    }


    val timeFrames = timeFramedContexts.keys

    val isEmpty: Boolean
        get() = timeFramedContexts.isEmpty()

    val isStable: Boolean
        get() = all { it.value.isStable }

    companion object {
        fun empty(symbol: Symbol = Symbol("UNDEFINED")) = IndicatorContexts(symbol)
    }
}
