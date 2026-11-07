/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.strategy

import org.ta4j.core.backtest.strategy.runtime.RuntimeContextFactory
import org.ta4j.core.strategy.StrategyFactory
import org.ta4j.core.strategy.configuration.StrategyConfiguration

/**
 * This class encapsulates classes that are required for backtesting.
 */
interface BacktestRun {
    /**
     * @return context that holds relevant data for strategy
     */
    val runtimeContextFactory: RuntimeContextFactory

    /**
     * @return factory that accepts related runtime context
     */
    val strategyFactory: StrategyFactory<BacktestStrategy>

    /**
     * @return configuration that will be used for creation of strategy
     */
    val configuration: StrategyConfiguration
}
