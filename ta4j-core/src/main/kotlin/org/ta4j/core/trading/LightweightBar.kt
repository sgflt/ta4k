/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.trading

import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.TimeFrame
import org.ta4j.core.num.Num

/**
 * Live trading implementation of a [org.ta4j.core.api.series.Bar].
 */
@JvmRecord
internal data class LightweightBar(
    override val timeFrame: TimeFrame,
    override val beginTime: Instant,
    override val endTime: Instant,
    override val openPrice: Num,
    override val highPrice: Num,
    override val lowPrice: Num,
    override val closePrice: Num,
    override val volume: Num,
) : Bar {
    override fun toString(): String {
        return String.format(
            "{end time: %1s, close price: %2\$f, open price: %3\$f, low price: %4\$f, high price: %5\$f, volume: %6\$f}",
            this.endTime.atZone(ZoneId.systemDefault()),
            this.closePrice.doubleValue(),
            this.openPrice.doubleValue(),
            this.lowPrice.doubleValue(),
            this.highPrice.doubleValue(),
            this.volume.doubleValue()
        )
    }


    override val timePeriod: Duration
        get() = Duration.between(this.beginTime, this.endTime)
}
