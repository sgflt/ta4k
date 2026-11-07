/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.oscillators.vortex

import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.NumFactory

/**
 * Vortex indicator (Botes and Siepman) with its three series. Register the ones you use.
 *
 * @see <a href="https://school.stockcharts.com/doku.php?id=technical_indicators:vortex_indicator">StockCharts</a>
 */
class VortexFacade(numFactory: NumFactory, barCount: Int = 14) {
    /** +VI */
    val positive: NumericIndicator = VortexLineIndicator(numFactory, true, barCount)

    /** -VI */
    val negative: NumericIndicator = VortexLineIndicator(numFactory, false, barCount)

    /** +VI - -VI */
    val oscillator: NumericIndicator = positive.minus(negative)
}
