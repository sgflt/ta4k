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
