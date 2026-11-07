/*
 * SPDX-License-Identifier: MIT
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
