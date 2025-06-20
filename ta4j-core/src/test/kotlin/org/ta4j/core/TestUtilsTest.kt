/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2024 Ta4j Organization & respective
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
package org.ta4j.core

import java.math.BigDecimal
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.TestUtils.assertNumEquals
import org.ta4j.core.TestUtils.assertNumNotEquals
import org.ta4j.core.num.NumFactory

internal class TestUtilsTest {

    companion object {
        private const val STRING_DOUBLE = "1234567890.12345"
        private const val DIFF_STRING_DOUBLE = "1234567890.12346"
        private val BIG_DECIMAL_DOUBLE = BigDecimal(STRING_DOUBLE)
        private val DIFF_BIG_DECIMAL_DOUBLE = BigDecimal(DIFF_STRING_DOUBLE)
        private const val A_INT = 1234567890
        private const val DIFF_INT = 1234567891
        private const val A_DOUBLE = 1234567890.1234
        private const val DIFF_DOUBLE = 1234567890.1235
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun testStringNum(numFactory: NumFactory) {
        val numStringDouble = numFactory.numOf(BIG_DECIMAL_DOUBLE)
        val diffNumStringDouble = numFactory.numOf(DIFF_BIG_DECIMAL_DOUBLE)

        assertNumEquals(STRING_DOUBLE, numStringDouble)
        assertNumNotEquals(STRING_DOUBLE, diffNumStringDouble)
        assertNumNotEquals(DIFF_STRING_DOUBLE, numStringDouble)
        assertNumEquals(DIFF_STRING_DOUBLE, diffNumStringDouble)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun testNumNum(numFactory: NumFactory) {
        val numStringDouble = numFactory.numOf(BIG_DECIMAL_DOUBLE)
        val diffNumStringDouble = numFactory.numOf(DIFF_BIG_DECIMAL_DOUBLE)

        assertNumEquals(numStringDouble, numStringDouble)
        assertNumNotEquals(numStringDouble, diffNumStringDouble)
        assertNumNotEquals(diffNumStringDouble, numStringDouble)
        assertNumEquals(diffNumStringDouble, diffNumStringDouble)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun testIntNum(numFactory: NumFactory) {
        val numInt = numFactory.numOf(A_INT)
        val diffNumInt = numFactory.numOf(DIFF_INT)

        assertNumEquals(A_INT, numInt)
        assertNumNotEquals(A_INT, diffNumInt)
        assertNumNotEquals(DIFF_INT, numInt)
        assertNumEquals(DIFF_INT, diffNumInt)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun testDoubleNum(numFactory: NumFactory) {
        val numDouble = numFactory.numOf(A_DOUBLE)
        val diffNumDouble = numFactory.numOf(DIFF_DOUBLE)

        assertNumEquals(A_DOUBLE, numDouble)
        assertNumNotEquals(A_DOUBLE, diffNumDouble)
        assertNumNotEquals(DIFF_DOUBLE, numDouble)
        assertNumEquals(DIFF_DOUBLE, diffNumDouble)
    }
}
