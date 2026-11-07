/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.oscillators.aroon

import org.ta4j.core.api.Indicators.lowPrice
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.indicators.numeric.helpers.LowestValueIndicator
import org.ta4j.core.num.NaN
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * Aroon down indicator.
 *
 * @see [chart_school:technical_indicators:aroon](http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:aroon)
 */
class AroonDownIndicator(
    numFactory: NumFactory,
    private val lowIndicator: NumericIndicator,
    private val barCount: Int,
) :
    NumericIndicator(numFactory) {
    private val lowestLowValueIndicator: LowestValueIndicator

    private var index = 0
    private val previousValues: ArrayList<Num> // TODO CircularNumArray

    override val lag: Int
        get() = barCount

    /**
     * Constructor.
     *
     * @param lowIndicator the indicator for the low price (default
     * [LowPriceIndicator])
     * @param barCount the time frame
     */
    init {
        previousValues = ArrayList<Num>(barCount).apply {
            repeat(barCount) { add(NaN) }
        }

        lowestLowValueIndicator = lowIndicator.lowest(barCount + 1)
    }

    /**
     * Default Constructor with `lowPriceIndicator` =
     * [LowPriceIndicator].
     *
     * @param numFactory the bar numFactory
     * @param barCount the time frame
     */
    constructor(numFactory: NumFactory, barCount: Int) : this(numFactory, lowPrice(), barCount)

    private fun calculate(): Num {
        val currentLow = lowIndicator.value
        previousValues[getIndex(index)] = currentLow

        if (currentLow.isNaN) {
            return NaN
        }

        val lowestValue = lowestLowValueIndicator.value

        val barCountFromLastMinimum = countBarsBetweenLows(lowestValue)
        return numFactory.numOf((barCount - barCountFromLastMinimum).toDouble() / barCount * 100.0)
    }

    private fun countBarsBetweenLows(lowestValue: Num?): Int {
        var i = getIndex(index)
        var barDistance = 0
        while (barDistance < barCount) {
            if (previousValues[getIndex(barCount + i)] == lowestValue) {
                return barDistance
            }
            barDistance++
            i--
        }
        return barCount
    }

    private fun getIndex(i: Int): Int {
        return i % barCount
    }

    public override fun updateState(bar: Bar) {
        ++index
        lowIndicator.onBar(bar)
        lowestLowValueIndicator.onBar(bar)
        value = calculate()
    }

    override val isStable: Boolean
        get() = index >= barCount && lowIndicator.isStable && lowestLowValueIndicator.isStable

    override fun toString() = "AroonDown($lowIndicator) => $value"
}
