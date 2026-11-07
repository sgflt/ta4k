/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.trading.signal

import org.ta4j.core.indicators.IndicatorContexts
import org.ta4j.core.strategy.RuntimeContext
import org.ta4j.core.strategy.Strategy
import org.ta4j.core.strategy.StrategyFactory
import org.ta4j.core.strategy.configuration.StrategyConfiguration

/**
 * @author Lukáš Kvídera
 */
class ObservableStrategyFactoryBuilder {
    private val signalListeners = ArrayList<SignalListener>(2)
    private lateinit var strategyFactory: StrategyFactory<Strategy>


    fun withSignalListener(entrySignalListener: SignalListener): ObservableStrategyFactoryBuilder {
        signalListeners += entrySignalListener
        return this
    }

    fun withStrategyFactory(strategyFactory: StrategyFactory<Strategy>): ObservableStrategyFactoryBuilder {
        this.strategyFactory = strategyFactory
        return this
    }


    fun build(): StrategyFactory<Strategy> {
        return object : StrategyFactory<Strategy> {
            override val tradeType = this@ObservableStrategyFactoryBuilder.strategyFactory.tradeType


            override fun createStrategy(
                configuration: StrategyConfiguration,
                runtimeContext: RuntimeContext,
                indicatorContexts: IndicatorContexts,
            ): ObservableStrategy {
                val observableStrategy = ObservableStrategy(
                    this@ObservableStrategyFactoryBuilder.strategyFactory.createStrategy(
                        configuration,
                        runtimeContext,
                        indicatorContexts
                    ),
                    this@ObservableStrategyFactoryBuilder.signalListeners,
                )
                indicatorContexts.register(observableStrategy)
                return observableStrategy
            }
        }
    }


}

