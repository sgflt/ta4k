/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.strategy.runtime

import org.ta4j.core.num.Num
import org.ta4j.core.strategy.RuntimeContext
import org.ta4j.core.strategy.RuntimeValueResolver

/**
 * @author Lukáš Kvídera
 */
class CurrentPriceResolver : RuntimeValueResolver<Num?> {
    override fun resolve(context: RuntimeContext): Num? {
        return context.getValue(RuntimeContextKeys.CURRENT_PRICE) as Num?
    }
}
