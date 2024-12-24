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
package org.ta4j.core.criteria;

import java.util.List;

import org.ta4j.core.ExternalCriterionTest;
import org.ta4j.core.XlsTestsUtils;
import org.ta4j.core.backtest.TradingRecord;
import org.ta4j.core.events.MarketEvent;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NumFactory;

public class XLSCriterionTest implements ExternalCriterionTest {

  private final Class<?> clazz;
  private final String fileName;
  private final int criterionColumn;
  private final int statesColumn;
  private List<MarketEvent> cachedSeries = null;
  private final NumFactory numFactory;


  /**
   * Constructor.
   *
   * @param clazz class containing the file resources
   * @param fileName file name of the file containing the workbook
   * @param criterionColumn column number containing the calculated criterion
   *     values
   * @param statesColumn column number containing the trading record states
   */
  public XLSCriterionTest(
      final Class<?> clazz,
      final String fileName,
      final int criterionColumn,
      final int statesColumn,
      final NumFactory numFactory
  ) {
    this.clazz = clazz;
    this.fileName = fileName;
    this.criterionColumn = criterionColumn;
    this.statesColumn = statesColumn;
    this.numFactory = numFactory;
  }


  /**
   * Gets the BarSeries from the XLS file. The BarSeries is cached so that
   * subsequent calls do not execute getMarketEvents.
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
   * Gets the final criterion value from the XLS file given the parameters.
   *
   * @param params criterion parameters
   *
   * @return Num final criterion value
   *
   * @throws Exception if getFinalCriterionValue throws IOException or
   *     DataFormatException
   */
  @Override
  public Num getFinalCriterionValue(final Object... params) throws Exception {
    return XlsTestsUtils.getFinalCriterionValue(
        this.clazz,
        this.fileName,
        this.criterionColumn,
        this.numFactory,
        params
    );
  }


  /**
   * Gets the trading record from the XLS file.
   *
   * @return TradingRecord from the file
   */
  @Override
  public TradingRecord getTradingRecord() throws Exception {
    return XlsTestsUtils.getTradingRecord(this.clazz, this.fileName, this.statesColumn, this.numFactory);
  }

}
