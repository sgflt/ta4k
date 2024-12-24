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
package org.ta4j.core.num;

import static java.math.RoundingMode.HALF_UP;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.ta4j.core.TestUtils.assertNumEquals;
import static org.ta4j.core.TestUtils.assertNumNotEquals;
import static org.ta4j.core.num.NaN.NaN;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Properties;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class NumTest {

  private static final int HIGH_PRECISION = 128;
  private static final MathContext HIGH_PRECISION_CONTEXT = new MathContext(HIGH_PRECISION, HALF_UP);


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void testZero(final NumFactory numFactory) {
    final var anyNaNNum = NaN;
    final var anyDecimalNum = DecimalNum.valueOf(3);
    final var anyDoubleNum = DoubleNum.valueOf(3);

    assertNumEquals(NaN, anyNaNNum.getNumFactory().zero());
    assertNumEquals(0, numFactory.numOf(3).getNumFactory().zero());
    assertNumEquals(0, anyDecimalNum.getNumFactory().zero());
    assertNumEquals(0, anyDoubleNum.getNumFactory().zero());
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void testOne(final NumFactory numFactory) {
    final var anyNaNNum = NaN;
    final var anyDecimalNum = DecimalNum.valueOf(3);
    final var anyDoubleNum = DoubleNum.valueOf(3);

    assertNumEquals(NaN, anyNaNNum.getNumFactory().one());
    assertNumEquals(1, numFactory.numOf(3).getNumFactory().one());
    assertNumEquals(1, anyDecimalNum.getNumFactory().one());
    assertNumEquals(1, anyDoubleNum.getNumFactory().one());
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void testHundred(final NumFactory numFactory) {
    final var anyNaNNum = NaN;
    final var anyDecimalNum = DecimalNum.valueOf(3);
    final var anyDoubleNum = DoubleNum.valueOf(3);

    assertNumEquals(NaN, anyNaNNum.getNumFactory().hundred());
    assertNumEquals(100, numFactory.numOf(3).getNumFactory().hundred());
    assertNumEquals(100, anyDecimalNum.getNumFactory().hundred());
    assertNumEquals(100, anyDoubleNum.getNumFactory().hundred());
  }


  @Test
  void testStringNumFail() {
    assertThatThrownBy(() -> assertNumEquals("1.234", DecimalNum.valueOf(4.321)))
        .isInstanceOf(AssertionError.class);
  }


  @Test
  void testStringNumPass() {
    assertNumEquals("1.234", DecimalNum.valueOf(1.234));
  }


  @Test
  void testDecimalNumPrecision() {
    final var highPrecisionString =
        "1.928749238479283749238472398472936872364823749823749238749238749283749238472983749238749832749274";
    final var num = DecimalNumFactory.getInstance(HIGH_PRECISION).numOf(highPrecisionString);
    final var highPrecisionNum = DecimalNum.valueOf(highPrecisionString, HIGH_PRECISION_CONTEXT);

    assertThat(highPrecisionNum.matches(num, 17)).isTrue();

    final var fromNum = new BigDecimal(num.toString());
    if (num instanceof DoubleNum) {
      assertThat(fromNum.precision()).isEqualTo(17);
      assertThat(highPrecisionNum.matches(num, 17)).isTrue();
      assertThat(highPrecisionNum.matches(num, 18)).isFalse();
    }

    if (num instanceof DecimalNum) {
      assertThat(fromNum.precision()).isEqualTo(97);
      assertThat(highPrecisionNum.matches(num, 10000)).isTrue();
    }
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void testValueOf(final NumFactory numFactory) {
    assertNumEquals(0.33333333333333333332, numFactory.numOf(0.33333333333333333332));
    assertNumEquals(1, numFactory.numOf(1d));
    assertNumEquals(2.54, numFactory.numOf(new BigDecimal("2.54")));

    assertNumEquals(0.33, numFactory.numOf(0.33));
    assertNumEquals(1, numFactory.numOf(1));
    assertNumEquals(2.54, numFactory.numOf(new BigDecimal("2.54")));
  }


  @Test
  void testMultiplicationSymmetrically() {
    final var decimalFromString = DecimalNum.valueOf("0.33");
    final var decimalFromDouble = DecimalNum.valueOf(45.33);
    assertThat(decimalFromString.multipliedBy(decimalFromDouble))
        .isEqualTo(decimalFromDouble.multipliedBy(decimalFromString));

    final var doubleNumFromString = DoubleNum.valueOf("0.33");
    final var doubleNumFromDouble = DoubleNum.valueOf(10.33);
    assertNumEquals(
        doubleNumFromString.multipliedBy(doubleNumFromDouble),
        doubleNumFromDouble.multipliedBy(doubleNumFromString)
    );
  }


  @Test
  void testFailDifferentNumsAdd() {
    final var a = DecimalNum.valueOf(12);
    final var b = DoubleNum.valueOf(12);
    assertThatThrownBy(() -> a.plus(b))
        .isInstanceOf(ClassCastException.class);
  }


  @Test
  void testFailDifferentNumsCompare() {
    final var a = DecimalNum.valueOf(12);
    final var b = DoubleNum.valueOf(13);
    assertThatThrownBy(() -> a.isEqual(b))
        .isInstanceOf(ClassCastException.class);
  }


  @Test
  void testFailNaNtoInt() {
    assertThatThrownBy(() -> NaN.intValue())
        .isInstanceOf(UnsupportedOperationException.class);
  }


  @Test
  void testFailNaNtoLong() {
    assertThatThrownBy(() -> NaN.longValue())
        .isInstanceOf(UnsupportedOperationException.class);
  }


  @Test
  void testNaN() {
    final var a = NaN;
    final var eleven = DecimalNum.valueOf(11);

    var mustBeNaN = a.plus(eleven);
    assertNumEquals(mustBeNaN, NaN);

    mustBeNaN = a.minus(eleven);
    assertNumEquals(mustBeNaN, NaN);

    mustBeNaN = a.dividedBy(a);
    assertNumEquals(mustBeNaN, NaN);

    mustBeNaN = a.multipliedBy(NaN);
    assertNumEquals(mustBeNaN, NaN);

    mustBeNaN = a.max(eleven);
    assertNumEquals(mustBeNaN, NaN);

    mustBeNaN = eleven.min(a);
    assertNumEquals(mustBeNaN, NaN);

    mustBeNaN = a.pow(12);
    assertNumEquals(mustBeNaN, NaN);

    mustBeNaN = a.pow(a);
    assertNumEquals(mustBeNaN, NaN);

    assertThat(a.doubleValue()).isNaN();
    assertThat(a.floatValue()).isNaN();
    assertThat(a).isEqualTo(NaN);
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void testArithmetic(final NumFactory numFactory) {
    final var ten = numFactory.numOf(10);
    final var million = numFactory.numOf(1000000);
    assertNumEquals(10, ten);
    assertNumEquals("1000000.0", million);

    final var zero = ten.minus(ten);
    assertNumEquals(0, zero);

    final var hundred = ten.multipliedBy(ten);
    assertNumEquals(100, hundred);

    final var hundredMillion = hundred.multipliedBy(million);
    assertNumEquals(100000000, hundredMillion);

    assertNumEquals(hundredMillion.dividedBy(hundred), million);
    assertNumEquals(0, hundredMillion.remainder(hundred));

    final var five = numFactory.numOf(5);
    final var zeroDotTwo = numFactory.numOf(0.2);
    final var fiveHundred54 = numFactory.numOf(554);
    assertNumEquals(0, hundredMillion.remainder(five));

    assertNumEquals(0.00032, zeroDotTwo.pow(5));
    assertNumEquals(0.7247796636776955, zeroDotTwo.pow(zeroDotTwo));
    assertNumEquals(1.37972966146, zeroDotTwo.pow(numFactory.numOf(-0.2)));
    assertNumEquals(554, fiveHundred54.max(five));
    assertNumEquals(5, fiveHundred54.min(five));
    assertThat(fiveHundred54.isGreaterThan(five)).isTrue();
    assertThat(five.isGreaterThan(numFactory.numOf(5))).isFalse();
    assertThat(five.isGreaterThanOrEqual(fiveHundred54)).isFalse();
    assertThat(five.isGreaterThanOrEqual(numFactory.numOf(6))).isFalse();
    assertThat(five.isGreaterThanOrEqual(numFactory.numOf(5))).isTrue();

    assertThat(five).isEqualTo(numFactory.numOf(5));
    assertThat(five).isEqualTo(numFactory.numOf(5.0));
    assertThat(five).isEqualTo(numFactory.numOf((float) 5));
    assertThat(five).isEqualTo(numFactory.numOf((short) 5));

    assertThat(five).isNotEqualTo(numFactory.numOf(4.9));
    assertThat(five).isNotEqualTo(numFactory.numOf(6));
    assertThat(five).isNotEqualTo(numFactory.numOf((float) 15));
    assertThat(five).isNotEqualTo(numFactory.numOf((short) 45));
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void testSqrtOfBigInteger(final NumFactory numFactory) {
    final var sqrtOfTwo = "1.4142135623730950488016887242096980785696718753769480731"
                          + "766797379907324784621070388503875343276415727350138462309122970249248360"
                          + "558507372126441214970999358314132226659275055927557999505011527820605715";

    assertNumEquals(sqrtOfTwo, numFactory.numOf(2).sqrt(new MathContext(200)));
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void testSqrtOfBigDouble(final NumFactory numFactory) {
    final var sqrtOfOnePointTwo =
        "1.095445115010332226913939565601604267905489389995966508453788899464986554245445467601716872327741252";

    assertNumEquals(sqrtOfOnePointTwo, numFactory.numOf(1.2).sqrt(new MathContext(100)));
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void testSqrtOfNegativeDouble(final NumFactory numFactory) {
    assertThat(numFactory.numOf(-1.2).sqrt(new MathContext(12)).isNaN()).isTrue();
    assertThat(numFactory.numOf(-1.2).sqrt().isNaN()).isTrue();
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void testSqrtOfZero(final NumFactory numFactory) {
    assertNumEquals(0, numFactory.numOf(0).sqrt(new MathContext(12)));
    assertNumEquals(0, numFactory.numOf(0).sqrt());
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void testSqrtLudicrousPrecision(final NumFactory numFactory) throws IOException {
    final var numBD = BigDecimal.valueOf(Double.MAX_VALUE)
        .multiply(BigDecimal.valueOf(Double.MAX_VALUE).add(BigDecimal.ONE));

    if (numFactory instanceof DoubleNumFactory) {
      final var sqrt = DoubleNum.valueOf(numBD).sqrt(new MathContext(100000));
      assertThat(sqrt.toString()).isEqualTo("Infinity");
    } else if (numFactory instanceof DecimalNumFactory) {
      final var sqrt = DecimalNum.valueOf(numBD, new MathContext(100000)).sqrt(new MathContext(100000));
      final var props = new Properties();

      try (final var is = getClass().getResourceAsStream("numTest.properties")) {
        props.load(is);
        assertNumEquals(props.getProperty("sqrtCorrect100000"), sqrt);
        assertNumNotEquals(props.getProperty("sqrtCorrect99999"), sqrt);
        assertNumEquals(Double.MAX_VALUE, sqrt);
        assertNumNotEquals(numFactory.numOf(Double.MAX_VALUE), sqrt);

        final var sqrtBD = new BigDecimal(sqrt.toString());
        assertNumEquals(
            numFactory.numOf(numBD),
            numFactory.numOf(sqrtBD.multiply(sqrtBD, new MathContext(99999, HALF_UP)))
        );
        assertNumNotEquals(numFactory.numOf(numBD), sqrt.multipliedBy(sqrt));
      }
    }
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void testSqrtOddExponent(final NumFactory numFactory) {
    final var numBD = BigDecimal.valueOf(Double.valueOf("3E11"));
    final var sqrt = numFactory.numOf(numBD).sqrt();
    assertNumEquals("547722.55750516611345696978280080", sqrt);
  }
}
