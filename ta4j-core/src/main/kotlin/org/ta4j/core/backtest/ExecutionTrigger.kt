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

import org.ta4j.core.events.MarketEvent
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * Defines when a pending order should be executed based on market events.
 */
interface ExecutionTrigger {
    /**
     * Determines if the pending order should be executed based on the given market event.
     *
     * @param event the market event to check
     * @return true if the order should be executed
     */
    fun shouldExecute(event: MarketEvent): Boolean

    /**
     * Gets the execution price for the order based on the market event.
     *
     * @param event the market event that triggered execution
     * @param numFactory the factory to create Num instances
     * @return the execution price, or null if price cannot be determined
     */
    fun getExecutionPrice(event: MarketEvent, numFactory: NumFactory): Num?
}

