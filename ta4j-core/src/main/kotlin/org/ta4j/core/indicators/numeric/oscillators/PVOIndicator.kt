/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.oscillators

import org.ta4j.core.api.Indicators
import org.ta4j.core.indicators.numeric.average.PPOIndicator
import org.ta4j.core.num.NumFactory

/**
 * Percentage Volume Oscillator (PVO) indicator.
 *
 * <pre>
 * ((12-day EMA of Volume - 26-day EMA of Volume) / 26-day EMA of Volume) x 100
</pre> *
 *
 * @see [
 * https://school.stockcharts.com/doku.php?id=technical_indicators:percentage_volume_oscillator_pvo
](https://school.stockcharts.com/doku.php?id=technical_indicators:percentage_volume_oscillator_pvo) *
 */
class PVOIndicator(
    numFactory: NumFactory,
    volumeBarCount: Int,
    shortBarCount: Int = 12,
    longBarCount: Int = 26,
) : PPOIndicator(
    numFactory = numFactory,
    indicator = Indicators.extended(numFactory).volume().runningTotal(volumeBarCount),
    shortBarCount = shortBarCount,
    longBarCount = longBarCount,
)
