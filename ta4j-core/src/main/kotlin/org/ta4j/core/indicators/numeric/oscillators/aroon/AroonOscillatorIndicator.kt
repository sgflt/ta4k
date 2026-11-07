/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.oscillators.aroon

import org.ta4j.core.api.Indicators
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.NumFactory

/**
 * Aroon Oscillator.
 *
 * @see [
 * http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:aroon_oscillator](http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:aroon_oscillator)
 */
class AroonOscillatorIndicator : NumericIndicator {
    /** @return the [.aroonUpIndicator]
     */
    val aroonUpIndicator: AroonUpIndicator

    /** @return the [.aroonDownIndicator]
     */
    val aroonDownIndicator: AroonDownIndicator


    /**
     * Constructor.
     *
     * @param numFactory the bar numFactory
     * @param barCount the number of periods used for the indicators
     */
    constructor(numFactory: NumFactory, barCount: Int) : super(numFactory) {
        aroonUpIndicator = Indicators.aroonUp(barCount)
        aroonDownIndicator = Indicators.aroonDown(barCount)
    }


    constructor(numFactory: NumFactory, indicator: NumericIndicator, barCount: Int) : this(
        numFactory,
        indicator,
        indicator,
        barCount
    )


    constructor(
        numFactory: NumFactory,
        downIndicator: NumericIndicator,
        upIndicator: NumericIndicator,
        barCount: Int,
    ) : super(numFactory) {
        aroonUpIndicator = upIndicator.aroonUp(barCount)
        aroonDownIndicator = downIndicator.aroonDown(barCount)
    }


    private fun calculate() = aroonUpIndicator.value.minus(aroonDownIndicator.value)


    override fun toString() = "AROON($aroonDownIndicator, $aroonUpIndicator) => $value"


    public override fun updateState(bar: Bar) {
        aroonUpIndicator.onBar(bar)
        aroonDownIndicator.onBar(bar)
        value = calculate()
    }


    override val isStable: Boolean
        get() = aroonUpIndicator.isStable && aroonDownIndicator.isStable

    override val lag: Int
        get() = aroonUpIndicator.lag
}
