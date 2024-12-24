/// *
// * The MIT License (MIT)
// *
// * Copyright (c) 2024 Lukáš Kvídera
// *
// * Permission is hereby granted, free of charge, to any person obtaining a copy of
// * this software and associated documentation files (the "Software"), to deal in
// * the Software without restriction, including without limitation the rights to
// * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
// * the Software, and to permit persons to whom the Software is furnished to do so,
// * subject to the following conditions:
// *
// * The above copyright notice and this permission notice shall be included in all
// * copies or substantial portions of the Software.
// *
// * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
// * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
// * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
// * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
// * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
// */
//
//package org.ta4j.core.indicators;
//
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//import org.junit.jupiter.api.Test;
//import org.ta4j.core.indicators.numeric.NumericIndicator;
//import org.ta4j.core.mocks.MockBarSeriesBuilder;
//import org.ta4j.core.num.DoubleNumFactory;
//
//class NamedIndicatorTest {
//
//  @Test
//  void testGetName() {
//    final var numNamedIndicator = NumericIndicator.closePrice(new MockBarSeriesBuilder().build()).named("Name");
//    assertThat(numNamedIndicator.name()).isEqualTo("Name");
//  }
//
//
//  @Test
//  void testGetValue() {
//    final var numFactory = DoubleNumFactory.getInstance();
//    final var series = new MockBarSeriesBuilder().withNumFactory(numFactory).withDefaultData().build();
//    final var namedIndicator = NumericIndicator.closePrice(series).named("Name");
//
//    series.advance();
//    assertThat(namedIndicator.getValue()).isEqualTo(numFactory.numOf(1.0));
//  }
//}
