/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.strategy

import org.ta4j.core.TradeType
import org.ta4j.core.indicators.IndicatorContexts
import org.ta4j.core.strategy.RuntimeContext
import org.ta4j.core.strategy.Strategy
import org.ta4j.core.strategy.StrategyFactory
import org.ta4j.core.strategy.configuration.StrategyConfiguration

/**
 * Converts standard StrategyFactory<Strategy> to StrategyFactory<BacktestStrategy>
 * for use in backtesting.
</BacktestStrategy></Strategy> */
object StrategyFactoryConverter {
    /**
     * Converts a standard strategy factory to a backtest strategy factory.
     *
     * @param originalFactory the original strategy factory to convert
     *
     * @return a new factory that creates BacktestStrategy instances
     */
    fun convert(originalFactory: StrategyFactory<Strategy>): StrategyFactory<BacktestStrategy> {
        return BacktestStrategyFactory(originalFactory)
    }


    private class BacktestStrategyFactory(private val originalFactory: StrategyFactory<Strategy>) :
        StrategyFactory<BacktestStrategy> {
        override val tradeType: TradeType
            get() = originalFactory.tradeType


        /**
         * {@inheritDoc}
         *
         * @return strategy for backtesting
         */
        override fun createStrategy(
            configuration: StrategyConfiguration,
            runtimeContext: RuntimeContext,
            indicatorContexts: IndicatorContexts,
        ): BacktestStrategy {
            val originalStrategy = this.originalFactory.createStrategy(
                configuration,
                runtimeContext,
                indicatorContexts
            )

            return BacktestStrategy(
                originalStrategy,
                runtimeContext
            )
        }
    }
}
