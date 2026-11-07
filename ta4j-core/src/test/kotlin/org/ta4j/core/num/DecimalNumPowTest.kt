/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.num

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.ta4j.core.TestUtils.assertNumEquals

internal class DecimalNumPowTest {

    private val precision32 = MathContext(32, RoundingMode.HALF_UP)

    private fun unlimited(value: Double) = DecimalNum.valueOf(BigDecimal.valueOf(value), MathContext.UNLIMITED)

    @Test
    fun nanExponentGivesNaN() {
        assertThat(DecimalNum.valueOf(2).pow(NaN).isNaN).isTrue()
    }

    @Test
    fun primitiveNaNOrInfiniteExponentGivesNaN() {
        val x = DecimalNum.valueOf(2)
        assertThat(x.pow(DoubleNum.valueOf(Double.NaN)).isNaN).isTrue()
        assertThat(x.pow(DoubleNum.valueOf(Double.POSITIVE_INFINITY)).isNaN).isTrue()
    }

    @Test
    fun negativeBaseWithFractionalExponentGivesNaN() {
        assertThat(DecimalNum.valueOf(-4).pow(DecimalNum.valueOf(0.5)).isNaN).isTrue()
        assertThat(unlimited(-4.0).pow(unlimited(-2.5)).isNaN).isTrue()
    }

    @Test
    fun zeroBaseWithNegativeExponentGivesNaN() {
        assertThat(DecimalNum.valueOf(0).pow(DecimalNum.valueOf(-0.5)).isNaN).isTrue()
    }

    @Test
    fun negativeBaseWithWholeExponent() {
        assertNumEquals("-8", DecimalNum.valueOf(-2).pow(DecimalNum.valueOf(3)))
    }

    @Test
    fun negativeFractionalExponent() {
        val x = DecimalNum.valueOf("4", precision32)
        assertNumEquals("0.03125", x.pow(DecimalNum.valueOf("-2.5", precision32)))
    }

    @Test
    fun negativeWholeExponentAtUnlimitedPrecisionGivesReciprocal() {
        assertThat(BigDecimal(unlimited(2.0).pow(unlimited(-1.0)).toString())).isEqualByComparingTo("0.5")
        assertThat(BigDecimal(unlimited(2.0).pow(unlimited(-2.0)).toString())).isEqualByComparingTo("0.25")
        // 1/3 does not terminate; the reciprocal is rounded instead of throwing
        assertThat(unlimited(3.0).pow(unlimited(-1.0)).isNaN).isFalse()
    }

    @Test
    fun wholeExponentKeepsUnlimitedPrecisionExact() {
        val base = BigDecimal("1.23456789012345678901")
        val powered = DecimalNum.valueOf(base, MathContext.UNLIMITED).pow(unlimited(3.0))
        assertThat(BigDecimal(powered.toString())).isEqualByComparingTo(base.pow(3))
    }

    @Test
    fun largeFractionalExponentStaysWithinPrecision() {
        val result = DecimalNum.valueOf("1.01", precision32).pow(DecimalNum.valueOf("262800.5", precision32))
        val power = BigDecimal(result.toString())
        assertThat(power.precision()).isEqualTo(32)
        // log10(1.01^262800.5) = 1135.6..., so the leading digit sits at 10^1135
        assertThat(power.precision() - power.scale() - 1).isEqualTo(1135)
    }

    @Test
    fun doubleNumExponentIsAccepted() {
        assertNumEquals("8", DecimalNum.valueOf(2).pow(DoubleNum.valueOf(3)))
    }
}
