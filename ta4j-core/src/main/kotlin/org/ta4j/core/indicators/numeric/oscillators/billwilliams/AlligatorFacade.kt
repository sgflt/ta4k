/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.oscillators.billwilliams

import org.ta4j.core.indicators.numeric.NumericIndicator

/**
 * Bill Williams Alligator: three smoothed (Wilder) moving averages of [price], each displaced into the past, and the
 * Gator Oscillator histograms derived from them. Register the series you use.
 *
 * Displacement reads the average from `shift` bars ago, so there is no look-ahead.
 *
 * @see <a href="https://www.investopedia.com/terms/a/alligatorindicator.asp">Investopedia: Alligator</a>
 * @see <a href="https://www.investopedia.com/terms/g/gatoroscillator.asp">Investopedia: Gator Oscillator</a>
 */
class AlligatorFacade(
    price: NumericIndicator,
    jawPeriod: Int = 13,
    jawShift: Int = 8,
    teethPeriod: Int = 8,
    teethShift: Int = 5,
    lipsPeriod: Int = 5,
    lipsShift: Int = 3,
) {
    /** Blue line: SMMA(13) shifted by 8 bars. */
    val jaw: NumericIndicator = price.mma(jawPeriod).previous(jawShift)

    /** Red line: SMMA(8) shifted by 5 bars. */
    val teeth: NumericIndicator = price.mma(teethPeriod).previous(teethShift)

    /** Green line: SMMA(5) shifted by 3 bars. */
    val lips: NumericIndicator = price.mma(lipsPeriod).previous(lipsShift)

    /** Gator upper histogram: `|jaw - teeth|`. */
    val upperGator: NumericIndicator = jaw.minus(teeth).abs()

    /** Gator lower histogram: `-|teeth - lips|`. */
    val lowerGator: NumericIndicator = teeth.minus(lips).abs().multipliedBy(-1)
}
