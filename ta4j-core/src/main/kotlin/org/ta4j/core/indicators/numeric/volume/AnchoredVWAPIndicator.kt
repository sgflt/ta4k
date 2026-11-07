/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.volume

import java.time.Instant
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.bool.BooleanIndicator
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.NaN
import org.ta4j.core.num.Num

/**
 * VWAP accumulated from an anchor bar on.
 *
 * The anchor is either a fixed time (accumulation starts with the first bar that begins at or after it, the value
 * is NaN before) or a signal (every bar where it is true starts a new accumulation, e.g. the first bar of a session).
 * The indicator is stable once anchored.
 */
class AnchoredVWAPIndicator private constructor(
    price: NumericIndicator,
    volume: NumericIndicator,
    private val anchorTime: Instant?,
    private val anchorSignal: BooleanIndicator?,
) : AbstractVWAPIndicator(price, volume) {

    /** Accumulates from the first bar beginning at or after [anchorTime]. */
    constructor(price: NumericIndicator, volume: NumericIndicator, anchorTime: Instant) :
        this(price, volume, anchorTime, null)

    /** Restarts the accumulation on every bar where [anchorSignal] is true. */
    constructor(price: NumericIndicator, volume: NumericIndicator, anchorSignal: BooleanIndicator) :
        this(price, volume, null, anchorSignal)

    private var anchored = false

    override fun updateWindow(bar: Bar, price: Num, volume: Num) {
        if (isAnchor(bar)) {
            reset()
            anchored = true
        }
        if (anchored) {
            add(price, volume)
        }
    }

    private fun isAnchor(bar: Bar): Boolean {
        if (anchorSignal != null) {
            anchorSignal.onBar(bar)
            return anchorSignal.value
        }
        return !anchored && !bar.beginTime.isBefore(anchorTime)
    }

    override fun updateState(bar: Bar) {
        super.updateState(bar)
        if (!anchored) {
            value = NaN
            variance = NaN
        }
    }

    override val lag = 1

    override val isStable: Boolean
        get() = anchored && price.isStable && volume.isStable

    override fun toString() = "AnchoredVWAP(${anchorTime ?: anchorSignal}) => $value"
}
