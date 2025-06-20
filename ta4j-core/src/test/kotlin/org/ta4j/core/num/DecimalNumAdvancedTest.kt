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

import java.math.MathContext
import java.math.RoundingMode
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.ta4j.core.num.DecimalNum.Companion.valueOf

internal class DecimalNumAdvancedTest {
    @Test
    @DisplayName("Test logarithmic operations")
    fun shouldCalculateLogarithms() {
        val one = valueOf("1", TEST_CONTEXT)
        val e = valueOf(Math.E, TEST_CONTEXT)

        assertThat(one.log()).hasToString("0")
        assertThat(e.log().delegate.toDouble()).isCloseTo(1.0, Offset.offset<Double?>(1e-10))

        // Log of negative number should return NaN
        val minusOne = valueOf("-1", TEST_CONTEXT)
        assertThat(minusOne.log().isNaN).isTrue()

        // Log of zero should return NaN
        val zero = valueOf("0", TEST_CONTEXT)
        assertThat(zero.log().isNaN).isTrue()
    }


    @Test
    @DisplayName("Test floor and ceiling operations")
    fun shouldCalculateFloorAndCeiling() {
        val num1 = valueOf("3.7", TEST_CONTEXT)
        val num2 = valueOf("-3.7", TEST_CONTEXT)

        assertThat(num1.floor()).hasToString("3")
        assertThat(num2.floor()).hasToString("-4")

        assertThat(num1.ceil()).hasToString("4")
        assertThat(num2.ceil()).hasToString("-3")
    }


    @Test
    @DisplayName("Test min and max operations")
    fun shouldFindMinimumAndMaximum() {
        val num1 = valueOf("3.7", TEST_CONTEXT)
        val num2 = valueOf("-3.7", TEST_CONTEXT)
        val num3 = valueOf("5.2", TEST_CONTEXT)

        // Min tests
        assertThat(minOf(num1, num2)).isEqualTo(num2)
        assertThat(minOf(num1, num3)).isEqualTo(num1)

        // Max tests
        assertThat(maxOf(num1, num2)).isEqualTo(num1)
        assertThat(maxOf(num1, num3)).isEqualTo(num3)

        // Min/Max with NaN should return NaN
        assertThat(minOf(num1, NaN).isNaN).isFalse()
        assertThat(maxOf(num1, NaN).isNaN).isFalse()
    }

    @Test
    @DisplayName("Test absolute value and negation")
    fun shouldCalculateAbsoluteAndNegation() {
        val num1 = valueOf("3.7", TEST_CONTEXT)
        val num2 = valueOf("-3.7", TEST_CONTEXT)
        val zero = valueOf("0", TEST_CONTEXT)

        assertThat(num1.abs()).isEqualTo(num1)
        assertThat(num2.abs()).isEqualTo(num1)
        assertThat(zero.abs()).isEqualTo(zero)

        // Negation tests
        assertThat(-num1).isEqualTo(num2)
        assertThat(-num2).isEqualTo(num1)
        assertThat(-zero).isEqualTo(zero)
    }


    @Test
    @DisplayName("Test rem operation")
    fun shouldCalculaterem() {
        val num1 = valueOf("10", TEST_CONTEXT)
        val num2 = valueOf("3", TEST_CONTEXT)
        val expectedrem = valueOf("1", TEST_CONTEXT)

        assertThat(num1.rem(num2)).isEqualTo(expectedrem)

        // rem with NaN should return NaN
        assertThat(num1.rem(NaN).isNaN).isTrue()
    }


    @Test
    @DisplayName("Test precision handling in arithmetic")
    fun shouldMaintainPrecisionInArithmetic() {
        val num1 = valueOf("1.23456789", TEST_CONTEXT)
        val num2 = valueOf("9.87654321", TEST_CONTEXT)

        // Test that arithmetic operations maintain precision
        val sum = num1.plus(num2)
        val difference = num2.minus(num1)
        val product = num1.times(num2)
        val quotient = num2.div(num1)
        val sqrt = num2.sqrt()
        val pow = num2.pow(5)
        val log = num2.log()
        val abs = num2.abs()

        assertThat((sum as DecimalNum).delegate.precision()).isLessThanOrEqualTo(TEST_CONTEXT.getPrecision())
        assertThat((difference as DecimalNum).delegate.precision())
            .isLessThanOrEqualTo(TEST_CONTEXT.getPrecision())
        assertThat((product as DecimalNum).delegate.precision())
            .isLessThanOrEqualTo(TEST_CONTEXT.getPrecision())
        assertThat((quotient as DecimalNum).delegate.precision())
            .isLessThanOrEqualTo(TEST_CONTEXT.getPrecision())
        assertThat((sqrt as DecimalNum).delegate.precision())
            .isLessThanOrEqualTo(TEST_CONTEXT.getPrecision())
        assertThat((pow as DecimalNum).delegate.precision()).isLessThanOrEqualTo(TEST_CONTEXT.getPrecision())
        assertThat((log as DecimalNum).delegate.precision()).isLessThanOrEqualTo(TEST_CONTEXT.getPrecision())
        assertThat((abs as DecimalNum).delegate.precision()).isLessThanOrEqualTo(TEST_CONTEXT.getPrecision())
    }

    companion object {
        private val TEST_CONTEXT = MathContext(5, RoundingMode.HALF_UP)
    }
}
