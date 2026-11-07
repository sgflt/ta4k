/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.supportresistance

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.bool.fractal.AbstractFractalIndicator
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.NaN
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/** Weights of the trend line score; the defaults are upstream ta4j's. */
data class TrendLineWeights(
    /** Share of swings the line touches. */
    val touchCount: Double = 0.10,
    /** Whether the line touches the most extreme swing. */
    val touchesExtreme: Double = 0.15,
    /** Share of swings the line does not touch (fewer is better). */
    val outsideCount: Double = 0.05,
    /** Closeness of all swings to the line, relative to the swing range. */
    val averageDeviation: Double = 0.65,
    /** How late in the window the newer anchor is. */
    val anchorRecency: Double = 0.05,
)

/** How close a swing must be to count as touching the line. */
sealed interface TrendLineTolerance {
    fun amount(swingRange: Num): Num

    /** A fraction of the price range of the swings in the window. */
    data class Percentage(val fraction: Double = 0.02) : TrendLineTolerance {
        override fun amount(swingRange: Num) = swingRange * swingRange.numFactory.numOf(fraction)
    }

    /** A fixed price distance. */
    data class Absolute(val distance: Number) : TrendLineTolerance {
        override fun amount(swingRange: Num) = swingRange.numFactory.numOf(distance)
    }
}

/**
 * Trend line through two confirmed swing points of the last [lookback] bars.
 *
 * Every pair of swings in the window is a candidate line. A candidate may not cut through any swing (lie above a
 * swing low for support, below a swing high for resistance) unless the swing is within the tolerance. Candidates
 * are scored by [TrendLineWeights]; ties go to more touches, fewer outside swings, a longer span and finally the
 * newer second anchor.
 *
 * The value is the best line extended to the current bar, NaN until two swings are in the window. Unlike upstream
 * ta4j, which redraws the line over the whole window, past values never change: each bar publishes the line as known
 * on that bar. The best line is picked again only when a swing enters or leaves the window.
 *
 * At most [maxSwingPoints] of the newest swings are used, which caps the work at `n² / 2` candidates.
 */
abstract class AbstractTrendLineIndicator(
    private val fractal: AbstractFractalIndicator,
    protected val lookback: Int,
    private val weights: TrendLineWeights,
    private val tolerance: TrendLineTolerance,
    private val maxSwingPoints: Int,
) : NumericIndicator(fractal.numFactory) {

    private class Swing(val x: Long, val price: Num)

    /** Line `price = slope * x + intercept`, x counting bars from the first one. */
    private class Line(
        val first: Swing,
        val second: Swing,
        val slope: Num,
        val intercept: Num,
        val score: Double,
        val touches: Int,
        val outside: Int,
    ) {
        fun valueAt(x: Long, numFactory: NumFactory) = slope * numFactory.numOf(x) + intercept
    }

    private val swings = ArrayDeque<Swing>()
    private var bestLine: Line? = null
    private var currentX = -1L

    init {
        require(lookback >= 2) { "Lookback must be at least 2, but was: $lookback" }
        require(maxSwingPoints >= 2) { "Max swing points must be at least 2, but was: $maxSwingPoints" }
    }

    /** Slope of the current line per bar, NaN without a line. */
    val slope: Num
        get() = bestLine?.slope ?: NaN

    /** True when [projected] lies on the wrong side of [swingPrice] for this line type. */
    protected abstract fun violates(projected: Num, swingPrice: Num): Boolean

    /** The more extreme of two swing prices for this line type. */
    protected abstract fun pickExtreme(a: Num, b: Num): Num

    override fun updateState(bar: Bar) {
        fractal.onBar(bar)
        ++currentX
        var changed = false
        if (fractal.value) {
            swings.addLast(Swing(currentX - fractal.followingBars, fractal.pivotPrice))
            if (swings.size > maxSwingPoints) swings.removeFirst()
            changed = true
        }
        val windowStart = currentX - lookback + 1
        while (swings.isNotEmpty() && swings.first().x < windowStart) {
            swings.removeFirst()
            changed = true
        }
        if (changed) bestLine = selectLine(windowStart)
        value = bestLine?.valueAt(currentX, numFactory) ?: NaN
    }

    private fun selectLine(windowStart: Long): Line? {
        if (swings.size < 2) return null
        val prices = swings.map { it.price }
        val extreme = prices.reduce(::pickExtreme)
        val rawRange = prices.max() - prices.min()
        val range = if (rawRange.isZero) numFactory.numOf(1e-9) else rawRange
        val touchDistance = tolerance.amount(range)

        var best: Line? = null
        for (i in 0 until swings.size - 1) {
            for (j in i + 1 until swings.size) {
                val candidate = candidate(swings[i], swings[j], extreme, range, touchDistance, windowStart) ?: continue
                if (best == null || isBetter(candidate, best)) best = candidate
            }
        }
        return best
    }

    private fun isBetter(line: Line, other: Line): Boolean {
        if (line.score != other.score) return line.score > other.score
        if (line.touches != other.touches) return line.touches > other.touches
        if (line.outside != other.outside) return line.outside < other.outside
        val span = line.second.x - line.first.x
        val otherSpan = other.second.x - other.first.x
        if (span != otherSpan) return span > otherSpan
        // newer anchors describe the current market better
        return line.second.x > other.second.x
    }

    private fun candidate(
        first: Swing,
        second: Swing,
        extreme: Num,
        range: Num,
        touchDistance: Num,
        windowStart: Long,
    ): Line? {
        val slope = (second.price - first.price) / numFactory.numOf(second.x - first.x)
        val intercept = first.price - slope * numFactory.numOf(first.x)
        var touches = 0
        var outside = 0
        var touchesExtreme = false
        var totalDeviation = numFactory.zero()
        for (swing in swings) {
            val projected = slope * numFactory.numOf(swing.x) + intercept
            val deviation = (projected - swing.price).abs()
            val isAnchor = swing === first || swing === second
            if (isAnchor || deviation <= touchDistance) {
                ++touches
                if (swing.price.compareTo(extreme) == 0) touchesExtreme = true
            } else {
                if (violates(projected, swing.price)) return null
                ++outside
            }
            totalDeviation += deviation
        }

        val count = swings.size.toDouble()
        val averageDeviation = totalDeviation.doubleValue() / count
        val proximity = 1.0 - minOf(1.0, averageDeviation / range.doubleValue())
        val recency = ((second.x - windowStart).toDouble() / lookback).coerceIn(0.0, 1.0)
        val score = weights.touchCount * touches / count +
            weights.touchesExtreme * (if (touchesExtreme) 1.0 else 0.0) +
            weights.outsideCount * (1.0 - outside / count) +
            weights.averageDeviation * proximity +
            weights.anchorRecency * recency
        return Line(first, second, slope, intercept, score, touches, outside)
    }

    override val lag: Int
        get() = lookback

    override val isStable: Boolean
        get() = bestLine != null
}

/** Rising or falling support line through swing lows; it never lies above a swing low outside the tolerance. */
class TrendLineSupportIndicator(
    swingLows: AbstractFractalIndicator,
    lookback: Int,
    weights: TrendLineWeights = TrendLineWeights(),
    tolerance: TrendLineTolerance = TrendLineTolerance.Percentage(),
    maxSwingPoints: Int = 64,
) : AbstractTrendLineIndicator(swingLows, lookback, weights, tolerance, maxSwingPoints) {
    override fun violates(projected: Num, swingPrice: Num) = projected > swingPrice
    override fun pickExtreme(a: Num, b: Num) = if (b < a) b else a
    override fun toString() = "TrendLineSupport($lookback) => $value"
}

/** Resistance line through swing highs; it never lies below a swing high outside the tolerance. */
class TrendLineResistanceIndicator(
    swingHighs: AbstractFractalIndicator,
    lookback: Int,
    weights: TrendLineWeights = TrendLineWeights(),
    tolerance: TrendLineTolerance = TrendLineTolerance.Percentage(),
    maxSwingPoints: Int = 64,
) : AbstractTrendLineIndicator(swingHighs, lookback, weights, tolerance, maxSwingPoints) {
    override fun violates(projected: Num, swingPrice: Num) = projected < swingPrice
    override fun pickExtreme(a: Num, b: Num) = if (b > a) b else a
    override fun toString() = "TrendLineResistance($lookback) => $value"
}
