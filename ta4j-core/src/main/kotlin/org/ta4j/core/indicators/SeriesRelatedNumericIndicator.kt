/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators

import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.NumFactory

/**
 * Indicator that fetches data from series directly
 */
abstract class SeriesRelatedNumericIndicator
/**
 * Constructor.
 *
 * @param numFactory the bar numFactory
 */
protected constructor(numFactory: NumFactory) : NumericIndicator(numFactory) {
    override val isStable get() = !value.isNaN
}
