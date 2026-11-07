/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.strategy

import org.ta4j.core.indicators.IndicatorContexts
import org.ta4j.core.indicators.TimeFrame

@JvmRecord
data class DefaultStrategy(
    override val name: String,
    override val timeFrames: Set<TimeFrame>,
    override val entryRule: Rule,
    override val exitRule: Rule,
    val indicatorContexts: IndicatorContexts,
) : Strategy {
    override val isStable
        get() = indicatorContexts.isStable
}
