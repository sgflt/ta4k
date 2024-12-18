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
package org.ta4j.csv;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.events.CandleReceived;

/**
 * This class build a Ta4j bar series from a CSV file containing bars.
 */
public final class CsvBarsLoader {
  private static final Logger LOG = LoggerFactory.getLogger(CsvBarsLoader.class);

  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_DATE_TIME;


  private CsvBarsLoader() {
    // utility class
  }


  public static List<CandleReceived> load(final Path filename) {
    final var candles = new ArrayList<CandleReceived>();

    try (final var stream = Files.newBufferedReader(filename)) {
      readCsv(stream, candles);
    } catch (final IOException ioe) {
      LOG.error("Unable to load bars from CSV", ioe);
    } catch (final NumberFormatException nfe) {
      LOG.error("Error while parsing value", nfe);
    }
    return candles;
  }


  private static void readCsv(final Reader reader, final List<CandleReceived> candleEvents) throws IOException {
    try (
        final var csvReader = new CSVReaderBuilder(reader)
            .withCSVParser(new CSVParserBuilder().withSeparator(',').build())
            .withSkipLines(1)
            .build()
    ) {
      String[] line;
      while ((line = csvReader.readNext()) != null) {
        final var date = ZonedDateTime.parse(line[0], DATE_FORMAT).toInstant();
        final var open = Double.parseDouble(line[1]);
        final var high = Double.parseDouble(line[2]);
        final var low = Double.parseDouble(line[3]);
        final var close = Double.parseDouble(line[4]);
        final var volume = Double.parseDouble(line[5]);

        candleEvents.add(createCandle(date, open, high, low, close, volume));
      }
    } catch (final CsvValidationException e) {
      LOG.error("Unable to load bars from CSV. File is not valid csv.", e);
    }
  }


  private static CandleReceived createCandle(
      final Instant start,
      final double open,
      final double high,
      final double low,
      final double close,
      final double volume
  ) {
    return new CandleReceived(
        start,
        start.plus(Duration.ofDays(1)),
        open,
        high,
        low,
        close,
        volume,
        0
    );
  }
}
