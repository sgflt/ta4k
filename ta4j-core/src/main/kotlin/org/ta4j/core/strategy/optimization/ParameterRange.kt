/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.strategy.optimization

import kotlin.math.ceil

@JvmRecord
data class ParameterRange(val min: Double, val max: Double, val step: Double) {
    fun countOfSteps(): Int {
        return ceil((this.max - this.min) / this.step).toInt()
    }
}
