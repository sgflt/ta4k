/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2025 Ta4j Organization & respective authors (see AUTHORS)
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
package org.ta4j.core.backtest.analysis

import java.time.Instant
import java.util.*
import org.ta4j.core.backtest.Position
import org.ta4j.core.backtest.TradingRecord
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

class Returns(
    private val numFactory: NumFactory,
    private val type: ReturnType,
) {
    private val returnHistory = mutableListOf<ReturnRange>()

    private data class ReturnRange(
        val startTime: Instant,
        val endTime: Instant,
        val returns: NavigableMap<Instant, Num>,
    )

    constructor(numFactory: NumFactory, position: Position, type: ReturnType) : this(numFactory, type) {
        position.entry?.let {
            returnHistory.add(
                ReturnRange(
                    it.whenExecuted,
                    position.exit?.whenExecuted ?: Instant.MAX,
                    calculatePositionReturns(position)
                )
            )
        }
    }

    constructor(numFactory: NumFactory, tradingRecord: TradingRecord, type: ReturnType) : this(numFactory, type) {
        tradingRecord.positions
            .filter { it.entry != null }
            .forEach { position ->
                val returns = calculatePositionReturns(position)
                returnHistory.add(
                    ReturnRange(
                        position.entry!!.whenExecuted,
                        position.exit?.whenExecuted ?: Instant.MAX,
                        returns
                    )
                )
            }
    }

    fun getValue(time: Instant): Num = returnHistory
        .filter { !time.isBefore(it.startTime) && !time.isAfter(it.endTime) }
        .map { it.returns.getOrDefault(time, numFactory.zero()) }
        .fold(numFactory.zero()) { acc, num -> acc.plus(num) }

    val values: List<Num>
        get() {
            val values = mutableListOf(numFactory.zero())

            val allTimestamps = returnHistory
                .flatMap { it.returns.keys }
                .distinct()
                .sorted()

            values.addAll(allTimestamps.map { getValue(it) })
            return values
        }


    val size: Int
        get() = returnHistory.size

    private fun calculatePositionReturns(position: Position): NavigableMap<Instant, Num> =
        position.getReturns(type)

    enum class ReturnType {
        LOG {
            override fun calculate(xNew: Num, xOld: Num, numFactory: NumFactory): Num =
                (xNew / (xOld)).log()
        },
        ARITHMETIC {
            override fun calculate(xNew: Num, xOld: Num, numFactory: NumFactory): Num =
                xNew / xOld - numFactory.one()
        };

        abstract fun calculate(xNew: Num, xOld: Num, numFactory: NumFactory): Num
    }
}
