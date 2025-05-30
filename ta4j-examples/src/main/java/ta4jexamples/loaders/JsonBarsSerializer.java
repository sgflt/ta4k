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
package ta4jexamples.loaders;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.ta4j.core.api.series.BarSeries;
import org.ta4j.core.backtest.BacktestBarSeries;
import ta4jexamples.loaders.jsonhelper.GsonBarSeries;

public class JsonBarsSerializer {

  private static final Logger LOG = Logger.getLogger(JsonBarsSerializer.class.getName());


  public static void persistSeries(final BacktestBarSeries series, final String filename) {
    final var exportableSeries = GsonBarSeries.from(series);
    final var gson = new GsonBuilder().setPrettyPrinting().create();
    FileWriter writer = null;
    try {
      writer = new FileWriter(filename);
      gson.toJson(exportableSeries, writer);
      LOG.info("Bar series '" + series.getName() + "' successfully saved to '" + filename + "'");
    } catch (final IOException e) {
      e.printStackTrace();
      LOG.log(Level.SEVERE, "Unable to store bars in JSON", e);
    } finally {
      if (writer != null) {
        try {
          writer.flush();
          writer.close();
        } catch (final IOException e) {
          e.printStackTrace();
        }
      }
    }
  }


  public static BarSeries loadSeries(final String filename) {
    final Gson gson = new Gson();
    FileReader reader = null;
    BacktestBarSeries result = null;
    try {
      reader = new FileReader(filename);
      final GsonBarSeries loadedSeries = gson.fromJson(reader, GsonBarSeries.class);

      result = loadedSeries.toBarSeries();
      LOG.info("Bar series '" + result.getName() + "' successfully loaded. #Entries: " + result.getBarCount());
    } catch (final FileNotFoundException e) {
      e.printStackTrace();
      LOG.log(Level.SEVERE, "Unable to load bars from JSON", e);
    } finally {
      try {
        if (reader != null) {
          reader.close();
        }
      } catch (final IOException e) {
        e.printStackTrace();
      }
    }
    return result;
  }
}
