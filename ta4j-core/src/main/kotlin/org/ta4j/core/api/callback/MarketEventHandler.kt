/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.api.callback

import org.ta4j.core.events.CandleReceived
import org.ta4j.core.events.NewsReceived
import org.ta4j.core.events.TickReceived

interface MarketEventHandler {
    /**
     * Processes candle
     *
     * Whole candle refreshes [org.ta4j.core.indicators.IndicatorContext]
     */
    fun onCandle(event: CandleReceived)

    /**
     * Processes tick
     *
     * tick refreshes [RuntimeContext]
     */
    fun onTick(event: TickReceived)

    /**
     * Processes news. Usually AI analysis.
     */
    fun onNews(event: NewsReceived)
}
