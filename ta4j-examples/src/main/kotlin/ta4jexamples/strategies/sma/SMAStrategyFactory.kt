/*
 * SPDX-License-Identifier: MIT
 */
package ta4jexamples.strategies.sma

import org.ta4j.core.TradeType
import org.ta4j.core.api.Indicators
import org.ta4j.core.indicators.IndicatorContexts
import org.ta4j.core.indicators.TimeFrame
import org.ta4j.core.strategy.DefaultStrategy
import org.ta4j.core.strategy.RuntimeContext
import org.ta4j.core.strategy.Strategy
import org.ta4j.core.strategy.StrategyFactory
import org.ta4j.core.strategy.configuration.StrategyConfiguration
import org.ta4j.core.strategy.rules.OverIndicatorRule
import org.ta4j.core.strategy.rules.UnderIndicatorRule

class SMAStrategyFactory(private val timeFrame: TimeFrame) : StrategyFactory<Strategy> {

    override val tradeType: TradeType = TradeType.BUY

    override fun createStrategy(
        configuration: StrategyConfiguration,
        runtimeContext: RuntimeContext,
        indicatorContexts: IndicatorContexts,
    ): Strategy {
        val indicatorContext = indicatorContexts[timeFrame]
        
        val closePrice = Indicators.closePrice()
        val shortSma = closePrice.sma(10)
        val longSma = closePrice.sma(20)
        
        indicatorContext.add(closePrice)
        indicatorContext.add(shortSma)
        indicatorContext.add(longSma)
        
        val entryRule = OverIndicatorRule(shortSma, longSma)
        val exitRule = UnderIndicatorRule(shortSma, longSma)
        
        return DefaultStrategy(
            "SMA Crossover",
            setOf(timeFrame),
            entryRule,
            exitRule,
            indicatorContexts
        )
    }
}
