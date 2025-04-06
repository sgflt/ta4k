/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2024 Ta4j Organization & respective authors (see AUTHORS)
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

import org.ta4j.core.Signal
import org.ta4j.core.api.callback.EntrySignalListener
import org.ta4j.core.api.callback.ExitSignalListener
import org.ta4j.core.indicators.IndicatorContextUpdateListener
import org.ta4j.core.indicators.IndicatorContexts
import org.ta4j.core.strategy.configuration.StrategyConfiguration
import java.time.Instant

/**
 * @author Lukáš Kvídera
 */
class ObservableStrategyFactoryBuilder {
    private val entrySignalListeners: MutableList<EntrySignalListener> = ArrayList<EntrySignalListener>(1)
    private val exitSignalListeners: MutableList<ExitSignalListener> = ArrayList<ExitSignalListener>(1)
    private lateinit var strategyFactory: StrategyFactory<Strategy>


    fun withEntryListener(entrySignalListener: EntrySignalListener): ObservableStrategyFactoryBuilder {
        entrySignalListeners.add(entrySignalListener)
        return this
    }


    fun withExitListener(exitSignalListener: ExitSignalListener): ObservableStrategyFactoryBuilder {
        exitSignalListeners.add(exitSignalListener)
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
                    this@ObservableStrategyFactoryBuilder.entrySignalListeners,
                    this@ObservableStrategyFactoryBuilder.exitSignalListeners
                )
                indicatorContexts.register(observableStrategy)
                return observableStrategy
            }
        }
    }


    class ObservableStrategy(
        private val strategy: Strategy,
        private val entrySignalListeners: MutableList<EntrySignalListener>,
        private val exitSignalListeners: MutableList<ExitSignalListener>,
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


        override fun onContextUpdate(time: Instant) {
            if (time.isAfter(lastCallTime)) {
                lastCallTime = time

                if (isStable && entryRule.isSatisfied) {
                    entrySignalListeners.forEach { it.onSignal(Signal(time, name)) }
                } else if (isStable && exitRule.isSatisfied) {
                    exitSignalListeners.forEach { it.onSignal(Signal(time, name)) }
                }
            }
        }
    }
}


