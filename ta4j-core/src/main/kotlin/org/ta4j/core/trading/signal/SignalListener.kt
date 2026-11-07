/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.trading.signal

fun interface SignalListener {
    fun onSignal(signal: Signal)
}
