/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.events

import java.time.Instant
import org.ta4j.core.indicators.TimeFrame
@JvmRecord
data class CandleReceived(
    val timeFrame: TimeFrame,
    override val beginTime: Instant,
    val endTime: Instant,
    val openPrice: Double = Double.NaN,
    val highPrice: Double = Double.NaN,
    val lowPrice: Double = Double.NaN,
    val closePrice: Double = Double.NaN,
    val volume: Double = Double.NaN,
) : MarketEvent
