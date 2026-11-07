/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators

import java.time.Instant
import org.ta4j.core.api.series.Symbol

fun interface IndicatorContextUpdateListener {
    /**
     * Called after timeframe context has been update on Bar.
     *
     * If there are multiple timeframes, method may be called multiple times. Call have to be idempotent.
     */
    fun onContextUpdate(symbol: Symbol, time: Instant)
}
