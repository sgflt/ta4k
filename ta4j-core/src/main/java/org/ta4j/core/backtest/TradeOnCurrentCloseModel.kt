/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2023 Ta4j Organization & respective
 * authors (see AUTHORS)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.ta4j.core.backtest

import org.ta4j.core.backtest.strategy.runtime.CurrentPriceResolver
import org.ta4j.core.backtest.strategy.runtime.CurrentTimeResolver
import org.ta4j.core.num.Num
import org.ta4j.core.strategy.RuntimeContext

/**
 * An execution model for [BacktestExecutor] objects.
 *
 * Executes trades on the current bar being considered using the closing price.
 *
 * This is used for strategies that explicitly trade just before the bar closes
 * at index `t`, in order to execute new or close existing trades as close as
 * possible to the closing price.
 */
class TradeOnCurrentCloseModel : TradeExecutionModel {
    override fun enter(runtimeContext: RuntimeContext, tradingRecord: TradingRecord, amount: Num) {
        tradingRecord.enter(
            CURRENT_TIME_RESOLVER.resolve(runtimeContext)!!,
            CURRENT_PRICE_RESOLVER.resolve(runtimeContext)!!,
            amount
        )
    }


    override fun exit(runtimeContext: RuntimeContext, tradingRecord: TradingRecord, amount: Num) {
        tradingRecord.exit(
            CURRENT_TIME_RESOLVER.resolve(runtimeContext)!!,
            CURRENT_PRICE_RESOLVER.resolve(runtimeContext)!!,
            amount
        )
    }

    companion object {
        val CURRENT_TIME_RESOLVER = CurrentTimeResolver()
        val CURRENT_PRICE_RESOLVER = CurrentPriceResolver()
    }
}
