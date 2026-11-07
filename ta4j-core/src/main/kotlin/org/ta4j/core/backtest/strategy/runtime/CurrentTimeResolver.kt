/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.strategy.runtime

import java.time.Instant
import org.ta4j.core.strategy.RuntimeContext
import org.ta4j.core.strategy.RuntimeValueResolver

/**
 * @author Lukáš Kvídera
 */
class CurrentTimeResolver : RuntimeValueResolver<Instant?> {
    override fun resolve(context: RuntimeContext): Instant? {
        return context.getValue(RuntimeContextKeys.CURRENT_TIME) as Instant?
    }
}
