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

import org.ta4j.core.strategy.ObservableStrategyFactoryBuilder.ObservableStrategy
import java.util.function.Consumer

/**
 * Aggregation class that stores indicator contexts related to defined timeframes.
 */
class IndicatorContexts private constructor() {
    private val timeFramedContexts: MutableMap<TimeFrame?, IndicatorContext> = HashMap<TimeFrame?, IndicatorContext>()


    fun add(context: IndicatorContext) {
        this.timeFramedContexts.put(context.timeFrame(), context)
    }


    operator fun get(timeFrame: TimeFrame?): IndicatorContext {
        return this.timeFramedContexts.computeIfAbsent(timeFrame) { timeFrame: TimeFrame? ->
            IndicatorContext.Companion.empty(
                timeFrame
            )
        }
    }


    fun register(observableStrategy: ObservableStrategy) {
        this.timeFramedContexts.values.forEach(Consumer { indicatorContext: IndicatorContext? ->
            indicatorContext!!.register(
                observableStrategy
            )
        })
    }


    val timeFrames = timeFramedContexts.keys

    val isEmpty: Boolean
        get() = this.timeFramedContexts.isEmpty()

    companion object {
        fun empty(): IndicatorContexts {
            return IndicatorContexts()
        }
    }
}
