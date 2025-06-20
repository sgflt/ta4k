/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2025 Ta4j Organization & respective authors (see AUTHORS)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
