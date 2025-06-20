/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2024 Ta4j Organization & respective authors (see AUTHORS)
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
package org.ta4j.core.num

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

internal class DecimalNumEdgeCasesTest {

    companion object {
        private val TEST_CONTEXT = MathContext(32, RoundingMode.HALF_UP)
    }

    @Test
    @DisplayName("Test scientific notation handling")
    fun shouldHandleScientificNotation() {
        // Very small numbers
        val verySmall = DecimalNum.valueOf("1E-30", TEST_CONTEXT)
        val verySmallPlus1 = verySmall.plus(DecimalNum.valueOf("1", TEST_CONTEXT))
        assertThat(verySmallPlus1).hasToString("1.000000000000000000000000000001")

        // Very large numbers
        val veryLarge = DecimalNum.valueOf("1E+30", TEST_CONTEXT)
        val veryLargeMinus1 = veryLarge.minus(DecimalNum.valueOf("1", TEST_CONTEXT))
        assertThat(veryLargeMinus1)
            .hasToString("999999999999999999999999999999")
    }

    @Test
    @DisplayName("Test precision with recurring decimals")
    fun shouldHandleRecurringDecimals() {
        // 1/3 = 0.333...
        val oneThird = DecimalNum.valueOf("1", TEST_CONTEXT)
            .div(DecimalNum.valueOf("3", TEST_CONTEXT))

        // Multiply back by 3
        val shouldBeOne = oneThird.times(DecimalNum.valueOf("3", TEST_CONTEXT))

        // Should equal 1 despite intermediate recurring decimal
        assertThat(shouldBeOne).hasToString("0.99999999999999999999999999999999")
    }

    @Test
    @DisplayName("Test behavior with extreme values")
    fun shouldHandleExtremeValues() {
        val mc = MathContext(5, RoundingMode.HALF_UP)

        // Test extremely close to zero
        val almostZero = DecimalNum.valueOf("0.0000000001", mc)
        assertThat(almostZero.isZero).isFalse()
        assertThat(almostZero.abs()).isEqualTo(almostZero)

        // Test extremely large numbers
        val veryLarge = DecimalNum.valueOf("9".repeat(100), mc)
        val doubled = veryLarge.times(DecimalNum.valueOf("2", mc))
        assertThat(doubled.compareTo(veryLarge) == 1).isTrue()
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "0.0",
            "-0.0",
            "0.00000",
            "-0.00000"
        ]
    )
    @DisplayName("Test zero representations")
    fun shouldHandleZeroRepresentations(zeroStr: String) {
        val num = DecimalNum.valueOf(zeroStr, TEST_CONTEXT)
        assertThat(num.isZero).isTrue()
        assertThat(num.isPositive).isFalse()
        assertThat(num.isNegative).isFalse()
    }

    @Test
    @DisplayName("Test precision handling across operations")
    fun shouldMaintainPrecisionAcrossOperations() {
        val highPrecisionContext = MathContext(50, RoundingMode.HALF_UP)
        val lowPrecisionContext = MathContext(5, RoundingMode.HALF_UP)

        val highPrecisionNum = DecimalNum.valueOf("1.1111111111111111111", highPrecisionContext)
        val lowPrecisionNum = DecimalNum.valueOf("1.1111111111111111111", lowPrecisionContext)

        // Operations between different precisions should use higher precision
        val result = highPrecisionNum.times(lowPrecisionNum)
        assertThat((result as DecimalNum).delegate.precision())
            .isLessThanOrEqualTo(highPrecisionContext.precision)
    }

    @Test
    @DisplayName("Test equality and hash code contracts")
    fun shouldMaintainEqualityContracts() {
        val num1 = DecimalNum.valueOf("1.23", TEST_CONTEXT)
        val num2 = DecimalNum.valueOf("1.23", TEST_CONTEXT)
        val num3 = DecimalNum.valueOf("1.23", MathContext(5, RoundingMode.HALF_UP))
        val differentNum = DecimalNum.valueOf("1.24", TEST_CONTEXT)

        // Equality tests
        assertEquals(num1, num2)
        assertNotEquals(num1, differentNum)

        // HashCode tests
        assertEquals(num1.hashCode(), num2.hashCode())
        assertNotEquals(num1.hashCode(), differentNum.hashCode())

        // Different precision but same value should be equal
        assertEquals(num1, num3)
    }

    @Test
    @DisplayName("Test conversion methods")
    fun shouldConvertCorrectly() {
        val num = DecimalNum.valueOf("123.456", TEST_CONTEXT)

        assertThat(num.intValue()).isEqualTo(123)
        assertThat(num.longValue()).isEqualTo(123L)
        assertThat(num.floatValue()).isEqualTo(123.456f)
        assertThat(num.doubleValue()).isEqualTo(123.456)
        assertThat(num.delegate).isInstanceOf(BigDecimal::class.java)
    }
}
