/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.strategy.runtime

import org.ta4j.core.strategy.RuntimeContext

/**
 * Factory for NOOP context
 */
class NOOPRuntimeContextFactory : RuntimeContextFactory {
    override fun createRuntimeContext(): RuntimeContext {
        return NOOPRuntimeContext
    }
}
