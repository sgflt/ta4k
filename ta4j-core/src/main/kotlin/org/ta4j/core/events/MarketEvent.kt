/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.events

import java.time.Instant

/**
 * This interface is marker for various events. Candle, Tick, News, ...
 */
interface MarketEvent {
    val beginTime: Instant
}
