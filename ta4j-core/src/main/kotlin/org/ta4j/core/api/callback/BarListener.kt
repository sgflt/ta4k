/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.api.callback

import org.ta4j.core.api.series.Bar

/**
 * @author Lukáš Kvídera
 */
fun interface BarListener {
    fun onBar(bar: Bar)
}
