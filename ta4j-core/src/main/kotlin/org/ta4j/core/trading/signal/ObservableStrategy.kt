/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.trading.signal

import java.time.Instant
import org.ta4j.core.api.series.Symbol
import org.ta4j.core.indicators.IndicatorContextUpdateListener
import org.ta4j.core.strategy.Strategy

class ObservableStrategy(
    private val strategy: Strategy,
    private val signalListeners: List<SignalListener>,
) : Strategy, IndicatorContextUpdateListener {
    private var lastCallTime: Instant = Instant.MIN


    override val name = strategy.name
    override val timeFrames = strategy.timeFrames
    override val entryRule = strategy.entryRule
    override val exitRule = strategy.exitRule

    override val isStable: Boolean
        get() {
            return strategy.isStable
        }


    override fun onContextUpdate(symbol: Symbol, time: Instant) {
        if (time.isAfter(lastCallTime)) {
            lastCallTime = time

            if (isStable && entryRule.isSatisfied) {
                signalListeners.forEach { it.onSignal(EntrySignal(symbol, time, name)) }
            } else if (isStable && exitRule.isSatisfied) {
                signalListeners.forEach { it.onSignal(ExitSignal(symbol, time, name)) }
            }
        }
    }
}
