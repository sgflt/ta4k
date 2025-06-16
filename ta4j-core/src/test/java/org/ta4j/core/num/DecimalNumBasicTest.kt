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

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

internal class DecimalNumBasicTest {

    companion object {
        private val TEST_CONTEXT = MathContext(5, RoundingMode.HALF_UP)

        // 120 digit precision
        private val SUPER_PRECISION_STRING = "1.234567890" + // 10
                "1234567890".repeat(11) // 110
    }

    @Test
    @DisplayName("Test value creation methods")
    fun shouldCreateValuesCorrectly() {
        // Test string constructor
        val stringNum = DecimalNum.valueOf("123.456", TEST_CONTEXT)
        assertEquals("123.46", stringNum.toString())

        // Test various number types
        val shortNum = DecimalNum.valueOf(123.toShort(), TEST_CONTEXT)
        val intNum = DecimalNum.valueOf(123, TEST_CONTEXT)
        val longNum = DecimalNum.valueOf(123L, TEST_CONTEXT)
        val floatNum = DecimalNum.valueOf(123.456f, TEST_CONTEXT)
        val doubleNum = DecimalNum.valueOf(123.456, TEST_CONTEXT)
        val bigDecimalNum = DecimalNum.valueOf(BigDecimal("123.456"), TEST_CONTEXT)

        assertThat(shortNum).hasToString("123")
        assertThat(intNum).hasToString("123")
        assertThat(longNum).hasToString("123")
        assertThat(floatNum).hasToString("123.46")
        assertThat(doubleNum.delegate).isEqualTo(BigDecimal("123.46"))
        assertThat(bigDecimalNum).hasToString("123.46")

        // Test NaN handling
        assertThrows(NumberFormatException::class.java) { DecimalNum.valueOf("NaN", TEST_CONTEXT) }
        assertThrows(NumberFormatException::class.java) { DecimalNum.valueOf(Double.NaN, TEST_CONTEXT) }
        assertThrows(NumberFormatException::class.java) { DecimalNum.valueOf(Float.NaN, TEST_CONTEXT) }
    }

    @Test
    @DisplayName("Test basic arithmetic operations")
    fun shouldPerformBasicArithmetic() {
        val two = DecimalNum.valueOf("2", TEST_CONTEXT)
        val three = DecimalNum.valueOf("3", TEST_CONTEXT)
        val six = DecimalNum.valueOf("6", TEST_CONTEXT)
        val minusTwo = DecimalNum.valueOf("-2", TEST_CONTEXT)

        // Addition
        assertThat(two.plus(three)).hasToString("5")
        assertThat(two.plus(minusTwo)).hasToString("0")

        // Subtraction
        assertThat(three.minus(two)).hasToString("1")
        assertThat(two.minus(three)).hasToString("-1")

        // Multiplication
        assertThat(two.times(three)).hasToString("6")
        assertThat(two.times(minusTwo)).hasToString("-4")

        // Division
        assertThat(six.div(two)).hasToString("3")
        assertThat(six.div(three)).hasToString("2")

        // Division by zero should return NaN
        assertThat(six.div(DecimalNum.valueOf("0", TEST_CONTEXT)).isNaN).isTrue()
    }

    @Test
    @DisplayName("Test comparison operations")
    fun shouldCompareCorrectly() {
        val two = DecimalNum.valueOf("2", TEST_CONTEXT)
        val three = DecimalNum.valueOf("3", TEST_CONTEXT)
        val twoAgain = DecimalNum.valueOf("2", TEST_CONTEXT)
        val zero = DecimalNum.valueOf("0", TEST_CONTEXT)
        val minusTwo = DecimalNum.valueOf("-2", TEST_CONTEXT)

        // Zero checks
        assertTrue(zero.isZero)
        assertFalse(two.isZero)

        // Negative checks
        assertTrue(minusTwo.isNegative)
        assertFalse(two.isNegative)

        // Positive checks
        assertTrue(two.isPositive)
        assertFalse(minusTwo.isPositive)
    }

    @Test
    @DisplayName("Test power and root operations")
    fun shouldCalculatePowersAndRoots() {
        val two = DecimalNum.valueOf("2", TEST_CONTEXT)
        val three = DecimalNum.valueOf("3", TEST_CONTEXT)
        val nine = DecimalNum.valueOf("9", TEST_CONTEXT)
        val minusNine = DecimalNum.valueOf("-9", TEST_CONTEXT)

        // Integer powers
        assertThat(two.pow(3)).hasToString("8")
        assertThat(three.pow(2)).hasToString("9")

        // Square root
        assertThat(nine.sqrt()).hasToString("3.0")

        // Square root of negative number should return NaN
        assertThat(minusNine.sqrt().isNaN).isTrue()

        // Power with Num
        assertThat(two.pow(three)).hasToString("8.0")
    }

    @Test
    fun testPowLargeBase() {
        val x: Num = DecimalNum.valueOf(SUPER_PRECISION_STRING)
        val n: Num = DecimalNum.valueOf("512")
        val result = x.pow(n)
        assertThat(result).isEqualTo(
            DecimalNum.valueOf(
                "71724632698264595311439425390811606219342673848.4006139819755840062853530122432564321605895163413448768150879157699962725"
            )
        )
        assertEquals(120, (result.delegate as BigDecimal).precision())
        assertEquals(120, (result as DecimalNum).mathContext.precision)
    }
}