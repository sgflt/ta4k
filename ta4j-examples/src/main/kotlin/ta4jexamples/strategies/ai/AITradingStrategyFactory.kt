/*
 * SPDX-License-Identifier: MIT
 */
package ta4jexamples.strategies.ai

import org.ta4j.core.TradeType
import org.ta4j.core.api.Indicators
import org.ta4j.core.indicators.IndicatorContexts
import org.ta4j.core.indicators.TimeFrame
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.indicators.numeric.average.SMAIndicator
import org.ta4j.core.indicators.numeric.momentum.ATRIndicator
import org.ta4j.core.indicators.numeric.momentum.RSIIndicator
import org.ta4j.core.num.DoubleNumFactory
import org.ta4j.core.strategy.DefaultStrategy
import org.ta4j.core.strategy.RuntimeContext
import org.ta4j.core.strategy.Strategy
import org.ta4j.core.strategy.StrategyFactory
import org.ta4j.core.strategy.configuration.StrategyConfiguration
import org.ta4j.core.strategy.rules.AbstractRule

/**
 * AI-powered strategy factory that uses mock OpenAI service for trading decisions
 */
class AITradingStrategyFactory(
    private val aiService: MockOpenAIService = MockOpenAIService(),
    private val timeFrame: TimeFrame = TimeFrame.MINUTES_5,
) : StrategyFactory<Strategy> {

    override val tradeType: TradeType = TradeType.BUY

    override fun createStrategy(
        configuration: StrategyConfiguration,
        runtimeContext: RuntimeContext,
        indicatorContexts: IndicatorContexts,
    ): Strategy {
        val indicatorContext = indicatorContexts[timeFrame]

        // Create technical indicators using fluent API
        val numFactory = DoubleNumFactory
        val closePrice = Indicators.extended(numFactory).closePrice()
        val rsi = closePrice.rsi(14)
        val sma = closePrice.sma(20)
        // ATR needs to be created separately as it doesn't operate on close price
        val atr = Indicators.extended(numFactory).atr(14)
        val volume = Indicators.extended(numFactory).volume()

        // Add indicators to context
        indicatorContext.add(closePrice)
        indicatorContext.add(rsi)
        indicatorContext.add(sma)
        indicatorContext.add(atr)
        indicatorContext.add(volume)

        // Create AI-powered rules
        val entryRule = AITradingRule(aiService, closePrice, rsi, sma, atr, MockOpenAIService.TradingAction.BUY)
        val exitRule = AITradingRule(aiService, closePrice, rsi, sma, atr, MockOpenAIService.TradingAction.SELL)

        return DefaultStrategy(
            "AI Trading Strategy",
            setOf(timeFrame),
            entryRule,
            exitRule,
            indicatorContexts
        )
    }

    /**
     * Custom rule that uses AI service for trading decisions
     */
    private class AITradingRule(
        private val aiService: MockOpenAIService,
        private val closePrice: NumericIndicator,
        private val rsi: RSIIndicator,
        private val sma: SMAIndicator,
        private val atr: ATRIndicator,
        private val targetAction: MockOpenAIService.TradingAction,
    ) : AbstractRule() {

        override val isSatisfied: Boolean
            get() {
                if (!isDataAvailable()) {
                    return false
                }

                val currentPrice = closePrice.value
                val currentRsi = rsi.value
                val currentSma = sma.value
                val currentAtr = atr.value

                // Get AI recommendation
                val recommendation = aiService.getTradingRecommendation(
                    currentPrice, currentRsi, currentSma, currentAtr
                )

                // Log AI reasoning
                println("🤖 AI Analysis: ${recommendation.reasoning}")
                println(
                    "📊 Recommendation: ${recommendation.action} (Confidence: ${
                        "%.1f".format(recommendation.confidence * 100)
                    }%)"
                )

                // Return true if AI recommends the target action with sufficient confidence
                // Reduced threshold from 0.6 to 0.5 to allow more trades
                return recommendation.action == targetAction && recommendation.confidence > 0.5
            }

        private fun isDataAvailable(): Boolean {
            return rsi.isStable
                    && sma.isStable
                    && atr.isStable
        }
    }
}
