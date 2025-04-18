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
package org.ta4j.core

import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.CellValue
import org.apache.poi.ss.usermodel.DateUtil
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.ta4j.core.backtest.TradingRecord
import org.ta4j.core.events.CandleReceived
import org.ta4j.core.events.MarketEvent
import org.ta4j.core.indicators.TimeFrame.Companion.DAY
import org.ta4j.core.mocks.MockIndicator
import org.ta4j.core.mocks.MockTradingRecord
import org.ta4j.core.num.NaN
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory
import java.io.IOException
import java.math.BigDecimal
import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.function.DoubleConsumer
import java.util.zip.DataFormatException

object XlsTestsUtils {
    /**
     * Returns the first Sheet (mutable) from a workbook with the file name in the
     * test class's resources.
     *
     * @param clazz class containing the file resources
     * @param fileName file name of the file containing the workbook
     *
     * @return Sheet number zero from the workbook (mutable)
     *
     * @throws IOException if inputStream returned by getResourceAsStream is null or
     * if HSSFWorkBook constructor throws IOException or if
     * close throws IOException
     */
    @Throws(IOException::class)
    private fun getSheet(clazz: Class<*>, fileName: String): Sheet {
        clazz.getResourceAsStream(fileName).use { inputStream ->
            if (inputStream == null) {
                throw IOException("Null InputStream for file " + fileName)
            }
            val workbook = HSSFWorkbook(inputStream)
            val sheet: Sheet = workbook.getSheetAt(0)
            workbook.close()
            return sheet
        }
    }


    /**
     * Writes the parameters into the second column of the parameters section of a
     * mutable sheet. The parameters section starts after the parameters section
     * header. There must be at least params.size() rows between the parameters
     * section header and the data section header or part of the data section will
     * be overwritten.
     *
     * @param sheet mutable Sheet
     * @param params parameters to write
     *
     * @throws DataFormatException if the parameters section header is not found
     */
    @Throws(DataFormatException::class)
    private fun setParams(sheet: Sheet, vararg params: Num?) {
        val evaluator = sheet.workbook.creationHelper.createFormulaEvaluator()
        val iterator = sheet.rowIterator()
        while (iterator.hasNext()) {
            val row = iterator.next()
            // skip rows with an empty first cell
            if (row.getCell(0) == null) {
                continue
            }
            // parameters section header is first row with "Param" in first cell
            if (evaluator.evaluate(row.getCell(0)).formatAsString().contains("Param")) {
                // stream parameters into the second column of subsequent rows
                // overwrites data section if there is not a large enough gap
                Arrays.stream<Num?>(params)
                    .mapToDouble { obj: Num? -> obj!!.doubleValue() }
                    .forEach(DoubleConsumer { d: Double -> iterator.next().getCell(1).setCellValue(d) })
                return
            }
        }
        // the parameters section header was not found
        throw DataFormatException("\"Param\" header row not found")
    }


    /**
     * Gets the BarSeries from a file.
     *
     * @param clazz class containing the file resources
     * @param fileName file name of the file resource
     *
     * @return BarSeries of the data
     *
     * @throws IOException if getSheet throws IOException
     * @throws DataFormatException if getMarketEvents throws DataFormatException
     */
    @Throws(IOException::class, DataFormatException::class)
    @JvmStatic
    fun getMarketEvents(
        clazz: Class<*>,
        fileName: String,
    ): MutableList<MarketEvent?> {
        val sheet = getSheet(clazz, fileName)
        return getMarketEvents(sheet)
    }


    /**
     * Gets a BarSeries from the data section of a mutable Sheet. Data follows a
     * data section header and appears in the first six columns to the end of the
     * file. Empty cells in the data are forbidden.
     *
     * @param sheet mutable Sheet
     *
     * @return BarSeries of the data
     *
     * @throws DataFormatException if getData throws DataFormatException or if the
     * data contains empty cells
     */
    @Throws(DataFormatException::class)
    private fun getMarketEvents(sheet: Sheet): MutableList<MarketEvent?> {
        val candleEvents = ArrayList<MarketEvent?>()
        val evaluator = sheet.workbook.creationHelper.createFormulaEvaluator()
        val rows = getData(sheet)
        var minInterval = Int.Companion.MAX_VALUE
        var previousNumber = Int.Companion.MAX_VALUE
        // find the minimum interval in days
        for (row in rows) {
            val currentNumber = evaluator.evaluate(row.getCell(0)).numberValue.toInt()
            if (previousNumber != Int.Companion.MAX_VALUE) {
                val interval = currentNumber - previousNumber
                if (interval < minInterval) {
                    minInterval = interval
                }
            }
            previousNumber = currentNumber
        }
        Duration.ofDays(minInterval.toLong())
        // parse the bars from the data section
        for (row in rows) {
            val cellValues = arrayOfNulls<CellValue>(6)
            for (i in 0..5) {
                // empty cells in the data section are forbidden
                if (row.getCell(i) == null) {
                    throw DataFormatException("empty cell in xls bar candleEvents data")
                }
                cellValues[i] = evaluator.evaluate(row.getCell(i))
            }
            // add a bar to the candleEvents
            val endDate = DateUtil.getJavaDate(cellValues[0]!!.numberValue)
            val endDateTime = Instant.ofEpochMilli(endDate.time)
            candleEvents.add(
                CandleReceived(
                    DAY,
                    endDateTime.minus(Duration.ofDays(1)),
                    endDateTime,
                    cellValues[1]!!.numberValue,  // open
                    cellValues[2]!!.numberValue,  // high
                    cellValues[3]!!.numberValue,  // low
                    cellValues[4]!!.numberValue,  // close
                    cellValues[5]!!.numberValue // volume
                )
            )
        }
        return candleEvents
    }


    /**
     * Converts Object parameters into Num parameters and calls getValues on a
     * column of a mutable sheet.
     *
     * @param sheet mutable Sheet
     * @param column column number of the values to get
     * @param params Object parameters to convert to Num
     *
     * @return List<Num> of values from the column
     *
     * @throws DataFormatException if getValues returns DataFormatException
    </Num> */
    @Throws(DataFormatException::class)
    private fun getValues(
        sheet: Sheet,
        column: Int,
        numFactory: NumFactory,
        vararg params: Any,
    ): MutableList<Num> {
        val numParams = params.map { numFactory.numOf(BigDecimal(it.toString())) }
        return XlsTestsUtils.getValues(sheet, column, numFactory, *numParams.toTypedArray())
    }


    /**
     * Writes the parameters to a mutable Sheet then gets the values from the
     * column.
     *
     * @param sheet mutable Sheet
     * @param column column number of the values to get
     * @param params Num parameters to write to the Sheet
     *
     * @return List<Num> of values from the column after the parameters have been
     * written
     *
     * @throws DataFormatException if setParams or getValues throws
     * DataFormatException
    </Num> */
    @Throws(DataFormatException::class)
    private fun getValues(
        sheet: Sheet,
        column: Int,
        numFactory: NumFactory,
        vararg params: Num,
    ): MutableList<Num> {
        setParams(sheet, *params)
        return XlsTestsUtils.getValues(sheet, column, numFactory)
    }


    /**
     * Gets the values in a column of the data section of a sheet. Rows with an
     * empty first cell are ignored.
     *
     * @param sheet mutable Sheet
     * @param column column number of the values to get
     *
     * @return List<Num> of values from the column
     *
     * @throws DataFormatException if getData throws DataFormatException
    </Num> */
    @Throws(DataFormatException::class)
    private fun getValues(sheet: Sheet, column: Int, numFactory: NumFactory): MutableList<Num> {
        val values = ArrayList<Num>()
        val evaluator = sheet.workbook.creationHelper.createFormulaEvaluator()
        // get all of the data from the data section of the sheet
        val rows = getData(sheet)
        for (row in rows) {
            // skip rows where the first cell is empty
            if (row.getCell(column) == null) {
                continue
            }
            val s = evaluator.evaluate(row.getCell(column)).formatAsString()
            if (s == "#DIV/0!") {
                values.add(NaN)
            } else {
                values.add(numFactory.numOf(BigDecimal(s)))
            }
        }
        return values
    }


    /**
     * Gets all data rows in the data section, following the data section header to
     * the end of the sheet. Skips rows that start with "//" as data comments.
     *
     * @param sheet mutable Sheet
     *
     * @return List<Row> of the data rows
     *
     * @throws DataFormatException if the data section header is not found.
    </Row> */
    @Throws(DataFormatException::class)
    private fun getData(sheet: Sheet): MutableList<Row> {
        val evaluator = sheet.workbook.creationHelper.createFormulaEvaluator()
        val iterator = sheet.rowIterator()
        var noHeader = true
        val rows: MutableList<Row> = ArrayList<Row>()
        // iterate through all rows of the sheet
        while (iterator.hasNext()) {
            val row = iterator.next()
            // skip rows with an empty first cell
            if (row.getCell(0) == null) {
                continue
            }
            // after the data section header is found, onCandle all rows that don't
            // have "//" in the first cell
            if (!noHeader) {
                if (evaluator.evaluate(row.getCell(0)).formatAsString().compareTo("\"//\"") != 0) {
                    rows.add(row)
                }
            }
            // if the data section header is not found and this row has "Date"
            // in its first cell, then mark the header as found
            if (noHeader && evaluator.evaluate(row.getCell(0)).formatAsString().contains("Date")) {
                noHeader = false
            }
        }
        // if the header was not found throw an exception
        if (noHeader) {
            throw DataFormatException("\"Date\" header row not found")
        }
        return rows
    }


    /**
     * Gets an Indicator from a column of an XLS file parameters.
     *
     * @param clazz class containing the file resource
     * @param fileName file name of the file resource
     * @param column column number of the indicator values
     * @param params indicator parameters
     *
     * @return Indicator<Num> as calculated by the XLS file given the parameters
     *
     * @throws IOException if getSheet throws IOException
     * @throws DataFormatException if getMarketEvents or getValues throws
     * DataFormatException
    </Num> */
    @Throws(IOException::class, DataFormatException::class)
    @JvmStatic
    fun getIndicator(
        clazz: Class<*>,
        fileName: String,
        column: Int,
        numFactory: NumFactory,
        vararg params: Any,
    ): MockIndicator {
        val sheet = getSheet(clazz, fileName)
        return MockIndicator(getValues(sheet, column, numFactory, *params))
    }


    /**
     * Gets the final criterion value from a column of an XLS file given parameters.
     *
     * @param clazz test class containing the file resources
     * @param fileName file name of the file resource
     * @param column column number of the calculated criterion values
     * @param params criterion parameters
     *
     * @return Num final criterion value as calculated by the XLS file given the
     * parameters
     *
     * @throws IOException if getSheet throws IOException
     * @throws DataFormatException if getValues throws DataFormatException
     */
    @Throws(IOException::class, DataFormatException::class)
    @JvmStatic
    fun getFinalCriterionValue(
        clazz: Class<*>, fileName: String, column: Int, numFactory: NumFactory,
        vararg params: Any,
    ): Num? {
        val sheet = getSheet(clazz, fileName)
        val values = getValues(sheet, column, numFactory, *params)
        return values[values.size - 1]
    }


    /**
     * Gets the trading record from an XLS file.
     *
     * @param clazz the test class containing the file resources
     * @param fileName file name of the file resource
     * @param column column number of the trading record
     *
     * @return TradingRecord from the file
     *
     * @throws IOException if getSheet throws IOException
     * @throws DataFormatException if getValues throws DataFormatException
     */
    @Throws(IOException::class, DataFormatException::class)
    @JvmStatic
    fun getTradingRecord(
        clazz: Class<*>,
        fileName: String,
        column: Int,
        numFactory: NumFactory,
    ): TradingRecord {
        val sheet = getSheet(clazz, fileName)
        return MockTradingRecord(XlsTestsUtils.getValues(sheet, column, numFactory))
    }
}
