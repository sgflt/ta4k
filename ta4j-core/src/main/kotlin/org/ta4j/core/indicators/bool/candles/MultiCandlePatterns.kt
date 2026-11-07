/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.bool.candles

import org.ta4j.core.api.series.Bar

/**
 * Dark cloud cover: long white candle, then a black candle opening above its high and closing
 * inside its body, at or below [penetration] of the way down from the top.
 */
class DarkCloudCoverIndicator(
    private val penetration: Double = 0.5,
    averagePeriod: Int = DEFAULT_AVERAGE_PERIOD,
) : CandlePatternIndicator(2, averagePeriod) {
    override fun matches(): Boolean {
        val first = candle(1)
        val second = candle(0)
        if (!(first.isBullish && isLongBody(1) && second.isBearish && second.openPrice > first.highPrice)) {
            return false
        }
        val required = pointBetween(bodyTop(first), bodyBottom(first), first.closePrice.numFactory.numOf(penetration))
        return second.closePrice > bodyBottom(first) && second.closePrice <= required
    }
}

/**
 * Piercing line: long black candle, then a white candle opening below its low and closing inside
 * its body, at or above [penetration] of the way up from the bottom.
 */
class PiercingLineIndicator(
    private val penetration: Double = 0.5,
    averagePeriod: Int = DEFAULT_AVERAGE_PERIOD,
) : CandlePatternIndicator(2, averagePeriod) {
    override fun matches(): Boolean {
        val first = candle(1)
        val second = candle(0)
        if (!(first.isBearish && isLongBody(1) && second.isBullish && second.openPrice < first.lowPrice)) {
            return false
        }
        val required = pointBetween(bodyBottom(first), bodyTop(first), first.closePrice.numFactory.numOf(penetration))
        return second.closePrice < bodyTop(first) && second.closePrice >= required
    }
}

/** Bullish kicker: bearish marubozu followed by a bullish marubozu gapping above its body. */
class BullishKickerIndicator(averagePeriod: Int = DEFAULT_AVERAGE_PERIOD) : CandlePatternIndicator(2, averagePeriod) {
    override fun matches() = candle(1).isBearish && isMarubozu(1) && candle(0).isBullish && isMarubozu(0) &&
        bodyBottom(candle(0)) > bodyTop(candle(1))
}

/** Bearish kicker: bullish marubozu followed by a bearish marubozu gapping below its body. */
class BearishKickerIndicator(averagePeriod: Int = DEFAULT_AVERAGE_PERIOD) : CandlePatternIndicator(2, averagePeriod) {
    override fun matches() = candle(1).isBullish && isMarubozu(1) && candle(0).isBearish && isMarubozu(0) &&
        bodyTop(candle(0)) < bodyBottom(candle(1))
}

/**
 * Morning star: long black candle, a short-bodied star gapping below it, then a long white candle
 * closing at or above [penetration] of the way up the first body.
 */
class MorningStarIndicator(
    private val penetration: Double = 0.5,
    averagePeriod: Int = DEFAULT_AVERAGE_PERIOD,
) : CandlePatternIndicator(3, averagePeriod) {
    override fun matches(): Boolean {
        val first = candle(2)
        val star = candle(1)
        val third = candle(0)
        val level = pointBetween(bodyBottom(first), bodyTop(first), first.closePrice.numFactory.numOf(penetration))
        return first.isBearish && isLongBody(2) && isShortBody(1) && bodyTop(star) < bodyBottom(first) &&
            third.isBullish && isLongBody(0) && third.closePrice >= level
    }
}

/**
 * Evening star: long white candle, a short-bodied star gapping above it, then a long black candle
 * closing at or below [penetration] of the way down the first body.
 */
class EveningStarIndicator(
    private val penetration: Double = 0.5,
    averagePeriod: Int = DEFAULT_AVERAGE_PERIOD,
) : CandlePatternIndicator(3, averagePeriod) {
    override fun matches(): Boolean {
        val first = candle(2)
        val star = candle(1)
        val third = candle(0)
        val level = pointBetween(bodyTop(first), bodyBottom(first), first.closePrice.numFactory.numOf(penetration))
        return first.isBullish && isLongBody(2) && isShortBody(1) && bodyBottom(star) > bodyTop(first) &&
            third.isBearish && isLongBody(0) && third.closePrice <= level
    }
}

/** Three inside up: bullish harami confirmed by a white candle closing above the first candle's open. */
class ThreeInsideUpIndicator(averagePeriod: Int = DEFAULT_AVERAGE_PERIOD) : CandlePatternIndicator(3, averagePeriod) {
    override fun matches(): Boolean {
        val first = candle(2)
        val second = candle(1)
        val harami = first.isBearish && isLongBody(2) && second.isBullish && isShortBody(1) && isInside(second, first)
        return harami && candle(0).isBullish && candle(0).closePrice > first.openPrice
    }
}

/** Three inside down: bearish harami confirmed by a black candle closing below the first candle's open. */
class ThreeInsideDownIndicator(averagePeriod: Int = DEFAULT_AVERAGE_PERIOD) : CandlePatternIndicator(3, averagePeriod) {
    override fun matches(): Boolean {
        val first = candle(2)
        val second = candle(1)
        val harami = first.isBullish && isLongBody(2) && second.isBearish && isShortBody(1) && isInside(second, first)
        return harami && candle(0).isBearish && candle(0).closePrice < first.openPrice
    }
}

/** Whether the body of [inner] lies within the body of [outer]. */
private fun isInside(inner: Bar, outer: Bar): Boolean {
    val innerTop = maxOf(inner.openPrice, inner.closePrice)
    val innerBottom = minOf(inner.openPrice, inner.closePrice)
    val outerTop = maxOf(outer.openPrice, outer.closePrice)
    val outerBottom = minOf(outer.openPrice, outer.closePrice)
    return innerTop <= outerTop && innerBottom >= outerBottom
}
