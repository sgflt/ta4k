/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.trading.signal

import java.time.Instant
import org.ta4j.core.api.series.Symbol

interface Signal {
    val symbol: Symbol
    val whenReceived: Instant
    val strategyName: String
}

data class EntrySignal(
    override val symbol: Symbol,
    override val whenReceived: Instant,
    override val strategyName: String,
) : Signal

data class ExitSignal(
    override val symbol: Symbol,
    override val whenReceived: Instant,
    override val strategyName: String,
) : Signal
