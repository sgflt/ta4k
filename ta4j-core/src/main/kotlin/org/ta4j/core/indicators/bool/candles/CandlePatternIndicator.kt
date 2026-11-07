/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.bool.candles

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.bool.BooleanIndicator
import org.ta4j.core.num.Num

/**
 * Base for candlestick patterns spanning [patternLength] bars.
 *
 * Keeps the pattern bars plus [averagePeriod] bars before each of them, and judges candles against
 * the average body and range of the [averagePeriod] bars preceding that candle (TA-Lib style):
 *
 * | check          | rule                                   |
 * |----------------|----------------------------------------|
 * | long body      | body > average body                    |
 * | short body     | body < 0.5 * average body              |
 * | long shadow    | shadow > 2 * average body              |
 * | short shadow   | shadow <= 0.1 * average range          |
 * | near           | abs(a - b) <= 0.1 * average range      |
 *
 * Subclasses implement [matches]; `candle(0)` is the current bar, `candle(1)` the one before, ...
 */
abstract class CandlePatternIndicator(
    private val patternLength: Int,
    private val averagePeriod: Int = DEFAULT_AVERAGE_PERIOD,
) : BooleanIndicator() {

    private val window = ArrayDeque<Bar>()
    // the oldest pattern candle needs averagePeriod bars before it
    private val capacity = patternLength + averagePeriod

    init {
        require(patternLength > 0) { "Pattern length must be positive" }
        require(averagePeriod > 0) { "Average period must be positive" }
    }

    /** @return whether the last [patternLength] bars form the pattern */
    protected abstract fun matches(): Boolean

    override fun updateState(bar: Bar) {
        window.addLast(bar)
        if (window.size > capacity) {
            window.removeFirst()
        }
        value = isStable && matches()
    }

    override val lag = patternLength + averagePeriod

    override val isStable: Boolean
        get() = window.size == capacity

    /** @param back 0 for the current bar, 1 for the previous one, ... */
    protected fun candle(back: Int): Bar = window[window.size - 1 - back]

    protected fun body(bar: Bar): Num = (bar.closePrice - bar.openPrice).abs()

    protected fun bodyTop(bar: Bar): Num = if (bar.closePrice > bar.openPrice) bar.closePrice else bar.openPrice

    protected fun bodyBottom(bar: Bar): Num = if (bar.closePrice < bar.openPrice) bar.closePrice else bar.openPrice

    protected fun upperShadow(bar: Bar): Num = bar.highPrice - bodyTop(bar)

    protected fun lowerShadow(bar: Bar): Num = bodyBottom(bar) - bar.lowPrice

    protected fun isLongBody(back: Int): Boolean = body(candle(back)) > averageBody(back)

    protected fun isShortBody(back: Int): Boolean = body(candle(back)) < averageBody(back) * half(back)

    protected fun isLongShadow(back: Int, shadow: Num): Boolean = shadow > averageBody(back) * two(back)

    protected fun isShortShadow(back: Int, shadow: Num): Boolean = shadow <= averageRange(back) * tenth(back)

    protected fun isNear(back: Int, a: Num, b: Num): Boolean = (a - b).abs() <= averageRange(back) * tenth(back)

    /** Long body with short shadows on both sides. */
    protected fun isMarubozu(back: Int): Boolean {
        val bar = candle(back)
        return isLongBody(back) && isShortShadow(back, upperShadow(bar)) && isShortShadow(back, lowerShadow(bar))
    }

    /** The point [penetration] of the way from [from] to [to]. */
    protected fun pointBetween(from: Num, to: Num, penetration: Num): Num = from + (to - from) * penetration

    private fun averageBody(back: Int): Num = average(back) { body(it) }

    private fun averageRange(back: Int): Num = average(back) { it.highPrice - it.lowPrice }

    private fun average(back: Int, measure: (Bar) -> Num): Num {
        val numFactory = candle(back).closePrice.numFactory
        val end = window.size - 1 - back
        var sum = numFactory.zero()
        for (i in end - averagePeriod until end) {
            sum += measure(window[i])
        }
        return sum / numFactory.numOf(averagePeriod)
    }

    private fun half(back: Int) = candle(back).closePrice.numFactory.numOf(0.5)
    private fun two(back: Int) = candle(back).closePrice.numFactory.two()
    private fun tenth(back: Int) = candle(back).closePrice.numFactory.numOf(0.1)

    override fun toString() = "${javaClass.simpleName} => $value"

    companion object {
        const val DEFAULT_AVERAGE_PERIOD = 5
    }
}
