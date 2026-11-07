/*
 * SPDX-License-Identifier: MIT
 */
package ta4jexamples.loaders

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
            timeFrame = TimeFrame.Companion.DAY,
            beginTime = start,
            endTime = start.plus(TimeFrameMapping.getDuration(TimeFrame.Companion.DAY)),
            openPrice = open,
            highPrice = high,
            lowPrice = low,
            closePrice = close,
            volume = volume
        )
    }
}
