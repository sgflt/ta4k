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

import org.ta4j.core.TradeType
import org.ta4j.core.indicators.IndicatorContexts
import org.ta4j.core.strategy.configuration.StrategyConfiguration

/**
 * Implement this class if you have strategy that works.
 *
 * If you want to evolve strategy parameters, implement [OptimizableStrategyFactory]
 */
interface StrategyFactory<T : Strategy> {
    /**
     * For what direction is produced Strategy designed
     *
     * @return BUY or SELL
     */
    val tradeType: TradeType

    /**
     * @param configuration used for strategy
     * @param runtimeContext that provides additional data to strategy
     * @param indicatorContexts that performs indicator recalculation on each bar
     *
     * @return strategy that is source for trading signals
     */
    fun createStrategy(
        configuration: StrategyConfiguration,
        runtimeContext: RuntimeContext,
        indicatorContexts: IndicatorContexts,
    ): T
}
