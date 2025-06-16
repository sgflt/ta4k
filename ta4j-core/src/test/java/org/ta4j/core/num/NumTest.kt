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
package org.ta4j.core.num

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.TestUtils.assertNumEquals
import org.ta4j.core.TestUtils.assertNumNotEquals
import java.io.IOException
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode.HALF_UP
import java.util.*

internal class NumTest {

    companion object {
        private const val HIGH_PRECISION = 128
        private val HIGH_PRECISION_CONTEXT = MathContext(HIGH_PRECISION, HALF_UP)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun testZero(numFactory: NumFactory) {
        val anyNaNNum = org.ta4j.core.num.NaN
        val anyDecimalNum = DecimalNum.valueOf(3)
        val anyDoubleNum = DoubleNum.valueOf(3)

        assertNumEquals(org.ta4j.core.num.NaN, anyNaNNum.numFactory.zero())
        assertNumEquals(0, numFactory.numOf(3).numFactory.zero())
        assertNumEquals(0, anyDecimalNum.numFactory.zero())
        assertNumEquals(0, anyDoubleNum.numFactory.zero())
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun testOne(numFactory: NumFactory) {
        val anyNaNNum = org.ta4j.core.num.NaN
        val anyDecimalNum = DecimalNum.valueOf(3)
        val anyDoubleNum = DoubleNum.valueOf(3)

        assertNumEquals(org.ta4j.core.num.NaN, anyNaNNum.numFactory.one())
        assertNumEquals(1, numFactory.numOf(3).numFactory.one())
        assertNumEquals(1, anyDecimalNum.numFactory.one())
        assertNumEquals(1, anyDoubleNum.numFactory.one())
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun testHundred(numFactory: NumFactory) {
        val anyNaNNum = org.ta4j.core.num.NaN
        val anyDecimalNum = DecimalNum.valueOf(3)
        val anyDoubleNum = DoubleNum.valueOf(3)

        assertNumEquals(org.ta4j.core.num.NaN, anyNaNNum.numFactory.hundred())
        assertNumEquals(100, numFactory.numOf(3).numFactory.hundred())
        assertNumEquals(100, anyDecimalNum.numFactory.hundred())
        assertNumEquals(100, anyDoubleNum.numFactory.hundred())
    }

    @Test
    fun testStringNumFail() {
        assertThatThrownBy { assertNumEquals("1.234", DecimalNum.valueOf(4.321)) }
            .isInstanceOf(AssertionError::class.java)
    }

    @Test
    fun testStringNumPass() {
        assertNumEquals("1.234", DecimalNum.valueOf(1.234))
    }

    @Test
    fun testDecimalNumPrecision() {
        val highPrecisionString =
            "1.928749238479283749238472398472936872364823749823749238749238749283749238472983749238749832749274"
        val num = DecimalNumFactory.getInstance(HIGH_PRECISION).numOf(highPrecisionString)
        val highPrecisionNum = DecimalNum.valueOf(highPrecisionString, HIGH_PRECISION_CONTEXT)

        assertThat(highPrecisionNum.matches(num, 17)).isTrue()

        val fromNum = BigDecimal(num.toString())
        if (num is DoubleNum) {
            assertThat(fromNum.precision()).isEqualTo(17)
            assertThat(highPrecisionNum.matches(num, 17)).isTrue()
            assertThat(highPrecisionNum.matches(num, 18)).isFalse()
        }

        if (num is DecimalNum) {
            assertThat(fromNum.precision()).isEqualTo(97)
            assertThat(highPrecisionNum.matches(num, 10000)).isTrue()
        }
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun testValueOf(numFactory: NumFactory) {
        assertNumEquals(0.33333333333333333332, numFactory.numOf(0.33333333333333333332))
        assertNumEquals(1, numFactory.numOf(1.0))
        assertNumEquals(2.54, numFactory.numOf(BigDecimal("2.54")))

        assertNumEquals(0.33, numFactory.numOf(0.33))
        assertNumEquals(1, numFactory.numOf(1))
        assertNumEquals(2.54, numFactory.numOf(BigDecimal("2.54")))
    }

    @Test
    fun testMultiplicationSymmetrically() {
        val decimalFromString = DecimalNum.valueOf("0.33")
        val decimalFromDouble = DecimalNum.valueOf(45.33)
        assertThat(decimalFromString.times(decimalFromDouble))
            .isEqualTo(decimalFromDouble.times(decimalFromString))

        val doubleNumFromString = DoubleNum.valueOf("0.33")
        val doubleNumFromDouble = DoubleNum.valueOf(10.33)
        assertNumEquals(
            doubleNumFromString.times(doubleNumFromDouble),
            doubleNumFromDouble.times(doubleNumFromString)
        )
    }

    @Test
    fun testFailDifferentNumsAdd() {
        val a = DecimalNum.valueOf(12)
        val b = DoubleNum.valueOf(12)
        assertThatThrownBy { a.plus(b) }
            .isInstanceOf(ClassCastException::class.java)
    }

    @Test
    fun testFailNaNtoInt() {
        assertThatThrownBy { org.ta4j.core.num.NaN.intValue() }
            .isInstanceOf(UnsupportedOperationException::class.java)
    }

    @Test
    fun testFailNaNtoLong() {
        assertThatThrownBy { org.ta4j.core.num.NaN.longValue() }
            .isInstanceOf(UnsupportedOperationException::class.java)
    }

    @Test
    fun testNaN() {
        val a = org.ta4j.core.num.NaN
        val eleven = DecimalNum.valueOf(11)

        var mustBeNaN = a.plus(eleven)
        assertNumEquals(mustBeNaN, org.ta4j.core.num.NaN)

        mustBeNaN = a.minus(eleven)
        assertNumEquals(mustBeNaN, org.ta4j.core.num.NaN)

        mustBeNaN = a.div(a)
        assertNumEquals(mustBeNaN, org.ta4j.core.num.NaN)

        mustBeNaN = a.times(org.ta4j.core.num.NaN)
        assertNumEquals(mustBeNaN, org.ta4j.core.num.NaN)

        mustBeNaN = a.pow(12)
        assertNumEquals(mustBeNaN, org.ta4j.core.num.NaN)

        mustBeNaN = a.pow(a)
        assertNumEquals(mustBeNaN, org.ta4j.core.num.NaN)

        assertThat(a.doubleValue()).isNaN()
        assertThat(a.floatValue()).isNaN()
        assertThat(a).isEqualTo(org.ta4j.core.num.NaN)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun testArithmetic(numFactory: NumFactory) {
        val ten = numFactory.numOf(10)
        val million = numFactory.numOf(1000000)
        assertNumEquals(10, ten)
        assertNumEquals("1000000.0", million)

        val zero = ten.minus(ten)
        assertNumEquals(0, zero)

        val hundred = ten.times(ten)
        assertNumEquals(100, hundred)

        val hundredMillion = hundred.times(million)
        assertNumEquals(100000000, hundredMillion)

        assertNumEquals(hundredMillion.div(hundred), million)
        assertNumEquals(0, hundredMillion.rem(hundred))

        val five = numFactory.numOf(5)
        val zeroDotTwo = numFactory.numOf(0.2)
        val fiveHundred54 = numFactory.numOf(554)
        assertNumEquals(0, hundredMillion.rem(five))

        assertNumEquals(0.00032, zeroDotTwo.pow(5))
        assertNumEquals(0.7247796636776955, zeroDotTwo.pow(zeroDotTwo))
        assertNumEquals(1.37972966146, zeroDotTwo.pow(numFactory.numOf(-0.2)))

        assertThat(five).isEqualTo(numFactory.numOf(5))
        assertThat(five).isEqualTo(numFactory.numOf(5.0))
        assertThat(five).isEqualTo(numFactory.numOf(5.toFloat()))
        assertThat(five).isEqualTo(numFactory.numOf(5.toShort()))

        assertThat(five).isNotEqualTo(numFactory.numOf(4.9))
        assertThat(five).isNotEqualTo(numFactory.numOf(6))
        assertThat(five).isNotEqualTo(numFactory.numOf(15.toFloat()))
        assertThat(five).isNotEqualTo(numFactory.numOf(45.toShort()))
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun testSqrtOfBigInteger(numFactory: NumFactory) {
        val sqrtOfTwo = "1.4142135623730950488016887242096980785696718753769480731" +
                "766797379907324784621070388503875343276415727350138462309122970249248360" +
                "558507372126441214970999358314132226659275055927557999505011527820605715"

        assertNumEquals(sqrtOfTwo, numFactory.numOf(2).sqrt(MathContext(200)))
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun testSqrtOfBigDouble(numFactory: NumFactory) {
        val sqrtOfOnePointTwo =
            "1.095445115010332226913939565601604267905489389995966508453788899464986554245445467601716872327741252"

        assertNumEquals(sqrtOfOnePointTwo, numFactory.numOf(1.2).sqrt(MathContext(100)))
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun testSqrtOfNegativeDouble(numFactory: NumFactory) {
        assertThat(numFactory.numOf(-1.2).sqrt(MathContext(12)).isNaN).isTrue()
        assertThat(numFactory.numOf(-1.2).sqrt().isNaN).isTrue()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun testSqrtOfZero(numFactory: NumFactory) {
        assertNumEquals(0, numFactory.numOf(0).sqrt(MathContext(12)))
        assertNumEquals(0, numFactory.numOf(0).sqrt())
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    @Throws(IOException::class)
    fun testSqrtLudicrousPrecision(numFactory: NumFactory) {
        val numBD = BigDecimal.valueOf(Double.MAX_VALUE)
            .multiply(BigDecimal.valueOf(Double.MAX_VALUE).add(BigDecimal.ONE))

        when (numFactory) {
            is DoubleNumFactory -> {
                val sqrt = DoubleNum.valueOf(numBD).sqrt(MathContext(100000))
                assertThat(sqrt.toString()).isEqualTo("Infinity")
            }
            is DecimalNumFactory -> {
                val sqrt = DecimalNum.valueOf(numBD, MathContext(100000)).sqrt(MathContext(100000))
                val props = Properties()

                javaClass.getResourceAsStream("numTest.properties").use { inputStream ->
                    props.load(inputStream)
                    assertNumEquals(props.getProperty("sqrtCorrect100000"), sqrt)
                    assertNumNotEquals(props.getProperty("sqrtCorrect99999"), sqrt)
                    assertNumEquals(Double.MAX_VALUE, sqrt)
                    assertNumNotEquals(numFactory.numOf(Double.MAX_VALUE), sqrt)

                    val sqrtBD = BigDecimal(sqrt.toString())
                    assertNumEquals(
                        numFactory.numOf(numBD),
                        numFactory.numOf(sqrtBD.multiply(sqrtBD, MathContext(99999, HALF_UP)))
                    )
                    assertNumNotEquals(numFactory.numOf(numBD), sqrt.times(sqrt))
                }
            }
        }
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun testSqrtOddExponent(numFactory: NumFactory) {
        val numBD = BigDecimal.valueOf(3E11)
        val sqrt = numFactory.numOf(numBD).sqrt()
        assertNumEquals("547722.55750516611345696978280080", sqrt)
    }
}