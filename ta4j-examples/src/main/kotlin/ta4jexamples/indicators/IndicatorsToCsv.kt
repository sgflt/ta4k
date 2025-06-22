/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2025 Ta4j Organization & respective
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
package ta4jexamples.indicators

import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.time.Instant
import org.ta4j.core.api.Indicators
import org.ta4j.core.api.callback.MarketEventHandler
import org.ta4j.core.events.CandleReceived
import org.ta4j.core.indicators.IndicatorContext
import org.ta4j.core.indicators.IndicatorContext.IndicatorIdentification
import org.ta4j.core.indicators.IndicatorContextUpdateListener
import org.ta4j.core.indicators.IndicatorContexts
import org.ta4j.core.indicators.TimeFrame
import org.ta4j.core.trading.preparation.ObservableIndicatorCalculationBuilder
import ta4jexamples.loaders.MockMarketEventsLoader

/**
 * This class builds a CSV file containing values from indicators using modern IndicatorContext listeners.
 *
 * Migrated from Java to Kotlin with the following improvements:
 * - Uses event-driven architecture with IndicatorContextUpdateListener
 * - Real-time CSV writing as bars are processed using ObservableIndicatorCalculationBuilder
 * - Modern Kotlin I/O with resource management
 * - Type-safe indicator identification
 * - Configurable indicator set and output path
 */
class IndicatorsToCsv(
    private val outputFile: File = File("target", "indicators.csv"),
    private val timeFrame: TimeFrame = TimeFrame.DAY,
) : IndicatorContextUpdateListener, AutoCloseable {

    private lateinit var csvWriter: BufferedWriter
    private var isHeaderWritten = false
    private var lastUpdateTime: Instant = Instant.MIN
    private val indicatorContexts: IndicatorContexts
    private val marketEventHandler: MarketEventHandler

    // Indicator identifications for type-safe access
    private val indicators = mapOf(
        "close" to CLOSE_PRICE,
        "typical" to TYPICAL_PRICE,
        "variation" to PRICE_VARIATION,
        "sma8" to SMA_8,
        "sma20" to SMA_20,
        "ema8" to EMA_8,
        "ema20" to EMA_20,
        "ppo" to PPO,
        "roc" to ROC,
        "rsi" to RSI,
        "williamsR" to WILLIAMS_R,
        "atr" to ATR,
        "sd" to STANDARD_DEVIATION
    )

    init {
        // Setup indicator contexts first
        indicatorContexts = setupIndicatorContexts()

        // Setup CSV writer
        setupCsvWriter()

        // Setup market event handler with this class as listener
        marketEventHandler = ObservableIndicatorCalculationBuilder()
            .withIndicatorContexts(indicatorContexts)
            .withIndicatorContextUpdateListener(this)
            .build()
    }

    private fun setupIndicatorContexts(): IndicatorContexts {
        val contexts = IndicatorContexts.empty().apply {
            add(IndicatorContext.empty(timeFrame))
        }

        val indicatorContext = contexts[timeFrame]

        val closePrice = Indicators.closePrice()
        val typicalPrice = Indicators.typicalPrice()
        val priceVariation = closePrice.ratio()

        // Simple moving averages
        val shortSma = closePrice.sma(8)
        val longSma = closePrice.sma(20)

        // Exponential moving averages
        val shortEma = closePrice.ema(8)
        val longEma = closePrice.ema(20)

        // Other indicators (migrated from original Java implementation)
        val ppo = closePrice.ppo(12, 26)
        val roc = closePrice.roc(100)
        val rsi = closePrice.rsi(14)
        val williamsR = Indicators.williamsR(20)
        val atr = Indicators.atr(20)
        val standardDeviation = closePrice.stddev(14)

        // Register all indicators with the context
        indicatorContext.add(closePrice, CLOSE_PRICE)
        indicatorContext.add(typicalPrice, TYPICAL_PRICE)
        indicatorContext.add(priceVariation, PRICE_VARIATION)
        indicatorContext.add(shortSma, SMA_8)
        indicatorContext.add(longSma, SMA_20)
        indicatorContext.add(shortEma, EMA_8)
        indicatorContext.add(longEma, EMA_20)
        indicatorContext.add(ppo, PPO)
        indicatorContext.add(roc, ROC)
        indicatorContext.add(rsi, RSI)
        indicatorContext.add(williamsR, WILLIAMS_R)
        indicatorContext.add(atr, ATR)
        indicatorContext.add(standardDeviation, STANDARD_DEVIATION)

        return contexts
    }

    private fun setupCsvWriter() {
        try {
            // Ensure target directory exists
            outputFile.parentFile?.mkdirs()
            csvWriter = BufferedWriter(FileWriter(outputFile))
        } catch (e: Exception) {
            println("Error setting up CSV writer: ${e.message}")
            e.printStackTrace()
        }
    }

    override fun onContextUpdate(time: Instant) {
        // Ensure idempotency - only process each time once
        if (time.isAfter(lastUpdateTime)) {
            lastUpdateTime = time

            try {
                if (!isHeaderWritten) {
                    writeHeader()
                    isHeaderWritten = true
                }

                writeDataRow(time)
                csvWriter.flush() // Ensure data is written immediately

            } catch (e: Exception) {
                println("Error writing CSV data at $time: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun writeHeader() {
        csvWriter.apply {
            write("timestamp")
            indicators.keys.forEach { name ->
                write(",$name")
            }
            newLine()
        }
    }

    private fun writeDataRow(time: Instant) {
        csvWriter.apply {
            write(time.toString())

            indicators.forEach { (_, id) ->
                val value = indicatorContexts[timeFrame].getNumericIndicator(id)?.value?.toString() ?: "NaN"
                write(",$value")
            }
            newLine()
        }
    }

    fun processMarketEvent(marketEvent: CandleReceived) {
        marketEventHandler.onCandle(marketEvent)
    }

    override fun close() {
        try {
            csvWriter.close()
            println("CSV file written successfully to: ${outputFile.absolutePath}")
        } catch (e: Exception) {
            println("Error closing CSV writer: ${e.message}")
            e.printStackTrace()
        }
    }

    companion object {
        // Indicator identifications for type-safe access
        private val CLOSE_PRICE = IndicatorIdentification("closePrice")
        private val TYPICAL_PRICE = IndicatorIdentification("typicalPrice")
        private val PRICE_VARIATION = IndicatorIdentification("priceVariation")
        private val SMA_8 = IndicatorIdentification("sma8")
        private val SMA_20 = IndicatorIdentification("sma20")
        private val EMA_8 = IndicatorIdentification("ema8")
        private val EMA_20 = IndicatorIdentification("ema20")
        private val PPO = IndicatorIdentification("ppo")
        private val ROC = IndicatorIdentification("roc")
        private val RSI = IndicatorIdentification("rsi")
        private val WILLIAMS_R = IndicatorIdentification("williamsR")
        private val ATR = IndicatorIdentification("atr")
        private val STANDARD_DEVIATION = IndicatorIdentification("standardDeviation")

        @JvmStatic
        fun main(args: Array<String>) {
            println("Starting Indicators to CSV export using IndicatorContext listeners...")
            // Get market data (modern equivalent of CsvTradesLoader.loadBitstampSeries())
            val marketEvents = MockMarketEventsLoader.loadMarketEvents()

            // Create the CSV exporter with listener
            IndicatorsToCsv().use { csvExporter ->
                // Process each market event - this will trigger the listener
                println("Processing ${marketEvents.size} market events...")
                marketEvents.forEach { marketEvent ->
                    csvExporter.processMarketEvent(marketEvent)
                }
            }

            println("Indicators to CSV export completed!")
        }
    }
}
