/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.strategy.runtime

import org.ta4j.core.strategy.RuntimeContext

/**
 * Creates RuntimeContext related to some strategy or multiple strategies.
 */
fun interface RuntimeContextFactory {
    fun createRuntimeContext(): RuntimeContext
}
