/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.events

import java.time.Instant

@JvmRecord
data class TickReceived(
    override val beginTime: Instant,
    val ask: Double,
    val bid: Double,
) : MarketEvent
