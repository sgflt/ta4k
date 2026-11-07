/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.strategy

import org.ta4j.core.strategy.rules.CompoundRule

/**
 * Aggregates multiple strategies. Usually used for multiple timeframes.
 *
 * Larger time frame used for hint and smaller timeframe for precise entry/exit.
 */
class CompoundStrategy(private vararg val strategies: Strategy) : Strategy {
    override val name = strategies.joinToString(",") { it.name }
    override val timeFrames = strategies.flatMap { it.timeFrames }.toSet()
    private val entry = CompoundRule(strategies.map { it.entryRule })
    private val exit = CompoundRule(strategies.map { it.exitRule })

    override val entryRule: Rule
        get() = entry

    override val exitRule: Rule
        get() = exit

    override val isStable: Boolean
        get() = strategies.all { it.isStable }
}
