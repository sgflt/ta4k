/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.api.callback

import org.ta4j.core.events.TickReceived

/**
 * @author Lukáš Kvídera
 */
fun interface TickListener {
    fun onTick(tick: TickReceived)
}
