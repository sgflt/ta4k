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
package org.ta4j.csv

import java.io.Reader
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import org.ta4j.core.events.CandleReceived
import org.ta4j.core.indicators.TimeFrame
import org.ta4j.core.utils.TimeFrameMapping

/**
 * This class build a Ta4j bar series from a CSV file containing bars.
 */
object MarketEventsLoader {
    private val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME


    fun load(filename: Path): List<CandleReceived> {
        Files.newBufferedReader(filename).use { stream ->
            return readCsv(stream)
        }
    }


    private fun readCsv(reader: Reader): List<CandleReceived> {
        return CSVReaderBuilder(reader)
            .withCSVParser(CSVParserBuilder().withSeparator(',').build())
            .withSkipLines(1)
            .build().use { csvReader ->
                csvReader.map { line ->
                    val date = ZonedDateTime.parse(line[0], DATE_FORMAT).toInstant()
                    val open = line[1].toDouble()
                    val high = line[2].toDouble()
                    val low = line[3].toDouble()
                    val close = line[4].toDouble()
                    val volume = line[5].toDouble()

                    createCandle(date, open, high, low, close, volume)
                }.toList()
            }
    }


    private fun createCandle(
        start: Instant,
        open: Double,
        high: Double,
        low: Double,
        close: Double,
        volume: Double,
    ): CandleReceived {
        return CandleReceived(
            timeFrame = TimeFrame.DAY,
            beginTime = start,
            endTime = start.plus(TimeFrameMapping.getDuration(TimeFrame.DAY)),
            openPrice = open,
            highPrice = high,
            lowPrice = low,
            closePrice = close,
            volume = volume
        )
    }
}
