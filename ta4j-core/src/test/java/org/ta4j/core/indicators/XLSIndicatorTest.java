/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2023 Ta4j Organization & respective
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
package org.ta4j.core.indicators;

import java.util.List;

import org.ta4j.core.ExternalIndicatorTest;
import org.ta4j.core.XlsTestsUtils;
import org.ta4j.core.events.MarketEvent;
import org.ta4j.core.mocks.MockIndicator;
import org.ta4j.core.num.NumFactory;

public class XLSIndicatorTest implements ExternalIndicatorTest {

  private final Class<?> clazz;
  private final String fileName;
  private final int column;
  private List<MarketEvent> cachedSeries = null;
  private final NumFactory numFactory;


  /**
   * Constructor.
   *
   * @param clazz class containing the file resources
   * @param fileName file name of the file containing the workbook
   * @param column column number containing the calculated indicator values
   */
  public XLSIndicatorTest(final Class<?> clazz, final String fileName, final int column, final NumFactory numFactory) {
    this.clazz = clazz;
    this.fileName = fileName;
    this.column = column;
    this.numFactory = numFactory;
  }


  /**
   * Gets the BarSeries from the XLS file.
   *
   * @return BarSeries from the file
   *
   * @throws Exception if getMarketEvents throws IOException or DataFormatException
   */
  @Override
  public List<MarketEvent> getMarketEvents() throws Exception {
    if (this.cachedSeries == null) {
      this.cachedSeries = XlsTestsUtils.getMarketEvents(this.clazz, this.fileName);
    }

    return this.cachedSeries;
  }


  /**
   * Gets the Indicator from the XLS file given the parameters.
   *
   * @param params indicator parameters
   *
   * @return Indicator from the file given the parameters
   *
   * @throws Exception if getIndicator throws IOException or DataFormatException
   */
  @Override
  public MockIndicator getIndicator(final Object... params) throws Exception {
    return XlsTestsUtils.getIndicator(
        this.clazz,
        this.fileName,
        this.column,
        this.numFactory,
        params
    );
  }

}
