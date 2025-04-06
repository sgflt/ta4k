/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2024 Ta4j Organization & respective authors (see AUTHORS)
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
package org.ta4j.core.strategy

import org.ta4j.core.api.series.Bar
import org.ta4j.core.events.TickReceived

/**
 * A RuntimeContext that combines multiple other RuntimeContexts.
 * When querying values, it checks contexts in order until it finds a non-null result.
 */
class CompoundRuntimeContext private constructor(
    private val primary: RuntimeContext,
    private val secondary: RuntimeContext,
) : RuntimeContext {
    override fun <T> getValue(resolver: RuntimeValueResolver<T?>): T? {
        val primaryValue = resolver.resolve(this.primary)
        return primaryValue ?: resolver.resolve(this.secondary)
    }


    override fun getValue(key: String): Any? {
        val value = this.primary.getValue(key)
        return value ?: this.secondary.getValue(key)
    }


    override fun onBar(bar: Bar) {
        this.primary.onBar(bar)
        this.secondary.onBar(bar)
    }


    override fun onTick(tick: TickReceived) {
        this.primary.onTick(tick)
        this.secondary.onTick(tick)
    }

    companion object {
        /**
         * Creates a new CompoundRuntimeContext by combining two RuntimeContexts.
         *
         * @param primary the primary context to check first
         * @param secondary the secondary context to check if primary returns null
         *
         * @return a new CompoundRuntimeContext
         */
        @JvmStatic
        fun of(primary: RuntimeContext, secondary: RuntimeContext): CompoundRuntimeContext {
            return CompoundRuntimeContext(primary, secondary)
        }
    }
}
