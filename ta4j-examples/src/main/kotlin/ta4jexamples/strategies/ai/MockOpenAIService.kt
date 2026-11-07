/*
 * SPDX-License-Identifier: MIT
 */
package ta4jexamples.strategies.ai

import org.ta4j.core.num.Num

/**
 * Mock OpenAI service that simulates AI-powered trading recommendations
 * based on technical indicator values.
 */
class MockOpenAIService {
    
    data class TradingRecommendation(
        val action: TradingAction,
        val confidence: Double,
        val reasoning: String
    )
    
    enum class TradingAction {
        BUY, SELL, HOLD
    }
    
    /**
     * Simulates OpenAI API call with technical indicator analysis
     * Parameters are tuned to generate more trading signals
     */
    fun getTradingRecommendation(
        currentPrice: Num,
        rsi: Num,
        sma: Num,
        atr: Num
    ): TradingRecommendation {
        val rsiValue = rsi.doubleValue()
        val currentPriceValue = currentPrice.doubleValue()
        val smaValue = sma.doubleValue()
        val atrValue = atr.doubleValue()
        
        // Market analysis based on indicators
        // Calculate ATR as percentage of current price
        val atrPercentage = atrValue / currentPriceValue
        
        return when {
            // High volatility hold - check if ATR is more than 5% of price
            atrPercentage > 0.05 -> {
                TradingRecommendation(
                    TradingAction.HOLD,
                    0.45,  // Reduced confidence to allow other signals through
                    "High volatility detected (ATR: ${String.format("%.2f%%", atrPercentage * 100)}). Avoiding trades until market stabilizes."
                )
            }
            
            // Oversold condition - adjusted thresholds
            rsiValue < 35 -> {
                val confidence = when {
                    rsiValue < 25 -> 0.85
                    else -> 0.65
                }
                TradingRecommendation(
                    TradingAction.BUY,
                    confidence,
                    "RSI indicates oversold condition (${String.format("%.2f", rsiValue)}). Strong buy opportunity."
                )
            }
            
            // Overbought condition - adjusted thresholds
            rsiValue > 65 -> {
                val confidence = when {
                    rsiValue > 75 -> 0.85
                    else -> 0.65
                }
                TradingRecommendation(
                    TradingAction.SELL,
                    confidence,
                    "RSI shows overbought condition (${String.format("%.2f", rsiValue)}). Sell signal detected."
                )
            }
            
            // Trend following signals - more sensitive thresholds
            currentPriceValue > smaValue * 1.01 && rsiValue < 70 -> {
                TradingRecommendation(
                    TradingAction.BUY,
                    0.60,
                    "Uptrend confirmed. Price above SMA with RSI room to grow."
                )
            }
            
            currentPriceValue < smaValue * 0.99 && rsiValue > 30 -> {
                TradingRecommendation(
                    TradingAction.SELL,
                    0.60,
                    "Downtrend confirmed. Price below SMA with RSI room to fall."
                )
            }
            
            // Default neutral position with momentum bias
            else -> {
                // Add slight momentum bias to generate more signals
                val momentumBias = (currentPriceValue - smaValue) / smaValue
                when {
                    momentumBias > 0.005 -> TradingRecommendation(
                        TradingAction.BUY,
                        0.52,
                        "Slight upward momentum detected."
                    )
                    momentumBias < -0.005 -> TradingRecommendation(
                        TradingAction.SELL,
                        0.52,
                        "Slight downward momentum detected."
                    )
                    else -> TradingRecommendation(
                        TradingAction.HOLD,
                        0.50,
                        "Market conditions neutral. Waiting for clearer signals."
                    )
                }
            }
        }
    }
    
    /**
     * Simulates sentiment analysis from news/social media
     */
    fun getMarketSentiment(): String {
        val sentiments = listOf(
            "Bullish sentiment detected in social media discussions",
            "Neutral market sentiment with mixed opinions",
            "Bearish sentiment prevailing in news headlines",
            "Strong institutional buying interest observed",
            "Retail investor fear and uncertainty rising"
        )
        return sentiments.random()
    }
}
