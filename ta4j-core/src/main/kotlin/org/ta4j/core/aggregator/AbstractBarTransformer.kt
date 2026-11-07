/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.aggregator

import org.ta4j.core.api.callback.BarListener
import org.ta4j.core.api.series.Bar

/**
 * Turns a stream of source bars into a stream of new bars (non-time bars, Heikin-Ashi, ...).
 * Chain transformers with [andThen].
 */
abstract class AbstractBarTransformer : BarListener {
    private val listeners = mutableListOf<BarListener>()

    fun addBarListener(listener: BarListener) = apply {
        listeners += listener
    }

    /**
     * Feeds this transformer's output into [next].
     *
     * @return [next], for further chaining
     */
    fun <T : AbstractBarTransformer> andThen(next: T): T {
        addBarListener(next)
        return next
    }

    protected fun emit(bar: Bar) {
        listeners.forEach { it.onBar(bar) }
    }
}
