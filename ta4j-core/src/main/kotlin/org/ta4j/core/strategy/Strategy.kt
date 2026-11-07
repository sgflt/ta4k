/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.strategy

import org.ta4j.core.indicators.TimeFrame

/**
 * A `Strategy` (also called "trading strategy") is a pair of
 * complementary (entry and exit) [rules][Rule]. It may recommend to enter
 * or to exit. Recommendations are based respectively on the entry rule or on
 * the exit rule.
 */
interface Strategy {
    /**
     * @return the name of the strategy
     */
    val name: String

    /**
     * Which timeframes this strategy accepts.
     *
     * Leaf strategy wil return exactly one element.
     *
     * Root strategy that aggregates multiple timeframe restricted strategies will return all timeframes of child
     * strategies.
     *
     * @return timeframes that this strategy expects
     */
    val timeFrames: Set<TimeFrame>

    /**
     * @return the entry rule
     */
    val entryRule: Rule

    /**
     * @return the exit rule
     */
    val exitRule: Rule

    /**
     * @return true if this strategy is stable at current moment, false otherwise
     * (unstable)
     */
    val isStable: Boolean

    /**
     * @return true to recommend to enter, false otherwise
     */
    fun shouldEnter(): Boolean {
        return this.isStable && entryRule.isSatisfied
    }


    /**
     * @return true to recommend to exit, false otherwise
     */
    fun shouldExit(): Boolean {
        return this.isStable && exitRule.isSatisfied
    }
}
