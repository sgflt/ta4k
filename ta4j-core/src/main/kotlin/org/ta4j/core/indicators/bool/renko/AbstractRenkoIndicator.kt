/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.bool.renko

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.bool.BooleanIndicator
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num

/**
 * Counts consecutive Renko bricks of [price]. A brick forms whenever price moves [pointSize] beyond
 * the last brick's close; a brick in the other direction resets the opposite count.
 */
abstract class AbstractRenkoIndicator(
    private val price: NumericIndicator,
    pointSize: Number,
    protected val brickCount: Int,
) : BooleanIndicator() {

    private val pointSize: Num = price.numFactory.numOf(pointSize)
    private var lastBrickClose: Num? = null

    protected var consecutiveUp = 0
        private set
    protected var consecutiveDown = 0
        private set

    init {
        require(this.pointSize.isPositive) { "Point size must be positive, but was: $pointSize" }
        require(brickCount > 0) { "Brick count must be positive, but was: $brickCount" }
    }

    /** @return the indicator value from the current brick counts */
    protected abstract fun evaluate(): Boolean

    override fun updateState(bar: Bar) {
        price.onBar(bar)
        val current = price.value
        val last = lastBrickClose
        if (last == null) {
            lastBrickClose = current
        } else if (!current.isNaN) {
            var close: Num = last
            while (current >= close + pointSize) {
                close += pointSize
                consecutiveUp++
                consecutiveDown = 0
            }
            while (current <= close - pointSize) {
                close -= pointSize
                consecutiveDown++
                consecutiveUp = 0
            }
            lastBrickClose = close
        }
        value = evaluate()
    }

    override val lag = 1

    override val isStable: Boolean
        get() = lastBrickClose != null && price.isStable

    override fun toString() = "${javaClass.simpleName}($pointSize, $brickCount) => $value"
}

/** True after at least [brickCount] consecutive bullish Renko bricks. */
class RenkoUpIndicator(price: NumericIndicator, pointSize: Number, brickCount: Int) :
    AbstractRenkoIndicator(price, pointSize, brickCount) {
    override fun evaluate() = consecutiveUp >= brickCount
}

/** True after at least [brickCount] consecutive bearish Renko bricks. */
class RenkoDownIndicator(price: NumericIndicator, pointSize: Number, brickCount: Int) :
    AbstractRenkoIndicator(price, pointSize, brickCount) {
    override fun evaluate() = consecutiveDown >= brickCount
}

/** True after at least [brickCount] consecutive Renko bricks in either direction. */
class RenkoXIndicator(price: NumericIndicator, pointSize: Number, brickCount: Int) :
    AbstractRenkoIndicator(price, pointSize, brickCount) {
    override fun evaluate() = maxOf(consecutiveUp, consecutiveDown) >= brickCount
}
