/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.bool.candles

/** Hammer: short body at the prior low, long lower shadow, almost no upper shadow. */
class HammerIndicator(averagePeriod: Int = DEFAULT_AVERAGE_PERIOD) : CandlePatternIndicator(2, averagePeriod) {
    override fun matches(): Boolean {
        val bar = candle(0)
        return isShortBody(0) && isLongShadow(0, lowerShadow(bar)) && isShortShadow(0, upperShadow(bar)) &&
            isNear(0, bodyBottom(bar), candle(1).lowPrice)
    }
}

/** Hanging man: hammer shape with its body at the prior high. */
class HangingManIndicator(averagePeriod: Int = DEFAULT_AVERAGE_PERIOD) : CandlePatternIndicator(2, averagePeriod) {
    override fun matches(): Boolean {
        val bar = candle(0)
        return isShortBody(0) && isLongShadow(0, lowerShadow(bar)) && isShortShadow(0, upperShadow(bar)) &&
            isNear(0, bodyTop(bar), candle(1).highPrice)
    }
}

/** Inverted hammer: short body opening below the prior close, long upper shadow, almost no lower shadow. */
class InvertedHammerIndicator(averagePeriod: Int = DEFAULT_AVERAGE_PERIOD) : CandlePatternIndicator(2, averagePeriod) {
    override fun matches(): Boolean {
        val bar = candle(0)
        return isShortBody(0) && isLongShadow(0, upperShadow(bar)) && isShortShadow(0, lowerShadow(bar)) &&
            bar.openPrice < candle(1).closePrice
    }
}

/** Shooting star: short body opening above the prior close, long upper shadow, almost no lower shadow. */
class ShootingStarIndicator(averagePeriod: Int = DEFAULT_AVERAGE_PERIOD) : CandlePatternIndicator(2, averagePeriod) {
    override fun matches(): Boolean {
        val bar = candle(0)
        return isShortBody(0) && isLongShadow(0, upperShadow(bar)) && isShortShadow(0, lowerShadow(bar)) &&
            bar.openPrice > candle(1).closePrice
    }
}

/** Bullish marubozu: long white body with almost no shadows. */
class BullishMarubozuIndicator(averagePeriod: Int = DEFAULT_AVERAGE_PERIOD) : CandlePatternIndicator(1, averagePeriod) {
    override fun matches() = candle(0).isBullish && isMarubozu(0)
}

/** Bearish marubozu: long black body with almost no shadows. */
class BearishMarubozuIndicator(averagePeriod: Int = DEFAULT_AVERAGE_PERIOD) : CandlePatternIndicator(1, averagePeriod) {
    override fun matches() = candle(0).isBearish && isMarubozu(0)
}
