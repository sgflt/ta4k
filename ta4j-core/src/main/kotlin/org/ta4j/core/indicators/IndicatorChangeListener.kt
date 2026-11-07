/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators

import java.time.Instant
import org.ta4j.core.api.Indicator
import org.ta4j.core.indicators.IndicatorContext.IndicatorIdentification

fun interface IndicatorChangeListener {
    fun accept(tick: Instant, indicatorId: IndicatorIdentification, indicator: Indicator<*>)
}
