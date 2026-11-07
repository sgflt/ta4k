/*
 * SPDX-License-Identifier: MIT
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
