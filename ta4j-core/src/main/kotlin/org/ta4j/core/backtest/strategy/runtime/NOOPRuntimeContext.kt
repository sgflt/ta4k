/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.strategy.runtime

import org.ta4j.core.api.series.Bar
import org.ta4j.core.events.TickReceived
import org.ta4j.core.strategy.RuntimeContext
import org.ta4j.core.strategy.RuntimeValueResolver

/**
 * This context does nothing.
 */
internal object NOOPRuntimeContext : RuntimeContext {
    override fun <T> getValue(resolver: RuntimeValueResolver<T?>): T? {
        return null
    }


    override fun getValue(key: String): Any? {
        return null
    }


    override fun onBar(bar: Bar) {
        // nothing to update
    }


    override fun onTick(tick: TickReceived) {
        // nothing to update
    }
}
