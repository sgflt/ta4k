/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.strategy

import org.ta4j.core.TradeType
import org.ta4j.core.indicators.IndicatorContexts
import org.ta4j.core.strategy.configuration.StrategyConfiguration

/**
 * @author Lukáš Kvídera
 */
class NOOPStrategyFactory : StrategyFactory<Strategy> {
    override val tradeType = TradeType.BUY


    override fun createStrategy(
        configuration: StrategyConfiguration,
        runtimeContext: RuntimeContext,
        indicatorContexts: IndicatorContexts,
    ): Strategy {
        return DefaultStrategy(
            name = "NOOP",
            timeFrames = mutableSetOf(),
            entryRule = Rule.NOOP,
            exitRule = Rule.NOOP,
            indicatorContexts = indicatorContexts
        )
    }
}
